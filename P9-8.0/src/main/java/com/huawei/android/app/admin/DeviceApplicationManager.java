package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;

public class DeviceApplicationManager {
    private static final String INSTALL_APKS_BLACK_LIST = "install-packages-black-list";
    public static final String LIST_IGNORE_FREQUENT_RELAUNCH_APP = "ignore-frequent-relaunch-app";
    public static final String LIST_IGNORE_FREQUENT_RELAUNCH_APP_ITEM = "ignore-frequent-relaunch-app/ignore-frequent-relaunch-app-item";
    public static final String LIST_ITEM_KEY = "value";
    public static final String POLICY_SINGLE_APP = "policy-single-app";
    private static final String TAG = "DeviceApplicationManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public void addPersistentApp(ComponentName admin, List<String> packageNames) {
        this.mDpm.addPersistentApp(admin, packageNames);
    }

    public void removePersistentApp(ComponentName admin, List<String> packageNames) {
        this.mDpm.removePersistentApp(admin, packageNames);
    }

    public List<String> getPersistentApp(ComponentName admin) {
        return this.mDpm.getPersistentApp(admin);
    }

    public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mDpm.addDisallowedRunningApp(admin, packageNames);
    }

    public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mDpm.removeDisallowedRunningApp(admin, packageNames);
    }

    public List<String> getDisallowedRunningApp(ComponentName admin) {
        return this.mDpm.getDisallowedRunningApp(admin);
    }

    public void killApplicationProcess(ComponentName admin, String packageName) {
        this.mDpm.killApplicationProcess(admin, packageName);
    }

    public boolean addInstallPackageBlackList(ComponentName admin, ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.size() == 0) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", packageNames);
        return this.mDpm.setPolicy(admin, INSTALL_APKS_BLACK_LIST, bundle);
    }

    public ArrayList<String> getInstallPackageBlackList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, INSTALL_APKS_BLACK_LIST);
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return null;
    }

    public boolean removeInstallPackageBlackList(ComponentName admin, ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.size() == 0) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", packageNames);
        return this.mDpm.removePolicy(admin, INSTALL_APKS_BLACK_LIST, bundle);
    }

    public boolean addIgnoreFrequentRelaunchAppList(ComponentName admin, ArrayList<String> apps) {
        if (apps == null || apps.isEmpty()) {
            throw new IllegalArgumentException("app list is empty");
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", apps);
        return this.mDpm.setPolicy(admin, LIST_IGNORE_FREQUENT_RELAUNCH_APP, bundle);
    }

    public boolean removeIgnoreFrequentRelaunchAppList(ComponentName admin, ArrayList<String> apps) {
        if (apps == null || apps.isEmpty()) {
            throw new IllegalArgumentException("app list is empty");
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", apps);
        return this.mDpm.removePolicy(admin, LIST_IGNORE_FREQUENT_RELAUNCH_APP, bundle);
    }

    public ArrayList<String> getIgnoreFrequentRelaunchAppList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, LIST_IGNORE_FREQUENT_RELAUNCH_APP);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean addSingleApp(ComponentName admin, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("package name is empty");
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", packageName);
        return this.mDpm.setPolicy(admin, POLICY_SINGLE_APP, bundle);
    }

    public boolean clearSingleApp(ComponentName admin, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("package name is empty");
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", packageName);
        return this.mDpm.removePolicy(admin, POLICY_SINGLE_APP, bundle);
    }

    public String getSingleApp(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SINGLE_APP);
        if (bundle == null) {
            return null;
        }
        return bundle.getString("value");
    }
}
