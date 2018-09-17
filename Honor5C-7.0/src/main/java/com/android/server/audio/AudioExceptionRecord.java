package com.android.server.audio;

public class AudioExceptionRecord {
    public String mLastMutePackageName;

    public AudioExceptionRecord() {
        this.mLastMutePackageName = null;
    }

    public void updateMuteMsg(String packagename) {
        this.mLastMutePackageName = packagename;
    }
}
