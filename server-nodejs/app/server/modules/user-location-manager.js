var AM = require('./account-manager');
var GM = require('./gcm-sender');
var connection = require('./database-connector').connection;
var utils = require('./utils');
var schemaValidate = require('jsonschema').validate;

// load json schema for validate request parameter
var locationSchema = require('../schema/user-location-schema.json');

/**
 * 사용자 위치 정보 등록
 */
exports.update = function(data, callback) {


    var sql = 'INSERT INTO `commonplace`.`userLocation` (`phone`, `latitude`, `longitude`, `crawled`,`update`) VALUES (?, ?, ?, 0, NOW())' + 'ON DUPLICATE KEY UPDATE `latitude` = ?, `longitude` = ?, `crawled` = 0, `update` = NOW()';

    var phone = utils.phoneToDbFormat(data.phone);
    var latitude = data.latitude;
    var longitude = data.longitude;

    var dataValidateResult = schemaValidate(data, locationSchema);

    // TODO 상세한 에러 원인 전달 - dataValidateResult.errors[x].argument

    if (dataValidateResult.errors.length > 0) {
        callback('insufficiency-form-data');
        return;
    }

    connection.query(sql, [phone, latitude, longitude, latitude, longitude], function(err, result) {
        if (err) {
            console.error(err);
            callback('server error');
        } else {
            callback(null, result);
        }
    });
}

/**
 * 사용자 위치 정보 조회
 */
exports.gets = function(phones, callback) {
    // validate data
    if (!phones) {
        callback('error-phone-number');
        return;
    }

    // Make Array if there is only one number.
    if (Object.prototype.toString.call(phones) !== '[object Array]') {
        phones = [phones];
    }

    for (var index in phones) {
        phones[index] = utils.phoneToDbFormat(phones[index]);

        if (!utils.isOnlyNumber(phones[index])) {
            callback('invalid-phone-number-format');
            return;
        }
    }

    var sql = 'SELECT a.phone, a.name, l.latitude, l.longitude '
            + 'FROM user a, userLocation l '
            + 'WHERE a.phone = l.phone '
            + 'AND a.phone IN (' + connection.escape(phones) + ')';

    connection.query(sql, function(err, result) {
        callback(err, result);
    });
}

// 전체 사용자 GPS 정보 조회
exports.getAllUsersLocation = function(callback) {
    // user 조회
    AM.getAllRecords(function(err, users) {
        if (err) {
            console.error(err);
            return;
        }

        var phones = [];

        for (var i = 0; i < users.length; i++) {
            phones.push(users[i].phone);
        }

        // userLocation table 전체 조회
        exports.gets(phones, function(err, locations) {
            if (err) {
                console.error(err);
                return;
            }

            callback(null, phones, locations);
        });
    });
}


/*
 * 위치 정보가 변경된 사용자 정보 조회 (참여 모임 포함)
 * 한번 조회된 정보는 다시 조회도록 crawled 필드 업데이트
 */ 
exports.findNewLocationMember = function(callback) {
    var selectSql = 'SELECT um.moimId, um.phone, u.name, ul.latitude, ul.longitude '
            + 'FROM `commonplace`.`userMoim` um, '
            + '     `commonplace`.`moim` m, '
            + '     `commonplace`.`user` u, '
            + '     `commonplace`.`userLocation` ul '
            + 'WHERE um.moimId = m.id '
            + 'AND um.phone = ul.phone '
            + 'AND um.phone = u.phone '
            + 'AND m.`broadcast` = 1 '
            + 'AND ul.`crawled` = 0 '
            + 'ORDER BY um.moimId, um.phone ';

    connection.query(selectSql, function(err, result) {
        if (err) return callback(err);

        if (result) {
            var phones = [];

            for (var i = 0; i < result.length; i++) {
                if (phones.indexOf(result[i].phone) < 0) {
                    phones.push(result[i].phone);
                }
            }

            if (phones.length < 1) {
                return callback('No changed user');
            }

            var updateSql = 'UPDATE `commonplace`.`userLocation` SET `crawled` = 1 WHERE `phone` IN (' + connection.escape(phones) + ')';

            connection.query(updateSql);

            callback(null, result);

        } else {
            callback('No result');
        }

    });
};


