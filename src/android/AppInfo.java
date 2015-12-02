package com.heytz.cordova;

public class AppInfo {
    private final String app_id;
    private final String product_key;

    /**
     * @param moduleIP module's IP
     * @param port     remote port
     */
    public AppInfo(String app_id, String product_key) {
        this.app_id = app_id;
        this.product_key = product_key;
    }

    public String getApp_id() {
        return app_id;
    }

    public String getProduct_key() {
        return product_key;
    }
}
