package com.android.server.rms.dump;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;

public class DumpAppResource {
    private static final String TAG = "RMS.DumpAppResource";

    public static final int getPolicyType(Context context, String[] args) {
        int policy = 0;
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppResource dump!");
            return 0;
        }
        HwSysResource sysResource = HwSysResImpl.getResource(18);
        String pkg = args[1];
        if (sysResource != null) {
            policy = sysResource.queryPkgPolicy(0, 0, pkg);
        }
        if (Utils.HWFLOW) {
            Log.i(TAG, "getPolicyType pkg " + pkg + " policy " + policy);
        }
        return policy;
    }

    public static final void dumpDispatchAQV(Context context, String[] args) {
        int uid = LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppResource dump!");
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
            Log.e(TAG, "dumpAppResource: get application info error!");
        }
    }
}
