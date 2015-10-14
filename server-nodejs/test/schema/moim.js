var test = require('unit.js');
var Validator = require('jsonschema').Validator;
var v = new Validator();
var schema = require('../../app/server/schema/moim-schema.json');
//v.addSchema(schema, '/Moim');

var testParam = {
    'title': '테스트 모임',
    'dateTime': '20151031 19:00',
    'locationName': '광화문 광장',
    'locationImageUrl': 'https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256',
    'locationLat': '37.574255',
    'locationLon': '126.976754',
    'locationPhone': '02-120',
    'locationDesc': '세종대왕 동상 앞에서 봅니다.',
    'owner': '01012340000',
    'member': ['01012340001', '01012340002']
};

var testParamKeys = Object.keys(testParam);

describe('Moim Schema', function() {

    it('load', function() {
        test.assert(typeof schema === 'object');
    });

    describe('# valid date', function () {
        it('# valid data', function() {
            var result = v.validate(testParam, schema);
            test.assert(result.errors.length === 0);
        });
    });

    describe('# invalid data - missing', function () {
        for (var i = 0; i < testParamKeys.length; i++) {
            var missingFieldName = testParamKeys[i];
            it(missingFieldName, function() {
                var invalidTestData = JSON.parse(JSON.stringify(testParam));
                invalidTestData[missingFieldName] = undefined;

                var result = v.validate(invalidTestData, schema);
                test.assert(result.errors.length === 1);
                test.assert(result.errors[0].argument === missingFieldName);
            });
        }
    });

    describe('# invalid data - type error', function () {
        var invalidParam = {
            'title': 1,
            'dateTime': '20151031 19:00',
            'locationName': null,
            'locationImageUrl': 'https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256',
            'locationLat': 37.574255,
            'locationLon': 126.976754,
            'locationPhone': '02-120',
            'locationDesc': '세종대왕 동상 앞에서 봅니다.',
            'owner': 01012340000,
            'member': ['01012340001', 01012340002]
        };

        it('# detect invalid field', function () {
            var result = v.validate(invalidParam, schema);
            test.assert(result.errors.length === 6);
        });
    });
});