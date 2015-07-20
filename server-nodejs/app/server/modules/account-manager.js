
var crypto 		= require('crypto');
var moment 		= require('moment');
var mysql 		= require('mysql');
var dbInfo 		= require(__dirname + '/../conf/database.json');

/* establish the database connection */
var connection;
var tag = '[account-manager.js] ';

function makeConnection() {
	connection = mysql.createConnection(dbInfo);

	connection.connect(function(err) {
		if (err) {
			console.error(tag + 'Error when connecting to db:', err);
			setTimeout(makeConnection, 3000);
		} else {
			console.log(tag + 'Establish db connection.')
		}
	});

	connection.on('error', function(err) {
		console.error(tag + 'Database error', err);

		if (err.code === 'PROTOCOL_CONNECTION_LOST') {
			makeConnection();
		} else {
			throw err;
		}
	});
}

makeConnection();

/* login validation methods */

exports.autoLogin = function(user, pass, callback)
{
	var sql = "SELECT * FROM `commonplace`.`user` WHERE user = ? AND pass = ?";

	connection.query({
		sql: sql,
		values: [user, pass]
	}, function(err, rows, fields) {
		if (err) throw err;

		if (rows && typeof rows[0] === 'object') {
			callback(rows[0]);
		} else {
			callback(null);
		}
	});
}

exports.manualLogin = function(user, pass, callback)
{
	findByUserId(user, function(err, o) {
		if (err) {
			throw err;
		} else if (typeof o !== 'object') {
			callback('invalid-user');
		} else {
			validatePassword(pass, o.pass, function(err, res) {
				if (res){
					callback(null, o);
				}	else{
					callback('invalid-password');
				}
			});
		}
	});
}

/* record insertion, update & deletion methods */

exports.addNewAccount = function(newData, callback)
{

	console.log(tag + 'add new account from Web', newData.user);

	if (!newData.user || !newData.phone || !newData.email || !newData.pass) {
		callback('insufficient-params'); return;
	}

	var sql = 'INSERT INTO `commonplace`.`user` SET ?';

	findByphone(newData.phone, function(err, o) {
		if (err) {
			callback('server-error'); return;
		} else if (typeof o === 'object') {
			callback('phone-number-taken'); return;
		}

		findByUserId(newData.user, function(err, o) {
			if (err) {
				callback(err); return;
			} else if (typeof o === 'object') {
				callback('username-taken'); return;
			} else {
				findByEmail(newData.email, function(err, o) {
					if (err) {
						throw err;
					} else if (typeof o === 'object') {
						callback('email-taken');
					} else {
						saltAndHash(newData.pass, function(hash){
							newData.pass = hash;
							newData.create = moment().format('YYYY-MM-DD HH:mm:ss');
							connection.query(sql, newData, function(err, result) {
								if (err) {
									callback('server-error');
								} else {
									callback();
								}
							});
						});
					}
				});
			}
		});

	});
}

exports.updateAccount = function(newData, callback)
{
	var sql = 'UPDATE `commonplace`.`user`' +
				'SET' +
				'`name` = ?,' +
				'`email` = ?,' +
				'`pass` = ?,' +
				'`token` = ?,' +
				'`country` = ?,' +
				'`update` = NOW()' +
				'WHERE `user` = ?';

	console.log(tag + 'update account', newData.user);

	findByUserId(newData.user, function(e, o) {
		if (e || typeof o !== 'object') {
			callback(e); return;
		} else {
			o.name = newData.name;
			o.email = newData.email;
			o.country = newData.country;
			o.token = newData.token;

			if (typeof newData.pass === 'string'){
				saltAndHash(newData.pass, function(hash) {
					o.pass = hash;
					connection.query(sql, [o.name, o.email, hash, o.token, o.country, o.user], function(err, result) {
						if (err) {
							console.error(err);
							callback('server-error');
						} else {
							callback(null, o);
						}
					});
				});
			} else {
				connection.query(sql, [o.name, o.email, o.pass, o.country, o.user], function(err, result) {
					if (err) {
						console.error(err);
						callback('server-error');
					} else {
						callback(null, o);
					}
				});
			}
		}
	});
}

exports.updatePassword = function(email, newPass, callback)
{
	var sql = 'UPDATE `commonplace`.`user`' +
				'SET' +
				'`pass` = ?,' +
				'`update` = NOW()' +
				'WHERE `user` = ?';
	findByEmail(email, function(e, o) {
		if (e || typeof o !== 'object') {
			callback(e);
		} else {
			saltAndHash(newPass, function(hash){
		        o.pass = hash;
				connection.query(sql, [o.pass, o.user], function(err, result) {
					if (err) {
						console.error(err);
						callback('server error');
					} else {
						callback(null, o);
					}
				});
			});
		}
	});
}

