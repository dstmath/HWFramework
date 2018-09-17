package com.android.server.rms.test;

import android.content.Context;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.HwConnectivityService;
import com.android.server.rms.iaware.cpu.CPUFeature;

public final class TestResource {
    public static final void testNotificationWhiteList(Context context) {
        int i = 0;
        int strategy = 1;
        int uid = 100000;
        HwSysResource sysResource = HwSysResImpl.getResource(10);
        String[] INTEREST = new String[]{"com.whatsapp", HwConnectivityService.MM_PKG_NAME, "com.google.android.gm"};
        if (sysResource != null) {
            int length = INTEREST.length;
            while (i < length) {
                String pkg = INTEREST[i];
                Log.i("RMS.Test", "TestResource !pkg" + pkg);
                for (int i2 = 0; i2 < 50; i2++) {
                    strategy = sysResource.acquire(uid, pkg, -1);
                }
                if (1 != strategy) {
                    Log.e("RMS.Test", "testNotificationWhiteList fails:pkg:" + pkg);
                }
                sysResource.clear(uid, pkg, -1);
                uid++;
                i++;
            }
            Log.i("RMS.Test", "testNotificationWhiteList pass !");
        }
    }

    public static final void testContentObserver(Context context, String[] args) {
        if (args.length == 2 && args[1] != null) {
            String pkg = args[1];
            int uid = 100000;
            HwSysResource sysResource = HwSysResImpl.getResource(35);
            if (!(sysResource == null || pkg == null)) {
                try {
                    uid = context.getPackageManager().getApplicationInfo(pkg, 1).uid;
                } catch (Exception e) {
                    Log.w("RMS.Test", "get packagemanager failed!");
                }
                Log.i("RMS.Test", "begin TestResource contentObserver, 115, packageName: " + pkg + ", uid: " + uid);
                for (int i = 0; i < CPUFeature.MSG_THREAD_BOOST; i++) {
                    sysResource.acquire(uid, pkg, -1);
                }
            }
        }
    }
}
