package com.android.server.power;

import android.content.Intent;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ShellCommand;
import java.io.PrintWriter;

class PowerManagerShellCommand extends ShellCommand {
    private static final int LOW_POWER_MODE_ON = 1;
    final IPowerManager mInterface;

    PowerManagerShellCommand(IPowerManager service) {
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
            if (cmd.hashCode() == 1369181230) {
                if (cmd.equals("set-mode")) {
                    z = false;
                    if (!z) {
                        return handleDefaultCommands(cmd);
                    }
                    return runSetMode();
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

    private int runSetMode() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        try {
            int mode = Integer.parseInt(getNextArgRequired());
            IPowerManager iPowerManager = this.mInterface;
            boolean z = true;
            if (mode != 1) {
                z = false;
            }
            iPowerManager.setPowerSaveMode(z);
            return 0;
        } catch (RuntimeException ex) {
            pw.println("Error: " + ex.toString());
            return -1;
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Power manager (power) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-mode MODE");
        pw.println("    sets the power mode of the device to MODE.");
        pw.println("    1 turns low power mode on and 0 turns low power mode off.");
        pw.println();
        Intent.printIntentArgsHelp(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }
}
