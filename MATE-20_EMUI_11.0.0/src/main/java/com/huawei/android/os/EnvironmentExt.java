package com.huawei.android.os;

import android.os.Environment;
import java.io.File;

public class EnvironmentExt {
    public static File getDataProfilesDePackageDirectory(int userId, String packageName) {
        return Environment.getDataProfilesDePackageDirectory(userId, packageName);
    }
}
