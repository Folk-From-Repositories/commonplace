
var crypto 		= require('crypto');
var moment 		= require('moment');
var mysql 		= require('mysql');
var dbInfo 		= require(__dirname + '/../conf/database.json');

/* establish the database connection */
var connection;

function makeConnection() {
	connection = mysql.createConnection(dbInfo);

	connection.connect(function(err) {
		if (err) {
			console.error('Error when connecting to db:', err);
			setTimeout(makeConnection, 3000);
		} else {
			console.log('Establish db connection.')
		}
	});

	connection.on('error', function(err) {
		console.error('Database error', err);

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
	var sql = 'INSERT INTO `commonplace`.`user` SET ?';

	findByUserId(newData.user, function(err, o) {
		if (err) {
			throw err;
		} else if (typeof o === 'object') {
			callback('username-taken');
		} else {
			findByEmail(newData.email, function(err, o) {
				if (err) {
					throw err;
				} else if (typeof o === 'object') {
					callback('email-taken');
				} else {
					saltAndHash(newData.pass, function(hash){
						newData.pass = hash;
						connection.query(sql, newData, function(err, result) {
							if (err) {
								callback('server error');
							} else {
								callback();
							}
						});
					});
				}
			});
		}
	});
}

exports.updateAccount = function(newData, callback)
{
	var sql = 'UPDATE `commonplace`.`user`' +
				'SET' +
				'`name` = ?,' +
				'`email` = ?,' +
				'`pass` = ?,' +
				'`country` = ?,' +
				'`updated_dttm` = NOW()' +
				'WHERE `user` = ?';

	findByUserId(newData.user, function(e, o) {
		if (e || typeof o !== 'object') {
			callback(e);
		} else {
			o.name = newData.name;
			o.email = newData.email;
			o.country = newData.country;

			if (typeof newData.pass === 'string'){
				saltAndHash(newData.pass, function(hash) {
					o.pass = hash;
					connection.query(sql, [o.name, o.email, hash, o.country, o.user], function(err, result) {
						if (err) {
							console.log(err);
							callback('server error');
						} else {
							callback(null, o);
						}
					});
				});
			} else {
				connection.query(sql, [o.name, o.email, o.pass, o.country, o.user], function(err, result) {
					if (err) {
						console.log(err);
						callback('server error');
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
				'`updated_dttm` = NOW()' +
				'WHERE `user` = ?';
	findByEmail(email, function(e, o) {
		if (e || typeof o !== 'object') {
			callback(e);
		} else {
			saltAndHash(newPass, function(hash){
		        o.pass = hash;
				connection.query(sql, [o.pass, o.user], function(err, result) {
					if (err) {
						console.log(err);
						callback('server error');
					} else {
						callback(null, o);
					}
				});
			});
		}
	});
}

// /* account lookup methods */

exports.deleteAccount = function(sessionUser, callback)
{
	console.log('deleteAccount : ' + sessionUser.user + ' / ' + sessionUser.pass);

	var sql = 'DELETE FROM `commonplace`.`user` WHERE `user` = ? AND `pass` = ?';

	connection.query(sql, [sessionUser.user, sessionUser.pass], function(err, result) {
		console.dir(err,result);

		if (err || result.affectedRows != 1) {
			console.log(err);
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
	var columns = ['user', 'name', 'email', 'country', 'group_cd', 'os', 'gcm_token', 'phone_number', 'updated_dttm', 'creation_dttm'];

	connection.query(sql, [columns], function(err, result) {
		callback(err, result);
	});
};

// exports.delAllRecords = function(callback)
// {
// 	accounts.remove({}, callback); // reset accounts collection for testing //
// }

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


saltAndHash('111111', function(hash) {console.log(hash)});