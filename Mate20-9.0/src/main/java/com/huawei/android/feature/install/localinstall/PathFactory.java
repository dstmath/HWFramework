package com.huawei.android.feature.install.localinstall;

import android.content.Context;

public class PathFactory {
    public PathParser getPathParser(Context context, String str) {
        if (str != null) {
            return str.startsWith("package://") ? new PackagePathParser(context, str) : new LocalFilePathParser(context, str);
        }
        throw new IllegalArgumentException("path is null");
    }
}
