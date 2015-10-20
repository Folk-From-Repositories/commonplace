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


    var sql = 'INSERT INTO `commonplace`.`userLocation` (`phone`, `latitude`, `longitude`, `update`) VALUES (?, ?, ?, NOW())' + 'ON DUPLICATE KEY UPDATE `latitude` = ?, `longitude` = ?, `update` = NOW()';

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

// var notiThread;

// function sendUserLocation() {
//     exports.getAllUsersLocation(function(err, phones, locations) {

//         if (err) return;

//         var message = {
//             category: 'GPS Push',
//             moimId: 1,
//             member: locations
//         };

//         GM.sendMessage(phones, message, function(e, o) {
//             if (e) {
//                 console.error(e);
//             }
//         });
//     });
// }

// /**
//  * @depreciated
//  */
// exports.enableGPSNotification = function() {

//     if (!notiThread) {
//         console.log('enableGPSNotification()');

//         notiThread = setInterval(function() {
//             sendUserLocation();
//         }, 5000);
//     } else {
//         console.log('enableGPSNotification() - already enabled.');
//     }
// }

// /**
//  * @depreciated
//  */
// exports.disableGPSNotification = function() {

//     if (notiThread) {
//         console.log('disableGPSNotification()');
//         clearInterval(notiThread);
//         notiThread = undefined;
//     } else {
//         console.log('disableGPSNotification() - already disabled.');
//     }
// }