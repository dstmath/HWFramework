package com.huawei.android.content.pm;

import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;

public class PackageInfoEx {
    private PackageInfo mPackageInfo;

    public PackageInfo getmPackageInfo() {
        return this.mPackageInfo;
    }

    public void setmPackageInfo(PackageInfo mPackageInfo2) {
        this.mPackageInfo = mPackageInfo2;
    }

    public String getPackageName() {
        return this.mPackageInfo.packageName;
    }

    public String getVersionName() {
        return this.mPackageInfo.versionName;
    }

    public ActivityInfoEx[] getReceivers() {
        ActivityInfo[] activityInfos = this.mPackageInfo.receivers;
        ActivityInfoEx[] activityInfoExes = new ActivityInfoEx[activityInfos.length];
        for (int i = 0; i < activityInfos.length; i++) {
            activityInfoExes[i] = new ActivityInfoEx();
            activityInfoExes[i].setActivityInfo(activityInfos[i]);
        }
        return activityInfoExes;
    }

    public ActivityInfoEx[] getActivities() {
        ActivityInfo[] activityInfos = this.mPackageInfo.activities;
        ActivityInfoEx[] activityInfoExes = new ActivityInfoEx[activityInfos.length];
        for (int i = 0; i < activityInfos.length; i++) {
            activityInfoExes[i] = new ActivityInfoEx();
            activityInfoExes[i].setActivityInfo(activityInfos[i]);
        }
        return activityInfoExes;
    }

    public ServiceInfoEx[] getServices() {
        ServiceInfo[] serviceInfos = this.mPackageInfo.services;
        ServiceInfoEx[] serviceInfosExes = new ServiceInfoEx[serviceInfos.length];
        for (int i = 0; i < serviceInfos.length; i++) {
            serviceInfosExes[i] = new ServiceInfoEx();
            serviceInfosExes[i].setServiceInfo(serviceInfos[i]);
        }
        return serviceInfosExes;
    }

    public ProviderInfoEx[] getProviders() {
        ProviderInfo[] providerInfos = this.mPackageInfo.providers;
        ProviderInfoEx[] providerInfoExes = new ProviderInfoEx[providerInfos.length];
        for (int i = 0; i < providerInfos.length; i++) {
            providerInfoExes[i] = new ProviderInfoEx();
            providerInfoExes[i].setProviderInfo(providerInfos[i]);
        }
        return providerInfoExes;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mPackageInfo.applicationInfo;
    }

    public Signature[] getSignatures() {
        return this.mPackageInfo.signatures;
    }

    public String[] requestedPermissions() {
        return this.mPackageInfo.requestedPermissions;
    }
}
