package com.huawei.android.feature.install;

import java.io.File;

public class FeatureApkInfo {
    private File mSplitApk;
    private String mSplitName;

    public FeatureApkInfo(File file, String str) {
        this.mSplitApk = file;
        this.mSplitName = str;
    }

    public final File getSplitApk() {
        return this.mSplitApk;
    }

    public final String getSplitName() {
        return this.mSplitName;
    }
}
