package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;

public class HwWifiCountryCode extends WifiCountryCode {
    private static final String TAG = "HwWifiCountryCode";
    private CodeReceiver mCodeReceiver;
    private Context mContext;
    private HwWiFiCCode mHwWiFiCCode;
    private boolean mIsSetWifiCountryCode;

    private class CodeReceiver extends BroadcastReceiver {
        private CodeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.android.net.wifi.countryCode".equals(intent.getAction())) {
                Log.d(HwWifiCountryCode.TAG, "com.android.net.wifi.countryCode is RECEIVER");
                HwWifiCountryCode.this.setCountryCode(Global.getString(context.getContentResolver(), "wifi_country_code"), true);
            }
        }
    }

    public HwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, String persistentCountryCode, boolean revertCountryCodeOnCellularLoss) {
        super(wifiNative, oemDefaultCountryCode, persistentCountryCode, revertCountryCodeOnCellularLoss);
        this.mContext = null;
        this.mIsSetWifiCountryCode = SystemProperties.getBoolean("ro.config.wifi_country_code", true);
        this.mContext = context;
        if (this.mIsSetWifiCountryCode) {
            this.mHwWiFiCCode = new HwWiFiCCode(context);
            this.mCodeReceiver = new CodeReceiver();
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("com.android.net.wifi.countryCode");
            this.mContext.registerReceiver(this.mCodeReceiver, myfilter);
        }
    }

    public String getWifiCountryCode(Context context, String countryCode) {
        if (!this.mIsSetWifiCountryCode) {
            return countryCode;
        }
        if (this.mHwWiFiCCode != null) {
            String wcountryCode = this.mHwWiFiCCode.getActiveCountryCode();
            if (!(wcountryCode.isEmpty() || wcountryCode.equals(countryCode))) {
                countryCode = wcountryCode;
            }
        }
        return countryCode;
    }

    public synchronized String getCurrentCountryCode() {
        return getWifiCountryCode(this.mContext, super.getCurrentCountryCode());
    }

    public synchronized boolean setCountryCode(String countryCode, boolean persist) {
        boolean result;
        if (this.mHwWiFiCCode != null) {
            countryCode = this.mHwWiFiCCode.getActiveCountryCode();
        }
        result = super.setCountryCode(countryCode, persist);
        Global.putString(this.mContext.getContentResolver(), "wifi_country_code", getCurrentCountryCode());
        return result;
    }
}
