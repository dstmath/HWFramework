package com.android.internal.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ExitCatch {
    private static final boolean DEBUG = false;
    public static final int EXIT_CATCH_ABORT_FLAG = 6;
    public static final int EXIT_CATCH_FORAPP_FLAG = 8;
    public static final int KILL_CATCH_FLAG = 1;
    private static final String TAG = "RMS.ExitCatch";

    public static boolean enable(int pid, int flags) {
        boolean ret = writeFile("/proc/" + pid + "/unexpected_die_catch", String.valueOf(flags));
        Log.w(TAG, "now ExitCatch is enable in pid =" + pid + " ret = " + ret);
        return ret;
    }

    public static boolean disable(int pid) {
        boolean ret = writeFile("/proc/" + pid + "/unexpected_die_catch", WifiEnterpriseConfig.ENGINE_DISABLE);
        Log.w(TAG, "now ExitCatch is disable in pid =" + pid + " ret = " + ret);
        return ret;
    }

    private static final boolean writeFile(String path, String data) {
        FileOutputStream fos = null;
        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, "ExitCatch file notexist " + path);
            return false;
        }
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            try {
                fos.close();
            } catch (IOException e) {
            }
            return true;
        } catch (IOException e2) {
            Log.w(TAG, "Unable to write " + path + " msg=" + e2.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e4) {
                }
            }
            return true;
        }
    }
}
