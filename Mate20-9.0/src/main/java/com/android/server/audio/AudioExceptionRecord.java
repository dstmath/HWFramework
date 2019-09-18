package com.android.server.audio;

public class AudioExceptionRecord {
    private String mLastMutePackageName = null;
    private String mLastMutePackageVersion = null;

    public void updateMuteMsg(String packagename, String packageversion) {
        this.mLastMutePackageName = packagename;
        this.mLastMutePackageVersion = packageversion;
    }

    public String getMutePackageName() {
        return this.mLastMutePackageName;
    }

    public String getMutePPackageVersion() {
        return this.mLastMutePackageVersion;
    }
}
