package com.heytz.cordova;

public class UserInfo {
    private final String user_token;
    private final String uid;

    /**
     * @param moduleIP module's IP
     * @param port     remote port
     */
    public UserInfo(String user_token, String uid) {
        this.user_token = user_token;
        this.uid = uid;
    }

    public String getUser_token() {
        return user_token;
    }

    public String getUid() {
        return uid;
    }
}
