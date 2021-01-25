package com.android.server.appprediction;

import android.os.ShellCommand;
import java.io.PrintWriter;

public class AppPredictionManagerServiceShellCommand extends ShellCommand {
    private static final String TAG = AppPredictionManagerServiceShellCommand.class.getSimpleName();
    private final AppPredictionManagerService mService;

    public AppPredictionManagerServiceShellCommand(AppPredictionManagerService service) {
        this.mService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        char c = 65535;
        if ((cmd.hashCode() == 113762 && cmd.equals("set")) ? false : true) {
            return handleDefaultCommands(cmd);
        }
        String what = getNextArgRequired();
        if (what.hashCode() == 2003978041 && what.equals("temporary-service")) {
            c = 0;
        }
        if (c == 0) {
            int userId = Integer.parseInt(getNextArgRequired());
            String serviceName = getNextArg();
            if (serviceName == null) {
                this.mService.resetTemporaryService(userId);
                return 0;
            }
            int duration = Integer.parseInt(getNextArgRequired());
            this.mService.setTemporaryService(userId, serviceName, duration);
            pw.println("AppPredictionService temporarily set to " + serviceName + " for " + duration + "ms");
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0037, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        r0.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0030, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0031, code lost:
        if (r1 != null) goto L_0x0033;
     */
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("AppPredictionManagerService commands:");
        pw.println("  help");
        pw.println("    Prints this help text.");
        pw.println("");
        pw.println("  set temporary-service USER_ID [COMPONENT_NAME DURATION]");
        pw.println("    Temporarily (for DURATION ms) changes the service implemtation.");
        pw.println("    To reset, call with just the USER_ID argument.");
        pw.println("");
        pw.close();
    }
}
