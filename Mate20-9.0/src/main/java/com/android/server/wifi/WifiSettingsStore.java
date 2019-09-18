package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiSettingsStore {
    private static final String TAG = "WifiSettingsStore";
    static final int WIFI_DISABLED = 0;
    private static final int WIFI_DISABLED_AIRPLANE_ON = 3;
    static final int WIFI_ENABLED = 1;
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    private boolean mAirplaneModeOn = false;
    private boolean mCheckSavedStateAtBoot = false;
    private final Context mContext;
    private int mPersistWifiState = 0;
    private boolean mScanAlwaysAvailable;

    WifiSettingsStore(Context context) {
        this.mContext = context;
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        this.mPersistWifiState = getPersistedWifiState();
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        return r1;
     */
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
        } else if (this.mPersistWifiState == 0) {
            z = false;
        }
    }

    public synchronized boolean isAirplaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public synchronized boolean isScanAlwaysAvailable() {
        return !this.mAirplaneModeOn && this.mScanAlwaysAvailable;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        return true;
     */
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
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        return true;
     */
    public synchronized boolean handleAirplaneModeToggled() {
        if (!isAirplaneSensitive()) {
            Log.d(TAG, "airplane not sensitive");
            return false;
        }
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        Log.d(TAG, "mAirplaneModeOn:" + this.mAirplaneModeOn + ", mPersistWifiState:" + this.mPersistWifiState);
        if (this.mAirplaneModeOn) {
            if (this.mPersistWifiState == 1) {
                persistWifiState(3);
            }
        } else if (testAndClearWifiSavedState() || this.mPersistWifiState == 2) {
            persistWifiState(1);
        }
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
        Log.d(TAG, "wifiSavedState:" + wifiSavedState);
        if (wifiSavedState == 1) {
            return true;
        }
        return false;
    }

    public void setWifiSavedState(int state) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_saved_state", state);
    }

    public int getWifiSavedState() {
        try {
            return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_saved_state");
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    private int getPersistedWifiState() {
        ContentResolver cr = this.mContext.getContentResolver();
        try {
            return Settings.Global.getInt(cr, "wifi_on");
        } catch (Settings.SettingNotFoundException e) {
            Settings.Global.putInt(cr, "wifi_on", 0);
            return 0;
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean getPersistedScanAlwaysAvailable() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
            z = false;
        }
        boolean ret = z;
        if (!ret || !"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            return ret;
        }
        Log.d(TAG, "factory version, WIFI_SCAN_ALWAYS_AVAILABLE doesnt work");
        return false;
    }

    public int getLocationModeSetting(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "location_mode", 0);
    }

    public int getLocationModeSetting(Context context, int userid) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "location_mode", 0, userid);
    }
}
