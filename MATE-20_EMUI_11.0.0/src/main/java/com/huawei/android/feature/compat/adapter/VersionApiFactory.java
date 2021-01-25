package com.huawei.android.feature.compat.adapter;

import android.os.Build;

public class VersionApiFactory {
    public static VersionApi create() {
        if (Build.VERSION.SDK_INT < 23 || Build.VERSION.PREVIEW_SDK_INT == 0) {
            switch (Build.VERSION.SDK_INT) {
                case 21:
                    return new V21();
                case 22:
                    return new V22();
                case 23:
                    return new V23();
                case 24:
                    return new V24();
                case 25:
                    return new V25();
                case 26:
                    return new V26();
                case 27:
                    return new V27();
                case 28:
                    return new V28();
                default:
                    throw new AssertionError("Unsupported Android Version");
            }
        } else {
            switch (Build.VERSION.SDK_INT) {
                case 27:
                    return new V28();
                default:
                    throw new AssertionError("Unsupported Android Preview Version");
            }
        }
    }
}
