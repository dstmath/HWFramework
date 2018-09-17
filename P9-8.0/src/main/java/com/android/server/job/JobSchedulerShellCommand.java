package com.android.server.job;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.ShellCommand;
import android.os.UserHandle;
import java.io.PrintWriter;

public final class JobSchedulerShellCommand extends ShellCommand {
    public static final int CMD_ERR_CONSTRAINTS = -1002;
    public static final int CMD_ERR_NO_JOB = -1001;
    public static final int CMD_ERR_NO_PACKAGE = -1000;
    JobSchedulerService mInternal;
    IPackageManager mPM = AppGlobals.getPackageManager();

    JobSchedulerShellCommand(JobSchedulerService service) {
        this.mInternal = service;
    }

    public int onCommand(String cmd) {
        PrintWriter pw = getOutPrintWriter();
        String str = cmd != null ? cmd : "";
        try {
            if (str.equals("run")) {
                return runJob(pw);
            }
            if (str.equals("timeout")) {
                return timeout(pw);
            }
            if (str.equals("monitor-battery")) {
                return monitorBattery(pw);
            }
            if (str.equals("get-battery-seq")) {
                return getBatterySeq(pw);
            }
            if (str.equals("get-battery-charging")) {
                return getBatteryCharging(pw);
            }
            if (str.equals("get-battery-not-low")) {
                return getBatteryNotLow(pw);
            }
            if (str.equals("get-storage-seq")) {
                return getStorageSeq(pw);
            }
            if (str.equals("get-storage-not-low")) {
                return getStorageNotLow(pw);
            }
            if (str.equals("get-job-state")) {
                return getJobState(pw);
            }
            return handleDefaultCommands(cmd);
        } catch (Exception e) {
            pw.println("Exception: " + e);
            return -1;
        }
    }

    private void checkPermission(String operation) throws Exception {
        int uid = Binder.getCallingUid();
        if (uid != 0 && this.mPM.checkUidPermission("android.permission.CHANGE_APP_IDLE_STATE", uid) != 0) {
            throw new SecurityException("Uid " + uid + " not permitted to " + operation);
        }
    }

