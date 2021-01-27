package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ServiceStartArgs;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.webkit.WebViewZygote;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.server.AppStateTracker;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ServiceRecord;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pg.PGManagerInternal;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.uri.NeededUriGrants;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityServiceConnectionsHolder;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ActiveServices {
    private static final String ACTION_HWPUSH_MSG_EVENT = "com.huawei.push.action.MESSAGING_EVENT";
    private static final boolean DEBUG_DELAYED_SERVICE = ActivityManagerDebugConfig.DEBUG_SERVICE;
    private static final boolean DEBUG_DELAYED_STARTS = DEBUG_DELAYED_SERVICE;
    private static final String HW_PARENT_CONTROL = "com.huawei.parentcontrol";
    static final boolean IS_FPGA = boardname.contains("fpga");
    private static final boolean IS_HWSERVICEBUS_ENABLE = SystemProperties.getBoolean("hw_sc.distributed_exec_fwk", true);
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static final boolean LOG_SERVICE_START_STOP = true;
    static final int SERVICE_BACKGROUND_TIMEOUT = (SERVICE_TIMEOUT * 10);
    private static final long SERVICE_CONNECTIONS_THRESHOLD = 100;
    static final int SERVICE_START_FOREGROUND_TIMEOUT = 10000;
    static final int SERVICE_TIMEOUT = (IS_FPGA ? 20000000 : 20000);
    static final int SERVICE_WAIT_PRECHECK = 3000;
    private static final boolean SHOW_DUNGEON_NOTIFICATION = false;
    private static final boolean SMART_CLEANING = SystemProperties.getBoolean("hw.app.smart_cleaning", false);
    private static final String TAG = "ActivityManager";
    private static final String TAG_MU = "ActivityManager_MU";
    private static final String TAG_SERVICE = "ActivityManager";
    private static final String TAG_SERVICE_EXECUTING = "ActivityManager";
    private static final long TOOK_THRESHOLD_MS = 1000;
    static String boardname = SystemProperties.get("ro.board.boardname", "0");
    final ActivityManagerService mAm;
    final ArrayList<ServiceRecord> mDestroyingServices = new ArrayList<>();
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer;
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices = new ArrayList<>();
    final ArrayList<ServiceRecord> mRestartingServices = new ArrayList<>();
    boolean mScreenOn;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections = new ArrayMap<>();
    final SparseArray<ServiceMap> mServiceMap = new SparseArray<>();
    private ArrayList<ServiceRecord> mTmpCollectionResults = null;

    /* access modifiers changed from: package-private */
    public class ForcedStandbyListener extends AppStateTracker.Listener {
        ForcedStandbyListener() {
        }

        @Override // com.android.server.AppStateTracker.Listener
        public void stopForegroundServicesForUidPackage(int uid, String packageName) {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ServiceMap smap = ActiveServices.this.getServiceMapLocked(UserHandle.getUserId(uid));
                    int N = smap.mServicesByInstanceName.size();
                    ArrayList<ServiceRecord> toStop = new ArrayList<>(N);
                    for (int i = 0; i < N; i++) {
                        ServiceRecord r = smap.mServicesByInstanceName.valueAt(i);
                        if ((uid == r.serviceInfo.applicationInfo.uid || packageName.equals(r.serviceInfo.packageName)) && r.isForeground) {
                            toStop.add(r);
                        }
                    }
                    int numToStop = toStop.size();
                    if (numToStop > 0 && ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.i(ActivityManagerService.TAG, "Package " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + uid + " entering FAS with foreground services");
                    }
                    for (int i2 = 0; i2 < numToStop; i2++) {
                        ServiceRecord r2 = toStop.get(i2);
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.i(ActivityManagerService.TAG, "  Stopping fg for service " + r2);
                        }
                        ActiveServices.this.setServiceForegroundInnerLocked(r2, 0, null, 0, 0);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ActiveForegroundApp {
        boolean mAppOnTop;
        long mEndTime;
        long mHideTime;
        CharSequence mLabel;
        int mNumActive;
        String mPackageName;
        boolean mShownWhileScreenOn;
        boolean mShownWhileTop;
        long mStartTime;
        long mStartVisibleTime;
        int mUid;

        ActiveForegroundApp() {
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceMap extends Handler {
        static final int MSG_BG_START_TIMEOUT = 1;
        static final int MSG_UPDATE_FOREGROUND_APPS = 2;
        final ArrayMap<String, ActiveForegroundApp> mActiveForegroundApps = new ArrayMap<>();
        boolean mActiveForegroundAppsChanged;
        final ArrayList<ServiceRecord> mDelayedStartList = new ArrayList<>();
        final ArrayMap<ComponentName, ServiceRecord> mServicesByInstanceName = new ArrayMap<>();
        final ArrayMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap<>();
        final ArrayList<ServiceRecord> mStartingBackground = new ArrayList<>();
        final int mUserId;

        ServiceMap(Looper looper, int userId) {
            super(looper);
            this.mUserId = userId;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        rescheduleDelayedStartsLocked();
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 2) {
                ActiveServices.this.updateForegroundApps(this);
            }
        }

        /* access modifiers changed from: package-private */
        public void ensureNotStartingBackgroundLocked(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActivityManagerService.TAG, "No longer background starting: " + r);
                }
                rescheduleDelayedStartsLocked();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "No longer delaying start: " + r);
            }
        }

        /* access modifiers changed from: package-private */
        public void rescheduleDelayedStartsLocked() {
            removeMessages(1);
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int N = this.mStartingBackground.size();
            while (i < N) {
                ServiceRecord r = this.mStartingBackground.get(i);
                if (r.startingBgTimeout <= now) {
                    Slog.i(ActivityManagerService.TAG, "Waited long enough for: " + r);
                    this.mStartingBackground.remove(i);
                    N += -1;
                    i += -1;
                }
                i++;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ServiceRecord r2 = this.mDelayedStartList.remove(0);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (exec next): " + r2);
                }
                if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                    Slog.v(ActivityManagerService.TAG, "Remaining delayed list:");
                    for (int i2 = 0; i2 < this.mDelayedStartList.size(); i2++) {
                        Slog.v(ActivityManagerService.TAG, "  #" + i2 + ": " + this.mDelayedStartList.get(i2));
                    }
                }
                r2.delayed = false;
                if (r2.pendingStarts.size() <= 0) {
                    Slog.wtf(ActivityManagerService.TAG, "**** NO PENDING STARTS! " + r2 + " startReq=" + r2.startRequested + " delayedStop=" + r2.delayedStop);
                } else {
                    try {
                        ProcessRecord servicePR = ActiveServices.this.mAm.getProcessRecordLocked(r2.processName, r2.appInfo.uid, false);
                        if (!(servicePR == null || servicePR.thread == null)) {
                            LogPower.notifyAction(UserHandle.getAppId(r2.appInfo.uid), "serviceboot", r2.packageName, Integer.toString(servicePR.pid), (String) null);
                        }
                        ActiveServices.this.startServiceInnerLocked(this, r2.pendingStarts.get(0).intent, r2, false, true);
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = this.mStartingBackground.get(0);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Top bg start is " + next + ", can delay others up to " + when);
                }
                sendMessageAtTime(obtainMessage(1), when);
            }
            if (this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ActiveServices.this.mAm.backgroundServicesFinishedLocked(this.mUserId);
            }
        }
    }

    public ActiveServices(ActivityManagerService service) {
        int i = 1;
        this.mScreenOn = true;
        this.mLastAnrDumpClearer = new Runnable() {
            /* class com.android.server.am.ActiveServices.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActiveServices.this.mLastAnrDump = null;
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        };
        this.mAm = service;
        int maxBg = 0;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg > 0) {
            i = maxBg;
        } else if (!ActivityManager.isLowRamDeviceStatic()) {
            i = 8;
        }
        this.mMaxStartingBackground = i;
    }

    /* access modifiers changed from: package-private */
    public void systemServicesReady() {
        ((AppStateTracker) LocalServices.getService(AppStateTracker.class)).addListener(new ForcedStandbyListener());
    }

    /* access modifiers changed from: package-private */
    public ServiceRecord getServiceByNameLocked(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByNameLocked(" + name + "), callingUser = " + callingUser);
        }
        return getServiceMapLocked(callingUser).mServicesByInstanceName.get(name);
    }

    /* access modifiers changed from: package-private */
    public boolean hasBackgroundServicesLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        return smap != null && smap.mStartingBackground.size() >= this.mMaxStartingBackground;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceMap getServiceMapLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        ServiceMap smap2 = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap2);
        return smap2;
    }

    /* access modifiers changed from: package-private */
    public ArrayMap<ComponentName, ServiceRecord> getServicesLocked(int callingUser) {
        return getServiceMapLocked(callingUser).mServicesByInstanceName;
    }

    private boolean appRestrictedAnyInBackground(int uid, String packageName) {
        return this.mAm.mAppOpsService.checkOperation(70, uid, packageName) != 0;
    }

    /* access modifiers changed from: package-private */
    public ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId) throws TransactionTooLargeException {
        return startServiceLocked(caller, service, resolvedType, callingPid, callingUid, fgRequired, callingPackage, userId, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x020c  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0286  */
    public ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId, boolean allowBackgroundActivityStarts) throws TransactionTooLargeException {
        boolean callerFg;
        boolean forcedStandby;
        boolean callerFg2;
        boolean callerFg3;
        boolean forceSilentAbort;
        String str;
        boolean fgRequired2;
        boolean callerFg4;
        String str2;
        boolean z;
        boolean fgRequired3;
        boolean addToStarting;
        int allowed;
        boolean forceSilentAbort2;
        if (DEBUG_DELAYED_STARTS) {
            Slog.v(ActivityManagerService.TAG, "startService: " + service + " type=" + resolvedType + " args=" + service.getExtras());
        }
        if (caller != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
            if (callerApp != null) {
                callerFg = callerApp.setSchedGroup != 0;
            } else {
                throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting service " + service);
            }
        } else {
            callerFg = true;
        }
        this.mAm.mHwAMSEx.setServiceFlagLocked(1);
        ServiceLookupResult res = retrieveServiceLocked(service, null, resolvedType, callingPackage, callingPid, callingUid, userId, true, callerFg, false, false);
        this.mAm.mHwAMSEx.setServiceFlagLocked(0);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r = res.record;
        if (!this.mAm.mUserController.exists(r.userId)) {
            Slog.w(ActivityManagerService.TAG, "Trying to start service with non-existent user! " + r.userId);
            return null;
        }
        boolean z2 = true;
        boolean bgLaunch = !this.mAm.isUidActiveLocked(r.appInfo.uid);
        if (!bgLaunch || !appRestrictedAnyInBackground(r.appInfo.uid, r.packageName)) {
            callerFg2 = callerFg;
            forcedStandby = false;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                StringBuilder sb = new StringBuilder();
                sb.append("Forcing bg-only service start only for ");
                sb.append(r.shortInstanceName);
                sb.append(" : bgLaunch=");
                sb.append(bgLaunch);
                sb.append(" callerFg=");
                callerFg2 = callerFg;
                sb.append(callerFg2);
                Slog.d(ActivityManagerService.TAG, sb.toString());
            } else {
                callerFg2 = callerFg;
            }
            forcedStandby = true;
        }
        if (fgRequired) {
            forceSilentAbort2 = false;
            callerFg3 = callerFg2;
            int mode = this.mAm.mAppOpsService.checkOperation(76, r.appInfo.uid, r.packageName);
            if (mode != 0) {
                z2 = true;
                if (mode == 1) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("startForegroundService not allowed due to app op: service ");
                    sb2.append(service);
                    sb2.append(" to ");
                    sb2.append(r.shortInstanceName);
                    sb2.append(" from pid=");
                    sb2.append(callingPid);
                    sb2.append(" uid=");
                    sb2.append(callingUid);
                    sb2.append(" pkg=");
                    str = callingPackage;
                    sb2.append(str);
                    Slog.w(ActivityManagerService.TAG, sb2.toString());
                    fgRequired2 = false;
                    forceSilentAbort = true;
                    if (!forcedStandby || (!r.startRequested && !fgRequired2)) {
                        ActivityManagerService activityManagerService = this.mAm;
                        int i = r.appInfo.uid;
                        String str3 = r.packageName;
                        int i2 = r.appInfo.targetSdkVersion;
                        callerFg4 = callerFg3;
                        str2 = ActivityManagerService.TAG;
                        allowed = activityManagerService.getAppStartModeLocked(i, str3, i2, callingPid, false, false, forcedStandby);
                        if (allowed == 0) {
                            Slog.w(str2, "Background start not allowed: service " + service + " to " + r.shortInstanceName + " from pid=" + callingPid + " uid=" + callingUid + " pkg=" + str + " startFg?=" + fgRequired2);
                            if (allowed == 1 || forceSilentAbort) {
                                return null;
                            }
                            if (!forcedStandby || !fgRequired2) {
                                return new ComponentName("?", "app is in background uid " + this.mAm.mProcessList.getUidRecordLocked(r.appInfo.uid));
                            } else if (!ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                return null;
                            } else {
                                Slog.v(str2, "Silently dropping foreground service launch due to FAS");
                                return null;
                            }
                        } else {
                            fgRequired2 = fgRequired2;
                            z = true;
                        }
                    } else {
                        callerFg4 = callerFg3;
                        str2 = ActivityManagerService.TAG;
                        z = z2;
                    }
                    if (isServiceProxy(str, callingUid, service.getAction(), r)) {
                        Slog.i(str2, "start service is proxy: " + r.name);
                        return null;
                    }
                    if (r.appInfo.targetSdkVersion >= 26 || !fgRequired2) {
                        fgRequired3 = fgRequired2;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK || ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.i(str2, "startForegroundService() but host targets " + r.appInfo.targetSdkVersion + " - not requiring startForeground()");
                        }
                        fgRequired3 = false;
                    }
                    NeededUriGrants neededGrants = this.mAm.mUgmInternal.checkGrantUriPermissionFromIntent(callingUid, r.packageName, service, service.getFlags(), null, r.userId);
                    if (!requestStartTargetPermissionsReviewIfNeededLocked(r, callingPackage, callingUid, service, callerFg4, userId)) {
                        return null;
                    }
                    if (unscheduleServiceRestartLocked(r, callingUid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(str2, "START SERVICE WHILE RESTART PENDING: " + r);
                    }
                    r.lastActivity = SystemClock.uptimeMillis();
                    r.startRequested = z;
                    r.delayedStop = false;
                    r.fgRequired = fgRequired3;
                    r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(), service, neededGrants, callingUid));
                    if (fgRequired3) {
                        ServiceState stracker = r.getTracker();
                        if (stracker != null) {
                            stracker.setForeground(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                        }
                        this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName, true);
                    }
                    ServiceMap smap = getServiceMapLocked(r.userId);
                    boolean addToStarting2 = false;
                    if (callerFg4 || fgRequired3 || r.app != null || !this.mAm.mUserController.hasStartedUserState(r.userId)) {
                        if (DEBUG_DELAYED_STARTS) {
                            if (callerFg4 || fgRequired3) {
                                Slog.v(str2, "Not potential delay (callerFg=" + callerFg4 + " uid=" + callingUid + " pid=" + callingPid + " fgRequired=" + fgRequired3 + "): " + r);
                            } else if (r.app != null) {
                                Slog.v(str2, "Not potential delay (cur app=" + r.app + "): " + r);
                            } else {
                                Slog.v(str2, "Not potential delay (user " + r.userId + " not started): " + r);
                            }
                        }
                        addToStarting = false;
                    } else {
                        ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                        if (proc == null || proc.getCurProcState() > 12) {
                            if (DEBUG_DELAYED_SERVICE) {
                                Slog.v(str2, "Potential start delay of " + r + " in " + proc);
                            }
                            if (r.delayed) {
                                if (DEBUG_DELAYED_STARTS) {
                                    Slog.v(str2, "Continuing to delay: " + r);
                                }
                                return r.name;
                            } else if (smap.mStartingBackground.size() >= this.mMaxStartingBackground) {
                                Slog.i(str2, "Delaying start of: " + r);
                                smap.mDelayedStartList.add(r);
                                r.delayed = true;
                                return r.name;
                            } else {
                                if (DEBUG_DELAYED_STARTS) {
                                    Slog.v(str2, "Not delaying: " + r);
                                }
                                addToStarting2 = true;
                            }
                        } else if (proc.getCurProcState() >= 11) {
                            addToStarting2 = true;
                            if (DEBUG_DELAYED_STARTS) {
                                Slog.v(str2, "Not delaying, but counting as bg: " + r);
                            }
                        } else if (DEBUG_DELAYED_STARTS) {
                            StringBuilder sb3 = new StringBuilder(128);
                            sb3.append("Not potential delay (state=");
                            sb3.append(proc.getCurProcState());
                            sb3.append(' ');
                            sb3.append(proc.adjType);
                            String reason = proc.makeAdjReason();
                            if (reason != null) {
                                sb3.append(' ');
                                sb3.append(reason);
                            }
                            sb3.append("): ");
                            sb3.append(r.toString());
                            Slog.v(str2, sb3.toString());
                        }
                        addToStarting = addToStarting2;
                    }
                    if (str != null && !str.equals(r.packageName)) {
                        LogPower.notifyAction(UserHandle.getAppId(r.appInfo.uid), "serviceboot", r.packageName, Integer.toString(0), str);
                    }
                    if (allowBackgroundActivityStarts) {
                        r.whitelistBgActivityStartsOnServiceStart();
                    }
                    return startServiceInnerLocked(smap, service, r, callerFg4, addToStarting);
                } else if (mode != 3) {
                    return new ComponentName("!!", "foreground not allowed as per app op");
                } else {
                    str = callingPackage;
                }
            } else {
                str = callingPackage;
                z2 = true;
            }
        } else {
            str = callingPackage;
            forceSilentAbort2 = false;
            callerFg3 = callerFg2;
        }
        fgRequired2 = fgRequired;
        forceSilentAbort = forceSilentAbort2;
        if (!forcedStandby) {
        }
        ActivityManagerService activityManagerService2 = this.mAm;
        int i3 = r.appInfo.uid;
        String str32 = r.packageName;
        int i22 = r.appInfo.targetSdkVersion;
        callerFg4 = callerFg3;
        str2 = ActivityManagerService.TAG;
        allowed = activityManagerService2.getAppStartModeLocked(i3, str32, i22, callingPid, false, false, forcedStandby);
        if (allowed == 0) {
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, final int userId) {
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(r.packageName, r.userId)) {
            return true;
        }
        if (!callerFg) {
            Slog.w(ActivityManagerService.TAG, "u" + r.userId + " Starting a service in package" + r.packageName + " requires a permissions review");
            return false;
        }
        IIntentSender target = this.mAm.mPendingIntentController.getIntentSender(4, callingPackage, callingUid, userId, null, null, 0, new Intent[]{service}, new String[]{service.resolveType(this.mAm.mContext.getContentResolver())}, 1409286144, null);
        final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
        intent.addFlags(411041792);
        intent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
        intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
        if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
            Slog.i(ActivityManagerService.TAG, "u" + r.userId + " Launching permission review for package " + r.packageName);
        }
        this.mAm.mHandler.post(new Runnable() {
            /* class com.android.server.am.ActiveServices.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(userId));
            }
        });
        return false;
    }

    /* access modifiers changed from: package-private */
    public ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        boolean first = false;
        r.callStart = false;
        StatsLog.write(99, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName(), 1);
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (r.startRequested && addToStarting) {
            if (smap.mStartingBackground.size() == 0) {
                first = true;
            }
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + this.mAm.mConstants.BG_START_TIMEOUT;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(ActivityManagerService.TAG, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStartsLocked();
            }
        } else if (callerFg || r.fgRequired) {
            smap.ensureNotStartingBackgroundLocked(r);
        }
        return r.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "Delaying stop of pending: " + service);
            }
            service.delayedStop = true;
            return;
        }
        StatsLog.write(99, service.appInfo.uid, service.name.getPackageName(), service.name.getClassName(), 2);
        synchronized (service.stats.getBatteryStats()) {
            service.stats.stopRunningLocked();
        }
        service.startRequested = false;
        if (service.tracker != null) {
            service.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        service.callStart = false;
        bringDownServiceIfNeededLocked(service, false, false);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "stopService: " + service + " type=" + resolvedType);
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (caller == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(service, null, resolvedType, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, false, false);
            if (r == null) {
                return 0;
            }
            if (r.record == null) {
                return -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                stopServiceLocked(r.record);
                Binder.restoreCallingIdentity(origId);
                return 1;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when stopping service " + service);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopInBackgroundLocked(int uid) {
        ServiceMap services = this.mServiceMap.get(UserHandle.getUserId(uid));
        ArrayList<ServiceRecord> stopping = null;
        if (services != null) {
            for (int i = services.mServicesByInstanceName.size() - 1; i >= 0; i--) {
                ServiceRecord service = services.mServicesByInstanceName.valueAt(i);
                if (service != null && service.appInfo.uid == uid && service.startRequested && this.mAm.getAppStartModeLocked(service.appInfo.uid, service.packageName, service.appInfo.targetSdkVersion, -1, false, false, false) != 0) {
                    if (stopping == null) {
                        stopping = new ArrayList<>();
                    }
                    String compName = service.shortInstanceName;
                    EventLogTags.writeAmStopIdleService(service.appInfo.uid, compName);
                    StringBuilder sb = new StringBuilder(64);
                    sb.append("Stopping service due to app idle: ");
                    UserHandle.formatUid(sb, service.appInfo.uid);
                    sb.append(" ");
                    TimeUtils.formatDuration(service.createRealTime - SystemClock.elapsedRealtime(), sb);
                    sb.append(" ");
                    sb.append(compName);
                    Slog.w(ActivityManagerService.TAG, sb.toString());
                    stopping.add(service);
                    if (appRestrictedAnyInBackground(service.appInfo.uid, service.packageName)) {
                        cancelForegroundNotificationLocked(service);
                    }
                }
            }
            if (stopping != null) {
                for (int i2 = stopping.size() - 1; i2 >= 0; i2--) {
                    ServiceRecord service2 = stopping.get(i2);
                    service2.delayed = false;
                    services.ensureNotStartingBackgroundLocked(service2);
                    stopServiceLocked(service2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void killMisbehavingService(ServiceRecord r, int appUid, int appPid, String localPackageName) {
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                stopServiceLocked(r);
                this.mAm.crashApplication(appUid, appPid, localPackageName, -1, "Bad notification for startForeground", true);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, null, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), false, false, false, false);
        if (r == null) {
            return null;
        }
        if (r.record != null) {
            IntentBindRecord ib = r.record.bindings.get(r.record.intent);
            if (ib != null) {
                return ib.binder;
            }
            return null;
        }
        throw new SecurityException("Permission Denial: Accessing service from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + r.permission);
    }

    /* access modifiers changed from: package-private */
    public boolean stopServiceTokenLocked(ComponentName className, IBinder token, int startId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "stopServiceToken: " + className + " " + token + " startId=" + startId);
        }
        ServiceRecord r = findServiceLocked(className, token, UserHandle.getCallingUserId());
        if (r == null) {
            return false;
        }
        if (startId >= 0) {
            ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    ServiceRecord.StartItem cur = r.deliveredStarts.remove(0);
                    cur.removeUriPermissionsLocked();
                    if (cur == si) {
                        break;
                    }
                }
            }
            if (r.getLastStartId() != startId) {
                return false;
            }
            if (r.deliveredStarts.size() > 0) {
                Slog.w(ActivityManagerService.TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
            }
        }
        StatsLog.write(99, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName(), 2);
        synchronized (r.stats.getBatteryStats()) {
            r.stats.stopRunningLocked();
        }
        r.startRequested = false;
        if (r.tracker != null) {
            r.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        r.callStart = false;
        long origId = Binder.clearCallingIdentity();
        bringDownServiceIfNeededLocked(r, false, false);
        Binder.restoreCallingIdentity(origId);
        return true;
    }

    public void setServiceForegroundLocked(ComponentName className, IBinder token, int id, Notification notification, int flags, int foregroundServiceType) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                setServiceForegroundInnerLocked(r, id, notification, flags, foregroundServiceType);
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public int getForegroundServiceTypeLocked(ComponentName className, IBinder token) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        int ret = 0;
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                ret = r.foregroundServiceType;
            }
            return ret;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean foregroundAppShownEnoughLocked(ActiveForegroundApp aa, long nowElapsed) {
        long j;
        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
            Slog.d(ActivityManagerService.TAG, "Shown enough: pkg=" + aa.mPackageName + ", uid=" + aa.mUid);
        }
        aa.mHideTime = JobStatus.NO_LATEST_RUNTIME;
        if (aa.mShownWhileTop) {
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return true;
            }
            Slog.d(ActivityManagerService.TAG, "YES - shown while on top");
            return true;
        } else if (this.mScreenOn || aa.mShownWhileScreenOn) {
            long minTime = aa.mStartVisibleTime;
            if (aa.mStartTime != aa.mStartVisibleTime) {
                j = this.mAm.mConstants.FGSERVICE_SCREEN_ON_AFTER_TIME;
            } else {
                j = this.mAm.mConstants.FGSERVICE_MIN_SHOWN_TIME;
            }
            long minTime2 = minTime + j;
            if (nowElapsed >= minTime2) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "YES - shown long enough with screen on");
                }
                return true;
            }
            long reportTime = this.mAm.mConstants.FGSERVICE_MIN_REPORT_TIME + nowElapsed;
            aa.mHideTime = reportTime > minTime2 ? reportTime : minTime2;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(ActivityManagerService.TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen on");
            return false;
        } else {
            long minTime3 = aa.mEndTime + this.mAm.mConstants.FGSERVICE_SCREEN_ON_BEFORE_TIME;
            if (nowElapsed >= minTime3) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "YES - gone long enough with screen off");
                }
                return true;
            }
            aa.mHideTime = minTime3;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(ActivityManagerService.TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen off");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateForegroundApps(ServiceMap smap) {
        ArrayList<ActiveForegroundApp> active = null;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.elapsedRealtime();
                long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                if (smap != null) {
                    if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.d(ActivityManagerService.TAG, "Updating foreground apps for user " + smap.mUserId);
                    }
                    for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                        ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i);
                        if (aa.mEndTime != 0) {
                            if (foregroundAppShownEnoughLocked(aa, now)) {
                                smap.mActiveForegroundApps.removeAt(i);
                                smap.mActiveForegroundAppsChanged = true;
                            } else if (aa.mHideTime < nextUpdateTime) {
                                nextUpdateTime = aa.mHideTime;
                            }
                        }
                        if (!aa.mAppOnTop) {
                            if (active == null) {
                                active = new ArrayList<>();
                            }
                            if (ActivityManagerDebugConfig.HWFLOW) {
                                Slog.i(ActivityManagerService.TAG, " updateForegroundApps Adding active: pkg= " + aa.mPackageName + ", uid=" + aa.mUid);
                            }
                            active.add(aa);
                        }
                    }
                    smap.removeMessages(2);
                    if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.d(ActivityManagerService.TAG, "Next update time in: " + (nextUpdateTime - now));
                        }
                        smap.sendMessageAtTime(smap.obtainMessage(2), (SystemClock.uptimeMillis() + nextUpdateTime) - SystemClock.elapsedRealtime());
                    }
                }
                if (smap.mActiveForegroundAppsChanged) {
                    smap.mActiveForegroundAppsChanged = false;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void requestUpdateActiveForegroundAppsLocked(ServiceMap smap, long timeElapsed) {
        Message msg = smap.obtainMessage(2);
        if (timeElapsed != 0) {
            smap.sendMessageAtTime(msg, (SystemClock.uptimeMillis() + timeElapsed) - SystemClock.elapsedRealtime());
            return;
        }
        smap.mActiveForegroundAppsChanged = true;
        smap.sendMessage(msg);
    }

    private void decActiveForegroundAppLocked(ServiceMap smap, ServiceRecord r) {
        ActiveForegroundApp active = smap.mActiveForegroundApps.get(r.packageName);
        if (active != null) {
            active.mNumActive--;
            if (active.mNumActive <= 0) {
                active.mEndTime = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Ended running of service");
                }
                if (foregroundAppShownEnoughLocked(active, active.mEndTime)) {
                    smap.mActiveForegroundApps.remove(r.packageName);
                    smap.mActiveForegroundAppsChanged = true;
                    requestUpdateActiveForegroundAppsLocked(smap, 0);
                } else if (active.mHideTime < JobStatus.NO_LATEST_RUNTIME) {
                    requestUpdateActiveForegroundAppsLocked(smap, active.mHideTime);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateScreenStateLocked(boolean screenOn) {
        if (this.mScreenOn != screenOn) {
            this.mScreenOn = screenOn;
            if (screenOn) {
                long nowElapsed = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Screen turned on");
                }
                for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                    ServiceMap smap = this.mServiceMap.valueAt(i);
                    long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                    boolean changed = false;
                    for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                        ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                        if (active.mEndTime != 0) {
                            if (!active.mShownWhileScreenOn && active.mStartVisibleTime == active.mStartTime) {
                                active.mStartVisibleTime = nowElapsed;
                                active.mEndTime = nowElapsed;
                            }
                            if (foregroundAppShownEnoughLocked(active, nowElapsed)) {
                                smap.mActiveForegroundApps.remove(active.mPackageName);
                                smap.mActiveForegroundAppsChanged = true;
                                changed = true;
                            } else if (active.mHideTime < nextUpdateTime) {
                                nextUpdateTime = active.mHideTime;
                            }
                        } else if (!active.mShownWhileScreenOn) {
                            active.mShownWhileScreenOn = true;
                            active.mStartVisibleTime = nowElapsed;
                        }
                    }
                    if (changed) {
                        requestUpdateActiveForegroundAppsLocked(smap, 0);
                    } else if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        requestUpdateActiveForegroundAppsLocked(smap, nextUpdateTime);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void foregroundServiceProcStateChangedLocked(UidRecord uidRec) {
        ServiceMap smap = this.mServiceMap.get(UserHandle.getUserId(uidRec.uid));
        if (smap != null) {
            boolean changed = false;
            for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                if (active.mUid == uidRec.uid) {
                    if (uidRec.getCurProcState() <= 2) {
                        if (!active.mAppOnTop) {
                            active.mAppOnTop = true;
                            changed = true;
                        }
                        active.mShownWhileTop = true;
                    } else if (active.mAppOnTop) {
                        active.mAppOnTop = false;
                        changed = true;
                    }
                }
            }
            if (changed) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    private boolean appIsTopLocked(int uid) {
        return this.mAm.getUidState(uid) <= 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setServiceForegroundInnerLocked(ServiceRecord r, int id, Notification notification, int flags, int foregroundServiceType) {
        int foregroundServiceType2;
        ServiceState stracker;
        if (id == 0) {
            if (r.isForeground) {
                ServiceMap smap = getServiceMapLocked(r.userId);
                if (smap != null) {
                    decActiveForegroundAppLocked(smap, r);
                }
                r.isForeground = false;
                ServiceState stracker2 = r.getTracker();
                if (stracker2 != null) {
                    stracker2.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 2);
                LogPower.push(234, r.packageName, r.shortInstanceName, Integer.toString(r.foregroundServiceType), new String[]{Long.toString(r.createRealTime)});
                this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, false);
                if (r.app != null) {
                    this.mAm.updateLruProcessLocked(r.app, false, null);
                    updateServiceForegroundLocked(r.app, true);
                }
            }
            if ((flags & 1) != 0) {
                cancelForegroundNotificationLocked(r);
                r.foregroundId = 0;
                r.foregroundNoti = null;
            } else if (r.appInfo.targetSdkVersion >= 21) {
                r.stripForegroundServiceFlagFromNotification();
                if ((flags & 2) != 0) {
                    r.foregroundId = 0;
                    r.foregroundNoti = null;
                }
            }
        } else if (notification != null) {
            if (r.appInfo.isInstantApp()) {
                int mode = this.mAm.mAppOpsService.checkOperation(68, r.appInfo.uid, r.appInfo.packageName);
                if (mode != 0) {
                    if (mode == 1) {
                        Slog.w(ActivityManagerService.TAG, "Instant app " + r.appInfo.packageName + " does not have permission to create foreground services, ignoring.");
                        return;
                    } else if (mode != 2) {
                        this.mAm.enforcePermission("android.permission.INSTANT_APP_FOREGROUND_SERVICE", r.app.pid, r.appInfo.uid, "startForeground");
                    } else {
                        throw new SecurityException("Instant app " + r.appInfo.packageName + " does not have permission to create foreground services");
                    }
                }
                foregroundServiceType2 = foregroundServiceType;
            } else {
                if (r.appInfo.targetSdkVersion >= 28) {
                    this.mAm.enforcePermission("android.permission.FOREGROUND_SERVICE", r.app.pid, r.appInfo.uid, "startForeground");
                }
                int manifestType = r.serviceInfo.getForegroundServiceType();
                if (foregroundServiceType == -1) {
                    foregroundServiceType2 = manifestType;
                } else {
                    foregroundServiceType2 = foregroundServiceType;
                }
                if ((foregroundServiceType2 & manifestType) != foregroundServiceType2) {
                    throw new IllegalArgumentException("foregroundServiceType " + String.format("0x%08X", Integer.valueOf(foregroundServiceType2)) + " is not a subset of foregroundServiceType attribute " + String.format("0x%08X", Integer.valueOf(manifestType)) + " in service element of manifest file");
                }
            }
            boolean alreadyStartedOp = false;
            boolean stopProcStatsOp = false;
            if (r.fgRequired) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                    Slog.i(ActivityManagerService.TAG, "Service called startForeground() as required: " + r);
                }
                r.fgRequired = false;
                r.fgWaiting = false;
                stopProcStatsOp = true;
                alreadyStartedOp = true;
                this.mAm.mHandler.removeMessages(66, r);
            }
            boolean ignoreForeground = false;
            try {
                int mode2 = this.mAm.mAppOpsService.checkOperation(76, r.appInfo.uid, r.packageName);
                if (mode2 != 0) {
                    if (mode2 == 1) {
                        Slog.w(ActivityManagerService.TAG, "Service.startForeground() not allowed due to app op: service " + r.shortInstanceName);
                        ignoreForeground = true;
                    } else if (mode2 != 3) {
                        throw new SecurityException("Foreground not allowed as per app op");
                    }
                }
                if (!ignoreForeground && !appIsTopLocked(r.appInfo.uid) && appRestrictedAnyInBackground(r.appInfo.uid, r.packageName)) {
                    Slog.w(ActivityManagerService.TAG, "Service.startForeground() not allowed due to bg restriction: service " + r.shortInstanceName);
                    updateServiceForegroundLocked(r.app, false);
                    ignoreForeground = true;
                }
                if (!ignoreForeground) {
                    if (r.foregroundId != id) {
                        cancelForegroundNotificationLocked(r);
                        r.foregroundId = id;
                    }
                    notification.flags |= 64;
                    r.foregroundNoti = notification;
                    r.foregroundServiceType = foregroundServiceType2;
                    if (!r.isForeground) {
                        ServiceMap smap2 = getServiceMapLocked(r.userId);
                        if (smap2 != null) {
                            ActiveForegroundApp active = smap2.mActiveForegroundApps.get(r.packageName);
                            if (active == null) {
                                active = new ActiveForegroundApp();
                                active.mPackageName = r.packageName;
                                active.mUid = r.appInfo.uid;
                                active.mShownWhileScreenOn = this.mScreenOn;
                                if (!(r.app == null || r.app.uidRecord == null)) {
                                    boolean z = r.app.uidRecord.getCurProcState() <= 2;
                                    active.mShownWhileTop = z;
                                    active.mAppOnTop = z;
                                }
                                long elapsedRealtime = SystemClock.elapsedRealtime();
                                active.mStartVisibleTime = elapsedRealtime;
                                active.mStartTime = elapsedRealtime;
                                smap2.mActiveForegroundApps.put(r.packageName, active);
                                requestUpdateActiveForegroundAppsLocked(smap2, 0);
                            }
                            active.mNumActive++;
                        }
                        r.isForeground = true;
                        if (!stopProcStatsOp) {
                            ServiceState stracker3 = r.getTracker();
                            if (stracker3 != null) {
                                stracker3.setForeground(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                            }
                        } else {
                            stopProcStatsOp = false;
                        }
                        this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName, true);
                        StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 1);
                        LogPower.push(233, r.packageName, r.shortInstanceName, Integer.toString(r.foregroundServiceType), new String[]{Long.toString(r.createRealTime)});
                        this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, true);
                    }
                    r.postNotification();
                    if (r.app != null) {
                        updateServiceForegroundLocked(r.app, true);
                    }
                    getServiceMapLocked(r.userId).ensureNotStartingBackgroundLocked(r);
                    this.mAm.notifyPackageUse(r.serviceInfo.packageName, 2);
                } else if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Suppressing startForeground() for FAS " + r);
                }
            } finally {
                if (stopProcStatsOp && (stracker = r.getTracker()) != null) {
                    stracker.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                if (alreadyStartedOp) {
                    this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                }
            }
        } else {
            throw new IllegalArgumentException("null notification");
        }
    }

    private void cancelForegroundNotificationLocked(ServiceRecord r) {
        if (!(r == null || r.foregroundId == 0)) {
            ServiceMap sm = getServiceMapLocked(r.userId);
            if (sm != null) {
                for (int i = sm.mServicesByInstanceName.size() - 1; i >= 0; i--) {
                    ServiceRecord other = sm.mServicesByInstanceName.valueAt(i);
                    if (other != null && other != r && other.foregroundId == r.foregroundId && other.packageName.equals(r.packageName)) {
                        return;
                    }
                }
            }
            r.cancelNotification();
        }
    }

    private void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = false;
        int fgServiceTypes = 0;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = proc.services.valueAt(i);
            if (sr.isForeground || sr.fgRequired) {
                anyForeground = true;
                fgServiceTypes |= sr.foregroundServiceType;
            }
        }
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, fgServiceTypes, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (proc.services.valueAt(i).whitelistManager) {
                proc.whitelistManager = true;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        if (!(clientProc == null || clientProc.connections == null)) {
            ArraySet<ProcessRecord> updatedProcesses = null;
            for (int i = 0; i < clientProc.connections.size(); i++) {
                ProcessRecord proc = clientProc.connections.valueAt(i).binding.service.app;
                if (!(proc == null || proc == clientProc)) {
                    if (updatedProcesses == null) {
                        updatedProcesses = new ArraySet<>();
                    } else if (updatedProcesses.contains(proc)) {
                    }
                    updatedProcesses.add(proc);
                    updateServiceClientActivitiesLocked(proc, null, false);
                }
            }
        }
    }

    private boolean updateServiceClientActivitiesLocked(ProcessRecord proc, ConnectionRecord modCr, boolean updateLru) {
        if (!(modCr == null || modCr.binding.client == null || modCr.binding.client.hasActivities())) {
            return false;
        }
        boolean anyClientActivities = false;
        for (int i = proc.services.size() - 1; i >= 0 && !anyClientActivities; i--) {
            ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = proc.services.valueAt(i).getConnections();
            for (int conni = connections.size() - 1; conni >= 0 && !anyClientActivities; conni--) {
                ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                int cri = clist.size() - 1;
                while (true) {
                    if (cri < 0) {
                        break;
                    }
                    ConnectionRecord cr = clist.get(cri);
                    if (!(cr.binding.client == null || cr.binding.client == proc || !cr.binding.client.hasActivities())) {
                        anyClientActivities = true;
                        break;
                    }
                    cri--;
                }
            }
        }
        if (anyClientActivities == proc.hasClientActivities()) {
            return false;
        }
        proc.setHasClientActivities(anyClientActivities);
        if (updateLru) {
            this.mAm.updateLruProcessLocked(proc, anyClientActivities, null);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, final IServiceConnection connection, int flags, String instanceName, String callingPackage, final int userId) throws TransactionTooLargeException {
        ActivityServiceConnectionsHolder<ConnectionRecord> activity;
        int clientLabel;
        PendingIntent clientIntent;
        final Intent service2;
        boolean permissionsReviewRequired;
        boolean callerFg;
        Throwable th;
        ConnectionRecord c;
        IBinder binder;
        ArrayList<ConnectionRecord> clist;
        boolean z;
        String str;
        int result;
        if (IS_HWSERVICEBUS_ENABLE && (result = this.mAm.mHwAMSEx.bindServiceEx(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId, -2)) != -2) {
            return result;
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "bindService: " + service + " type=" + resolvedType + " conn=" + connection.asBinder() + " flags=0x" + Integer.toHexString(flags));
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (callerApp != null) {
            if (token != null) {
                ActivityServiceConnectionsHolder<ConnectionRecord> activity2 = this.mAm.mAtmInternal.getServiceConnectionsHolder(token);
                if (activity2 == null) {
                    Slog.w(ActivityManagerService.TAG, "Binding with unknown activity: " + token);
                    return 0;
                }
                activity = activity2;
            } else {
                activity = null;
            }
            boolean isCallerSystem = callerApp.info.uid == 1000;
            if (isCallerSystem) {
                service.setDefusable(true);
                PendingIntent clientIntent2 = (PendingIntent) service.getParcelableExtra("android.intent.extra.client_intent");
                if (clientIntent2 != null) {
                    int clientLabel2 = service.getIntExtra("android.intent.extra.client_label", 0);
                    if (clientLabel2 != 0) {
                        service2 = service.cloneFilter();
                        clientLabel = clientLabel2;
                        clientIntent = clientIntent2;
                    } else {
                        service2 = service;
                        clientLabel = clientLabel2;
                        clientIntent = clientIntent2;
                    }
                } else {
                    service2 = service;
                    clientLabel = 0;
                    clientIntent = clientIntent2;
                }
            } else {
                service2 = service;
                clientLabel = 0;
                clientIntent = null;
            }
            if ((flags & DumpState.DUMP_HWFEATURES) != 0) {
                this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
            }
            if ((flags & DumpState.DUMP_FROZEN) != 0 && !isCallerSystem) {
                throw new SecurityException("Non-system caller (pid=" + Binder.getCallingPid() + ") set BIND_SCHEDULE_LIKE_TOP_APP when binding service " + service2);
            } else if ((flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 && !isCallerSystem) {
                throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service2);
            } else if ((flags & DumpState.DUMP_CHANGES) == 0 || isCallerSystem) {
                if ((flags & DumpState.DUMP_DEXOPT) != 0) {
                    this.mAm.enforceCallingPermission("android.permission.START_ACTIVITIES_FROM_BACKGROUND", "BIND_ALLOW_BACKGROUND_ACTIVITY_STARTS");
                }
                final boolean callerFg2 = callerApp.setSchedGroup != 0;
                boolean isBindExternal = (flags & Integer.MIN_VALUE) != 0;
                boolean allowInstant = (flags & DumpState.DUMP_CHANGES) != 0;
                this.mAm.mHwAMSEx.setServiceFlagLocked(2);
                ServiceLookupResult res = retrieveServiceLocked(service2, instanceName, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), userId, true, callerFg2, isBindExternal, allowInstant);
                this.mAm.mHwAMSEx.setServiceFlagLocked(0);
                if (res == null) {
                    return 0;
                }
                if (res.record == null) {
                    return -1;
                }
                final ServiceRecord s = res.record;
                if (isServiceProxy(callingPackage, callerApp.info.uid, service2.getAction(), s)) {
                    Slog.i(ActivityManagerService.TAG, "bind service is proxy: " + s.name);
                    return 0;
                }
                if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                    callerFg = callerFg2;
                    permissionsReviewRequired = false;
                } else if (!callerFg2) {
                    Slog.w(ActivityManagerService.TAG, "u" + s.userId + " Binding to a service in package" + s.packageName + " requires a permissions review");
                    return 0;
                } else {
                    callerFg = callerFg2;
                    permissionsReviewRequired = true;
                    RemoteCallback callback = new RemoteCallback(new RemoteCallback.OnResultListener() {
                        /* class com.android.server.am.ActiveServices.AnonymousClass3 */

                        public void onResult(Bundle result) {
                            synchronized (ActiveServices.this.mAm) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    long identity = Binder.clearCallingIdentity();
                                    try {
                                        if (ActiveServices.this.mPendingServices.contains(s)) {
                                            if (!ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                                                try {
                                                    ActiveServices.this.bringUpServiceLocked(s, service2.getFlags(), callerFg2, false, false);
                                                } catch (RemoteException e) {
                                                }
                                            } else {
                                                ActiveServices.this.unbindServiceLocked(connection);
                                            }
                                            Binder.restoreCallingIdentity(identity);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        }
                                    } finally {
                                        Binder.restoreCallingIdentity(identity);
                                    }
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        }
                    });
                    final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent.addFlags(411041792);
                    intent.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                    intent.putExtra("android.intent.extra.REMOTE_CALLBACK", (Parcelable) callback);
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        Slog.i(ActivityManagerService.TAG, "u" + s.userId + " Launching permission review for package " + s.packageName);
                    }
                    this.mAm.mHandler.post(new Runnable() {
                        /* class com.android.server.am.ActiveServices.AnonymousClass4 */

                        @Override // java.lang.Runnable
                        public void run() {
                            ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(userId));
                        }
                    });
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    if (unscheduleServiceRestartLocked(s, callerApp.info.uid, false)) {
                        try {
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.v(ActivityManagerService.TAG, "BIND SERVICE WHILE RESTART PENDING: " + s);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    if ((flags & 1) != 0) {
                        s.lastActivity = SystemClock.uptimeMillis();
                        if (!s.hasAutoCreateConnections()) {
                            ServiceState stracker = s.getTracker();
                            if (stracker != null) {
                                stracker.setBound(true, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                            }
                        }
                    }
                    if ((flags & DumpState.DUMP_COMPILER_STATS) != 0) {
                        this.mAm.requireAllowedAssociationsLocked(s.appInfo.packageName);
                    }
                    this.mAm.startAssociationLocked(callerApp.uid, callerApp.processName, callerApp.getCurProcState(), s.appInfo.uid, s.appInfo.longVersionCode, s.instanceName, s.processName);
                    try {
                        this.mAm.grantEphemeralAccessLocked(callerApp.userId, service2, UserHandle.getAppId(s.appInfo.uid), UserHandle.getAppId(callerApp.uid));
                        try {
                            this.mAm.mHwAMSEx.reportServiceRelationIAware(1, s, callerApp, service2, null);
                            AppBindRecord b = s.retrieveAppBindingLocked(service2, callerApp);
                            try {
                                c = new ConnectionRecord(b, activity, connection, flags, clientLabel, clientIntent, callerApp.uid, callerApp.processName, callingPackage);
                                binder = connection.asBinder();
                                s.addConnection(binder, c);
                                b.connections.add(c);
                                if (activity != null) {
                                    try {
                                        activity.addConnection(c);
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                            try {
                                b.client.connections.add(c);
                                c.startAssociationIfNeeded();
                                int connectionSize = s.getConnections().size();
                                if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
                                    Flog.i(102, "bindServiceLocked " + s + ",connection size= " + connectionSize + ",callingPackage= " + callingPackage);
                                }
                                if ((c.flags & 8) != 0) {
                                    b.client.hasAboveClient = true;
                                }
                                if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                                    s.whitelistManager = true;
                                }
                                if ((flags & DumpState.DUMP_DEXOPT) != 0) {
                                    s.setHasBindingWhitelistingBgActivityStarts(true);
                                }
                                if (s.app != null) {
                                    updateServiceClientActivitiesLocked(s.app, c, true);
                                }
                                ArrayList<ConnectionRecord> clist2 = this.mServiceConnections.get(binder);
                                if (clist2 == null) {
                                    ArrayList<ConnectionRecord> clist3 = new ArrayList<>();
                                    this.mServiceConnections.put(binder, clist3);
                                    clist = clist3;
                                } else {
                                    clist = clist2;
                                }
                                clist.add(c);
                                if ((flags & 1) != 0) {
                                    s.lastActivity = SystemClock.uptimeMillis();
                                    if (bringUpServiceLocked(s, service2.getFlags(), callerFg, false, permissionsReviewRequired) != null) {
                                        Binder.restoreCallingIdentity(origId);
                                        return 0;
                                    }
                                    z = false;
                                } else {
                                    z = false;
                                }
                                if (s.app != null) {
                                    if ((flags & DumpState.DUMP_HWFEATURES) != 0) {
                                        s.app.treatLikeActivity = true;
                                    }
                                    if (s.whitelistManager) {
                                        s.app.whitelistManager = true;
                                    }
                                    this.mAm.updateLruProcessLocked(s.app, ((!callerApp.hasActivitiesOrRecentTasks() || !s.app.hasClientActivities()) && (callerApp.getCurProcState() > 2 || (flags & DumpState.DUMP_HWFEATURES) == 0)) ? z : true, b.client);
                                    this.mAm.updateOomAdjLocked("updateOomAdj_bindService");
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    str = ActivityManagerService.TAG;
                                    Slog.v(str, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                                } else {
                                    str = ActivityManagerService.TAG;
                                }
                                if (s.app != null && b.intent.received) {
                                    try {
                                        c.conn.connected(s.name, b.intent.binder, z);
                                    } catch (Exception e) {
                                        Slog.w(str, "Failure sending service " + s.shortInstanceName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
                                    }
                                    if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                                        try {
                                            requestServiceBindingLocked(s, b.intent, callerFg, true);
                                        } catch (Throwable th5) {
                                            th = th5;
                                            Binder.restoreCallingIdentity(origId);
                                            throw th;
                                        }
                                    }
                                } else if (!b.intent.requested) {
                                    requestServiceBindingLocked(s, b.intent, callerFg, z);
                                }
                                getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                                Binder.restoreCallingIdentity(origId);
                                ProcessRecord app = s.app;
                                if (app == null && s.processName != null) {
                                    app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
                                }
                                if (app == null || callerApp.pid == app.pid || callerApp.info == null || app.info == null || callerApp.info.packageName == null || callerApp.info.packageName.equals(app.info.packageName)) {
                                    return 1;
                                }
                                LogPower.notifyAction(UserHandle.getAppId(app.uid), "bindservice", s.packageName, Integer.toString(app.pid), callingPackage);
                                LogPower.push(166, s.processName, Integer.toString(callerApp.pid), Integer.toString(app.pid), new String[]{"service"});
                                return 1;
                            } catch (Throwable th6) {
                                th = th6;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            } else {
                throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_INSTANT when binding service " + service2);
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when binding service " + service);
        }
    }

    /* access modifiers changed from: package-private */
    public void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
        Throwable th;
        Intent.FilterComparison filter;
        Intent intent2 = intent;
        long origId = Binder.clearCallingIdentity();
        try {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "PUBLISHING " + r + " " + intent2 + ": " + service);
            }
            if (r != null) {
                Intent.FilterComparison filter2 = new Intent.FilterComparison(intent2);
                IntentBindRecord b = r.bindings.get(filter2);
                int i = 0;
                if (b != null) {
                    try {
                        if (!b.received) {
                            b.binder = service;
                            b.requested = true;
                            b.received = true;
                            ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
                            int connectionSize = connections.size();
                            if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
                                Flog.i(102, "publishServiceLocked " + r + ",connection size= " + connectionSize);
                            }
                            long start = SystemClock.uptimeMillis();
                            int conni = connections.size() - 1;
                            while (conni >= 0) {
                                ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                                int i2 = i;
                                while (i2 < clist.size()) {
                                    ConnectionRecord c = clist.get(i2);
                                    if (!filter2.equals(c.binding.intent.intent)) {
                                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                            StringBuilder sb = new StringBuilder();
                                            filter = filter2;
                                            sb.append("Not publishing to: ");
                                            sb.append(c);
                                            Slog.v(ActivityManagerService.TAG, sb.toString());
                                        } else {
                                            filter = filter2;
                                        }
                                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                            Slog.v(ActivityManagerService.TAG, "Bound intent: " + c.binding.intent.intent);
                                        }
                                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                            Slog.v(ActivityManagerService.TAG, "Published intent: " + intent2);
                                        }
                                    } else {
                                        filter = filter2;
                                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                            Slog.v(ActivityManagerService.TAG, "Publishing to: " + c);
                                        }
                                        try {
                                            c.conn.connected(r.name, service, false);
                                        } catch (Exception e) {
                                            Slog.w(ActivityManagerService.TAG, "Failure sending service " + r.shortInstanceName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
                                        }
                                    }
                                    i2++;
                                    intent2 = intent;
                                    filter2 = filter;
                                }
                                conni--;
                                intent2 = intent;
                                i = 0;
                            }
                            long end = SystemClock.uptimeMillis();
                            if (ActivityManagerDebugConfig.HWFLOW && end - start > 1000) {
                                Flog.i(102, "publishServiceLocked " + r + ",took " + (end - start) + "ms");
                            }
                            serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), false);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                }
                serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), false);
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateServiceGroupLocked(IServiceConnection connection, int group, int importance) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "updateServiceGroup: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = this.mServiceConnections.get(binder);
        if (clist != null) {
            for (int i = clist.size() - 1; i >= 0; i--) {
                ServiceRecord srec = clist.get(i).binding.service;
                if (!(srec == null || (srec.serviceInfo.flags & 2) == 0)) {
                    if (srec.app != null) {
                        if (group > 0) {
                            srec.app.connectionService = srec;
                            srec.app.connectionGroup = group;
                            srec.app.connectionImportance = importance;
                        } else {
                            srec.app.connectionService = null;
                            srec.app.connectionGroup = 0;
                            srec.app.connectionImportance = 0;
                        }
                    } else if (group > 0) {
                        srec.pendingConnectionGroup = group;
                        srec.pendingConnectionImportance = importance;
                    } else {
                        srec.pendingConnectionGroup = 0;
                        srec.pendingConnectionImportance = 0;
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Could not find connection for " + connection.asBinder());
    }

    /* access modifiers changed from: package-private */
    public boolean unbindServiceLocked(IServiceConnection connection) {
        boolean z;
        int result;
        if (IS_HWSERVICEBUS_ENABLE && (result = this.mAm.mHwAMSEx.unbindServiceEx(connection, -2)) != -2) {
            return result != 0;
        }
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "unbindService: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = this.mServiceConnections.get(binder);
        if (clist == null) {
            Slog.w(ActivityManagerService.TAG, "Unbind failed: could not find connection for " + connection.asBinder());
            return false;
        }
        long origId = Binder.clearCallingIdentity();
        while (clist.size() > 0) {
            try {
                ConnectionRecord r = clist.get(0);
                removeConnectionLocked(r, null, null);
                if (clist.size() > 0 && clist.get(0) == r) {
                    Slog.wtf(ActivityManagerService.TAG, "Connection " + r + " not removed for binder " + binder);
                    clist.remove(0);
                }
                if (r.binding.service.app != null) {
                    if (r.binding.service.app.whitelistManager) {
                        updateWhitelistManagerLocked(r.binding.service.app);
                    }
                    if ((r.flags & DumpState.DUMP_HWFEATURES) != 0) {
                        r.binding.service.app.treatLikeActivity = true;
                        ActivityManagerService activityManagerService = this.mAm;
                        ProcessRecord processRecord = r.binding.service.app;
                        if (!r.binding.service.app.hasClientActivities()) {
                            if (!r.binding.service.app.treatLikeActivity) {
                                z = false;
                                activityManagerService.updateLruProcessLocked(processRecord, z, null);
                            }
                        }
                        z = true;
                        activityManagerService.updateLruProcessLocked(processRecord, z, null);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        this.mAm.updateOomAdjLocked("updateOomAdj_unbindService");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = r.bindings.get(new Intent.FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("unbindFinished in ");
                    sb.append(r);
                    sb.append(" at ");
                    sb.append(b);
                    sb.append(": apps=");
                    sb.append(b != null ? b.apps.size() : 0);
                    Slog.v(ActivityManagerService.TAG, sb.toString());
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || inDestroying) {
                        b.doRebind = true;
                    } else {
                        boolean inFg = false;
                        int i = b.apps.size() - 1;
                        while (true) {
                            if (i >= 0) {
                                ProcessRecord client = b.apps.valueAt(i).client;
                                if (client != null && client.setSchedGroup != 0) {
                                    inFg = true;
                                    break;
                                }
                                i--;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, true);
                            break;
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        ServiceRecord r = getServiceByNameLocked(name, userId);
        if (r == token) {
            return r;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public final class ServiceLookupResult {
        final String permission;
        final ServiceRecord record;

        ServiceLookupResult(ServiceRecord _record, String _permission) {
            this.record = _record;
            this.permission = _permission;
        }
    }

    /* access modifiers changed from: private */
    public class ServiceRestarter implements Runnable {
        private ServiceRecord mService;

        private ServiceRestarter() {
        }

        /* access modifiers changed from: package-private */
        public void setService(ServiceRecord service) {
            this.mService = service;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActiveServices.this.performServiceRestartLocked(this.mService);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* JADX WARN: Type inference failed for: r10v6, types: [com.android.server.am.ActiveServices$ServiceLookupResult, com.android.server.am.ServiceRecord] */
    /* JADX WARN: Type inference failed for: r10v26 */
    /* JADX WARN: Type inference failed for: r10v28 */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x06d2, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0527  */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0684  */
    /* JADX WARNING: Unknown variable types count: 1 */
    private ServiceLookupResult retrieveServiceLocked(Intent service, String instanceName, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal, boolean allowInstant) {
        ProcessRecord callerApp;
        ComponentName comp;
        ServiceRecord r;
        int i;
        int i2;
        Intent intent;
        String str;
        ServiceRecord r2;
        String str2;
        int userId2;
        ServiceInfo serviceInfo;
        ServiceMap smap;
        ?? r10;
        int i3;
        String str3;
        Intent intent2;
        String str4;
        int userId3;
        ServiceInfo sInfo;
        ServiceMap smap2;
        ServiceRecord r3;
        ServiceRecord r4;
        BatteryStatsImpl.Uid.Pkg.Serv ss;
        ServiceRecord r5 = null;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "retrieveServiceLocked: " + service + " type=" + resolvedType + " callingUid=" + callingUid);
        }
        int userId4 = this.mAm.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 1, "service", callingPackage);
        synchronized (this.mAm.mPidsSelfLocked) {
            callerApp = this.mAm.mPidsSelfLocked.get(callingPid);
        }
        ServiceMap smap3 = getServiceMapLocked(userId4);
        if (instanceName == null) {
            comp = service.getComponent();
        } else {
            ComponentName realComp = service.getComponent();
            if (realComp != null) {
                comp = new ComponentName(realComp.getPackageName(), realComp.getClassName() + ":" + instanceName);
            } else {
                throw new IllegalArgumentException("Can't use custom instance name '" + instanceName + "' without expicit component in Intent");
            }
        }
        if (comp != null) {
            ServiceRecord r6 = smap3.mServicesByInstanceName.get(comp);
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r6 != null) {
                Slog.v(ActivityManagerService.TAG, "Retrieved by component: " + r6);
            }
            r5 = r6;
        }
        if (r5 == null && !isBindExternal && instanceName == null) {
            r = smap3.mServicesByIntent.get(new Intent.FilterComparison(service));
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r != null) {
                Slog.v(ActivityManagerService.TAG, "Retrieved by intent: " + r);
            }
        } else {
            r = r5;
        }
        if (!(r == null || (r.serviceInfo.flags & 4) == 0 || callingPackage.equals(r.packageName))) {
            r = null;
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "Whoops, can't use existing external service");
            }
        }
        ServiceRecord r7 = r;
        if (r7 == null) {
            try {
                int userId5 = callingUid;
                try {
                    ResolveInfo rInfo = this.mAm.getPackageManagerInternalLocked().resolveService(service, resolvedType, allowInstant ? 268436480 | DumpState.DUMP_VOLUMES : 268436480, userId4, userId5);
                    if (rInfo != null) {
                        try {
                            serviceInfo = rInfo.serviceInfo;
                        } catch (RemoteException e) {
                            r7 = r7;
                        }
                    } else {
                        serviceInfo = null;
                    }
                    ServiceInfo sInfo2 = serviceInfo;
                    if (sInfo2 != null) {
                        try {
                            smap = smap3;
                            userId5 = userId4;
                            i3 = callingUid;
                            str4 = callingPackage;
                            str3 = instanceName;
                            intent2 = service;
                            try {
                                if (this.mAm.mHwAMSEx.shouldPreventStartService(sInfo2, callingPid, callingUid, userId5, callerApp, false, service)) {
                                    Slog.w(ActivityManagerService.TAG, "Service starting has been prevented by iaware or trustsbase sInfo " + sInfo2);
                                    return null;
                                }
                                r10 = 0;
                            } catch (RemoteException e2) {
                                userId4 = userId5;
                                r7 = r7;
                                smap3 = smap;
                                intent = service;
                                str = callingPackage;
                                i = callingPid;
                                r2 = r7;
                                i2 = callingUid;
                                if (r2 != null) {
                                }
                            }
                        } catch (RemoteException e3) {
                            r7 = r7;
                            intent = service;
                            str = callingPackage;
                            i = callingPid;
                            r2 = r7;
                            i2 = callingUid;
                            if (r2 != null) {
                            }
                        }
                    } else {
                        smap = smap3;
                        userId5 = userId4;
                        i3 = callingUid;
                        str4 = callingPackage;
                        str3 = instanceName;
                        intent2 = service;
                        r10 = 0;
                    }
                    if (sInfo2 == null) {
                        Slog.w(ActivityManagerService.TAG, "Unable to start service " + intent2 + " U=" + userId5 + ": not found");
                        return r10;
                    }
                    if (str3 != null) {
                        if ((sInfo2.flags & 2) == 0) {
                            throw new IllegalArgumentException("Can't use instance name '" + str3 + "' with non-isolated service '" + sInfo2.name + "'");
                        }
                    }
                    ComponentName className = new ComponentName(sInfo2.applicationInfo.packageName, sInfo2.name);
                    ComponentName name = comp != null ? comp : className;
                    if (!this.mAm.validateAssociationAllowedLocked(str4, i3, name.getPackageName(), sInfo2.applicationInfo.uid)) {
                        String msg = "association not allowed between packages " + str4 + " and " + name.getPackageName();
                        Slog.w(ActivityManagerService.TAG, "Service lookup failed: " + msg);
                        return new ServiceLookupResult(r10, msg);
                    }
                    String definingPackageName = sInfo2.applicationInfo.packageName;
                    int definingUid = sInfo2.applicationInfo.uid;
                    if ((sInfo2.flags & 4) != 0) {
                        if (!isBindExternal) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                        } else if (!sInfo2.exported) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + className + " is not exported");
                        } else if ((sInfo2.flags & 2) != 0) {
                            ApplicationInfo aInfo = AppGlobals.getPackageManager().getApplicationInfo(str4, 1024, userId5);
                            if (aInfo != null) {
                                sInfo2 = new ServiceInfo(sInfo2);
                                sInfo2.applicationInfo = new ApplicationInfo(sInfo2.applicationInfo);
                                sInfo2.applicationInfo.packageName = aInfo.packageName;
                                sInfo2.applicationInfo.uid = aInfo.uid;
                                name = new ComponentName(aInfo.packageName, name.getClassName());
                                className = new ComponentName(aInfo.packageName, str3 == null ? className.getClassName() : className.getClassName() + ":" + str3);
                                intent2.setComponent(name);
                            } else {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + str4);
                            }
                        } else {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + className + " is not an isolatedProcess");
                        }
                    } else if (isBindExternal) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                    }
                    if (userId5 > 0) {
                        if (this.mAm.isSingleton(sInfo2.processName, sInfo2.applicationInfo, sInfo2.name, sInfo2.flags) && this.mAm.isValidSingletonCall(i3, sInfo2.applicationInfo.uid)) {
                            userId5 = 0;
                            smap = getServiceMapLocked(0);
                        }
                        ServiceInfo sInfo3 = new ServiceInfo(sInfo2);
                        sInfo3.applicationInfo = this.mAm.getAppInfoForUser(sInfo3.applicationInfo, userId5);
                        sInfo = sInfo3;
                        userId3 = userId5;
                        smap2 = smap;
                    } else {
                        sInfo = sInfo2;
                        userId3 = userId5;
                        smap2 = smap;
                    }
                    try {
                        r3 = smap2.mServicesByInstanceName.get(name);
                    } catch (RemoteException e4) {
                        smap3 = smap2;
                        userId4 = userId3;
                        r7 = r7;
                        intent = service;
                        str = callingPackage;
                        i = callingPid;
                        r2 = r7;
                        i2 = callingUid;
                        if (r2 != null) {
                        }
                    }
                    try {
                        if (ActivityManagerDebugConfig.DEBUG_SERVICE && r3 != null) {
                            Slog.v(ActivityManagerService.TAG, "Retrieved via pm by intent: " + r3);
                        }
                        if (r3 != null || !createIfNeeded) {
                            r4 = r3;
                        } else {
                            Intent.FilterComparison filter = new Intent.FilterComparison(service.cloneFilter());
                            ServiceRestarter res = new ServiceRestarter();
                            BatteryStatsImpl stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                            synchronized (stats) {
                                ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, name.getPackageName(), name.getClassName());
                            }
                            ServiceRecord r8 = new ServiceRecord(this.mAm, ss, className, name, definingPackageName, definingUid, filter, sInfo, callingFromFg, res);
                            try {
                                res.setService(r8);
                                smap2.mServicesByInstanceName.put(name, r8);
                                smap2.mServicesByIntent.put(filter, r8);
                                for (int i4 = this.mPendingServices.size() - 1; i4 >= 0; i4--) {
                                    ServiceRecord pr = this.mPendingServices.get(i4);
                                    if (pr.serviceInfo.applicationInfo.uid == sInfo.applicationInfo.uid && pr.instanceName.equals(name)) {
                                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                            Slog.v(ActivityManagerService.TAG, "Remove pending: " + pr);
                                        }
                                        this.mPendingServices.remove(i4);
                                    }
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(ActivityManagerService.TAG, "Retrieve created new service: " + r8);
                                }
                                r4 = r8;
                            } catch (RemoteException e5) {
                                r7 = r8;
                                smap3 = smap2;
                                userId4 = userId3;
                                intent = service;
                                str = callingPackage;
                                i = callingPid;
                                r2 = r7;
                                i2 = callingUid;
                                if (r2 != null) {
                                }
                            }
                        }
                        intent = service;
                        i = callingPid;
                        r2 = r4;
                        userId4 = userId3;
                        str = callingPackage;
                        i2 = callingUid;
                    } catch (RemoteException e6) {
                        smap3 = smap2;
                        userId4 = userId3;
                        r7 = r3;
                        intent = service;
                        str = callingPackage;
                        i = callingPid;
                        r2 = r7;
                        i2 = callingUid;
                        if (r2 != null) {
                        }
                    }
                } catch (RemoteException e7) {
                    r7 = r7;
                    intent = service;
                    str = callingPackage;
                    i = callingPid;
                    r2 = r7;
                    i2 = callingUid;
                    if (r2 != null) {
                    }
                }
            } catch (RemoteException e8) {
                intent = service;
                str = callingPackage;
                i = callingPid;
                r2 = r7;
                i2 = callingUid;
                if (r2 != null) {
                }
            }
        } else {
            if (createIfNeeded) {
                r2 = r7;
                if (r2.serviceInfo != null) {
                    str = callingPackage;
                    intent = service;
                    i = callingPid;
                    userId2 = userId4;
                    i2 = callingUid;
                    if (this.mAm.mHwAMSEx.shouldPreventStartService(r2.serviceInfo, callingPid, callingUid, userId4, callerApp, true, service)) {
                        Slog.w(ActivityManagerService.TAG, "Service starting has been prevented by iaware or trustsbase sInfo " + r2.serviceInfo);
                        return null;
                    }
                } else {
                    intent = service;
                    str = callingPackage;
                    i = callingPid;
                    userId2 = userId4;
                    i2 = callingUid;
                }
            } else {
                intent = service;
                str = callingPackage;
                i = callingPid;
                r2 = r7;
                i2 = callingUid;
                userId2 = userId4;
            }
            userId4 = userId2;
        }
        if (r2 != null) {
            Flog.i(102, "retrieve service " + intent + " U=" + userId4 + ": ret null");
            return null;
        } else if (!this.mAm.validateAssociationAllowedLocked(str, i2, r2.packageName, r2.appInfo.uid)) {
            String msg2 = "association not allowed between packages " + str + " and " + r2.packageName;
            Slog.w(ActivityManagerService.TAG, "Service lookup failed: " + msg2);
            return new ServiceLookupResult(null, msg2);
        } else if (!this.mAm.mIntentFirewall.checkService(r2.name, service, callingUid, callingPid, resolvedType, r2.appInfo)) {
            return new ServiceLookupResult(null, "blocked by firewall");
        } else {
            ActivityManagerService activityManagerService = this.mAm;
            if (ActivityManagerService.checkComponentPermission(r2.permission, i, i2, r2.appInfo.uid, r2.exported) == 0) {
                if (r2.permission == null || str == null) {
                    str2 = null;
                } else {
                    int opCode = AppOpsManager.permissionToOpCode(r2.permission);
                    if (opCode == -1 || this.mAm.mAppOpsService.checkOperation(opCode, i2, str) == 0) {
                        str2 = null;
                    } else {
                        Slog.w(ActivityManagerService.TAG, "Appop Denial: Accessing service " + r2.shortInstanceName + " from pid=" + i + ", uid=" + i2 + " requires appop " + AppOpsManager.opToName(opCode));
                        return null;
                    }
                }
                return new ServiceLookupResult(r2, str2);
            } else if (!r2.exported) {
                Slog.w(ActivityManagerService.TAG, "Permission Denial: Accessing service " + r2.shortInstanceName + " from pid=" + i + ", uid=" + i2 + " that is not exported from uid " + r2.appInfo.uid);
                return new ServiceLookupResult(null, "not exported from uid " + r2.appInfo.uid);
            } else {
                Slog.w(ActivityManagerService.TAG, "Permission Denial: Accessing service " + r2.shortInstanceName + " from pid=" + i + ", uid=" + i2 + " requires " + r2.permission);
                return new ServiceLookupResult(null, r2.permission);
            }
        }
        while (true) {
        }
    }

    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            StringBuilder sb = new StringBuilder();
            sb.append(">>> EXECUTING ");
            sb.append(why);
            sb.append(" of ");
            sb.append(r);
            sb.append(" in app ");
            sb.append(r.app);
            sb.append(", r.executeNesting: ");
            sb.append(r.executeNesting);
            sb.append(", fg: ");
            sb.append(fg);
            sb.append(", r.app.execServicesFg: ");
            int i = 0;
            sb.append(r.app == null ? false : r.app.execServicesFg);
            sb.append(", r.app.executingServices.size: ");
            if (r.app != null) {
                i = r.app.executingServices.size();
            }
            sb.append(i);
            Slog.v(ActivityManagerService.TAG, sb.toString());
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(ActivityManagerService.TAG, ">>> EXECUTING " + why + " of " + r.shortInstanceName);
        }
        boolean timeoutNeeded = true;
        if (this.mAm.mBootPhase < 600 && r.app != null && r.app.pid == Process.myPid()) {
            Slog.w(ActivityManagerService.TAG, "Too early to start/bind service in system_server: Phase=" + this.mAm.mBootPhase + " " + r.getComponentName());
            timeoutNeeded = false;
        }
        long now = SystemClock.uptimeMillis();
        if (r.executeNesting == 0) {
            r.executeFg = fg;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setExecuting(true, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                r.app.executingServices.add(r);
                r.app.execServicesFg |= fg;
                if (timeoutNeeded && r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (r.app != null && fg && !r.app.execServicesFg) {
            r.app.execServicesFg = true;
            if (timeoutNeeded) {
                scheduleServiceTimeoutLocked(r.app);
            }
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.d(ActivityManagerService.TAG, "requestBind " + i + ": requested=" + i.requested + " rebind=" + rebind);
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(11);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.getReportedProcState());
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while binding " + r, e);
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                throw e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while binding " + r);
                }
                boolean inDestroying2 = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying2, inDestroying2);
                return false;
            }
        }
        return true;
    }

    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean canceled;
        long now;
        long now2;
        long minDuration;
        long now3;
        int i = 0;
        if (this.mAm.mAtmInternal.isShuttingDown()) {
            Slog.w(ActivityManagerService.TAG, "Not scheduling restart of crashed service " + r.shortInstanceName + " - system is shutting down");
            return false;
        }
        ServiceMap smap = getServiceMapLocked(r.userId);
        if (smap.mServicesByInstanceName.get(r.instanceName) != r) {
            Slog.wtf(ActivityManagerService.TAG, "Attempting to schedule restart of " + r + " when found in map: " + smap.mServicesByInstanceName.get(r.instanceName));
            return false;
        }
        long now4 = SystemClock.uptimeMillis();
        int i2 = 3;
        if ((r.serviceInfo.applicationInfo.flags & 8) == 0) {
            long minDuration2 = this.mAm.mConstants.SERVICE_RESTART_DURATION;
            long resetTime = this.mAm.mConstants.SERVICE_RESET_RUN_DURATION;
            int N = r.deliveredStarts.size();
            if (N > 0) {
                canceled = false;
                int i3 = N - 1;
                while (i3 >= 0) {
                    ServiceRecord.StartItem si = r.deliveredStarts.get(i3);
                    si.removeUriPermissionsLocked();
                    if (si.intent == null) {
                        now3 = now4;
                    } else if (!allowCancel || (si.deliveryCount < i2 && si.doneExecutingCount < 6)) {
                        r.pendingStarts.add(i, si);
                        now3 = now4;
                        long dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                        if (minDuration2 < dur) {
                            minDuration2 = dur;
                        }
                        if (resetTime < dur) {
                            resetTime = dur;
                        }
                    } else {
                        Slog.w(ActivityManagerService.TAG, "Canceling start item " + si.intent + " in service " + r.shortInstanceName);
                        now3 = now4;
                        canceled = true;
                    }
                    i3--;
                    now4 = now3;
                    i = 0;
                    i2 = 3;
                }
                now2 = now4;
                r.deliveredStarts.clear();
            } else {
                now2 = now4;
                canceled = false;
            }
            r.totalRestartCount++;
            if (r.restartDelay == 0) {
                r.restartCount++;
                r.restartDelay = minDuration2;
            } else if (r.crashCount > 1) {
                r.restartDelay = this.mAm.mConstants.BOUND_SERVICE_CRASH_RESTART_DURATION * ((long) (r.crashCount - 1));
            } else if (now2 > r.restartTime + resetTime) {
                r.restartCount = 1;
                r.restartDelay = minDuration2;
            } else {
                r.restartDelay *= (long) this.mAm.mConstants.SERVICE_RESTART_DURATION_FACTOR;
                if (r.restartDelay < minDuration2) {
                    r.restartDelay = minDuration2;
                }
            }
            r.nextRestartTime = now2 + r.restartDelay;
            while (true) {
                boolean repeat = false;
                long restartTimeBetween = this.mAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN;
                int i4 = this.mRestartingServices.size() - 1;
                while (true) {
                    if (i4 < 0) {
                        minDuration = minDuration2;
                        break;
                    }
                    ServiceRecord r2 = this.mRestartingServices.get(i4);
                    if (r2 != r) {
                        minDuration = minDuration2;
                        if (r.nextRestartTime >= r2.nextRestartTime - restartTimeBetween && r.nextRestartTime < r2.nextRestartTime + restartTimeBetween) {
                            r.nextRestartTime = r2.nextRestartTime + restartTimeBetween;
                            r.restartDelay = r.nextRestartTime - now2;
                            repeat = true;
                            break;
                        }
                    } else {
                        minDuration = minDuration2;
                    }
                    i4--;
                    minDuration2 = minDuration;
                }
                if (!repeat) {
                    break;
                }
                minDuration2 = minDuration;
            }
            now = now2;
        } else {
            r.totalRestartCount++;
            r.restartCount = 0;
            r.restartDelay = 0;
            now = now4;
            r.nextRestartTime = now;
            canceled = false;
        }
        if ("com.tencent.mm".equals(r.packageName) && SMART_CLEANING) {
            r.restartDelay = this.mAm.mConstants.SERVICE_RESTART_DURATION;
            r.nextRestartTime = r.restartDelay + now;
        }
        if (!this.mRestartingServices.contains(r)) {
            r.createdFromFg = false;
            this.mRestartingServices.add(r);
            r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
        }
        cancelForegroundNotificationLocked(r);
        this.mAm.mHandler.removeCallbacks(r.restarter);
        this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
        r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
        Slog.w(ActivityManagerService.TAG, "Scheduling restart of crashed service " + r.shortInstanceName + " in " + r.restartDelay + "ms");
        EventLog.writeEvent((int) EventLogTags.AM_SCHEDULE_SERVICE_RESTART, Integer.valueOf(r.userId), r.shortInstanceName, Long.valueOf(r.restartDelay));
        return canceled;
    }

    /* access modifiers changed from: package-private */
    public final void performServiceRestartLocked(ServiceRecord r) {
        if (!this.mRestartingServices.contains(r)) {
            Flog.i(102, "no need to performServiceRestart for r:" + r);
        } else if (!isServiceNeededLocked(r, false, false)) {
            Slog.wtf(ActivityManagerService.TAG, "Restarting service that is not needed: " + r);
        } else {
            try {
                bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
            } catch (TransactionTooLargeException e) {
                Flog.w(102, "performServiceRestart TransactionTooLarge e:", e);
            }
        }
    }

    private final boolean unscheduleServiceRestartLocked(ServiceRecord r, int callingUid, boolean force) {
        if (!force && r.restartDelay == 0) {
            return false;
        }
        boolean removed = this.mRestartingServices.remove(r);
        if (removed || callingUid != r.appInfo.uid) {
            r.resetRestartCounter();
        }
        if (removed) {
            clearRestartingIfNeededLocked(r);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        return true;
    }

    private void clearRestartingIfNeededLocked(ServiceRecord r) {
        if (r.restartTracker != null) {
            boolean stillTracking = false;
            int i = this.mRestartingServices.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (this.mRestartingServices.get(i).restartTracker == r.restartTracker) {
                    stillTracking = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x02e7  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x02a4  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x02de  */
    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        HostingRecord hostingRecord;
        ProcessRecord app;
        if (r.app == null || r.app.thread == null) {
            if (whileRestarting || !this.mRestartingServices.contains(r)) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Bringing up " + r + " " + r.intent + " fg=" + r.fgRequired);
                }
                if (this.mRestartingServices.remove(r)) {
                    if (HW_PARENT_CONTROL.equals(r.packageName)) {
                        Slog.i(ActivityManagerService.TAG, "parentcontrol reset restart counter");
                        r.resetRestartCounter();
                    }
                    clearRestartingIfNeededLocked(r);
                }
                if (r.delayed) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (bring up): " + r);
                    }
                    getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                    r.delayed = false;
                }
                if (!this.mAm.mUserController.hasStartedUserState(r.userId)) {
                    String msg = "Unable to launch app " + r.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is stopped";
                    Slog.w(ActivityManagerService.TAG, msg);
                    bringDownServiceLocked(r);
                    return msg;
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, false, r.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(ActivityManagerService.TAG, "Failed trying to unstop package " + r.packageName + ": " + e2);
                }
                boolean isolated = (r.serviceInfo.flags & 2) != 0;
                String procName = r.processName;
                HostingRecord hostingRecord2 = new HostingRecord("service", r.instanceName);
                if (!isolated) {
                    ProcessRecord app2 = this.mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("bringUpServiceLocked: appInfo.uid=");
                        sb.append(r.appInfo.uid);
                        sb.append(" app=");
                        sb.append(app2);
                        sb.append(" app.thread=");
                        sb.append(app2 != null ? app2.thread : null);
                        Slog.v(TAG_MU, sb.toString());
                    }
                    if (!(app2 == null || app2.thread == null)) {
                        try {
                            app2.addPackage(r.appInfo.packageName, r.appInfo.longVersionCode, this.mAm.mProcessStats);
                            realStartServiceLocked(r, app2, execInFg);
                            return null;
                        } catch (TransactionTooLargeException e3) {
                            throw e3;
                        } catch (RemoteException e4) {
                            Slog.w(ActivityManagerService.TAG, "Exception when starting service " + r.shortInstanceName, e4);
                        }
                    }
                    hostingRecord = hostingRecord2;
                    app = app2;
                } else {
                    ProcessRecord app3 = r.isolatedProc;
                    if (WebViewZygote.isMultiprocessEnabled() && r.serviceInfo.packageName.equals(WebViewZygote.getPackageName())) {
                        hostingRecord2 = HostingRecord.byWebviewZygote(r.instanceName);
                    }
                    if ((r.serviceInfo.flags & 8) != 0) {
                        hostingRecord = HostingRecord.byAppZygote(r.instanceName, r.definingPackageName, r.definingUid);
                        app = app3;
                    } else {
                        hostingRecord = hostingRecord2;
                        app = app3;
                    }
                }
                if (app == null || this.mAm.mHwAMSEx.needCheckProcDied(app)) {
                    if (!permissionsReviewRequired) {
                        if (whileRestarting) {
                            this.mAm.shouldPreventRestartService(r.serviceInfo, true);
                        }
                        ProcessRecord app4 = this.mAm.mProcessList.startProcessLocked(procName, r.appInfo, true, intentFlags, hostingRecord, false, isolated, 0, false, null, null, initPCEntryArgs(r), null);
                        if (app4 == null) {
                            String msg2 = "Unable to launch app " + r.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.appInfo.uid + " for service " + r.intent.getIntent() + ": process is bad";
                            Slog.w(ActivityManagerService.TAG, msg2);
                            try {
                                bringDownServiceLocked(r);
                            } catch (IllegalStateException e5) {
                                Slog.w(ActivityManagerService.TAG, "Exception when bring down Service " + r.shortInstanceName, e5);
                            }
                            return msg2;
                        }
                        if (isolated) {
                            r.isolatedProc = app4;
                        }
                        if (r.fgRequired) {
                            if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                                Slog.v(ActivityManagerService.TAG, "Whitelisting " + UserHandle.formatUid(r.appInfo.uid) + " for fg-service launch");
                            }
                            this.mAm.tempWhitelistUidLocked(r.appInfo.uid, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, "fg-service-launch");
                        }
                        if (!this.mPendingServices.contains(r)) {
                            this.mPendingServices.add(r);
                        }
                        if (r.delayedStop) {
                            r.delayedStop = false;
                            if (r.startRequested) {
                                if (DEBUG_DELAYED_STARTS) {
                                    Slog.v(ActivityManagerService.TAG, "Applying delayed stop (in bring up): " + r);
                                }
                                stopServiceLocked(r);
                            }
                        }
                        return null;
                    }
                }
                if (r.fgRequired) {
                }
                if (!this.mPendingServices.contains(r)) {
                }
                if (r.delayedStop) {
                }
                return null;
            }
            Flog.i(102, "do nothing when waiting for a restart and Bringing up " + r + " " + r.intent);
            return null;
        } else if (this.mAm.mHwAMSEx.needCheckProcDied(r.app)) {
            return null;
        } else {
            sendServiceArgsLocked(r, execInFg, false);
            return null;
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, r.bindings.valueAt(i), execInFg, false)) {
            i--;
        }
    }

    private final void realStartServiceLocked(ServiceRecord r, ProcessRecord app, boolean execInFg) throws RemoteException {
        String nameTerm;
        boolean z;
        if (app.thread != null) {
            if (ActivityManagerDebugConfig.DEBUG_MU) {
                Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + r.appInfo.uid + ", ProcessRecord.uid = " + app.uid);
            }
            r.setProcess(app);
            long uptimeMillis = SystemClock.uptimeMillis();
            r.lastActivity = uptimeMillis;
            r.restartTime = uptimeMillis;
            boolean newService = app.services.add(r);
            bumpServiceExecutingLocked(r, execInFg, "create");
            this.mAm.updateLruProcessLocked(app, false, null);
            updateServiceForegroundLocked(r.app, false);
            int prevExecuteNesting = r.executeNesting;
            this.mAm.updateOomAdjLocked("updateOomAdj_startService");
            r.executeNesting = prevExecuteNesting;
            try {
                int lastPeriod = r.shortInstanceName.lastIndexOf(46);
                if (lastPeriod >= 0) {
                    nameTerm = r.shortInstanceName.substring(lastPeriod);
                } else {
                    nameTerm = r.shortInstanceName;
                }
                EventLogTags.writeAmCreateService(r.userId, System.identityHashCode(r), nameTerm, r.app.uid, r.app.pid);
                StatsLog.write(100, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName());
                synchronized (r.stats.getBatteryStats()) {
                    r.stats.startLaunchedLocked();
                }
                this.mAm.notifyPackageUse(r.serviceInfo.packageName, 1);
                app.forceProcessStateUpTo(11);
                app.thread.scheduleCreateService(r, r.serviceInfo, this.mAm.compatibilityInfoForPackage(r.serviceInfo.applicationInfo), app.getReportedProcState());
                r.postNotification();
                if (1 == 0) {
                    boolean inDestroying = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                    if (newService) {
                        app.services.remove(r);
                        r.setProcess(null);
                    }
                    if (!inDestroying) {
                        scheduleServiceRestartLocked(r, false);
                    } else {
                        Flog.w(102, "Destroying no retry when creating service " + r);
                    }
                }
                if (r.whitelistManager) {
                    app.whitelistManager = true;
                }
                requestServiceBindingsLocked(r, execInFg);
                updateServiceClientActivitiesLocked(app, null, true);
                if (newService && 1 != 0) {
                    app.addBoundClientUidsOfNewService(r);
                }
                if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
                    r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(), null, null, 0));
                }
                sendServiceArgsLocked(r, execInFg, true);
                if (r.delayed) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (new proc): " + r);
                    }
                    getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                    z = false;
                    r.delayed = false;
                } else {
                    z = false;
                }
                if (r.delayedStop) {
                    r.delayedStop = z;
                    if (r.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(ActivityManagerService.TAG, "Applying delayed stop (from start): " + r);
                        }
                        stopServiceLocked(r);
                    }
                }
            } catch (DeadObjectException e) {
                Slog.w(ActivityManagerService.TAG, "Application dead when creating service " + r);
                this.mAm.appDiedLocked(app);
                throw e;
            } catch (Throwable th) {
                if (0 == 0) {
                    boolean inDestroying2 = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying2, inDestroying2);
                    if (newService) {
                        app.services.remove(r);
                        r.setProcess(null);
                    }
                    if (!inDestroying2) {
                        scheduleServiceRestartLocked(r, false);
                    } else {
                        Flog.w(102, "Destroying no retry when creating service " + r);
                    }
                }
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int N = r.pendingStarts.size();
        if (N != 0) {
            ArrayList<ServiceStartArgs> args = new ArrayList<>();
            while (r.pendingStarts.size() > 0) {
                ServiceRecord.StartItem si = r.pendingStarts.remove(0);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Sending arguments to: " + r + " " + r.intent + " args=" + si.intent);
                }
                if (si.intent != null || N <= 1) {
                    si.deliveredTime = SystemClock.uptimeMillis();
                    r.deliveredStarts.add(si);
                    si.deliveryCount++;
                    if (si.neededGrants != null) {
                        this.mAm.mUgmInternal.grantUriPermissionUncheckedFromIntent(si.neededGrants, si.getUriPermissionsLocked());
                    }
                    this.mAm.grantEphemeralAccessLocked(r.userId, si.intent, UserHandle.getAppId(r.appInfo.uid), UserHandle.getAppId(si.callingId));
                    bumpServiceExecutingLocked(r, execInFg, "start");
                    if (!oomAdjusted) {
                        oomAdjusted = true;
                        this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_startService");
                    }
                    if (r.fgRequired && !r.fgWaiting) {
                        if (!r.isForeground) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Launched service must call startForeground() within timeout: " + r);
                            }
                            scheduleServiceForegroundTransitionTimeoutLocked(r);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Service already foreground; no new timeout: " + r);
                            }
                            r.fgRequired = false;
                        }
                    }
                    int flags = 0;
                    if (si.deliveryCount > 1) {
                        flags = 0 | 2;
                    }
                    if (si.doneExecutingCount > 0) {
                        flags |= 1;
                    }
                    args.add(new ServiceStartArgs(si.taskRemoved, si.id, flags, si.intent));
                }
            }
            ParceledListSlice<ServiceStartArgs> slice = new ParceledListSlice<>(args);
            slice.setInlineCountLimit(4);
            Exception caughtException = null;
            try {
                r.app.thread.scheduleServiceArgs(r, slice);
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Transaction too large for " + args.size() + " args, first: " + args.get(0).args);
                }
                Slog.w(ActivityManagerService.TAG, "Failed delivering service starts", e);
                caughtException = e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while sending args: " + r);
                }
                Slog.w(ActivityManagerService.TAG, "Failed delivering service starts", e2);
                caughtException = e2;
            } catch (Exception e3) {
                Slog.w(ActivityManagerService.TAG, "Unexpected exception", e3);
                caughtException = e3;
            }
            if (caughtException != null) {
                boolean inDestroying = this.mDestroyingServices.contains(r);
                for (int i = 0; i < args.size(); i++) {
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                }
                if (caughtException instanceof TransactionTooLargeException) {
                    throw ((TransactionTooLargeException) caughtException);
                }
            }
        }
    }

    private final boolean isServiceNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (r.startRequested) {
            return true;
        }
        if (!knowConn) {
            hasConn = r.hasAutoCreateConnections();
        }
        if (hasConn) {
            return true;
        }
        return false;
    }

    private final void bringDownServiceIfNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (isServiceNeededLocked(r, knowConn, hasConn)) {
            Flog.i(102, "ServiceNeeded not bring down service:" + r);
        } else if (this.mPendingServices.contains(r)) {
            Flog.i(102, "still in launching not bring down service:" + r);
        } else {
            try {
                bringDownServiceLocked(r);
            } catch (IllegalStateException e) {
                Slog.w(ActivityManagerService.TAG, "Exception when r is illegal!");
            }
        }
    }

    private final void bringDownServiceLocked(ServiceRecord r) {
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        int connectionSize = connections.size();
        if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
            Flog.i(102, "bringDownServiceLocked " + r + ",connection size= " + connectionSize);
        }
        long start = SystemClock.uptimeMillis();
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> c = connections.valueAt(conni);
            for (int i = 0; i < c.size(); i++) {
                ConnectionRecord cr = c.get(i);
                cr.serviceDead = true;
                cr.stopAssociation();
                if (this.mAm.mHwAMSEx.needCheckProcDied(cr.binding == null ? null : cr.binding.client)) {
                    Slog.d(ActivityManagerService.TAG, "client is dead, cr:" + cr);
                } else {
                    try {
                        cr.conn.connected(r.name, (IBinder) null, true);
                    } catch (Exception e) {
                        Slog.w(ActivityManagerService.TAG, "Failure disconnecting service " + r.shortInstanceName + " to connection " + c.get(i).conn.asBinder() + " (in " + c.get(i).binding.client.processName + ")", e);
                    }
                }
            }
        }
        long diff = SystemClock.uptimeMillis() - start;
        if (diff > 1000 && ActivityManagerDebugConfig.HWFLOW) {
            Flog.i(102, "bringDownServiceLocked " + r + ",took " + diff + "ms");
        }
        if (!(r.app == null || r.app.thread == null || this.mAm.mHwAMSEx.needCheckProcDied(r.app))) {
            boolean needOomAdj = false;
            for (int i2 = r.bindings.size() - 1; i2 >= 0; i2--) {
                IntentBindRecord ibr = r.bindings.valueAt(i2);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Bringing down binding " + ibr + ": hasBound=" + ibr.hasBound);
                }
                if (ibr.hasBound) {
                    try {
                        bumpServiceExecutingLocked(r, false, "bring down unbind");
                        needOomAdj = true;
                        ibr.hasBound = false;
                        ibr.requested = false;
                        r.app.thread.scheduleUnbindService(r, ibr.intent.getIntent());
                    } catch (Exception e2) {
                        Slog.w(ActivityManagerService.TAG, "Exception when unbinding service " + r.shortInstanceName, e2);
                        serviceProcessGoneLocked(r);
                    }
                }
            }
            if (needOomAdj) {
                this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
            }
        }
        if (r.fgRequired) {
            Slog.w(ActivityManagerService.TAG, "Bringing down service while still waiting for start foreground: " + r);
            r.fgRequired = false;
            r.fgWaiting = false;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
            }
            this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
            this.mAm.mHandler.removeMessages(66, r);
            if (r.app != null) {
                Message msg = this.mAm.mHandler.obtainMessage(69);
                msg.obj = r.app;
                msg.getData().putCharSequence("servicerecord", r.toString());
                this.mAm.mHandler.sendMessage(msg);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            RuntimeException here = new RuntimeException();
            here.fillInStackTrace();
            Slog.v(ActivityManagerService.TAG, "Bringing down " + r + " " + r.intent, here);
        }
        r.destroyTime = SystemClock.uptimeMillis();
        EventLogTags.writeAmDestroyService(r.userId, System.identityHashCode(r), r.app != null ? r.app.pid : -1);
        ServiceMap smap = getServiceMapLocked(r.userId);
        ServiceRecord found = smap.mServicesByInstanceName.remove(r.instanceName);
        if (found == null || found == r) {
            smap.mServicesByIntent.remove(r.intent);
            r.totalRestartCount = 0;
            unscheduleServiceRestartLocked(r, 0, true);
            for (int i3 = this.mPendingServices.size() - 1; i3 >= 0; i3--) {
                if (this.mPendingServices.get(i3) == r) {
                    this.mPendingServices.remove(i3);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(ActivityManagerService.TAG, "Removed pending: " + r);
                    }
                }
            }
            cancelForegroundNotificationLocked(r);
            if (r.isForeground) {
                decActiveForegroundAppLocked(smap, r);
                ServiceState stracker2 = r.getTracker();
                if (stracker2 != null) {
                    stracker2.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 2);
                LogPower.push(234, r.packageName, r.shortInstanceName, Integer.toString(r.foregroundServiceType), new String[]{Long.toString(r.createRealTime)});
                this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, false);
            }
            r.isForeground = false;
            r.foregroundId = 0;
            r.foregroundNoti = null;
            r.clearDeliveredStartsLocked();
            r.pendingStarts.clear();
            smap.mDelayedStartList.remove(r);
            if (r.app != null) {
                synchronized (r.stats.getBatteryStats()) {
                    r.stats.stopLaunchedLocked();
                }
                r.app.services.remove(r);
                r.app.updateBoundClientUids();
                if (r.whitelistManager) {
                    updateWhitelistManagerLocked(r.app);
                }
                if (r.app.thread != null) {
                    updateServiceForegroundLocked(r.app, false);
                    try {
                        bumpServiceExecutingLocked(r, false, "destroy");
                        this.mDestroyingServices.add(r);
                        r.destroying = true;
                        this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
                        r.app.thread.scheduleStopService(r);
                    } catch (Exception e3) {
                        Slog.w(ActivityManagerService.TAG, "Exception when destroying service " + r.shortInstanceName, e3);
                        serviceProcessGoneLocked(r);
                    }
                } else {
                    Flog.i(102, "Removed service that has no process: " + r);
                }
            } else {
                Flog.i(102, "Removed service that is not running: " + r);
            }
            if (r.bindings.size() > 0) {
                r.bindings.clear();
            }
            if (r.restarter instanceof ServiceRestarter) {
                ((ServiceRestarter) r.restarter).setService(null);
            }
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            if (r.tracker != null) {
                r.tracker.setStarted(false, memFactor, now);
                r.tracker.setBound(false, memFactor, now);
                if (r.executeNesting == 0) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            smap.ensureNotStartingBackgroundLocked(r);
            return;
        }
        smap.mServicesByInstanceName.put(r.instanceName, found);
        throw new IllegalStateException("Bringing down " + r + " but actually running " + found);
    }

    /* access modifiers changed from: package-private */
    public void removeConnectionLocked(ConnectionRecord c, ProcessRecord skipApp, ActivityServiceConnectionsHolder skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        ArrayList<ConnectionRecord> clist = s.getConnections().get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                s.removeConnection(binder);
            }
        }
        b.connections.remove(c);
        c.stopAssociation();
        if (c.activity != null) {
            if (c.activity != skipAct) {
                c.activity.removeConnection(c);
            }
        }
        if (b.client != skipApp) {
            b.client.connections.remove(c);
            ProcessRecord pr = b.client;
            if (!(s.app == null || pr.pid == s.app.pid || s.app.info == null || pr.info == null || s.app.info.packageName == null || s.app.info.packageName.equals(pr.info.packageName))) {
                LogPower.push(167, s.processName, Integer.toString(pr.pid), Integer.toString(s.app.pid), new String[]{"service"});
            }
            if ((c.flags & 8) != 0) {
                b.client.updateHasAboveClientLocked();
            }
            if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                s.updateWhitelistManager();
                if (!s.whitelistManager && s.app != null) {
                    updateWhitelistManagerLocked(s.app);
                }
            }
            if ((c.flags & DumpState.DUMP_DEXOPT) != 0) {
                s.updateHasBindingWhitelistingBgActivityStarts();
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, true);
            }
        }
        ArrayList<ConnectionRecord> clist2 = this.mServiceConnections.get(binder);
        if (clist2 != null) {
            clist2.remove(c);
            if (clist2.size() == 0) {
                this.mServiceConnections.remove(binder);
            }
        }
        this.mAm.stopAssociationLocked(b.client.uid, b.client.processName, s.appInfo.uid, s.appInfo.longVersionCode, s.instanceName, s.processName);
        this.mAm.mHwAMSEx.reportServiceRelationIAware(3, s, b.client, null, b);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (!c.serviceDead) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "Disconnecting binding " + b.intent + ": shouldUnbind=" + b.intent.hasBound);
            }
            if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0 && b.intent.hasBound && !this.mAm.mHwAMSEx.needCheckProcDied(s.app)) {
                try {
                    bumpServiceExecutingLocked(s, false, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 14) {
                        this.mAm.updateLruProcessLocked(s.app, false, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app, true, "updateOomAdj_unbindService");
                    b.intent.hasBound = false;
                    b.intent.doRebind = false;
                    s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
                } catch (Exception e) {
                    Slog.w(ActivityManagerService.TAG, "Exception when unbinding service " + s.shortInstanceName, e);
                    if (!s.app.killedByAm) {
                        serviceProcessGoneLocked(s);
                    }
                }
            }
            this.mPendingServices.remove(s);
            if ((c.flags & 1) != 0) {
                boolean hasAutoCreate = s.hasAutoCreateConnections();
                if (!hasAutoCreate && s.tracker != null) {
                    s.tracker.setBound(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, true, hasAutoCreate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = true;
                if (res == 0 || res == 1) {
                    r.findDeliveredStart(startId, false, true);
                    r.stopIfKilled = false;
                } else if (res == 2) {
                    r.findDeliveredStart(startId, false, true);
                    if (r.getLastStartId() == startId) {
                        r.stopIfKilled = true;
                    }
                } else if (res == 3) {
                    ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
                    if (si != null) {
                        si.deliveryCount = 0;
                        si.doneExecutingCount++;
                        r.stopIfKilled = true;
                    }
                } else if (res == 1000) {
                    r.findDeliveredStart(startId, true, true);
                } else {
                    throw new IllegalArgumentException("Unknown service start result: " + res);
                }
                if (res == 0) {
                    r.callStart = false;
                }
            } else if (type == 2) {
                if (!inDestroying) {
                    if (r.app != null) {
                        Flog.w(102, "Service done with onDestroy, but not inDestroying:" + r + ", app=" + r.app);
                    }
                } else if (r.executeNesting != 1) {
                    Flog.w(102, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                    Slog.w(ActivityManagerService.TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                    r.executeNesting = 1;
                }
            }
            long origId = Binder.clearCallingIdentity();
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            Binder.restoreCallingIdentity(origId);
            return;
        }
        Slog.w(ActivityManagerService.TAG, "Done executing unknown service from pid " + Binder.getCallingPid());
    }

    private void serviceProcessGoneLocked(ServiceRecord r) {
        if (r.tracker != null) {
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            r.tracker.setExecuting(false, memFactor, now);
            r.tracker.setForeground(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            r.tracker.setStarted(false, memFactor, now);
        }
        serviceDoneExecutingLocked(r, true, true);
    }

    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying, boolean finishing) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "<<< DONE EXECUTING " + r + ": nesting=" + r.executeNesting + ", inDestroying=" + inDestroying + ", app=" + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(ActivityManagerService.TAG, "<<< DONE EXECUTING " + r.shortInstanceName);
        }
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Nesting at 0 of " + r.shortInstanceName);
                }
                r.app.execServicesFg = false;
                r.app.executingServices.remove(r);
                if (r.app.executingServices.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
                        Slog.v(ActivityManagerService.TAG, "No more executingServices of " + r.shortInstanceName);
                    }
                    this.mAm.mHandler.removeMessages(12, r.app);
                    this.mAm.mHandler.removeMessages(96, r.app);
                    this.mAm.mHandler.removeMessages(99, r.app);
                } else if (r.executeFg) {
                    int i = r.app.executingServices.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        } else if (r.app.executingServices.valueAt(i).executeFg) {
                            r.app.execServicesFg = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                }
                if (inDestroying) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(ActivityManagerService.TAG, "doneExecuting remove destroying " + r);
                    }
                    this.mDestroyingServices.remove(r);
                    r.bindings.clear();
                }
                this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
            }
            r.executeFg = false;
            if (r.tracker != null) {
                int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
                long now = SystemClock.uptimeMillis();
                r.tracker.setExecuting(false, memFactor, now);
                r.tracker.setForeground(false, memFactor, now);
                if (finishing) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            if (finishing) {
                if (r.app != null && !r.app.isPersistent()) {
                    r.app.services.remove(r);
                    r.app.updateBoundClientUids();
                    if (r.whitelistManager) {
                        updateWhitelistManagerLocked(r.app);
                    }
                }
                r.setProcess(null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean attachApplicationLocked(ProcessRecord proc, String processName) throws RemoteException {
        boolean didSomething = false;
        if (this.mPendingServices.size() > 0) {
            ServiceRecord sr = null;
            int i = 0;
            while (i < this.mPendingServices.size()) {
                try {
                    sr = this.mPendingServices.get(i);
                    if (proc != sr.isolatedProc) {
                        if (proc.uid == sr.appInfo.uid) {
                            if (!processName.equals(sr.processName)) {
                            }
                        }
                        i++;
                    }
                    this.mPendingServices.remove(i);
                    i--;
                    proc.addPackage(sr.appInfo.packageName, sr.appInfo.longVersionCode, this.mAm.mProcessStats);
                    realStartServiceLocked(sr, proc, sr.createdFromFg);
                    didSomething = true;
                    if (!isServiceNeededLocked(sr, false, false)) {
                        bringDownServiceLocked(sr);
                    }
                    i++;
                } catch (RemoteException e) {
                    Slog.w(ActivityManagerService.TAG, "Exception in new application when starting service " + sr.shortInstanceName, e);
                    throw e;
                }
            }
        }
        if (this.mRestartingServices.size() > 0) {
            for (int i2 = 0; i2 < this.mRestartingServices.size(); i2++) {
                ServiceRecord sr2 = this.mRestartingServices.get(i2);
                if (proc == sr2.isolatedProc || (proc.uid == sr2.appInfo.uid && processName.equals(sr2.processName))) {
                    this.mAm.mHandler.removeCallbacks(sr2.restarter);
                    this.mAm.mHandler.post(sr2.restarter);
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void processStartTimedOutLocked(ProcessRecord proc) {
        int i = 0;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = this.mPendingServices.get(i);
            if ((proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName)) || sr.isolatedProc == proc) {
                Slog.w(ActivityManagerService.TAG, "Forcing bringing down service: " + sr);
                sr.isolatedProc = null;
                this.mPendingServices.remove(i);
                i += -1;
                bringDownServiceLocked(sr);
            }
            i++;
        }
    }

    private boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = false;
        for (int i = services.size() - 1; i >= 0; i--) {
            ServiceRecord service = services.valueAt(i);
            if (service != null) {
                if ((packageName == null || (service.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(service.name.getClassName())))) && (service.app == null || evenPersistent || !service.app.isPersistent())) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
                    Slog.i(ActivityManagerService.TAG, "  Force stopping service " + service);
                    if (service.app != null && !service.app.isPersistent()) {
                        service.app.services.remove(service);
                        service.app.updateBoundClientUids();
                        if (service.whitelistManager) {
                            updateWhitelistManagerLocked(service.app);
                        }
                        if (service.app.executingServices.size() == 1 && service.app.executingServices.contains(service)) {
                            Slog.w(ActivityManagerService.TAG, "Remove timeout message for service: " + service);
                            service.app.execServicesFg = false;
                            this.mAm.mHandler.removeMessages(12, service.app);
                            this.mAm.mHandler.removeMessages(99, service.app);
                        }
                        service.app.executingServices.remove(service);
                    }
                    service.setProcess(null);
                    service.isolatedProc = null;
                    if (this.mTmpCollectionResults == null) {
                        this.mTmpCollectionResults = new ArrayList<>();
                    }
                    this.mTmpCollectionResults.add(service);
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean doit) {
        if (Thread.holdsLock(this.mAm)) {
            boolean didSomething = false;
            ArrayList<ServiceRecord> arrayList = this.mTmpCollectionResults;
            if (arrayList != null) {
                arrayList.clear();
            }
            if (userId == -1) {
                for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                    didSomething |= collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, this.mServiceMap.valueAt(i).mServicesByInstanceName);
                    if (!doit && didSomething) {
                        return true;
                    }
                    if (doit && filterByClasses == null) {
                        forceStopPackageLocked(packageName, this.mServiceMap.valueAt(i).mUserId);
                    }
                }
            } else {
                ServiceMap smap = this.mServiceMap.get(userId);
                if (smap != null) {
                    didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, smap.mServicesByInstanceName);
                }
                if (doit && filterByClasses == null) {
                    forceStopPackageLocked(packageName, userId);
                }
            }
            ArrayList<ServiceRecord> arrayList2 = this.mTmpCollectionResults;
            if (arrayList2 != null) {
                for (int i2 = arrayList2.size() - 1; i2 >= 0; i2--) {
                    bringDownServiceLocked(this.mTmpCollectionResults.get(i2));
                }
                this.mTmpCollectionResults.clear();
            }
            return didSomething;
        }
        throw new RuntimeException("Please contact h00523928 for this deliberate crash used to find unlocked path");
    }

    /* access modifiers changed from: package-private */
    public void forceStopPackageLocked(String packageName, int userId) {
        ServiceMap smap = this.mServiceMap.get(userId);
        if (smap != null && smap.mActiveForegroundApps.size() > 0) {
            for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                if (smap.mActiveForegroundApps.valueAt(i).mPackageName.equals(packageName)) {
                    smap.mActiveForegroundApps.removeAt(i);
                    smap.mActiveForegroundAppsChanged = true;
                }
            }
            if (smap.mActiveForegroundAppsChanged) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanUpServices(int userId, ComponentName component, Intent baseIntent) {
        ArrayList<ServiceRecord> services = new ArrayList<>();
        ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(userId);
        for (int i = alls.size() - 1; i >= 0; i--) {
            ServiceRecord sr = alls.valueAt(i);
            if (sr != null && sr.packageName.equals(component.getPackageName())) {
                services.add(sr);
            }
        }
        if (services.size() > 0) {
            LogPower.push(148, "cleanUpservice", component.getPackageName());
        }
        for (int i2 = services.size() - 1; i2 >= 0; i2--) {
            ServiceRecord sr2 = services.get(i2);
            if (sr2.startRequested) {
                if ((sr2.serviceInfo.flags & 1) != 0) {
                    Slog.i(ActivityManagerService.TAG, "Stopping service " + sr2.shortInstanceName + ": remove task");
                    stopServiceLocked(sr2);
                } else {
                    sr2.pendingStarts.add(new ServiceRecord.StartItem(sr2, true, sr2.getLastStartId(), baseIntent, null, 0));
                    if (!(sr2.app == null || sr2.app.thread == null)) {
                        try {
                            sendServiceArgsLocked(sr2, true, false);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v24, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r4v26 */
    /* JADX WARN: Type inference failed for: r4v30 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        ProcessRecord processRecord;
        ServiceMap smap;
        boolean z = true;
        int i = app.connections.size() - 1;
        while (true) {
            processRecord = null;
            if (i < 0) {
                break;
            }
            removeConnectionLocked(app.connections.valueAt(i), app, null);
            i--;
        }
        updateServiceConnectionActivitiesLocked(app);
        app.connections.clear();
        app.whitelistManager = false;
        int i2 = app.services.size() - 1;
        while (i2 >= 0) {
            ServiceRecord sr = app.services.valueAt(i2);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            if (!(sr.app == app || sr.app == null || sr.app.isPersistent())) {
                sr.app.services.remove(sr);
                sr.app.updateBoundClientUids();
            }
            sr.setProcess(processRecord);
            sr.isolatedProc = processRecord;
            sr.executeNesting = 0;
            sr.forceClearTracker();
            if (this.mDestroyingServices.remove(sr) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "killServices remove destroying " + sr);
            }
            int bindingi = sr.bindings.size() - 1;
            ?? r4 = processRecord;
            while (bindingi >= 0) {
                IntentBindRecord b = sr.bindings.valueAt(bindingi);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Killing binding " + b + ": shouldUnbind=" + b.hasBound);
                }
                b.binder = r4;
                b.hasBound = false;
                b.received = false;
                b.requested = false;
                for (int appi = b.apps.size() - 1; appi >= 0; appi--) {
                    ProcessRecord proc = b.apps.keyAt(appi);
                    if (!proc.killedByAm && proc.thread != null) {
                        AppBindRecord abind = b.apps.valueAt(appi);
                        boolean hasCreate = false;
                        int conni = abind.connections.size() - 1;
                        while (true) {
                            if (conni < 0) {
                                break;
                            } else if ((abind.connections.valueAt(conni).flags & 49) == 1) {
                                hasCreate = true;
                                break;
                            } else {
                                conni--;
                            }
                        }
                        if (!hasCreate) {
                        }
                    }
                }
                bindingi--;
                r4 = 0;
            }
            i2--;
            processRecord = null;
        }
        ServiceMap smap2 = getServiceMapLocked(app.userId);
        int i3 = app.services.size() - 1;
        boolean preventRestart = false;
        boolean getPreventResult = false;
        boolean allowRestart2 = allowRestart;
        while (i3 >= 0) {
            ServiceRecord sr2 = app.services.valueAt(i3);
            if (!app.isPersistent()) {
                app.services.removeAt(i3);
                app.updateBoundClientUids();
            }
            ServiceRecord curRec = smap2.mServicesByInstanceName.get(sr2.instanceName);
            if (curRec != sr2) {
                if (curRec != null) {
                    Slog.wtf(ActivityManagerService.TAG, "Service " + sr2 + " in process " + app + " not same as in map: " + curRec);
                    smap = smap2;
                } else {
                    smap = smap2;
                }
            } else if (!allowRestart2 || ((long) sr2.crashCount) < this.mAm.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY || (sr2.serviceInfo.applicationInfo.flags & 8) != 0) {
                if (!allowRestart2) {
                    smap = smap2;
                } else if (!this.mAm.mUserController.isUserRunning(sr2.userId, 0)) {
                    smap = smap2;
                } else {
                    boolean canceled = scheduleServiceRestartLocked(sr2, z);
                    boolean bringDown = false;
                    if (!sr2.startRequested) {
                        smap = smap2;
                    } else if (!sr2.stopIfKilled && !canceled) {
                        smap = smap2;
                    } else if (sr2.pendingStarts.size() == 0) {
                        sr2.startRequested = false;
                        if (sr2.tracker != null) {
                            smap = smap2;
                            sr2.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                        } else {
                            smap = smap2;
                        }
                        if (!sr2.hasAutoCreateConnections()) {
                            bringDownServiceLocked(sr2);
                            bringDown = true;
                        }
                    } else {
                        smap = smap2;
                    }
                    if (!bringDown) {
                        if (!getPreventResult) {
                            preventRestart = this.mAm.shouldPreventRestartService(sr2.serviceInfo, false);
                            getPreventResult = true;
                        }
                        boolean allowRestart3 = !preventRestart;
                        if (!allowRestart3) {
                            bringDownServiceLocked(sr2);
                        }
                        allowRestart2 = allowRestart3;
                    }
                }
                bringDownServiceLocked(sr2);
            } else {
                Slog.w(ActivityManagerService.TAG, "Service crashed " + sr2.crashCount + " times, stopping: " + sr2);
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(sr2.userId);
                Integer valueOf = Integer.valueOf(sr2.crashCount);
                char c = z ? 1 : 0;
                char c2 = z ? 1 : 0;
                char c3 = z ? 1 : 0;
                objArr[c] = valueOf;
                objArr[2] = sr2.shortInstanceName;
                objArr[3] = Integer.valueOf(app.pid);
                EventLog.writeEvent((int) EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, objArr);
                bringDownServiceLocked(sr2);
                smap = smap2;
            }
            i3--;
            smap2 = smap;
            z = true;
        }
        if (!allowRestart2) {
            app.services.clear();
            app.clearBoundClientUids();
            for (int i4 = this.mRestartingServices.size() - 1; i4 >= 0; i4--) {
                ServiceRecord r = this.mRestartingServices.get(i4);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mRestartingServices.remove(i4);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (int i5 = this.mPendingServices.size() - 1; i5 >= 0; i5--) {
                ServiceRecord r2 = this.mPendingServices.get(i5);
                if (r2.processName.equals(app.processName) && r2.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mPendingServices.remove(i5);
                }
            }
        }
        int i6 = this.mDestroyingServices.size();
        while (i6 > 0) {
            i6--;
            ServiceRecord sr3 = this.mDestroyingServices.get(i6);
            if (sr3.app == app) {
                sr3.forceClearTracker();
                this.mDestroyingServices.remove(i6);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "killServices remove destroying " + sr3);
                }
            }
        }
        app.executingServices.clear();
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createRealTime;
        info.started = r.startRequested;
        info.clientCount = r.getConnections().size();
        info.crashCount = r.crashCount;
        info.lastActivityTime = r.lastActivity;
        if (r.isForeground) {
            info.flags |= 2;
        }
        if (r.startRequested) {
            info.flags |= 1;
        }
        if (r.app != null && r.app.pid == ActivityManagerService.MY_PID) {
            info.flags |= 4;
        }
        if (r.app != null && r.app.isPersistent()) {
            info.flags |= 8;
        }
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = connections.valueAt(conni);
            for (int i = 0; i < connl.size(); i++) {
                ConnectionRecord conn = connl.get(i);
                if (conn.clientLabel != 0) {
                    info.clientPackage = conn.binding.client.info.packageName;
                    info.clientLabel = conn.clientLabel;
                    return info;
                }
            }
        }
        return info;
    }

    /* access modifiers changed from: package-private */
    public List<ActivityManager.RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags, int callingUid, boolean allowed, boolean canInteractAcrossUsers) {
        ArrayList<ActivityManager.RunningServiceInfo> res = new ArrayList<>();
        long ident = Binder.clearCallingIdentity();
        int i = 0;
        if (canInteractAcrossUsers) {
            try {
                int[] users = this.mAm.mUserController.getUsers();
                for (int ui = 0; ui < users.length && res.size() < maxNum; ui++) {
                    ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(users[ui]);
                    for (int i2 = 0; i2 < alls.size() && res.size() < maxNum; i2++) {
                        ServiceRecord sr = alls.valueAt(i2);
                        if (sr != null) {
                            res.add(makeRunningServiceInfoLocked(sr));
                        }
                    }
                }
                while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                    ServiceRecord r = this.mRestartingServices.get(i);
                    if (r != null) {
                        ActivityManager.RunningServiceInfo info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                    i++;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            int userId = UserHandle.getUserId(callingUid);
            ArrayMap<ComponentName, ServiceRecord> alls2 = getServicesLocked(userId);
            for (int i3 = 0; i3 < alls2.size() && res.size() < maxNum; i3++) {
                ServiceRecord sr2 = alls2.valueAt(i3);
                if ((allowed || !(sr2 == null || sr2.app == null || sr2.app.uid != callingUid)) && sr2 != null) {
                    res.add(makeRunningServiceInfoLocked(sr2));
                }
            }
            while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                ServiceRecord r2 = this.mRestartingServices.get(i);
                if (r2.userId == userId && (allowed || (r2.app != null && r2.app.uid == callingUid))) {
                    ActivityManager.RunningServiceInfo info2 = makeRunningServiceInfoLocked(r2);
                    info2.restarting = r2.nextRestartTime;
                    res.add(info2);
                }
                i++;
            }
        }
        Binder.restoreCallingIdentity(ident);
        return res;
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByNameLocked(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r == null) {
            return null;
        }
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> conn = connections.valueAt(conni);
            for (int i = 0; i < conn.size(); i++) {
                if (conn.get(i).clientIntent != null) {
                    return conn.get(i).clientIntent;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void serviceTimeout(ProcessRecord proc) {
        Throwable th;
        String anrMessage;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (proc.isDebugging()) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                } else if (proc.executingServices.size() == 0 || proc.thread == null) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                } else {
                    long maxTime = SystemClock.uptimeMillis() - ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
                    ServiceRecord timeout = null;
                    long nextTime = 0;
                    int i = proc.executingServices.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        ServiceRecord sr = proc.executingServices.valueAt(i);
                        if (sr.executingStart < maxTime) {
                            timeout = sr;
                            break;
                        }
                        if (sr.executingStart > nextTime) {
                            nextTime = sr.executingStart;
                        }
                        i--;
                    }
                    if (timeout == null || !this.mAm.mProcessList.mLruProcesses.contains(proc)) {
                        Message msg = this.mAm.mHandler.obtainMessage(12);
                        msg.obj = proc;
                        this.mAm.mHandler.sendMessageAtTime(msg, proc.execServicesFg ? ((long) SERVICE_TIMEOUT) + nextTime : ((long) SERVICE_BACKGROUND_TIMEOUT) + nextTime);
                        anrMessage = null;
                    } else {
                        Slog.w(ActivityManagerService.TAG, "Timeout executing service: " + timeout);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                        pw.println(timeout);
                        timeout.dump(pw, "    ");
                        pw.close();
                        this.mLastAnrDump = sw.toString();
                        this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                        this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                        anrMessage = "executing service " + timeout.shortInstanceName;
                    }
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (anrMessage != null) {
            proc.appNotResponding(null, null, null, null, false, anrMessage);
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceForegroundTimeout(ServiceRecord r) {
        ProcessRecord app;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (r.fgRequired) {
                    if (!r.destroying) {
                        app = r.app;
                        if (app == null || !app.isDebugging()) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Service foreground-required timeout for " + r);
                            }
                            r.fgWaiting = false;
                            stopServiceLocked(r);
                        } else {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                }
                return;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (app != null) {
            app.appNotResponding(null, null, null, null, false, "Context.startForegroundService() did not then call Service.startForeground(): " + r);
        }
    }

    public void updateServiceApplicationInfoLocked(ApplicationInfo applicationInfo) {
        ServiceMap serviceMap = this.mServiceMap.get(UserHandle.getUserId(applicationInfo.uid));
        if (serviceMap != null) {
            ArrayMap<ComponentName, ServiceRecord> servicesByName = serviceMap.mServicesByInstanceName;
            for (int j = servicesByName.size() - 1; j >= 0; j--) {
                ServiceRecord serviceRecord = servicesByName.valueAt(j);
                if (applicationInfo.packageName.equals(serviceRecord.appInfo.packageName)) {
                    serviceRecord.appInfo = applicationInfo;
                    serviceRecord.serviceInfo.applicationInfo = applicationInfo;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceForegroundCrash(ProcessRecord app, CharSequence serviceRecord) {
        ActivityManagerService activityManagerService = this.mAm;
        int i = app.uid;
        int i2 = app.pid;
        String str = app.info.packageName;
        int i3 = app.userId;
        activityManagerService.crashApplication(i, i2, str, i3, "Context.startForegroundService() did not then call Service.startForeground(): " + ((Object) serviceRecord), false);
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() == 0 || proc.thread == null) {
            Flog.i(102, "no need to schedule service timeout");
            return;
        }
        Message msg = this.mAm.mHandler.obtainMessage(12);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg, (long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
        Message msg2 = this.mAm.mHandler.obtainMessage(96);
        msg2.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg2, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceForegroundTransitionTimeoutLocked(ServiceRecord r) {
        if (r.app.executingServices.size() != 0 && r.app.thread != null) {
            Message msg = this.mAm.mHandler.obtainMessage(66);
            msg.obj = r;
            r.fgWaiting = true;
            this.mAm.mHandler.sendMessageDelayed(msg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ActivityManagerService.ItemMatcher matcher;
        private boolean needSep;
        private final long nowReal = SystemClock.elapsedRealtime();
        private boolean printed;
        private boolean printedAnything;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services = new ArrayList<>();
        final /* synthetic */ ActiveServices this$0;

        ServiceDumper(ActiveServices this$02, FileDescriptor fd2, PrintWriter pw2, String[] args2, int opti, boolean dumpAll2, String dumpPackage2) {
            ActiveServices activeServices = this$02;
            this.this$0 = activeServices;
            int i = 0;
            this.needSep = false;
            this.printedAnything = false;
            this.printed = false;
            this.fd = fd2;
            this.pw = pw2;
            this.args = args2;
            this.dumpAll = dumpAll2;
            this.dumpPackage = dumpPackage2;
            this.matcher = new ActivityManagerService.ItemMatcher();
            this.matcher.build(args2, opti);
            int[] users = activeServices.mAm.mUserController.getUsers();
            int length = users.length;
            while (i < length) {
                ServiceMap smap = activeServices.getServiceMapLocked(users[i]);
                if (smap.mServicesByInstanceName.size() > 0) {
                    for (int si = 0; si < smap.mServicesByInstanceName.size(); si++) {
                        ServiceRecord r = smap.mServicesByInstanceName.valueAt(si);
                        if (this.matcher.match(r, r.name) && (dumpPackage2 == null || dumpPackage2.equals(r.appInfo.packageName))) {
                            this.services.add(r);
                        }
                    }
                }
                i++;
                activeServices = this$02;
            }
        }

        private void dumpHeaderLocked() {
            this.pw.println("ACTIVITY MANAGER SERVICES (dumpsys activity services)");
            if (this.this$0.mLastAnrDump != null) {
                this.pw.println("  Last ANR service:");
                this.pw.print(this.this$0.mLastAnrDump);
                this.pw.println();
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpLocked() {
            dumpHeaderLocked();
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            dumpServiceLocalLocked(r);
                        }
                        this.needSep |= this.printed;
                    }
                    dumpUserRemainsLocked(user);
                }
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception in dumpServicesLocked", e);
            }
            dumpRemainsLocked();
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void dumpWithClient() {
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpHeaderLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            synchronized (this.this$0.mAm) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    dumpServiceLocalLocked(r);
                                } catch (Throwable th) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            dumpServiceClient(r);
                        }
                        this.needSep |= this.printed;
                    }
                    synchronized (this.this$0.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            dumpUserRemainsLocked(user);
                        } catch (Throwable th2) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th2;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception in dumpServicesLocked", e);
            }
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpRemainsLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private void dumpUserHeaderLocked(int user) {
            if (!this.printed) {
                if (this.printedAnything) {
                    this.pw.println();
                }
                PrintWriter printWriter = this.pw;
                printWriter.println("  User " + user + " active services:");
                this.printed = true;
            }
            this.printedAnything = true;
            if (this.needSep) {
                this.pw.println();
            }
        }

        private void dumpServiceLocalLocked(ServiceRecord r) {
            dumpUserHeaderLocked(r.userId);
            this.pw.print("  * ");
            this.pw.println(r);
            if (this.dumpAll) {
                r.dump(this.pw, "    ");
                this.needSep = true;
                return;
            }
            this.pw.print("    app=");
            this.pw.println(r.app);
            this.pw.print("    created=");
            TimeUtils.formatDuration(r.createRealTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
            this.pw.println(connections.size());
            if (connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = 0; conni < connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                    for (int i = 0; i < clist.size(); i++) {
                        ConnectionRecord conn = clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(false, false, false, false));
                        this.pw.print(" -> ");
                        ProcessRecord proc = conn.binding.client;
                        this.pw.println(proc != null ? proc.toShortString() : "null");
                    }
                }
            }
        }

        private void dumpServiceClient(ServiceRecord r) {
            IApplicationThread thread;
            ProcessRecord proc = r.app;
            if (proc != null && (thread = proc.thread) != null) {
                this.pw.println("    Client:");
                this.pw.flush();
                try {
                    TransferPipe tp = new TransferPipe();
                    try {
                        thread.dumpService(tp.getWriteFd(), r, this.args);
                        tp.setBufferPrefix("      ");
                        tp.go(this.fd, 2000);
                    } finally {
                        tp.kill();
                    }
                } catch (IOException e) {
                    PrintWriter printWriter = this.pw;
                    printWriter.println("      Failure while dumping the service: " + e);
                } catch (RemoteException e2) {
                    this.pw.println("      Got a RemoteException while dumping the service");
                }
                this.needSep = true;
            }
        }

        private void dumpUserRemainsLocked(int user) {
            String str;
            String str2;
            ServiceMap smap = this.this$0.getServiceMapLocked(user);
            this.printed = false;
            int SN = smap.mDelayedStartList.size();
            for (int si = 0; si < SN; si++) {
                ServiceRecord r = smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && ((str2 = this.dumpPackage) == null || str2.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter = this.pw;
                        printWriter.println("  User " + user + " delayed start services:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = false;
            int SN2 = smap.mStartingBackground.size();
            for (int si2 = 0; si2 < SN2; si2++) {
                ServiceRecord r2 = smap.mStartingBackground.get(si2);
                if (this.matcher.match(r2, r2.name) && ((str = this.dumpPackage) == null || str.equals(r2.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter2 = this.pw;
                        printWriter2.println("  User " + user + " starting in background:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r2);
                }
            }
        }

        private void dumpRemainsLocked() {
            String str;
            String str2;
            String str3;
            if (this.this$0.mPendingServices.size() > 0) {
                this.printed = false;
                for (int i = 0; i < this.this$0.mPendingServices.size(); i++) {
                    ServiceRecord r = this.this$0.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && ((str3 = this.dumpPackage) == null || str3.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Pending services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Pending ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mRestartingServices.size() > 0) {
                this.printed = false;
                for (int i2 = 0; i2 < this.this$0.mRestartingServices.size(); i2++) {
                    ServiceRecord r2 = this.this$0.mRestartingServices.get(i2);
                    if (this.matcher.match(r2, r2.name) && ((str2 = this.dumpPackage) == null || str2.equals(r2.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Restarting services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Restarting ");
                        this.pw.println(r2);
                        r2.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mDestroyingServices.size() > 0) {
                this.printed = false;
                for (int i3 = 0; i3 < this.this$0.mDestroyingServices.size(); i3++) {
                    ServiceRecord r3 = this.this$0.mDestroyingServices.get(i3);
                    if (this.matcher.match(r3, r3.name) && ((str = this.dumpPackage) == null || str.equals(r3.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Destroying services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Destroy ");
                        this.pw.println(r3);
                        r3.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.dumpAll) {
                this.printed = false;
                for (int ic = 0; ic < this.this$0.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r4 = this.this$0.mServiceConnections.valueAt(ic);
                    for (int i4 = 0; i4 < r4.size(); i4++) {
                        ConnectionRecord cr = r4.get(i4);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && this.dumpPackage.equals(cr.binding.client.info.packageName)))) {
                            this.printedAnything = true;
                            if (!this.printed) {
                                if (this.needSep) {
                                    this.pw.println();
                                }
                                this.needSep = true;
                                this.pw.println("  Connection bindings to services:");
                                this.printed = true;
                            }
                            this.pw.print("  * ");
                            this.pw.println(cr);
                            cr.dump(this.pw, "    ");
                        }
                    }
                }
            }
            if (this.matcher.all) {
                long nowElapsed = SystemClock.elapsedRealtime();
                int[] users = this.this$0.mAm.mUserController.getUsers();
                for (int user : users) {
                    boolean printedUser = false;
                    ServiceMap smap = this.this$0.mServiceMap.get(user);
                    if (smap != null) {
                        for (int i5 = smap.mActiveForegroundApps.size() - 1; i5 >= 0; i5--) {
                            ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i5);
                            String str4 = this.dumpPackage;
                            if (str4 == null || str4.equals(aa.mPackageName)) {
                                if (!printedUser) {
                                    printedUser = true;
                                    this.printedAnything = true;
                                    if (this.needSep) {
                                        this.pw.println();
                                    }
                                    this.needSep = true;
                                    this.pw.print("Active foreground apps - user ");
                                    this.pw.print(user);
                                    this.pw.println(":");
                                }
                                this.pw.print("  #");
                                this.pw.print(i5);
                                this.pw.print(": ");
                                this.pw.println(aa.mPackageName);
                                if (aa.mLabel != null) {
                                    this.pw.print("    mLabel=");
                                    this.pw.println(aa.mLabel);
                                }
                                this.pw.print("    mNumActive=");
                                this.pw.print(aa.mNumActive);
                                this.pw.print(" mAppOnTop=");
                                this.pw.print(aa.mAppOnTop);
                                this.pw.print(" mShownWhileTop=");
                                this.pw.print(aa.mShownWhileTop);
                                this.pw.print(" mShownWhileScreenOn=");
                                this.pw.println(aa.mShownWhileScreenOn);
                                this.pw.print("    mStartTime=");
                                TimeUtils.formatDuration(aa.mStartTime - nowElapsed, this.pw);
                                this.pw.print(" mStartVisibleTime=");
                                TimeUtils.formatDuration(aa.mStartVisibleTime - nowElapsed, this.pw);
                                this.pw.println();
                                if (aa.mEndTime != 0) {
                                    this.pw.print("    mEndTime=");
                                    TimeUtils.formatDuration(aa.mEndTime - nowElapsed, this.pw);
                                    this.pw.println();
                                }
                            }
                        }
                        if (smap.hasMessagesOrCallbacks()) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.printedAnything = true;
                            this.needSep = true;
                            this.pw.print("  Handler - user ");
                            this.pw.print(user);
                            this.pw.println(":");
                            smap.dumpMine(new PrintWriterPrinter(this.pw), "    ");
                        }
                    }
                }
            }
            if (!this.printedAnything) {
                this.pw.println("  (nothing)");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        return new ServiceDumper(this, fd, pw, args, opti, dumpAll, dumpPackage);
    }

    /* access modifiers changed from: protected */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long outterToken = proto.start(fieldId);
                int[] users = this.mAm.mUserController.getUsers();
                int length = users.length;
                int i2 = 0;
                int i3 = 0;
                while (i3 < length) {
                    int user = users[i3];
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap == null) {
                        i = i3;
                    } else {
                        long token = proto.start(2246267895809L);
                        proto.write(1120986464257L, user);
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByInstanceName;
                        int i4 = i2;
                        while (i4 < alls.size()) {
                            alls.valueAt(i4).writeToProto(proto, 2246267895810L);
                            i4++;
                            i3 = i3;
                        }
                        i = i3;
                        proto.end(token);
                    }
                    i3 = i + 1;
                    i2 = 0;
                }
                proto.end(outterToken);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<ServiceRecord> services = new ArrayList<>();
        Predicate<ServiceRecord> filter = DumpUtils.filterRecord(name);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int user : this.mAm.mUserController.getUsers()) {
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap != null) {
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByInstanceName;
                        for (int i = 0; i < alls.size(); i++) {
                            ServiceRecord r1 = alls.valueAt(i);
                            if (filter.test(r1)) {
                                services.add(r1);
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (services.size() <= 0) {
            return false;
        }
        services.sort(Comparator.comparing($$Lambda$Y_KRxxoOXfyYceuDG7WHd46Y_I.INSTANCE));
        boolean needSep = false;
        int i2 = 0;
        while (i2 < services.size()) {
            if (needSep) {
                pw.println();
            }
            dumpService("", fd, pw, services.get(i2), args, dumpAll);
            i2++;
            needSep = true;
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    private void dumpService(String prefix, FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("SERVICE ");
                pw.print(r.shortInstanceName);
                pw.print(" ");
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.app != null) {
                    pw.println(r.app.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (r.app != null && r.app.thread != null) {
            pw.print(prefix);
            pw.println("  Client:");
            pw.flush();
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    r.app.thread.dumpService(tp.getWriteFd(), r, args);
                    tp.setBufferPrefix(prefix + "    ");
                    tp.go(fd);
                } finally {
                    tp.kill();
                }
            } catch (IOException e) {
                pw.println(prefix + "    Failure while dumping the service: " + e);
            } catch (RemoteException e2) {
                pw.println(prefix + "    Got a RemoteException while dumping the service");
            }
        }
    }

    private boolean isServiceProxy(String callingPackage, int callingUid, String action, ServiceRecord record) {
        PGManagerInternal pgm;
        if (callingPackage == null) {
            return false;
        }
        if ((PackageManagerService.PLATFORM_PACKAGE_NAME.equals(callingPackage) && ACTION_HWPUSH_MSG_EVENT.equals(action)) || "com.android.systemui".equals(callingPackage)) {
            return false;
        }
        if ((!"com.huawei.harmonyos.foundation".equals(callingPackage) || callingUid != 1000) && this.mAm.getUidState(callingUid) != 2 && (pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class)) != null && pgm.isServiceProxy(record.name, null) && (!callingPackage.equals(record.name.getPackageName()) || pgm.isServiceProxySelf(record.name.getPackageName()))) {
            return true;
        }
        return false;
    }

    private String[] initPCEntryArgs(ServiceRecord r) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return null;
        }
        String[] entryPointArgs = null;
        if (TextUtils.equals(r.intent.getIntent().getAction(), "android.view.InputMethod")) {
            WindowManagerInternal wmi = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            entryPointArgs = (wmi == null || !HwPCUtils.enabledInPad()) ? (wmi == null || !wmi.isHardKeyboardAvailable()) ? HwPCUtils.mTouchDeviceID != -1 ? new String[]{String.valueOf(this.mAm.mWindowManager.getFocusedDisplayId()), String.valueOf(HwPCUtils.mTouchDeviceID)} : new String[]{String.valueOf(0)} : new String[]{String.valueOf(this.mAm.mWindowManager.getFocusedDisplayId())} : new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
        }
        return (TextUtils.equals(((ApplicationInfo) r.appInfo).packageName, "com.huawei.desktop.systemui") || TextUtils.equals(r.appInfo.packageName, "com.huawei.desktop.explorer")) ? new String[]{String.valueOf(-HwPCUtils.getPCDisplayID())} : entryPointArgs;
    }
}
