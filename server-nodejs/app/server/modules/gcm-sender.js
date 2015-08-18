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
