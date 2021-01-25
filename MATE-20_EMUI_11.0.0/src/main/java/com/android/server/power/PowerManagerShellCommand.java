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

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0035 A[Catch:{ RemoteException -> 0x0046 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0041 A[Catch:{ RemoteException -> 0x0046 }] */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            int hashCode = cmd.hashCode();
            if (hashCode != -531688203) {
                if (hashCode == 1369181230 && cmd.equals("set-mode")) {
                    z = true;
                    if (!z) {
                        return runSetAdaptiveEnabled();
                    }
                    if (!z) {
                        return handleDefaultCommands(cmd);
                    }
                    return runSetMode();
                }
            } else if (cmd.equals("set-adaptive-power-saver-enabled")) {
                z = false;
                if (!z) {
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

    private int runSetAdaptiveEnabled() throws RemoteException {
        this.mInterface.setAdaptivePowerSaveEnabled(Boolean.parseBoolean(getNextArgRequired()));
        return 0;
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
            iPowerManager.setPowerSaveModeEnabled(z);
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
        pw.println("");
        pw.println("  set-adaptive-power-saver-enabled [true|false]");
        pw.println("    enables or disables adaptive power saver.");
        pw.println("  set-mode MODE");
        pw.println("    sets the power mode of the device to MODE.");
        pw.println("    1 turns low power mode on and 0 turns low power mode off.");
        pw.println();
        Intent.printIntentArgsHelp(pw, "");
    }
}
