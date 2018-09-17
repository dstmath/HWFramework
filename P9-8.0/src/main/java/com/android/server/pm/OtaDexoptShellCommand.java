package com.android.server.pm;

import android.content.pm.IOtaDexopt;
import android.os.RemoteException;
import android.os.ShellCommand;
import java.io.PrintWriter;
import java.util.Locale;

class OtaDexoptShellCommand extends ShellCommand {
    final IOtaDexopt mInterface;

    OtaDexoptShellCommand(OtaDexoptService service) {
        this.mInterface = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(null);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.equals("prepare")) {
                return runOtaPrepare();
            }
            if (cmd.equals("cleanup")) {
                return runOtaCleanup();
            }
            if (cmd.equals("done")) {
                return runOtaDone();
            }
            if (cmd.equals("step")) {
                return runOtaStep();
            }
            if (cmd.equals("next")) {
                return runOtaNext();
            }
            if (cmd.equals("progress")) {
                return runOtaProgress();
            }
            return handleDefaultCommands(cmd);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int runOtaPrepare() throws RemoteException {
        this.mInterface.prepare();
        getOutPrintWriter().println("Success");
        return 0;
    }

    private int runOtaCleanup() throws RemoteException {
        this.mInterface.cleanup();
        return 0;
    }

    private int runOtaDone() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        if (this.mInterface.isDone()) {
            pw.println("OTA complete.");
        } else {
            pw.println("OTA incomplete.");
        }
        return 0;
    }

    private int runOtaStep() throws RemoteException {
        this.mInterface.dexoptNextPackage();
        return 0;
    }

    private int runOtaNext() throws RemoteException {
        getOutPrintWriter().println(this.mInterface.nextDexoptCommand());
        return 0;
    }

    private int runOtaProgress() throws RemoteException {
        float progress = this.mInterface.getProgress();
        getOutPrintWriter().format(Locale.ROOT, "%.2f", new Object[]{Float.valueOf(progress)});
        return 0;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("OTA Dexopt (ota) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        pw.println("  prepare");
        pw.println("    Prepare an OTA dexopt pass, collecting all packages.");
        pw.println("  done");
        pw.println("    Replies whether the OTA is complete or not.");
        pw.println("  step");
        pw.println("    OTA dexopt the next package.");
        pw.println("  next");
        pw.println("    Get parameters for OTA dexopt of the next package.");
        pw.println("  cleanup");
        pw.println("    Clean up internal states. Ends an OTA session.");
    }
}
