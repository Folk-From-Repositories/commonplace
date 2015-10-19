var async = require('async');
var conf = require(__dirname + '/conf.json');
var CronJob = require('cron').CronJob;
var fs = require('fs');
var moment = require('moment');
var MM = require(__dirname + '/../server/modules/moim-manager');

var job = new CronJob({
    cronTime: conf.userLocationBroadCastActivatorCronTime,
    // Seconds: 0-59
    // Minutes: 0-59
    // Hours: 0-23
    // Day of Month: 1-31
    // Months: 0-11
    // Day of Week: 0-6
    onTick: function() {
        var startTime, endTime;

        async.waterfall([

            function init(callback) {
                startTime = moment();
                console.log('[CronJob] START: user-location-broadcast-activator - ' + startTime.tz(conf.timezone).format());
                callback(null);
            },
            function findEnabledOrDisableNotificationPerMoim(callback) {
                MM.getAll(function(err, queryResult) {
                    var now = moment.tz(conf.timezone);
                    var enableNotiMoims = [];
                    var disableNotiMoims = [];

                    for (var i = 0; i < queryResult.length; i++) {
                        var moimDateTime = moment.tz(queryResult[i].dateTime, conf.dateTimeFormat, conf.timezone);
                        var diffMins = now.diff(moimDateTime) / 1000 / 60;

                        if (diffMins > conf.enableNotiBeforeMin && diffMins < conf.disableNotiAfterMin) {
                            enableNotiMoims.push(queryResult[i].id);
                        } else {
                            disableNotiMoims.push(queryResult[i].id);
                        }
                    }

                    callback(null, enableNotiMoims, disableNotiMoims);
                });
            },
            function updateBroadcastStatus(enableNotiMoims, disableNotiMoims, callback) {
                console.log('Push 활성화 대상', enableNotiMoims);
                console.log('Push 비활성화 대상', disableNotiMoims);

                MM.enableLocationBroadcast(enableNotiMoims, function(err, queryResult) {
                    if (err) console.error(err);
                });

                MM.disableLocationBroadcast(disableNotiMoims, function(err, queryResult) {
                    if (err) console.error(err);
                });

                callback(null, 'done');
            }
        ], function finish(err, result) {
            endTime = moment();
            console.log('[CronJob] END: user-location-broadcast-activator - ' + endTime.tz(conf.timezone).format() + ' | diff : ' + endTime.diff(startTime) + "ms");
        });

    },
    start: false,
    timeZone: conf.timezone
});

job.start();