package com.android.server.am;

import android.app.INotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;
import com.android.server.LocalServices;
import com.android.server.notification.NotificationManagerInternal;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ServiceRecord extends Binder {
    static final int MAX_DELIVERY_COUNT = 3;
    static final int MAX_DONE_EXECUTING_COUNT = 6;
    private static final String TAG = null;
    final ActivityManagerService ams;
    public ProcessRecord app;
    final ApplicationInfo appInfo;
    final ArrayMap<FilterComparison, IntentBindRecord> bindings;
    boolean callStart;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections;
    int crashCount;
    final long createTime;
    boolean createdFromFg;
    boolean delayed;
    boolean delayedStop;
    final ArrayList<StartItem> deliveredStarts;
    long destroyTime;
    boolean destroying;
    boolean executeFg;
    int executeNesting;
    long executingStart;
    final boolean exported;
    int foregroundId;
    Notification foregroundNoti;
    final FilterComparison intent;
    boolean isForeground;
    ProcessRecord isolatedProc;
    long lastActivity;
    private int lastStartId;
    final ComponentName name;
    long nextRestartTime;
    final String packageName;
    final ArrayList<StartItem> pendingStarts;
    final String permission;
    final String processName;
    int restartCount;
    long restartDelay;
    long restartTime;
    ServiceState restartTracker;
    final Runnable restarter;
    final ServiceInfo serviceInfo;
    final String shortName;
    boolean startRequested;
    long startingBgTimeout;
    final Serv stats;
    boolean stopIfKilled;
    String stringName;
    int totalRestartCount;
    ServiceState tracker;
    final int userId;
    boolean whitelistManager;

    /* renamed from: com.android.server.am.ServiceRecord.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Notification val$_foregroundNoti;
        final /* synthetic */ int val$appPid;
        final /* synthetic */ int val$appUid;
        final /* synthetic */ int val$localForegroundId;
        final /* synthetic */ String val$localPackageName;

        AnonymousClass1(String val$localPackageName, Notification val$_foregroundNoti, int val$appUid, int val$appPid, int val$localForegroundId) {
            this.val$localPackageName = val$localPackageName;
            this.val$_foregroundNoti = val$_foregroundNoti;
            this.val$appUid = val$appUid;
            this.val$appPid = val$appPid;
            this.val$localForegroundId = val$localForegroundId;
        }

        public void run() {
            NotificationManagerInternal nm = (NotificationManagerInternal) LocalServices.getService(NotificationManagerInternal.class);
            if (nm == null) {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "postNotification nm no ready for " + this.val$localPackageName);
                return;
            }
            Notification localForegroundNoti = this.val$_foregroundNoti;
            try {
                if (localForegroundNoti.getSmallIcon() == null) {
                    Slog.v(ServiceRecord.TAG, "Attempted to start a foreground service (" + ServiceRecord.this.name + ") with a broken notification (no icon: " + localForegroundNoti + ")");
                    CharSequence appName = ServiceRecord.this.appInfo.loadLabel(ServiceRecord.this.ams.mContext.getPackageManager());
                    if (appName == null) {
                        appName = ServiceRecord.this.appInfo.packageName;
                    }
                    try {
                        Builder builder = new Builder(ServiceRecord.this.ams.mContext.createPackageContextAsUser(ServiceRecord.this.appInfo.packageName, 0, new UserHandle(UserHandle.getUserId(this.val$appUid))));
                        builder.setSmallIcon(ServiceRecord.this.appInfo.icon);
                        builder.setFlag(64, true);
                        builder.setPriority(-2);
                        Intent runningIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        runningIntent.setData(Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, ServiceRecord.this.appInfo.packageName, null));
                        PendingIntent pi = PendingIntent.getActivityAsUser(ServiceRecord.this.ams.mContext, 0, runningIntent, 134217728, null, UserHandle.CURRENT);
                        builder.setColor(ServiceRecord.this.ams.mContext.getColor(17170519));
                        builder = builder;
                        builder.setContentTitle(ServiceRecord.this.ams.mContext.getString(17040237, new Object[]{appName}));
                        builder = builder;
                        builder.setContentText(ServiceRecord.this.ams.mContext.getString(17040238, new Object[]{appName}));
                        builder.setContentIntent(pi);
                        localForegroundNoti = builder.build();
                    } catch (Throwable e) {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "postNotification NameNotFoundException error:", e);
                    }
                }
                if (localForegroundNoti.getSmallIcon() == null) {
                    throw new RuntimeException("invalid service notification: " + ServiceRecord.this.foregroundNoti);
                }
                NotificationManagerInternal notificationManagerInternal = nm;
                notificationManagerInternal.enqueueNotification(this.val$localPackageName, this.val$localPackageName, this.val$appUid, this.val$appPid, null, this.val$localForegroundId, localForegroundNoti, new int[1], ServiceRecord.this.userId);
                ServiceRecord.this.foregroundNoti = localForegroundNoti;
            } catch (Throwable e2) {
                Slog.w(ServiceRecord.TAG, "Error showing notification for service", e2);
                ServiceRecord.this.ams.setServiceForeground(ServiceRecord.this.name, ServiceRecord.this, 0, null, 0);
                ServiceRecord.this.ams.crashApplication(this.val$appUid, this.val$appPid, this.val$localPackageName, "Bad notification for startForeground: " + e2);
            }
        }
    }

    /* renamed from: com.android.server.am.ServiceRecord.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ int val$localForegroundId;
        final /* synthetic */ String val$localPackageName;

        AnonymousClass2(String val$localPackageName, int val$localForegroundId) {
            this.val$localPackageName = val$localPackageName;
            this.val$localForegroundId = val$localForegroundId;
        }

        public void run() {
            INotificationManager inm = NotificationManager.getService();
            if (inm == null) {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "cancelNotification no inm for " + this.val$localPackageName);
                return;
            }
            try {
                inm.cancelNotificationWithTag(this.val$localPackageName, null, this.val$localForegroundId, ServiceRecord.this.userId);
            } catch (RuntimeException e) {
                Slog.w(ServiceRecord.TAG, "Error canceling notification for service", e);
            } catch (RemoteException e2) {
            }
        }
    }

    /* renamed from: com.android.server.am.ServiceRecord.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ int val$localForegroundId;
        final /* synthetic */ String val$localPackageName;
        final /* synthetic */ int val$localUserId;

        AnonymousClass3(String val$localPackageName, int val$localForegroundId, int val$localUserId) {
            this.val$localPackageName = val$localPackageName;
            this.val$localForegroundId = val$localForegroundId;
            this.val$localUserId = val$localUserId;
        }

        public void run() {
            NotificationManagerInternal nmi = (NotificationManagerInternal) LocalServices.getService(NotificationManagerInternal.class);
            if (nmi == null) {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "stripForegroundServiceFlagFromNotification no nmi for " + this.val$localPackageName);
            } else {
                nmi.removeForegroundServiceFlagFromNotification(this.val$localPackageName, this.val$localForegroundId, this.val$localUserId);
            }
        }
    }

    static class StartItem {
        long deliveredTime;
        int deliveryCount;
        int doneExecutingCount;
        final int id;
        final Intent intent;
        final NeededUriGrants neededGrants;
        final ServiceRecord sr;
        String stringName;
        final boolean taskRemoved;
        UriPermissionOwner uriPermissions;

        StartItem(ServiceRecord _sr, boolean _taskRemoved, int _id, Intent _intent, NeededUriGrants _neededGrants) {
            this.sr = _sr;
            this.taskRemoved = _taskRemoved;
            this.id = _id;
            this.intent = _intent;
            this.neededGrants = _neededGrants;
        }

        UriPermissionOwner getUriPermissionsLocked() {
            if (this.uriPermissions == null) {
                this.uriPermissions = new UriPermissionOwner(this.sr.ams, this);
            }
            return this.uriPermissions;
        }

        void removeUriPermissionsLocked() {
            if (this.uriPermissions != null) {
                this.uriPermissions.removeUriPermissionsLocked();
                this.uriPermissions = null;
            }
        }

        public String toString() {
            if (this.stringName != null) {
                return this.stringName;
            }
            StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append("ServiceRecord{").append(Integer.toHexString(System.identityHashCode(this.sr))).append(' ').append(this.sr.shortName).append(" StartItem ").append(Integer.toHexString(System.identityHashCode(this))).append(" id=").append(this.id).append('}');
            String stringBuilder = sb.toString();
            this.stringName = stringBuilder;
            return stringBuilder;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ServiceRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ServiceRecord.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ServiceRecord.<clinit>():void");
    }

    void dumpStartList(PrintWriter pw, String prefix, List<StartItem> list, long now) {
        int N = list.size();
        for (int i = 0; i < N; i++) {
            StartItem si = (StartItem) list.get(i);
            pw.print(prefix);
            pw.print("#");
            pw.print(i);
            pw.print(" id=");
            pw.print(si.id);
            if (now != 0) {
                pw.print(" dur=");
                TimeUtils.formatDuration(si.deliveredTime, now, pw);
            }
            if (si.deliveryCount != 0) {
                pw.print(" dc=");
                pw.print(si.deliveryCount);
            }
            if (si.doneExecutingCount != 0) {
                pw.print(" dxc=");
                pw.print(si.doneExecutingCount);
            }
            pw.println("");
            pw.print(prefix);
            pw.print("  intent=");
            if (si.intent != null) {
                pw.println(si.intent.toString());
            } else {
                pw.println("null");
            }
            if (si.neededGrants != null) {
                pw.print(prefix);
                pw.print("  neededGrants=");
                pw.println(si.neededGrants);
            }
            if (si.uriPermissions != null) {
                si.uriPermissions.dump(pw, prefix);
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
        int i;
        int conni;
        ArrayList<ConnectionRecord> c;
        pw.print(prefix);
        pw.print("intent={");
        pw.print(this.intent.getIntent().toShortString(true, true, false, true));
        pw.println('}');
        pw.print(prefix);
        pw.print("packageName=");
        pw.println(this.packageName);
        pw.print(prefix);
        pw.print("processName=");
        pw.println(this.processName);
        if (this.permission != null) {
            pw.print(prefix);
            pw.print("permission=");
            pw.println(this.permission);
        }
        long now = SystemClock.uptimeMillis();
        long nowReal = SystemClock.elapsedRealtime();
        if (this.appInfo != null) {
            pw.print(prefix);
            pw.print("baseDir=");
            pw.println(this.appInfo.sourceDir);
            if (!Objects.equals(this.appInfo.sourceDir, this.appInfo.publicSourceDir)) {
                pw.print(prefix);
                pw.print("resDir=");
                pw.println(this.appInfo.publicSourceDir);
            }
            pw.print(prefix);
            pw.print("dataDir=");
            pw.println(this.appInfo.dataDir);
        }
        pw.print(prefix);
        pw.print("app=");
        pw.println(this.app);
        if (this.isolatedProc != null) {
            pw.print(prefix);
            pw.print("isolatedProc=");
            pw.println(this.isolatedProc);
        }
        if (this.whitelistManager) {
            pw.print(prefix);
            pw.print("whitelistManager=");
            pw.println(this.whitelistManager);
        }
        if (this.delayed) {
            pw.print(prefix);
            pw.print("delayed=");
            pw.println(this.delayed);
        }
        if (this.isForeground || this.foregroundId != 0) {
            pw.print(prefix);
            pw.print("isForeground=");
            pw.print(this.isForeground);
            pw.print(" foregroundId=");
            pw.print(this.foregroundId);
            pw.print(" foregroundNoti=");
            pw.println(this.foregroundNoti);
        }
        pw.print(prefix);
        pw.print("createTime=");
        TimeUtils.formatDuration(this.createTime, nowReal, pw);
        pw.print(" startingBgTimeout=");
        TimeUtils.formatDuration(this.startingBgTimeout, now, pw);
        pw.println();
        pw.print(prefix);
        pw.print("lastActivity=");
        TimeUtils.formatDuration(this.lastActivity, now, pw);
        pw.print(" restartTime=");
        TimeUtils.formatDuration(this.restartTime, now, pw);
        pw.print(" createdFromFg=");
        pw.println(this.createdFromFg);
        if (this.startRequested || this.delayedStop || this.lastStartId != 0) {
            pw.print(prefix);
            pw.print("startRequested=");
            pw.print(this.startRequested);
            pw.print(" delayedStop=");
            pw.print(this.delayedStop);
            pw.print(" stopIfKilled=");
            pw.print(this.stopIfKilled);
            pw.print(" callStart=");
            pw.print(this.callStart);
            pw.print(" lastStartId=");
            pw.println(this.lastStartId);
        }
        if (this.executeNesting != 0) {
            pw.print(prefix);
            pw.print("executeNesting=");
            pw.print(this.executeNesting);
            pw.print(" executeFg=");
            pw.print(this.executeFg);
            pw.print(" executingStart=");
            TimeUtils.formatDuration(this.executingStart, now, pw);
            pw.println();
        }
        if (this.destroying || this.destroyTime != 0) {
            pw.print(prefix);
            pw.print("destroying=");
            pw.print(this.destroying);
            pw.print(" destroyTime=");
            TimeUtils.formatDuration(this.destroyTime, now, pw);
            pw.println();
        }
        if (this.crashCount == 0 && this.restartCount == 0 && this.restartDelay == 0) {
            if (this.nextRestartTime != 0) {
            }
            if (this.deliveredStarts.size() > 0) {
                pw.print(prefix);
                pw.println("Delivered Starts:");
                dumpStartList(pw, prefix, this.deliveredStarts, now);
            }
            if (this.pendingStarts.size() > 0) {
                pw.print(prefix);
                pw.println("Pending Starts:");
                dumpStartList(pw, prefix, this.pendingStarts, 0);
            }
            if (this.bindings.size() > 0) {
                pw.print(prefix);
                pw.println("Bindings:");
                for (i = 0; i < this.bindings.size(); i++) {
                    IntentBindRecord b;
                    b = (IntentBindRecord) this.bindings.valueAt(i);
                    pw.print(prefix);
                    pw.print("* IntentBindRecord{");
                    pw.print(Integer.toHexString(System.identityHashCode(b)));
                    if ((b.collectFlags() & 1) != 0) {
                        pw.append(" CREATE");
                    }
                    pw.println("}:");
                    b.dumpInService(pw, prefix + "  ");
                }
            }
            if (this.connections.size() > 0) {
                pw.print(prefix);
                pw.println("All Connections:");
                for (conni = 0; conni < this.connections.size(); conni++) {
                    c = (ArrayList) this.connections.valueAt(conni);
                    for (i = 0; i < c.size(); i++) {
                        pw.print(prefix);
                        pw.print("  ");
                        pw.println(c.get(i));
                    }
                }
            }
        }
        pw.print(prefix);
        pw.print("restartCount=");
        pw.print(this.restartCount);
        pw.print(" restartDelay=");
        TimeUtils.formatDuration(this.restartDelay, now, pw);
        pw.print(" nextRestartTime=");
        TimeUtils.formatDuration(this.nextRestartTime, now, pw);
        pw.print(" crashCount=");
        pw.println(this.crashCount);
        if (this.deliveredStarts.size() > 0) {
            pw.print(prefix);
            pw.println("Delivered Starts:");
            dumpStartList(pw, prefix, this.deliveredStarts, now);
        }
        if (this.pendingStarts.size() > 0) {
            pw.print(prefix);
            pw.println("Pending Starts:");
            dumpStartList(pw, prefix, this.pendingStarts, 0);
        }
        if (this.bindings.size() > 0) {
            pw.print(prefix);
            pw.println("Bindings:");
            for (i = 0; i < this.bindings.size(); i++) {
                b = (IntentBindRecord) this.bindings.valueAt(i);
                pw.print(prefix);
                pw.print("* IntentBindRecord{");
                pw.print(Integer.toHexString(System.identityHashCode(b)));
                if ((b.collectFlags() & 1) != 0) {
                    pw.append(" CREATE");
                }
                pw.println("}:");
                b.dumpInService(pw, prefix + "  ");
            }
        }
        if (this.connections.size() > 0) {
            pw.print(prefix);
            pw.println("All Connections:");
            for (conni = 0; conni < this.connections.size(); conni++) {
                c = (ArrayList) this.connections.valueAt(conni);
                for (i = 0; i < c.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(c.get(i));
                }
            }
        }
    }

    ServiceRecord(ActivityManagerService ams, Serv servStats, ComponentName name, FilterComparison intent, ServiceInfo sInfo, boolean callerIsFg, Runnable restarter) {
        this.bindings = new ArrayMap();
        this.connections = new ArrayMap();
        this.deliveredStarts = new ArrayList();
        this.pendingStarts = new ArrayList();
        this.ams = ams;
        this.stats = servStats;
        this.name = name;
        this.shortName = name.flattenToShortString();
        this.intent = intent;
        this.serviceInfo = sInfo;
        this.appInfo = sInfo.applicationInfo;
        this.packageName = sInfo.applicationInfo.packageName;
        this.processName = sInfo.processName;
        this.permission = sInfo.permission;
        this.exported = sInfo.exported;
        this.restarter = restarter;
        this.createTime = SystemClock.elapsedRealtime();
        this.lastActivity = SystemClock.uptimeMillis();
        this.userId = UserHandle.getUserId(this.appInfo.uid);
        this.createdFromFg = callerIsFg;
    }

    public ServiceState getTracker() {
        if (this.tracker != null) {
            return this.tracker;
        }
        if ((this.serviceInfo.applicationInfo.flags & 8) == 0) {
            this.tracker = this.ams.mProcessStats.getServiceStateLocked(this.serviceInfo.packageName, this.serviceInfo.applicationInfo.uid, this.serviceInfo.applicationInfo.versionCode, this.serviceInfo.processName, this.serviceInfo.name);
            this.tracker.applyNewOwner(this);
        }
        return this.tracker;
    }

    public void forceClearTracker() {
        if (this.tracker != null) {
            this.tracker.clearCurrentOwner(this, true);
            this.tracker = null;
        }
    }

    public void makeRestarting(int memFactor, long now) {
        if (this.restartTracker == null) {
            if ((this.serviceInfo.applicationInfo.flags & 8) == 0) {
                this.restartTracker = this.ams.mProcessStats.getServiceStateLocked(this.serviceInfo.packageName, this.serviceInfo.applicationInfo.uid, this.serviceInfo.applicationInfo.versionCode, this.serviceInfo.processName, this.serviceInfo.name);
            }
            if (this.restartTracker == null) {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "makeRestarting restartTracker null for service " + this.serviceInfo.name);
                return;
            }
        }
        this.restartTracker.setRestarting(true, memFactor, now);
    }

    public AppBindRecord retrieveAppBindingLocked(Intent intent, ProcessRecord app) {
        FilterComparison filter = new FilterComparison(intent);
        IntentBindRecord i = (IntentBindRecord) this.bindings.get(filter);
        if (i == null) {
            i = new IntentBindRecord(this, filter);
            this.bindings.put(filter, i);
        }
        AppBindRecord a = (AppBindRecord) i.apps.get(app);
        if (a != null) {
            return a;
        }
        a = new AppBindRecord(this, i, app);
        i.apps.put(app, a);
        return a;
    }

    public boolean hasAutoCreateConnections() {
        for (int conni = this.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> cr = (ArrayList) this.connections.valueAt(conni);
            for (int i = 0; i < cr.size(); i++) {
                if ((((ConnectionRecord) cr.get(i)).flags & 1) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateWhitelistManager() {
        this.whitelistManager = false;
        for (int conni = this.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> cr = (ArrayList) this.connections.valueAt(conni);
            for (int i = 0; i < cr.size(); i++) {
                if ((((ConnectionRecord) cr.get(i)).flags & 16777216) != 0) {
                    this.whitelistManager = true;
                    return;
                }
            }
        }
    }

    public void resetRestartCounter() {
        this.restartCount = 0;
        this.restartDelay = 0;
        this.restartTime = 0;
    }

    public StartItem findDeliveredStart(int id, boolean remove) {
        int N = this.deliveredStarts.size();
        for (int i = 0; i < N; i++) {
            StartItem si = (StartItem) this.deliveredStarts.get(i);
            if (si.id == id) {
                if (remove) {
                    this.deliveredStarts.remove(i);
                }
                return si;
            }
        }
        return null;
    }

    public int getLastStartId() {
        return this.lastStartId;
    }

    public int makeNextStartId() {
        this.lastStartId++;
        if (this.lastStartId < 1) {
            this.lastStartId = 1;
        }
        return this.lastStartId;
    }

    public void postNotification() {
        int appUid = this.appInfo.uid;
        int appPid = this.app.pid;
        if (this.foregroundId != 0 && this.foregroundNoti != null) {
            String localPackageName = this.packageName;
            int localForegroundId = this.foregroundId;
            this.ams.mHandler.post(new AnonymousClass1(localPackageName, this.foregroundNoti, appUid, appPid, localForegroundId));
        }
    }

    public void cancelNotification() {
        if (this.foregroundId != 0) {
            this.ams.mHandler.post(new AnonymousClass2(this.packageName, this.foregroundId));
        }
    }

    public void stripForegroundServiceFlagFromNotification() {
        if (this.foregroundId != 0) {
            int localForegroundId = this.foregroundId;
            int localUserId = this.userId;
            this.ams.mHandler.post(new AnonymousClass3(this.packageName, localForegroundId, localUserId));
        }
    }

    public void clearDeliveredStartsLocked() {
        for (int i = this.deliveredStarts.size() - 1; i >= 0; i--) {
            ((StartItem) this.deliveredStarts.get(i)).removeUriPermissionsLocked();
        }
        this.deliveredStarts.clear();
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
        sb.append("ServiceRecord{").append(Integer.toHexString(System.identityHashCode(this))).append(" u").append(this.userId).append(" euid: ").append(this.appInfo != null ? this.appInfo.euid : 0).append(' ').append(this.shortName).append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
