package com.huawei.android.feature.install;

import java.io.File;

public class FeatureDownloadInfo {
    public File mFile;
    public String mFileName;

    public FeatureDownloadInfo(File file, String str) {
        this.mFile = file;
        this.mFileName = str;
    }
}
