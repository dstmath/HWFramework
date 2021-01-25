package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiSettingsStore {
    private static final String[] CHANGE_AIRPLANE_MODE_WHITE_PACKAGE_LIST = {WifiConfigManager.SYSUI_PACKAGE_NAME, WifiCommonUtils.PACKAGE_NAME_ASSOCIATE_SETTINGS};
    private static final String TAG = "WifiSettingsStore";
    static final int WIFI_DISABLED = 0;
    private static final int WIFI_DISABLED_AIRPLANE_ON = 3;
    static final int WIFI_ENABLED = 1;
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    private boolean mAirplaneModeOn = false;
    private boolean mCheckSavedStateAtBoot = false;
    private final Context mContext;
    private final IHwWifiSettingsStoreEx mHwWifiSettingsStoreEx;
    private int mPersistWifiState = 0;
    private boolean mScanAlwaysAvailable;

    WifiSettingsStore(Context context) {
        this.mContext = context;
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        this.mPersistWifiState = getPersistedWifiState();
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
        if (!isAirplaneSensitive() && this.mAirplaneModeOn && this.mPersistWifiState == 1) {
            this.mPersistWifiState = 2;
        }
        this.mHwWifiSettingsStoreEx = HwWifiServiceFactory.getHwWifiSettingsStoreEx(this.mContext);
    }

    public synchronized boolean isWifiToggleEnabled() {
        boolean z = true;
        if (!this.mCheckSavedStateAtBoot) {
            this.mCheckSavedStateAtBoot = true;
            if (testAndClearWifiSavedState()) {
                return true;
            }
        }
        if (this.mAirplaneModeOn) {
            if (this.mPersistWifiState != 2) {
                z = false;
            }
            return z;
        }
        if (this.mPersistWifiState == 0) {
            z = false;
        }
        return z;
    }

    public synchronized boolean isAirplaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public synchronized boolean isScanAlwaysAvailable() {
        return !this.mAirplaneModeOn && this.mScanAlwaysAvailable;
    }

    public IHwWifiSettingsStoreEx getHwWifiSettingsStoreEx() {
        return this.mHwWifiSettingsStoreEx;
    }

    private boolean isInChangeAirplaneModeWhiteList(String packageName) {
        for (String whitePackageName : CHANGE_AIRPLANE_MODE_WHITE_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean handleWifiToggled(String packageName, boolean wifiEnabled) {
        if (this.mAirplaneModeOn && this.mHwWifiSettingsStoreEx != null && isInChangeAirplaneModeWhiteList(packageName)) {
            this.mHwWifiSettingsStoreEx.changeAirplaneModeRadios(this.mAirplaneModeOn, wifiEnabled);
        }
        return handleWifiToggled(wifiEnabled);
    }

    public synchronized boolean handleWifiToggled(boolean wifiEnabled) {
        if (this.mAirplaneModeOn && !isAirplaneToggleable()) {
            return false;
        }
        if (!wifiEnabled) {
            persistWifiState(0);
        } else if (this.mAirplaneModeOn) {
            persistWifiState(2);
        } else {
            persistWifiState(1);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean handleAirplaneModeToggled() {
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        Log.i(TAG, "mAirplaneModeOn:" + this.mAirplaneModeOn);
        if (!isAirplaneSensitive()) {
            if (!this.mAirplaneModeOn && this.mPersistWifiState == 2) {
                persistWifiState(1);
            }
            Log.i(TAG, "airplane not sensitive");
            return false;
        }
        Log.i(TAG, "mPersistWifiState:" + this.mPersistWifiState);
        if (this.mAirplaneModeOn) {
            if (this.mPersistWifiState == 1) {
                persistWifiState(3);
            }
        } else if (testAndClearWifiSavedState() || this.mPersistWifiState == 2 || this.mPersistWifiState == 3) {
            persistWifiState(1);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public synchronized void handleWifiScanAlwaysAvailableToggled() {
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mPersistWifiState " + this.mPersistWifiState);
        pw.println("mAirplaneModeOn " + this.mAirplaneModeOn);
    }

    private void persistWifiState(int state) {
        ContentResolver cr = this.mContext.getContentResolver();
        this.mPersistWifiState = state;
        Settings.Global.putInt(cr, "wifi_on", state);
        Log.d(TAG, "Persist wifi state is " + state);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        return airplaneModeRadios == null || airplaneModeRadios.contains("wifi");
    }

    private boolean isAirplaneToggleable() {
        String toggleableRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        return toggleableRadios != null && toggleableRadios.contains("wifi");
    }

    private boolean testAndClearWifiSavedState() {
        int wifiSavedState = getWifiSavedState();
        if (wifiSavedState == 1) {
            setWifiSavedState(0);
        }
        Log.i(TAG, "wifiSavedState:" + wifiSavedState);
        return wifiSavedState == 1;
    }

    public void setWifiSavedState(int state) {
        Log.d(TAG, "Saved wifi state is " + state);
        Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_saved_state", state);
    }

    public int getWifiSavedState() {
        try {
            return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_saved_state");
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Get wifi saved state but not found!");
            return 0;
        }
    }

    private int getPersistedWifiState() {
        ContentResolver cr = this.mContext.getContentResolver();
        try {
            return Settings.Global.getInt(cr, "wifi_on");
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Get persisted wifi state but not found!");
            Settings.Global.putInt(cr, "wifi_on", 0);
            return 0;
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean getPersistedScanAlwaysAvailable() {
        boolean ret = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
            ret = false;
        }
        if (!ret || !"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            return ret;
        }
        Log.i(TAG, "factory version, WIFI_SCAN_ALWAYS_AVAILABLE doesnt work");
        return false;
    }
}
