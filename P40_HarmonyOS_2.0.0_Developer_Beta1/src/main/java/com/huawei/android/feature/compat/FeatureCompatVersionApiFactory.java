package com.huawei.android.feature.compat;

import android.os.Build;
import com.huawei.android.feature.install.IDynamicFeatureInstaller;

public class FeatureCompatVersionApiFactory {
    public static IDynamicFeatureInstaller createDynamicInstaller() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.PREVIEW_SDK_INT != 0) {
            switch (Build.VERSION.SDK_INT) {
                case 27:
                    return new V26Compat();
            }
        }
        if (Build.VERSION.SDK_INT < 21) {
            throw new AssertionError("Unsupported Android Version");
        }
        switch (Build.VERSION.SDK_INT) {
            case 21:
            case 22:
                return new V14Compat();
            case 23:
            case 24:
            case 25:
                return new V23Compat();
            case 26:
            case 27:
                return new V26Compat();
            default:
                return new V26Compat();
        }
    }
}
