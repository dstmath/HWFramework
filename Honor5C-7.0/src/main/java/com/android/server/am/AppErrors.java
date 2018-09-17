package com.android.server.am;

import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport;
import android.app.ApplicationErrorReport.AnrInfo;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.Dialog;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.Watchdog;
import com.android.server.power.AbsPowerManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class AppErrors {
    private static final boolean IS_DEBUG_VERSION = false;
    private static final String TAG = null;
    public static final Set<String> whitelist_for_short_time = null;
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses;
    private final Context mContext;
    private final boolean mIsNotShowAnrDialog;
    private final boolean mIswhitelist_for_short_time;
    private final ProcessMap<Long> mProcessCrashTimes;
    private final ProcessMap<Long> mProcessCrashTimesPersistent;
    private final ActivityManagerService mService;

    static final class BadProcessInfo {
        final String longMsg;
        final String shortMsg;
        final String stack;
        final long time;

        BadProcessInfo(long time, String shortMsg, String longMsg, String stack) {
            this.time = time;
            this.shortMsg = shortMsg;
            this.longMsg = longMsg;
            this.stack = stack;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.AppErrors.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.AppErrors.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.AppErrors.<clinit>():void");
    }

    AppErrors(Context context, ActivityManagerService service) {
        this.mProcessCrashTimes = new ProcessMap();
        this.mProcessCrashTimesPersistent = new ProcessMap();
        this.mBadProcesses = new ProcessMap();
        this.mIswhitelist_for_short_time = SystemProperties.getBoolean("persist.sys.hwgmstemporary", false);
        this.mIsNotShowAnrDialog = SystemProperties.getBoolean("ro.config.noshow_anrdialog", false);
        this.mService = service;
        this.mContext = context;
    }

    boolean dumpLocked(FileDescriptor fd, PrintWriter pw, boolean needSep, String dumpPackage) {
        boolean printed;
        int processCount;
        int ip;
        String pname;
        int uidCount;
        int i;
        ProcessRecord r;
        if (!this.mProcessCrashTimes.getMap().isEmpty()) {
            printed = false;
            long now = SystemClock.uptimeMillis();
            ArrayMap<String, SparseArray<Long>> pmap = this.mProcessCrashTimes.getMap();
            processCount = pmap.size();
            for (ip = 0; ip < processCount; ip++) {
                pname = (String) pmap.keyAt(ip);
                SparseArray<Long> uids = (SparseArray) pmap.valueAt(ip);
                uidCount = uids.size();
                for (i = 0; i < uidCount; i++) {
                    int puid = uids.keyAt(i);
                    r = (ProcessRecord) this.mService.mProcessNames.get(pname, puid);
                    if (dumpPackage != null) {
                        if (r != null) {
                            if (!r.pkgList.containsKey(dumpPackage)) {
                            }
                        }
                    }
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Time since processes crashed:");
                        printed = true;
                    }
                    pw.print("    Process ");
                    pw.print(pname);
                    pw.print(" uid ");
                    pw.print(puid);
                    pw.print(": last crashed ");
                    TimeUtils.formatDuration(now - ((Long) uids.valueAt(i)).longValue(), pw);
                    pw.println(" ago");
                }
            }
        }
        if (!this.mBadProcesses.getMap().isEmpty()) {
            printed = false;
            ArrayMap<String, SparseArray<BadProcessInfo>> pmap2 = this.mBadProcesses.getMap();
            processCount = pmap2.size();
            for (ip = 0; ip < processCount; ip++) {
                pname = (String) pmap2.keyAt(ip);
                SparseArray<BadProcessInfo> uids2 = (SparseArray) pmap2.valueAt(ip);
                uidCount = uids2.size();
                for (i = 0; i < uidCount; i++) {
                    puid = uids2.keyAt(i);
                    r = (ProcessRecord) this.mService.mProcessNames.get(pname, puid);
                    if (dumpPackage != null) {
                        if (r != null) {
                            if (!r.pkgList.containsKey(dumpPackage)) {
                            }
                        }
                    }
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Bad processes:");
                        printed = true;
                    }
                    BadProcessInfo info = (BadProcessInfo) uids2.valueAt(i);
                    pw.print("    Bad process ");
                    pw.print(pname);
                    pw.print(" uid ");
                    pw.print(puid);
                    pw.print(": crashed at time ");
                    pw.println(info.time);
                    if (info.shortMsg != null) {
                        pw.print("      Short msg: ");
                        pw.println(info.shortMsg);
                    }
                    if (info.longMsg != null) {
                        pw.print("      Long msg: ");
                        pw.println(info.longMsg);
                    }
                    if (info.stack != null) {
                        pw.println("      Stack:");
                        int lastPos = 0;
                        int pos = 0;
                        while (true) {
                            if (pos >= info.stack.length()) {
                                break;
                            }
                            if (info.stack.charAt(pos) == '\n') {
                                pw.print("        ");
                                pw.write(info.stack, lastPos, pos - lastPos);
                                pw.println();
                                lastPos = pos + 1;
                            }
                            pos++;
                        }
                        if (lastPos < info.stack.length()) {
                            pw.print("        ");
                            pw.write(info.stack, lastPos, info.stack.length() - lastPos);
                            pw.println();
                        }
                    }
                }
            }
        }
        return needSep;
    }

    boolean isBadProcessLocked(ApplicationInfo info) {
        return this.mBadProcesses.get(info.processName, info.uid) != null;
    }

    void clearBadProcessLocked(ApplicationInfo info) {
        this.mBadProcesses.remove(info.processName, info.uid);
    }

    void resetProcessCrashTimeLocked(ApplicationInfo info) {
        this.mProcessCrashTimes.remove(info.processName, info.uid);
    }

    void resetProcessCrashTimeLocked(boolean resetEntireUser, int appId, int userId) {
        ArrayMap<String, SparseArray<Long>> pmap = this.mProcessCrashTimes.getMap();
        for (int ip = pmap.size() - 1; ip >= 0; ip--) {
            SparseArray<Long> ba = (SparseArray) pmap.valueAt(ip);
            for (int i = ba.size() - 1; i >= 0; i--) {
                boolean remove = false;
                int entUid = ba.keyAt(i);
                if (resetEntireUser) {
                    if (UserHandle.getUserId(entUid) == userId) {
                        remove = true;
                    }
                } else if (userId == -1) {
                    if (UserHandle.getAppId(entUid) == appId) {
                        remove = true;
                    }
                } else if (entUid == UserHandle.getUid(userId, appId)) {
                    remove = true;
                }
                if (remove) {
                    ba.removeAt(i);
                }
            }
            if (ba.size() == 0) {
                pmap.removeAt(ip);
            }
        }
    }

    void loadAppsNotReportingCrashesFromConfigLocked(String appsNotReportingCrashesConfig) {
        if (appsNotReportingCrashesConfig != null) {
            String[] split = appsNotReportingCrashesConfig.split(",");
            if (split.length > 0) {
                this.mAppsNotReportingCrashes = new ArraySet();
                Collections.addAll(this.mAppsNotReportingCrashes, split);
            }
        }
    }

    void killAppAtUserRequestLocked(ProcessRecord app, Dialog fromDialog) {
        app.crashing = false;
        app.crashingReport = null;
        app.notResponding = false;
        app.notRespondingReport = null;
        if (app.anrDialog == fromDialog) {
            app.anrDialog = null;
        }
        if (app.waitDialog == fromDialog) {
            app.waitDialog = null;
        }
        if (app.pid > 0 && app.pid != ActivityManagerService.MY_PID) {
            handleAppCrashLocked(app, "user-terminated", null, null, null, null);
            app.kill("user request after error", true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void scheduleAppCrashLocked(int uid, int initialPid, String packageName, String message) {
        ProcessRecord proc = null;
        synchronized (this.mService.mPidsSelfLocked) {
            int i = 0;
            while (true) {
                if (i >= this.mService.mPidsSelfLocked.size()) {
                    break;
                }
                ProcessRecord p = (ProcessRecord) this.mService.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid) {
                    if (p.pid == initialPid) {
                        break;
                    } else if (p.pkgList.containsKey(packageName)) {
                        proc = p;
                    }
                }
                i++;
            }
        }
        if (proc == null) {
            Slog.w(TAG, "crashApplication: nothing for uid=" + uid + " initialPid=" + initialPid + " packageName=" + packageName);
        } else {
            proc.scheduleCrash(message);
        }
    }

    void crashApplication(ProcessRecord r, CrashInfo crashInfo) {
        long origId = Binder.clearCallingIdentity();
        try {
            crashApplicationInner(r, crashInfo);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void crashApplicationInner(ProcessRecord r, CrashInfo crashInfo) {
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        AppErrorResult result = new AppErrorResult();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (handleAppCrashInActivityController(r, crashInfo, shortMsg, longMsg, stackTrace, timeMillis)) {
                    return;
                }
                if (r != null) {
                    if (r.instrumentationClass != null) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                if (r != null) {
                    this.mService.mBatteryStatsService.noteProcessCrash(r.processName, r.uid);
                }
                Data data = new Data();
                data.result = result;
                data.proc = r;
                if (r == null || !makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, data)) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                Message msg = Message.obtain();
                msg.what = 1;
                TaskRecord task = data.task;
                msg.obj = data;
                this.mService.mUiHandler.sendMessage(msg);
                ActivityManagerService.resetPriorityAfterLockedSection();
                int res = result.get();
                Intent appErrorIntent = null;
                MetricsLogger.action(this.mContext, 316, res);
                if (res == 6 || res == 7) {
                    res = 1;
                }
                synchronized (this.mService) {
                    long orig;
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (res == 5) {
                            stopReportingCrashesLocked(r);
                        }
                        if (res == 3) {
                            this.mService.removeProcessLocked(r, false, true, "crash");
                            if (task != null) {
                                this.mService.startActivityFromRecents(task.taskId, ActivityOptions.makeBasic().toBundle());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        if (task.intent.getCategories().contains("android.intent.category.LAUNCHER")) {
                            this.mService.startActivityInPackage(task.mCallingUid, task.mCallingPackage, task.intent, null, null, null, 0, 0, ActivityOptions.makeBasic().toBundle(), task.userId, null, null);
                        }
                    } catch (Throwable th) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                    if (res == 1) {
                        orig = Binder.clearCallingIdentity();
                        this.mService.mStackSupervisor.handleAppCrashLocked(r);
                        if (!r.persistent) {
                            this.mService.removeProcessLocked(r, false, false, "crash");
                            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                        }
                        Binder.restoreCallingIdentity(orig);
                    }
                    if (res == 2) {
                        appErrorIntent = createAppErrorIntentLocked(r, timeMillis, crashInfo);
                    }
                    if (!(r == null || r.isolated || res == 3)) {
                        this.mProcessCrashTimes.put(r.info.processName, r.uid, Long.valueOf(SystemClock.uptimeMillis()));
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (appErrorIntent != null) {
                    try {
                        this.mContext.startActivityAsUser(appErrorIntent, new UserHandle(r.userId));
                    } catch (Throwable e2) {
                        Slog.w(TAG, "bug report receiver dissappeared", e2);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis) {
        if (this.mService.mController == null) {
            return false;
        }
        String str;
        if (r != null) {
            try {
                str = r.processName;
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        } else {
            str = null;
        }
        int pid = r != null ? r.pid : Binder.getCallingPid();
        int uid = r != null ? r.info.uid : Binder.getCallingUid();
        if (!this.mService.mController.appCrashed(str, pid, shortMsg, longMsg, timeMillis, crashInfo.stackTrace)) {
            if ("1".equals(SystemProperties.get("ro.debuggable", "0")) && "Native crash".equals(crashInfo.exceptionClassName)) {
                Slog.w(TAG, "Skip killing native crashed app " + str + "(" + pid + ") during testing");
            } else {
                Slog.w(TAG, "Force-killing crashed app " + str + " at watcher's request");
                if (r == null) {
                    Process.killProcess(pid);
                    ActivityManagerService.killProcessGroup(uid, pid);
                } else if (!makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, null)) {
                    r.kill("crash", true);
                }
            }
            return true;
        }
        return false;
    }

    private boolean makeAppCrashingLocked(ProcessRecord app, String shortMsg, String longMsg, String stackTrace, Data data) {
        app.crashing = true;
        app.crashingReport = generateProcessError(app, 1, null, shortMsg, longMsg, stackTrace);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
        return handleAppCrashLocked(app, "force-crash", shortMsg, longMsg, stackTrace, data);
    }

    void startAppProblemLocked(ProcessRecord app) {
        app.errorReportReceiver = null;
        for (int userId : this.mService.mUserController.getCurrentProfileIdsLocked()) {
            if (app.userId == userId) {
                app.errorReportReceiver = ApplicationErrorReport.getErrorReportReceiver(this.mContext, app.info.packageName, app.info.flags);
            }
        }
        this.mService.skipCurrentReceiverLocked(app);
    }

    private ProcessErrorStateInfo generateProcessError(ProcessRecord app, int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
        ProcessErrorStateInfo report = new ProcessErrorStateInfo();
        report.condition = condition;
        report.processName = app.processName;
        report.pid = app.pid;
        report.uid = app.info.uid;
        report.tag = activity;
        report.shortMsg = shortMsg;
        report.longMsg = longMsg;
        report.stackTrace = stackTrace;
        return report;
    }

    Intent createAppErrorIntentLocked(ProcessRecord r, long timeMillis, CrashInfo crashInfo) {
        ApplicationErrorReport report = createAppErrorReportLocked(r, timeMillis, crashInfo);
        if (report == null) {
            return null;
        }
        Intent result = new Intent("android.intent.action.APP_ERROR");
        result.setComponent(r.errorReportReceiver);
        result.putExtra("android.intent.extra.BUG_REPORT", report);
        result.addFlags(268435456);
        return result;
    }

    private ApplicationErrorReport createAppErrorReportLocked(ProcessRecord r, long timeMillis, CrashInfo crashInfo) {
        boolean z = false;
        if (r.errorReportReceiver == null) {
            return null;
        }
        if (!r.crashing && !r.notResponding && !r.forceCrashReport) {
            return null;
        }
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.packageName = r.info.packageName;
        report.installerPackageName = r.errorReportReceiver.getPackageName();
        report.processName = r.processName;
        report.time = timeMillis;
        if ((r.info.flags & 1) != 0) {
            z = true;
        }
        report.systemApp = z;
        if (r.crashing || r.forceCrashReport) {
            report.type = 1;
            report.crashInfo = crashInfo;
        } else if (r.notResponding) {
            report.type = 2;
            report.anrInfo = new AnrInfo();
            report.anrInfo.activity = r.notRespondingReport.tag;
            report.anrInfo.cause = r.notRespondingReport.shortMsg;
            report.anrInfo.info = r.notRespondingReport.longMsg;
        }
        return report;
    }

    boolean handleAppCrashLocked(ProcessRecord app, String reason, String shortMsg, String longMsg, String stackTrace, Data data) {
        Long crashTime;
        long now = SystemClock.uptimeMillis();
        Long crashTimePersistent;
        if (app.isolated) {
            crashTimePersistent = null;
            crashTime = null;
        } else {
            crashTime = (Long) this.mProcessCrashTimes.get(app.info.processName, app.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(app.info.processName, app.uid);
        }
        if (crashTime == null || now >= crashTime.longValue() + 60000) {
            TaskRecord affectedTask = this.mService.mStackSupervisor.finishTopRunningActivityLocked(app, reason);
            if (data != null) {
                data.task = affectedTask;
            }
            if (!(data == null || r15 == null || now >= r15.longValue() + 60000)) {
                data.repeating = true;
            }
        } else {
            Slog.w(TAG, "Process " + app.info.processName + " has crashed too many times: killing!");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, new Object[]{Integer.valueOf(app.userId), app.info.processName, Integer.valueOf(app.uid)});
            this.mService.mStackSupervisor.handleAppCrashLocked(app);
            if (app.persistent) {
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            } else {
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.uid), app.info.processName});
                if (!app.isolated) {
                    ProcessMap processMap = this.mBadProcesses;
                    String str = app.info.processName;
                    int i = app.uid;
                    processMap.put(str, r21, new BadProcessInfo(now, shortMsg, longMsg, stackTrace));
                    this.mProcessCrashTimes.remove(app.info.processName, app.uid);
                }
                app.bad = true;
                app.removed = true;
                this.mService.removeProcessLocked(app, false, false, "crash");
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                return false;
            }
        }
        for (int i2 = app.services.size() - 1; i2 >= 0; i2--) {
            ServiceRecord sr = (ServiceRecord) app.services.valueAt(i2);
            sr.crashCount++;
        }
        ArrayList<ActivityRecord> activities = app.activities;
        if (app == this.mService.mHomeProcess && activities.size() > 0 && (this.mService.mHomeProcess.info.flags & 1) == 0) {
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.isHomeActivity()) {
                    Log.i(TAG, "Clearing package preferred activities from " + r.packageName);
                    try {
                        ActivityThread.getPackageManager().clearPackagePreferredActivities(r.packageName);
                    } catch (RemoteException e) {
                    }
                    this.mService.showUninstallLauncherDialog(r.packageName);
                }
            }
        }
        if (!app.isolated) {
            this.mProcessCrashTimes.put(app.info.processName, app.uid, Long.valueOf(now));
            this.mProcessCrashTimesPersistent.put(app.info.processName, app.uid, Long.valueOf(now));
        }
        if (app.crashHandler != null) {
            this.mService.mHandler.post(app.crashHandler);
        }
        return true;
    }

    private boolean hasForegroundUI(ProcessRecord proc) {
        boolean hasForegroundUI = proc != null ? proc.foregroundActivities : false;
        if (hasForegroundUI || proc == null) {
            return hasForegroundUI;
        }
        String packageName = proc.info.packageName;
        List<RunningTaskInfo> taskInfo = this.mService.getTasks(1, 0);
        if (taskInfo == null || taskInfo.size() <= 0) {
            return hasForegroundUI;
        }
        ComponentName componentInfo = ((RunningTaskInfo) taskInfo.get(0)).topActivity;
        if (componentInfo == null || packageName == null || !packageName.equalsIgnoreCase(componentInfo.getPackageName())) {
            return hasForegroundUI;
        }
        return true;
    }

    void handleShowAppErrorUi(Message msg) {
        Throwable th;
        Data data = msg.obj;
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        boolean showCrashDialog = false;
        Dialog d = null;
        synchronized (this.mService) {
            ActivityManagerService.boostPriorityForLockedSection();
            ProcessRecord proc = data.proc;
            AppErrorResult res = data.result;
            boolean isDebuggable = "1".equals(SystemProperties.get("ro.debuggable", "0"));
            if (isDebuggable || proc == null || !proc.forceCrashReport) {
                if (proc != null) {
                    if (proc.info != null) {
                        String packageName = proc.info.packageName;
                        if (this.mIswhitelist_for_short_time && whitelist_for_short_time.contains(packageName)) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        try {
                            if (this.mService.getRecordCust() != null) {
                                this.mService.getRecordCust().appExitRecord(packageName, "crash");
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                if (proc == null || proc.crashDialog == null) {
                    int isBackground = UserHandle.getAppId(proc.uid) >= AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT ? proc.pid != ActivityManagerService.MY_PID ? 1 : 0 : 0;
                    for (int userId : this.mService.mUserController.getCurrentProfileIdsLocked()) {
                        isBackground &= proc.userId != userId ? 1 : 0;
                    }
                    if (isBackground == 0 || showBackground) {
                        boolean contains;
                        if (this.mAppsNotReportingCrashes != null) {
                            contains = this.mAppsNotReportingCrashes.contains(proc.info.packageName);
                        } else {
                            contains = false;
                        }
                        if (isDebuggable && this.mService.canShowErrorDialogs() && !r1) {
                            if (!HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && hasForegroundUI(proc)) {
                                Dialog d2 = new AppErrorDialog(this.mContext, this.mService, data);
                                try {
                                    proc.crashDialog = d2;
                                    showCrashDialog = true;
                                    d = d2;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    if (showCrashDialog && d != null) {
                                        d.show();
                                    }
                                    return;
                                } catch (Throwable th3) {
                                    th = th3;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                        }
                        if (res != null) {
                            res.set(AppErrorDialog.CANT_SHOW);
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        try {
                            d.show();
                        } catch (RuntimeException e) {
                            Slog.w(TAG, "error dlg shows exception d != null", e);
                        }
                        return;
                    }
                    Slog.w(TAG, "Skipping crash dialog of " + proc + ": background");
                    if (res != null) {
                        res.set(AppErrorDialog.BACKGROUND_USER);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                Slog.e(TAG, "App already has crash dialog: " + proc);
                if (res != null) {
                    res.set(AppErrorDialog.ALREADY_SHOWING);
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                return;
            }
            Slog.w(TAG, "Skipping native crash dialog of " + proc);
            if (res != null) {
                res.set(AppErrorDialog.CANT_SHOW);
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }

    void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    final void appNotResponding(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        appNotResponding(app.pid, app, activity, parent, aboveSystem, annotation);
    }

    final void appNotResponding(int anrPid, ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        OutputStream outputStream;
        String[] nativeProcs;
        ActivityManagerService activityManagerService;
        int res;
        Message msg;
        HashMap<String, Object> map;
        Throwable th;
        if (IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("checkType", "FocusWindowNullScene");
            params.put("anrActivityName", activity != null ? activity.toString() : null);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().cancelCheckFreezeScreen(params);
            }
        }
        ArrayList<Integer> arrayList = new ArrayList(5);
        SparseArray<Boolean> sparseArray = new SparseArray(20);
        Trace.traceBegin(64, "anr_event_sync: appPid=" + app.pid + ", appName=" + app.processName + ", category=" + annotation);
        Trace.traceEnd(64);
        if (Log.HWINFO) {
            HwFrameworkFactory.getLogException().cmd(HwBroadcastRadarUtil.KEY_ACTION, "copy_systrace_to_cache");
        }
        if (this.mService.mController != null) {
            try {
                if (this.mService.mController.appEarlyNotResponding(app.processName, app.pid, annotation) < 0 && app.pid != ActivityManagerService.MY_PID) {
                    app.kill("anr", true);
                }
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        }
        long anrTime = SystemClock.uptimeMillis();
        this.mService.updateCpuStatsNow();
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mService.mShuttingDown) {
                    Slog.i(TAG, "During shutdown skipping ANR: " + app + " " + annotation);
                } else if (app.notResponding) {
                    Slog.i(TAG, "Skipping duplicate ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (app.crashing) {
                    Slog.i(TAG, "Crashing app skipping ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (anrPid != app.pid) {
                    Slog.i(TAG, "Skipping ANR because pid of " + app.processName + " is changed: " + "anr pid: " + anrPid + ", new pid: " + app.pid + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (this.mService.handleANRFilterFIFO(app.uid, 2)) {
                    Slog.i(TAG, "During holding skipping ANR: " + app + " " + annotation + "uid = " + app.uid);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else {
                    File tracesFile;
                    String cpuInfo;
                    String currentState;
                    app.notResponding = true;
                    EventLog.writeEvent(EventLogTags.AM_ANR, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.pid), app.processName, Integer.valueOf(app.info.flags), annotation});
                    arrayList.add(Integer.valueOf(app.pid));
                    boolean isSilentANR = (showBackground || app.isInterestingToUserLocked() || app.pid == ActivityManagerService.MY_PID) ? false : true;
                    if (!isSilentANR) {
                        int parentPid = app.pid;
                        if (!(parent == null || parent.app == null || parent.app.pid <= 0)) {
                            parentPid = parent.app.pid;
                        }
                        if (parentPid != app.pid) {
                            arrayList.add(Integer.valueOf(parentPid));
                        }
                        IHwBinderMonitor iBinderM = HwServiceFactory.getIHwBinderMonitor();
                        if (iBinderM != null) {
                            iBinderM.addBinderPid(arrayList, app.pid);
                        }
                        if (!(ActivityManagerService.MY_PID == app.pid || ActivityManagerService.MY_PID == parentPid)) {
                            arrayList.add(Integer.valueOf(ActivityManagerService.MY_PID));
                        }
                        for (int i = this.mService.mLruProcesses.size() - 1; i >= 0; i--) {
                            ProcessRecord r = (ProcessRecord) this.mService.mLruProcesses.get(i);
                            if (!(r == null || r.thread == null)) {
                                int pid = r.pid;
                                if (!(pid <= 0 || pid == app.pid || pid == parentPid || pid == ActivityManagerService.MY_PID)) {
                                    if (r.persistent) {
                                        arrayList.add(Integer.valueOf(pid));
                                    } else {
                                        sparseArray.put(pid, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    StringBuilder info = new StringBuilder();
                    info.setLength(0);
                    info.append("ANR in ").append(app.processName);
                    if (!(activity == null || activity.shortComponentName == null)) {
                        info.append(" (").append(activity.shortComponentName).append(")");
                    }
                    info.append("\n");
                    info.append("PID: ").append(app.pid).append("\n");
                    if (annotation != null) {
                        info.append("Reason: ").append(annotation).append("\n");
                    }
                    if (!(parent == null || parent == activity)) {
                        info.append("Parent: ").append(parent.shortComponentName).append("\n");
                    }
                    ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(true);
                    OutputStreamWriter outputStreamWriter = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        OutputStreamWriter outputStreamWriter2;
                        OutputStream fileOutputStream2 = new FileOutputStream(new File("/proc/sysrq-trigger"));
                        try {
                            outputStreamWriter2 = new OutputStreamWriter(fileOutputStream2, "UTF-8");
                        } catch (IOException e2) {
                            outputStream = fileOutputStream2;
                            try {
                                Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e3) {
                                        Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                                    }
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                nativeProcs = Watchdog.NATIVE_STACKS_OF_INTEREST;
                                if (isSilentANR) {
                                    activityManagerService = this.mService;
                                    tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, null, (SparseArray) sparseArray, null);
                                } else {
                                    activityManagerService = this.mService;
                                    tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, processCpuTracker, (SparseArray) sparseArray, nativeProcs);
                                }
                                this.mService.updateCpuStatsNow();
                                synchronized (this.mService.mProcessCpuTracker) {
                                    cpuInfo = this.mService.mProcessCpuTracker.printCurrentState(anrTime);
                                }
                                info.append(processCpuTracker.printCurrentLoad());
                                info.append(cpuInfo);
                                currentState = processCpuTracker.printCurrentState(anrTime);
                                info.append(currentState);
                                Slog.e(TAG, info.toString());
                                Slog.e(TAG, currentState);
                                if (tracesFile == null) {
                                    Process.sendSignal(app.pid, 3);
                                }
                                this.mService.addErrorToDropBox("anr", app, app.processName, activity, parent, annotation, cpuInfo, tracesFile, null);
                                if (this.mService.mController != null) {
                                    try {
                                        res = this.mService.mController.appNotResponding(app.processName, app.pid, info.toString());
                                        if (res != 0) {
                                            if (res < 0) {
                                            }
                                            synchronized (this.mService) {
                                                ActivityManagerService.boostPriorityForLockedSection();
                                                this.mService.mServices.scheduleServiceTimeoutLocked(app);
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                    } catch (RemoteException e4) {
                                        this.mService.mController = null;
                                        Watchdog.getInstance().setActivityController(null);
                                    } catch (Throwable th2) {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                synchronized (this.mService) {
                                    try {
                                        ActivityManagerService.boostPriorityForLockedSection();
                                        this.mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);
                                        if (app.pid != ActivityManagerService.MY_PID) {
                                            app.kill("bg anr", true);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                        makeAppNotRespondingLocked(app, activity == null ? null : activity.shortComponentName, annotation == null ? "ANR" : "ANR " + annotation, info.toString());
                                        msg = Message.obtain();
                                        map = new HashMap();
                                        msg.what = 2;
                                        msg.obj = map;
                                        msg.arg1 = aboveSystem ? 0 : 1;
                                        map.put("app", app);
                                        if (activity != null) {
                                            map.put("activity", activity);
                                        }
                                        this.mService.mUiHandler.sendMessage(msg);
                                    } finally {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e5) {
                                        Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                                        throw th;
                                    }
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            outputStream = fileOutputStream2;
                            if (outputStreamWriter != null) {
                                outputStreamWriter.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            throw th;
                        }
                        try {
                            outputStreamWriter2.write("w");
                            if (outputStreamWriter2 != null) {
                                try {
                                    outputStreamWriter2.close();
                                } catch (IOException e6) {
                                    Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                                }
                            }
                            if (fileOutputStream2 != null) {
                                fileOutputStream2.close();
                            }
                            outputStream = fileOutputStream2;
                            outputStreamWriter = outputStreamWriter2;
                        } catch (IOException e7) {
                            outputStream = fileOutputStream2;
                            outputStreamWriter = outputStreamWriter2;
                            Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                            if (outputStreamWriter != null) {
                                outputStreamWriter.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            nativeProcs = Watchdog.NATIVE_STACKS_OF_INTEREST;
                            if (isSilentANR) {
                                activityManagerService = this.mService;
                                tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, null, (SparseArray) sparseArray, null);
                            } else {
                                activityManagerService = this.mService;
                                tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, processCpuTracker, (SparseArray) sparseArray, nativeProcs);
                            }
                            this.mService.updateCpuStatsNow();
                            synchronized (this.mService.mProcessCpuTracker) {
                                cpuInfo = this.mService.mProcessCpuTracker.printCurrentState(anrTime);
                            }
                            info.append(processCpuTracker.printCurrentLoad());
                            info.append(cpuInfo);
                            currentState = processCpuTracker.printCurrentState(anrTime);
                            info.append(currentState);
                            Slog.e(TAG, info.toString());
                            Slog.e(TAG, currentState);
                            if (tracesFile == null) {
                                Process.sendSignal(app.pid, 3);
                            }
                            this.mService.addErrorToDropBox("anr", app, app.processName, activity, parent, annotation, cpuInfo, tracesFile, null);
                            if (this.mService.mController != null) {
                                res = this.mService.mController.appNotResponding(app.processName, app.pid, info.toString());
                                if (res != 0) {
                                    if (res < 0) {
                                    }
                                    synchronized (this.mService) {
                                        ActivityManagerService.boostPriorityForLockedSection();
                                        this.mService.mServices.scheduleServiceTimeoutLocked(app);
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                            }
                            synchronized (this.mService) {
                                ActivityManagerService.boostPriorityForLockedSection();
                                this.mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);
                                if (app.pid != ActivityManagerService.MY_PID) {
                                    app.kill("bg anr", true);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                if (activity == null) {
                                }
                                if (annotation == null) {
                                }
                                makeAppNotRespondingLocked(app, activity == null ? null : activity.shortComponentName, annotation == null ? "ANR" : "ANR " + annotation, info.toString());
                                msg = Message.obtain();
                                map = new HashMap();
                                msg.what = 2;
                                msg.obj = map;
                                if (aboveSystem) {
                                }
                                msg.arg1 = aboveSystem ? 0 : 1;
                                map.put("app", app);
                                if (activity != null) {
                                    map.put("activity", activity);
                                }
                                this.mService.mUiHandler.sendMessage(msg);
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            outputStream = fileOutputStream2;
                            outputStreamWriter = outputStreamWriter2;
                            if (outputStreamWriter != null) {
                                outputStreamWriter.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            throw th;
                        }
                    } catch (IOException e8) {
                        Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        nativeProcs = Watchdog.NATIVE_STACKS_OF_INTEREST;
                        if (isSilentANR) {
                            activityManagerService = this.mService;
                            tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, processCpuTracker, (SparseArray) sparseArray, nativeProcs);
                        } else {
                            activityManagerService = this.mService;
                            tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, null, (SparseArray) sparseArray, null);
                        }
                        this.mService.updateCpuStatsNow();
                        synchronized (this.mService.mProcessCpuTracker) {
                            cpuInfo = this.mService.mProcessCpuTracker.printCurrentState(anrTime);
                        }
                        info.append(processCpuTracker.printCurrentLoad());
                        info.append(cpuInfo);
                        currentState = processCpuTracker.printCurrentState(anrTime);
                        info.append(currentState);
                        Slog.e(TAG, info.toString());
                        Slog.e(TAG, currentState);
                        if (tracesFile == null) {
                            Process.sendSignal(app.pid, 3);
                        }
                        this.mService.addErrorToDropBox("anr", app, app.processName, activity, parent, annotation, cpuInfo, tracesFile, null);
                        if (this.mService.mController != null) {
                            res = this.mService.mController.appNotResponding(app.processName, app.pid, info.toString());
                            if (res != 0) {
                                if (res < 0) {
                                }
                                synchronized (this.mService) {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    this.mService.mServices.scheduleServiceTimeoutLocked(app);
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        }
                        synchronized (this.mService) {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);
                            if (app.pid != ActivityManagerService.MY_PID) {
                                app.kill("bg anr", true);
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            if (activity == null) {
                            }
                            if (annotation == null) {
                            }
                            makeAppNotRespondingLocked(app, activity == null ? null : activity.shortComponentName, annotation == null ? "ANR" : "ANR " + annotation, info.toString());
                            msg = Message.obtain();
                            map = new HashMap();
                            msg.what = 2;
                            msg.obj = map;
                            if (aboveSystem) {
                            }
                            msg.arg1 = aboveSystem ? 0 : 1;
                            map.put("app", app);
                            if (activity != null) {
                                map.put("activity", activity);
                            }
                            this.mService.mUiHandler.sendMessage(msg);
                        }
                    }
                    nativeProcs = Watchdog.NATIVE_STACKS_OF_INTEREST;
                    if (isSilentANR) {
                        activityManagerService = this.mService;
                        tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, null, (SparseArray) sparseArray, null);
                    } else {
                        activityManagerService = this.mService;
                        tracesFile = ActivityManagerService.dumpStackTraces(true, (ArrayList) arrayList, processCpuTracker, (SparseArray) sparseArray, nativeProcs);
                    }
                    this.mService.updateCpuStatsNow();
                    synchronized (this.mService.mProcessCpuTracker) {
                        cpuInfo = this.mService.mProcessCpuTracker.printCurrentState(anrTime);
                    }
                    info.append(processCpuTracker.printCurrentLoad());
                    info.append(cpuInfo);
                    currentState = processCpuTracker.printCurrentState(anrTime);
                    info.append(currentState);
                    Slog.e(TAG, info.toString());
                    Slog.e(TAG, currentState);
                    if (tracesFile == null) {
                        Process.sendSignal(app.pid, 3);
                    }
                    this.mService.addErrorToDropBox("anr", app, app.processName, activity, parent, annotation, cpuInfo, tracesFile, null);
                    if (this.mService.mController != null) {
                        res = this.mService.mController.appNotResponding(app.processName, app.pid, info.toString());
                        if (res != 0) {
                            if (res < 0 || app.pid == ActivityManagerService.MY_PID) {
                                synchronized (this.mService) {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    this.mService.mServices.scheduleServiceTimeoutLocked(app);
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            } else {
                                app.kill("anr", true);
                            }
                            return;
                        }
                    }
                    synchronized (this.mService) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        this.mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);
                        if (!(showBackground || app.isInterestingToUserLocked())) {
                            if (app.pid != ActivityManagerService.MY_PID) {
                                app.kill("bg anr", true);
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        }
                        if (activity == null) {
                        }
                        if (annotation == null) {
                        }
                        makeAppNotRespondingLocked(app, activity == null ? null : activity.shortComponentName, annotation == null ? "ANR" : "ANR " + annotation, info.toString());
                        msg = Message.obtain();
                        map = new HashMap();
                        msg.what = 2;
                        msg.obj = map;
                        if (aboveSystem) {
                        }
                        msg.arg1 = aboveSystem ? 0 : 1;
                        map.put("app", app);
                        if (activity != null) {
                            map.put("activity", activity);
                        }
                        this.mService.mUiHandler.sendMessage(msg);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void makeAppNotRespondingLocked(ProcessRecord app, String activity, String shortMsg, String longMsg) {
        app.notResponding = true;
        app.notRespondingReport = generateProcessError(app, 2, activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }

    void handleShowAnrUi(Message msg) {
        Throwable th;
        synchronized (this.mService) {
            ActivityManagerService.boostPriorityForLockedSection();
            HashMap<String, Object> data = msg.obj;
            ProcessRecord proc = (ProcessRecord) data.get("app");
            if (!(proc == null || proc.info == null)) {
                String packageName = proc.info.packageName;
                if (this.mIswhitelist_for_short_time && whitelist_for_short_time.contains(packageName)) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                try {
                    if (this.mService.getRecordCust() != null) {
                        this.mService.getRecordCust().appExitRecord(packageName, "anr");
                    }
                } catch (Throwable th2) {
                    th = th2;
                    r5 = null;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            Dialog dialog;
            if (proc == null || proc.anrDialog == null) {
                Intent intent = new Intent("android.intent.action.ANR");
                if (!this.mService.mProcessesReady) {
                    intent.addFlags(1342177280);
                }
                this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, 0);
                if (this.mService.canShowErrorDialogs() && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                    if (!this.mIsNotShowAnrDialog) {
                        dialog = new AppNotRespondingDialog(this.mService, this.mContext, proc, (ActivityRecord) data.get("activity"), msg.arg1 != 0);
                        try {
                            proc.anrDialog = dialog;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            if (dialog != null) {
                                dialog.show();
                            }
                            return;
                        } catch (Throwable th3) {
                            th = th3;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                MetricsLogger.action(this.mContext, 317, -1);
                this.mService.killAppAtUsersRequest(proc, null);
                dialog = null;
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (dialog != null) {
                    dialog.show();
                }
                return;
            }
            Slog.e(TAG, "App already has anr dialog: " + proc);
            MetricsLogger.action(this.mContext, 317, -2);
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }
}
