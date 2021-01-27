package com.android.server.pm;

import android.content.Context;
import java.io.File;

public class DefaultHwPackageManagerUtils {
    public boolean isPackageFilenameEx(String name) {
        return false;
    }

    public String getPackageNameFromApkEx(String apkFile) {
        return null;
    }

    public boolean isHepFileNameEx(String name) {
        return false;
    }

    public boolean isUserUnlockedEx(Context context) {
        return false;
    }

    public File getCustomizedFileNameEx(String xmlName, int flag) {
        return null;
    }
}
