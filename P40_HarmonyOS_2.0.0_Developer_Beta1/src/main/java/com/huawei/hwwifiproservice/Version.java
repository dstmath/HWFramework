package com.huawei.hwwifiproservice;

import android.content.Context;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class Version {
    private static final String BETA_CFG_FILE = "/system/etc/log_collect_service_beta.xml";
    private static final int BETA_VER = 1;
    private static final int NON_BETA_VER = 2;
    private static final int UNKNOWN_VER = 0;
    private static int mVersion = 0;

    public static boolean isBeta(Context context) {
        if (mVersion != 0) {
            return false;
        }
        if (HwCfgFilePolicy.getCfgFile("log_collect_service_beta.xml", 0) != null) {
            Log.i("wifipro", "isBetaUser");
            mVersion = 1;
        } else if (new File(BETA_CFG_FILE).exists()) {
            Log.i("wifipro", "isBetaUser");
            mVersion = 1;
        } else {
            mVersion = 2;
        }
        if (mVersion == 1) {
            return true;
        }
        return false;
    }
}
