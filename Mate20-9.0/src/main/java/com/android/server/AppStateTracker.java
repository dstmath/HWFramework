package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IUidObserver;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseSetArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.StatLogger;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class AppStateTracker {
    private static final boolean DEBUG = false;
    private static final String TAG = "AppStateTracker";
    @VisibleForTesting
    static final int TARGET_OP = 70;
    @GuardedBy("mLock")
    final SparseBooleanArray mActiveUids = new SparseBooleanArray();
    ActivityManagerInternal mActivityManagerInternal;
    AppOpsManager mAppOpsManager;
    IAppOpsService mAppOpsService;
    @GuardedBy("mLock")
    boolean mBatterySaverEnabled;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final SparseSetArray<String> mExemptedPackages = new SparseSetArray<>();
    @VisibleForTesting
    FeatureFlagsObserver mFlagsObserver;
    @GuardedBy("mLock")
    boolean mForceAllAppStandbyForSmallBattery;
    @GuardedBy("mLock")
    boolean mForceAllAppsStandby;
    @GuardedBy("mLock")
    boolean mForcedAppStandbyEnabled;
    @GuardedBy("mLock")
    final SparseBooleanArray mForegroundUids = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public final MyHandler mHandler;
    IActivityManager mIActivityManager;
    @GuardedBy("mLock")
    boolean mIsPluggedIn;
    @GuardedBy("mLock")
    final ArraySet<Listener> mListeners = new ArraySet<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    PowerManagerInternal mPowerManagerInternal;
    @GuardedBy("mLock")
    private int[] mPowerWhitelistedAllAppIds = new int[0];
    @GuardedBy("mLock")
    private int[] mPowerWhitelistedUserAppIds = new int[0];
    @GuardedBy("mLock")
    final ArraySet<Pair<Integer, String>> mRunAnyRestrictedPackages = new ArraySet<>();
    StandbyTracker mStandbyTracker;
    @GuardedBy("mLock")
    boolean mStarted;
    /* access modifiers changed from: private */
    public final StatLogger mStatLogger = new StatLogger(new String[]{"UID_FG_STATE_CHANGED", "UID_ACTIVE_STATE_CHANGED", "RUN_ANY_CHANGED", "ALL_UNWHITELISTED", "ALL_WHITELIST_CHANGED", "TEMP_WHITELIST_CHANGED", "EXEMPT_CHANGED", "FORCE_ALL_CHANGED", "FORCE_APP_STANDBY_FEATURE_FLAG_CHANGED", "IS_UID_ACTIVE_CACHED", "IS_UID_ACTIVE_RAW"});
    @GuardedBy("mLock")
    private int[] mTempWhitelistedAppIds = this.mPowerWhitelistedAllAppIds;
    UsageStatsManagerInternal mUsageStatsManagerInternal;

    private final class AppOpsWatcher extends IAppOpsCallback.Stub {
        private AppOpsWatcher() {
        }

        public void opChanged(int op, int uid, String packageName) throws RemoteException {
            boolean z = false;
            boolean restricted = false;
            try {
                if (AppStateTracker.this.mAppOpsService.checkOperation(70, uid, packageName) != 0) {
                    z = true;
                }
                restricted = z;
            } catch (RemoteException e) {
            }
            synchronized (AppStateTracker.this.mLock) {
                if (AppStateTracker.this.updateForcedAppStandbyUidPackageLocked(uid, packageName, restricted)) {
                    AppStateTracker.this.mHandler.notifyRunAnyAppOpsChanged(uid, packageName);
                }
            }
        }
    }

    @VisibleForTesting
    class FeatureFlagsObserver extends ContentObserver {
        FeatureFlagsObserver() {
            super(null);
        }

        /* access modifiers changed from: package-private */
        public void register() {
            AppStateTracker.this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("forced_app_standby_enabled"), false, this);
            AppStateTracker.this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("forced_app_standby_for_small_battery_enabled"), false, this);
        }

        /* access modifiers changed from: package-private */
        public boolean isForcedAppStandbyEnabled() {
            return AppStateTracker.this.injectGetGlobalSettingInt("forced_app_standby_enabled", 1) == 1;
        }

        /* access modifiers changed from: package-private */
        public boolean isForcedAppStandbyForSmallBatteryEnabled() {
            return AppStateTracker.this.injectGetGlobalSettingInt("forced_app_standby_for_small_battery_enabled", 0) == 1;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
            com.android.server.AppStateTracker.access$200(r3.this$0).notifyForcedAppStandbyFeatureFlagChanged();
         */
        public void onChange(boolean selfChange, Uri uri) {
            if (Settings.Global.getUriFor("forced_app_standby_enabled").equals(uri)) {
                boolean enabled = isForcedAppStandbyEnabled();
                synchronized (AppStateTracker.this.mLock) {
                    if (AppStateTracker.this.mForcedAppStandbyEnabled != enabled) {
                        AppStateTracker.this.mForcedAppStandbyEnabled = enabled;
                    }
                }
            } else if (Settings.Global.getUriFor("forced_app_standby_for_small_battery_enabled").equals(uri)) {
                boolean enabled2 = isForcedAppStandbyForSmallBatteryEnabled();
                synchronized (AppStateTracker.this.mLock) {
                    if (AppStateTracker.this.mForceAllAppStandbyForSmallBattery != enabled2) {
                        AppStateTracker.this.mForceAllAppStandbyForSmallBattery = enabled2;
                        AppStateTracker.this.updateForceAllAppStandbyState();
                    }
                }
            } else {
                Slog.w(AppStateTracker.TAG, "Unexpected feature flag uri encountered: " + uri);
            }
        }
    }

    public static abstract class Listener {
        /* access modifiers changed from: private */
        public void onRunAnyAppOpsChanged(AppStateTracker sender, int uid, String packageName) {
            updateJobsForUidPackage(uid, packageName, sender.isUidActive(uid));
            if (!sender.areAlarmsRestricted(uid, packageName, false)) {
                unblockAlarmsForUidPackage(uid, packageName);
            } else if (!sender.areAlarmsRestricted(uid, packageName, true)) {
                unblockAllUnrestrictedAlarms();
            }
            if (!sender.isRunAnyInBackgroundAppOpsAllowed(uid, packageName)) {
                Slog.v(AppStateTracker.TAG, "Package " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + uid + " toggled into fg service restriction");
                stopForegroundServicesForUidPackage(uid, packageName);
            }
        }

        /* access modifiers changed from: private */
        public void onUidForegroundStateChanged(AppStateTracker sender, int uid) {
            onUidForeground(uid, sender.isUidInForeground(uid));
        }

        /* access modifiers changed from: private */
        public void onUidActiveStateChanged(AppStateTracker sender, int uid) {
            boolean isActive = sender.isUidActive(uid);
            updateJobsForUid(uid, isActive);
            if (isActive) {
                unblockAlarmsForUid(uid);
            }
        }

        /* access modifiers changed from: private */
        public void onPowerSaveUnwhitelisted(AppStateTracker sender) {
            updateAllJobs();
            unblockAllUnrestrictedAlarms();
        }

        /* access modifiers changed from: private */
        public void onPowerSaveWhitelistedChanged(AppStateTracker sender) {
            updateAllJobs();
        }

        /* access modifiers changed from: private */
        public void onTempPowerSaveWhitelistChanged(AppStateTracker sender) {
            updateAllJobs();
        }

        /* access modifiers changed from: private */
        public void onExemptChanged(AppStateTracker sender) {
            updateAllJobs();
            unblockAllUnrestrictedAlarms();
        }

        /* access modifiers changed from: private */
        public void onForceAllAppsStandbyChanged(AppStateTracker sender) {
            updateAllJobs();
            if (!sender.isForceAllAppsStandbyEnabled()) {
                unblockAllUnrestrictedAlarms();
            }
        }

        public void updateAllJobs() {
        }

        public void updateJobsForUid(int uid, boolean isNowActive) {
        }

        public void updateJobsForUidPackage(int uid, String packageName, boolean isNowActive) {
        }

        public void stopForegroundServicesForUidPackage(int uid, String packageName) {
        }

        public void unblockAllUnrestrictedAlarms() {
        }

        public void unblockAlarmsForUid(int uid) {
        }

        public void unblockAlarmsForUidPackage(int uid, String packageName) {
        }

        public void onUidForeground(int uid, boolean foreground) {
        }
    }

    private class MyHandler extends Handler {
        private static final int MSG_ALL_UNWHITELISTED = 4;
        private static final int MSG_ALL_WHITELIST_CHANGED = 5;
        private static final int MSG_EXEMPT_CHANGED = 10;
        private static final int MSG_FORCE_ALL_CHANGED = 7;
        private static final int MSG_FORCE_APP_STANDBY_FEATURE_FLAG_CHANGED = 9;
        private static final int MSG_ON_UID_ACTIVE = 12;
        private static final int MSG_ON_UID_GONE = 13;
        private static final int MSG_ON_UID_IDLE = 14;
        private static final int MSG_ON_UID_STATE_CHANGED = 11;
        private static final int MSG_RUN_ANY_CHANGED = 3;
        private static final int MSG_TEMP_WHITELIST_CHANGED = 6;
        private static final int MSG_UID_ACTIVE_STATE_CHANGED = 0;
        private static final int MSG_UID_FG_STATE_CHANGED = 1;
        private static final int MSG_USER_REMOVED = 8;

        public MyHandler(Looper looper) {
            super(looper);
        }

        public void notifyUidActiveStateChanged(int uid) {
            obtainMessage(0, uid, 0).sendToTarget();
        }

        public void notifyUidForegroundStateChanged(int uid) {
            obtainMessage(1, uid, 0).sendToTarget();
        }

        public void notifyRunAnyAppOpsChanged(int uid, String packageName) {
            obtainMessage(3, uid, 0, packageName).sendToTarget();
        }

        public void notifyAllUnwhitelisted() {
            removeMessages(4);
            obtainMessage(4).sendToTarget();
        }

        public void notifyAllWhitelistChanged() {
            removeMessages(5);
            obtainMessage(5).sendToTarget();
        }

        public void notifyTempWhitelistChanged() {
            removeMessages(6);
            obtainMessage(6).sendToTarget();
        }

        public void notifyForceAllAppsStandbyChanged() {
            removeMessages(7);
            obtainMessage(7).sendToTarget();
        }

        public void notifyForcedAppStandbyFeatureFlagChanged() {
            removeMessages(9);
            obtainMessage(9).sendToTarget();
        }

        public void notifyExemptChanged() {
            removeMessages(10);
            obtainMessage(10).sendToTarget();
        }

        public void doUserRemoved(int userId) {
            obtainMessage(8, userId, 0).sendToTarget();
        }

        public void onUidStateChanged(int uid, int procState) {
            obtainMessage(11, uid, procState).sendToTarget();
        }

        public void onUidActive(int uid) {
            obtainMessage(12, uid, 0).sendToTarget();
        }

        public void onUidGone(int uid, boolean disabled) {
            obtainMessage(13, uid, disabled).sendToTarget();
        }

        public void onUidIdle(int uid, boolean disabled) {
            obtainMessage(14, uid, disabled).sendToTarget();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v17, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v19, resolved type: boolean} */
        /* JADX WARNING: type inference failed for: r5v0 */
        /* JADX WARNING: type inference failed for: r5v1, types: [int] */
        /* JADX WARNING: type inference failed for: r5v3, types: [int] */
        /* JADX WARNING: type inference failed for: r5v5, types: [int] */
        /* JADX WARNING: type inference failed for: r5v7, types: [int] */
        /* JADX WARNING: type inference failed for: r5v9, types: [int] */
        /* JADX WARNING: type inference failed for: r5v11, types: [int] */
        /* JADX WARNING: type inference failed for: r5v13, types: [int] */
        /* JADX WARNING: type inference failed for: r5v15, types: [int] */
        /* JADX WARNING: type inference failed for: r5v18 */
        /* JADX WARNING: type inference failed for: r5v20 */
        /* JADX WARNING: type inference failed for: r5v21 */
        /* JADX WARNING: type inference failed for: r5v22 */
        /* JADX WARNING: type inference failed for: r5v23 */
        /* JADX WARNING: type inference failed for: r5v24 */
        /* JADX WARNING: type inference failed for: r5v25 */
        /* JADX WARNING: type inference failed for: r5v26 */
        /* JADX WARNING: type inference failed for: r5v27 */
        /* JADX WARNING: type inference failed for: r5v28 */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
            r0 = r9.this$0;
            r2 = com.android.server.AppStateTracker.access$800(r9.this$0).getTime();
            r5 = 0;
            r6 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
            switch(r10.what) {
                case 0: goto L_0x015d;
                case 1: goto L_0x013f;
                case 2: goto L_0x0029;
                case 3: goto L_0x011d;
                case 4: goto L_0x0101;
                case 5: goto L_0x00e5;
                case 6: goto L_0x00c9;
                case 7: goto L_0x00ad;
                case 8: goto L_0x00a5;
                case 9: goto L_0x006c;
                case 10: goto L_0x0050;
                case 11: goto L_0x0048;
                case 12: goto L_0x0042;
                case 13: goto L_0x0036;
                case 14: goto L_0x002a;
                default: goto L_0x0029;
            };
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
            r1 = r10.arg1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
            if (r10.arg1 == 0) goto L_0x0032;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
            r5 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0032, code lost:
            handleUidIdle(r1, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
            r1 = r10.arg1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
            if (r10.arg1 == 0) goto L_0x003e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
            r5 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x003e, code lost:
            handleUidGone(r1, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
            handleUidActive(r10.arg1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0047, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0048, code lost:
            handleUidStateChanged(r10.arg1, r10.arg2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0050, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0057, code lost:
            if (r5 >= r4) goto L_0x0061;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
            com.android.server.AppStateTracker.Listener.access$1600(r1[r5], r0);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0061, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(6, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x006b, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x006c, code lost:
            r4 = com.android.server.AppStateTracker.access$100(r9.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0072, code lost:
            monitor-enter(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0077, code lost:
            if (r9.this$0.mForcedAppStandbyEnabled != false) goto L_0x0080;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x007d, code lost:
            if (r9.this$0.mForceAllAppsStandby != false) goto L_0x0080;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0080, code lost:
            r6 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0081, code lost:
            monitor-exit(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0082, code lost:
            r4 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r7 = r4.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0089, code lost:
            if (r5 >= r7) goto L_0x0098;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x008b, code lost:
            r8 = r4[r5];
            r8.updateAllJobs();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0090, code lost:
            if (r6 == false) goto L_0x0095;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x0092, code lost:
            r8.unblockAllUnrestrictedAlarms();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x0095, code lost:
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0098, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(8, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a1, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a5, code lost:
            r9.this$0.handleUserRemoved(r10.arg1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ad, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b4, code lost:
            if (r5 >= r4) goto L_0x00be;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
            com.android.server.AppStateTracker.Listener.access$1700(r1[r5], r0);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(7, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x00c8, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c9, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d0, code lost:
            if (r5 >= r4) goto L_0x00da;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d2, code lost:
            com.android.server.AppStateTracker.Listener.access$1500(r1[r5], r0);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x00da, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(5, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x00e4, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x00e5, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x00ec, code lost:
            if (r5 >= r4) goto L_0x00f6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x00ee, code lost:
            com.android.server.AppStateTracker.Listener.access$1400(r1[r5], r0);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x00f6, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(4, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x0100, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x0101, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:0x0108, code lost:
            if (r5 >= r4) goto L_0x0112;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x010a, code lost:
            com.android.server.AppStateTracker.Listener.access$1300(r1[r5], r0);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x0112, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x011c, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x011d, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x0124, code lost:
            if (r5 >= r4) goto L_0x0134;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0126, code lost:
            com.android.server.AppStateTracker.Listener.access$1200(r1[r5], r0, r10.arg1, (java.lang.String) r10.obj);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x0134, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(2, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x013e, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x013f, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
            r6 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x0147, code lost:
            if (r6 >= r4) goto L_0x0153;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x0149, code lost:
            com.android.server.AppStateTracker.Listener.access$1100(r1[r6], r0, r10.arg1);
            r6 = r6 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x0153, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(0, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x015c, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x015d, code lost:
            r1 = com.android.server.AppStateTracker.access$900(r9.this$0);
            r4 = r1.length;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0164, code lost:
            if (r5 >= r4) goto L_0x0170;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x0166, code lost:
            com.android.server.AppStateTracker.Listener.access$1000(r1[r5], r0, r10.arg1);
            r5 = r5 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:0x0170, code lost:
            com.android.server.AppStateTracker.access$800(r9.this$0).logDurationStat(1, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x0179, code lost:
            return;
         */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg) {
            if (msg.what != 8) {
                synchronized (AppStateTracker.this.mLock) {
                    if (!AppStateTracker.this.mStarted) {
                    }
                }
            } else {
                AppStateTracker.this.handleUserRemoved(msg.arg1);
            }
        }

        public void handleUidStateChanged(int uid, int procState) {
            synchronized (AppStateTracker.this.mLock) {
                if (procState > 5) {
                    try {
                        if (AppStateTracker.removeUidFromArray(AppStateTracker.this.mForegroundUids, uid, false)) {
                            AppStateTracker.this.mHandler.notifyUidForegroundStateChanged(uid);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else if (AppStateTracker.addUidToArray(AppStateTracker.this.mForegroundUids, uid)) {
                    AppStateTracker.this.mHandler.notifyUidForegroundStateChanged(uid);
                }
            }
        }

        public void handleUidActive(int uid) {
            synchronized (AppStateTracker.this.mLock) {
                if (AppStateTracker.addUidToArray(AppStateTracker.this.mActiveUids, uid)) {
                    AppStateTracker.this.mHandler.notifyUidActiveStateChanged(uid);
                }
            }
        }

        public void handleUidGone(int uid, boolean disabled) {
            removeUid(uid, true);
        }

        public void handleUidIdle(int uid, boolean disabled) {
            removeUid(uid, false);
        }

        private void removeUid(int uid, boolean remove) {
            synchronized (AppStateTracker.this.mLock) {
                if (AppStateTracker.removeUidFromArray(AppStateTracker.this.mActiveUids, uid, remove)) {
                    AppStateTracker.this.mHandler.notifyUidActiveStateChanged(uid);
                }
                if (AppStateTracker.removeUidFromArray(AppStateTracker.this.mForegroundUids, uid, remove)) {
                    AppStateTracker.this.mHandler.notifyUidForegroundStateChanged(uid);
                }
            }
        }
    }

    private final class MyReceiver extends BroadcastReceiver {
        private MyReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId > 0) {
                    AppStateTracker.this.mHandler.doUserRemoved(userId);
                }
            } else if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                synchronized (AppStateTracker.this.mLock) {
                    AppStateTracker appStateTracker = AppStateTracker.this;
                    boolean z = false;
                    if (intent.getIntExtra("plugged", 0) != 0) {
                        z = true;
                    }
                    appStateTracker.mIsPluggedIn = z;
                }
                AppStateTracker.this.updateForceAllAppStandbyState();
            }
        }
    }

    final class StandbyTracker extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        StandbyTracker() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            boolean changed;
            if (bucket == 5) {
                changed = AppStateTracker.this.mExemptedPackages.add(userId, packageName);
            } else {
                changed = AppStateTracker.this.mExemptedPackages.remove(userId, packageName);
            }
            if (changed) {
                AppStateTracker.this.mHandler.notifyExemptChanged();
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
        }
    }

    interface Stats {
        public static final int ALL_UNWHITELISTED = 3;
        public static final int ALL_WHITELIST_CHANGED = 4;
        public static final int EXEMPT_CHANGED = 6;
        public static final int FORCE_ALL_CHANGED = 7;
        public static final int FORCE_APP_STANDBY_FEATURE_FLAG_CHANGED = 8;
        public static final int IS_UID_ACTIVE_CACHED = 9;
        public static final int IS_UID_ACTIVE_RAW = 10;
        public static final int RUN_ANY_CHANGED = 2;
        public static final int TEMP_WHITELIST_CHANGED = 5;
        public static final int UID_ACTIVE_STATE_CHANGED = 1;
        public static final int UID_FG_STATE_CHANGED = 0;
    }

    private final class UidObserver extends IUidObserver.Stub {
        private UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            AppStateTracker.this.mHandler.onUidStateChanged(uid, procState);
        }

        public void onUidActive(int uid) {
            AppStateTracker.this.mHandler.onUidActive(uid);
        }

        public void onUidGone(int uid, boolean disabled) {
            AppStateTracker.this.mHandler.onUidGone(uid, disabled);
        }

        public void onUidIdle(int uid, boolean disabled) {
            AppStateTracker.this.mHandler.onUidIdle(uid, disabled);
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    public AppStateTracker(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new MyHandler(looper);
    }

    public void onSystemServicesReady() {
        synchronized (this.mLock) {
            if (!this.mStarted) {
                this.mStarted = true;
                this.mIActivityManager = (IActivityManager) Preconditions.checkNotNull(injectIActivityManager());
                this.mActivityManagerInternal = (ActivityManagerInternal) Preconditions.checkNotNull(injectActivityManagerInternal());
                this.mAppOpsManager = (AppOpsManager) Preconditions.checkNotNull(injectAppOpsManager());
                this.mAppOpsService = (IAppOpsService) Preconditions.checkNotNull(injectIAppOpsService());
                this.mPowerManagerInternal = (PowerManagerInternal) Preconditions.checkNotNull(injectPowerManagerInternal());
                this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) Preconditions.checkNotNull(injectUsageStatsManagerInternal());
                this.mFlagsObserver = new FeatureFlagsObserver();
                this.mFlagsObserver.register();
                this.mForcedAppStandbyEnabled = this.mFlagsObserver.isForcedAppStandbyEnabled();
                this.mForceAllAppStandbyForSmallBattery = this.mFlagsObserver.isForcedAppStandbyForSmallBatteryEnabled();
                this.mStandbyTracker = new StandbyTracker();
                this.mUsageStatsManagerInternal.addAppIdleStateChangeListener(this.mStandbyTracker);
                try {
                    this.mIActivityManager.registerUidObserver(new UidObserver(), 15, -1, null);
                    this.mAppOpsService.startWatchingMode(70, null, new AppOpsWatcher());
                } catch (RemoteException e) {
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.USER_REMOVED");
                filter.addAction("android.intent.action.BATTERY_CHANGED");
                this.mContext.registerReceiver(new MyReceiver(), filter);
                refreshForcedAppStandbyUidPackagesLocked();
                this.mPowerManagerInternal.registerLowPowerModeObserver(11, new Consumer() {
                    public final void accept(Object obj) {
                        AppStateTracker.lambda$onSystemServicesReady$0(AppStateTracker.this, (PowerSaveState) obj);
                    }
                });
                this.mBatterySaverEnabled = this.mPowerManagerInternal.getLowPowerState(11).batterySaverEnabled;
                updateForceAllAppStandbyState();
            }
        }
    }

    public static /* synthetic */ void lambda$onSystemServicesReady$0(AppStateTracker appStateTracker, PowerSaveState state) {
        synchronized (appStateTracker.mLock) {
            appStateTracker.mBatterySaverEnabled = state.batterySaverEnabled;
            appStateTracker.updateForceAllAppStandbyState();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppOpsManager injectAppOpsManager() {
        return (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public IAppOpsService injectIAppOpsService() {
        return IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public IActivityManager injectIActivityManager() {
        return ActivityManager.getService();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ActivityManagerInternal injectActivityManagerInternal() {
        return (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public PowerManagerInternal injectPowerManagerInternal() {
        return (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UsageStatsManagerInternal injectUsageStatsManagerInternal() {
        return (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isSmallBatteryDevice() {
        return ActivityManager.isSmallBatteryDevice();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int injectGetGlobalSettingInt(String key, int def) {
        return Settings.Global.getInt(this.mContext.getContentResolver(), key, def);
    }

    @GuardedBy("mLock")
    private void refreshForcedAppStandbyUidPackagesLocked() {
        this.mRunAnyRestrictedPackages.clear();
        List<AppOpsManager.PackageOps> ops = this.mAppOpsManager.getPackagesForOps(new int[]{70});
        if (ops != null) {
            int size = ops.size();
            for (int i = 0; i < size; i++) {
                AppOpsManager.PackageOps pkg = ops.get(i);
                List<AppOpsManager.OpEntry> entries = ops.get(i).getOps();
                for (int j = 0; j < entries.size(); j++) {
                    AppOpsManager.OpEntry ent = entries.get(j);
                    if (ent.getOp() == 70 && ent.getMode() != 0) {
                        this.mRunAnyRestrictedPackages.add(Pair.create(Integer.valueOf(pkg.getUid()), pkg.getPackageName()));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateForceAllAppStandbyState() {
        synchronized (this.mLock) {
            if (!this.mForceAllAppStandbyForSmallBattery || !isSmallBatteryDevice()) {
                toggleForceAllAppsStandbyLocked(this.mBatterySaverEnabled);
            } else {
                toggleForceAllAppsStandbyLocked(!this.mIsPluggedIn);
            }
        }
    }

    @GuardedBy("mLock")
    private void toggleForceAllAppsStandbyLocked(boolean enable) {
        if (enable != this.mForceAllAppsStandby) {
            this.mForceAllAppsStandby = enable;
            this.mHandler.notifyForceAllAppsStandbyChanged();
        }
    }

    @GuardedBy("mLock")
    private int findForcedAppStandbyUidPackageIndexLocked(int uid, String packageName) {
        int size = this.mRunAnyRestrictedPackages.size();
        if (size > 8) {
            return this.mRunAnyRestrictedPackages.indexOf(Pair.create(Integer.valueOf(uid), packageName));
        }
        for (int i = 0; i < size; i++) {
            Pair<Integer, String> pair = this.mRunAnyRestrictedPackages.valueAt(i);
            if (((Integer) pair.first).intValue() == uid && packageName.equals(pair.second)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean isRunAnyRestrictedLocked(int uid, String packageName) {
        return findForcedAppStandbyUidPackageIndexLocked(uid, packageName) >= 0;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean updateForcedAppStandbyUidPackageLocked(int uid, String packageName, boolean restricted) {
        int index = findForcedAppStandbyUidPackageIndexLocked(uid, packageName);
        if ((index >= 0) == restricted) {
            return false;
        }
        if (restricted) {
            this.mRunAnyRestrictedPackages.add(Pair.create(Integer.valueOf(uid), packageName));
        } else {
            this.mRunAnyRestrictedPackages.removeAt(index);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean addUidToArray(SparseBooleanArray array, int uid) {
        if (UserHandle.isCore(uid) || array.get(uid)) {
            return false;
        }
        array.put(uid, true);
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean removeUidFromArray(SparseBooleanArray array, int uid, boolean remove) {
        if (UserHandle.isCore(uid) || !array.get(uid)) {
            return false;
        }
        if (remove) {
            array.delete(uid);
        } else {
            array.put(uid, false);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public Listener[] cloneListeners() {
        Listener[] listenerArr;
        synchronized (this.mLock) {
            listenerArr = (Listener[]) this.mListeners.toArray(new Listener[this.mListeners.size()]);
        }
        return listenerArr;
    }

    /* access modifiers changed from: package-private */
    public void handleUserRemoved(int removedUserId) {
        synchronized (this.mLock) {
            for (int i = this.mRunAnyRestrictedPackages.size() - 1; i >= 0; i--) {
                if (UserHandle.getUserId(((Integer) this.mRunAnyRestrictedPackages.valueAt(i).first).intValue()) == removedUserId) {
                    this.mRunAnyRestrictedPackages.removeAt(i);
                }
            }
            cleanUpArrayForUser(this.mActiveUids, removedUserId);
            cleanUpArrayForUser(this.mForegroundUids, removedUserId);
            this.mExemptedPackages.remove(removedUserId);
        }
    }

    private void cleanUpArrayForUser(SparseBooleanArray array, int removedUserId) {
        for (int i = array.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(array.keyAt(i)) == removedUserId) {
                array.removeAt(i);
            }
        }
    }

    public void setPowerSaveWhitelistAppIds(int[] powerSaveWhitelistExceptIdleAppIdArray, int[] powerSaveWhitelistUserAppIdArray, int[] tempWhitelistAppIdArray) {
        synchronized (this.mLock) {
            int[] previousWhitelist = this.mPowerWhitelistedAllAppIds;
            int[] previousTempWhitelist = this.mTempWhitelistedAppIds;
            this.mPowerWhitelistedAllAppIds = powerSaveWhitelistExceptIdleAppIdArray;
            this.mTempWhitelistedAppIds = tempWhitelistAppIdArray;
            this.mPowerWhitelistedUserAppIds = powerSaveWhitelistUserAppIdArray;
            if (isAnyAppIdUnwhitelisted(previousWhitelist, this.mPowerWhitelistedAllAppIds)) {
                this.mHandler.notifyAllUnwhitelisted();
            } else if (!Arrays.equals(previousWhitelist, this.mPowerWhitelistedAllAppIds)) {
                this.mHandler.notifyAllWhitelistChanged();
            }
            if (!Arrays.equals(previousTempWhitelist, this.mTempWhitelistedAppIds)) {
                this.mHandler.notifyTempWhitelistChanged();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0029 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002a A[RETURN] */
    @VisibleForTesting
    static boolean isAnyAppIdUnwhitelisted(int[] prevArray, int[] newArray) {
        boolean prevFinished;
        int i1 = 0;
        int i2 = 0;
        while (true) {
            prevFinished = i1 >= prevArray.length;
            boolean newFinished = i2 >= newArray.length;
            if (!prevFinished && !newFinished) {
                int a1 = prevArray[i1];
                int a2 = newArray[i2];
                if (a1 == a2) {
                    i1++;
                    i2++;
                } else if (a1 < a2) {
                    return true;
                } else {
                    i2++;
                }
            } else if (!prevFinished) {
                return false;
            } else {
                return newFinished;
            }
        }
        if (!prevFinished) {
        }
    }

    public void addListener(Listener listener) {
        synchronized (this.mLock) {
            this.mListeners.add(listener);
        }
    }

    public boolean areAlarmsRestricted(int uid, String packageName, boolean isExemptOnBatterySaver) {
        return isRestricted(uid, packageName, false, isExemptOnBatterySaver);
    }

    public boolean areJobsRestricted(int uid, String packageName, boolean hasForegroundExemption) {
        return isRestricted(uid, packageName, true, hasForegroundExemption);
    }

    public boolean areForegroundServicesRestricted(int uid, String packageName) {
        boolean isRunAnyRestrictedLocked;
        synchronized (this.mLock) {
            isRunAnyRestrictedLocked = isRunAnyRestrictedLocked(uid, packageName);
        }
        return isRunAnyRestrictedLocked;
    }

    private boolean isRestricted(int uid, String packageName, boolean useTempWhitelistToo, boolean exemptOnBatterySaver) {
        if (isUidActive(uid)) {
            return false;
        }
        synchronized (this.mLock) {
            int appId = UserHandle.getAppId(uid);
            if (ArrayUtils.contains(this.mPowerWhitelistedAllAppIds, appId)) {
                return false;
            }
            if (useTempWhitelistToo && ArrayUtils.contains(this.mTempWhitelistedAppIds, appId)) {
                return false;
            }
            if (this.mForcedAppStandbyEnabled && isRunAnyRestrictedLocked(uid, packageName)) {
                return true;
            }
            if (exemptOnBatterySaver) {
                return false;
            }
            if (this.mExemptedPackages.contains(UserHandle.getUserId(uid), packageName)) {
                return false;
            }
            boolean z = this.mForceAllAppsStandby;
            return z;
        }
    }

    public boolean isUidActive(int uid) {
        boolean z;
        if (UserHandle.isCore(uid)) {
            return true;
        }
        synchronized (this.mLock) {
            z = this.mActiveUids.get(uid);
        }
        return z;
    }

    public boolean isUidActiveSynced(int uid) {
        if (isUidActive(uid)) {
            return true;
        }
        long start = this.mStatLogger.getTime();
        boolean ret = this.mActivityManagerInternal.isUidActive(uid);
        this.mStatLogger.logDurationStat(10, start);
        return ret;
    }

    public boolean isUidInForeground(int uid) {
        boolean z;
        if (UserHandle.isCore(uid)) {
            return true;
        }
        synchronized (this.mLock) {
            z = this.mForegroundUids.get(uid);
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isForceAllAppsStandbyEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mForceAllAppsStandby;
        }
        return z;
    }

    public boolean isRunAnyInBackgroundAppOpsAllowed(int uid, String packageName) {
        boolean z;
        synchronized (this.mLock) {
            z = !isRunAnyRestrictedLocked(uid, packageName);
        }
        return z;
    }

    public boolean isUidPowerSaveWhitelisted(int uid) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mPowerWhitelistedAllAppIds, UserHandle.getAppId(uid));
        }
        return contains;
    }

    public boolean isUidPowerSaveUserWhitelisted(int uid) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mPowerWhitelistedUserAppIds, UserHandle.getAppId(uid));
        }
        return contains;
    }

    public boolean isUidTempPowerSaveWhitelisted(int uid) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mTempWhitelistedAppIds, UserHandle.getAppId(uid));
        }
        return contains;
    }

    @Deprecated
    public void dump(PrintWriter pw, String prefix) {
        dump(new IndentingPrintWriter(pw, "  ").setIndent(prefix));
    }

    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Forced App Standby Feature enabled: " + this.mForcedAppStandbyEnabled);
            pw.print("Force all apps standby: ");
            pw.println(isForceAllAppsStandbyEnabled());
            pw.print("Small Battery Device: ");
            pw.println(isSmallBatteryDevice());
            pw.print("Force all apps standby for small battery device: ");
            pw.println(this.mForceAllAppStandbyForSmallBattery);
            pw.print("Plugged In: ");
            pw.println(this.mIsPluggedIn);
            pw.print("Active uids: ");
            dumpUids(pw, this.mActiveUids);
            pw.print("Foreground uids: ");
            dumpUids(pw, this.mForegroundUids);
            pw.print("Except-idle + user whitelist appids: ");
            pw.println(Arrays.toString(this.mPowerWhitelistedAllAppIds));
            pw.print("User whitelist appids: ");
            pw.println(Arrays.toString(this.mPowerWhitelistedUserAppIds));
            pw.print("Temp whitelist appids: ");
            pw.println(Arrays.toString(this.mTempWhitelistedAppIds));
            pw.println("Exempted packages:");
            pw.increaseIndent();
            for (int i = 0; i < this.mExemptedPackages.size(); i++) {
                pw.print("User ");
                pw.print(this.mExemptedPackages.keyAt(i));
                pw.println();
                pw.increaseIndent();
                for (int j = 0; j < this.mExemptedPackages.sizeAt(i); j++) {
                    pw.print((String) this.mExemptedPackages.valueAt(i, j));
                    pw.println();
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Restricted packages:");
            pw.increaseIndent();
            Iterator<Pair<Integer, String>> it = this.mRunAnyRestrictedPackages.iterator();
            while (it.hasNext()) {
                Pair<Integer, String> uidAndPackage = it.next();
                pw.print(UserHandle.formatUid(((Integer) uidAndPackage.first).intValue()));
                pw.print(" ");
                pw.print((String) uidAndPackage.second);
                pw.println();
            }
            pw.decreaseIndent();
            this.mStatLogger.dump(pw);
        }
    }

    private void dumpUids(PrintWriter pw, SparseBooleanArray array) {
        pw.print("[");
        String sep = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        for (int i = 0; i < array.size(); i++) {
            if (array.valueAt(i)) {
                pw.print(sep);
                pw.print(UserHandle.formatUid(array.keyAt(i)));
                sep = " ";
            }
        }
        pw.println("]");
    }

    public void dumpProto(ProtoOutputStream proto, long fieldId) {
        ProtoOutputStream protoOutputStream = proto;
        synchronized (this.mLock) {
            long token = proto.start(fieldId);
            protoOutputStream.write(1133871366145L, this.mForceAllAppsStandby);
            protoOutputStream.write(1133871366150L, isSmallBatteryDevice());
            protoOutputStream.write(1133871366151L, this.mForceAllAppStandbyForSmallBattery);
            protoOutputStream.write(1133871366152L, this.mIsPluggedIn);
            for (int i = 0; i < this.mActiveUids.size(); i++) {
                if (this.mActiveUids.valueAt(i)) {
                    protoOutputStream.write(2220498092034L, this.mActiveUids.keyAt(i));
                }
            }
            for (int i2 = 0; i2 < this.mForegroundUids.size(); i2++) {
                if (this.mForegroundUids.valueAt(i2)) {
                    protoOutputStream.write(2220498092043L, this.mForegroundUids.keyAt(i2));
                }
            }
            for (int appId : this.mPowerWhitelistedAllAppIds) {
                protoOutputStream.write(2220498092035L, appId);
            }
            for (int appId2 : this.mPowerWhitelistedUserAppIds) {
                protoOutputStream.write(2220498092044L, appId2);
            }
            for (int appId3 : this.mTempWhitelistedAppIds) {
                protoOutputStream.write(2220498092036L, appId3);
            }
            for (int i3 = 0; i3 < this.mExemptedPackages.size(); i3++) {
                for (int j = 0; j < this.mExemptedPackages.sizeAt(i3); j++) {
                    long token2 = protoOutputStream.start(2246267895818L);
                    protoOutputStream.write(1120986464257L, this.mExemptedPackages.keyAt(i3));
                    protoOutputStream.write(1138166333442L, (String) this.mExemptedPackages.valueAt(i3, j));
                    protoOutputStream.end(token2);
                }
            }
            Iterator<Pair<Integer, String>> it = this.mRunAnyRestrictedPackages.iterator();
            while (it.hasNext()) {
                Pair<Integer, String> uidAndPackage = it.next();
                long token22 = protoOutputStream.start(2246267895813L);
                protoOutputStream.write(1120986464257L, ((Integer) uidAndPackage.first).intValue());
                protoOutputStream.write(1138166333442L, (String) uidAndPackage.second);
                protoOutputStream.end(token22);
            }
            this.mStatLogger.dumpProto(protoOutputStream, 1146756268041L);
            protoOutputStream.end(token);
        }
    }
}