exports.registDeviceInfo = function(data, callback) {
	var phone = phoneToDbFormat(data.phone);
	var token = data.token;

	// validate data
	if (!phone) { callback('error-phone-number'); return; }

	if (!token) { callback('needed-gcm-token'); return;	}

	if (!isOnlyNumber(phone)) { callback('invalid-phone-number-format'); return; }

	// find with phone number
	var insertSql = 'INSERT INTO `commonplace`.`user` (gcm_token, phone) VALUES (?, ?)';
	var updateSql = 'UPDATE `commonplace`.`user` SET token = ?, update = NOW() WHERE phone = ?';

	findByphone(phone, function(err, o) {
		if (err) { callback(err); return; }

		var sql;

		if (o) {
			// existing phone number. Do token update
			connection.query(updateSql, [token, phone], function(err, result) {
				if (err) {
					console.error(err);
					callback('server error');
				} else {
					callback(null, result);
				}
			});
		} else {
			// add new account with phone number
			connection.query(insertSql, [token, phone], function(err, result) {
				if (err) {
					console.error(err);
					callback('server error');
				} else {
					callback(null, result);
				}
			});
		}
	});
}

exports.getGcmTokens = function(phones, callback) {
	// validate data
	if (!phones) { callback('error-phone-number'); return; }

	// Make Array if there is only one number.
	if (Object.prototype.toString.call( phones ) !== '[object Array]' ) {
		phones = [phones];
	}

	for (var index in phones) {
		phones[index] = phoneToDbFormat(phones[index]);

		if (!isOnlyNumber(phones[index])) {
			callback('invalid-phone-number-format'); return;
		}
	}

	var sql = 'SELECT phone_number, gcm_token FROM `commonplace`.`user` WHERE phone_number IN (' + connection.escape(phones) + ')';

	connection.query(sql, function(err, result) {
		callback(err, result);
	});
}

// /* account lookup methods */

exports.deleteAccount = function(sessionUser, callback)
{
	console.log(tag + 'deleteAccount : ' + sessionUser.user);

	var sql = 'DELETE FROM `commonplace`.`user` WHERE `user` = ? AND `pass` = ?';

	connection.query(sql, [sessionUser.user, sessionUser.pass], function(err, result) {

		if (err || result.affectedRows != 1) {
			console.error(err);
			callback('server error');
		} else {
			callback(null);
		}
	});
}

exports.getAccountByEmail = function(email, callback)
{
	findByEmail(email, function(e, o){ callback(o); });
}

exports.validateResetLink = function(email, passHash, callback)
{
	var sql = "SELECT * FROM `commonplace`.`user` WHERE email = ? AND pass = ?";

	connection.query({
		sql: sql,
		values: [email, passHash]
	}, function(err, rows, fields) {
		if (err) throw err;

		if (rows && typeof rows[0] === 'object') {
			callback('ok');
		} else {
			callback(null);
		}
	});
}

exports.getAllRecords = function(callback)
{
	var sql = 'SELECT ?? FROM `commonplace`.`user`';
	var columns = ['user', 'name', 'email', 'country', 'group', 'os', 'token', 'phone', 'update', 'create'];

	connection.query(sql, [columns], function(err, result) {
		callback(err, result);
	});
};


/* private encryption & validation methods */

var generateSalt = function()
{
	var set = '0123456789abcdefghijklmnopqurstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ';
	var salt = '';
	for (var i = 0; i < 10; i++) {
		var p = Math.floor(Math.random() * set.length);
		salt += set[p];
	}
	return salt;
}

var md5 = function(str) {
	return crypto.createHash('md5').update(str).digest('hex');
}

var saltAndHash = function(pass, callback)
{
	var salt = generateSalt();
	callback(salt + md5(pass + salt));
}

var validatePassword = function(plainPass, hashedPass, callback)
{
	var salt = hashedPass.substr(0, 10);
	var validHash = salt + md5(plainPass + salt);
	callback(null, hashedPass === validHash);
}

/* auxiliary methods */

var findByUserId = function(user, callback)
{
	var sql = "SELECT * FROM `commonplace`.`user` WHERE user = ?";

	connection.query({
		sql : sql,
		values: [user]
	}, function(err, rows, fields) {
		callback(err, rows[0]);
	});
};

var findByEmail = function(email, callback)
{
	var sql = "SELECT * FROM `commonplace`.`user` WHERE email = ?";

	connection.query({
		sql : sql,
  		values: [email]
	}, function(err, rows, fields) {
		callback(err, rows[0]);
	});
};

var findByphone = function(phone, callback)
{
	var sql = "SELECT * FROM `commonplace`.`user` WHERE phone = ?";

	connection.query({
		sql : sql,
  		values: [phone]
	}, function(err, rows, fields) {
		callback(err, rows[0]);
	});
};


var phoneToDbFormat = function(phone) {

	if (phone) {
		phone = phone.replace(/[-_\W]/g, "");
	}

	return phone;
};

var isOnlyNumber = function(str) {
	var isNumber = true;
	for (var i=0; i < str.length; i++) {
		if (isNaN(str[i])) {
			isNumber = false;
			break;
		}
	}
	return isNumber;
}