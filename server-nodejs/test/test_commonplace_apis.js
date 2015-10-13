var hippie = require('hippie');
var server = require('../app');

hippie.assert.showDiff = true;

var url_createAccount = '/commonplace/regist';      //서비스 가입 (기존 가입자일 경우 필수 정보 업데이트)
var url_deleteAccount = '/commonplace/unregist';    //서비스 탈퇴 (가입정보 삭제)
var url_userLocation = '/commonplace/user/location';   //사용자 GPS 정보 등록

var valid_param_for_user_creation = {
    phone: '010-1234-0000',
    token: 'dummy_gcm_token',
    name: 'dummy_name'
};

var valid_param_for_user_location = {
    phone: valid_param_for_user_creation.phone,
    latitude: 37.574255,
    longitude: 126.976754
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