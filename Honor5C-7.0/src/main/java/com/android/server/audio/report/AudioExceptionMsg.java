package com.android.server.audio.report;

public class AudioExceptionMsg {
    public String msgPackagename;
    public int msgType;

    public AudioExceptionMsg(int type, String packagename) {
        this.msgType = 0;
        this.msgPackagename = null;
        this.msgType = type;
        this.msgPackagename = packagename;
    }
}
