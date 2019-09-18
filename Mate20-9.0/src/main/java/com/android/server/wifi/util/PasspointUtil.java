package com.android.server.wifi.util;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.WifiConfigStore;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class PasspointUtil {
    private static String[] CFG_DIRS = {"/cust_spec", "/hw_oem", "/data/cust"};
    private static final String DBKEY_HOTSPOT20_VALUE = "hw_wifi_hotspot2_on";
    private static final String DBKEY_IS_SIM_SUPPORT_HS20 = "hw_is_support_hotspot2";
    private static final String STORE_FILE_NAME = "WifiConfigStore.xml";
    private static final String TAG = "PasspointUtil";

    private static File[] searchFile(File folder) {
        File[] subFolders = folder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() || (pathname.isFile() && pathname.getName().equals(PasspointUtil.STORE_FILE_NAME))) {
                    return true;
                }
                return false;
            }
        });
        List<File> result = new ArrayList<>();
        if (subFolders != null) {
            for (File subFile : subFolders) {
                if (subFile.isFile()) {
                    result.add(subFile);
                } else {
                    for (File file : searchFile(subFile)) {
                        result.add(file);
                    }
                }
            }
        }
        return (File[]) result.toArray(new File[0]);
    }

    public static WifiConfigStore.StoreFile createCustFile() {
        for (String dir : CFG_DIRS) {
            File[] result = searchFile(new File(dir));
            if (result.length > 0) {
                Log.i(TAG, dir);
                return new WifiConfigStore.StoreFile(result[0]);
            }
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
