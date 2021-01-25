package com.android.server.wifi;

import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WifiCountryCode {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final String TAG = "WifiCountryCode";
    private boolean DBG = false;
    private String mDefaultCountryCode = null;
    private String mDriverCountryCode = null;
    private String mDriverCountryTimestamp = null;
    private boolean mReady = false;
    private String mReadyTimestamp = null;
    private boolean mRevertCountryCodeOnCellularLoss;
    private String mTelephonyCountryCode = null;
    private String mTelephonyCountryTimestamp = null;
    private final WifiNative mWifiNative;

    public WifiCountryCode(WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        this.mWifiNative = wifiNative;
        this.mRevertCountryCodeOnCellularLoss = revertCountryCodeOnCellularLoss;
        if (!TextUtils.isEmpty(oemDefaultCountryCode)) {
            this.mDefaultCountryCode = oemDefaultCountryCode.toUpperCase(Locale.US);
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.w(TAG, "config_wifi_revert_country_code_on_cellular_loss is set, but there is no default country code.");
            this.mRevertCountryCodeOnCellularLoss = false;
        }
        Log.i(TAG, "mDefaultCountryCode " + this.mDefaultCountryCode + " mRevertCountryCodeOnCellularLoss " + this.mRevertCountryCodeOnCellularLoss);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
    }

    public synchronized void airplaneModeEnabled() {
        Log.d(TAG, "Airplane Mode Enabled");
        this.mTelephonyCountryCode = null;
    }

    public synchronized void setReadyForChange(boolean ready) {
        this.mReady = ready;
        this.mReadyTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        if (this.mReady) {
            updateCountryCode();
        }
    }

    public synchronized boolean setCountryCode(String countryCode) {
        Log.i(TAG, "Receive set country code request");
        this.mTelephonyCountryTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(countryCode)) {
            this.mTelephonyCountryCode = countryCode.toUpperCase(Locale.US);
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.i(TAG, "Received empty country code, reset to default country code");
            this.mTelephonyCountryCode = null;
        }
        if (this.mReady) {
            updateCountryCode();
        } else {
            Log.i(TAG, "skip update supplicant not ready yet");
        }
        return true;
    }

    public synchronized String getCountryCodeSentToDriver() {
        return this.mDriverCountryCode;
    }

    public synchronized String getCountryCode() {
        return pickCountryCode();
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mRevertCountryCodeOnCellularLoss: " + this.mRevertCountryCodeOnCellularLoss);
        pw.println("mDefaultCountryCode: " + this.mDefaultCountryCode);
        pw.println("mDriverCountryCode: " + this.mDriverCountryCode);
        pw.println("mTelephonyCountryCode: " + this.mTelephonyCountryCode);
        pw.println("mTelephonyCountryTimestamp: " + this.mTelephonyCountryTimestamp);
        pw.println("mDriverCountryTimestamp: " + this.mDriverCountryTimestamp);
        pw.println("mReadyTimestamp: " + this.mReadyTimestamp);
        pw.println("mReady: " + this.mReady);
    }

    private void updateCountryCode() {
        String country = pickCountryCode();
        Log.i(TAG, "updateCountryCode");
        if (country != null) {
            setCountryCodeNative(country);
        }
    }

    private String pickCountryCode() {
        String str = this.mTelephonyCountryCode;
        if (str != null) {
            return str;
        }
        String str2 = this.mDefaultCountryCode;
        if (str2 != null) {
            return str2;
        }
        return null;
    }

    private boolean setCountryCodeNative(String country) {
        this.mDriverCountryTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative.setCountryCode(wifiNative.getClientInterfaceName(), country)) {
            Log.d(TAG, "Succeeded to set country code");
            this.mDriverCountryCode = country;
            return true;
        }
        Log.d(TAG, "Failed to set country code");
        return false;
    }
}
