var AM = require('./account-manager');
var GCM = require('./gcm-sender');
var connection = require('./database-connector').connection;
var utils = require('./utils');
/**
 * 신규 모임 정보 정보
 */
exports.createNewMoim = function(data, callback) {
    if (typeof data.title !== 'string' || typeof data.locationName !== 'string' || typeof data.locationImageUrl !== 'string' ||
        typeof data.locationLat !== 'string' || typeof data.locationLon !== 'string' || typeof data.locationPhone !== 'string' ||
        typeof data.owner !== 'string' || Object.prototype.toString.call(data.member) !== '[object Array]') {

        callback('insufficiency-form-data');
        return;
    }

    var moimObj = {
        title: data.title,
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

    var sql = 'SELECT * FROM `commonplace`.`moim` WHERE `id` IN (' + connection.escape(moimIds) + ')';

    connection.query(sql, function(err, result) {
        callback(err, result);
    });
}