package com.android.server.content;

import android.content.IContentService;
import android.os.RemoteException;
import android.os.ShellCommand;
import java.io.PrintWriter;

public class ContentShellCommand extends ShellCommand {
    final IContentService mInterface;

    ContentShellCommand(IContentService service) {
        this.mInterface = service;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0024 A[Catch:{ RemoteException -> 0x002e }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029 A[Catch:{ RemoteException -> 0x002e }] */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.hashCode() == -796331115) {
                if (cmd.equals("reset-today-stats")) {
                    z = false;
                    if (!z) {
                        return handleDefaultCommands(cmd);
                    }
                    return runResetTodayStats();
                }
            }
            z = true;
            if (!z) {
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int runResetTodayStats() throws RemoteException {
        this.mInterface.resetTodayStats();
        return 0;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Content service commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  reset-today-stats");
        pw.println("    Reset 1-day sync stats.");
        pw.println();
    }
}
