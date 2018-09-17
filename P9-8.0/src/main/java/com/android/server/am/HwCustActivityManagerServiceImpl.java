package com.android.server.am;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class HwCustActivityManagerServiceImpl extends HwCustActivityManagerService {
    private static final boolean IS_IQI_Enable = SystemProperties.getBoolean("ro.config.iqi_att_support", false);
    static final String TAG = "HwCustAMSImpl";
    private boolean mAllowMemoryCompress = SystemProperties.getBoolean("ro.config.enable_rcc", false);
    private String mBlackListPkg = null;
    private boolean mDelaySwitchUserDlg = SystemProperties.getBoolean("ro.config.DelaySwitchUserDlg", false);

    protected boolean isIQIEnable() {
        return IS_IQI_Enable;
    }

    protected boolean shouldDelaySwitchUserDlg() {
        return this.mDelaySwitchUserDlg;
    }

    protected boolean isAllowRamCompress() {
        return this.mAllowMemoryCompress;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0050 A:{SYNTHETIC, Splitter: B:20:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0062 A:{SYNTHETIC, Splitter: B:26:0x0062} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void setEvent(String event) {
        IOException e;
        Throwable th;
        BufferedWriter bw = null;
        try {
            File file = new File("sys/kernel/rcc/event");
            if (file.exists()) {
                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                try {
                    bw2.write(event);
                    bw2.flush();
                    if (bw2 != null) {
                        try {
                            bw2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "can not close file descriptor!");
                        }
                    }
                    bw = bw2;
                } catch (IOException e3) {
                    e = e3;
                    bw = bw2;
                    try {
                        Log.e(TAG, e.toString());
                        if (bw != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bw != null) {
                            try {
                                bw.close();
                            } catch (IOException e4) {
                                Log.e(TAG, "can not close file descriptor!");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bw = bw2;
                    if (bw != null) {
                    }
                    throw th;
                }
            }
            Log.e(TAG, "sys/kernel/rcc/event doesn't exist!");
        } catch (IOException e5) {
            e = e5;
            Log.e(TAG, e.toString());
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e6) {
                    Log.e(TAG, "can not close file descriptor!");
                }
            }
        }
    }

    protected int addProcesstoPersitList(ProcessRecord proc) {
        int maxAdj = proc.maxAdj;
        if (proc.processName.equals("diagandroid.iqd")) {
            return -800;
        }
        return maxAdj;
    }

    public boolean isInMultiWinBlackList(String pkg, ContentResolver resolver) {
        if (this.mBlackListPkg == null) {
            this.mBlackListPkg = Secure.getString(resolver, "multi_window_black_list") + ",";
        }
        if (!this.mBlackListPkg.contains(pkg + ",")) {
            return false;
        }
        Log.d(TAG, "isInMultiWinBlackList pkg:" + pkg);
        return true;
    }
}
