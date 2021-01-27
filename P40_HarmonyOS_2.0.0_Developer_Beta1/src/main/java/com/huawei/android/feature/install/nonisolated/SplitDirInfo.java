package com.huawei.android.feature.install.nonisolated;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.huawei.android.feature.install.FeatureApkInfo;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SplitDirInfo {
    private static final String PRE_SPLITS = "presplits";
    private static final String SPLIT_SEPARATOR = ",";
    private static final String TAG = SplitDirInfo.class.getSimpleName();

    public Set<FeatureApkInfo> getMetaSplits(Context context, String str) {
        ApplicationInfo applicationInfo = null;
        HashSet hashSet = new HashSet();
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "cannot find the package info.");
        }
        if (applicationInfo == null) {
            return hashSet;
        }
        String[] split = applicationInfo.metaData.getString(PRE_SPLITS).split(SPLIT_SEPARATOR);
        HashMap<String, String> sourceDir = getSourceDir(context, str);
        if (split.length == 0 || sourceDir.isEmpty()) {
            return hashSet;
        }
        for (String str2 : split) {
            for (Map.Entry<String, String> entry : sourceDir.entrySet()) {
                if (str2.equals(entry.getKey())) {
                    hashSet.add(new FeatureApkInfo(new File(entry.getValue()), entry.getKey()));
                }
            }
        }
        return hashSet;
    }

    @TargetApi(26)
    public HashMap<String, String> getSourceDir(Context context, String str) {
        HashMap<String, String> hashMap = new HashMap<>();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(str, 128);
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 128);
            if (packageInfo.splitNames == null || applicationInfo.splitSourceDirs == null) {
                Log.e(TAG, "splitNames or splitSourceDirs is null.");
                return hashMap;
            }
            int length = packageInfo.splitNames.length > applicationInfo.splitSourceDirs.length ? applicationInfo.splitSourceDirs.length : packageInfo.splitNames.length;
            for (int i = 0; i < length; i++) {
                hashMap.put(packageInfo.splitNames[i], applicationInfo.splitSourceDirs[i]);
            }
            return hashMap;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "cannot find the package info.");
        }
    }
}
