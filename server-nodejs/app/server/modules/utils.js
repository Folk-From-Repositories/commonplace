exports.phoneToDbFormat = phoneToDbFormat;
exports.isOnlyNumber = isOnlyNumber;
exports.isArray = isArray;

/**
 * Force to change the phone number format like "01012345678"
 * For this, get last 8 chars, and preappend '010'.
 */
function phoneToDbFormat(phone) {

    if (typeof phone === 'string') {
        var expectedStartStr = '010';

        phone = phone.replace(/[-_\W]/g, "");
        
        // get last 8 characters
        var str = phone.substring(phone.length - 8, phone.length);

        // prepend '010'
        phone = expectedStartStr + str;

        return phone;
    } else {
        return undefined;
    }
};

function isOnlyNumber(str) {
    var isNumber = true;

    if (str && str.length > 0) {
        for (var i = 0; i < str.length; i++) {
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

function isArray(arr) {
    var isArr = Object.prototype.toString.call( arr ) === '[object Array]';
    return isArr;
}