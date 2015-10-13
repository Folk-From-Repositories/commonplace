var hippie = require('hippie');
var server = require('../app');

hippie.assert.showDiff = true;

var url_createAccount = '/commonplace/regist';      //서비스 가입 (기존 가입자일 경우 필수 정보 업데이트)
var url_deleteAccount = '/commonplace/unregist';    //서비스 탈퇴 (가입정보 삭제)
var url_userLocation = '/commonplace/user/location';   //사용자 GPS 정보 등록

var url_createMoim = '/commonplace/moim/regist'; // 모임 생성 정보 등록
var url_myMoim = '/commonplace/moim/my';    // 내 모임 정보 조회

var valid_param_for_user_creation = {
    phone: '01012340000',
    token: 'dummy_gcm_token',
    name: 'dummy_name'
};

var valid_param_for_user_location = {
    phone: valid_param_for_user_creation.phone,
    latitude: 37.574255,
    longitude: 126.976754
};

var valid_param_for_user_location = {
    phone: valid_param_for_user_creation.phone,
    latitude: "37.574255",
    longitude: "126.976754"
};

var valid_param_for_moim_creation = {
    "title"            : '테스트 모임',
    "locationName"     : '광화문 광장',
    "locationImageUrl" : 'https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256',
    "locationLat"      : valid_param_for_user_location.latitude,
    "locationLon"      : valid_param_for_user_location.longitude,
    "locationPhone"    : '02-120',
    "locationDesc"     : '세종대왕 동상 앞에서 봅니다.',
    "owner"            : valid_param_for_user_creation.phone,
    "member"           : ['01012340001', '01012340002']
};

describe('CommonPlace - Account', function() {

    describe('Create Account (' + url_createAccount + ')', function() {

        var params = valid_param_for_user_creation;

        it('work fine with valid form data - ' + JSON.stringify(params), function(done) {
            hippie(server)
                .form()
                .post(url_createAccount)
                .send(params)
                .expectStatus(200)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with empty form data', function(done) {
            hippie(server)
                .form()
                .post(url_createAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with invalid phone number - +82-10-1234-0000', function(done) {
            var param = JSON.parse(JSON.stringify(params));
            param.phone = '+82-10-1234-0000';

            hippie(server)
                .form()
                .post(url_createAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with invalid phone number - 210-1234-0000', function(done) {
            var param = JSON.parse(JSON.stringify(params));
            param.phone = '210-1234-0000';

            hippie(server)
                .form()
                .post(url_createAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });
    });

    describe('Update User Location(' + url_userLocation + ')', function() {
        var params = valid_param_for_user_location;

        it('work fine with valid form data - ' + JSON.stringify(params), function(done) {
            hippie(server)
                .form()
                .post(url_userLocation)
                .send(params)
                .expectStatus(200)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        var param = {phone : params.phone};

        it('return false with invalid form data - ' + JSON.stringify(param), function(done) {
            hippie(server)
                .form()
                .post(url_userLocation)
                .send(param)
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });
    });

    describe('Delete Account (' + url_deleteAccount + ')', function() {
        var params = valid_param_for_user_creation;

        it('work fine with valid form data - ' + JSON.stringify(params), function(done) {
            hippie(server)
                .form()
                .post(url_deleteAccount)
                .send(params)
                .expectStatus(200)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with empty form data', function(done) {
            hippie(server)
                .form()
                .post(url_deleteAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with invalid phone number - +82-10-1234-0000', function(done) {
            var param = JSON.parse(JSON.stringify(params));
            param.phone = '+82-10-1234-0000';

            hippie(server)
                .form()
                .post(url_deleteAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });

        it('returns fail with invalid phone number - 210-1234-0000', function(done) {
            var param = JSON.parse(JSON.stringify(params));
            param.phone = '210-1234-0000';

            hippie(server)
                .form()
                .post(url_deleteAccount)
                .send()
                .expectStatus(400)
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });
    });

});


describe('CommonPlace - Moim', function() {

    var testMoimId; // for test

    describe('Create Moim(' + url_createMoim + ')', function() {
        var params = JSON.parse(JSON.stringify(valid_param_for_moim_creation));

        it('work fine with valid form data - ' + JSON.stringify(params), function(done) {
            hippie(server)
                .json()
                .form()
                .post(url_createMoim)
                .send(params)
                .expectStatus(200)
                .expectValue('sms', valid_param_for_moim_creation.member)
                .expect(function(res, body, next) {
                    var err;
                    testMoimId = body.moimId;

                    if ( isNaN(testMoimId) ) {
                        err = 'Needed moimId in response';
                    }

                    next(err);
                })
                .end(function(err, res, body) {
                    if (err) throw err;
                    done();
                });
        });
    });

    describe('Get my Moim(' + url_myMoim + ')', function () {
        var ownerPhone = valid_param_for_moim_creation.owner;
        it('work fine with valid phone number - ' + ownerPhone, function(done) {
            hippie(server)
                .form()
                .post(url_myMoim)
                .send({phone: ownerPhone})
                .expectStatus(200)
                .end(function(err, res, body) {
                    if (err) throw err;
                    //console.log(body);
                    done();
                });
        });
    });
});