var fs = require('fs');
var gcm = require('node-gcm');
var CronJob = require('cron').CronJob;

var server_api_key = fs.readFileSync(__dirname + '/../../app/server/conf/gcm-server-key', 'utf8');
var sender = new gcm.Sender(server_api_key);

var tokenOfK = 'APA91bGjdNCdPJ8g5sReX_IDKA2hkfHcvsyN7zZvxbbAH_Mk981TwkfeBAz66IKdEvmdUvttM3bvcFNgoNCpxRbD2iObmVxODRUSjdLPtX5flnqYXHIXGBwAwVJrgbfMpQALruQDnD4Y';
var tokenOfP = 'APA91bFsvRUGN6s1LXyyDq8hoO7WwxCJPjz7-kWXGvPEo2tqlP8vgs4v4GMu-ikvOO0wm6vVLpru4eYn3sQyqpM8fEVrHKw-ROSWVF0_lbAHNhvbPpyabjQHtM4nEX-i8HwAyr0Os_Wa';
var messageBody = {
	category: 'GPS Push',
	moimId: 34,
	member: [
		{phone: '01072770090', name: 'KIM', latitude: '111.111', longitude: '222.222'},
		{phone: '01020702175', name: 'PARK', latitude: '333.333', longitude: '555.555'}
	]
};

var message = new gcm.Message({
    collapseKey: 'CommonPlace Notification',
    delayWhileIdle: false,
    timeToLive: 1,
    dryRun: false,
    data: messageBody
});


var job = new CronJob({
    cronTime: '*/3 * * * * *',
    onTick: function() {
    	
		sender.sendNoRetry(message, [tokenOfK, tokenOfP], function(err, result) {
		    if (err) {
		        console.error('-----------------------GCM ERROR------------------------')
		        console.dir(err);
		        console.error('--------------------------------------------------------');
		    }
		    console.dir(result);
		});
    },
    start: true
});

job.start();