package com.android.server.am;

import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RunningTaskInfo;
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
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.HwServiceFactory.ISystemBlockMonitor;
import com.android.server.RescueParty;
import com.android.server.Watchdog;
import com.android.server.connectivity.LingerMonitor;
import com.huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class AppErrors {
    private static final boolean IS_DEBUG_VERSION;
    private static final String TAG = "ActivityManager";
    public static final Set<String> whitelist_for_short_time = new ArraySet();
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses = new ProcessMap();
    private final Context mContext;
    private HwCustAppErrors mHwCustAppErrors = ((HwCustAppErrors) HwCustUtils.createObj(HwCustAppErrors.class, new Object[0]));
    private final boolean mIsNotShowAnrDialog = SystemProperties.getBoolean("ro.config.noshow_anrdialog", false);
    private final boolean mIswhitelist_for_short_time = SystemProperties.getBoolean("persist.sys.hwgmstemporary", false);
    private final ProcessMap<Long> mProcessCrashTimes = new ProcessMap();
    private final ProcessMap<Long> mProcessCrashTimesPersistent = new ProcessMap();
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
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
        whitelist_for_short_time.add("com.google.android.gms");
        whitelist_for_short_time.add("com.google.android.gsf");
        whitelist_for_short_time.add("com.google.android.gsf.login");
        whitelist_for_short_time.add("com.google.android.marvin.talkback");
        whitelist_for_short_time.add("com.android.chrome");
        whitelist_for_short_time.add("com.google.android.apps.books");
        whitelist_for_short_time.add("com.android.vending");
        whitelist_for_short_time.add("com.google.android.apps.docs");
        whitelist_for_short_time.add("com.google.android.apps.magazines");
        whitelist_for_short_time.add("com.google.android.apps.maps");
        whitelist_for_short_time.add("com.google.android.apps.photos");
        whitelist_for_short_time.add("com.google.android.apps.plus");
        whitelist_for_short_time.add("com.google.android.backuptransport");
        whitelist_for_short_time.add("com.google.android.configupdater");
        whitelist_for_short_time.add("com.google.android.ext.services");
        whitelist_for_short_time.add("com.google.android.ext.shared");
        whitelist_for_short_time.add("com.google.android.feedback");
        whitelist_for_short_time.add("com.google.android.gm");
        whitelist_for_short_time.add("com.google.android.googlequicksearchbox");
        whitelist_for_short_time.add("com.google.android.play.games");
        whitelist_for_short_time.add("com.google.android.inputmethod.latin");
        whitelist_for_short_time.add("com.google.android.music");
        whitelist_for_short_time.add("com.google.android.onetimeinitializer");
        whitelist_for_short_time.add("com.google.android.partnersetup");
        whitelist_for_short_time.add("com.google.android.play.games");
        whitelist_for_short_time.add("com.google.android.printservice.recommendation");
        whitelist_for_short_time.add("com.google.android.setupwizard");
        whitelist_for_short_time.add("com.google.android.syncadapters.calendar");
        whitelist_for_short_time.add("com.google.android.syncadapters.contacts");
        whitelist_for_short_time.add("com.google.android.talk");
        whitelist_for_short_time.add("com.google.android.tts");
        whitelist_for_short_time.add("com.google.android.videos");
        whitelist_for_short_time.add("com.google.android.youtube");
    }

    AppErrors(Context context, ActivityManagerService service) {
        context.assertRuntimeOverlayThemable();
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
        int puid;
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
                    puid = uids.keyAt(i);
                    r = (ProcessRecord) this.mService.mProcessNames.get(pname, puid);
                    if (dumpPackage == null || (r != null && (r.pkgList.containsKey(dumpPackage) ^ 1) == 0)) {
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
                    if (dumpPackage == null || (r != null && (r.pkgList.containsKey(dumpPackage) ^ 1) == 0)) {
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
                            for (int pos = 0; pos < info.stack.length(); pos++) {
                                if (info.stack.charAt(pos) == 10) {
                                    pw.print("        ");
                                    pw.write(info.stack, lastPos, pos - lastPos);
                                    pw.println();
                                    lastPos = pos + 1;
                                }
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

    void scheduleAppCrashLocked(int uid, int initialPid, String packageName, int userId, String message) {
        ProcessRecord proc = null;
        synchronized (this.mService.mPidsSelfLocked) {
            for (int i = 0; i < this.mService.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mService.mPidsSelfLocked.valueAt(i);
                if (uid < 0 || p.uid == uid) {
                    if (p.pid == initialPid) {
                        proc = p;
                        break;
                    } else if (p.pkgList.containsKey(packageName) && (userId < 0 || p.userId == userId)) {
                        proc = p;
                    }
                }
            }
        }
        if (proc == null) {
            Slog.w(TAG, "crashApplication: nothing for uid=" + uid + " initialPid=" + initialPid + " packageName=" + packageName + " userId=" + userId);
        } else {
            proc.scheduleCrash(message);
        }
    }

    void crashApplication(ProcessRecord r, CrashInfo crashInfo) {
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            crashApplicationInner(r, crashInfo, callingPid, callingUid);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX WARNING: Missing block: B:33:0x00ab, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:34:0x00ae, code:
            return;
     */
    /* JADX WARNING: Missing block: B:38:0x00d0, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r40 = r41.get();
            r32 = null;
            com.android.internal.logging.MetricsLogger.action(r43.mContext, 316, r40);
     */
    /* JADX WARNING: Missing block: B:39:0x00e7, code:
            if (r40 == 6) goto L_0x00ee;
     */
    /* JADX WARNING: Missing block: B:41:0x00ec, code:
            if (r40 != 7) goto L_0x00f0;
     */
    /* JADX WARNING: Missing block: B:42:0x00ee, code:
            r40 = 1;
     */
    /* JADX WARNING: Missing block: B:43:0x00f0, code:
            r5 = r43.mService;
     */
    /* JADX WARNING: Missing block: B:44:0x00f4, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
     */
    /* JADX WARNING: Missing block: B:47:0x00fb, code:
            if (r40 != 5) goto L_0x0100;
     */
    /* JADX WARNING: Missing block: B:48:0x00fd, code:
            stopReportingCrashesLocked(r44);
     */
    /* JADX WARNING: Missing block: B:50:0x0103, code:
            if (r40 != 3) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:51:0x0105, code:
            r43.mService.removeProcessLocked(r44, false, true, "crash");
            r37 = android.app.ActivityOptions.makeBasic();
     */
    /* JADX WARNING: Missing block: B:52:0x011b, code:
            if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x0130;
     */
    /* JADX WARNING: Missing block: B:54:0x0125, code:
            if (android.util.HwPCUtils.isValidExtDisplayId(r44.mDisplayId) == false) goto L_0x0130;
     */
    /* JADX WARNING: Missing block: B:55:0x0127, code:
            r37.setLaunchDisplayId(r44.mDisplayId);
     */
    /* JADX WARNING: Missing block: B:56:0x0130, code:
            if (r42 == null) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            r43.mService.startActivityFromRecents(r42.taskId, r37.toBundle());
     */
    /* JADX WARNING: Missing block: B:60:0x0144, code:
            if (r40 != 1) goto L_0x0175;
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            r38 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:64:?, code:
            r43.mService.mStackSupervisor.handleAppCrashLocked(r44);
     */
    /* JADX WARNING: Missing block: B:65:0x0159, code:
            if (r44.persistent != false) goto L_0x0172;
     */
    /* JADX WARNING: Missing block: B:66:0x015b, code:
            r43.mService.removeProcessLocked(r44, false, false, "crash");
            r43.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
     */
    /* JADX WARNING: Missing block: B:68:?, code:
            android.os.Binder.restoreCallingIdentity(r38);
     */
    /* JADX WARNING: Missing block: B:70:0x0178, code:
            if (r40 != 2) goto L_0x0184;
     */
    /* JADX WARNING: Missing block: B:71:0x017a, code:
            r32 = createAppErrorIntentLocked(r44, r10, r45);
     */
    /* JADX WARNING: Missing block: B:72:0x0184, code:
            if (r44 == null) goto L_0x01ac;
     */
    /* JADX WARNING: Missing block: B:74:0x018c, code:
            if ((r44.isolated ^ 1) == 0) goto L_0x01ac;
     */
    /* JADX WARNING: Missing block: B:76:0x0191, code:
            if (r40 == 3) goto L_0x01ac;
     */
    /* JADX WARNING: Missing block: B:77:0x0193, code:
            r43.mProcessCrashTimes.put(r44.info.processName, r44.uid, java.lang.Long.valueOf(android.os.SystemClock.uptimeMillis()));
     */
    /* JADX WARNING: Missing block: B:78:0x01ac, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:79:0x01ad, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:80:0x01b0, code:
            if (r32 == null) goto L_0x01c4;
     */
    /* JADX WARNING: Missing block: B:82:?, code:
            r43.mContext.startActivityAsUser(r32, new android.os.UserHandle(r44.userId));
     */
    /* JADX WARNING: Missing block: B:88:?, code:
            r33 = r42.intent.getCategories();
     */
    /* JADX WARNING: Missing block: B:89:0x01d4, code:
            if (r33 != null) goto L_0x01d6;
     */
    /* JADX WARNING: Missing block: B:91:0x01df, code:
            if (r33.contains("android.intent.category.LAUNCHER") != false) goto L_0x01e1;
     */
    /* JADX WARNING: Missing block: B:92:0x01e1, code:
            r43.mService.startActivityInPackage(r42.mCallingUid, r42.mCallingPackage, r42.intent, null, null, null, 0, 0, r37.toBundle(), r42.userId, null, null, "AppErrors");
     */
    /* JADX WARNING: Missing block: B:94:0x021b, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            android.os.Binder.restoreCallingIdentity(r38);
     */
    /* JADX WARNING: Missing block: B:99:0x0224, code:
            r34 = move-exception;
     */
    /* JADX WARNING: Missing block: B:100:0x0225, code:
            android.util.Slog.w(TAG, "bug report receiver dissappeared", r34);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void crashApplicationInner(ProcessRecord r, CrashInfo crashInfo, int callingPid, int callingUid) {
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        if (r != null && r.persistent) {
            RescueParty.notePersistentAppCrash(this.mContext, r.uid);
        }
        AppErrorResult result = new AppErrorResult();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (handleAppCrashInActivityController(r, crashInfo, shortMsg, longMsg, stackTrace, timeMillis, callingPid, callingUid)) {
                } else {
                    if (r != null) {
                        if (r.instr != null) {
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
                    if (r == null || (makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, data) ^ 1) != 0) {
                    } else {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        TaskRecord task = data.task;
                        msg.obj = data;
                        this.mService.mUiHandler.sendMessage(msg);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis, int callingPid, int callingUid) {
        if (this.mService.mController == null) {
            return false;
        }
        String name;
        if (r != null) {
            try {
                name = r.processName;
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        } else {
            name = null;
        }
        int pid = r != null ? r.pid : callingPid;
        int uid = r != null ? r.info.uid : callingUid;
        if (!this.mService.mController.appCrashed(name, pid, shortMsg, longMsg, timeMillis, crashInfo.stackTrace)) {
            if ("1".equals(SystemProperties.get("ro.debuggable", "0")) && "Native crash".equals(crashInfo.exceptionClassName)) {
                Slog.w(TAG, "Skip killing native crashed app " + name + "(" + pid + ") during testing");
            } else {
                Slog.w(TAG, "Force-killing crashed app " + name + " at watcher's request");
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
        if (!r.crashing && (r.notResponding ^ 1) != 0 && (r.forceCrashReport ^ 1) != 0) {
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
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        Long crashTimePersistent;
        if (app.isolated) {
            crashTimePersistent = null;
            crashTime = null;
        } else {
            crashTime = (Long) this.mProcessCrashTimes.get(app.info.processName, app.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(app.info.processName, app.uid);
        }
        if (crashTime == null || now >= crashTime.longValue() + LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS) {
            TaskRecord affectedTask = this.mService.mStackSupervisor.finishTopRunningActivityLocked(app, reason);
            if (data != null) {
                data.task = affectedTask;
            }
            if (!(data == null || crashTimePersistent == null || now >= crashTimePersistent.longValue() + LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS)) {
                data.repeating = true;
            }
        } else {
            Slog.w(TAG, "Process " + app.info.processName + " has crashed too many times: killing!");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, new Object[]{Integer.valueOf(app.userId), app.info.processName, Integer.valueOf(app.uid)});
            this.mService.mStackSupervisor.handleAppCrashLocked(app);
            if (!app.persistent) {
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.uid), app.info.processName});
                if (!app.isolated) {
                    this.mBadProcesses.put(app.info.processName, app.uid, new BadProcessInfo(now, shortMsg, longMsg, stackTrace));
                    this.mProcessCrashTimes.remove(app.info.processName, app.uid);
                }
                app.bad = true;
                app.removed = true;
                this.mService.removeProcessLocked(app, false, false, "crash");
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                if (!showBackground) {
                    return false;
                }
            }
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        boolean procIsBoundForeground = app.curProcState == 3;
        for (int i = app.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = (ServiceRecord) app.services.valueAt(i);
            sr.crashCount++;
            if (data != null && sr.crashCount <= 1 && (sr.isForeground || procIsBoundForeground)) {
                data.isRestartableForService = true;
            }
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

    /* JADX WARNING: Missing block: B:15:0x0074, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:16:0x0077, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:0x00f2, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:41:0x00f5, code:
            return;
     */
    /* JADX WARNING: Missing block: B:65:0x0188, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:66:0x018b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:96:0x0216, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:97:0x0219, code:
            if (r15 == false) goto L_0x0220;
     */
    /* JADX WARNING: Missing block: B:98:0x021b, code:
            if (r4 == null) goto L_0x0220;
     */
    /* JADX WARNING: Missing block: B:100:?, code:
            r4.show();
     */
    /* JADX WARNING: Missing block: B:121:0x025e, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:122:0x025f, code:
            android.util.Slog.w(TAG, "error dlg shows exception d != null", r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAppErrorUi(Message msg) {
        Throwable th;
        Data data = msg.obj;
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        boolean showCrashDialog = false;
        Dialog dialog = null;
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
                    int isBackground;
                    if (UserHandle.getAppId(proc.uid) >= 10000) {
                        isBackground = proc.pid != ActivityManagerService.MY_PID ? 1 : 0;
                    } else {
                        isBackground = 0;
                    }
                    int[] currentProfileIdsLocked = this.mService.mUserController.getCurrentProfileIdsLocked();
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= currentProfileIdsLocked.length) {
                            break;
                        }
                        isBackground &= proc.userId != currentProfileIdsLocked[i2] ? 1 : 0;
                        i = i2 + 1;
                    }
                    if (isBackground == 0 || (showBackground ^ 1) == 0) {
                        boolean isShowCrash;
                        int crashSilenced;
                        if (this.mAppsNotReportingCrashes != null) {
                            crashSilenced = this.mAppsNotReportingCrashes.contains(proc.info.packageName);
                        } else {
                            crashSilenced = 0;
                        }
                        if (this.mHwCustAppErrors == null || !this.mHwCustAppErrors.isCustom()) {
                            if (isDebuggable) {
                                if (!((!this.mService.canShowErrorDialogs() && !showBackground) || (crashSilenced ^ 1) == 0 || (HwFrameworkFactory.getVRSystemServiceManager().isVRMode() ^ 1) == 0)) {
                                    isShowCrash = hasForegroundUI(proc);
                                }
                            }
                            isShowCrash = false;
                        } else if ((!this.mService.canShowErrorDialogs() && !showBackground) || (crashSilenced ^ 1) == 0 || (HwFrameworkFactory.getVRSystemServiceManager().isVRMode() ^ 1) == 0) {
                            isShowCrash = false;
                        } else {
                            isShowCrash = hasForegroundUI(proc);
                        }
                        if (isShowCrash) {
                            Context context = this.mContext;
                            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(proc.mDisplayId)) {
                                Context tmpContext = HwPCUtils.getDisplayContext(context, proc.mDisplayId);
                                if (tmpContext != null) {
                                    context = tmpContext;
                                }
                            }
                            Dialog d = new AppErrorDialog(context, this.mService, data);
                            try {
                                proc.crashDialog = d;
                                showCrashDialog = true;
                                dialog = d;
                            } catch (Throwable th3) {
                                th = th3;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else if (res != null) {
                            res.set(AppErrorDialog.CANT_SHOW);
                        }
                    } else {
                        Slog.w(TAG, "Skipping crash dialog of " + proc + ": background");
                        if (res != null) {
                            res.set(AppErrorDialog.BACKGROUND_USER);
                        }
                    }
                } else {
                    Slog.e(TAG, "App already has crash dialog: " + proc);
                    if (res != null) {
                        res.set(AppErrorDialog.ALREADY_SHOWING);
                    }
                }
            } else {
                Slog.w(TAG, "Skipping native crash dialog of " + proc);
                if (res != null) {
                    res.set(AppErrorDialog.CANT_SHOW);
                }
            }
        }
    }

    void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    static boolean isInterestingForBackgroundTraces(ProcessRecord app) {
        boolean z = true;
        if (app.pid == ActivityManagerService.MY_PID) {
            return true;
        }
        if (!app.isInterestingToUserLocked() && ((app.info == null || !"com.android.systemui".equals(app.info.packageName)) && !app.hasTopUi)) {
            z = app.hasOverlayUi;
        }
        return z;
    }

    final void appNotResponding(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        appNotResponding(app.pid, app, activity, parent, aboveSystem, annotation);
    }

    /* JADX WARNING: Removed duplicated region for block: B:188:0x0528  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x04a2  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x052b  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04c7  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x05e3  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0536  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x05e7  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0539  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0551 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x058f  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x05b7 A:{SYNTHETIC, Splitter: B:205:0x05b7} */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x061b A:{SYNTHETIC, Splitter: B:230:0x061b} */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x04fb A:{SYNTHETIC, Splitter: B:172:0x04fb} */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x0500 A:{Catch:{ IOException -> 0x0504 }} */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x04a2  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0528  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04c7  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x052b  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0536  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x05e3  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0539  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x05e7  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0551 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x058f  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x05b7 A:{SYNTHETIC, Splitter: B:205:0x05b7} */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x061b A:{SYNTHETIC, Splitter: B:230:0x061b} */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0511 A:{SYNTHETIC, Splitter: B:180:0x0511} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0516 A:{Catch:{ IOException -> 0x051a }} */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0528  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x04a2  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x052b  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04c7  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x05e3  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0536  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x05e7  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0539  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0551 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x058f  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x05b7 A:{SYNTHETIC, Splitter: B:205:0x05b7} */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x061b A:{SYNTHETIC, Splitter: B:230:0x061b} */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x04fb A:{SYNTHETIC, Splitter: B:172:0x04fb} */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x0500 A:{Catch:{ IOException -> 0x0504 }} */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x04a2  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0528  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04c7  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x052b  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0536  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x05e3  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0539  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x05e7  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0551 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x058f  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x05b7 A:{SYNTHETIC, Splitter: B:205:0x05b7} */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x061b A:{SYNTHETIC, Splitter: B:230:0x061b} */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0511 A:{SYNTHETIC, Splitter: B:180:0x0511} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0516 A:{Catch:{ IOException -> 0x051a }} */
    /* JADX WARNING: Missing block: B:126:0x03cf, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r27 = new java.lang.StringBuilder();
            r27.setLength(0);
            r27.append("ANR in ").append(r50.processName);
     */
    /* JADX WARNING: Missing block: B:127:0x03ed, code:
            if (r51 == null) goto L_0x040c;
     */
    /* JADX WARNING: Missing block: B:129:0x03f3, code:
            if (r51.shortComponentName == null) goto L_0x040c;
     */
    /* JADX WARNING: Missing block: B:130:0x03f5, code:
            r27.append(" (").append(r51.shortComponentName).append(")");
     */
    /* JADX WARNING: Missing block: B:131:0x040c, code:
            r27.append("\n");
            r27.append("PID: ").append(r50.pid).append("\n");
     */
    /* JADX WARNING: Missing block: B:132:0x042b, code:
            if (r54 == null) goto L_0x0442;
     */
    /* JADX WARNING: Missing block: B:133:0x042d, code:
            r27.append("Reason: ").append(r54).append("\n");
     */
    /* JADX WARNING: Missing block: B:134:0x0442, code:
            if (r52 == null) goto L_0x0461;
     */
    /* JADX WARNING: Missing block: B:136:0x0448, code:
            if (r52 == r51) goto L_0x0461;
     */
    /* JADX WARNING: Missing block: B:137:0x044a, code:
            r27.append("Parent: ").append(r52.shortComponentName).append("\n");
     */
    /* JADX WARNING: Missing block: B:138:0x0461, code:
            r0 = new com.android.internal.os.ProcessCpuTracker(true);
            r43 = null;
            r45 = null;
     */
    /* JADX WARNING: Missing block: B:140:?, code:
            r0 = new java.io.FileOutputStream(new java.io.File("/proc/sysrq-trigger"));
     */
    /* JADX WARNING: Missing block: B:142:?, code:
            r0 = new java.io.OutputStreamWriter(r0, "UTF-8");
     */
    /* JADX WARNING: Missing block: B:144:?, code:
            r0.write("w");
     */
    /* JADX WARNING: Missing block: B:145:0x0490, code:
            if (r0 == null) goto L_0x0495;
     */
    /* JADX WARNING: Missing block: B:147:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:148:0x0495, code:
            if (r0 == null) goto L_0x049a;
     */
    /* JADX WARNING: Missing block: B:149:0x0497, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:150:0x049a, code:
            r45 = r0;
            r43 = r0;
     */
    /* JADX WARNING: Missing block: B:151:0x049e, code:
            r34 = null;
     */
    /* JADX WARNING: Missing block: B:152:0x04a0, code:
            if (r28 != 0) goto L_0x04a2;
     */
    /* JADX WARNING: Missing block: B:153:0x04a2, code:
            r25 = 0;
     */
    /* JADX WARNING: Missing block: B:155:0x04a9, code:
            if (r25 < com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST.length) goto L_0x04ab;
     */
    /* JADX WARNING: Missing block: B:157:0x04b7, code:
            if (com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST[r25].equals(r50.processName) != false) goto L_0x04b9;
     */
    /* JADX WARNING: Missing block: B:158:0x04b9, code:
            r34 = new java.lang.String[]{r50.processName};
     */
    /* JADX WARNING: Missing block: B:159:0x04c5, code:
            if (r34 == null) goto L_0x04c7;
     */
    /* JADX WARNING: Missing block: B:160:0x04c7, code:
            r38 = null;
     */
    /* JADX WARNING: Missing block: B:161:0x04c9, code:
            r9 = null;
     */
    /* JADX WARNING: Missing block: B:162:0x04ca, code:
            if (r38 != null) goto L_0x04cc;
     */
    /* JADX WARNING: Missing block: B:163:0x04cc, code:
            r9 = new java.util.ArrayList(r38.length);
            r4 = 0;
            r5 = r38.length;
     */
    /* JADX WARNING: Missing block: B:164:0x04d8, code:
            if (r4 < r5) goto L_0x04da;
     */
    /* JADX WARNING: Missing block: B:165:0x04da, code:
            r9.add(java.lang.Integer.valueOf(r38[r4]));
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:167:0x04e7, code:
            android.util.Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
     */
    /* JADX WARNING: Missing block: B:170:?, code:
            android.util.Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
     */
    /* JADX WARNING: Missing block: B:171:0x04f9, code:
            if (r43 != null) goto L_0x04fb;
     */
    /* JADX WARNING: Missing block: B:173:?, code:
            r43.close();
     */
    /* JADX WARNING: Missing block: B:174:0x04fe, code:
            if (r45 != null) goto L_0x0500;
     */
    /* JADX WARNING: Missing block: B:175:0x0500, code:
            r45.close();
     */
    /* JADX WARNING: Missing block: B:177:0x0505, code:
            android.util.Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
     */
    /* JADX WARNING: Missing block: B:178:0x050e, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:179:0x050f, code:
            if (r43 != null) goto L_0x0511;
     */
    /* JADX WARNING: Missing block: B:181:?, code:
            r43.close();
     */
    /* JADX WARNING: Missing block: B:182:0x0514, code:
            if (r45 != null) goto L_0x0516;
     */
    /* JADX WARNING: Missing block: B:183:0x0516, code:
            r45.close();
     */
    /* JADX WARNING: Missing block: B:184:0x0519, code:
            throw r4;
     */
    /* JADX WARNING: Missing block: B:186:0x051b, code:
            android.util.Slog.e(TAG, "Failed to write to /proc/sysrq-trigger");
     */
    /* JADX WARNING: Missing block: B:187:0x0524, code:
            r25 = r25 + 1;
     */
    /* JADX WARNING: Missing block: B:188:0x0528, code:
            r34 = com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST;
     */
    /* JADX WARNING: Missing block: B:189:0x052b, code:
            r38 = android.os.Process.getPidsForCommands(r34);
     */
    /* JADX WARNING: Missing block: B:190:0x0530, code:
            r4 = r48.mService;
     */
    /* JADX WARNING: Missing block: B:191:0x0534, code:
            if (r28 != 0) goto L_0x0536;
     */
    /* JADX WARNING: Missing block: B:192:0x0536, code:
            r7 = null;
     */
    /* JADX WARNING: Missing block: B:193:0x0537, code:
            if (r28 != 0) goto L_0x0539;
     */
    /* JADX WARNING: Missing block: B:194:0x0539, code:
            r8 = null;
     */
    /* JADX WARNING: Missing block: B:195:0x053a, code:
            r18 = com.android.server.am.ActivityManagerService.dumpStackTraces(r50, true, r6, r7, r8, r9);
            r48.mService.updateCpuStatsNow();
     */
    /* JADX WARNING: Missing block: B:196:0x0550, code:
            monitor-enter(r48.mService.mProcessCpuTracker);
     */
    /* JADX WARNING: Missing block: B:198:?, code:
            r17 = r48.mService.mProcessCpuTracker.printCurrentState(r20);
     */
    /* JADX WARNING: Missing block: B:200:0x055e, code:
            r27.append(r0.printCurrentLoad());
            r27.append(r17);
            r22 = r0.printCurrentState(r20);
            r27.append(r22);
            android.util.Slog.e(TAG, r27.toString());
            android.util.Slog.e(TAG, r22);
     */
    /* JADX WARNING: Missing block: B:201:0x058d, code:
            if (r18 == null) goto L_0x058f;
     */
    /* JADX WARNING: Missing block: B:202:0x058f, code:
            android.os.Process.sendSignal(r50.pid, 3);
     */
    /* JADX WARNING: Missing block: B:203:0x0597, code:
            r48.mService.addErrorToDropBox("anr", r50, r50.processName, r51, r52, r54, r17, r18, null);
     */
    /* JADX WARNING: Missing block: B:204:0x05b5, code:
            if (r48.mService.mController != null) goto L_0x05b7;
     */
    /* JADX WARNING: Missing block: B:206:?, code:
            r41 = r48.mService.mController.appNotResponding(r50.processName, r50.pid, r27.toString());
     */
    /* JADX WARNING: Missing block: B:207:0x05cd, code:
            if (r41 != 0) goto L_0x05cf;
     */
    /* JADX WARNING: Missing block: B:208:0x05cf, code:
            if (r41 >= 0) goto L_0x05ee;
     */
    /* JADX WARNING: Missing block: B:211:0x05d9, code:
            r50.kill("anr", true);
     */
    /* JADX WARNING: Missing block: B:212:0x05e2, code:
            return;
     */
    /* JADX WARNING: Missing block: B:213:0x05e3, code:
            r7 = r0;
     */
    /* JADX WARNING: Missing block: B:214:0x05e7, code:
            r8 = r0;
     */
    /* JADX WARNING: Missing block: B:220:0x05f2, code:
            monitor-enter(r48.mService);
     */
    /* JADX WARNING: Missing block: B:222:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r48.mService.mServices.scheduleServiceTimeoutLocked(r50);
     */
    /* JADX WARNING: Missing block: B:225:0x0602, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:227:0x0607, code:
            r48.mService.mController = null;
            com.android.server.Watchdog.getInstance().setActivityController(null);
     */
    /* JADX WARNING: Missing block: B:229:0x061a, code:
            monitor-enter(r48.mService);
     */
    /* JADX WARNING: Missing block: B:231:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r48.mService.mBatteryStatsService.noteProcessAnr(r50.processName, r50.uid);
     */
    /* JADX WARNING: Missing block: B:232:0x062f, code:
            if (r42 != false) goto L_0x0655;
     */
    /* JADX WARNING: Missing block: B:237:0x0641, code:
            r50.kill("bg anr", true);
     */
    /* JADX WARNING: Missing block: B:243:0x0651, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:245:0x0655, code:
            if (r51 != null) goto L_0x0657;
     */
    /* JADX WARNING: Missing block: B:247:?, code:
            r5 = r51.shortComponentName;
     */
    /* JADX WARNING: Missing block: B:248:0x065c, code:
            if (r54 != null) goto L_0x065e;
     */
    /* JADX WARNING: Missing block: B:249:0x065e, code:
            r4 = "ANR " + r54;
     */
    /* JADX WARNING: Missing block: B:250:0x0674, code:
            makeAppNotRespondingLocked(r50, r5, r4, r27.toString());
            r33 = android.os.Message.obtain();
            r32 = new java.util.HashMap();
            r33.what = 2;
            r33.obj = r32;
     */
    /* JADX WARNING: Missing block: B:251:0x0693, code:
            if (r53 != false) goto L_0x0695;
     */
    /* JADX WARNING: Missing block: B:252:0x0695, code:
            r4 = 1;
     */
    /* JADX WARNING: Missing block: B:253:0x0696, code:
            r33.arg1 = r4;
            r32.put("app", r50);
     */
    /* JADX WARNING: Missing block: B:254:0x06a4, code:
            if (r51 != null) goto L_0x06a6;
     */
    /* JADX WARNING: Missing block: B:255:0x06a6, code:
            r32.put("activity", r51);
     */
    /* JADX WARNING: Missing block: B:256:0x06b0, code:
            r48.mService.mUiHandler.sendMessage(r33);
     */
    /* JADX WARNING: Missing block: B:258:0x06bc, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:259:0x06bf, code:
            return;
     */
    /* JADX WARNING: Missing block: B:260:0x06c0, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:262:?, code:
            r4 = "ANR";
     */
    /* JADX WARNING: Missing block: B:263:0x06c7, code:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:265:0x06cb, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:266:0x06cf, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:267:0x06d0, code:
            r45 = r0;
     */
    /* JADX WARNING: Missing block: B:268:0x06d4, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:269:0x06d5, code:
            r45 = r0;
            r43 = r0;
     */
    /* JADX WARNING: Missing block: B:271:0x06dc, code:
            r45 = r0;
     */
    /* JADX WARNING: Missing block: B:273:0x06e1, code:
            r45 = r0;
            r43 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void appNotResponding(int anrPid, ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        if (IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("checkType", "FocusWindowNullScene");
            params.put("anrActivityName", activity != null ? activity.toString() : null);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().cancelCheckFreezeScreen(params);
            }
        }
        ArrayList<Integer> firstPids = new ArrayList(5);
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
                } else if (app.killedByAm) {
                    Slog.i(TAG, "App already killed by AM skipping ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else {
                    if (app.killed) {
                        Slog.i(TAG, "Skipping died app ANR: " + app + " " + annotation);
                    } else if (anrPid != app.pid) {
                        Slog.i(TAG, "Skipping ANR because pid of " + app.processName + " is changed: " + "anr pid: " + anrPid + ", new pid: " + app.pid + " " + annotation);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    } else if (this.mService.handleANRFilterFIFO(app.uid, 2)) {
                        Slog.i(TAG, "During holding skipping ANR: " + app + " " + annotation + "uid = " + app.uid);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    app.notResponding = true;
                    EventLog.writeEvent(EventLogTags.AM_ANR, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.pid), app.processName, Integer.valueOf(app.info.flags), annotation});
                    firstPids.add(Integer.valueOf(app.pid));
                    ISystemBlockMonitor mSystemBlockMonitor = HwServiceFactory.getISystemBlockMonitor();
                    if (!(mSystemBlockMonitor == null || mSystemBlockMonitor.checkRecentLockedState() == 0)) {
                        firstPids.add(Integer.valueOf(mSystemBlockMonitor.checkRecentLockedState()));
                    }
                    int isSilentANR = !showBackground ? isInterestingForBackgroundTraces(app) ^ 1 : 0;
                    if (isSilentANR == 0) {
                        int parentPid = app.pid;
                        if (!(parent == null || parent.app == null || parent.app.pid <= 0)) {
                            parentPid = parent.app.pid;
                        }
                        if (parentPid != app.pid) {
                            firstPids.add(Integer.valueOf(parentPid));
                        }
                        IHwBinderMonitor iBinderM = HwServiceFactory.getIHwBinderMonitor();
                        if (iBinderM != null) {
                            iBinderM.addBinderPid(firstPids, app.pid);
                        }
                        if (!(ActivityManagerService.MY_PID == app.pid || ActivityManagerService.MY_PID == parentPid)) {
                            firstPids.add(Integer.valueOf(ActivityManagerService.MY_PID));
                        }
                        for (int i = this.mService.mLruProcesses.size() - 1; i >= 0; i--) {
                            ProcessRecord r = (ProcessRecord) this.mService.mLruProcesses.get(i);
                            if (!(r == null || r.thread == null)) {
                                int pid = r.pid;
                                if (!(pid <= 0 || pid == app.pid || pid == parentPid || pid == ActivityManagerService.MY_PID)) {
                                    if (r.persistent) {
                                        firstPids.add(Integer.valueOf(pid));
                                    } else if (r.treatLikeActivity) {
                                        firstPids.add(Integer.valueOf(pid));
                                    } else {
                                        sparseArray.put(pid, Boolean.TRUE);
                                    }
                                }
                            }
                        }
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

    /* JADX WARNING: Missing block: B:53:0x0134, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:54:0x0137, code:
            if (r5 == null) goto L_0x013c;
     */
    /* JADX WARNING: Missing block: B:55:0x0139, code:
            r5.show();
     */
    /* JADX WARNING: Missing block: B:56:0x013c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAnrUi(Message msg) {
        Throwable th;
        synchronized (this.mService) {
            Dialog dialog;
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
                    dialog = null;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            if (proc == null || proc.anrDialog == null) {
                Intent intent = new Intent("android.intent.action.ANR");
                if (!this.mService.mProcessesReady) {
                    intent.addFlags(1342177280);
                }
                this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, 0);
                boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
                if ((!this.mService.canShowErrorDialogs() && !showBackground) || (HwFrameworkFactory.getVRSystemServiceManager().isVRMode() ^ 1) == 0 || (this.mIsNotShowAnrDialog ^ 1) == 0) {
                    MetricsLogger.action(this.mContext, 317, -1);
                    this.mService.killAppAtUsersRequest(proc, null);
                    dialog = null;
                } else {
                    Context context = this.mContext;
                    if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(proc.mDisplayId)) {
                        Context tmpContext = HwPCUtils.getDisplayContext(context, proc.mDisplayId);
                        if (tmpContext != null) {
                            context = tmpContext;
                        }
                    }
                    dialog = new AppNotRespondingDialog(this.mService, context, proc, (ActivityRecord) data.get("activity"), msg.arg1 != 0);
                    try {
                        proc.anrDialog = dialog;
                    } catch (Throwable th3) {
                        th = th3;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            Slog.e(TAG, "App already has anr dialog: " + proc);
            MetricsLogger.action(this.mContext, 317, -2);
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }
}
