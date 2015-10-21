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
    onTick: function() {
        var startTime, endTime;

        async.waterfall([

            function init(callback) {
                startTime = moment();
                console.log('');
                console.log('');
                console.log('[CronJob] START: user-location-broadcaster - ' + startTime.tz(conf.timezone).format(conf.dateTimeFormatForLogging));
                callback(null);
            },
            function findNewLocationMember(callback) {
                ULM.findNewLocationMember(function(err, result) {
                    if (err) {
                        if (err === 'No changed user' || err === 'No result') {
                            console.log('* There is no user who was changed location info.')
                        }
                        return callback('finish');
                    }

                    var moims = {};

                    for (var i = 0; i < result.length; i++) {
                        var moimId = result[i].moimId;
                        var member = {
                            phone: result[i].phone,
                            name: result[i].name,
                            latitude: result[i].latitude,
                            longitude: result[i].longitude
                        };

                        if (typeof moims[moimId] === 'object') {
                            moims[moimId].push(member);
                        } else {
                            moims[moimId] = [member];
                        }
                    }

                    callback(null, moims);
                });
            },
            function sendGCMPush(moims, callback) {

                var moimIds = Object.keys(moims);

                async.eachSeries(moimIds, function(moimId, eachCallback) {

                    moimId = parseInt(moimId);

                    async.waterfall([

                        function init(innerWaterFallCB) {
                            console.log('* Processing moim id ' + moimId);
                            innerWaterFallCB(null);
                        },
                        function generateData(innerWaterFallCB) {

                            var message = {
                                category: 'GPS Push',
                                moimId: moimId,
                                member: moims[moimId]
                            };

                            innerWaterFallCB(null, message);
                        },
                        function getMoimMember(message, innerWaterFallCB) {

                            MM.findMemberInMoims([message.moimId], function(err, phones) {
                                innerWaterFallCB(err, message, phones);
                            });
                        },
                        function sendMessage(message, phones, innerWaterFallCB) {

                            GM.sendNoRetry(phones, message, function(e, o) {
                                if (e && e === 'No target for sending GCM') {
                                    // do next item
                                    innerWaterFallCB();
                                } else {
                                    console.log('* GCM Pushed.');
                                    console.log(JSON.stringify(message, null, 4));
                                    console.log('* GCM Result.');
                                    console.log(JSON.stringify(o, null, 4));
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
                    } else {
                        callback(null, 'All GCM Push for user location have been processed successfully');
                    }
                });

            }
        ], function finish(err, result) {
            if (err) {
                if (err !== 'finish') {
                    console.error(err);
                }
            }

            endTime = moment();
            console.log('[CronJob] END  : user-location-broadcaster - ' + endTime.tz(conf.timezone).format(conf.dateTimeFormatForLogging) + ' | working time : ' + endTime.diff(startTime) + "ms");
        });

    },
    start: false,
    timeZone: conf.timezone
});

job.start();