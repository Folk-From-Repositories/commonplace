
var CT = require('./modules/country-list');
var AM = require('./modules/account-manager');
var EM = require('./modules/email-dispatcher');
var GCM = require('./modules/gcm-sender');
var ULM = require('./modules/user-location-manager');
var MM 	= require('./modules/moim-manager');
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

	/**
	 * 서비스 가입 (기존 가입자일 경우 필수 정보 업데이트)
	 *
	 * @url /commonplace/regist
	 * @method POST
	 * @param {string} phone 전화번호
	 * @param {string} token GCM 토큰
	 * @param {string} name  사용자 이름(별칭)
	 **/
	app.post('/commonplace/regist', function(req, res) {
		var data = {
			phone : req.body['phone'],
			token : req.body['token'],
			name  : req.body['name']
		};

		console.debug(tag, req.originalUrl, data);

		AM.registDeviceInfo(data, function(e) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send('ok');
			}
		});
	});

	/**
	 * 사용자 GPS 정보 등록
	 *
	 * @url /commonplace/user/location
	 * @method POST
	 * @param {string} phone 전화번호
	 * @param {string} latitude 위도
	 * @param {string} longitude 경도
	 **/
	app.post('/commonplace/user/location', function(req, res) {

		var data = {
			phone : req.body['phone'],
			latitude : req.body['latitude'],
			longitude  : req.body['longitude']
		};

		ULM.update(data, function(e) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send('ok');
			}
		});
	});

	/**
	 * @depreciated
	 */
	app.get('/admin/gcm/gps/on', function(req, res) {
		ULM.enableGPSNotification();
		res.status(200).send('ok');
	});
	app.get('/admin/gcm/gps/off', function(req, res) {
		ULM.disableGPSNotification();
		res.status(200).send('ok');
	});

	/**
	 * // TODO delete
	 * 사용자 GPS 정보 조회
	 *
	 * @private
	 * @url /test/commonplace/user/location/retrieve
	 * @method POST
	 * @param {string[]} phones 전화번호
	 **/
	app.post('/test/commonplace/user/location/retrieve', function(req, res) {

		var phones = req.body['phones'];

		ULM.gets(phones, function(e, result) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(result);
			}
		});
	});

	/**
	 * 모임 생성 정보 등록
	 *
	 * @url /commonplace/moim/regist
	 * method POST
	 * @param {string} title 모임명
	 * @param {string} locationName 모임장소명
	 * @param {string} locationImageUrl 모임장소 이미지 주소
	 * @param {string} locationLat 모임장소 위도
	 * @param {string} locationLon 모임장소 경도
	 * @param {string} locationPhone 모임장소 연락처
	 * @param {string} locationDesc 모임장소 기타정보
	 * @param {string} owner 모임 만든이 연락처
	 * @param {string[]} member 모임 참여자 연락처 리스트
	 * @response {json} sms {array} 서비스 미 가입자 연락처
	 **/
	app.post('/commonplace/moim/regist', function(req, res) {
		var data = {
			title 				: req.body['title'],
			locationName 		: req.body['locationName'],
			locationImageUrl 	: req.body['locationImageUrl'],
			locationLat 		: req.body['locationLat'],
			locationLon 		: req.body['locationLon'],
			locationPhone 		: req.body['locationPhone'],
			locationDesc 		: req.body['locationDesc'],
			owner 				: req.body['owner'],
			member 				: req.body['member']
		};

		MM.createNewMoim(data, function(e, result) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				GCM.sendMessage(result.users, {
					category: 'invitation',
					moimId: result.moimId
				});
				res.status(200).send({sms: result.nonUsers});
			}
		});
	});

	/**
	 * 나의 모임 조회
	 *
	 * @url /commonplace/moim/my
	 * method POST
	 * @param {string} phone 내 전화번호
	 * @return {json} Moim 테이블 조회 결과, member field는 사용자 정보 추가된 json
	 **/
	app.post('/commonplace/moim/my', function(req, res) {

		MM.getMyMoims(req.body['phone'], function(e, result) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(result);
			}
		});
	});

	/**
	 * 모임 상세 조회
	 *
	 * @url /commonplace/moim/details
	 * method GET
	 * @param {array} or {int} id 모임 ID (int 또는 int array)
	 * @return {json} Moim 테이블 조회 결과, member field는 사용자 정보 추가된 json
	 **/
	app.post('/commonplace/moim/details', function(req, res) {

		MM.getDetails(req.body['id'], function(e, result) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(result);
			}
		});
	});


	/**
	 * // TODO delete
	 * @private 테스트용
	 *
	 * Phone Number Array로 GCM 메시지 전송
	 *
	 * @url /test/commonplace/gcm/send
	 * @method POST
	 * @param {string[]} phones 전송 대상 전화번호 리스트
	 * @param {json} message 전송 메시지
	 **/
	 app.post('/test/commonplace/gcm/send', function(req, res) {
		var phones = req.body['phones'];
		var message = req.body['message'] || {};

		// Make Array if there is only one object.
		if (Object.prototype.toString.call( phones ) !== '[object Array]' ) {
			phones = [phones];
		}

		message.category = "test";

		GCM.sendMessage(phones, message, function(e, o) {
			//TODO 에러 메시지 처리
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(o);
			}
		});
	 });

	/**
	 * // TODO delete
	 * @private 테스트용
	 *
	 * 사용자 GCM token 조회
	 *
	 * @url /test/commonplace/user/list
	 * @method POST
	 * @param {string[]} phones 조회 대상 전화번호 리스트
	 * @return {json} phone, token
	 **/
	app.post('/test/commonplace/user/list', function(req, res) {
		AM.getGcmTokens(req.body['phones'], function(e, o) {
			if (e){
				res.status(400).send(e);
			} else {
				res.status(200).send(o);
			}
		});
	});

	/**
	 * // TODO delete
	 * @private 테스트용
	 *
	 * GCM token 으로 메시지 전송
	 *
	 * @url /test/gcm-send-with-token
	 * @method POST
	 * @param {string} token 전송 대상
	 **/
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