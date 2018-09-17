package com.android.internal.os;

import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ExitCatch {
    private static final boolean DEBUG = false;
    public static final int EXIT_CATCH_ABORT_FLAG = 6;
    public static final int KILL_CATCH_FLAG = 1;
    private static final String TAG = "RMS.ExitCatch";

    public static boolean enable(int pid, int flags) {
        boolean ret = writeFile("/proc/" + pid + "/unexpected_die_catch", String.valueOf(flags));
        Log.w(TAG, "now ExitCatch is enable in pid =" + pid + " ret = " + ret);
        return ret;
    }

    public static boolean disable(int pid) {
        boolean ret = writeFile("/proc/" + pid + "/unexpected_die_catch", "0");
        Log.w(TAG, "now ExitCatch is disable in pid =" + pid + " ret = " + ret);
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x004c A:{SYNTHETIC, Splitter: B:16:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005e A:{SYNTHETIC, Splitter: B:23:0x005e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static final boolean writeFile(String path, String data) {
        IOException e;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes());
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e2) {
                        Log.w(TAG, "find IOException.");
                    }
                }
                return true;
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                try {
                    Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
                    if (fos != null) {
                    }
                    return false;
                } catch (Throwable th) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                            Log.w(TAG, "find IOException.");
                        }
                    }
                    return true;
                }
            } catch (Throwable th2) {
                fos = fos2;
                if (fos != null) {
                }
                return true;
            }
        } catch (IOException e5) {
            e = e5;
            Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e6) {
                    Log.w(TAG, "find IOException.");
                }
            }
            return false;
        }
    }
}
