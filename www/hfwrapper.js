var exec = require('cordova/exec');

exports.start = function (appId, productKey, uid, userToken, wifiSSID, wifiKey, success, failure) {
    exec(success, failure, 'HFWrapper', 'start', [appId, productKey, uid, userToken, wifiSSID, wifiKey]);
};

exports.deallocate = function (success, failure) {
    exec(success, failure, 'HFWrapper', 'deallocate', []);
};