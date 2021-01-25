package com.huawei.android.feature.install;

import java.io.File;
import java.util.Set;

public interface IDynamicFeatureInstaller {
    int dexInstall(ClassLoader classLoader, File file, File file2);

    int nativeInstall(ClassLoader classLoader, Set<File> set);
}
