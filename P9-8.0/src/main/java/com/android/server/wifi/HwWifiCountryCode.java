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
    private static final Object sLock = new Object();
    private CodeReceiver mCodeReceiver;
    private Context mContext = null;
    private HwWiFiCCode mHwWiFiCCode;
    private boolean mIsSetWifiCountryCode = SystemProperties.getBoolean("ro.config.wifi_country_code", true);

    private class CodeReceiver extends BroadcastReceiver {
        /* synthetic */ CodeReceiver(HwWifiCountryCode this$0, CodeReceiver -this1) {
            this();
        }

        private CodeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.android.net.wifi.countryCode".equals(intent.getAction())) {
                Log.d(HwWifiCountryCode.TAG, "com.android.net.wifi.countryCode is RECEIVER");
                HwWifiCountryCode.this.setCountryCode(Global.getString(context.getContentResolver(), "wifi_country_code"));
            }
        }
    }

    public HwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        super(wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
        this.mContext = context;
        if (this.mIsSetWifiCountryCode) {
            this.mHwWiFiCCode = new HwWiFiCCode(context);
            this.mCodeReceiver = new CodeReceiver(this, null);
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
            if (!(wcountryCode.isEmpty() || (wcountryCode.equals(countryCode) ^ 1) == 0)) {
                countryCode = wcountryCode;
            }
        }
        return countryCode;
    }

    public String getCountryCodeSentToDriver() {
        String wifiCountryCode;
        synchronized (sLock) {
            wifiCountryCode = getWifiCountryCode(this.mContext, super.getCountryCodeSentToDriver());
        }
        return wifiCountryCode;
    }

    public boolean setCountryCode(String countryCode) {
        boolean result;
        synchronized (sLock) {
            if (this.mHwWiFiCCode != null) {
                countryCode = this.mHwWiFiCCode.getActiveCountryCode();
            }
            result = super.setCountryCode(countryCode);
            String countryCodeExistInDB = Global.getString(this.mContext.getContentResolver(), "wifi_country_code");
            String countryCodeReadyInsertToDB = getCountryCodeSentToDriver();
            if (countryCodeExistInDB == null || !countryCodeExistInDB.equals(countryCodeReadyInsertToDB)) {
                Global.putString(this.mContext.getContentResolver(), "wifi_country_code", countryCodeReadyInsertToDB);
            } else {
                Log.d(TAG, "the countryCode already exist in DB which ready to insert");
            }
        }
        return result;
    }
}
