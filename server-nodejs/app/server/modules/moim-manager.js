var AM = require('./account-manager');
var GCM = require('./gcm-sender');
var connection = require('./database-connector').connection;
var utils = require('./utils');
var schemaValidate = require('jsonschema').validate;

// load json schema for validate request parameter
var moimSchema = require('../schema/moim-schema.json');

/**
 * 신규 모임 정보 정보
 */
exports.createNewMoim = function(data, callback) {
    var dataValidateResult = schemaValidate(data, moimSchema);

    // TODO 상세한 에러 원인 전달 - dataValidateResult.errors[x].argument

    if (dataValidateResult.errors.length > 0) {
        callback('insufficiency-form-data');
        return;
    }

    var moimObj = {
        title: data.title,
        dateTime: data.dateTime,
        locationName: data.locationName,
        locationImageUrl: data.locationImageUrl,
        locationLat: data.locationLat,
        locationLon: data.locationLon,
        locationPhone: data.locationPhone,
        locationDesc: data.locationDesc,
        owner: utils.phoneToDbFormat(data.owner)
    };

    connection.beginTransaction(function(err) {
        if (err) {
            console.error('Fail to begin transction.');
            throw err;
        }

        connection.query('INSERT INTO `commonplace`.`moim` SET ?', moimObj, function(err, result) {
            if (err) {
                console.error('신규 모임 생성 실패', err);
                connection.rollback(function() {
                    console.error('rollback error');
                    throw err;
                });
            } // if err

            var moimId = result.insertId;
            var userMoimObj = [
                [moimObj.owner, moimId]
            ];

            for (var i = 0; i < data.member.length; i++) {
                userMoimObj.push([data.member[i], moimId]);
            }

            connection.query('INSERT INTO `commonplace`.`userMoim` (phone, moimId) VALUES ?', [userMoimObj], function(err, result) {
                if (err) {
                    console.error('사용자 모임정보 등록 실패', err);
                    connection.rollback(function() {
                        console.error('rollback error');
                        throw err;
                    });
                } // if err

                // user 조회 (등록 여부 = token 존재) 후 리턴
                AM.getGcmTokens(data.member, function(e, r) {
                    if (err) {
                        console.error('신규 모임 정보 생성 후 사용자 GCM 정보 조회 에러', e);
                        connection.rollback(function() {
                            console.error('rollback error');
                            throw err;
                        });
                    }

                    var nonUsers = data.member.slice(0); // clone
                    var users = [];

                    for (var j = 0; j < r.length; j++) {
                        users.push(r[j].phone);

                        var index = nonUsers.indexOf(r[j].phone);
                        if (index >= 0) {
                            nonUsers.splice(index, 1);
                        }
                    }

                    var httpRes = {
                        sms: nonUsers,
                        moimId: moimId
                    };

                    // GCM 전송
                    if (users.length > 0) {
                        GCM.sendMessage(users, {
                            category: 'invitation',
                            moimId: moimId
                        }, function(e, o) {
                            if (e) {
                                console.error('신규 모임 생성 완료 후 GCM 전송 실패', e);
                                connection.rollback(function() {
                                    console.error('rollback error');
                                    throw err;
                                });
                            } else {
                                console.log('신규 모임 생성 완료 및 GCM 전송 완료');
                                connection.commit(function(err) {
                                    if (err) {
                                        console.error('모임 정보 생성을 위한 트랜잭션 커밋 에러', err);
                                        connection.rollback(function() {
                                            console.error('rollback error');
                                            throw err;
                                        });
                                    } else {
                                        callback(null, httpRes);
                                    }
                                }); // commit
                            }
                        });
                    } else {
                        console.log('신규 모임 생성 완료');
                        connection.commit(function(err) {
                            if (err) {
                                console.error('모임 정보 생성을 위한 트랜잭션 커밋 에러', err);
                                connection.rollback(function() {
                                    console.error('rollback error');
                                    throw err;
                                });
                            } else {
                                callback(null, httpRes);
                            }
                        }); // commit
                    } // GCM 전송
                }); // select user for gcm send
            }); // insert into userMoim
        }); // inset into moim
    }); // begin trnsaction
}

/**
 * 나의 모임 정보 조회
 */
exports.getMyMoims = function(phone, callback) {
    // validate data
    if (!phone) {
        callback('error-phone-number');
        return;
    }

    var sql = 'SELECT * FROM `commonplace`.`userMoim` WHERE `phone` = ?';

    connection.query(sql, phone, function(err, result) {
        if (err) return callback(err);

        if (result.length === 0) {
            return callback(null, []);
        }

        var moimIds = [];

        for (var i = 0; i < result.length; i++) {
            moimIds.push(result[i].moimId);
        }

        exports.getDetails(moimIds, callback);
    });
}

/**
 * 모임 상세 정보 조회
 */
