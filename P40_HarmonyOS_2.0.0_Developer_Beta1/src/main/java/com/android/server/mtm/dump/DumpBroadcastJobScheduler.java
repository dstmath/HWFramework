package com.android.server.mtm.dump;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import java.io.PrintWriter;

public final class DumpBroadcastJobScheduler {
    private static AwareJobSchedulerService sAwareJobSchedulerService = null;

    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (MultiTaskManagerService.self() != null) {
                sAwareJobSchedulerService = MultiTaskManagerService.self().getAwareJobSchedulerService();
            }
            if (sAwareJobSchedulerService == null) {
                pw.println("  iAware schedule broadcast have exception ");
            } else if (args.length < 2 || args[1] == null) {
                pw.println("  bad command");
                pw.println("  please input show, or show action [action name], or show receiver [receiver name]");
            } else {
                String argsCmd = args[1];
                if ("disable_log".equals(argsCmd)) {
                    sAwareJobSchedulerService.disableDebug();
                    pw.println("  iAware schedule broadcast log disabled");
                } else if ("enable_log".equals(argsCmd)) {
                    sAwareJobSchedulerService.enableDebug();
                    pw.println("  iAware schedule broadcast log enabled");
                } else if ("show".equals(argsCmd)) {
                    pw.println("  iAware schedule broadcast");
                    sAwareJobSchedulerService.dump(pw, args);
                } else if ("help".equals(argsCmd)) {
                    dumpHelp(pw);
                } else {
                    pw.println("  bad command" + args[1]);
                    pw.println("  please input help");
                }
            }
        }
    }

    private static void dumpHelp(PrintWriter pw) {
        pw.println("  AwareJob Scheduler (iawarejobscheduler) dump options:");
        pw.println("   help: print this help");
        pw.println("   show: is an optional to show the output to.");
        pw.println("   show action [action]: is an optional action name to limit the output to.");
        pw.println("   show receiver [receiver]: is an optional receiver name to limit the output to.");
    }
}
