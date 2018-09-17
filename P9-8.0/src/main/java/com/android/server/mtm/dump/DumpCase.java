package com.android.server.mtm.dump;

import android.content.Context;
import com.android.server.mtm.dump.iaware.DumpAwareAppFreezeMng;
import com.android.server.mtm.dump.iaware.DumpAwareAppIoLimitMng;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import java.io.PrintWriter;

public final class DumpCase {
    public static final void dump(Context context, PrintWriter pw, String[] args) {
        if (checkParam(pw, args)) {
            String cmd = args[0];
            if ("appMng".equals(cmd)) {
                DumpAppMngListInfo.dump(context, pw, args);
            } else if ("mtmproc".equals(cmd)) {
                ProcessInfoCollector.getInstance().dump(pw);
            } else {
                dumpAware(context, pw, args, cmd);
            }
        }
    }

    private static final void dumpAware(Context context, PrintWriter pw, String[] args, String cmd) {
        if ("proxybr".equals(cmd)) {
            DumpBroadcastProxy.dump(pw, args);
        } else if ("srmng".equals(cmd)) {
            DumpAppSrMng.dump(context, pw, args);
        } else if ("appClean".equals(cmd)) {
            DumpAppMngClean.dump(context, pw, args);
        } else if ("brjobscheduler".equals(cmd)) {
            DumpBroadcastJobScheduler.dump(pw, args);
        } else if ("freeze".equals(cmd)) {
            DumpAwareAppFreezeMng.dump(context, pw, args);
        } else if ("freezeApp".equals(cmd)) {
            DumpAwareAppFreezeMng.dumpFreezeApp(context, pw, args);
        } else if ("iolimit".equals(cmd)) {
            DumpAwareAppIoLimitMng.dump(context, pw, args);
        } else if ("checkBadPid".equals(cmd)) {
            DumpAwareAppFreezeMng.dumpCheckBadApp(context, pw, args);
        } else {
            pw.println("Bad command :" + cmd);
        }
    }

    private static final boolean checkParam(PrintWriter pw, String[] args) {
        if (args == null || args.length == 0 || args[0] == null || pw == null) {
            return false;
        }
        return true;
    }
}
