package com.heytz.cordova;

public class ModuleInfo {
    private final String moduleIP;
    private final int port;

    /**
     * @param moduleIP module's IP
     * @param port     remote port
     */
    public ModuleInfo(String moduleIP, int port) {
        this.moduleIP = moduleIP;
        this.port = port;
    }

    public String getModuleIP() {
        return moduleIP;
    }

    public int getPort() {
        return port;
    }
}
