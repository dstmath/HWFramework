package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.HwServiceFactory;
import com.android.server.RescueParty;
import com.android.server.Watchdog;
import com.android.server.am.AppErrorDialog;
import com.android.server.am.AppNotRespondingDialog;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.zrhung.IZRHungService;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.IHwPackageManager;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class AppErrors {
    private static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final String TAG = "ActivityManager";
    private static boolean sIsMygote;
    public static final Set<String> whitelist_for_short_time = new ArraySet();
    private final IZrHung mAppEyeANR = HwFrameworkFactory.getZrHung("appeye_anr");
    private final IZrHung mAppEyeBinderBlock = HwFrameworkFactory.getZrHung("appeye_ssbinderfull");
    private HwSysResource mAppResource;
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses = new ProcessMap<>();
    private final Context mContext;
    private HwCustAppErrors mHwCustAppErrors = ((HwCustAppErrors) HwCustUtils.createObj(HwCustAppErrors.class, new Object[0]));
    private final boolean mIsNotShowAnrDialog = SystemProperties.getBoolean("ro.config.noshow_anrdialog", false);
    private final boolean mIswhitelist_for_short_time = SystemProperties.getBoolean("persist.sys.hwgmstemporary", false);
    private final ProcessMap<Long> mProcessCrashTimes = new ProcessMap<>();
    private final ProcessMap<Long> mProcessCrashTimesPersistent = new ProcessMap<>();
    private final ActivityManagerService mService;

    static final class BadProcessInfo {
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

    static {
        boolean z = true;
        if (System.getenv("MAPLE_RUNTIME") == null) {
            z = false;
        }
        sIsMygote = z;
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
        if (this.mAppEyeANR != null) {
            this.mAppEyeANR.init(null);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId, String dumpPackage) {
        SparseArray<BadProcessInfo> uids;
        String pname;
        ArrayMap<String, SparseArray<BadProcessInfo>> pmap;
        long token;
        String pname2;
        int procCount;
        ArrayMap<String, SparseArray<Long>> pmap2;
        int uidCount;
        ProtoOutputStream protoOutputStream = proto;
        String str = dumpPackage;
        if (!this.mProcessCrashTimes.getMap().isEmpty() || !this.mBadProcesses.getMap().isEmpty()) {
            long token2 = proto.start(fieldId);
            long now = SystemClock.uptimeMillis();
            protoOutputStream.write(1112396529665L, now);
            long j = 1138166333441L;
            long j2 = 2246267895810L;
            if (!this.mProcessCrashTimes.getMap().isEmpty()) {
                ArrayMap<String, SparseArray<Long>> pmap3 = this.mProcessCrashTimes.getMap();
                int procCount2 = pmap3.size();
                int ip = 0;
                while (ip < procCount2) {
                    long ctoken = protoOutputStream.start(j2);
                    String pname3 = pmap3.keyAt(ip);
                    SparseArray<Long> uids2 = pmap3.valueAt(ip);
                    long now2 = now;
                    int uidCount2 = uids2.size();
                    protoOutputStream.write(j, pname3);
                    int i = 0;
                    while (i < uidCount2) {
                        int puid = uids2.keyAt(i);
                        ProcessRecord r = (ProcessRecord) this.mService.mProcessNames.get(pname3, puid);
                        if (str != null) {
                            if (r != null) {
                                uidCount = uidCount2;
                                if (r.pkgList.containsKey(str) == 0) {
                                    token = token2;
                                    pmap2 = pmap3;
                                    procCount = procCount2;
                                    pname2 = pname3;
                                }
                            } else {
                                uidCount = uidCount2;
                                token = token2;
                                pmap2 = pmap3;
                                procCount = procCount2;
                                pname2 = pname3;
                            }
                            i++;
                            uidCount2 = uidCount;
                            pmap3 = pmap2;
                            procCount2 = procCount;
                            pname3 = pname2;
                            token2 = token;
                        } else {
                            uidCount = uidCount2;
                        }
                        pmap2 = pmap3;
                        procCount = procCount2;
                        ProcessRecord processRecord = r;
                        pname2 = pname3;
                        long etoken = protoOutputStream.start(2246267895810L);
                        protoOutputStream.write(1120986464257L, puid);
                        token = token2;
                        protoOutputStream.write(1112396529666L, uids2.valueAt(i).longValue());
                        protoOutputStream.end(etoken);
                        i++;
                        uidCount2 = uidCount;
                        pmap3 = pmap2;
                        procCount2 = procCount;
                        pname3 = pname2;
                        token2 = token;
                    }
                    int i2 = uidCount2;
                    ArrayMap<String, SparseArray<Long>> arrayMap = pmap3;
                    int i3 = procCount2;
                    String str2 = pname3;
                    protoOutputStream.end(ctoken);
                    ip++;
                    now = now2;
                    j = 1138166333441L;
                    j2 = 2246267895810L;
                }
            }
            long token3 = token2;
            long j3 = now;
            if (!this.mBadProcesses.getMap().isEmpty()) {
                ArrayMap<String, SparseArray<BadProcessInfo>> pmap4 = this.mBadProcesses.getMap();
                int processCount = pmap4.size();
                int ip2 = 0;
                while (ip2 < processCount) {
                    long btoken = protoOutputStream.start(2246267895811L);
                    String pname4 = pmap4.keyAt(ip2);
                    SparseArray<BadProcessInfo> uids3 = pmap4.valueAt(ip2);
                    int uidCount3 = uids3.size();
                    protoOutputStream.write(1138166333441L, pname4);
                    int i4 = 0;
                    while (i4 < uidCount3) {
                        int puid2 = uids3.keyAt(i4);
                        ProcessRecord r2 = (ProcessRecord) this.mService.mProcessNames.get(pname4, puid2);
                        if (str == null || (r2 != null && r2.pkgList.containsKey(str))) {
                            BadProcessInfo info = uids3.valueAt(i4);
                            pmap = pmap4;
                            pname = pname4;
                            uids = uids3;
                            long etoken2 = protoOutputStream.start(2246267895810L);
                            protoOutputStream.write(1120986464257L, puid2);
                            int i5 = puid2;
                            ProcessRecord processRecord2 = r2;
                            protoOutputStream.write(1112396529666L, info.time);
                            protoOutputStream.write(1138166333443L, info.shortMsg);
                            protoOutputStream.write(1138166333444L, info.longMsg);
                            protoOutputStream.write(1138166333445L, info.stack);
                            protoOutputStream.end(etoken2);
                        } else {
                            pmap = pmap4;
                            pname = pname4;
                            uids = uids3;
                        }
                        i4++;
                        pmap4 = pmap;
                        pname4 = pname;
                        uids3 = uids;
                        str = dumpPackage;
                    }
                    String str3 = pname4;
                    SparseArray<BadProcessInfo> sparseArray = uids3;
                    protoOutputStream.end(btoken);
                    ip2++;
                    str = dumpPackage;
                }
            }
            protoOutputStream.end(token3);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0055, code lost:
        if (r4.pkgList.containsKey(r2) == false) goto L_0x005a;
     */
    public boolean dumpLocked(FileDescriptor fd, PrintWriter pw, boolean needSep, String dumpPackage) {
        boolean needSep2;
        int processCount;
        ArrayMap<String, SparseArray<BadProcessInfo>> pmap;
        int processCount2;
        ArrayMap<String, SparseArray<Long>> pmap2;
        AppErrors appErrors = this;
        PrintWriter printWriter = pw;
        String str = dumpPackage;
        if (!appErrors.mProcessCrashTimes.getMap().isEmpty()) {
            long now = SystemClock.uptimeMillis();
            ArrayMap<String, SparseArray<Long>> pmap3 = appErrors.mProcessCrashTimes.getMap();
            int processCount3 = pmap3.size();
            needSep2 = needSep;
            boolean printed = false;
            int ip = 0;
            while (ip < processCount3) {
                String pname = pmap3.keyAt(ip);
                SparseArray<Long> uids = pmap3.valueAt(ip);
                int uidCount = uids.size();
                boolean printed2 = printed;
                int i = 0;
                while (i < uidCount) {
                    int puid = uids.keyAt(i);
                    ProcessRecord r = (ProcessRecord) appErrors.mService.mProcessNames.get(pname, puid);
                    if (str != null) {
                        if (r != null) {
                            pmap2 = pmap3;
                        } else {
                            pmap2 = pmap3;
                        }
                        processCount2 = processCount3;
                        i++;
                        pmap3 = pmap2;
                        processCount3 = processCount2;
                    } else {
                        pmap2 = pmap3;
                    }
                    if (!printed2) {
                        if (needSep2) {
                            pw.println();
                        }
                        needSep2 = true;
                        printWriter.println("  Time since processes crashed:");
                        printed2 = true;
                    }
                    printWriter.print("    Process ");
                    printWriter.print(pname);
                    printWriter.print(" uid ");
                    printWriter.print(puid);
                    printWriter.print(": last crashed ");
                    processCount2 = processCount3;
                    TimeUtils.formatDuration(now - uids.valueAt(i).longValue(), printWriter);
                    printWriter.println(" ago");
                    i++;
                    pmap3 = pmap2;
                    processCount3 = processCount2;
                }
                int i2 = processCount3;
                ip++;
                printed = printed2;
            }
        } else {
            needSep2 = needSep;
        }
        if (!appErrors.mBadProcesses.getMap().isEmpty()) {
            ArrayMap<String, SparseArray<BadProcessInfo>> pmap4 = appErrors.mBadProcesses.getMap();
            int processCount4 = pmap4.size();
            boolean printed3 = false;
            int ip2 = 0;
            while (ip2 < processCount4) {
                String pname2 = pmap4.keyAt(ip2);
                SparseArray<BadProcessInfo> uids2 = pmap4.valueAt(ip2);
                int uidCount2 = uids2.size();
                boolean printed4 = printed3;
                int i3 = 0;
                while (i3 < uidCount2) {
                    int puid2 = uids2.keyAt(i3);
                    ProcessRecord r2 = (ProcessRecord) appErrors.mService.mProcessNames.get(pname2, puid2);
                    if (str == null || (r2 != null && r2.pkgList.containsKey(str))) {
                        if (!printed4) {
                            if (needSep2) {
                                pw.println();
                            }
                            needSep2 = true;
                            printWriter.println("  Bad processes:");
                            printed4 = true;
                        }
                        BadProcessInfo info = uids2.valueAt(i3);
                        printWriter.print("    Bad process ");
                        printWriter.print(pname2);
                        printWriter.print(" uid ");
                        printWriter.print(puid2);
                        printWriter.print(": crashed at time ");
                        pmap = pmap4;
                        processCount = processCount4;
                        printWriter.println(info.time);
                        if (info.shortMsg != null) {
                            printWriter.print("      Short msg: ");
                            printWriter.println(info.shortMsg);
                        }
                        if (info.longMsg != null) {
                            printWriter.print("      Long msg: ");
                            printWriter.println(info.longMsg);
                        }
                        if (info.stack != null) {
                            printWriter.println("      Stack:");
                            int lastPos = 0;
                            int pos = 0;
                            while (pos < info.stack.length()) {
                                if (info.stack.charAt(pos) == 10) {
                                    printWriter.print("        ");
                                    printWriter.write(info.stack, lastPos, pos - lastPos);
                                    pw.println();
                                    lastPos = pos + 1;
                                }
                                pos++;
                            }
                            if (lastPos < info.stack.length()) {
                                printWriter.print("        ");
                                printWriter.write(info.stack, lastPos, info.stack.length() - lastPos);
                                pw.println();
                            }
                        }
                    } else {
                        pmap = pmap4;
                        processCount = processCount4;
                    }
                    i3++;
                    pmap4 = pmap;
                    processCount4 = processCount;
                    appErrors = this;
                }
                int i4 = processCount4;
                ip2++;
                printed3 = printed4;
                appErrors = this;
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

    /* access modifiers changed from: package-private */
    public void scheduleAppCrashLocked(int uid, int initialPid, String packageName, int userId, String message) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01e8, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x01eb, code lost:
        if (r1 == null) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        r11.mContext.startActivityAsUser(r1, new android.os.UserHandle(r12.userId));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x01fa, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x01fb, code lost:
        android.util.Slog.w("ActivityManager", "bug report receiver dissappeared", r0);
     */
    public void crashApplicationInner(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo, int callingPid, int callingUid) {
        ActivityManagerService activityManagerService;
        long orig;
        ProcessRecord processRecord = r;
        ApplicationErrorReport.CrashInfo crashInfo2 = crashInfo;
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo2.exceptionClassName;
        String longMsg = crashInfo2.exceptionMessage;
        String stackTrace = crashInfo2.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        String longMsg2 = longMsg;
        if (processRecord != null && processRecord.persistent) {
            RescueParty.notePersistentAppCrash(this.mContext, processRecord.uid);
        }
        AppErrorResult result = new AppErrorResult();
        ActivityManagerService activityManagerService2 = this.mService;
        synchronized (activityManagerService2) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ApplicationErrorReport.CrashInfo crashInfo3 = crashInfo2;
                AppErrorResult result2 = result;
                activityManagerService = activityManagerService2;
                String stackTrace2 = stackTrace;
                String shortMsg2 = shortMsg;
                try {
                    if (handleAppCrashInActivityController(processRecord, crashInfo3, shortMsg, longMsg2, stackTrace, timeMillis, callingPid, callingUid)) {
                        try {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            th = th;
                            AppErrorResult appErrorResult = result2;
                            ApplicationErrorReport.CrashInfo crashInfo4 = crashInfo;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        if (processRecord != null) {
                            if (processRecord.instr != null) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        }
                        if (processRecord != null) {
                            this.mService.mBatteryStatsService.noteProcessCrash(processRecord.processName, processRecord.uid);
                        }
                        AppErrorDialog.Data data = new AppErrorDialog.Data();
                        data.result = result2;
                        data.proc = processRecord;
                        if (processRecord == null) {
                            ApplicationErrorReport.CrashInfo crashInfo5 = crashInfo;
                        } else if (!makeAppCrashingLocked(processRecord, shortMsg2, longMsg2, stackTrace2, data)) {
                            AppErrorResult appErrorResult2 = result2;
                            ApplicationErrorReport.CrashInfo crashInfo6 = crashInfo;
                        } else {
                            Message msg = Message.obtain();
                            msg.what = 1;
                            TaskRecord task = data.task;
                            msg.obj = data;
                            this.mService.mUiHandler.sendMessage(msg);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            int res = result2.get();
                            Intent appErrorIntent = null;
                            MetricsLogger.action(this.mContext, 316, res);
                            if (res == 6 || res == 7) {
                                res = 1;
                            }
                            int res2 = res;
                            synchronized (this.mService) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    if (res2 == 5) {
                                        try {
                                            stopReportingCrashesLocked(r);
                                        } catch (IllegalArgumentException e) {
                                            Set<String> cats = task.intent != null ? task.intent.getCategories() : null;
                                            if (cats != null && cats.contains("android.intent.category.LAUNCHER")) {
                                                IllegalArgumentException illegalArgumentException = e;
                                                this.mService.getActivityStartController().startActivityInPackage(task.mCallingUid, callingPid, callingUid, task.mCallingPackage, task.intent, null, null, null, 0, 0, new SafeActivityOptions(ActivityOptions.makeBasic()), task.userId, null, "AppErrors", false);
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            AppErrorResult appErrorResult3 = result2;
                                            ApplicationErrorReport.CrashInfo crashInfo7 = crashInfo;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    }
                                    if (res2 == 3) {
                                        this.mService.removeProcessLocked(processRecord, false, true, "crash");
                                        ActivityOptions options = ActivityOptions.makeBasic();
                                        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(processRecord.mDisplayId)) {
                                            options.setLaunchDisplayId(processRecord.mDisplayId);
                                        }
                                        if (task != null) {
                                            this.mService.startActivityFromRecents(task.taskId, options.toBundle());
                                        }
                                    }
                                    if (res2 == 1) {
                                        orig = Binder.clearCallingIdentity();
                                        this.mService.mStackSupervisor.handleAppCrashLocked(processRecord);
                                        if (!processRecord.persistent) {
                                            this.mService.removeProcessLocked(processRecord, false, false, "crash");
                                            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                                        }
                                        Binder.restoreCallingIdentity(orig);
                                    }
                                    if (res2 == 8) {
                                        appErrorIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        appErrorIntent.setData(Uri.parse("package:" + processRecord.info.packageName));
                                        appErrorIntent.addFlags(268435456);
                                    }
                                    if (res2 == 2) {
                                        AppErrorResult appErrorResult4 = result2;
                                        try {
                                            appErrorIntent = createAppErrorIntentLocked(processRecord, timeMillis, crashInfo);
                                        } catch (Throwable th3) {
                                            th = th3;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } else {
                                        ApplicationErrorReport.CrashInfo crashInfo8 = crashInfo;
                                    }
                                    if (!(processRecord == null || processRecord.isolated || res2 == 3)) {
                                        this.mProcessCrashTimes.put(processRecord.info.processName, processRecord.uid, Long.valueOf(SystemClock.uptimeMillis()));
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    AppErrorResult appErrorResult5 = result2;
                                    ApplicationErrorReport.CrashInfo crashInfo9 = crashInfo;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                        }
                        try {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th5) {
                            th = th5;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    AppErrorResult appErrorResult6 = result2;
                    ApplicationErrorReport.CrashInfo crashInfo10 = crashInfo;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th7) {
                th = th7;
                AppErrorResult appErrorResult7 = result;
                activityManagerService = activityManagerService2;
                String str = stackTrace;
                String str2 = shortMsg;
                ApplicationErrorReport.CrashInfo crashInfo11 = crashInfo2;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis, int callingPid, int callingUid) {
        String name;
        ProcessRecord processRecord = r;
        ApplicationErrorReport.CrashInfo crashInfo2 = crashInfo;
        if (this.mService.mController == null) {
            return false;
        }
        if (processRecord != null) {
            try {
                name = processRecord.processName;
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        } else {
            name = null;
        }
        int pid = processRecord != null ? processRecord.pid : callingPid;
        int uid = processRecord != null ? processRecord.info.uid : callingUid;
        if (!this.mService.mController.appCrashed(name, pid, shortMsg, longMsg, timeMillis, crashInfo2.stackTrace)) {
            if (!"1".equals(SystemProperties.get("ro.debuggable", "0")) || !"Native crash".equals(crashInfo2.exceptionClassName)) {
                Slog.w("ActivityManager", "Force-killing crashed app " + name + " at watcher's request");
                if (processRecord != null) {
                    int i = uid;
                    int i2 = pid;
                    if (!makeAppCrashingLocked(processRecord, shortMsg, longMsg, stackTrace, null)) {
                        processRecord.kill("crash", true);
                    }
                } else {
                    int pid2 = pid;
                    Process.killProcess(pid2);
                    ActivityManagerService.killProcessGroup(uid, pid2);
                }
            } else {
                Slog.w("ActivityManager", "Skip killing native crashed app " + name + "(" + pid + ") during testing");
                int i3 = uid;
                int i4 = pid;
            }
            return true;
        }
        return false;
    }

    private boolean makeAppCrashingLocked(ProcessRecord app, String shortMsg, String longMsg, String stackTrace, AppErrorDialog.Data data) {
        app.crashing = true;
        ProcessRecord processRecord = app;
        app.crashingReport = generateProcessError(processRecord, 1, null, shortMsg, longMsg, stackTrace);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
        return handleAppCrashLocked(processRecord, "force-crash", shortMsg, longMsg, stackTrace, data);
    }

    /* access modifiers changed from: package-private */
    public void startAppProblemLocked(ProcessRecord app) {
        app.errorReportReceiver = null;
        for (int userId : this.mService.mUserController.getCurrentProfileIds()) {
            if (app.userId == userId) {
                app.errorReportReceiver = ApplicationErrorReport.getErrorReportReceiver(this.mContext, app.info.packageName, app.info.flags);
            }
        }
        this.mService.skipCurrentReceiverLocked(app);
    }

    private ActivityManager.ProcessErrorStateInfo generateProcessError(ProcessRecord app, int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
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
        if (!r.crashing && !r.notResponding && !r.forceCrashReport) {
            return null;
        }
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.packageName = r.info.packageName;
        report.installerPackageName = r.errorReportReceiver.getPackageName();
        report.processName = r.processName;
        report.time = timeMillis;
        report.systemApp = (r.info.flags & 1) != 0;
        if (r.crashing || r.forceCrashReport) {
            report.type = 1;
            report.crashInfo = crashInfo;
        } else if (r.notResponding) {
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
        Long crashTime;
        Long crashTimePersistent;
        long now;
        boolean tryAgain;
        ProcessRecord processRecord = app;
        AppErrorDialog.Data data2 = data;
        long now2 = SystemClock.uptimeMillis();
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        boolean procIsBoundForeground = processRecord.curProcState == 4;
        if (!processRecord.isolated) {
            crashTime = (Long) this.mProcessCrashTimes.get(processRecord.info.processName, processRecord.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(processRecord.info.processName, processRecord.uid);
        } else {
            crashTime = null;
            crashTimePersistent = null;
        }
        Long crashTimePersistent2 = crashTimePersistent;
        Long crashTime2 = crashTime;
        int i = processRecord.services.size() - 1;
        boolean tryAgain2 = false;
        while (true) {
            int i2 = i;
            if (i2 < 0) {
                break;
            }
            ServiceRecord sr = processRecord.services.valueAt(i2);
            if (now2 > sr.restartTime + 60000) {
                sr.crashCount = 1;
            } else {
                sr.crashCount++;
            }
            if (((long) sr.crashCount) < this.mService.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY && (sr.isForeground || procIsBoundForeground)) {
                tryAgain2 = true;
            }
            i = i2 - 1;
        }
        if (crashTime2 == null || now2 >= crashTime2.longValue() + 60000) {
            now = now2;
            boolean z = procIsBoundForeground;
            tryAgain = tryAgain2;
            Long crashTimePersistent3 = crashTimePersistent2;
            TaskRecord affectedTask = this.mService.mStackSupervisor.finishTopCrashedActivitiesLocked(processRecord, reason);
            if (data2 != null) {
                data2.task = affectedTask;
            }
            if (!(data2 == null || crashTimePersistent3 == null || now >= crashTimePersistent3.longValue() + 60000)) {
                data2.repeating = true;
                if (sIsMygote) {
                    rescueMapleProcess(app);
                }
            }
        } else {
            Slog.w("ActivityManager", "Process " + processRecord.info.processName + " has crashed too many times: killing!");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, new Object[]{Integer.valueOf(processRecord.userId), processRecord.info.processName, Integer.valueOf(processRecord.uid)});
            this.mService.mStackSupervisor.handleAppCrashLocked(processRecord);
            if (!processRecord.persistent) {
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, new Object[]{Integer.valueOf(processRecord.userId), Integer.valueOf(processRecord.uid), processRecord.info.processName});
                if (!processRecord.isolated) {
                    ProcessMap<BadProcessInfo> processMap = this.mBadProcesses;
                    String str = processRecord.info.processName;
                    int i3 = processRecord.uid;
                    boolean z2 = procIsBoundForeground;
                    BadProcessInfo badProcessInfo = r4;
                    long j = now2;
                    now = now2;
                    tryAgain = tryAgain2;
                    Long l = crashTime2;
                    Long crashTime3 = crashTimePersistent2;
                    BadProcessInfo badProcessInfo2 = new BadProcessInfo(j, shortMsg, longMsg, stackTrace);
                    processMap.put(str, i3, badProcessInfo);
                    this.mProcessCrashTimes.remove(processRecord.info.processName, processRecord.uid);
                } else {
                    now = now2;
                    boolean z3 = procIsBoundForeground;
                    tryAgain = tryAgain2;
                    Long l2 = crashTimePersistent2;
                }
                processRecord.bad = true;
                processRecord.removed = true;
                this.mService.removeProcessLocked(processRecord, false, tryAgain, "crash");
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                if (!showBackground) {
                    return false;
                }
            } else {
                now = now2;
                boolean z4 = procIsBoundForeground;
                tryAgain = tryAgain2;
                Long l3 = crashTimePersistent2;
            }
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            if (sIsMygote) {
                rescueMapleProcess(app);
            }
            String str2 = reason;
        }
        if (data2 != null && tryAgain) {
            data2.isRestartableForService = true;
        }
        ArrayList<ActivityRecord> activities = processRecord.activities;
        if (processRecord == this.mService.mHomeProcess && activities.size() > 0 && (this.mService.mHomeProcess.info.flags & 1) == 0) {
            int activityNdx = activities.size() - 1;
            while (true) {
                int activityNdx2 = activityNdx;
                if (activityNdx2 < 0) {
                    break;
                }
                ActivityRecord r = activities.get(activityNdx2);
                if (r.isActivityTypeHome()) {
                    Log.i("ActivityManager", "Clearing package preferred activities from " + r.packageName);
                    try {
                        ActivityThread.getPackageManager().clearPackagePreferredActivities(r.packageName);
                    } catch (RemoteException e) {
                    }
                    this.mService.showUninstallLauncherDialog(r.packageName);
                }
                activityNdx = activityNdx2 - 1;
            }
        }
        if (processRecord.isolated == 0) {
            long now3 = now;
            this.mProcessCrashTimes.put(processRecord.info.processName, processRecord.uid, Long.valueOf(now3));
            this.mProcessCrashTimesPersistent.put(processRecord.info.processName, processRecord.uid, Long.valueOf(now3));
        }
        if (processRecord.crashHandler != null) {
            this.mService.mHandler.post(processRecord.crashHandler);
        }
        return true;
    }

    private boolean hasForegroundUI(ProcessRecord proc) {
        boolean hasForegroundUI = proc != null && proc.foregroundActivities;
        if (hasForegroundUI || proc == null) {
            return hasForegroundUI;
        }
        String packageName = proc.info.packageName;
        List<ActivityManager.RunningTaskInfo> taskInfo = this.mService.getTasks(1);
        if (taskInfo == null || taskInfo.size() <= 0) {
            return hasForegroundUI;
        }
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo == null || packageName == null || !packageName.equalsIgnoreCase(componentInfo.getPackageName())) {
            return hasForegroundUI;
        }
        return true;
    }

    private void rescueMapleProcess(ProcessRecord app) {
        if ((app.info.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
            try {
                IHwPackageManager hwPM = HwPackageManager.getService();
                if (hwPM != null && hwPM.getMapleEnableFlag(app.info.packageName)) {
                    for (String pkg : app.pkgList.keySet()) {
                        hwPM.setMapleEnableFlag(pkg, false);
                        Slog.w("ActivityManager", "set maple runtime to art for the app:" + pkg);
                    }
                }
            } catch (RemoteException e) {
                Slog.e("ActivityManager", "cannot get PackageManager.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x02a1, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
        r0 = r10;
        r2 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x02a6, code lost:
        if (r7 == null) goto L_0x02c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x02a8, code lost:
        android.util.Slog.i("ActivityManager", "Showing crash dialog for package " + r0 + " u" + r2);
        r7.show();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x02c9, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0073, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0076, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ae, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b1, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x010c, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x010f, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0185 A[Catch:{ all -> 0x0035 }] */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x01c3 A[Catch:{ all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x0213 A[Catch:{ all -> 0x02d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x023e A[Catch:{ all -> 0x02d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0299 A[Catch:{ all -> 0x02d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00d3 A[SYNTHETIC, Splitter:B:63:0x00d3] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x011f A[Catch:{ all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0121 A[Catch:{ all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0139 A[Catch:{ all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x013b A[Catch:{ all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0140 A[SYNTHETIC, Splitter:B:88:0x0140] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0153 A[SYNTHETIC, Splitter:B:96:0x0153] */
    public void handleShowAppErrorUi(Message msg) {
        boolean isBackground;
        boolean isBackground2;
        boolean showFirstCrash;
        boolean showFirstCrashDevOption;
        boolean crashSilenced;
        boolean isShowCrash;
        int processType;
        AppErrorDialog.Data data = (AppErrorDialog.Data) msg.obj;
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        AppErrorDialog dialogToShow = null;
        synchronized (this.mService) {
            ActivityManagerService.boostPriorityForLockedSection();
            ProcessRecord proc = data.proc;
            AppErrorResult res = data.result;
            if (proc == null) {
                try {
                    Slog.e("ActivityManager", "handleShowAppErrorUi: proc is null");
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    th = th;
                    boolean z = showBackground;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } else {
                String packageName = proc.info.packageName;
                boolean isDebuggable = "1".equals(SystemProperties.get("ro.debuggable", "0"));
                if (!isDebuggable && proc != null) {
                    if (proc.forceCrashReport) {
                        Slog.w("ActivityManager", "Skipping native crash dialog of " + proc);
                        if (res != null) {
                            res.set(AppErrorDialog.CANT_SHOW);
                        }
                    }
                }
                if (packageName != null) {
                    if (this.mIswhitelist_for_short_time && whitelist_for_short_time.contains(packageName)) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                try {
                    int userId = proc.userId;
                    if (proc.crashDialog != null) {
                        Slog.e("ActivityManager", "App already has crash dialog: " + proc);
                        if (res != null) {
                            res.set(AppErrorDialog.ALREADY_SHOWING);
                        }
                    } else {
                        if (UserHandle.getAppId(proc.uid) >= 10000) {
                            if (proc.pid != ActivityManagerService.MY_PID) {
                                isBackground = true;
                                isBackground2 = isBackground;
                                for (int i : this.mService.mUserController.getCurrentProfileIds()) {
                                    isBackground2 &= userId != i;
                                }
                                if (isBackground2 || showBackground) {
                                    showFirstCrash = Settings.Global.getInt(this.mContext.getContentResolver(), "show_first_crash_dialog", 0) == 0;
                                    showFirstCrashDevOption = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", 0, this.mService.mUserController.getCurrentUserId()) == 0;
                                    if (this.mAppsNotReportingCrashes != null) {
                                        if (this.mAppsNotReportingCrashes.contains(proc.info.packageName)) {
                                            crashSilenced = true;
                                            if (this.mHwCustAppErrors != null) {
                                                if (this.mHwCustAppErrors.isCustom()) {
                                                    isShowCrash = (this.mService.canShowErrorDialogs() || showBackground) && !crashSilenced && (showFirstCrash || showFirstCrashDevOption || data.repeating) && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && hasForegroundUI(proc);
                                                    if (this.mAppResource == null) {
                                                        Slog.i("ActivityManager", "get AppResource");
                                                        this.mAppResource = HwFrameworkFactory.getHwResource(18);
                                                    }
                                                    if (this.mAppResource != null || !this.mService.canShowErrorDialogs() || crashSilenced || HwFrameworkFactory.getVRSystemServiceManager().isVRMode() || !hasForegroundUI(proc)) {
                                                        boolean z2 = showBackground;
                                                    } else {
                                                        if ((proc.info.flags & 1) != 0) {
                                                            if ((proc.info.hwFlags & DumpState.DUMP_HANDLE) == 0 && (proc.info.hwFlags & 67108864) == 0) {
                                                                processType = 2;
                                                                boolean z3 = showFirstCrash;
                                                                boolean z4 = showBackground;
                                                                if (2 == this.mAppResource.acquire(proc.uid, proc.info.packageName, processType)) {
                                                                    Slog.w("ActivityManager", "Failed to acquire AppResource:" + proc.info.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + proc.uid);
                                                                }
                                                            }
                                                        }
                                                        processType = 0;
                                                        boolean z32 = showFirstCrash;
                                                        boolean z42 = showBackground;
                                                        try {
                                                            if (2 == this.mAppResource.acquire(proc.uid, proc.info.packageName, processType)) {
                                                            }
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                                            throw th;
                                                        }
                                                    }
                                                    if (!isShowCrash) {
                                                        Context context = this.mContext;
                                                        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(proc.mDisplayId)) {
                                                            Context tmpContext = HwPCUtils.getDisplayContext(context, proc.mDisplayId);
                                                            if (tmpContext != null) {
                                                                context = tmpContext;
                                                            }
                                                        }
                                                        AppErrorDialog appErrorDialog = new AppErrorDialog(context, this.mService, data);
                                                        dialogToShow = appErrorDialog;
                                                        proc.crashDialog = appErrorDialog;
                                                        if (this.mAppEyeANR != null) {
                                                            ZrHungData arg = new ZrHungData();
                                                            arg.putString("WpName", "APP_CRASH");
                                                            arg.putString("packageName", proc.info.packageName);
                                                            arg.putString(IZRHungService.PARA_PROCNAME, proc.processName);
                                                            arg.putInt(IZRHungService.PARAM_PID, proc.pid);
                                                            arg.putBoolean("isRepeating", data.repeating);
                                                            this.mAppEyeANR.sendEvent(arg);
                                                        }
                                                    } else if (res != null) {
                                                        res.set(AppErrorDialog.CANT_SHOW);
                                                    }
                                                }
                                            }
                                            isShowCrash = !isDebuggable && (this.mService.canShowErrorDialogs() || showBackground) && !crashSilenced && ((showFirstCrash || showFirstCrashDevOption || data.repeating) && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && hasForegroundUI(proc));
                                            if (this.mAppResource == null) {
                                            }
                                            if (this.mAppResource != null) {
                                            }
                                            boolean z22 = showBackground;
                                            if (!isShowCrash) {
                                            }
                                        }
                                    }
                                    crashSilenced = false;
                                    if (this.mHwCustAppErrors != null) {
                                    }
                                    if (!isDebuggable) {
                                    }
                                    if (this.mAppResource == null) {
                                    }
                                    if (this.mAppResource != null) {
                                    }
                                    boolean z222 = showBackground;
                                    if (!isShowCrash) {
                                    }
                                } else {
                                    Slog.w("ActivityManager", "Skipping crash dialog of " + proc + ": background");
                                    if (res != null) {
                                        res.set(AppErrorDialog.BACKGROUND_USER);
                                    }
                                }
                            }
                        }
                        isBackground = false;
                        isBackground2 = isBackground;
                        while (r13 < r15) {
                        }
                        if (isBackground2) {
                        }
                        if (Settings.Global.getInt(this.mContext.getContentResolver(), "show_first_crash_dialog", 0) == 0) {
                        }
                        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", 0, this.mService.mUserController.getCurrentUserId()) == 0) {
                        }
                        if (this.mAppsNotReportingCrashes != null) {
                        }
                        crashSilenced = false;
                        if (this.mHwCustAppErrors != null) {
                        }
                        if (!isDebuggable) {
                        }
                        if (this.mAppResource == null) {
                        }
                        if (this.mAppResource != null) {
                        }
                        boolean z2222 = showBackground;
                        if (!isShowCrash) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    boolean z5 = showBackground;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet<>();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    static boolean isInterestingForBackgroundTraces(ProcessRecord app) {
        boolean z = true;
        if (app.pid == ActivityManagerService.MY_PID) {
            return true;
        }
        if (!app.isInterestingToUserLocked() && ((app.info == null || !"com.android.systemui".equals(app.info.packageName)) && !app.hasTopUi && !app.hasOverlayUi)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public final void appNotResponding(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        appNotResponding(app.pid, app, activity, parent, aboveSystem, annotation);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02cc, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
        r11 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x0484, code lost:
        r11.append(r2.printCurrentLoad());
        r11.append(r6);
        r5 = r2.printCurrentState(r7);
        r11.append(r5);
        android.util.Slog.e("ActivityManager", r11.toString());
        android.util.Slog.e("ActivityManager", r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x04a3, code lost:
        if (r24 != null) goto L_0x04ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x04a5, code lost:
        android.os.Process.sendSignal(r13.pid, 3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x04ab, code lost:
        r4 = r13.uid;
        r0 = r13.processName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x04af, code lost:
        if (r14 != null) goto L_0x04b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x04b1, code lost:
        r26 = com.android.server.UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x04b5, code lost:
        r26 = r14.shortComponentName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x04bb, code lost:
        if (r13.info == null) goto L_0x04cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x04c3, code lost:
        if (r13.info.isInstantApp() == false) goto L_0x04c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:0x04c5, code lost:
        r27 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x04c9, code lost:
        r27 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x04cd, code lost:
        r27 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x04d0, code lost:
        if (r13 == null) goto L_0x04e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x04d6, code lost:
        if (r43.isInterestingToUserLocked() == false) goto L_0x04dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x04d8, code lost:
        r18 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x04dc, code lost:
        r18 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x04e1, code lost:
        r28 = r5;
        r31 = r7;
        r26 = r9;
        android.util.StatsLog.write(79, r4, r0, r26, r12, r27, r18);
        r18 = r10;
        r33 = r2;
        r2 = r11;
        r1.mService.addErrorToDropBox("anr", r13, r13.processName, r14, r15, r12, r6, r24, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0514, code lost:
        if (r1.mService.mController == null) goto L_0x0563;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:?, code lost:
        r3 = r1.mService.mController.appNotResponding(r13.processName, r13.pid, r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x0527, code lost:
        if (r3 == 0) goto L_0x0553;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x0529, code lost:
        if (r3 >= 0) goto L_0x0538;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x052f, code lost:
        if (r13.pid == com.android.server.am.ActivityManagerService.MY_PID) goto L_0x0538;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x0533, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:?, code lost:
        r13.kill("anr", true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0538, code lost:
        r4 = true;
        r5 = r1.mService;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x053b, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:?, code lost:
        com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
        r1.mService.mServices.scheduleServiceTimeoutLocked(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x0546, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:?, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x054a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x054b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:?, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x0550, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x0553, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x0556, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x0557, code lost:
        r1.mService.mController = null;
        com.android.server.Watchdog.getInstance().setActivityController(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x0563, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x0566, code lost:
        monitor-enter(r1.mService);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:?, code lost:
        com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
        r1.mService.mBatteryStatsService.noteProcessAnr(r13.processName, r13.uid);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x0575, code lost:
        if (r17 != false) goto L_0x058e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x0583, code lost:
        r13.kill("bg anr", r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0589, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x058c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x058e, code lost:
        if (r14 != null) goto L_0x0590;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:?, code lost:
        r11 = r14.shortComponentName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x0593, code lost:
        r11 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x0596, code lost:
        if (r47 != null) goto L_0x0598;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:?, code lost:
        r0 = "ANR " + r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x05aa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x05ab, code lost:
        r7 = r46;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x05ae, code lost:
        r0 = "ANR";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:279:0x05b0, code lost:
        makeAppNotRespondingLocked(r13, r11, r0, r2.toString());
        r0 = android.os.Message.obtain();
        r0.what = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:?, code lost:
        r0.obj = new com.android.server.am.AppNotRespondingDialog.Data(r13, r14, r46);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x05e5, code lost:
        if (r1.mService.zrHungSendEvent(com.android.server.zrhung.IZRHungService.EVENT_SHOWANRDIALOG, r13.pid, r13.uid, r13.info.packageName, null, com.android.server.zrhung.IZRHungService.TYPE_ORIGINAL) != false) goto L_0x05e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x05e7, code lost:
        r13.anrType = r4 ? 1 : 0;
        r1.mService.mUiHandler.sendMessage(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x05f1, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x05f4, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x05f5, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x05f6, code lost:
        r7 = r46;
        r6 = r47;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x05fb, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x05fe, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x05ff, code lost:
        r0 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:233:0x0534, B:237:0x053c] */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x03eb  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0412  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x041a  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x041d  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0425  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x044c  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x044f  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0452  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x0455  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x0478 A[SYNTHETIC, Splitter:B:196:0x0478] */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0567 A[SYNTHETIC, Splitter:B:257:0x0567] */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0648 A[SYNTHETIC, Splitter:B:308:0x0648] */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0650 A[Catch:{ IOException -> 0x064c }] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0245 A[SYNTHETIC, Splitter:B:70:0x0245] */
    public final void appNotResponding(int anrPid, ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        long anrTime;
        boolean isSilentANR;
        FileOutputStream sysrq_trigger_io_stream;
        OutputStreamWriter sysrq_trigger;
        Throwable th;
        int i;
        int[] pids;
        String cpuInfo;
        boolean isSilentANR2;
        long anrTime2;
        int i2 = anrPid;
        ProcessRecord processRecord = app;
        ActivityRecord activityRecord = activity;
        ActivityRecord activityRecord2 = parent;
        String str = annotation;
        if (IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put("checkType", "FocusWindowNullScene");
            params.put("anrActivityName", activityRecord != null ? activity.toString() : null);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().cancelCheckFreezeScreen(params);
            }
        }
        ArrayList<Integer> firstPids = new ArrayList<>(5);
        SparseArray<Boolean> lastPids = new SparseArray<>(20);
        String traceMark = "anr_event_sync: appPid=" + processRecord.pid + ", appName=" + processRecord.processName + ", category=" + str;
        Trace.traceBegin(64, traceMark);
        Trace.traceEnd(64);
        if (Log.HWINFO) {
            HwFrameworkFactory.getLogException().cmd(HwBroadcastRadarUtil.KEY_ACTION, "copy_systrace_to_cache");
        }
        if (this.mService.mController != null) {
            try {
                if (this.mService.mController.appEarlyNotResponding(processRecord.processName, processRecord.pid, str) < 0 && processRecord.pid != ActivityManagerService.MY_PID) {
                    processRecord.kill("anr", true);
                }
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        }
        long anrTime3 = SystemClock.uptimeMillis();
        this.mService.updateCpuStatsNow();
        boolean showBackground = Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        if (this.mService.mShuttingDown) {
            Slog.i("ActivityManager", "During shutdown skipping ANR: " + processRecord + " " + str);
        } else if (processRecord.notResponding) {
            Slog.i("ActivityManager", "Skipping duplicate ANR: " + processRecord + " " + str);
        } else if (processRecord.crashing) {
            Slog.i("ActivityManager", "Crashing app skipping ANR: " + processRecord + " " + str);
        } else if (processRecord.killedByAm) {
            Slog.i("ActivityManager", "App already killed by AM skipping ANR: " + processRecord + " " + str);
        } else {
            if (processRecord.killed) {
                Slog.i("ActivityManager", "Skipping died app ANR: " + processRecord + " " + str);
            } else if (i2 != processRecord.pid) {
                Slog.i("ActivityManager", "Skipping ANR because pid of " + processRecord.processName + " is changed: anr pid: " + i2 + ", new pid: " + processRecord.pid + " " + str);
                return;
            } else if (this.mService.handleANRFilterFIFO(processRecord.uid, 2)) {
                Slog.i("ActivityManager", "During holding skipping ANR: " + processRecord + " " + str + "uid = " + processRecord.uid);
                return;
            }
            processRecord.notResponding = true;
            EventLog.writeEvent(EventLogTags.AM_ANR, new Object[]{Integer.valueOf(processRecord.userId), Integer.valueOf(processRecord.pid), processRecord.processName, Integer.valueOf(processRecord.info.flags), str});
            firstPids.add(Integer.valueOf(processRecord.pid));
            if (this.mAppEyeANR == null || this.mAppEyeANR.check(null)) {
                anrTime = anrTime3;
                isSilentANR = false;
            } else {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (!showBackground) {
                        try {
                            if (!isInterestingForBackgroundTraces(app)) {
                                isSilentANR2 = true;
                                if (!isSilentANR2) {
                                    int parentPid = processRecord.pid;
                                    if (activityRecord2 != null) {
                                        if (activityRecord2.app != null && activityRecord2.app.pid > 0) {
                                            parentPid = activityRecord2.app.pid;
                                        }
                                    }
                                    try {
                                        if (parentPid != processRecord.pid) {
                                            firstPids.add(Integer.valueOf(parentPid));
                                        }
                                        if (ActivityManagerService.MY_PID != processRecord.pid) {
                                            if (ActivityManagerService.MY_PID != parentPid) {
                                                firstPids.add(Integer.valueOf(ActivityManagerService.MY_PID));
                                            }
                                        }
                                        int i3 = this.mService.mLruProcesses.size() - 1;
                                        while (i3 >= 0) {
                                            ProcessRecord r = this.mService.mLruProcesses.get(i3);
                                            if (!(r == null || r.thread == null)) {
                                                int pid = r.pid;
                                                if (pid > 0) {
                                                    anrTime2 = anrTime3;
                                                    try {
                                                        if (!(pid == processRecord.pid || pid == parentPid || pid == ActivityManagerService.MY_PID)) {
                                                            if (r.persistent) {
                                                                firstPids.add(Integer.valueOf(pid));
                                                            } else if (r.treatLikeActivity) {
                                                                firstPids.add(Integer.valueOf(pid));
                                                            } else {
                                                                lastPids.put(pid, Boolean.TRUE);
                                                            }
                                                        }
                                                        i3--;
                                                        anrTime3 = anrTime2;
                                                        int i4 = anrPid;
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th;
                                                    }
                                                }
                                            }
                                            anrTime2 = anrTime3;
                                            i3--;
                                            anrTime3 = anrTime2;
                                            int i42 = anrPid;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        long j = anrTime3;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                                anrTime = anrTime3;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            long j2 = anrTime3;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    isSilentANR2 = false;
                    if (!isSilentANR2) {
                    }
                    anrTime = anrTime3;
                }
            }
            boolean isSilentANR3 = isSilentANR;
            if (this.mAppEyeANR != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString(IZRHungService.PARA_PROCNAME, processRecord.processName);
                arg.putInt(IZRHungService.PARAM_PID, processRecord.pid);
                arg.putBoolean("isSilentANR", isSilentANR3);
                arg.putString("packageName", processRecord.info.packageName);
                arg.putString(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, str);
                if (activityRecord != null) {
                    arg.putString("activityName", activityRecord.shortComponentName);
                }
                this.mAppEyeANR.sendEvent(arg);
            }
            StringBuilder info = new StringBuilder();
            info.setLength(0);
            info.append("ANR in ");
            info.append(processRecord.processName);
            if (!(activityRecord == null || activityRecord.shortComponentName == null)) {
                info.append(" (");
                info.append(activityRecord.shortComponentName);
                info.append(")");
            }
            info.append("\n");
            info.append("PID: ");
            info.append(processRecord.pid);
            info.append("\n");
            if (str != null) {
                info.append("Reason: ");
                info.append(str);
                info.append("\n");
            }
            if (!(activityRecord2 == null || activityRecord2 == activityRecord)) {
                info.append("Parent: ");
                info.append(activityRecord2.shortComponentName);
                info.append("\n");
            }
            ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(true);
            OutputStreamWriter sysrq_trigger2 = null;
            FileOutputStream sysrq_trigger_io_stream2 = null;
            try {
                sysrq_trigger_io_stream2 = new FileOutputStream(new File("/proc/sysrq-trigger"));
                sysrq_trigger2 = new OutputStreamWriter(sysrq_trigger_io_stream2, "UTF-8");
                sysrq_trigger2.write("w");
                try {
                    sysrq_trigger2.close();
                    sysrq_trigger_io_stream2.close();
                } catch (IOException e2) {
                    Slog.e("ActivityManager", "Failed to write to /proc/sysrq-trigger");
                    OutputStreamWriter outputStreamWriter = sysrq_trigger2;
                    FileOutputStream fileOutputStream = sysrq_trigger_io_stream2;
                    String[] nativeProcs = null;
                    if (isSilentANR3) {
                    }
                    String[] nativeProcs2 = nativeProcs;
                    pids = nativeProcs2 == null ? null : Process.getPidsForCommands(nativeProcs2);
                    ArrayList<Integer> nativePids = null;
                    if (pids != null) {
                    }
                    int[] pids2 = pids;
                    ArrayList<Integer> nativePids2 = nativePids;
                    if (isSilentANR3) {
                    }
                    if (isSilentANR3) {
                    }
                    boolean z = isSilentANR3;
                    int[] iArr = pids2;
                    String[] strArr = nativeProcs2;
                    long anrTime4 = anrTime;
                    ProcessCpuTracker processCpuTracker2 = processCpuTracker;
                    String str2 = traceMark;
                    File tracesFile = ActivityManagerService.dumpStackTraces(processRecord, true, firstPids, r6, r0, nativePids2);
                    this.mService.updateCpuStatsNow();
                    synchronized (this.mService.mProcessCpuTracker) {
                    }
                }
            } catch (IOException e3) {
                Slog.e("ActivityManager", "Failed to write to /proc/sysrq-trigger");
                if (sysrq_trigger2 != null) {
                    try {
                        sysrq_trigger2.close();
                    } catch (IOException e4) {
                        Slog.e("ActivityManager", "Failed to write to /proc/sysrq-trigger");
                        OutputStreamWriter outputStreamWriter2 = sysrq_trigger2;
                        FileOutputStream fileOutputStream2 = sysrq_trigger_io_stream2;
                        String[] nativeProcs3 = null;
                        if (isSilentANR3) {
                        }
                        String[] nativeProcs22 = nativeProcs3;
                        pids = nativeProcs22 == null ? null : Process.getPidsForCommands(nativeProcs22);
                        ArrayList<Integer> nativePids3 = null;
                        if (pids != null) {
                        }
                        int[] pids22 = pids;
                        ArrayList<Integer> nativePids22 = nativePids3;
                        if (isSilentANR3) {
                        }
                        if (isSilentANR3) {
                        }
                        boolean z2 = isSilentANR3;
                        int[] iArr2 = pids22;
                        String[] strArr2 = nativeProcs22;
                        long anrTime42 = anrTime;
                        ProcessCpuTracker processCpuTracker22 = processCpuTracker;
                        String str22 = traceMark;
                        File tracesFile2 = ActivityManagerService.dumpStackTraces(processRecord, true, firstPids, r6, r0, nativePids22);
                        this.mService.updateCpuStatsNow();
                        synchronized (this.mService.mProcessCpuTracker) {
                        }
                    }
                }
                if (sysrq_trigger_io_stream2 != null) {
                    sysrq_trigger_io_stream2.close();
                }
            } catch (Throwable th5) {
                boolean z3 = isSilentANR3;
                ProcessCpuTracker processCpuTracker3 = processCpuTracker;
                SparseArray<Boolean> sparseArray = lastPids;
                ArrayList<Integer> arrayList = firstPids;
                StringBuilder sb = info;
                String str3 = str;
                long j3 = anrTime;
                boolean z4 = aboveSystem;
                String str4 = traceMark;
                sysrq_trigger_io_stream = sysrq_trigger_io_stream2;
                sysrq_trigger = sysrq_trigger2;
                th = th5;
                if (sysrq_trigger != null) {
                }
                if (sysrq_trigger_io_stream != null) {
                }
                throw th;
            }
            OutputStreamWriter outputStreamWriter22 = sysrq_trigger2;
            FileOutputStream fileOutputStream22 = sysrq_trigger_io_stream2;
            String[] nativeProcs32 = null;
            if (isSilentANR3) {
                int i5 = 0;
                while (true) {
                    if (i5 >= Watchdog.NATIVE_STACKS_OF_INTEREST.length) {
                        i = 0;
                        break;
                    } else if (Watchdog.NATIVE_STACKS_OF_INTEREST[i5].equals(processRecord.processName)) {
                        i = 0;
                        nativeProcs32 = new String[]{processRecord.processName};
                        break;
                    } else {
                        i5++;
                    }
                }
            } else {
                i = 0;
                nativeProcs32 = Watchdog.NATIVE_STACKS_OF_INTEREST;
            }
            String[] nativeProcs222 = nativeProcs32;
            pids = nativeProcs222 == null ? null : Process.getPidsForCommands(nativeProcs222);
            ArrayList<Integer> nativePids32 = null;
            if (pids != null) {
                nativePids32 = new ArrayList<>(pids.length);
                int length = pids.length;
                int i6 = i;
                while (i6 < length) {
                    nativePids32.add(Integer.valueOf(pids[i6]));
                    i6++;
                    length = length;
                    pids = pids;
                }
            }
            int[] pids222 = pids;
            ArrayList<Integer> nativePids222 = nativePids32;
            ProcessCpuTracker processCpuTracker4 = isSilentANR3 ? null : processCpuTracker;
            SparseArray<Boolean> sparseArray2 = isSilentANR3 ? null : lastPids;
            boolean z22 = isSilentANR3;
            int[] iArr22 = pids222;
            String[] strArr22 = nativeProcs222;
            long anrTime422 = anrTime;
            ProcessCpuTracker processCpuTracker222 = processCpuTracker;
            String str222 = traceMark;
            File tracesFile22 = ActivityManagerService.dumpStackTraces(processRecord, true, firstPids, processCpuTracker4, sparseArray2, nativePids222);
            this.mService.updateCpuStatsNow();
            synchronized (this.mService.mProcessCpuTracker) {
                try {
                    long anrTime5 = anrTime422;
                    try {
                        cpuInfo = this.mService.mProcessCpuTracker.printCurrentState(anrTime5);
                    } catch (Throwable th6) {
                        th = th6;
                        ProcessCpuTracker processCpuTracker5 = processCpuTracker222;
                        long j4 = anrTime5;
                        SparseArray<Boolean> sparseArray3 = lastPids;
                        ArrayList<Integer> arrayList2 = firstPids;
                        StringBuilder sb2 = info;
                        String str5 = str;
                        boolean z5 = aboveSystem;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th7) {
                                th = th7;
                            }
                        }
                        throw th;
                    }
                    try {
                    } catch (Throwable th8) {
                        th = th8;
                        ProcessCpuTracker processCpuTracker6 = processCpuTracker222;
                        String str6 = cpuInfo;
                        long j5 = anrTime5;
                        SparseArray<Boolean> sparseArray4 = lastPids;
                        ArrayList<Integer> arrayList3 = firstPids;
                        StringBuilder sb3 = info;
                        String cpuInfo2 = str;
                        boolean z6 = aboveSystem;
                        String str7 = str6;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    boolean z7 = aboveSystem;
                    ProcessCpuTracker processCpuTracker7 = processCpuTracker222;
                    SparseArray<Boolean> sparseArray5 = lastPids;
                    ArrayList<Integer> arrayList4 = firstPids;
                    StringBuilder sb4 = info;
                    String str8 = str;
                    long j6 = anrTime422;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private void makeAppNotRespondingLocked(ProcessRecord app, String activity, String shortMsg, String longMsg) {
        app.notResponding = true;
        app.notRespondingReport = generateProcessError(app, 2, activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00eb, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ee, code lost:
        if (r2 == null) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f0, code lost:
        r2.show();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f3, code lost:
        return;
     */
    public void handleShowAnrUi(Message msg) {
        Dialog dialogToShow = null;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                AppNotRespondingDialog.Data data = (AppNotRespondingDialog.Data) msg.obj;
                ProcessRecord proc = data.proc;
                if (proc == null) {
                    Slog.e("ActivityManager", "handleShowAnrUi: proc is null");
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                if (proc.info != null) {
                    String packageName = proc.info.packageName;
                    if (this.mIswhitelist_for_short_time && whitelist_for_short_time.contains(packageName)) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                try {
                    if (proc.anrDialog != null) {
                        Slog.e("ActivityManager", "App already has anr dialog: " + proc);
                        MetricsLogger.action(this.mContext, 317, -2);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    Intent intent = new Intent("android.intent.action.ANR");
                    if (!this.mService.mProcessesReady) {
                        intent.addFlags(1342177280);
                    }
                    this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, 0);
                    boolean z = false;
                    if (Settings.Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0) {
                        z = true;
                    }
                    boolean showBackground = z;
                    if ((this.mService.canShowErrorDialogs() || showBackground) && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && !this.mIsNotShowAnrDialog) {
                        Context context = this.mContext;
                        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(proc.mDisplayId)) {
                            Context tmpContext = HwPCUtils.getDisplayContext(context, proc.mDisplayId);
                            if (tmpContext != null) {
                                context = tmpContext;
                            }
                        }
                        dialogToShow = new AppNotRespondingDialog(this.mService, context, data);
                        proc.anrDialog = dialogToShow;
                    } else {
                        MetricsLogger.action(this.mContext, 317, -1);
                        this.mService.killAppAtUsersRequest(proc, null);
                    }
                } catch (Throwable th) {
                    th = th;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Message message = msg;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public void makeAppeyeAppNotRespondingLocked(ProcessRecord app, String activity, String shortMsg, String longMsg) {
        app.notRespondingReport = generateProcessError(app, 2, activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }
}
