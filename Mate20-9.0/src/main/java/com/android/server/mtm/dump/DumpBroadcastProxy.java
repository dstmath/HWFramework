package com.android.server.mtm.dump;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import java.io.PrintWriter;

public final class DumpBroadcastProxy {
    private static AwareBroadcastPolicy mIawareBrPolicy = null;

    public static final void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (MultiTaskManagerService.self() != null) {
                mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
            }
            if (mIawareBrPolicy == null) {
                pw.println("  iAware proxy broadcast have exception ");
                return;
            }
            if (args.length < 2 || args[1] == null) {
                pw.println("  iAware Proxy broadcast");
                mIawareBrPolicy.dumpIawareBr(pw);
            } else {
                dumpExBroadcast(pw, args);
            }
        }
    }

    private static void dumpExBroadcast(PrintWriter pw, String[] args) {
        if ("disable_log".equals(args[1])) {
            AwareBroadcastDebug.disableDebug();
            pw.println("  iAware Proxy broadcast log disabled");
        } else if ("enable_log".equals(args[1])) {
            AwareBroadcastDebug.enableDebug();
            pw.println("  iAware Proxy broadcast log enabled");
        } else if ("big_data".equals(args[1])) {
            boolean clear = false;
            boolean forBeta = true;
            if (args.length >= 4 && "clear".equals(args[3])) {
                clear = true;
            }
            if (args.length >= 3 && "commercial".equals(args[2])) {
                forBeta = false;
            }
            AwareBroadcastDumpRadar radar = MultiTaskManagerService.self().getIawareBrRadar();
            if (radar != null) {
                String data = radar.getData(forBeta, clear);
                String filterDetailData = radar.getDFTData(forBeta, clear, false);
                String brData = radar.getDFTData(forBeta, clear, true);
                pw.println("  " + data);
                pw.println("  " + filterDetailData);
                pw.println("  " + brData);
                return;
            }
            pw.println("  radar is null");
        } else if ("filter_br".equals(args[1])) {
            pw.println("  iAware filter broadcast");
            mIawareBrPolicy.dumpIawareFilterBr(pw);
        } else if ("filter_br_log".equals(args[1])) {
            pw.println("  iAware filter broadcast enable log");
            AwareBroadcastDebug.enableFilterDebug();
        } else if ("filter_br_nolog".equals(args[1])) {
            pw.println("  iAware filter broadcast disabled log");
            AwareBroadcastDebug.disableFilterDebug();
        } else {
            pw.println("  bad command" + args[1]);
        }
    }
}
