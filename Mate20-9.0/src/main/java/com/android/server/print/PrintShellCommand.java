package com.android.server.print;

import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.print.IPrintManager;
import java.io.PrintWriter;

final class PrintShellCommand extends ShellCommand {
    final IPrintManager mService;

    PrintShellCommand(IPrintManager service) {
        this.mService = service;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0036  */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd != null) {
            int hashCode = cmd.hashCode();
            if (hashCode == -859068373) {
                if (cmd.equals("get-bind-instant-service-allowed")) {
                    z = false;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 789489311 && cmd.equals("set-bind-instant-service-allowed")) {
                z = true;
                switch (z) {
                    case false:
                        return runGetBindInstantServiceAllowed();
                    case true:
                        return runSetBindInstantServiceAllowed();
                    default:
                        return -1;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
            }
        } else {
            return handleDefaultCommands(cmd);
        }
    }

    private int runGetBindInstantServiceAllowed() {
        Integer userId = parseUserId();
        if (userId == null) {
            return -1;
        }
        try {
            getOutPrintWriter().println(Boolean.toString(this.mService.getBindInstantServiceAllowed(userId.intValue())));
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        return 0;
    }

    private int runSetBindInstantServiceAllowed() {
        Integer userId = parseUserId();
        if (userId == null) {
            return -1;
        }
        String allowed = getNextArgRequired();
        if (allowed == null) {
            getErrPrintWriter().println("Error: no true/false specified");
            return -1;
        }
        try {
            this.mService.setBindInstantServiceAllowed(userId.intValue(), Boolean.parseBoolean(allowed));
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        return 0;
    }

    private Integer parseUserId() {
        String option = getNextOption();
        if (option == null) {
            return 0;
        }
        if (option.equals("--user")) {
            return Integer.valueOf(UserHandle.parseUserArg(getNextArgRequired()));
        }
        PrintWriter errPrintWriter = getErrPrintWriter();
        errPrintWriter.println("Unknown option: " + option);
        return null;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Print service commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set-bind-instant-service-allowed [--user <USER_ID>] true|false ");
        pw.println("    Set whether binding to print services provided by instant apps is allowed.");
        pw.println("  get-bind-instant-service-allowed [--user <USER_ID>]");
        pw.println("    Get whether binding to print services provided by instant apps is allowed.");
    }
}
