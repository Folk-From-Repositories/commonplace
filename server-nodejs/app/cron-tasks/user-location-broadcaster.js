var async = require('async');
var conf = require(__dirname + '/conf.json');
var CronJob = require('cron').CronJob;
var fs = require('fs');
var moment = require('moment');
var GM = require(__dirname + '/../server/modules/gcm-sender');
var MM = require(__dirname + '/../server/modules/moim-manager');
var ULM = require(__dirname + '/../server/modules/user-location-manager');

var job = new CronJob({
    cronTime: conf.userLocationBroadCasterCronTime,
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
                console.log('[CronJob] START: user-location-broadcaster - ' + startTime.tz(conf.timezone).format());
                callback(null);
            },
            function findEnabledNotificationMoims(callback) {
                MM.getEnabledLocationBroadcast(function(err, queryResult) {
                    var ids = [];

                    for (var i = 0; i < queryResult.length; i++) {
                        ids.push(queryResult[i].id);
                    }

                    MM.getDetails(ids, function(err, moims) {
                        callback(err, moims);
                    });

                });
            },
            function sendGCMPush(moims, callback) {

                async.eachSeries(moims, function(moim, eachCallback) {

                    async.waterfall([

                        function init(innerWaterFallCB) {
                            console.log('Processing moim id ' + moim.id);
                            innerWaterFallCB(null);
                        },
                        function generateData(innerWaterFallCB) {
                            // gcm message
                            var message = {
                                category: 'GPS Push',
                                moimId: moim.id,
                                member: undefined
                            };

                            // parse phone number per moim
                            var phones = moim.member;

                            innerWaterFallCB(null, message, phones);
                        },
                        function retrieveUserLocation(message, phones, innerWaterFallCB) {
                            ULM.gets(phones, function(err, userLocations) {
                                message.member = userLocations;
                                innerWaterFallCB(err, message, phones);
                            });
                        },
                        function sendMessage(message, phones, innerWaterFallCB) {
                            GM.sendMessage(phones, message, function(e, o) {
                                if (e && e === 'No target for sending GCM') {
                                    // do next item
                                    innerWaterFallCB();
                                } else {
                                    innerWaterFallCB(e);
                                }
                            });
                        }
                    ], function finish(err, result) {
                        // next
                        eachCallback(err);
                    });
                }, function(err) {
                    if (err) {
                        callback('GCM Push for user location are failed.');
                        console.error(err);
                    } else {
                        callback(null, 'All GCM Push for user location have been processed successfully');
                    }
                });

            }
        ], function finish(err, result) {
            if (err) {
                console.error(err);
            } else {
                console.log(result);
            }

            endTime = moment();
            console.log('[CronJob] END: user-location-broadcaster - ' + endTime.tz(conf.timezone).format() + ' | diff : ' + endTime.diff(startTime) + "ms");
        });

    },
    start: false,
    timeZone: conf.timezone
});

job.start();