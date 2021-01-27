package com.android.server.mtm.dump;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastRegister;
import java.io.PrintWriter;

public final class DumpBroadcastRegister {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2 || args[1] == null) {
                AwareBroadcastRegister.getInstance().dumpAwareBrRegInfo(pw);
                return;
            }
            String argsFirst = args[1];
            if ("config".equals(argsFirst)) {
                AwareBroadcastRegister.getInstance().dumpBrRegConfig(pw);
            } else if ("disable_log".equals(argsFirst)) {
                AwareBroadcastDebug.disableDebug();
                pw.println("  iAware BR register log disabled");
            } else if ("enable_log".equals(argsFirst)) {
                AwareBroadcastDebug.enableDebug();
                pw.println("  iAware BR register log enabled");
            } else if ("big_data".equals(argsFirst)) {
                boolean clear = false;
                boolean forBeta = true;
                if (args.length >= 4 && "clear".equals(args[3])) {
                    clear = true;
                }
                if (args.length >= 3 && "commercial".equals(args[2])) {
                    forBeta = false;
                }
                AwareBroadcastDumpRadar radar = null;
                if (MultiTaskManagerService.self() != null) {
                    radar = MultiTaskManagerService.self().getAwareBrRadar();
                }
                if (radar != null) {
                    pw.println(radar.dumpBrRegBigData(forBeta, clear));
                } else {
                    pw.println("  radar is null");
                }
            } else {
                pw.println("  bad command" + argsFirst);
            }
        }
    }
}
