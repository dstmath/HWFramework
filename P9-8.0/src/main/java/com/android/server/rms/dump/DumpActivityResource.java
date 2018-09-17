package com.android.server.rms.dump;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;

public final class DumpActivityResource {
    public static final void dumpActivity(Context context, String[] args) {
        if (args.length != 3 || args[1] == null) {
            Log.e("RMS.Dump", "Format pkgName OverloadCount");
            return;
        }
        int uid = LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS;
        HwSysResource sysResource = HwSysResImpl.getResource(30);
        String pkg = args[1];
        int loop = 0;
        try {
            loop = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            Log.i("RMS.dump", "parse cmd error" + e.getMessage());
        }
        if (sysResource != null) {
            Log.i("RMS.Dump", "DumpResource !pkg " + pkg);
            try {
                ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
                if (ai != null) {
                    uid = ai.uid;
                    Log.i("RMS.Dump", "DumpResource uid " + uid);
                }
                sysResource.acquire(uid, pkg, -1, loop);
            } catch (NameNotFoundException e2) {
                Log.e("RMS.dump", "dumpActivity: get application info error!");
            }
        } else {
            Log.e("RMS.Dump", "dumpActivity failed!");
        }
    }
}
