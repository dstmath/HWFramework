package com.android.server.wifi;

import android.text.TextUtils;
import android.util.Log;

public class WifiCountryCode {
    private static final String TAG = "WifiCountryCode";
    private boolean DBG;
    private String mCurrentCountryCode;
    private String mDefaultCountryCode;
    private boolean mReady;
    private boolean mRevertCountryCodeOnCellularLoss;
    private String mTelephonyCountryCode;
    private final WifiNative mWifiNative;

    public WifiCountryCode(WifiNative wifiNative, String oemDefaultCountryCode, String persistentCountryCode, boolean revertCountryCodeOnCellularLoss) {
        this.DBG = false;
        this.mReady = false;
        this.mDefaultCountryCode = null;
        this.mTelephonyCountryCode = null;
        this.mCurrentCountryCode = null;
        this.mWifiNative = wifiNative;
        this.mRevertCountryCodeOnCellularLoss = revertCountryCodeOnCellularLoss;
        if (!TextUtils.isEmpty(persistentCountryCode)) {
            this.mDefaultCountryCode = persistentCountryCode.toUpperCase();
        } else if (!TextUtils.isEmpty(oemDefaultCountryCode)) {
            this.mDefaultCountryCode = oemDefaultCountryCode.toUpperCase();
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.w(TAG, "config_wifi_revert_country_code_on_cellular_loss is set, but there is no default country code.");
            this.mRevertCountryCodeOnCellularLoss = false;
            return;
        }
        if (this.mRevertCountryCodeOnCellularLoss) {
            Log.d(TAG, "Country code will be reverted to " + this.mDefaultCountryCode + " on MCC loss");
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
    }

    public synchronized void simCardRemoved() {
        if (this.DBG) {
            Log.d(TAG, "SIM Card Removed");
        }
        if (this.mRevertCountryCodeOnCellularLoss) {
            this.mTelephonyCountryCode = null;
            if (this.mReady) {
                updateCountryCode();
            }
        }
    }

    public synchronized void airplaneModeEnabled() {
        if (this.DBG) {
            Log.d(TAG, "Airplane Mode Enabled");
        }
        this.mTelephonyCountryCode = null;
        if (this.mRevertCountryCodeOnCellularLoss) {
            this.mTelephonyCountryCode = null;
        }
    }

    public synchronized void setReadyForChange(boolean ready) {
        if (this.DBG) {
            Log.d(TAG, "Set ready: " + ready);
        }
        this.mReady = ready;
        if (this.mReady) {
            updateCountryCode();
        }
    }

    public synchronized boolean setCountryCode(String countryCode, boolean persist) {
        if (this.DBG) {
            Log.d(TAG, "Receive set country code request: " + countryCode);
        }
        if (TextUtils.isEmpty(countryCode)) {
            if (this.DBG) {
                Log.d(TAG, "Ignore empty country code");
            }
            return false;
        }
        if (persist) {
            this.mDefaultCountryCode = countryCode;
        }
        this.mTelephonyCountryCode = countryCode.toUpperCase();
        if (this.mReady) {
            updateCountryCode();
        }
        return true;
    }

    public synchronized String getCurrentCountryCode() {
        return this.mCurrentCountryCode;
    }

    private void updateCountryCode() {
        if (this.DBG) {
            Log.d(TAG, "Update country code");
        }
        String country = pickCountryCode();
        if (country.length() != 0) {
            setCountryCodeNative(country);
        }
    }

    private String pickCountryCode() {
        if (this.mTelephonyCountryCode != null) {
            return this.mTelephonyCountryCode;
        }
        if (this.mDefaultCountryCode != null) {
            return this.mDefaultCountryCode;
        }
        return "";
    }

    private boolean setCountryCodeNative(String country) {
        if (this.mWifiNative.setCountryCode(country)) {
            Log.d(TAG, "Succeeded to set country code to: " + country);
            this.mCurrentCountryCode = country;
            return true;
        }
        Log.d(TAG, "Failed to set country code to: " + country);
        return false;
    }
}
