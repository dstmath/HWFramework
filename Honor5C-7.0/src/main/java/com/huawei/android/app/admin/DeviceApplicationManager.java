package com.huawei.android.app.admin;

import android.content.ComponentName;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.List;

public class DeviceApplicationManager {
    private static final String TAG = "DeviceApplicationManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DeviceApplicationManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

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
}
