var AM 			= require('./account-manager');
var connection 	= require('./database-connector').connection;
var utils 		= require('./utils');
/**
 * 신규 모임 정보 정보
 */
exports.createNewMoim = function(data, callback)
{
	if (typeof data.title !== 'string' || typeof data.locationName !== 'string' || typeof data.locationImageUrl !== 'string' ||
		typeof data.locationLat !== 'string' || typeof data.locationLon !== 'string' || typeof data.locationPhone !== 'string' ||
		typeof data.owner !== 'string' || Object.prototype.toString.call( data.member ) !== '[object Array]') {

		callback('insufficiency-form-data');
		return;
	}

	var moimObj = {
		title 				: data.title,
		locationName 		: data.locationName,
		locationImageUrl 	: data.locationImageUrl,
		locationLat 		: data.locationLat,
		locationLon 		: data.locationLon,
		locationPhone 		: data.locationPhone,
		locationDesc 		: data.locationDesc,
		owner 				: utils.phoneToDbFormat(data.owner)
	};

	connection.beginTransaction(function(err) {
		if (err) {
			console.error('Fail to begin transction.');
			throw err;
		}

		connection.query('INSERT INTO `commonplace`.`moim` SET ?', moimObj, function (err, result) {
			if (err) {
				console.error(err);
				connection.rollback(function () {
					console.error('rollback error');
					throw err;
				});
			}// if err

			var moimId = result.insertId;
			var userMoimObj = [[moimObj.owner, moimId]];

			for (var i = 0; i < data.member.length; i++) {
				userMoimObj.push([data.member[i], moimId]);
			}

			connection.query('INSERT INTO `commonplace`.`userMoim` (phone, moimId) VALUES ?', [userMoimObj], function (err, result) {
				if (err) {
					console.error(err);
					connection.rollback(function () {
						console.error('rollback error');
						throw err;
					});
				}// if err

				connection.commit(function (err) {
					if (err) {
						console.error(err);
						connection.rollback(function () {
							console.error('rollback error');
							throw err;
						});
					}// if err

					// user 조회 (등록 여부 = token 존재) 후 리턴
					AM.getGcmTokens(data.member, function(e, r) {
						if (err) {
							console.error(e);
							throw e;
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

						callback(null, {nonUsers: nonUsers, users: users, moimId: moimId});
					});
				});// commit
			});// insert into userMoim
		});// inset into moim
	}); // begin trnsaction
}

/**
 * 나의 모임 정보 조회
 */
exports.getMyMoims = function(phone, callback)
{
	// validate data
	if (!phone) { callback('error-phone-number'); return; }

	phone = utils.phoneToDbFormat()

	var sql = 'SELECT * FROM `commonplace`.`moim` WHERE `owner` = ?';

	connection.query(sql, phone, function(err, result) {
		callback(err, result);
	});
}

/**
 * 모임 상세 정보 조회
 */
exports.getDetails = function(moimIds, callback)
{
	// validate data
	if (!moimId) { callback('error-moim-id'); return; }

	if (Object.prototype.toString.call( moimIds ) !== '[object Array]') {
		moimIds = [moimIds];
	}

	var sql = 'SELECT * FROM `commonplace`.`moim` WHERE `id` IN (' + ')';

	connection.query(sql, moimId, function(err, result) {
		callback(err, result);
	});
}

