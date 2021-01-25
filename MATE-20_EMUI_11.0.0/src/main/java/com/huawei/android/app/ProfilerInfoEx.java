package com.huawei.android.app;

import android.app.ProfilerInfo;

public class ProfilerInfoEx {
    private ProfilerInfo mProfilerInfo;

    public void setProfilerInfo(ProfilerInfo profilerInfo) {
        this.mProfilerInfo = profilerInfo;
    }

    public ProfilerInfo getProfilerInfo() {
        return this.mProfilerInfo;
    }
}
