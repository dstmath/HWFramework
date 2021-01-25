package com.android.server.am;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public class HwCustActivityManagerServiceImpl extends HwCustActivityManagerService {
    private static final boolean ALLOW_MEMORY_COMPRESS = SystemProperties.getBoolean("ro.config.enable_rcc", false);
    private static final boolean IS_ADD_RESTRICTED = SystemProperties.getBoolean("ro.config.add_restricted_bg", false);
    private static final boolean IS_IQI_ENABLE = SystemProperties.getBoolean("ro.config.iqi_att_support", false);
    private static final String TAG = "HwCustActivityManagerServiceImpl";
    private String mBlackListPkg = null;
    private Set<String> pkgNames = new HashSet<String>() {
        /* class com.android.server.am.HwCustActivityManagerServiceImpl.AnonymousClass1 */

        {
            add("com.nttdocomo.android.homeagent");
        }
    };

    /* access modifiers changed from: protected */
    public boolean isIQIEnable() {
        return IS_IQI_ENABLE;
    }

    /* access modifiers changed from: protected */
    public boolean isAllowRamCompress() {
        return ALLOW_MEMORY_COMPRESS;
    }

    /* access modifiers changed from: protected */
    public void setEvent(String event) {
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File("sys/kernel/rcc/event");
            if (!file.exists()) {
                Log.e(TAG, "sys/kernel/rcc/event doesn't exist!");
                if (0 != 0) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        Log.e(TAG, "can not close file descriptor!");
                    }
                }
            } else {
                BufferedWriter bufferedWriter2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                bufferedWriter2.write(event);
                bufferedWriter2.flush();
                try {
                    bufferedWriter2.close();
                } catch (IOException e2) {
                    Log.e(TAG, "can not close file descriptor!");
                }
            }
        } catch (IOException e3) {
            Log.e(TAG, e3.toString());
            if (0 != 0) {
                bufferedWriter.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bufferedWriter.close();
                } catch (IOException e4) {
                    Log.e(TAG, "can not close file descriptor!");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public int addProcesstoPersitList(ProcessRecord proc) {
        int maxAdj = proc.maxAdj;
        if (proc.processName.equals("diagandroid.iqd")) {
            return -800;
        }
        return maxAdj;
    }

    /* access modifiers changed from: protected */
    public boolean isAddRestrictedForCust(String pkgName) {
        return IS_ADD_RESTRICTED && this.pkgNames.contains(pkgName);
    }

    public boolean isInMultiWinBlackList(String pkgName, ContentResolver resolver) {
        if (this.mBlackListPkg == null) {
            this.mBlackListPkg = Settings.Secure.getString(resolver, "multi_window_black_list") + ",";
        }
        String str = this.mBlackListPkg;
        if (!str.contains(pkgName + ",")) {
            return false;
        }
        Log.d(TAG, "isInMultiWinBlackList pkgName:" + pkgName);
        return true;
    }

    public boolean notKillProcessWhenRemoveTask(ProcessRecord processRecord, ContentResolver resolver) {
        if (resolver == null || Settings.System.getInt(resolver, "not_kill_process_when_remove_task", 1) == 1) {
            return true;
        }
        return false;
    }
}
