package com.android.server.mtm.dump;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import java.io.PrintWriter;

public final class DumpBroadcastProxy {
    private static AwareBroadcastPolicy sAwareBrPolicy = null;

    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (MultiTaskManagerService.self() != null) {
                sAwareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
            }
            if (sAwareBrPolicy == null) {
                pw.println("  iAware proxy broadcast have exception ");
            } else if (args.length < 2 || args[1] == null) {
                pw.println("  iAware Proxy broadcast");
                sAwareBrPolicy.dumpAwareBr(pw);
            } else {
                dumpExBroadcast(pw, args);
            }
        }
    }

    private static void dumpExBroadcast(PrintWriter pw, String[] args) {
        String argsFirst = args[1];
        if ("disable_log".equals(argsFirst)) {
            AwareBroadcastDebug.disableDebug();
            pw.println("  iAware Proxy broadcast log disabled");
        } else if ("enable_log".equals(argsFirst)) {
            AwareBroadcastDebug.enableDebug();
            pw.println("  iAware Proxy broadcast log enabled");
        } else if ("big_data".equals(argsFirst)) {
            boolean clear = false;
            boolean forBeta = true;
            if (args.length >= 4 && "clear".equals(args[3])) {
                clear = true;
            }
            if (args.length >= 3 && "commercial".equals(args[2])) {
                forBeta = false;
            }
            AwareBroadcastDumpRadar radar = MultiTaskManagerService.self().getAwareBrRadar();
            if (radar != null) {
                String data = radar.getData(forBeta, clear);
                String filterDetailData = radar.getDftData(forBeta, clear, false);
                String brData = radar.getDftData(forBeta, clear, true);
                pw.println("  " + data);
                pw.println("  " + filterDetailData);
                pw.println("  " + brData);
                return;
            }
            pw.println("  radar is null");
        } else if ("filter_br".equals(argsFirst)) {
            pw.println("  iAware filter broadcast");
            sAwareBrPolicy.dumpAwareFilterBr(pw);
        } else if ("filter_br_log".equals(argsFirst)) {
            pw.println("  iAware filter broadcast enable log");
            AwareBroadcastDebug.enableFilterDebug();
        } else if ("filter_br_nolog".equals(argsFirst)) {
            pw.println("  iAware filter broadcast disabled log");
            AwareBroadcastDebug.disableFilterDebug();
        } else {
            pw.println("  bad command" + argsFirst);
        }
    }
}