exports.getDetails = function(moimIds, callback) {
    if (Object.prototype.toString.call(moimIds) !== '[object Array]') {
        moimIds = [moimIds];
    }

    var moimSql = 'SELECT * FROM `commonplace`.`moim` WHERE `id` IN (' + connection.escape(moimIds) + ')';
    var userMoimSql = 'SELECT * FROM `commonplace`.`userMoim` WHERE `moimId` IN (' + connection.escape(moimIds) + ')';

    connection.query(moimSql, function(err, moim) {
        if (err) {
            callback('Moim 정보 조회 실패');
            return;
        } else {
            connection.query(userMoimSql, function(err, userMoim) {
                if (err) {
                    callback('Moim 참여자 정보 조회 실패');
                    return;
                } else {
                    var memberInMoim = {};
                    var i, moimId;

                    // make member list per moim
                    for (i = 0; i < userMoim.length; i++) {
                        moimId = userMoim[i].moimId;

                        if (!memberInMoim[moimId]) {
                            memberInMoim[moimId] = [];
                        }

                        memberInMoim[moimId].push(userMoim[i].phone);
                    }

                    // add member into moim object
                    for (i = 0; i < moim.length; i++) {
                        moimId = moim[i].id;
                        moim[i].member = memberInMoim[moimId];
                    }

                    callback(null, moim);
                }
            });
        }
    });
}

/**
 * 모임 삭제 (owner만 삭제 가능, 참석자가 요청시 참여 정보에서만 삭제)
 */
exports.deleteMoim = function (moimId, phone, callback) {
    phone = utils.phoneToDbFormat(phone);
    if (isNaN(moimId)) {
        return callback('invalid-moim-id');
    } else if (typeof phone !== 'string') {
        return callback('invalid-phone-number');
    }

    var sql = 'SELECT * FROM `commonplace`.`moim` WHERE `id` = ' + connection.escape(moimId) + ' AND `owner` = ' + connection.escape(phone);

    connection.query(sql, function (err, result) {
        if (err) return callback('database-error');

        if (result.length > 0) { // owner 가 모임 삭제
            var moimDeleteSql = 'DELETE FROM `commonplace`.`moim` WHERE `id` = ' + connection.escape(moimId) + ' AND `owner` = ' + connection.escape(phone);
            var userMoimDeleteSql = 'DELETE FROM `commonplace`.`userMoim` WHERE `moimId` = ' + connection.escape(moimId);

            connection.beginTransaction(function (err) {
                if (err) {
                    return callback('트랜잭션 생성 실패');
                }

                connection.query(userMoimDeleteSql, function (err, userMoimDeleteResult) {
                    if (err) {
                        connection.rollback();
                        return callback('모임 참여자 정보 삭제 실패');
                    }

                    connection.query(moimDeleteSql, function (err, moimDeleteResult) {
                        if (err) {
                            connection.rollback();
                            return callback('모임 삭제 실패');
                        }

                        connection.commit(function(err) {
                            if (err) {
                                console.error('트랜잭션 커밋 에러', err);
                            } else {
                                callback(null, '모임 삭제 완료');
                            }
                        }); // commit                        
                        
                    });                    
                });
            });
        } else { // 참여자 정보만 삭제
            var memberDeleteSql = 'DELETE FROM `commonplace`.`userMoim` WHERE `moimId` = ' + connection.escape(moimId) + ' AND `phone` = ' + connection.escape(phone);
            connection.query(memberDeleteSql, function (err, deleteResult) {
                if (err) callback('모임 참여 정보 삭제 실패');
                else callback(null, '모임 참여 정보 삭제 성공');
            });
        }
    });


}

/**
 * 모임 참여자의 위치 정보 broadcast 기능 활성화
 */
exports.enableLocationBroadcast = function(moimId, callback) {
    if (isNaN(moimId)) {
        return callback('invalid-moim-id');
    }

    var sql = 'UPDATE `commonplace`.`moim` SET `broadcast` = 1 WHERE id = '+ connection.escape(moimId);

    connection.query(sql, function(err, result) {
        if (err) console.error(err);

        callback(err, err ? false : true);
    });
}
/**
 * 모임 참여자의 위치 정보 broadcast 기능 비활성화
 */
exports.disableLocationBroadcast = function(moimId, callback) {
    if (isNaN(moimId)) {
        return callback('invalid-moim-id');
    }

    var sql = 'UPDATE `commonplace`.`moim` SET `broadcast` = 0 WHERE id = '+ connection.escape(moimId);

    connection.query(sql, function(err, result) {
        if (err) console.error(err);

        callback(err, err ? false : true);
    });
}

/**
 * Location 정보 Notification이 활성화 된 모임 정보 조회
 */
exports.getAvailableLocationBroadcast = function(callback) {

}
