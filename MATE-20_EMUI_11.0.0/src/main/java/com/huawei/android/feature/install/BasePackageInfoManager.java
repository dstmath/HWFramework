package com.huawei.android.feature.install;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.huawei.android.feature.BuildConfig;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BasePackageInfoManager {
    private static final String TAG = BasePackageInfoManager.class.getSimpleName();
    private static volatile BasePackageInfoManager sInstance;
    private Context mContext;
    private long mVersionCode = -1;

    private BasePackageInfoManager(Context context) {
        this.mContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    public static BasePackageInfoManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DynamicModuleManager.class) {
                if (sInstance == null) {
                    sInstance = new BasePackageInfoManager(context);
                }
            }
        }
        return sInstance;
    }

    private String[] getV19HigherFeatures() {
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0);
            return packageInfo != null ? packageInfo.splitNames : new String[0];
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return new String[0];
        }
    }

    private Set<String> getV19LowerFeatures() {
        String string;
        HashSet hashSet = new HashSet();
        try {
            ApplicationInfo applicationInfo = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), 128);
            if (!(applicationInfo == null || applicationInfo.metaData == null || (string = applicationInfo.metaData.getString("com.android.dynamic.apk.fused.modules")) == null || string.isEmpty())) {
                Collections.addAll(hashSet, string.split(",", -1));
                hashSet.remove(BuildConfig.FLAVOR);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return hashSet;
    }

    private static String subFeatureName(String str) {
        return str.split("\\.config\\.")[0];
    }

    public Set<String> getInstalledModules() {
        String[] v19HigherFeatures;
        Set<String> v19LowerFeatures = getV19LowerFeatures();
        if (Build.VERSION.SDK_INT >= 21 && (v19HigherFeatures = getV19HigherFeatures()) != null) {
            for (String str : v19HigherFeatures) {
                if (!str.startsWith("config.")) {
                    v19LowerFeatures.add(subFeatureName(str));
                }
            }
        }
        return v19LowerFeatures;
    }

    public long getVersionCode() {
        if (this.mVersionCode == -1) {
            try {
                this.mVersionCode = (long) this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return this.mVersionCode;
    }
}
