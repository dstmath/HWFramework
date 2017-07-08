package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.List;

public class DeviceControlManager {
    private static final String TAG = "DeviceControlManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DeviceControlManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

    public void shutdownDevice(ComponentName admin) {
        this.mDpm.shutdownDevice(admin);
    }

    public void rebootDevice(ComponentName admin) {
        this.mDpm.rebootDevice(admin);
    }

    public boolean isRooted(ComponentName admin) {
        return this.mDpm.isRooted(admin);
    }

    public void turnOnGPS(ComponentName admin, boolean on) {
        this.mDpm.turnOnGPS(admin, on);
    }

    public boolean isGPSTurnOn(ComponentName admin) {
        return this.mDpm.isGPSTurnOn(admin);
    }

    public void setSysTime(ComponentName admin, long millis) {
        this.mDpm.setSysTime(admin, millis);
    }

    public void setCustomSettingsMenu(ComponentName admin, List<String> menusToDelete) {
        this.mDpm.setCustomSettingsMenu(admin, menusToDelete);
    }

    public void setDefaultLauncher(ComponentName admin, String packageName, String className) {
        this.mDpm.setDefaultLauncher(admin, packageName, className);
    }

    public void clearDefaultLauncher(ComponentName admin) {
        this.mDpm.clearDefaultLauncher(admin);
    }

    public Bitmap captureScreen(ComponentName admin) {
        return this.mDpm.captureScreen(admin);
    }
}
