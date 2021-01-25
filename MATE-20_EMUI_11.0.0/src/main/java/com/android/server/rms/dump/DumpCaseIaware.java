package com.android.server.rms.dump;

import android.content.Context;
import android.rms.utils.Utils;
import java.io.PrintWriter;

public final class DumpCaseIaware {
    public static boolean dump(Context context, PrintWriter pw, String[] args) {
        if (!Utils.DEBUG) {
            return false;
        }
        if (Utils.scanArgs(args, "--dump-AppAssociate")) {
            DumpAppAssociate.dumpAppAssociate(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppImportance")) {
            DumpAppKeyBackgroup.dumpAppImportance(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppWhitelist")) {
            DumpAppWhiteList.dumpAppWhiteList(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AwareUserHabit")) {
            DumpAwareUserHabit.dumpAwareUserHabit(pw, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AlarmManager")) {
            DumpAlarmManager.dump(context, pw, args);
            return true;
        } else if (dumpEx(context, pw, args)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean dumpEx(Context context, PrintWriter pw, String[] args) {
        if (Utils.scanArgs(args, "--dump-AwareAppUseData")) {
            DumpAwareAppUseData.dump(pw, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppQuickStart")) {
            DumpAppQuickStart.dump(pw, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AwareComponentPreloadManager")) {
            DumpAwareComponentPreMgr.dump(pw, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-CachedMemoryCleanPolicy")) {
            DumpCachedMemoryCleanPolicy.dump(pw, args);
            return true;
        } else if (!Utils.scanArgs(args, "--dump-HwConfigReader")) {
            return false;
        } else {
            DumpHwConfigReader.dumpConfigReader(pw, context);
            return true;
        }
    }
}
