package com.android.server.audio;

public class AudioExceptionRecord {
    private String mLastMutePackageName = null;
    private String mLastMutePackageVersion = null;

    public void updateMuteMsg(String packageName, String packageVersion) {
        this.mLastMutePackageName = packageName;
        this.mLastMutePackageVersion = packageVersion;
    }

    public String getMutePackageName() {
        return this.mLastMutePackageName;
    }

    public String getMutePPackageVersion() {
        return this.mLastMutePackageVersion;
    }
}
