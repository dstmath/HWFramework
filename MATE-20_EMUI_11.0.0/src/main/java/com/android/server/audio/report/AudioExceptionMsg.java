package com.android.server.audio.report;

public class AudioExceptionMsg {
    private String mMsgPackageName = null;
    private String mMsgPackageVersion = null;
    private int mMsgType = 0;

    public AudioExceptionMsg(int type, String packageName, String packageVersion) {
        this.mMsgType = type;
        this.mMsgPackageName = packageName;
        this.mMsgPackageVersion = packageVersion;
    }

    public int getMsgType() {
        return this.mMsgType;
    }

    public String getMsgPackagename() {
        return this.mMsgPackageName;
    }

    public String getMsgPackageversion() {
        return this.mMsgPackageVersion;
    }
}
