package com.android.server.rms.iaware.memory.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import java.io.File;

public class PackageInfoCollector {
    private static final String TAG = "AwareMem_PkgInfo";

    public static ArraySet<String> getLibFilesFromPackage(Context mContext, ArraySet<String> pkgSet) {
        if (mContext == null || pkgSet == null) {
            return null;
        }
        ArraySet<String> libFileSet = new ArraySet();
        for (String pkg : pkgSet) {
            try {
                Context context = mContext.createPackageContext(pkg, 1152);
                String sourceDir = context.getApplicationInfo().sourceDir;
                AwareLog.i(TAG, "pkg: " + pkg + ", sourceDir: " + sourceDir);
                if (sourceDir != null) {
                    if (sourceDir.lastIndexOf(File.separator) <= 0) {
                        AwareLog.w(TAG, "source dir name error, sourceDir.lastIndexOf(File.separator)=" + sourceDir.lastIndexOf(File.separator));
                    } else {
                        sourceDir = sourceDir.substring(0, sourceDir.lastIndexOf(File.separator));
                        AwareLog.i(TAG, "sourceDir: " + sourceDir);
                        libFileSet.add(sourceDir);
                    }
                }
                String dataDir = context.getApplicationInfo().dataDir;
                AwareLog.i(TAG, "pkg: " + pkg + ", dataDir: " + dataDir);
                if (dataDir != null) {
                    libFileSet.add(dataDir);
                }
            } catch (NameNotFoundException e) {
                AwareLog.w(TAG, "Unable to create context for heavy notification");
            }
        }
        return libFileSet;
    }
}
