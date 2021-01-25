package com.huawei.android.feature.install.localinstall;

import android.content.Context;
import java.io.File;

public class LocalFilePathParser extends PathParser {
    public LocalFilePathParser(Context context, String str) {
        super(context, str);
    }

    @Override // com.huawei.android.feature.install.localinstall.PathParser
    public File getLoadingFile() {
        return new File(this.mOriginPath);
    }

    @Override // com.huawei.android.feature.install.localinstall.PathParser
    public int parsePath() {
        return new File(this.mOriginPath).exists() ? 0 : -10;
    }
}
