var async = require('async');
var conf = require(__dirname + '/conf.json');
var CronJob = require('cron').CronJob;
var fs = require('fs');
var moment = require('moment');
var GM = require(__dirname + '/../server/modules/gcm-sender');
var MM = require(__dirname + '/../server/modules/moim-manager');
var ULM = require(__dirname + '/../server/modules/user-location-manager');

var job = new CronJob({
    cronTime: conf.campaign119CronTime,
    onTick: function() {

        var startTime, endTime;

        async.waterfall([
            function init(cb) {
                startTime = moment();
                console.log('[CronJob] START: Campaign 119 CronJob - ' + startTime.tz(conf.timezone).format());
                cb(null);
            },
            function findTargetMoims(cb) {
                var todayYYYYMMDD = moment.tz(conf.timezone).format('YYYYMMDD');

                var from = todayYYYYMMDD + ' ' + conf.campain119TargetMoimFrom;
                var to = todayYYYYMMDD + ' ' + conf.campain119TargetMoimTo;

                console.log('- Search moim :' + from + ' ~ ' + to);

                MM.findIdsWithDatetimeFromTo(from, to, function(err, moims) {
                    var ids = [];

                    if (moims) {
                        for (var i = 0; i < moims.length; i++) {
                            ids.push(moims[i].id);
                        }
                    }

                    cb(err, ids);
                });
            },
            function findTargetMembers(moimIds, cb) {
                MM.findMemberInMoims(moimIds, function(err, phones) {
                    if (phones) {
                        console.log('메시지 전송 대상 : ' + JSON.stringify(phones));
                    }
                    cb(err, phones);
                });
            },
            function sendGCM(phones, cb) {
                var message = {
                    category: 'Campaign 119',
                    title: '건전한 119 회식 문화',
                    message: '아직 회식이 진행중이신지요?\n건전한 회식문화를 위한 우리들의 약속!\n119회식 문화를 정착시키기 위해 노력합시다.'
                };

                if (phones.length < 1) return cb('no target');

                GM.sendNoRetry(phones, message, function(err, result) {
                    if (err) {
                        console.error('-----------------------GCM ERROR------------------------')
                        console.dir(err);
                        console.error('--------------------------------------------------------');
                    }
                    console.dir(result);
                });
            }
        ], function finish(err, result) {
            if (err) {
                console.error(err);
            } else {
                console.log(result);
            }

            endTime = moment();
            console.log('[CronJob] END: Campaign 119 CronJob - ' + endTime.tz(conf.timezone).format() + ' | diff : ' + endTime.diff(startTime) + "ms");
        });
    },
    start: true,
    timeZone: conf.timezone
});

job.start();
