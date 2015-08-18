
var CT = require('./modules/country-list');
var AM = require('./modules/account-manager');
var EM = require('./modules/email-dispatcher');
var GCM = require('./modules/gcm-sender');
var tag = '[routes.js] ';

module.exports = function(app) {

// main login page //

	app.get('/', function(req, res){
	// check if the user's credentials are saved in a cookie //
		if (req.cookies.user == undefined || req.cookies.pass == undefined){
			res.render('login', { title: 'Common Place - Please Login To Your Account' });
		} else {
	// attempt automatic login //
			AM.autoLogin(req.cookies.user, req.cookies.pass, function(o){
				if (o != null){
					console.log('[routes.js] Auto Login - ' + req.cookies.user);
					req.session.user = o;

					if (o.group_cd === 9) {
						res.redirect('/user/print');
					} else {
						res.redirect('/user/update');
					}
				} else {
					res.render('login', { title: 'Common Place - Please Login To Your Account' });
				}
			});
		}
	});
	
	app.post('/', function(req, res){
		AM.manualLogin(req.body['user'], req.body['pass'], function(e, o){
			if (!o){
				res.status(400).send(e);
			} else {
				req.session.user = o;
				if (req.body['remember-me'] == 'true'){
					res.cookie('user', o.user, { maxAge: 900000 });
					res.cookie('pass', o.pass, { maxAge: 900000 });
				}

				console.log('[routes.js] Manual Login - ' + req.body['user']);

				if (o.group_cd === 9) {
					res.redirect('/user/print');
				} else {
					res.redirect('/user/update');
				}
			}
		});
	});
	
// logged-in user homepage //
	
	app.get('/user/update', function(req, res) {
		if (req.session.user == null){
	// if user is not logged-in redirect back to login page //
			res.redirect('/');
		} else {
			res.render('user', {
				title : 'Control Panel',
				countries : CT,
				udata : req.session.user
			});
		}
	});
	
	app.post('/user/update', function(req, res){
		if (req.body['user'] != undefined) {

			AM.updateAccount({
				user 	: req.body['user'],
				name 	: req.body['name'],
				email 	: req.body['email'],
				token 	: req.body['token'],
				phone 	: req.body['phone'],
				pass	: req.body['pass'],
				country : req.body['country']
			}, function(e, o){
				if (e){
					res.status(400).send('error-updating-account');
				} else {
					req.session.user = o;
			// update the user's login cookies if they exists //
					if (req.cookies.user != undefined && req.cookies.pass != undefined){
						res.cookie('user', o.user, { maxAge: 900000 });
						res.cookie('pass', o.pass, { maxAge: 900000 });	
					}
					res.status(200).send('ok');
				}
			});
		} else if (req.body['logout'] == 'true'){
			res.clearCookie('user');
			res.clearCookie('pass');
			req.session.destroy(function(e){ res.status(200).send('ok'); });
		}
	});
	
// creating new accounts //
	
	app.get('/user/signup', function(req, res) {
		res.render('signup', {  title: 'Signup', countries : CT });
	});
	
	app.post('/user/signup', function(req, res){
		AM.addNewAccount({
			name 	: req.body['name'],
			email 	: req.body['email'],
			user 	: req.body['user'],
			pass 	: req.body['pass'],
			phone 	: req.body['phone'],
			token 	: req.body['token'],
			country : req.body['country']
		}, function(e){
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send('ok');
			}
		});
	});

// password reset //

	app.post('/user/lost-password', function(req, res){
	// look up the user's account via their email //
		AM.getAccountByEmail(req.body['email'], function(o){
			if (o){
				EM.dispatchResetPasswordLink(o, function(e, m){
				// this callback takes a moment to return //
				// TODO add an ajax loader to give user feedback //
					if (!e){
						res.status(200).send('ok');
					} else {
						for (k in e) console.log('ERROR : ', k, e[k]);
						res.status(400).send('unable to dispatch password reset');
					}
				});
			} else {
				res.status(400).send('email-not-found');
			}
		});
	});

	app.get('/user/reset-password', function(req, res) {
		var email = req.query["e"];
		var passH = req.query["p"];
		AM.validateResetLink(email, passH, function(e){
			if (e != 'ok'){
				res.redirect('/');
			} else {
	// save the user's email in a session instead of sending to the client //
				req.session.reset = { email:email, passHash:passH };
				res.render('reset', { title : 'Reset Password' });
			}
		})
	});
	
	app.post('/user/reset-password', function(req, res) {
		var nPass = req.body['pass'];
	// retrieve the user's email from the session to lookup their account and reset password //
		var email = req.session.reset.email;
	// destory the session immediately after retrieving the stored email //
		req.session.destroy();
		AM.updatePassword(email, nPass, function(e, o){
			if (o){
				res.status(200).send('ok');
			} else {
				res.status(400).send('unable to update password');
			}
		})
	});
	
// view & delete accounts //
	
	app.get('/user/print', function(req, res) {
		AM.getAllRecords( function(e, accounts){
			res.render('print', { title : 'Account List', accts : accounts });
		})
	});
	
	app.post('/user/delete', function(req, res){
		AM.deleteAccount(req.session.user, function(e, obj){
			if (!e){
				res.clearCookie('user');
				res.clearCookie('pass');
				req.session.destroy(function(e){ res.status(200).send('ok'); });
			} else {
				res.status(400).send('record not found');
			}
		});
	});



// common place api //

	// regist gcm token //
	app.post('/commonplace/gcm/regist', function(req, res) {
		AM.registDeviceInfo({
			phone: req.body['phone'],
			token: req.body['token']
		}, function(e) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send('ok');
			}
		});
	});

	app.post('/commonplace/gcm/send', function(req, res) {
		var phones = req.body['phones'];
		var title = req.body['title'];
		var message = req.body['message'];

		// Make Array if there is only one object.
		if (Object.prototype.toString.call( phones ) !== '[object Array]' ) {
			phones = [phones];
		}

		GCM.sendMessage(phones, title, message, function(e, o) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(o);
			}
		});
	});

	// TODO deletion //
	app.post('/commonplace/gcm/list', function(req, res) {
		AM.getGcmTokens(req.body['phones'], function(e, o) {
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(o);
			}
		});
	});

	// Only for testing //
	/*
		req_params = {
				'token': '___GCM_TOKEN_SHA___',
				'title': 'Hello',
				'message': 'hello world.'
		};
	 */
	app.post('/test/gcm-send-with-token', function(req, res) {
		var token = req.body['token'];
		var title = req.body['title'];
		var message = req.body['message'];

		GCM.test_send_with_token(token, title, message, function(e, o) {
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(o);
			}
		});
	});

	app.get('*', function(req, res) { res.render('404', { title: 'Page Not Found'}); });
};