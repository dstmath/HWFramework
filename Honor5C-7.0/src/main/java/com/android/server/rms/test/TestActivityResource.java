package com.android.server.rms.test;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;

public final class TestActivityResource {
    public static final void testActivity(Context context, String[] args) {
        if (args.length != 2 || args[1] == null) {
            Log.e("RMS.Test", "please input correct package name for Activity test!");
            return;
        }
        int uid = 100000;
        HwSysResource sysResource = HwSysResImpl.getResource(36);
        String pkg = args[1];
        if (sysResource != null) {
            Log.i("RMS.Test", "TestResource !pkg " + pkg);
            try {
                ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
                if (ai != null) {
                    uid = ai.uid;
                    Log.i("RMS.Test", "TestResource uid " + uid);
                }
                for (int i = 0; i < 110; i++) {
                    sysResource.acquire(uid, pkg, -1, i);
                }
            } catch (NameNotFoundException e) {
                Log.e("RMS.test", "testActivity: get application info error!");
            }
        } else {
            Log.e("RMS.Test", "testActivity failed!");
        }
    }
}
