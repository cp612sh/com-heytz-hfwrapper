HFWrapper = {
    start: function (appId, productKey, uid, userToken, success, failure) {
        cordova.exec(success, failure, 'HFWrapper', 'start', [appId, productKey, uid, userToken, wifiSSID, wifiKey]);
    },
};