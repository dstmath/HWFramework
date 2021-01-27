package com.huawei.android.app;

import android.app.ApplicationPackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ApplicationPackageManagerEx {
    private ApplicationPackageManager mApplicationPackageManager;

    @HwSystemApi
    public ApplicationPackageManagerEx(PackageManager pm) {
        this.mApplicationPackageManager = (ApplicationPackageManager) pm;
    }

    @HwSystemApi
    public static boolean isApplicationPackageManager(PackageManager pm) {
        return pm instanceof ApplicationPackageManager;
    }

    @HwSystemApi
    public CharSequence getCachedString(ResourceNameEx nameEx) {
        return this.mApplicationPackageManager.getCachedString(nameEx.mResourceName);
    }

    @HwSystemApi
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return this.mApplicationPackageManager.getApplicationInfo(packageName, flags);
    }

    @HwSystemApi
    public Resources getResourcesForApplication(ApplicationInfo app) throws PackageManager.NameNotFoundException {
        return this.mApplicationPackageManager.getResourcesForApplication(app);
    }

    @HwSystemApi
    public void putCachedString(ResourceNameEx nameEx, CharSequence cs) {
        this.mApplicationPackageManager.putCachedString(nameEx.mResourceName, cs);
    }

    public static class ResourceNameEx {
        private ApplicationPackageManager.ResourceName mResourceName;

        @HwSystemApi
        public ResourceNameEx(String packageName, int iconId) {
            this.mResourceName = new ApplicationPackageManager.ResourceName(packageName, iconId);
        }
    }
}
