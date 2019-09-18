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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(null);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1001078227:
                    if (cmd.equals("progress")) {
                        c = 5;
                        break;
                    }
                case -318370553:
                    if (cmd.equals("prepare")) {
                        c = 0;
                        break;
                    }
                case 3089282:
                    if (cmd.equals("done")) {
                        c = 2;
                        break;
                    }
                case 3377907:
                    if (cmd.equals("next")) {
                        c = 4;
                        break;
                    }
                case 3540684:
                    if (cmd.equals("step")) {
                        c = 3;
                        break;
                    }
                case 856774308:
                    if (cmd.equals("cleanup")) {
                        c = 1;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    return runOtaPrepare();
                case 1:
                    return runOtaCleanup();
                case 2:
                    return runOtaDone();
                case 3:
                    return runOtaStep();
                case 4:
                    return runOtaNext();
                case 5:
                    return runOtaProgress();
                default:
                    return handleDefaultCommands(cmd);
            }
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
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
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
