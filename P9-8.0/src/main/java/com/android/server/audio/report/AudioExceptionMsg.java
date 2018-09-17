package com.android.server.audio.report;

public class AudioExceptionMsg {
    private String msgPackagename = null;
    private String msgPackageversion = null;
    private int msgType = 0;

    public AudioExceptionMsg(int type, String packagename, String packageversion) {
        this.msgType = type;
        this.msgPackagename = packagename;
        this.msgPackageversion = packageversion;
    }

    public int getMsgType() {
        return this.msgType;
    }

    public String getMsgPackagename() {
        return this.msgPackagename;
    }

    public String getMsgPackageversion() {
        return this.msgPackageversion;
    }
}
