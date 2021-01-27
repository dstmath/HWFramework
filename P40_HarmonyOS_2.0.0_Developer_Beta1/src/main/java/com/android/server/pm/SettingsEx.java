package com.android.server.pm;

import android.util.ArrayMap;
import android.util.SparseArray;

public class SettingsEx {
    private Settings mSettings;

    public Settings getSettings() {
        return this.mSettings;
    }

    public void setSettings(Settings settings) {
        this.mSettings = settings;
    }

    public ArrayMap<String, PackageSettingEx> getPackages() {
        ArrayMap<String, PackageSetting> settingMap = this.mSettings.mPackages;
        ArrayMap<String, PackageSettingEx> settingExMap = new ArrayMap<>();
        for (int i = 0; i < settingMap.size(); i++) {
            PackageSettingEx valueEx = new PackageSettingEx();
            valueEx.setPackageSetting(settingMap.valueAt(i));
            settingExMap.put(settingMap.keyAt(i), valueEx);
        }
        return settingExMap;
    }

    public void writeLPr() {
        this.mSettings.writeLPr();
    }

    public PackageSettingEx getPackageLPr(String pkgName) {
        PackageSetting setting = this.mSettings.getPackageLPr(pkgName);
        PackageSettingEx packageSettingEx = new PackageSettingEx();
        packageSettingEx.setPackageSetting(setting);
        return packageSettingEx;
    }

    public SparseArray<PreferredIntentResolverEx> getPreferredActivities() {
        SparseArray<PreferredIntentResolver> preferredArray = this.mSettings.mPreferredActivities;
        SparseArray<PreferredIntentResolverEx> preferredExArray = new SparseArray<>();
        for (int i = 0; i < preferredArray.size(); i++) {
            PreferredIntentResolverEx resolverEx = new PreferredIntentResolverEx();
            resolverEx.setPreferredIntentResolver(preferredArray.valueAt(i));
            preferredExArray.put(preferredArray.keyAt(i), resolverEx);
        }
        return preferredExArray;
    }

    public ArrayMap<String, PackageSettingEx> getDisabledSysPackages() {
        ArrayMap<String, PackageSetting> settingMap = this.mSettings.getDisabledSysPackages();
        ArrayMap<String, PackageSettingEx> settingExMap = new ArrayMap<>();
        for (int i = 0; i < settingMap.size(); i++) {
            PackageSettingEx valueEx = new PackageSettingEx();
            valueEx.setPackageSetting(settingMap.valueAt(i));
            settingExMap.put(settingMap.keyAt(i), valueEx);
        }
        return settingExMap;
    }

    public boolean isInDelAppList(String packageName) {
        return this.mSettings.isInDelAppList(packageName);
    }
}
