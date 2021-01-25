package com.android.server.mtm.dump;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastSend;
import java.io.PrintWriter;

public final class DumpBroadcastSend {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2 || args[1] == null) {
                AwareBroadcastSend.getInstance().dumpBrSendInfo(pw);
            } else {
                dumpWithArgs(pw, args);
            }
        }
    }

    private static void dumpWithArgs(PrintWriter pw, String[] args) {
        String argsFirst = args[1];
        if ("config".equals(argsFirst)) {
            AwareBroadcastSend.getInstance().dumpBrSendConfig(pw);
        } else if ("switch_on".equals(argsFirst)) {
            AwareBroadcastSend.getInstance().changeSwitch(true);
            pw.println("  iAware BR send control switch on");
        } else if ("switch_off".equals(argsFirst)) {
            AwareBroadcastSend.getInstance().changeSwitch(false);
            pw.println("  iAware BR send control switch off");
        } else if ("disable_log".equals(argsFirst)) {
            AwareBroadcastDebug.disableDebug();
            pw.println("  iAware BR send log disabled");
        } else if ("enable_log".equals(argsFirst)) {
            AwareBroadcastDebug.enableDebug();
            pw.println("  iAware BR send log enabled");
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
                pw.println(radar.dumpBrSendBigData(forBeta, clear));
            } else {
                pw.println("  radar is null");
            }
        } else {
            pw.println("  bad command" + argsFirst);
        }
    }
}