    private boolean printError(int errCode, String pkgName, int userId, int jobId) {
        PrintWriter pw;
        switch (errCode) {
            case CMD_ERR_CONSTRAINTS /*-1002*/:
                pw = getErrPrintWriter();
                pw.print("Job ");
                pw.print(jobId);
                pw.print(" in package ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.print(userId);
                pw.println(" has functional constraints but --force not specified");
                return true;
            case CMD_ERR_NO_JOB /*-1001*/:
                pw = getErrPrintWriter();
                pw.print("Could not find job ");
                pw.print(jobId);
                pw.print(" in package ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.println(userId);
                return true;
            case CMD_ERR_NO_PACKAGE /*-1000*/:
                pw = getErrPrintWriter();
                pw.print("Package not found: ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.println(userId);
                return true;
            default:
                return false;
        }
    }

    private int runJob(PrintWriter pw) throws Exception {
        checkPermission("force scheduled jobs");
        boolean force = false;
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String pkgName = getNextArgRequired();
                int jobId = Integer.parseInt(getNextArgRequired());
                long ident = Binder.clearCallingIdentity();
                try {
                    int ret = this.mInternal.executeRunCommand(pkgName, userId, jobId, force);
                    if (printError(ret, pkgName, userId, jobId)) {
                        return ret;
                    }
                    pw.print("Running job");
                    if (force) {
                        pw.print(" [FORCED]");
                    }
                    pw.println();
                    Binder.restoreCallingIdentity(ident);
                    return ret;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else if (opt.equals("-f") || opt.equals("--force")) {
                force = true;
            } else if (opt.equals("-u") || opt.equals("--user")) {
                userId = Integer.parseInt(getNextArgRequired());
            } else {
                pw.println("Error: unknown option '" + opt + "'");
                return -1;
            }
        }
    }

    private int timeout(PrintWriter pw) throws Exception {
        checkPermission("force timeout jobs");
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                if (userId == -2) {
                    userId = ActivityManager.getCurrentUser();
                }
                String pkgName = getNextArg();
                String jobIdStr = getNextArg();
                int jobId = jobIdStr != null ? Integer.parseInt(jobIdStr) : -1;
                long ident = Binder.clearCallingIdentity();
                try {
                    int executeTimeoutCommand = this.mInternal.executeTimeoutCommand(pw, pkgName, userId, jobIdStr != null, jobId);
                    return executeTimeoutCommand;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else if (opt.equals("-u") || opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: unknown option '" + opt + "'");
                return -1;
            }
        }
    }

    private int monitorBattery(PrintWriter pw) throws Exception {
        boolean enabled;
        checkPermission("change battery monitoring");
        String opt = getNextArgRequired();
        if ("on".equals(opt)) {
            enabled = true;
        } else if ("off".equals(opt)) {
            enabled = false;
        } else {
            getErrPrintWriter().println("Error: unknown option " + opt);
            return 1;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mInternal.setMonitorBattery(enabled);
            if (enabled) {
                pw.println("Battery monitoring enabled");
            } else {
                pw.println("Battery monitoring disabled");
            }
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private int getBatterySeq(PrintWriter pw) {
        pw.println(this.mInternal.getBatterySeq());
        return 0;
    }

    private int getBatteryCharging(PrintWriter pw) {
        pw.println(this.mInternal.getBatteryCharging());
        return 0;
    }

    private int getBatteryNotLow(PrintWriter pw) {
        pw.println(this.mInternal.getBatteryNotLow());
        return 0;
    }

    private int getStorageSeq(PrintWriter pw) {
        pw.println(this.mInternal.getStorageSeq());
        return 0;
    }

    private int getStorageNotLow(PrintWriter pw) {
        pw.println(this.mInternal.getStorageNotLow());
        return 0;
    }

    private int getJobState(PrintWriter pw) throws Exception {
        checkPermission("force timeout jobs");
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                if (userId == -2) {
                    userId = ActivityManager.getCurrentUser();
                }
                String pkgName = getNextArgRequired();
                int jobId = Integer.parseInt(getNextArgRequired());
                long ident = Binder.clearCallingIdentity();
                try {
                    int ret = this.mInternal.getJobState(pw, pkgName, userId, jobId);
                    printError(ret, pkgName, userId, jobId);
                    return ret;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else if (opt.equals("-u") || opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: unknown option '" + opt + "'");
                return -1;
            }
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Job scheduler (jobscheduler) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  run [-f | --force] [-u | --user USER_ID] PACKAGE JOB_ID");
        pw.println("    Trigger immediate execution of a specific scheduled job.");
        pw.println("    Options:");
        pw.println("      -f or --force: run the job even if technical constraints such as");
        pw.println("         connectivity are not currently met");
        pw.println("      -u or --user: specify which user's job is to be run; the default is");
        pw.println("         the primary or system user");
        pw.println("  timeout [-u | --user USER_ID] [PACKAGE] [JOB_ID]");
        pw.println("    Trigger immediate timeout of currently executing jobs, as if their.");
        pw.println("    execution timeout had expired.");
        pw.println("    Options:");
        pw.println("      -u or --user: specify which user's job is to be run; the default is");
        pw.println("         all users");
        pw.println("  monitor-battery [on|off]");
        pw.println("    Control monitoring of all battery changes.  Off by default.  Turning");
        pw.println("    on makes get-battery-seq useful.");
        pw.println("  get-battery-seq");
        pw.println("    Return the last battery update sequence number that was received.");
        pw.println("  get-battery-charging");
        pw.println("    Return whether the battery is currently considered to be charging.");
        pw.println("  get-battery-not-low");
        pw.println("    Return whether the battery is currently considered to not be low.");
        pw.println("  get-storage-seq");
        pw.println("    Return the last storage update sequence number that was received.");
        pw.println("  get-storage-not-low");
        pw.println("    Return whether storage is currently considered to not be low.");
        pw.println("  get-job-state [-u | --user USER_ID] PACKAGE JOB_ID");
        pw.println("    Return the current state of a job, may be any combination of:");
        pw.println("      pending: currently on the pending list, waiting to be active");
        pw.println("      active: job is actively running");
        pw.println("      user-stopped: job can't run because its user is stopped");
        pw.println("      backing-up: job can't run because app is currently backing up its data");
        pw.println("      no-component: job can't run because its component is not available");
        pw.println("      ready: job is ready to run (all constraints satisfied or bypassed)");
        pw.println("      waiting: if nothing else above is printed, job not ready to run");
        pw.println("    Options:");
        pw.println("      -u or --user: specify which user's job is to be run; the default is");
        pw.println("         the primary or system user");
        pw.println();
    }
}
