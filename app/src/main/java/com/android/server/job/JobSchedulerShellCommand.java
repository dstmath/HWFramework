package com.android.server.job;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ShellCommand;
import java.io.PrintWriter;

public class JobSchedulerShellCommand extends ShellCommand {
    public static final int CMD_ERR_CONSTRAINTS = -1002;
    public static final int CMD_ERR_NO_JOB = -1001;
    public static final int CMD_ERR_NO_PACKAGE = -1000;
    JobSchedulerService mInternal;
    IPackageManager mPM;

    JobSchedulerShellCommand(JobSchedulerService service) {
        this.mInternal = service;
        this.mPM = AppGlobals.getPackageManager();
    }

    public int onCommand(String cmd) {
        PrintWriter pw = getOutPrintWriter();
        try {
            if ("run".equals(cmd)) {
                return runJob();
            }
            return handleDefaultCommands(cmd);
        } catch (Exception e) {
            pw.println("Exception: " + e);
            return -1;
        }
    }

    private int runJob() {
        try {
            int uid = Binder.getCallingUid();
            if (this.mPM.checkUidPermission("android.permission.CHANGE_APP_IDLE_STATE", uid) != 0) {
                throw new SecurityException("Uid " + uid + " not permitted to force scheduled jobs");
            }
        } catch (RemoteException e) {
        }
        PrintWriter pw = getOutPrintWriter();
        boolean force = false;
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                break;
            } else if (opt.equals("-f") || opt.equals("--force")) {
                force = true;
            } else if (opt.equals("-u") || opt.equals("--user")) {
                userId = Integer.parseInt(getNextArgRequired());
            } else {
                pw.println("Error: unknown option '" + opt + "'");
                return -1;
            }
        }
        String pkgName = getNextArgRequired();
        int jobId = Integer.parseInt(getNextArgRequired());
        int ret = this.mInternal.executeRunCommand(pkgName, userId, jobId, force);
        switch (ret) {
            case CMD_ERR_CONSTRAINTS /*-1002*/:
                pw.print("Job ");
                pw.print(jobId);
                pw.print(" in package ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.print(userId);
                pw.println(" has functional constraints but --force not specified");
                break;
            case CMD_ERR_NO_JOB /*-1001*/:
                pw.print("Could not find job ");
                pw.print(jobId);
                pw.print(" in package ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.println(userId);
                break;
            case CMD_ERR_NO_PACKAGE /*-1000*/:
                pw.print("Package not found: ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.println(userId);
                break;
            default:
                pw.print("Running job");
                if (force) {
                    pw.print(" [FORCED]");
                }
                pw.println();
                break;
        }
        return ret;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Job scheduler (jobscheduler) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println();
        pw.println("  run [-f | --force] [-u | --user USER_ID] PACKAGE JOB_ID");
        pw.println("    Trigger immediate execution of a specific scheduled job.");
        pw.println("    Options:");
        pw.println("      -f or --force: run the job even if technical constraints such as");
        pw.println("         connectivity are not currently met");
        pw.println("      -u or --user: specify which user's job is to be run; the default is");
        pw.println("         the primary or system user");
        pw.println();
    }
}
