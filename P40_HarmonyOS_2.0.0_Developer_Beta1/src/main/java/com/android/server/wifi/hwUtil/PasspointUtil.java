package com.android.server.wifi.hwUtil;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.WifiConfigStore;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class PasspointUtil {
    private static final String DBKEY_HOTSPOT20_VALUE = "hw_wifi_hotspot2_on";
    private static final String DBKEY_IS_SIM_SUPPORT_HS20 = "hw_is_support_hotspot2";
    private static final String STORE_FILE_NAME = "etc/wifi/WifiConfigStore.xml";
    private static final String TAG = "PasspointUtil";

    public static WifiConfigStore.StoreFile createCustFile() {
        File file = HwCfgFilePolicy.getCfgFile(STORE_FILE_NAME, 0);
        if (file != null) {
            return new WifiConfigStore.StoreFile(file, 0);
        }
        Log.e(TAG, "Can not find the customer file");
        return null;
    }

    public static boolean ishs2Enabled(Context context) {
        if (!context.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint") || Settings.Global.getInt(context.getContentResolver(), DBKEY_HOTSPOT20_VALUE, 1) != 1) {
            return false;
        }
        return true;
    }

    public static boolean ishs20EanbledBySim(Context context) {
        if (context != null) {
            return "true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), DBKEY_IS_SIM_SUPPORT_HS20));
        }
        return false;
    }
}
