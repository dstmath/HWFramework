package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.server.PackageWatchdog;
import com.android.server.RescueParty;
import com.android.server.am.AppErrorDialog;
import com.android.server.am.AppNotRespondingDialog;
import com.android.server.pm.DumpState;
import com.android.server.wm.WindowProcessController;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.IHwPackageManager;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/* access modifiers changed from: package-private */
public class AppErrors {
    private static final String TAG = "ActivityManager";
    private final IZrHung mAppEyeANR = HwFrameworkFactory.getZrHung("appeye_anr");
    private final IZrHung mAppEyeBinderBlock = HwFrameworkFactory.getZrHung("appeye_ssbinderfull");
    private HwSysResource mAppResource;
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses = new ProcessMap<>();
    private final Context mContext;
    private HwCustAppErrors mHwCustAppErrors = ((HwCustAppErrors) HwCustUtils.createObj(HwCustAppErrors.class, new Object[0]));
    private final PackageWatchdog mPackageWatchdog;
    private final ProcessMap<Long> mProcessCrashTimes = new ProcessMap<>();
    private final ProcessMap<Long> mProcessCrashTimesPersistent = new ProcessMap<>();
    private final ActivityManagerService mService;

    AppErrors(Context context, ActivityManagerService service, PackageWatchdog watchdog) {
        context.assertRuntimeOverlayThemable();
        this.mService = service;
        this.mContext = context;
        this.mPackageWatchdog = watchdog;
        IZrHung iZrHung = this.mAppEyeANR;
        if (iZrHung != null) {
            iZrHung.init((ZrHungData) null);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId, String dumpPackage) {
        long token;
        SparseArray<BadProcessInfo> uids;
        String pname;
        ArrayMap<String, SparseArray<BadProcessInfo>> pmap;
        long token2;
        String pname2;
        int procCount;
        int uidCount;
        ArrayMap<String, SparseArray<Long>> pmap2;
        String str = dumpPackage;
        if (!this.mProcessCrashTimes.getMap().isEmpty() || !this.mBadProcesses.getMap().isEmpty()) {
            long token3 = proto.start(fieldId);
            long now = SystemClock.uptimeMillis();
            proto.write(1112396529665L, now);
            long j = 1138166333441L;
            long j2 = 2246267895810L;
            if (!this.mProcessCrashTimes.getMap().isEmpty()) {
                ArrayMap<String, SparseArray<Long>> pmap3 = this.mProcessCrashTimes.getMap();
                int procCount2 = pmap3.size();
                int ip = 0;
                while (ip < procCount2) {
                    long ctoken = proto.start(j2);
                    String pname3 = pmap3.keyAt(ip);
                    SparseArray<Long> uids2 = pmap3.valueAt(ip);
                    int uidCount2 = uids2.size();
                    proto.write(j, pname3);
                    int i = 0;
                    while (i < uidCount2) {
                        int puid = uids2.keyAt(i);
                        ProcessRecord r = (ProcessRecord) this.mService.getProcessNames().get(pname3, puid);
                        if (str != null) {
                            if (r != null) {
                                uidCount = uidCount2;
                                if (!r.pkgList.containsKey(str)) {
                                    token2 = token3;
                                    pmap2 = pmap3;
                                    procCount = procCount2;
                                    pname2 = pname3;
                                }
                            } else {
                                uidCount = uidCount2;
                                token2 = token3;
                                pmap2 = pmap3;
                                procCount = procCount2;
                                pname2 = pname3;
                            }
                            i++;
                            pmap3 = pmap2;
                            uidCount2 = uidCount;
                            procCount2 = procCount;
                            pname3 = pname2;
                            token3 = token2;
                        } else {
                            uidCount = uidCount2;
                        }
                        pmap2 = pmap3;
                        procCount = procCount2;
                        pname2 = pname3;
                        long etoken = proto.start(2246267895810L);
                        proto.write(1120986464257L, puid);
                        token2 = token3;
                        proto.write(1112396529666L, uids2.valueAt(i).longValue());
                        proto.end(etoken);
                        i++;
                        pmap3 = pmap2;
                        uidCount2 = uidCount;
                        procCount2 = procCount;
                        pname3 = pname2;
                        token3 = token2;
                    }
                    proto.end(ctoken);
                    ip++;
                    now = now;
                    j = 1138166333441L;
                    j2 = 2246267895810L;
                }
                token = token3;
            } else {
                token = token3;
            }
            if (!this.mBadProcesses.getMap().isEmpty()) {
                ArrayMap<String, SparseArray<BadProcessInfo>> pmap4 = this.mBadProcesses.getMap();
                int processCount = pmap4.size();
                int ip2 = 0;
                while (ip2 < processCount) {
                    long btoken = proto.start(2246267895811L);
                    String pname4 = pmap4.keyAt(ip2);
                    SparseArray<BadProcessInfo> uids3 = pmap4.valueAt(ip2);
                    int uidCount3 = uids3.size();
                    proto.write(1138166333441L, pname4);
                    int i2 = 0;
                    while (i2 < uidCount3) {
                        int puid2 = uids3.keyAt(i2);
                        ProcessRecord r2 = (ProcessRecord) this.mService.getProcessNames().get(pname4, puid2);
                        if (str != null) {
                            if (r2 == null) {
                                pmap = pmap4;
                                pname = pname4;
                                uids = uids3;
                            } else if (!r2.pkgList.containsKey(str)) {
                                pmap = pmap4;
                                pname = pname4;
                                uids = uids3;
                            }
                            i2++;
                            str = dumpPackage;
                            pmap4 = pmap;
                            pname4 = pname;
                            uids3 = uids;
                        }
                        BadProcessInfo info = uids3.valueAt(i2);
                        pmap = pmap4;
                        pname = pname4;
                        uids = uids3;
                        long etoken2 = proto.start(2246267895810L);
                        proto.write(1120986464257L, puid2);
                        proto.write(1112396529666L, info.time);
                        proto.write(1138166333443L, info.shortMsg);
                        proto.write(1138166333444L, info.longMsg);
                        proto.write(1138166333445L, info.stack);
                        proto.end(etoken2);
                        i2++;
                        str = dumpPackage;
                        pmap4 = pmap;
                        pname4 = pname;
                        uids3 = uids;
                    }
                    proto.end(btoken);
                    ip2++;
                    str = dumpPackage;
                }
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpLocked(FileDescriptor fd, PrintWriter pw, boolean needSep, String dumpPackage) {
        boolean needSep2;
        ArrayMap<String, SparseArray<BadProcessInfo>> pmap;
        String str;
        int processCount;
        AppErrors appErrors = this;
        String str2 = dumpPackage;
        String str3 = " uid ";
        if (!appErrors.mProcessCrashTimes.getMap().isEmpty()) {
            boolean printed = false;
            long now = SystemClock.uptimeMillis();
            ArrayMap<String, SparseArray<Long>> pmap2 = appErrors.mProcessCrashTimes.getMap();
            int processCount2 = pmap2.size();
            needSep2 = needSep;
            for (int ip = 0; ip < processCount2; ip++) {
                String pname = pmap2.keyAt(ip);
                SparseArray<Long> uids = pmap2.valueAt(ip);
                int uidCount = uids.size();
                int i = 0;
                while (i < uidCount) {
                    int puid = uids.keyAt(i);
                    ProcessRecord r = (ProcessRecord) appErrors.mService.getProcessNames().get(pname, puid);
                    if (str2 != null) {
                        if (r != null) {
                            processCount = processCount2;
                            if (!r.pkgList.containsKey(str2)) {
                            }
                        } else {
                            processCount = processCount2;
                        }
                        i++;
                        pmap2 = pmap2;
                        processCount2 = processCount;
                    } else {
                        processCount = processCount2;
                    }
                    if (!printed) {
                        if (needSep2) {
                            pw.println();
                        }
                        needSep2 = true;
                        pw.println("  Time since processes crashed:");
                        printed = true;
                    }
                    pw.print("    Process ");
                    pw.print(pname);
                    pw.print(str3);
                    pw.print(puid);
                    pw.print(": last crashed ");
                    TimeUtils.formatDuration(now - uids.valueAt(i).longValue(), pw);
                    pw.println(" ago");
                    i++;
                    pmap2 = pmap2;
                    processCount2 = processCount;
                }
            }
        } else {
            needSep2 = needSep;
        }
        if (!appErrors.mBadProcesses.getMap().isEmpty()) {
            boolean printed2 = false;
            ArrayMap<String, SparseArray<BadProcessInfo>> pmap3 = appErrors.mBadProcesses.getMap();
            int processCount3 = pmap3.size();
            int ip2 = 0;
            while (ip2 < processCount3) {
                String pname2 = pmap3.keyAt(ip2);
                SparseArray<BadProcessInfo> uids2 = pmap3.valueAt(ip2);
                int uidCount2 = uids2.size();
                int i2 = 0;
                while (i2 < uidCount2) {
                    int puid2 = uids2.keyAt(i2);
                    ProcessRecord r2 = (ProcessRecord) appErrors.mService.getProcessNames().get(pname2, puid2);
                    if (str2 == null || (r2 != null && r2.pkgList.containsKey(str2))) {
                        if (!printed2) {
                            if (needSep2) {
                                pw.println();
                            }
                            needSep2 = true;
                            pw.println("  Bad processes:");
                            printed2 = true;
                        }
                        BadProcessInfo info = uids2.valueAt(i2);
                        pw.print("    Bad process ");
                        pw.print(pname2);
                        pw.print(str3);
                        pw.print(puid2);
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
                                str = str3;
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
                                str3 = str;
                                pmap3 = pmap3;
                            }
                            pmap = pmap3;
                            if (lastPos < info.stack.length()) {
                                pw.print("        ");
                                pw.write(info.stack, lastPos, info.stack.length() - lastPos);
                                pw.println();
                            }
                        } else {
                            str = str3;
                            pmap = pmap3;
                        }
                        printed2 = printed2;
                    } else {
                        str = str3;
                        pmap = pmap3;
                    }
                    i2++;
                    appErrors = this;
                    str2 = dumpPackage;
                    str3 = str;
                    pmap3 = pmap;
                }
                ip2++;
                appErrors = this;
                str2 = dumpPackage;
            }
        }
        return needSep2;
    }

    /* access modifiers changed from: package-private */
    public boolean isBadProcessLocked(ApplicationInfo info) {
        return this.mBadProcesses.get(info.processName, info.uid) != null;
    }

    /* access modifiers changed from: package-private */
    public void clearBadProcessLocked(ApplicationInfo info) {
        this.mBadProcesses.remove(info.processName, info.uid);
    }

    /* access modifiers changed from: package-private */
    public void resetProcessCrashTimeLocked(ApplicationInfo info) {
        this.mProcessCrashTimes.remove(info.processName, info.uid);
    }

    /* access modifiers changed from: package-private */
    public void resetProcessCrashTimeLocked(boolean resetEntireUser, int appId, int userId) {
        ArrayMap<String, SparseArray<Long>> pmap = this.mProcessCrashTimes.getMap();
        for (int ip = pmap.size() - 1; ip >= 0; ip--) {
            SparseArray<Long> ba = pmap.valueAt(ip);
            for (int i = ba.size() - 1; i >= 0; i--) {
                boolean remove = false;
                int entUid = ba.keyAt(i);
                if (!resetEntireUser) {
                    if (userId == -1) {
                        if (UserHandle.getAppId(entUid) == appId) {
                            remove = true;
                        }
                    } else if (entUid == UserHandle.getUid(userId, appId)) {
                        remove = true;
                    }
                } else if (UserHandle.getUserId(entUid) == userId) {
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

    /* access modifiers changed from: package-private */
    public void loadAppsNotReportingCrashesFromConfigLocked(String appsNotReportingCrashesConfig) {
        if (appsNotReportingCrashesConfig != null) {
            String[] split = appsNotReportingCrashesConfig.split(",");
            if (split.length > 0) {
                this.mAppsNotReportingCrashes = new ArraySet<>();
                Collections.addAll(this.mAppsNotReportingCrashes, split);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void killAppAtUserRequestLocked(ProcessRecord app, Dialog fromDialog) {
        if (app.anrDialog == fromDialog) {
            app.anrDialog = null;
        }
        if (app.waitDialog == fromDialog) {
            app.waitDialog = null;
        }
        killAppImmediateLocked(app, "user-terminated", "user request after error");
    }

    private void killAppImmediateLocked(ProcessRecord app, String reason, String killReason) {
        app.setCrashing(false);
        app.crashingReport = null;
        app.setNotResponding(false);
        app.notRespondingReport = null;
        Flog.i(100, "Make notRespondingReport empty, caller=" + Debug.getCallers(15));
        if (app.pid > 0 && app.pid != ActivityManagerService.MY_PID) {
            handleAppCrashLocked(app, reason, null, null, null, null);
            app.kill(killReason, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleAppCrashLocked(int uid, int initialPid, String packageName, int userId, String message, boolean force) {
        ProcessRecord proc = null;
        synchronized (this.mService.mPidsSelfLocked) {
            int i = 0;
            while (true) {
                if (i >= this.mService.mPidsSelfLocked.size()) {
                    break;
                }
                ProcessRecord p = this.mService.mPidsSelfLocked.valueAt(i);
                if (uid < 0 || p.uid == uid) {
                    if (p.pid == initialPid) {
                        proc = p;
                        break;
                    } else if (p.pkgList.containsKey(packageName) && (userId < 0 || p.userId == userId)) {
                        proc = p;
                    }
                }
                i++;
            }
        }
        if (proc == null) {
            Slog.w("ActivityManager", "crashApplication: nothing for uid=" + uid + " initialPid=" + initialPid + " packageName=" + packageName + " userId=" + userId);
            return;
        }
        proc.scheduleCrash(message);
        if (force) {
            this.mService.mHandler.postDelayed(new Runnable(proc) {
                /* class com.android.server.am.$$Lambda$AppErrors$1aFX_jMSc0clpKk9XdlBZz9lU */
                private final /* synthetic */ ProcessRecord f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AppErrors.this.lambda$scheduleAppCrashLocked$0$AppErrors(this.f$1);
                }
            }, 5000);
        }
    }

    public /* synthetic */ void lambda$scheduleAppCrashLocked$0$AppErrors(ProcessRecord p) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                killAppImmediateLocked(p, "forced", "killed for invalid state");
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void crashApplication(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo) {
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            crashApplicationInner(r, crashInfo, callingPid, callingUid);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public void crashApplicationInner(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo, int callingPid, int callingUid) {
        String longMsg;
        ActivityManagerService activityManagerService;
        Throwable th;
        int res;
        Throwable th2;
        boolean z;
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg2 = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg2 != null) {
            longMsg = shortMsg + ": " + longMsg2;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        } else {
            longMsg = longMsg2;
        }
        if (r != null) {
            boolean isApexModule = false;
            try {
                String[] packageList = r.getPackageList();
                int length = packageList.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    if (this.mContext.getPackageManager().getModuleInfo(packageList[i], 0) != null) {
                        isApexModule = true;
                        break;
                    }
                    i++;
                }
            } catch (PackageManager.NameNotFoundException | IllegalStateException e) {
            }
            if (r.isPersistent() || isApexModule) {
                RescueParty.noteAppCrash(this.mContext, r.uid);
            }
            this.mPackageWatchdog.onPackageFailure(r.getPackageListWithVersionCode());
        }
        int relaunchReason = r != null ? r.getWindowProcessController().computeRelaunchReason() : 0;
        AppErrorResult result = new AppErrorResult();
        ActivityManagerService activityManagerService2 = this.mService;
        synchronized (activityManagerService2) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                activityManagerService = activityManagerService2;
                try {
                    if (handleAppCrashInActivityController(r, crashInfo, shortMsg, longMsg, stackTrace, timeMillis, callingPid, callingUid)) {
                        try {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th3) {
                            th = th3;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else if (relaunchReason == 2) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    } else if (r == null || r.getActiveInstrumentation() == null) {
                        if (r != null) {
                            this.mService.mBatteryStatsService.noteProcessCrash(r.processName, r.uid);
                        }
                        AppErrorDialog.Data data = new AppErrorDialog.Data();
                        try {
                            data.result = result;
                            data.proc = r;
                            if (r != null) {
                                if (makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, data)) {
                                    Message msg = Message.obtain();
                                    msg.what = 1;
                                    int taskId = data.taskId;
                                    msg.obj = data;
                                    this.mService.mUiHandler.sendMessage(msg);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    int res2 = result.get();
                                    Intent appErrorIntent = null;
                                    MetricsLogger.action(this.mContext, 316, res2);
                                    if (res2 == 6 || res2 == 7) {
                                        res = 1;
                                    } else {
                                        res = res2;
                                    }
                                    synchronized (this.mService) {
                                        try {
                                            ActivityManagerService.boostPriorityForLockedSection();
                                            if (res == 5) {
                                                try {
                                                    stopReportingCrashesLocked(r);
                                                } catch (Throwable th4) {
                                                    th2 = th4;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th2;
                                                }
                                            }
                                            if (res == 3) {
                                                z = false;
                                                this.mService.mProcessList.removeProcessLocked(r, false, true, "crash");
                                                if (taskId != -1) {
                                                    try {
                                                        this.mService.startActivityFromRecents(taskId, ActivityOptions.makeBasic().toBundle());
                                                    } catch (IllegalArgumentException e2) {
                                                        Slog.e("ActivityManager", "Could not restart taskId=" + taskId, e2);
                                                    }
                                                }
                                            } else {
                                                z = false;
                                            }
                                            if (res == 1) {
                                                long orig = Binder.clearCallingIdentity();
                                                try {
                                                    this.mService.mAtmInternal.onHandleAppCrash(r.getWindowProcessController());
                                                    if (!r.isPersistent()) {
                                                        this.mService.mProcessList.removeProcessLocked(r, z, z, "crash");
                                                        this.mService.mAtmInternal.resumeTopActivities(z);
                                                    }
                                                } finally {
                                                    Binder.restoreCallingIdentity(orig);
                                                }
                                            }
                                            if (res == 8) {
                                                appErrorIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                                                appErrorIntent.setData(Uri.parse("package:" + r.info.packageName));
                                                appErrorIntent.addFlags(268435456);
                                            }
                                            if (res == 2) {
                                                try {
                                                    appErrorIntent = createAppErrorIntentLocked(r, timeMillis, crashInfo);
                                                } catch (Throwable th5) {
                                                    th2 = th5;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th2;
                                                }
                                            }
                                            if (!r.isolated && res != 3) {
                                                this.mProcessCrashTimes.put(r.info.processName, r.uid, Long.valueOf(SystemClock.uptimeMillis()));
                                            }
                                        } catch (Throwable th6) {
                                            th2 = th6;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th2;
                                        }
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    if (appErrorIntent != null) {
                                        try {
                                            this.mContext.startActivityAsUser(appErrorIntent, new UserHandle(r.userId));
                                            return;
                                        } catch (ActivityNotFoundException e3) {
                                            Slog.w("ActivityManager", "bug report receiver dissappeared", e3);
                                            return;
                                        }
                                    } else {
                                        return;
                                    }
                                }
                            }
                            try {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            } catch (Throwable th7) {
                                th = th7;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } catch (Throwable th9) {
                    th = th9;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th10) {
                th = th10;
                activityManagerService = activityManagerService2;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis, int callingPid, int callingUid) {
        String name = r != null ? r.processName : null;
        int pid = r != null ? r.pid : callingPid;
        return this.mService.mAtmInternal.handleAppCrashInActivityController(name, pid, shortMsg, longMsg, timeMillis, crashInfo.stackTrace, new Runnable(crashInfo, name, pid, r, shortMsg, longMsg, stackTrace, r != null ? r.info.uid : callingUid) {
            /* class com.android.server.am.$$Lambda$AppErrors$Ziph9zXnTzhEV6frMYJe_IEvvfY */
            private final /* synthetic */ ApplicationErrorReport.CrashInfo f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ ProcessRecord f$4;
            private final /* synthetic */ String f$5;
            private final /* synthetic */ String f$6;
            private final /* synthetic */ String f$7;
            private final /* synthetic */ int f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AppErrors.this.lambda$handleAppCrashInActivityController$1$AppErrors(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    public /* synthetic */ void lambda$handleAppCrashInActivityController$1$AppErrors(ApplicationErrorReport.CrashInfo crashInfo, String name, int pid, ProcessRecord r, String shortMsg, String longMsg, String stackTrace, int uid) {
        if ("1".equals(SystemProperties.get("ro.debuggable", "0"))) {
            if ("Native crash".equals(crashInfo.exceptionClassName)) {
                Slog.w("ActivityManager", "Skip killing native crashed app " + name + "(" + pid + ") during testing");
                return;
            }
        }
        Slog.w("ActivityManager", "Force-killing crashed app " + name + " at watcher's request");
        if (r == null) {
            Process.killProcess(pid);
            ProcessList.killProcessGroup(uid, pid);
        } else if (!makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, null)) {
            r.kill("crash", true);
        }
    }

    private boolean makeAppCrashingLocked(ProcessRecord app, String shortMsg, String longMsg, String stackTrace, AppErrorDialog.Data data) {
        app.setCrashing(true);
        app.crashingReport = generateProcessError(app, 1, null, shortMsg, longMsg, stackTrace);
        app.startAppProblemLocked();
        app.getWindowProcessController().stopFreezingActivities();
        return handleAppCrashLocked(app, "force-crash", shortMsg, longMsg, stackTrace, data);
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.ProcessErrorStateInfo generateProcessError(ProcessRecord app, int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
        ActivityManager.ProcessErrorStateInfo report = new ActivityManager.ProcessErrorStateInfo();
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

    /* access modifiers changed from: package-private */
    public Intent createAppErrorIntentLocked(ProcessRecord r, long timeMillis, ApplicationErrorReport.CrashInfo crashInfo) {
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

    private ApplicationErrorReport createAppErrorReportLocked(ProcessRecord r, long timeMillis, ApplicationErrorReport.CrashInfo crashInfo) {
        if (r.errorReportReceiver == null) {
            return null;
        }
        if (!r.isCrashing() && !r.isNotResponding() && !r.forceCrashReport) {
            return null;
        }
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.packageName = r.info.packageName;
        report.installerPackageName = r.errorReportReceiver.getPackageName();
        report.processName = r.processName;
        report.time = timeMillis;
        report.systemApp = (r.info.flags & 1) != 0;
        if (r.isCrashing() || r.forceCrashReport) {
            report.type = 1;
            report.crashInfo = crashInfo;
        } else if (r.isNotResponding()) {
            report.type = 2;
            report.anrInfo = new ApplicationErrorReport.AnrInfo();
            report.anrInfo.activity = r.notRespondingReport.tag;
            report.anrInfo.cause = r.notRespondingReport.shortMsg;
            report.anrInfo.info = r.notRespondingReport.longMsg;
        }
        return report;
    }

    /* access modifiers changed from: package-private */
    public boolean handleAppCrashLocked(ProcessRecord app, String reason, String shortMsg, String longMsg, String stackTrace, AppErrorDialog.Data data) {
        Long crashTimePersistent;
        Long crashTime;
        long now;
        boolean tryAgain;
        boolean z;
        long now2 = SystemClock.uptimeMillis();
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        boolean procIsBoundForeground = app.getCurProcState() == 6;
        if (!app.isolated) {
            crashTime = (Long) this.mProcessCrashTimes.get(app.info.processName, app.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(app.info.processName, app.uid);
        } else {
            crashTime = null;
            crashTimePersistent = null;
        }
        boolean tryAgain2 = false;
        for (int i = app.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = app.services.valueAt(i);
            if (now2 > sr.restartTime + 60000) {
                sr.crashCount = 1;
            } else {
                sr.crashCount++;
            }
            if (((long) sr.crashCount) < this.mService.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY && (sr.isForeground || procIsBoundForeground)) {
                tryAgain2 = true;
            }
        }
        if (!((app.info.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0 || shortMsg == null || !shortMsg.contains("VerifyError") || longMsg == null || !longMsg.contains("Unimplemented function in interpreter"))) {
            rescueMapleProcess(app);
        }
        if (crashTime == null || now2 >= crashTime.longValue() + 60000) {
            now = now2;
            tryAgain = tryAgain2;
            int affectedTaskId = this.mService.mAtmInternal.finishTopCrashedActivities(app.getWindowProcessController(), reason);
            if (data != null) {
                data.taskId = affectedTaskId;
            }
            if (!(data == null || crashTimePersistent == null || now >= crashTimePersistent.longValue() + 60000)) {
                data.repeating = true;
                rescueMapleProcess(app);
            }
        } else {
            Slog.w("ActivityManager", "Process " + app.info.processName + " has crashed too many times: killing!");
            EventLog.writeEvent((int) EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, Integer.valueOf(app.userId), app.info.processName, Integer.valueOf(app.uid));
            this.mService.mAtmInternal.onHandleAppCrash(app.getWindowProcessController());
            if (!app.isPersistent()) {
                EventLog.writeEvent((int) EventLogTags.AM_PROC_BAD, Integer.valueOf(app.userId), Integer.valueOf(app.uid), app.info.processName);
                if (!app.isolated) {
                    now = now2;
                    tryAgain = tryAgain2;
                    this.mBadProcesses.put(app.info.processName, app.uid, new BadProcessInfo(now2, shortMsg, longMsg, stackTrace));
                    this.mProcessCrashTimes.remove(app.info.processName, app.uid);
                } else {
                    now = now2;
                    tryAgain = tryAgain2;
                }
                app.bad = true;
                app.removed = true;
                z = false;
                this.mService.mProcessList.removeProcessLocked(app, false, tryAgain, "crash");
                this.mService.mAtmInternal.resumeTopActivities(false);
                if (!showBackground) {
                    return false;
                }
            } else {
                now = now2;
                z = false;
                tryAgain = tryAgain2;
            }
            this.mService.mAtmInternal.resumeTopActivities(z);
            rescueMapleProcess(app);
        }
        if (data != null && tryAgain) {
            data.isRestartableForService = true;
        }
        WindowProcessController proc = app.getWindowProcessController();
        WindowProcessController homeProc = this.mService.mAtmInternal.getHomeProcess();
        if (proc == homeProc && proc.hasActivities() && (((ProcessRecord) homeProc.mOwner).info.flags & 1) == 0) {
            proc.clearPackagePreferredForHomeActivities();
        }
        if (!app.isolated) {
            this.mProcessCrashTimes.put(app.info.processName, app.uid, Long.valueOf(now));
            this.mProcessCrashTimesPersistent.put(app.info.processName, app.uid, Long.valueOf(now));
        }
        if (app.crashHandler == null) {
            return true;
        }
        this.mService.mHandler.post(app.crashHandler);
        return true;
    }

    private boolean hasForegroundUi(ProcessRecord proc) {
        ComponentName componentInfo;
        boolean isForeground = proc != null && proc.hasForegroundActivities();
        if (isForeground || proc == null) {
            return isForeground;
        }
        String packageName = proc.info.packageName;
        List<ActivityManager.RunningTaskInfo> taskInfo = this.mService.getTasks(1);
        if (taskInfo == null || taskInfo.size() <= 0 || (componentInfo = taskInfo.get(0).topActivity) == null || packageName == null || !packageName.equalsIgnoreCase(componentInfo.getPackageName())) {
            return isForeground;
        }
        return true;
    }

    private void rescueMapleProcess(ProcessRecord app) {
        if ((app.info.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 && (app.info.hwFlags & DumpState.DUMP_CHANGES) == 0) {
            try {
                IHwPackageManager hwPackageManager = HwPackageManager.getService();
                if (hwPackageManager != null && hwPackageManager.getMapleEnableFlag(app.info.packageName)) {
                    for (String pkg : app.pkgList.mPkgList.keySet()) {
                        hwPackageManager.setMapleEnableFlag(pkg, false);
                        Slog.w("ActivityManager", "set maple runtime to art for the app:" + pkg);
                    }
                }
            } catch (RemoteException e) {
                Slog.e("ActivityManager", "cannot get PackageManager.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleShowAppErrorUi(Message msg) {
        Throwable th;
        String packageName;
        int userId;
        boolean isShowCrash;
        AppErrorDialog.Data data = (AppErrorDialog.Data) msg.obj;
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        AppErrorDialog dialogToShow = null;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ProcessRecord proc = data.proc;
                AppErrorResult res = data.result;
                if (proc == null) {
                    try {
                        Slog.e("ActivityManager", "handleShowAppErrorUi: proc is null");
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } else {
                    packageName = proc.info.packageName;
                    boolean isDebuggable = "1".equals(SystemProperties.get("ro.debuggable", "0"));
                    if (isDebuggable || !proc.forceCrashReport) {
                        userId = proc.userId;
                        if (proc.crashDialog != null) {
                            Slog.e("ActivityManager", "App already has crash dialog: " + proc);
                            if (res != null) {
                                res.set(AppErrorDialog.ALREADY_SHOWING);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        boolean isBackground = UserHandle.getAppId(proc.uid) >= 10000 && proc.pid != ActivityManagerService.MY_PID;
                        int[] currentProfileIds = this.mService.mUserController.getCurrentProfileIds();
                        int length = currentProfileIds.length;
                        boolean isBackground2 = isBackground;
                        for (int i = 0; i < length; i++) {
                            isBackground2 &= userId != currentProfileIds[i];
                        }
                        if (!isBackground2 || showBackground) {
                            boolean showFirstCrash = Settings.Global.getInt(this.mContext.getContentResolver(), "show_first_crash_dialog", 0) != 0;
                            boolean showFirstCrashDevOption = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", 0, this.mService.mUserController.getCurrentUserId()) != 0;
                            boolean crashSilenced = this.mAppsNotReportingCrashes != null && this.mAppsNotReportingCrashes.contains(proc.info.packageName);
                            if (this.mHwCustAppErrors == null || !this.mHwCustAppErrors.isCustom()) {
                                isShowCrash = isDebuggable && (this.mService.mAtmInternal.canShowErrorDialogs() || showBackground) && !crashSilenced && ((showFirstCrash || showFirstCrashDevOption || data.repeating) && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && hasForegroundUi(proc));
                            } else {
                                isShowCrash = (this.mService.mAtmInternal.canShowErrorDialogs() || showBackground) && !crashSilenced && (showFirstCrash || showFirstCrashDevOption || data.repeating) && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && hasForegroundUi(proc);
                            }
                            if (this.mAppResource == null) {
                                Slog.i("ActivityManager", "get AppResource");
                                this.mAppResource = HwFrameworkFactory.getHwResource(18);
                            }
                            if (this.mAppResource != null) {
                                try {
                                    this.mAppResource.acquire(proc.uid, proc.info.packageName, ((proc.info.flags & 1) != 0 && (proc.info.hwFlags & DumpState.DUMP_APEX) == 0 && (proc.info.hwFlags & DumpState.DUMP_HANDLE) == 0) ? 2 : 0);
                                } catch (Throwable th3) {
                                    th = th3;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            if (isShowCrash) {
                                AppErrorDialog appErrorDialog = new AppErrorDialog(this.mContext, this.mService, data);
                                dialogToShow = appErrorDialog;
                                proc.crashDialog = appErrorDialog;
                                if (this.mAppEyeANR != null) {
                                    ZrHungData arg = new ZrHungData();
                                    arg.putString("WpName", "APP_CRASH");
                                    arg.putString("packageName", proc.info.packageName);
                                    arg.putString("processName", proc.processName);
                                    arg.putInt("pid", proc.pid);
                                    arg.putBoolean("isRepeating", data.repeating);
                                    this.mAppEyeANR.sendEvent(arg);
                                }
                            } else if (res != null) {
                                res.set(AppErrorDialog.CANT_SHOW);
                            }
                        } else {
                            Slog.w("ActivityManager", "Skipping crash dialog of " + proc + ": background");
                            if (res != null) {
                                res.set(AppErrorDialog.BACKGROUND_USER);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    } else {
                        Slog.w("ActivityManager", "Skipping native crash dialog of " + proc);
                        if (res != null) {
                            res.set(AppErrorDialog.CANT_SHOW);
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (dialogToShow != null) {
            Slog.i("ActivityManager", "Showing crash dialog for package " + packageName + " u" + userId);
            dialogToShow.show();
        }
    }

    private void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet<>();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    /* access modifiers changed from: package-private */
    public void handleShowAnrUi(Message msg) {
        Dialog dialogToShow = null;
        List<VersionedPackage> packageList = null;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                AppNotRespondingDialog.Data data = (AppNotRespondingDialog.Data) msg.obj;
                ProcessRecord proc = data.proc;
                if (proc == null) {
                    Slog.e("ActivityManager", "handleShowAnrUi: proc is null");
                    return;
                }
                if (!proc.isPersistent()) {
                    packageList = proc.getPackageListWithVersionCode();
                }
                if (proc.anrDialog != null) {
                    Slog.e("ActivityManager", "App already has anr dialog: " + proc);
                    MetricsLogger.action(this.mContext, 317, -2);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                boolean showBackground = false;
                if (Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0) {
                    showBackground = true;
                }
                if (this.mService.mAtmInternal.canShowErrorDialogs() || showBackground) {
                    dialogToShow = new AppNotRespondingDialog(this.mService, this.mContext, data);
                    proc.anrDialog = dialogToShow;
                } else {
                    MetricsLogger.action(this.mContext, 317, -1);
                    this.mService.killAppAtUsersRequest(proc, null);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (dialogToShow != null) {
            dialogToShow.show();
        }
        if (packageList != null) {
            this.mPackageWatchdog.onPackageFailure(packageList);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BadProcessInfo {
        final String longMsg;
        final String shortMsg;
        final String stack;
        final long time;

        BadProcessInfo(long time2, String shortMsg2, String longMsg2, String stack2) {
            this.time = time2;
            this.shortMsg = shortMsg2;
            this.longMsg = longMsg2;
            this.stack = stack2;
        }
    }
}
