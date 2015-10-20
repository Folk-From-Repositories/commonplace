var path = require('path');
var test = require('unit.js');
var util = require(path.join(__dirname, '../../app/server/modules/utils.js'));

describe('Phone Number Formatting', function() {

	var phoneNumbers = [
		'01012345678',
		'010-1234-5678',
		'010-12345678',
		'0101234-5678',
		'210-1234-5678',
		'+82-10-1234-5678'
	];

	var expectedResult = '01012345678';

	for (var i = 0; i < phoneNumbers.length; i++){
		var testNumber = phoneNumbers[i];

	    it('case - ' + testNumber, function() {
			var r = util.phoneToDbFormat(testNumber);
	        test.assert(r === expectedResult);
	    });
		
	}

});
