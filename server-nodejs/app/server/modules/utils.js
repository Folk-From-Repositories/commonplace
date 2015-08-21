exports.phoneToDbFormat = phoneToDbFormat;
exports.isOnlyNumber 	= isOnlyNumber;

function phoneToDbFormat(phone) {

	if (phone) {
		phone = phone.replace(/[-_\W]/g, "");
	}

	return phone;
};

function isOnlyNumber(str) {
	var isNumber = true;

	if (str && str.length > 0) {
		for (var i=0; i < str.length; i++) {
			if (isNaN(str[i])) {
				isNumber = false;
				break;
			}
		}
	} else {
		isNumber = false;
	}

	return isNumber;
}