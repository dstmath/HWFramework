package com.huawei.android.app.admin;

import android.content.ComponentName;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceRestrictionManager {
    private static final String TAG = "DeviceRestrictionManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DeviceRestrictionManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

    public void setWifiDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setWifiDisabled(admin, disabled);
    }

    public boolean isWifiDisabled(ComponentName admin) {
        return this.mDpm.isWifiDisabled(admin);
    }

    public void setBluetoothDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setBluetoothDisabled(admin, disabled);
    }

    public boolean isBluetoothDisabled(ComponentName admin) {
        return this.mDpm.isBluetoothDisabled(admin);
    }

    public void setWifiApDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setWifiApDisabled(admin, disabled);
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        return this.mDpm.isWifiApDisabled(admin);
    }

    public void setUSBDataDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setUSBDataDisabled(admin, disabled);
    }

    public boolean isUSBDataDisabled(ComponentName admin) {
        return this.mDpm.isUSBDataDisabled(admin);
    }

    public void setExternalStorageDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setExternalStorageDisabled(admin, disabled);
    }

    public boolean isExternalStorageDisabled(ComponentName admin) {
        return this.mDpm.isExternalStorageDisabled(admin);
    }

    public void setNFCDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setNFCDisabled(admin, disabled);
    }

    public boolean isNFCDisabled(ComponentName admin) {
        return this.mDpm.isNFCDisabled(admin);
    }

    public void setDataConnectivityDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setDataConnectivityDisabled(admin, disabled);
    }

    public boolean isDataConnectivityDisabled(ComponentName admin) {
        return this.mDpm.isDataConnectivityDisabled(admin);
    }

    public void setVoiceDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setVoiceDisabled(admin, disabled);
    }

    public boolean isVoiceDisabled(ComponentName admin) {
        return this.mDpm.isVoiceDisabled(admin);
    }

    public void setSMSDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setSMSDisabled(admin, disabled);
    }

    public boolean isSMSDisabled(ComponentName admin) {
        return this.mDpm.isSMSDisabled(admin);
    }

    public void setStatusBarExpandPanelDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setStatusBarExpandPanelDisabled(admin, disabled);
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName admin) {
        return this.mDpm.isStatusBarExpandPanelDisabled(admin);
    }

    public void setSafeModeDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setSafeModeDisabled(admin, disabled);
    }

    public boolean isSafeModeDisabled(ComponentName admin) {
        return this.mDpm.isSafeModeDisabled(admin);
    }

    public void setAdbDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setAdbDisabled(admin, disabled);
    }

    public boolean isAdbDisabled(ComponentName admin) {
        return this.mDpm.isAdbDisabled(admin);
    }

    public void setUSBOtgDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setUSBOtgDisabled(admin, disabled);
    }

    public boolean isUSBOtgDisabled(ComponentName admin) {
        return this.mDpm.isUSBOtgDisabled(admin);
    }

    public void setGPSDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setGPSDisabled(admin, disabled);
    }

    public boolean isGPSDisabled(ComponentName admin) {
        return this.mDpm.isGPSDisabled(admin);
    }

    public void setHomeButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setHomeButtonDisabled(admin, disabled);
    }

    public boolean isHomeButtonDisabled(ComponentName admin) {
        return this.mDpm.isHomeButtonDisabled(admin);
    }

    public void setTaskButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setTaskButtonDisabled(admin, disabled);
    }

    public boolean isTaskButtonDisabled(ComponentName admin) {
        return this.mDpm.isTaskButtonDisabled(admin);
    }

    public void setBackButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setBackButtonDisabled(admin, disabled);
    }

    public boolean isBackButtonDisabled(ComponentName admin) {
        return this.mDpm.isBackButtonDisabled(admin);
    }
}
