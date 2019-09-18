package com.huawei.android.feature.install.localinstall;

import android.content.Context;
import java.io.File;

public class LocalFilePathParser extends PathParser {
    public LocalFilePathParser(Context context, String str) {
        super(context, str);
    }

    public File getLoadingFile() {
        return new File(this.mOriginPath);
    }

    public int parsePath() {
        return new File(this.mOriginPath).exists() ? 0 : -10;
    }
}
