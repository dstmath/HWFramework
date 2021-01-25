package com.android.server.rms.iaware.memory.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import java.io.File;
import java.util.Iterator;

public class PackageInfoCollector {
    private static final String TAG = "AwareMem_PkgInfo";

    public static ArraySet<String> getLibFilesFromPackage(Context mContext, ArraySet<String> pkgSet) {
        if (mContext == null || pkgSet == null) {
            return null;
        }
        ArraySet<String> libFileSet = new ArraySet<>();
        Iterator<String> it = pkgSet.iterator();
        while (it.hasNext()) {
            try {
                String sourceDir = mContext.createPackageContext(it.next(), 1152).getApplicationInfo().sourceDir;
                if (sourceDir != null) {
                    if (sourceDir.lastIndexOf(File.separator) <= 0) {
                        AwareLog.w(TAG, "source dir name error, sourceDir.lastIndexOf(File.separator)=" + sourceDir.lastIndexOf(File.separator));
                    } else {
                        libFileSet.add(sourceDir.substring(0, sourceDir.lastIndexOf(File.separator)));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                AwareLog.e(TAG, "Unable to create context for heavy notification");
            }
        }
        return libFileSet;
    }
}
