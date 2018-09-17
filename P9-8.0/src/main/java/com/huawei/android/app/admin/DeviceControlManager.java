package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.List;
import java.util.Locale;

public class DeviceControlManager {
    public static final int CERTIFICATE_DEFAULT = 0;
    public static final int CERTIFICATE_PEM_BASE64 = 1;
    public static final int CERTIFICATE_PKCS12 = 0;
    public static final int CERTIFICATE_WIFI = 1;
    public static final String POLICY_TURN_ON_EYE_COMFORT = "device_control_turn_on_eye_comfort";
    private static final String SET_SYSTEM_LANGUAGE = "set-system-language";
    private static final String TAG = "DeviceControlManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

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

    public void setSilentActiveAdmin(ComponentName admin) {
        this.mDpm.setSilentActiveAdmin(admin);
    }

    public boolean formatSDCard(ComponentName who, String diskId) {
        return this.mDpm.formatSDCard(who, diskId);
    }

    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean requestAccess) {
        return this.mDpm.installCertificateWithType(who, type, certBuffer, name, password, flag, requestAccess);
    }

    public boolean setSystemLanguage(ComponentName who, Locale locale) {
        Bundle bundle = new Bundle();
        bundle.putString("locale", locale.toLanguageTag());
        return this.mDpm.setCustomPolicy(who, SET_SYSTEM_LANGUAGE, bundle);
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName) {
        this.mDpm.setDeviceOwnerApp(admin, ownerName);
    }

    public void clearDeviceOwnerApp() {
        this.mDpm.clearDeviceOwnerApp();
    }

    public void turnOnMobiledata(ComponentName admin, boolean on) {
        this.mDpm.turnOnMobiledata(admin, on);
    }

    public void forceMobiledataOn(ComponentName admin) {
        this.mDpm.setDataConnectivityDisabled(admin, true);
        this.mDpm.turnOnMobiledata(admin, true);
    }

    public boolean turnOnEyeComfort(ComponentName admin, boolean on) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", on);
        return this.mDpm.setPolicy(admin, POLICY_TURN_ON_EYE_COMFORT, bundle);
    }

    public boolean isEyeComfortTurnedOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_TURN_ON_EYE_COMFORT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber) {
        return this.mDpm.setCarrierLockScreenPassword(who, password, phoneNumber);
    }

    public boolean clearCarrierLockScreenPassword(ComponentName who, String password) {
        return this.mDpm.clearCarrierLockScreenPassword(who, password);
    }
}
