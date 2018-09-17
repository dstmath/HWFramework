package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiSettingsStore {
    private static final String TAG = "WifiSettingsStore";
    static final int WIFI_DISABLED = 0;
    private static final int WIFI_DISABLED_AIRPLANE_ON = 3;
    static final int WIFI_ENABLED = 1;
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    private boolean mAirplaneModeOn;
    private boolean mCheckSavedStateAtBoot;
    private final Context mContext;
    private int mPersistWifiState;
    private boolean mScanAlwaysAvailable;

    WifiSettingsStore(Context context) {
        this.mPersistWifiState = WIFI_DISABLED;
        this.mAirplaneModeOn = false;
        this.mCheckSavedStateAtBoot = false;
        this.mContext = context;
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        this.mPersistWifiState = getPersistedWifiState();
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    public synchronized boolean isWifiToggleEnabled() {
        boolean z = true;
        synchronized (this) {
            if (!this.mCheckSavedStateAtBoot) {
                this.mCheckSavedStateAtBoot = true;
                if (testAndClearWifiSavedState()) {
                    return true;
                }
            }
            if (this.mAirplaneModeOn) {
                if (this.mPersistWifiState != WIFI_ENABLED_AIRPLANE_OVERRIDE) {
                    z = false;
                }
                return z;
            }
            if (this.mPersistWifiState == 0) {
                z = false;
            }
            return z;
        }
    }

    public synchronized boolean isAirplaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public synchronized boolean isScanAlwaysAvailable() {
        return !this.mAirplaneModeOn ? this.mScanAlwaysAvailable : false;
    }

    public synchronized boolean handleWifiToggled(boolean wifiEnabled) {
        if (this.mAirplaneModeOn && !isAirplaneToggleable()) {
            return false;
        }
        if (!wifiEnabled) {
            persistWifiState(WIFI_DISABLED);
        } else if (this.mAirplaneModeOn) {
            persistWifiState(WIFI_ENABLED_AIRPLANE_OVERRIDE);
        } else {
            persistWifiState(WIFI_ENABLED);
        }
        return true;
    }

    synchronized boolean handleAirplaneModeToggled() {
        if (isAirplaneSensitive()) {
            this.mAirplaneModeOn = getPersistedAirplaneModeOn();
            Log.d(TAG, "mAirplaneModeOn:" + this.mAirplaneModeOn + ", mPersistWifiState:" + this.mPersistWifiState);
            if (this.mAirplaneModeOn) {
                if (this.mPersistWifiState == WIFI_ENABLED) {
                    persistWifiState(WIFI_DISABLED_AIRPLANE_ON);
                }
            } else if (testAndClearWifiSavedState() || this.mPersistWifiState == WIFI_ENABLED_AIRPLANE_OVERRIDE) {
                persistWifiState(WIFI_ENABLED);
            }
            return true;
        }
        Log.d(TAG, "airplane not sensitive");
        return false;
    }

    synchronized void handleWifiScanAlwaysAvailableToggled() {
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mPersistWifiState " + this.mPersistWifiState);
        pw.println("mAirplaneModeOn " + this.mAirplaneModeOn);
    }

    private void persistWifiState(int state) {
        ContentResolver cr = this.mContext.getContentResolver();
        this.mPersistWifiState = state;
        Global.putInt(cr, "wifi_on", state);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if (airplaneModeRadios != null) {
            return airplaneModeRadios.contains("wifi");
        }
        return true;
    }

    private boolean isAirplaneToggleable() {
        String toggleableRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        if (toggleableRadios != null) {
            return toggleableRadios.contains("wifi");
        }
        return false;
    }

    private boolean testAndClearWifiSavedState() {
        int wifiSavedState = getWifiSavedState();
        if (wifiSavedState == WIFI_ENABLED) {
            setWifiSavedState(WIFI_DISABLED);
        }
        Log.d(TAG, "wifiSavedState:" + wifiSavedState);
        if (wifiSavedState == WIFI_ENABLED) {
            return true;
        }
        return false;
    }

    public void setWifiSavedState(int state) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_saved_state", state);
    }

    public int getWifiSavedState() {
        try {
            return Global.getInt(this.mContext.getContentResolver(), "wifi_saved_state");
        } catch (SettingNotFoundException e) {
            return WIFI_DISABLED;
        }
    }

    private int getPersistedWifiState() {
        ContentResolver cr = this.mContext.getContentResolver();
        try {
            return Global.getInt(cr, "wifi_on");
        } catch (SettingNotFoundException e) {
            Global.putInt(cr, "wifi_on", WIFI_DISABLED);
            return WIFI_DISABLED;
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", WIFI_DISABLED) == WIFI_ENABLED;
    }

    private boolean getPersistedScanAlwaysAvailable() {
        boolean ret = true;
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", WIFI_DISABLED) != WIFI_ENABLED) {
            ret = false;
        }
        if (!ret || !"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            return ret;
        }
        Log.d(TAG, "factory version, WIFI_SCAN_ALWAYS_AVAILABLE doesnt work");
        return false;
    }
}
