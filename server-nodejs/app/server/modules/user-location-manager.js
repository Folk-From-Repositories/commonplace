var connection 	= require('./database-connector').connection;
var utils 		= require('./utils');

/**
 * 사용자 위치 정보 등록
 */
exports.update = function(data, callback)
{
	var sql = 'INSERT INTO `commonplace`.`userLocation` (`phone`, `latitude`, `longitude`, `update`) VALUES (?, ?, ?, NOW())'
			+ 'ON DUPLICATE KEY UPDATE `latitude` = ?, `longitude` = ?, `update` = NOW()';

	var phone = utils.phoneToDbFormat(data.phone);
	var latitude = data.latitude;
	var longitude = data.longitude;

	if(typeof phone !== 'string' || typeof latitude !== 'string' || typeof longitude !== 'string') {
		callback('invalid-form-data');
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
exports.gets = function(phones, callback)
{
	// validate data
	if (!phones) { callback('error-phone-number'); return; }

	// Make Array if there is only one number.
	if (Object.prototype.toString.call( phones ) !== '[object Array]' ) {
		phones = [phones];
	}

	for (var index in phones) {
		phones[index] = utils.phoneToDbFormat(phones[index]);

		if (!utils.isOnlyNumber(phones[index])) {
			callback('invalid-phone-number-format'); return;
		}
	}

	var sql = 'SELECT * FROM `commonplace`.`userLocation` WHERE `phone` IN (' + connection.escape(phones) + ')';

	connection.query(sql, function(err, result) {
		callback(err, result);
	});
}

function sendUserLocation() {
	var AM = require('./account-manager');
	var GM = require('./gcm-sender');

	// user 조회
	AM.getAllRecords(function(err, users) {
		if (err) { console.error(err); return; }

		var phones = [];

		for (var i = 0; i < users.length; i++) {
			phones.push(users[i].phone);
		}

		// userLocation table 전체 조회
		exports.gets(phones, function(err, locations) {
			if (err) { console.error(err); return; }

			var notification = {
				category : 'GPS Push',
				moimId 	 : 1,
				member 	 : locations
			};

			GM.sendMessage(phones, notification, function(e, o) {
				if (e) {
					console.error(e);
				}
			});
		});
	});

	// 모든 유저에게 userLocation PUSH
}
// 5초마다 PUSH
setInterval(function() {
    sendUserLocation();
}, 5000);
