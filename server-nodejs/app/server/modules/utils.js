exports.phoneToDbFormat = phoneToDbFormat;
exports.isOnlyNumber = isOnlyNumber;
exports.isArray = isArray;

function phoneToDbFormat(phone) {

    if (phone) {
        phone = phone.replace(/[-_\W]/g, "");
    }

    return phone;
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