package com.android.server.wifi;

import android.text.TextUtils;
import android.util.Log;

public class WifiCountryCode {
    private static final String TAG = "WifiCountryCode";
    private boolean DBG = false;
    private String mCurrentCountryCode = null;
    private String mDefaultCountryCode = null;
    private boolean mReady = false;
    private boolean mRevertCountryCodeOnCellularLoss;
    private String mTelephonyCountryCode = null;
    private final WifiNative mWifiNative;

    public WifiCountryCode(WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        this.mWifiNative = wifiNative;
        this.mRevertCountryCodeOnCellularLoss = revertCountryCodeOnCellularLoss;
        if (!TextUtils.isEmpty(oemDefaultCountryCode)) {
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

    public synchronized boolean setCountryCode(String countryCode) {
        if (this.DBG) {
            Log.d(TAG, "Receive set country code request: " + countryCode);
        }
        if (TextUtils.isEmpty(countryCode)) {
            if (this.DBG) {
                Log.d(TAG, "Received empty country code, reset to default country code");
            }
            this.mTelephonyCountryCode = null;
        } else {
            this.mTelephonyCountryCode = countryCode.toUpperCase();
        }
        if (this.mReady) {
            updateCountryCode();
        }
        return true;
    }

    public synchronized String getCountryCodeSentToDriver() {
        return this.mCurrentCountryCode;
    }

    public synchronized String getCountryCode() {
        return pickCountryCode();
    }

    private void updateCountryCode() {
        if (this.DBG) {
            Log.d(TAG, "Update country code");
        }
        String country = pickCountryCode();
        if (country != null) {
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
        return null;
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
