var gcm = require('node-gcm');
var fs = require('fs');
var AM = require('./account-manager');
var server_api_key = fs.readFileSync(__dirname + '/../conf/gcm-server-key', 'utf8');

if (typeof server_api_key !== 'string') {
	throw 'GCM Server\'s API key is not exist.'
}

var sender = new gcm.Sender(server_api_key);

// test api
exports.gcmtest = function(data, callback)
{
	// validate data
	if (!data) { callback('error-request-parameter'); return; }

	var phones = data.to;

	// Make Array if there is only one object.
	if (Object.prototype.toString.call( phones ) !== '[object Array]' ) {
		phones = [phones];
	}

	AM.getGcmTokens(phones, function(err, res) {

		if (err) { callback(err); return; }

		var tokens = [];

		for(var idx in res) {
			tokens.push(res[idx].gcm_token);
		}

		var message = new gcm.Message({
			collapseKey: 'test',
			delayWhileIdle: true,
			timeToLive: 3,
			data: {
				title: data.title,
				message: data.message
			}
		});

		sender.send(message, tokens, 4, callback);
	});
}
