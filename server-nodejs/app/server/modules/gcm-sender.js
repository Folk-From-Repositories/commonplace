var gcm = require('node-gcm');
var fs = require('fs');
var AM = require('./account-manager');
var server_api_key = fs.readFileSync(__dirname + '/../conf/gcm-server-key', 'utf8');

if (typeof server_api_key !== 'string') {
	throw 'GCM Server\'s API key is not exist.'
}

var sender = new gcm.Sender(server_api_key);

// ONLY FOR TEST //
exports.test_send_with_token = function(token, title, message, callback)
{
	// validate data
	if (!token) { callback('error-request-parameter(token)'); return; }
	if (!title) { callback('error-request-parameter(title)'); return; }
	if (!message) { callback('error-request-parameter(message)'); return; }

	// update message with current time
	var current = new Date().toString();

	var gcmMsg = new gcm.Message({
		collapseKey: 'test',
		delayWhileIdle: true,
		timeToLive: 3,
		data: {
			title: title,
			message: message + '(' + current + ')'
		}
	});

	sender.send(gcmMsg, token, 4, callback);
}

/**
 * Send Google Cloud Message using phone numbers.
 *
 * @public
 * @param {array} phones 전송 대상 전화번호 리스트
 * @param {json} data 메시지
 * @return {json} 전송 결과
 */
exports.sendMessage = function(phones, data, callback)
{
	// validate data
	if (!phones) { callback('error-request-parameter(phones)'); return; }
	if (Object.prototype.toString.call( phones ) !== '[object Array]' ) { callback('error-request-parameter(phones is not array.)'); return; }

	AM.getGcmTokens(phones, function(err, res) {

		if (err) { callback(err); return; }

		var tokens = [];

		for(var idx in res) {
			tokens.push(res[idx].token);
		}

		console.dir({
			where: 'gcm-sender.js -> sendMessage()',
			what: 'phone number에 대한 gcm token 조회 결과 (from database)',
			data: data
		});

		var gcmMsg = new gcm.Message({
			collapseKey: 'CommonPlace Notification',
			delayWhileIdle: true,
			timeToLive: 3,
			data: data
		});

		sender.send(gcmMsg, tokens, 4, callback);
	});
}
