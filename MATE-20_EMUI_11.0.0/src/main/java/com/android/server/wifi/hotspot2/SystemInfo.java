package com.android.server.wifi.hotspot2;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiNative;
import java.util.Locale;

public class SystemInfo {
    public static final String TAG = "SystemInfo";
    public static final String UNKNOWN_INFO = "Unknown";
    private static SystemInfo sSystemInfo = null;
    private final TelephonyManager mTelephonyManager;
    private final WifiNative mWifiNative;

    @VisibleForTesting
    SystemInfo(Context context, WifiNative wifiNative) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mWifiNative = wifiNative;
    }

    public static SystemInfo getInstance(Context context, WifiNative wifiNative) {
        if (sSystemInfo == null) {
            sSystemInfo = new SystemInfo(context, wifiNative);
        }
        return sSystemInfo;
    }

    public String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getDeviceModel() {
        return Build.MODEL;
    }

    public String getMacAddress(String ifaceName) {
        return this.mWifiNative.getMacAddress(ifaceName);
    }

    public String getDeviceId() {
        TelephonyManager defaultDataTm = this.mTelephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        String imei = defaultDataTm.getImei();
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        String meid = defaultDataTm.getMeid();
        if (!TextUtils.isEmpty(meid)) {
            return meid;
        }
        return UNKNOWN_INFO;
    }

    public String getHwVersion() {
        return Build.HARDWARE + "." + SystemProperties.get("ro.revision", "0");
    }

    public String getSoftwareVersion() {
        StringBuffer stringBuffer = new StringBuffer("Android ");
        stringBuffer.append(Build.VERSION.RELEASE);
        return stringBuffer.toString();
    }

    public String getFirmwareVersion() {
        StringBuffer stringBuffer = new StringBuffer(Build.ID);
        stringBuffer.append("/");
        stringBuffer.append(SystemProperties.get("gsm.version.baseband", UNKNOWN_INFO));
        return stringBuffer.toString();
    }
}
