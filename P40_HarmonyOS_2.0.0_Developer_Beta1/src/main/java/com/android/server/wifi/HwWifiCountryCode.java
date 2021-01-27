package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import java.util.Locale;

public class HwWifiCountryCode extends WifiCountryCode {
    private static final String EXTRA_FORCE_SET_WIFI_CCODE = "isWifiConnected";
    private static final String EXTRA_SOFTAP_COUNTRY_CODE = "softApCountryCode";
    private static final String HW_SYSTEM_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final String SOFTAP_COUNTRY_CODE_CHANGED_ACTION = "com.huawei.android.SOFTAP_CCODE_UPDATE";
    private static final String TAG = "HwWifiCountryCode";
    private static final Object sLock = new Object();
    private int apState = 11;
    private CodeReceiver mCodeReceiver;
    private Context mContext = null;
    private HwWiFiCCode mHwWiFiCCode;
    private boolean mIsSetWifiCountryCode = SystemProperties.getBoolean("ro.config.wifi_country_code", true);
    private WifiManager mWifiManager;
    private final WifiNative mWifiNative;

    private class CodeReceiver extends BroadcastReceiver {
        private CodeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.net.wifi.countryCode".equals(intent.getAction())) {
                HwHiLog.d(HwWifiCountryCode.TAG, false, "com.android.net.wifi.countryCode is RECEIVER", new Object[0]);
                String countryCode = Settings.Global.getString(context.getContentResolver(), "wifi_country_code");
                boolean isWifiConnected = intent.getBooleanExtra(HwWifiCountryCode.EXTRA_FORCE_SET_WIFI_CCODE, false);
                if (isWifiConnected) {
                    HwWifiCountryCode.this.setReadyForChange(true);
                }
                HwWifiCountryCode.this.setCountryCode(countryCode);
                if (isWifiConnected) {
                    HwWifiCountryCode.this.setReadyForChange(false);
                }
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                HwWifiCountryCode.this.apState = intent.getIntExtra("wifi_state", 11);
                HwHiLog.d(HwWifiCountryCode.TAG, false, "android.net.wifi.WIFI_AP_STATE_CHANGED is RECEIVER, state = %{public}d", new Object[]{Integer.valueOf(HwWifiCountryCode.this.apState)});
            }
        }
    }

    public HwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        super(wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
        this.mContext = context;
        this.mWifiNative = wifiNative;
        if (this.mIsSetWifiCountryCode) {
            this.mHwWiFiCCode = new HwWiFiCCode(context);
            this.mCodeReceiver = new CodeReceiver();
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("com.android.net.wifi.countryCode");
            myfilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
            this.mContext.registerReceiver(this.mCodeReceiver, myfilter);
        }
    }

    public String getWifiCountryCode(Context context, String countryCode) {
        HwWiFiCCode hwWiFiCCode;
        if (!this.mIsSetWifiCountryCode || (hwWiFiCCode = this.mHwWiFiCCode) == null) {
            return countryCode;
        }
        String wcountryCode = hwWiFiCCode.getActiveCountryCode();
        return (wcountryCode.isEmpty() || wcountryCode.equals(countryCode)) ? countryCode : wcountryCode;
    }

    public String getCountryCodeSentToDriver() {
        String wifiCountryCode;
        synchronized (sLock) {
            wifiCountryCode = getWifiCountryCode(this.mContext, HwWifiCountryCode.super.getCountryCodeSentToDriver());
        }
        return wifiCountryCode;
    }

    public boolean setCountryCode(String countryCode) {
        synchronized (sLock) {
            if (this.mHwWiFiCCode != null) {
                countryCode = this.mHwWiFiCCode.getActiveCountryCode();
            }
            String countryCodeExistInDB = Settings.Global.getString(this.mContext.getContentResolver(), "wifi_country_code");
            if (countryCode != null && !countryCode.equalsIgnoreCase(countryCodeExistInDB) && this.apState == 13) {
                if (this.mWifiManager == null) {
                    this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                }
                WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
                if (wifiConfig != null && !this.mWifiNative.setCountryCodeHal(this.mWifiNative.getSoftApInterfaceName(), countryCode.toUpperCase(Locale.ENGLISH)) && wifiConfig.apBand == 1) {
                    HwHiLog.e(TAG, false, "Failed to set country code, required for setting up soft ap in 5GHz", new Object[0]);
                    return false;
                }
            }
            boolean result = HwWifiCountryCode.super.setCountryCode(countryCode);
            String countryCodeReadyInsertToDB = getCountryCodeSentToDriver();
            if (countryCodeExistInDB == null || !countryCodeExistInDB.equals(countryCodeReadyInsertToDB)) {
                Settings.Global.putString(this.mContext.getContentResolver(), "wifi_country_code", countryCodeReadyInsertToDB);
            } else {
                HwHiLog.d(TAG, false, "the countryCode already exist in DB which ready to insert", new Object[0]);
            }
            if (HwSoftApManager.shouldUseLiteUi() && countryCode != null && !countryCode.equalsIgnoreCase(countryCodeExistInDB) && this.apState == 13) {
                HwHiLog.d(TAG, false, "sendBroadcast countryCode change from %{private}s to %{private}s", new Object[]{countryCodeExistInDB, countryCode});
                Intent intent = new Intent(SOFTAP_COUNTRY_CODE_CHANGED_ACTION);
                intent.setFlags(67108864);
                intent.setPackage("com.android.settings");
                intent.putExtra(EXTRA_SOFTAP_COUNTRY_CODE, countryCode);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, HW_SYSTEM_PERMISSION);
            }
            return result;
        }
    }
}
