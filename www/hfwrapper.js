var exec = require('cordova/exec');

exports.start = function (appId, productKey, uid, userToken, wifiSSID, wifiKey, success, failure) {
    exec(success, failure, 'HFWrapper', 'start', [appId, productKey, uid, userToken, wifiSSID, wifiKey]);
};

exports.deallocate = function () {
    exec(null, null, 'HFWrapper', 'deallocate', []);
};