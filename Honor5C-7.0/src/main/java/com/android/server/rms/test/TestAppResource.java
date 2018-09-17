package com.android.server.rms.test;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.rms.utils.Utils;

public class TestAppResource {
    private static final String TAG = "RMS.TestAppResource";

    public static final int getPolicyType(Context context, String[] args) {
        int policy = 0;
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppResource test!");
            return 0;
        }
        HwSysResource sysResource = HwSysResImpl.getResource(19);
        String pkg = args[1];
        if (sysResource != null) {
            policy = sysResource.queryPkgPolicy(0, 0, pkg);
        }
        if (Utils.HWFLOW) {
            Log.i(TAG, "getPolicyType pkg " + pkg + " policy " + policy);
        }
        return policy;
    }

    public static final void testDispatchAQV(Context context, String[] args) {
        int uid = 100000;
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppResource test!");
            return;
        }
        String pkg = args[1];
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
            if (ai != null) {
                uid = ai.uid;
            }
            HwSysResManagerService.self().dispatchProcessDiedOverload(pkg, uid);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "testAppResource: get application info error!");
        }
    }
}
