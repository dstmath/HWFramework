package com.android.server.rms.dump;

import android.content.Context;
import android.content.pm.PackageManager;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;

public final class DumpResource {
    public static final void dumpNotificationWhiteList(Context context) {
        HwSysResource sysResource = HwSysResImpl.getResource(10);
        String[] INTEREST = {"com.whatsapp", "com.tencent.mm", "com.google.android.gm"};
        if (sysResource != null) {
            int length = INTEREST.length;
            int uid = 100000;
            int uid2 = 1;
            int strategy = 0;
            while (strategy < length) {
                String pkg = INTEREST[strategy];
                Log.i("RMS.Dump", "DumpResource !pkg" + pkg);
                int strategy2 = uid2;
                for (int i = 0; i < 50; i++) {
                    strategy2 = sysResource.acquire(uid, pkg, -1);
                }
                if (1 != strategy2) {
                    Log.e("RMS.Dump", "dumpNotificationWhiteList fails:pkg:" + pkg);
                }
                sysResource.clear(uid, pkg, -1);
                uid++;
                strategy++;
                uid2 = strategy2;
            }
            Log.i("RMS.Dump", "dumpNotificationWhiteList pass !");
            int i2 = uid2;
            int strategy3 = uid;
        }
    }

    public static final void dumpContentObserver(Context context, String[] args) {
        if (args.length == 2 && args[1] != null) {
            String pkg = args[1];
            int uid = LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS;
            HwSysResource sysResource = HwSysResImpl.getResource(29);
            if (!(sysResource == null || pkg == null)) {
                try {
                    uid = context.getPackageManager().getApplicationInfo(pkg, 1).uid;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("RMS.Dump", "get packagemanager failed!");
                }
                Log.i("RMS.Dump", "begin DumpResource contentObserver, 115, packageName: " + pkg + ", uid: " + uid);
                for (int i = 0; i < 115; i++) {
                    sysResource.acquire(uid, pkg, -1);
                }
            }
        }
    }
}
