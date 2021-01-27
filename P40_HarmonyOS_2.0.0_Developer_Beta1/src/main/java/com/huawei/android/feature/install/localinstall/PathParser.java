package com.huawei.android.feature.install.localinstall;

import android.content.Context;
import java.io.File;

public abstract class PathParser {
    protected Context mContext;
    protected String mOriginPath;

    public PathParser(Context context, String str) {
        this.mOriginPath = str;
        this.mContext = context;
    }

    public abstract File getLoadingFile();

    public abstract int parsePath();
}
