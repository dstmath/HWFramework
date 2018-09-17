package com.android.server.rms.dump;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;

public final class DumpResource {
    public static final void dumpNotificationWhiteList(Context context) {
        int i = 0;
        int strategy = 1;
        int uid = LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
        HwSysResource sysResource = HwSysResImpl.getResource(10);
        String[] INTEREST = new String[]{"com.whatsapp", "com.tencent.mm", "com.google.android.gm"};
        if (sysResource != null) {
            int length = INTEREST.length;
            while (i < length) {
                String pkg = INTEREST[i];
                Log.i("RMS.Dump", "DumpResource !pkg" + pkg);
                for (int i2 = 0; i2 < 50; i2++) {
                    strategy = sysResource.acquire(uid, pkg, -1);
                }
                if (1 != strategy) {
                    Log.e("RMS.Dump", "dumpNotificationWhiteList fails:pkg:" + pkg);
                }
                sysResource.clear(uid, pkg, -1);
                uid++;
                i++;
            }
            Log.i("RMS.Dump", "dumpNotificationWhiteList pass !");
        }
    }

    public static final void dumpContentObserver(Context context, String[] args) {
        if (args.length == 2 && args[1] != null) {
            String pkg = args[1];
            int uid = LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
            HwSysResource sysResource = HwSysResImpl.getResource(29);
            if (!(sysResource == null || pkg == null)) {
                try {
                    uid = context.getPackageManager().getApplicationInfo(pkg, 1).uid;
                } catch (NameNotFoundException e) {
                    Log.w("RMS.Dump", "get packagemanager failed!");
                }
                Log.i("RMS.Dump", "begin DumpResource contentObserver, 115, packageName: " + pkg + ", uid: " + uid);
                for (int i = 0; i < CPUFeature.MSG_THREAD_BOOST; i++) {
                    sysResource.acquire(uid, pkg, -1);
                }
            }
        }
    }
}
