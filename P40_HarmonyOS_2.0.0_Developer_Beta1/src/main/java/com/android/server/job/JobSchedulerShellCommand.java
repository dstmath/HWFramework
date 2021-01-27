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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        char c;
        PrintWriter pw = getOutPrintWriter();
        String str = cmd != null ? cmd : "";
        try {
            switch (str.hashCode()) {
                case -1894245460:
                    if (str.equals("trigger-dock-state")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -1845752298:
                    if (str.equals("get-storage-seq")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1687551032:
                    if (str.equals("get-battery-charging")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1367724422:
                    if (str.equals("cancel")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1313911455:
                    if (str.equals("timeout")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 113291:
                    if (str.equals("run")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 55361425:
                    if (str.equals("get-storage-not-low")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 200896764:
                    if (str.equals("heartbeat")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 703160488:
                    if (str.equals("get-battery-seq")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1749711139:
                    if (str.equals("get-battery-not-low")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1791471818:
                    if (str.equals("get-job-state")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1854493850:
                    if (str.equals("monitor-battery")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return runJob(pw);
                case 1:
                    return timeout(pw);
                case 2:
                    return cancelJob(pw);
                case 3:
                    return monitorBattery(pw);
                case 4:
                    return getBatterySeq(pw);
                case 5:
                    return getBatteryCharging(pw);
                case 6:
                    return getBatteryNotLow(pw);
                case 7:
                    return getStorageSeq(pw);
                case '\b':
                    return getStorageNotLow(pw);
                case '\t':
                    return getJobState(pw);
                case '\n':
                    return doHeartbeat(pw);
                case 11:
                    return triggerDockState(pw);
                default:
                    return handleDefaultCommands(cmd);
            }
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
        switch (errCode) {
            case CMD_ERR_CONSTRAINTS /* -1002 */:
                PrintWriter pw = getErrPrintWriter();
                pw.print("Job ");
                pw.print(jobId);
                pw.print(" in package ");
                pw.print(pkgName);
                pw.print(" / user ");
                pw.print(userId);
                pw.println(" has functional constraints but --force not specified");
                return true;
            case CMD_ERR_NO_JOB /* -1001 */:
                PrintWriter pw2 = getErrPrintWriter();
                pw2.print("Could not find job ");
                pw2.print(jobId);
                pw2.print(" in package ");
                pw2.print(pkgName);
                pw2.print(" / user ");
                pw2.println(userId);
                return true;
            case CMD_ERR_NO_PACKAGE /* -1000 */:
                PrintWriter pw3 = getErrPrintWriter();
                pw3.print("Package not found: ");
                pw3.print(pkgName);
                pw3.print(" / user ");
                pw3.println(userId);
                return true;
            default:
                return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0054 A[ADDED_TO_REGION] */
    private int runJob(PrintWriter pw) throws Exception {
        boolean z;
        checkPermission("force scheduled jobs");
        boolean force = false;
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                int hashCode = opt.hashCode();
                if (hashCode != -1626076853) {
                    if (hashCode != 1497) {
                        if (hashCode != 1512) {
                            if (hashCode == 1333469547 && opt.equals("--user")) {
                                z = true;
                                if (z || z) {
                                    force = true;
                                } else if (z || z) {
                                    userId = Integer.parseInt(getNextArgRequired());
                                } else {
                                    pw.println("Error: unknown option '" + opt + "'");
                                    return -1;
                                }
                            }
                        } else if (opt.equals("-u")) {
                            z = true;
                            if (z) {
                            }
                            force = true;
                        }
                    } else if (opt.equals("-f")) {
                        z = false;
                        if (z) {
                        }
                        force = true;
                    }
                } else if (opt.equals("--force")) {
                    z = true;
                    if (z) {
                    }
                    force = true;
                }
                z = true;
                if (z) {
                }
                force = true;
            } else {
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
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0030, code lost:
        if (r2.equals("-u") != false) goto L_0x0034;
     */
    private int timeout(PrintWriter pw) throws Exception {
        int userId;
        checkPermission("force timeout jobs");
        int userId2 = -1;
        while (true) {
            String opt = getNextOption();
            boolean z = false;
            int jobId = -1;
            if (opt != null) {
                int hashCode = opt.hashCode();
                if (hashCode != 1512) {
                    if (hashCode == 1333469547 && opt.equals("--user")) {
                        z = true;
                        if (z || z) {
                            userId2 = UserHandle.parseUserArg(getNextArgRequired());
                        } else {
                            pw.println("Error: unknown option '" + opt + "'");
                            return -1;
                        }
                    }
                }
                z = true;
                if (z) {
                }
                userId2 = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                if (userId2 == -2) {
                    userId = ActivityManager.getCurrentUser();
                } else {
                    userId = userId2;
                }
                String pkgName = getNextArg();
                String jobIdStr = getNextArg();
                if (jobIdStr != null) {
                    jobId = Integer.parseInt(jobIdStr);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    return this.mInternal.executeTimeoutCommand(pw, pkgName, userId, jobIdStr != null, jobId);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002e, code lost:
        if (r1.equals("-u") != false) goto L_0x0032;
     */
    private int cancelJob(PrintWriter pw) throws Exception {
        checkPermission("cancel jobs");
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            boolean z = false;
            if (opt != null) {
                int hashCode = opt.hashCode();
                if (hashCode != 1512) {
                    if (hashCode == 1333469547 && opt.equals("--user")) {
                        z = true;
                        if (z || z) {
                            userId = UserHandle.parseUserArg(getNextArgRequired());
                        } else {
                            pw.println("Error: unknown option '" + opt + "'");
                            return -1;
                        }
                    }
                }
                z = true;
                if (z) {
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else if (userId < 0) {
                pw.println("Error: must specify a concrete user ID");
                return -1;
            } else {
                String pkgName = getNextArg();
                String jobIdStr = getNextArg();
                int jobId = jobIdStr != null ? Integer.parseInt(jobIdStr) : -1;
                long ident = Binder.clearCallingIdentity();
                try {
                    return this.mInternal.executeCancelCommand(pw, pkgName, userId, jobIdStr != null, jobId);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private int monitorBattery(PrintWriter pw) throws Exception {
        boolean enabled;
        checkPermission("change battery monitoring");
        String opt = getNextArgRequired();
        if ("on".equals(opt)) {
            enabled = true;
        } else if ("off".equals(opt)) {
            enabled = false;
        } else {
            PrintWriter errPrintWriter = getErrPrintWriter();
            errPrintWriter.println("Error: unknown option " + opt);
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
            throw th;
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

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0034 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0050 A[ADDED_TO_REGION, SYNTHETIC] */
    private int getJobState(PrintWriter pw) throws Exception {
        boolean z;
        checkPermission("force timeout jobs");
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                int hashCode = opt.hashCode();
                if (hashCode != 1512) {
                    if (hashCode == 1333469547 && opt.equals("--user")) {
                        z = true;
                        if (!z || z) {
                            userId = UserHandle.parseUserArg(getNextArgRequired());
                        } else {
                            pw.println("Error: unknown option '" + opt + "'");
                            return -1;
                        }
                    }
                } else if (opt.equals("-u")) {
                    z = false;
                    if (!z) {
                    }
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                }
                z = true;
                if (!z) {
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
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
            }
        }
    }

    private int doHeartbeat(PrintWriter pw) throws Exception {
        checkPermission("manipulate scheduler heartbeat");
        String arg = getNextArg();
        int numBeats = arg != null ? Integer.parseInt(arg) : 0;
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mInternal.executeHeartbeatCommand(pw, numBeats);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    private int triggerDockState(PrintWriter pw) throws Exception {
        boolean idleState;
        checkPermission("trigger wireless charging dock state");
        String opt = getNextArgRequired();
        if ("idle".equals(opt)) {
            idleState = true;
        } else if ("active".equals(opt)) {
            idleState = false;
        } else {
            PrintWriter errPrintWriter = getErrPrintWriter();
            errPrintWriter.println("Error: unknown option " + opt);
            return 1;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mInternal.triggerDockState(idleState);
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
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
        pw.println("  cancel [-u | --user USER_ID] PACKAGE [JOB_ID]");
        pw.println("    Cancel a scheduled job.  If a job ID is not supplied, all jobs scheduled");
        pw.println("    by that package will be canceled.  USE WITH CAUTION.");
        pw.println("    Options:");
        pw.println("      -u or --user: specify which user's job is to be run; the default is");
        pw.println("         the primary or system user");
        pw.println("  heartbeat [num]");
        pw.println("    With no argument, prints the current standby heartbeat.  With a positive");
        pw.println("    argument, advances the standby heartbeat by that number.");
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
        pw.println("  trigger-dock-state [idle|active]");
        pw.println("    Trigger wireless charging dock state.  Active by default.");
        pw.println();
    }
}
