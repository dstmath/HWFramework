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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(null);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1001078227:
                    if (cmd.equals("progress")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case -318370553:
                    if (cmd.equals("prepare")) {
                        z = false;
                        break;
                    }
                    z = true;
                    break;
                case 3089282:
                    if (cmd.equals("done")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3377907:
                    if (cmd.equals("next")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3540684:
                    if (cmd.equals("step")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 856774308:
                    if (cmd.equals("cleanup")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (!z) {
                return runOtaPrepare();
            }
            if (z) {
                return runOtaCleanup();
            }
            if (z) {
                return runOtaDone();
            }
            if (z) {
                return runOtaStep();
            }
            if (z) {
                return runOtaNext();
            }
            if (!z) {
                return handleDefaultCommands(cmd);
            }
            return runOtaProgress();
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
            return 0;
        }
        pw.println("OTA incomplete.");
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
        getOutPrintWriter().format(Locale.ROOT, "%.2f", Float.valueOf(this.mInterface.getProgress()));
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
