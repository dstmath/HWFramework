package com.huawei.android.feature.install.config;

import com.huawei.android.feature.BuildConfig;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public class FeatureInstallConfig {
    public static final Set<String> support = Collections.unmodifiableSet(new w());

    public static String getSuffix(String str) {
        if (str == null) {
            return BuildConfig.FLAVOR;
        }
        for (String next : support) {
            if (str.endsWith(next)) {
                return next;
            }
        }
        return BuildConfig.FLAVOR;
    }

    public static boolean isFileEndWithConfig(File file) {
        for (String endsWith : support) {
            if (file.getName().endsWith(endsWith)) {
                return true;
            }
        }
        return false;
    }
}
