package com.android.server.trust;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.ITrustListener;
import android.app.trust.ITrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.DumpUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.SystemService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.slice.SliceClientPermissions;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class TrustManagerService extends SystemService {
    static final boolean DEBUG = (Build.IS_DEBUGGABLE && Log.isLoggable(TAG, 2));
    private static final int MSG_CLEANUP_USER = 8;
    private static final int MSG_DISPATCH_UNLOCK_ATTEMPT = 3;
    private static final int MSG_DISPATCH_UNLOCK_LOCKOUT = 13;
    private static final int MSG_ENABLED_AGENTS_CHANGED = 4;
    private static final int MSG_FLUSH_TRUST_USUALLY_MANAGED = 10;
    private static final int MSG_KEYGUARD_SHOWING_CHANGED = 6;
    private static final int MSG_REFRESH_DEVICE_LOCKED_FOR_USER = 14;
    private static final int MSG_REGISTER_LISTENER = 1;
    private static final int MSG_SCHEDULE_TRUST_TIMEOUT = 15;
    private static final int MSG_START_USER = 7;
    private static final int MSG_STOP_USER = 12;
    private static final int MSG_SWITCH_USER = 9;
    private static final int MSG_UNLOCK_USER = 11;
    private static final int MSG_UNREGISTER_LISTENER = 2;
    private static final String PERMISSION_PROVIDE_AGENT = "android.permission.PROVIDE_TRUST_AGENT";
    private static final String TAG = "TrustManagerService";
    private static final Intent TRUST_AGENT_INTENT = new Intent("android.service.trust.TrustAgentService");
    private static final String TRUST_TIMEOUT_ALARM_TAG = "TrustManagerService.trustTimeoutForUser";
    private static final long TRUST_TIMEOUT_IN_MILLIS = 14400000;
    private static final int TRUST_USUALLY_MANAGED_FLUSH_DELAY = 120000;
    private final ArraySet<AgentInfo> mActiveAgents = new ArraySet<>();
    private final ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    final TrustArchive mArchive = new TrustArchive();
    private final Context mContext;
    private int mCurrentUser = 0;
    @GuardedBy({"mDeviceLockedForUser"})
    private final SparseBooleanArray mDeviceLockedForUser = new SparseBooleanArray();
    private final Handler mHandler = new Handler() {
        /* class com.android.server.trust.TrustManagerService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SparseBooleanArray usuallyManaged;
            boolean z = false;
            switch (msg.what) {
                case 1:
                    TrustManagerService.this.addListener((ITrustListener) msg.obj);
                    return;
                case 2:
                    TrustManagerService.this.removeListener((ITrustListener) msg.obj);
                    return;
                case 3:
                    TrustManagerService trustManagerService = TrustManagerService.this;
                    if (msg.arg1 != 0) {
                        z = true;
                    }
                    trustManagerService.dispatchUnlockAttempt(z, msg.arg2);
                    return;
                case 4:
                    TrustManagerService.this.refreshAgentList(-1);
                    TrustManagerService.this.refreshDeviceLockedForUser(-1);
                    return;
                case 5:
                default:
                    return;
                case 6:
                    TrustManagerService trustManagerService2 = TrustManagerService.this;
                    trustManagerService2.refreshDeviceLockedForUser(trustManagerService2.mCurrentUser);
                    return;
                case 7:
                case 8:
                case 11:
                    TrustManagerService.this.refreshAgentList(msg.arg1);
                    return;
                case 9:
                    TrustManagerService.this.mCurrentUser = msg.arg1;
                    TrustManagerService.this.mSettingsObserver.updateContentObserver();
                    TrustManagerService.this.refreshDeviceLockedForUser(-1);
                    return;
                case 10:
                    synchronized (TrustManagerService.this.mTrustUsuallyManagedForUser) {
                        usuallyManaged = TrustManagerService.this.mTrustUsuallyManagedForUser.clone();
                    }
                    for (int i = 0; i < usuallyManaged.size(); i++) {
                        int userId = usuallyManaged.keyAt(i);
                        boolean value = usuallyManaged.valueAt(i);
                        if (value != TrustManagerService.this.mLockPatternUtils.isTrustUsuallyManaged(userId)) {
                            TrustManagerService.this.mLockPatternUtils.setTrustUsuallyManaged(value, userId);
                        }
                    }
                    return;
                case 12:
                    TrustManagerService.this.setDeviceLockedForUser(msg.arg1, true);
                    return;
                case 13:
                    TrustManagerService.this.dispatchUnlockLockout(msg.arg1, msg.arg2);
                    return;
                case 14:
                    if (msg.arg2 == 1) {
                        TrustManagerService.this.updateTrust(msg.arg1, 0, true);
                    }
                    TrustManagerService.this.refreshDeviceLockedForUser(msg.arg1);
                    return;
                case 15:
                    TrustManagerService.this.handleScheduleTrustTimeout(msg.arg1, msg.arg2);
                    return;
            }
        }
    };
    private final LockPatternUtils mLockPatternUtils;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.android.server.trust.TrustManagerService.AnonymousClass3 */

        public void onSomePackagesChanged() {
            TrustManagerService.this.refreshAgentList(-1);
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            return true;
        }

        public void onPackageDisappeared(String packageName, int reason) {
            TrustManagerService.this.removeAgentsOfPackage(packageName);
        }
    };
    private final Receiver mReceiver = new Receiver();
    private final IBinder mService = new ITrustManager.Stub() {
        /* class com.android.server.trust.TrustManagerService.AnonymousClass1 */

        public void reportUnlockAttempt(boolean authenticated, int userId) throws RemoteException {
            enforceReportPermission();
            TrustManagerService.this.mHandler.obtainMessage(3, authenticated ? 1 : 0, userId).sendToTarget();
        }

        public void reportUnlockLockout(int timeoutMs, int userId) throws RemoteException {
            enforceReportPermission();
            TrustManagerService.this.mHandler.obtainMessage(13, timeoutMs, userId).sendToTarget();
        }

        public void reportEnabledTrustAgentsChanged(int userId) throws RemoteException {
            enforceReportPermission();
            TrustManagerService.this.mHandler.removeMessages(4);
            TrustManagerService.this.mHandler.sendEmptyMessage(4);
        }

        public void reportKeyguardShowingChanged() throws RemoteException {
            enforceReportPermission();
            Log.i(TrustManagerService.TAG, "handle reportKeyguardShowingChanged.");
            TrustManagerService.this.mHandler.removeMessages(6);
            TrustManagerService.this.mHandler.sendEmptyMessage(6);
            TrustManagerService.this.mHandler.runWithScissors($$Lambda$TrustManagerService$1$98HKBkgC1PLlz_Q1vJz1OJtw4c.INSTANCE, 0);
        }

        static /* synthetic */ void lambda$reportKeyguardShowingChanged$0() {
        }

        public void registerTrustListener(ITrustListener trustListener) throws RemoteException {
            enforceListenerPermission();
            TrustManagerService.this.mHandler.obtainMessage(1, trustListener).sendToTarget();
        }

        public void unregisterTrustListener(ITrustListener trustListener) throws RemoteException {
            enforceListenerPermission();
            TrustManagerService.this.mHandler.obtainMessage(2, trustListener).sendToTarget();
        }

        public boolean isDeviceLocked(int userId) throws RemoteException {
            int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "isDeviceLocked", null);
            long token = Binder.clearCallingIdentity();
            try {
                if (!TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId2)) {
                    userId2 = TrustManagerService.this.resolveProfileParent(userId2);
                }
                return TrustManagerService.this.isDeviceLockedInner(userId2);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isDeviceSecure(int userId) throws RemoteException {
            int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "isDeviceSecure", null);
            long token = Binder.clearCallingIdentity();
            try {
                if (!TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId2)) {
                    userId2 = TrustManagerService.this.resolveProfileParent(userId2);
                }
                return TrustManagerService.this.mLockPatternUtils.isSecure(userId2);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void enforceReportPermission() {
            TrustManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_KEYGUARD_SECURE_STORAGE", "reporting trust events");
        }

        private void enforceListenerPermission() {
            TrustManagerService.this.mContext.enforceCallingPermission("android.permission.TRUST_LISTENER", "register trust listener");
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
            if (DumpUtils.checkDumpPermission(TrustManagerService.this.mContext, TrustManagerService.TAG, fout)) {
                if (TrustManagerService.this.isSafeMode()) {
                    fout.println("disabled because the system is in safe mode.");
                } else if (!TrustManagerService.this.mTrustAgentsCanRun) {
                    fout.println("disabled because the third-party apps can't run yet.");
                } else {
                    final List<UserInfo> userInfos = TrustManagerService.this.mUserManager.getUsers(true);
                    TrustManagerService.this.mHandler.runWithScissors(new Runnable() {
                        /* class com.android.server.trust.TrustManagerService.AnonymousClass1.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            fout.println("Trust manager state:");
                            for (UserInfo user : userInfos) {
                                AnonymousClass1.this.dumpUser(fout, user, user.id == TrustManagerService.this.mCurrentUser);
                            }
                        }
                    }, 1500);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dumpUser(PrintWriter fout, UserInfo user, boolean isCurrent) {
            fout.printf(" User \"%s\" (id=%d, flags=%#x)", user.name, Integer.valueOf(user.id), Integer.valueOf(user.flags));
            if (!user.supportsSwitchToByUser()) {
                fout.println("(managed profile)");
                fout.println("   disabled because switching to this user is not possible.");
                return;
            }
            if (isCurrent) {
                fout.print(" (current)");
            }
            fout.print(": trusted=" + dumpBool(TrustManagerService.this.aggregateIsTrusted(user.id)));
            fout.print(", trustManaged=" + dumpBool(TrustManagerService.this.aggregateIsTrustManaged(user.id)));
            fout.print(", deviceLocked=" + dumpBool(TrustManagerService.this.isDeviceLockedInner(user.id)));
            fout.print(", strongAuthRequired=" + dumpHex(TrustManagerService.this.mStrongAuthTracker.getStrongAuthForUser(user.id)));
            fout.println();
            fout.println("   Enabled agents:");
            boolean duplicateSimpleNames = false;
            ArraySet<String> simpleNames = new ArraySet<>();
            Iterator it = TrustManagerService.this.mActiveAgents.iterator();
            while (it.hasNext()) {
                AgentInfo info = (AgentInfo) it.next();
                if (info.userId == user.id) {
                    boolean trusted = info.agent.isTrusted();
                    fout.print("    ");
                    fout.println(info.component.flattenToShortString());
                    fout.print("     bound=" + dumpBool(info.agent.isBound()));
                    fout.print(", connected=" + dumpBool(info.agent.isConnected()));
                    fout.print(", managingTrust=" + dumpBool(info.agent.isManagingTrust()));
                    fout.print(", trusted=" + dumpBool(trusted));
                    fout.println();
                    if (trusted) {
                        fout.println("      message=\"" + ((Object) info.agent.getMessage()) + "\"");
                    }
                    if (!info.agent.isConnected()) {
                        String restartTime = TrustArchive.formatDuration(info.agent.getScheduledRestartUptimeMillis() - SystemClock.uptimeMillis());
                        fout.println("      restartScheduledAt=" + restartTime);
                    }
                    if (!simpleNames.add(TrustArchive.getSimpleName(info.component))) {
                        duplicateSimpleNames = true;
                    }
                }
            }
            fout.println("   Events:");
            TrustManagerService.this.mArchive.dump(fout, 50, user.id, "    ", duplicateSimpleNames);
            fout.println();
        }

        private String dumpBool(boolean b) {
            return b ? "1" : "0";
        }

        private String dumpHex(int i) {
            return "0x" + Integer.toHexString(i);
        }

        public void setDeviceLockedForUser(int userId, boolean locked) {
            enforceReportPermission();
            long identity = Binder.clearCallingIdentity();
            try {
                if (TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) && TrustManagerService.this.mLockPatternUtils.isSecure(userId)) {
                    synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                        TrustManagerService.this.mDeviceLockedForUser.put(userId, locked);
                    }
                    KeyStore.getInstance().onUserLockedStateChanged(userId, locked);
                    if (locked) {
                        try {
                            ActivityManager.getService().notifyLockedProfile(userId);
                        } catch (RemoteException e) {
                        }
                    }
                    Intent lockIntent = new Intent("android.intent.action.DEVICE_LOCKED_CHANGED");
                    lockIntent.addFlags(1073741824);
                    lockIntent.putExtra("android.intent.extra.user_handle", userId);
                    TrustManagerService.this.mContext.sendBroadcastAsUser(lockIntent, UserHandle.SYSTEM, "android.permission.TRUST_LISTENER", null);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isTrustUsuallyManaged(int userId) {
            TrustManagerService.this.mContext.enforceCallingPermission("android.permission.TRUST_LISTENER", "query trust state");
            return TrustManagerService.this.isTrustUsuallyManagedInternal(userId);
        }

        public void unlockedByBiometricForUser(int userId, BiometricSourceType biometricSource) {
            enforceReportPermission();
            synchronized (TrustManagerService.this.mUsersUnlockedByBiometric) {
                TrustManagerService.this.mUsersUnlockedByBiometric.put(userId, true);
            }
            TrustManagerService.this.mHandler.obtainMessage(14, userId, TrustManagerService.this.mSettingsObserver.getTrustAgentsExtendUnlock() ? 1 : 0).sendToTarget();
        }

        public void clearAllBiometricRecognized(BiometricSourceType biometricSource) {
            enforceReportPermission();
            synchronized (TrustManagerService.this.mUsersUnlockedByBiometric) {
                TrustManagerService.this.mUsersUnlockedByBiometric.clear();
            }
            TrustManagerService.this.mHandler.obtainMessage(14, -1, 0).sendToTarget();
        }
    };
    private final SettingsObserver mSettingsObserver;
    private final StrongAuthTracker mStrongAuthTracker;
    private boolean mTrustAgentsCanRun = false;
    private final ArrayList<ITrustListener> mTrustListeners = new ArrayList<>();
    private final ArrayMap<Integer, TrustTimeoutAlarmListener> mTrustTimeoutAlarmListenerForUser = new ArrayMap<>();
    @GuardedBy({"mTrustUsuallyManagedForUser"})
    private final SparseBooleanArray mTrustUsuallyManagedForUser = new SparseBooleanArray();
    @GuardedBy({"mUserIsTrusted"})
    private final SparseBooleanArray mUserIsTrusted = new SparseBooleanArray();
    private final UserManager mUserManager;
    @GuardedBy({"mUsersUnlockedByBiometric"})
    private final SparseBooleanArray mUsersUnlockedByBiometric = new SparseBooleanArray();

    /* JADX WARN: Type inference failed for: r0v10, types: [com.android.server.trust.TrustManagerService$1, android.os.IBinder] */
    public TrustManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mStrongAuthTracker = new StrongAuthTracker(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("trust", this.mService);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (!isSafeMode()) {
            if (phase == 500) {
                this.mPackageMonitor.register(this.mContext, this.mHandler.getLooper(), UserHandle.ALL, true);
                this.mReceiver.register(this.mContext);
                this.mLockPatternUtils.registerStrongAuthTracker(this.mStrongAuthTracker);
            } else if (phase == 600) {
                this.mTrustAgentsCanRun = true;
                refreshAgentList(-1);
                refreshDeviceLockedForUser(-1);
            } else if (phase == 1000) {
                maybeEnableFactoryTrustAgents(this.mLockPatternUtils, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Uri LOCK_SCREEN_WHEN_TRUST_LOST = Settings.Secure.getUriFor("lock_screen_when_trust_lost");
        private final Uri TRUST_AGENTS_EXTEND_UNLOCK = Settings.Secure.getUriFor("trust_agents_extend_unlock");
        private final ContentResolver mContentResolver;
        private final boolean mIsAutomotive;
        private boolean mLockWhenTrustLost;
        private boolean mTrustAgentsExtendUnlock;

        SettingsObserver(Handler handler) {
            super(handler);
            this.mIsAutomotive = TrustManagerService.this.getContext().getPackageManager().hasSystemFeature("android.hardware.type.automotive");
            this.mContentResolver = TrustManagerService.this.getContext().getContentResolver();
            updateContentObserver();
        }

        /* access modifiers changed from: package-private */
        public void updateContentObserver() {
            this.mContentResolver.unregisterContentObserver(this);
            this.mContentResolver.registerContentObserver(this.TRUST_AGENTS_EXTEND_UNLOCK, false, this, TrustManagerService.this.mCurrentUser);
            this.mContentResolver.registerContentObserver(this.LOCK_SCREEN_WHEN_TRUST_LOST, false, this, TrustManagerService.this.mCurrentUser);
            onChange(true, this.TRUST_AGENTS_EXTEND_UNLOCK);
            onChange(true, this.LOCK_SCREEN_WHEN_TRUST_LOST);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            boolean z = true;
            if (this.TRUST_AGENTS_EXTEND_UNLOCK.equals(uri)) {
                if (Settings.Secure.getIntForUser(this.mContentResolver, "trust_agents_extend_unlock", !this.mIsAutomotive ? 1 : 0, TrustManagerService.this.mCurrentUser) == 0) {
                    z = false;
                }
                this.mTrustAgentsExtendUnlock = z;
            } else if (this.LOCK_SCREEN_WHEN_TRUST_LOST.equals(uri)) {
                if (Settings.Secure.getIntForUser(this.mContentResolver, "lock_screen_when_trust_lost", 0, TrustManagerService.this.mCurrentUser) == 0) {
                    z = false;
                }
                this.mLockWhenTrustLost = z;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean getTrustAgentsExtendUnlock() {
            return this.mTrustAgentsExtendUnlock;
        }

        /* access modifiers changed from: package-private */
        public boolean getLockWhenTrustLost() {
            return this.mLockWhenTrustLost;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeLockScreen(int userId) {
        if (userId == this.mCurrentUser && this.mSettingsObserver.getLockWhenTrustLost()) {
            if (DEBUG) {
                Slog.d(TAG, "Locking device because trust was lost");
            }
            try {
                WindowManagerGlobal.getWindowManagerService().lockNow((Bundle) null);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error locking screen when trust was lost");
            }
            TrustTimeoutAlarmListener alarm = this.mTrustTimeoutAlarmListenerForUser.get(Integer.valueOf(userId));
            if (alarm != null && this.mSettingsObserver.getTrustAgentsExtendUnlock()) {
                this.mAlarmManager.cancel(alarm);
                alarm.setQueued(false);
            }
        }
    }

    private void scheduleTrustTimeout(int userId, boolean override) {
        boolean z = override;
        if (override) {
            z = true;
        }
        Handler handler = this.mHandler;
        int shouldOverride = z ? 1 : 0;
        int shouldOverride2 = z ? 1 : 0;
        int shouldOverride3 = z ? 1 : 0;
        handler.obtainMessage(15, userId, shouldOverride).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScheduleTrustTimeout(int userId, int shouldOverride) {
        long when = SystemClock.elapsedRealtime() + 14400000;
        int userId2 = this.mCurrentUser;
        TrustTimeoutAlarmListener alarm = this.mTrustTimeoutAlarmListenerForUser.get(Integer.valueOf(userId2));
        if (alarm == null) {
            alarm = new TrustTimeoutAlarmListener(userId2);
            this.mTrustTimeoutAlarmListenerForUser.put(Integer.valueOf(userId2), alarm);
        } else if (shouldOverride != 0 || !alarm.isQueued()) {
            this.mAlarmManager.cancel(alarm);
        } else if (DEBUG) {
            Slog.d(TAG, "Found existing trust timeout alarm. Skipping.");
            return;
        } else {
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "\tSetting up trust timeout alarm");
        }
        alarm.setQueued(true);
        this.mAlarmManager.setExact(2, when, TRUST_TIMEOUT_ALARM_TAG, alarm, this.mHandler);
    }

    /* access modifiers changed from: private */
    public static final class AgentInfo {
        TrustAgentWrapper agent;
        ComponentName component;
        Drawable icon;
        CharSequence label;
        SettingsAttrs settings;
        int userId;

        private AgentInfo() {
        }

        public boolean equals(Object other) {
            if (!(other instanceof AgentInfo)) {
                return false;
            }
            AgentInfo o = (AgentInfo) other;
            if (!this.component.equals(o.component) || this.userId != o.userId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (this.component.hashCode() * 31) + this.userId;
        }
    }

    private void updateTrustAll() {
        for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
            updateTrust(userInfo.id, 0);
        }
    }

    public void updateTrust(int userId, int flags) {
        updateTrust(userId, flags, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrust(int userId, int flags, boolean isFromUnlock) {
        boolean changed;
        boolean managed = aggregateIsTrustManaged(userId);
        dispatchOnTrustManagedChanged(managed, userId);
        if (this.mStrongAuthTracker.isTrustAllowedForUser(userId) && isTrustUsuallyManagedInternal(userId) != managed) {
            updateTrustUsuallyManaged(userId, managed);
        }
        boolean trusted = aggregateIsTrusted(userId);
        boolean showingKeyguard = true;
        try {
            showingKeyguard = WindowManagerGlobal.getWindowManagerService().isKeyguardLocked();
        } catch (RemoteException e) {
        }
        synchronized (this.mUserIsTrusted) {
            changed = true;
            if (this.mSettingsObserver.getTrustAgentsExtendUnlock()) {
                trusted = trusted && (!showingKeyguard || isFromUnlock || !(this.mUserIsTrusted.get(userId) != trusted)) && userId == this.mCurrentUser;
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Extend unlock setting trusted as ");
                    sb.append(Boolean.toString(trusted));
                    sb.append(" && ");
                    sb.append(Boolean.toString(!showingKeyguard));
                    sb.append(" && ");
                    sb.append(Boolean.toString(userId == this.mCurrentUser));
                    Slog.d(TAG, sb.toString());
                }
            }
            if (this.mUserIsTrusted.get(userId) == trusted) {
                changed = false;
            }
            this.mUserIsTrusted.put(userId, trusted);
        }
        dispatchOnTrustChanged(trusted, userId, flags);
        if (changed) {
            refreshDeviceLockedForUser(userId);
            if (!trusted) {
                maybeLockScreen(userId);
            } else {
                scheduleTrustTimeout(userId, false);
            }
        }
    }

    private void updateTrustUsuallyManaged(int userId, boolean managed) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            this.mTrustUsuallyManagedForUser.put(userId, managed);
        }
        this.mHandler.removeMessages(10);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(10), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
    }

    public long addEscrowToken(byte[] token, int userId) {
        return this.mLockPatternUtils.addEscrowToken(token, userId, new LockPatternUtils.EscrowTokenStateChangeCallback() {
            /* class com.android.server.trust.$$Lambda$TrustManagerService$fEkVwjahpkATIGtXudiFOG8VXOo */

            public final void onEscrowTokenActivated(long j, int i) {
                TrustManagerService.this.lambda$addEscrowToken$0$TrustManagerService(j, i);
            }
        });
    }

    public boolean removeEscrowToken(long handle, int userId) {
        return this.mLockPatternUtils.removeEscrowToken(handle, userId);
    }

    public boolean isEscrowTokenActive(long handle, int userId) {
        return this.mLockPatternUtils.isEscrowTokenActive(handle, userId);
    }

    public void unlockUserWithToken(long handle, byte[] token, int userId) {
        this.mLockPatternUtils.unlockUserWithToken(handle, token, userId);
    }

    /* access modifiers changed from: package-private */
    public void showKeyguardErrorMessage(CharSequence message) {
        dispatchOnTrustError(message);
    }

    /* access modifiers changed from: package-private */
    public void refreshAgentList(int userIdOrAll) {
        List<UserInfo> userInfos;
        Iterator<UserInfo> it;
        PackageManager pm;
        int userIdOrAll2 = userIdOrAll;
        if (DEBUG) {
            Slog.d(TAG, "refreshAgentList(" + userIdOrAll2 + ")");
        }
        if (this.mTrustAgentsCanRun) {
            if (userIdOrAll2 != -1 && userIdOrAll2 < 0) {
                Log.e(TAG, "refreshAgentList(userId=" + userIdOrAll2 + "): Invalid user handle, must be USER_ALL or a specific user.", new Throwable("here"));
                userIdOrAll2 = -1;
            }
            PackageManager pm2 = this.mContext.getPackageManager();
            boolean z = true;
            if (userIdOrAll2 == -1) {
                userInfos = this.mUserManager.getUsers(true);
            } else {
                userInfos = new ArrayList<>();
                userInfos.add(this.mUserManager.getUserInfo(userIdOrAll2));
            }
            LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
            ArraySet<AgentInfo> obsoleteAgents = new ArraySet<>();
            obsoleteAgents.addAll(this.mActiveAgents);
            Iterator<UserInfo> it2 = userInfos.iterator();
            while (it2.hasNext()) {
                UserInfo userInfo = it2.next();
                if (userInfo == null || userInfo.partial || !userInfo.isEnabled()) {
                    z = true;
                } else if (!userInfo.guestToRemove) {
                    if (!userInfo.supportsSwitchToByUser()) {
                        if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": switchToByUser=false");
                        }
                    } else if (!this.mActivityManager.isUserRunning(userInfo.id)) {
                        if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": user not started");
                        }
                    } else if (lockPatternUtils.isSecure(userInfo.id)) {
                        DevicePolicyManager dpm = lockPatternUtils.getDevicePolicyManager();
                        boolean disableTrustAgents = (dpm.getKeyguardDisabledFeatures(null, userInfo.id) & 16) != 0 ? z : false;
                        List<ComponentName> enabledAgents = lockPatternUtils.getEnabledTrustAgents(userInfo.id);
                        if (enabledAgents != null) {
                            List<ResolveInfo> resolveInfos = resolveAllowedTrustAgents(pm2, userInfo.id);
                            for (ResolveInfo resolveInfo : resolveInfos) {
                                ComponentName name = getComponentName(resolveInfo);
                                if (!enabledAgents.contains(name)) {
                                    if (DEBUG) {
                                        Slog.d(TAG, "refreshAgentList: skipping " + name.flattenToShortString() + " u" + userInfo.id + ": not enabled by user");
                                        lockPatternUtils = lockPatternUtils;
                                    }
                                    resolveInfos = resolveInfos;
                                    userInfos = userInfos;
                                } else {
                                    if (disableTrustAgents) {
                                        it = it2;
                                        List<PersistableBundle> config = dpm.getTrustAgentConfiguration(null, name, userInfo.id);
                                        if (config == null || config.isEmpty()) {
                                            if (DEBUG) {
                                                Slog.d(TAG, "refreshAgentList: skipping " + name.flattenToShortString() + " u" + userInfo.id + ": not allowed by DPM");
                                            }
                                            lockPatternUtils = lockPatternUtils;
                                            resolveInfos = resolveInfos;
                                            userInfos = userInfos;
                                            it2 = it;
                                        }
                                    } else {
                                        it = it2;
                                    }
                                    AgentInfo agentInfo = new AgentInfo();
                                    agentInfo.component = name;
                                    agentInfo.userId = userInfo.id;
                                    if (!this.mActiveAgents.contains(agentInfo)) {
                                        agentInfo.label = resolveInfo.loadLabel(pm2);
                                        agentInfo.icon = resolveInfo.loadIcon(pm2);
                                        agentInfo.settings = getSettingsAttrs(pm2, resolveInfo);
                                    } else {
                                        agentInfo = this.mActiveAgents.valueAt(this.mActiveAgents.indexOf(agentInfo));
                                    }
                                    boolean directUnlock = false;
                                    if (agentInfo.settings != null) {
                                        directUnlock = resolveInfo.serviceInfo.directBootAware && agentInfo.settings.canUnlockProfile;
                                    }
                                    if (directUnlock && DEBUG) {
                                        Slog.d(TAG, "refreshAgentList: trustagent " + name + "of user " + userInfo.id + "can unlock user profile.");
                                    }
                                    if (this.mUserManager.isUserUnlockingOrUnlocked(userInfo.id) || directUnlock) {
                                        if (!this.mStrongAuthTracker.canAgentsRunForUser(userInfo.id)) {
                                            int flag = this.mStrongAuthTracker.getStrongAuthForUser(userInfo.id);
                                            if (flag == 8) {
                                                pm = pm2;
                                            } else if (flag == 1 && directUnlock) {
                                                pm = pm2;
                                            } else if (DEBUG) {
                                                Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": prevented by StrongAuthTracker = 0x" + Integer.toHexString(this.mStrongAuthTracker.getStrongAuthForUser(userInfo.id)));
                                                lockPatternUtils = lockPatternUtils;
                                                resolveInfos = resolveInfos;
                                                userInfos = userInfos;
                                                it2 = it;
                                                pm2 = pm2;
                                            } else {
                                                lockPatternUtils = lockPatternUtils;
                                                resolveInfos = resolveInfos;
                                                userInfos = userInfos;
                                                it2 = it;
                                            }
                                        } else {
                                            pm = pm2;
                                        }
                                        if (agentInfo.agent == null) {
                                            agentInfo.agent = new TrustAgentWrapper(this.mContext, this, new Intent().setComponent(name), userInfo.getUserHandle());
                                        }
                                        if (!this.mActiveAgents.contains(agentInfo)) {
                                            this.mActiveAgents.add(agentInfo);
                                        } else {
                                            obsoleteAgents.remove(agentInfo);
                                        }
                                        lockPatternUtils = lockPatternUtils;
                                        resolveInfos = resolveInfos;
                                        userInfos = userInfos;
                                        it2 = it;
                                        pm2 = pm;
                                    } else {
                                        if (DEBUG) {
                                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + "'s trust agent " + name + ": FBE still locked and  the agent cannot unlock user profile.");
                                        }
                                        lockPatternUtils = lockPatternUtils;
                                        resolveInfos = resolveInfos;
                                        userInfos = userInfos;
                                        it2 = it;
                                    }
                                }
                            }
                            z = true;
                        } else if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": no agents enabled by user");
                        }
                    } else if (DEBUG) {
                        Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": no secure credential");
                    }
                }
            }
            boolean trustMayHaveChanged = false;
            for (int i = 0; i < obsoleteAgents.size(); i++) {
                AgentInfo info = obsoleteAgents.valueAt(i);
                if (userIdOrAll2 == -1 || userIdOrAll2 == info.userId) {
                    if (info.agent.isManagingTrust()) {
                        trustMayHaveChanged = true;
                    }
                    info.agent.destroy();
                    this.mActiveAgents.remove(info);
                }
            }
            if (!trustMayHaveChanged) {
                return;
            }
            if (userIdOrAll2 == -1) {
                updateTrustAll();
            } else {
                updateTrust(userIdOrAll2, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceLockedInner(int userId) {
        boolean z;
        synchronized (this.mDeviceLockedForUser) {
            z = this.mDeviceLockedForUser.get(userId, true);
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshDeviceLockedForUser(int userId) {
        List<UserInfo> userInfos;
        if (userId != -1 && userId < 0) {
            Log.e(TAG, "refreshDeviceLockedForUser(userId=" + userId + "): Invalid user handle, must be USER_ALL or a specific user.", new Throwable("here"));
            userId = -1;
        }
        if (userId == -1) {
            userInfos = this.mUserManager.getUsers(true);
        } else {
            userInfos = new ArrayList<>();
            userInfos.add(this.mUserManager.getUserInfo(userId));
        }
        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        for (int i = 0; i < userInfos.size(); i++) {
            UserInfo info = userInfos.get(i);
            if (info != null && !info.partial && info.isEnabled() && !info.guestToRemove) {
                int id = info.id;
                boolean secure = this.mLockPatternUtils.isSecure(id);
                boolean deviceLocked = false;
                if (info.supportsSwitchToByUser()) {
                    boolean trusted = aggregateIsTrusted(id);
                    boolean showingKeyguard = true;
                    boolean biometricAuthenticated = false;
                    if (this.mCurrentUser == id) {
                        synchronized (this.mUsersUnlockedByBiometric) {
                            biometricAuthenticated = this.mUsersUnlockedByBiometric.get(id, false);
                        }
                        try {
                            showingKeyguard = wm.isKeyguardLocked();
                        } catch (RemoteException e) {
                        }
                    }
                    if (secure && showingKeyguard && !trusted && !biometricAuthenticated) {
                        deviceLocked = true;
                    }
                    Log.d(TAG, "refreshDeviceLockedForUser : " + deviceLocked + " " + showingKeyguard);
                    setDeviceLockedForUser(id, deviceLocked);
                } else if (info.isManagedProfile() && !secure) {
                    setDeviceLockedForUser(id, false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDeviceLockedForUser(int userId, boolean locked) {
        int i;
        boolean changed;
        synchronized (this.mDeviceLockedForUser) {
            changed = isDeviceLockedInner(userId) != locked;
            this.mDeviceLockedForUser.put(userId, locked);
        }
        if (changed) {
            dispatchDeviceLocked(userId, locked);
            KeyStore.getInstance().onUserLockedStateChanged(userId, locked);
            int[] enabledProfileIds = this.mUserManager.getEnabledProfileIds(userId);
            for (int profileHandle : enabledProfileIds) {
                if (this.mLockPatternUtils.isManagedProfileWithUnifiedChallenge(profileHandle)) {
                    KeyStore.getInstance().onUserLockedStateChanged(profileHandle, locked);
                }
            }
        }
    }

    private void dispatchDeviceLocked(int userId, boolean isLocked) {
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo agent = this.mActiveAgents.valueAt(i);
            if (agent.userId == userId) {
                if (isLocked) {
                    agent.agent.onDeviceLocked();
                } else {
                    agent.agent.onDeviceUnlocked();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: dispatchEscrowTokenActivatedLocked */
    public void lambda$addEscrowToken$0$TrustManagerService(long handle, int userId) {
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo agent = this.mActiveAgents.valueAt(i);
            if (agent.userId == userId) {
                agent.agent.onEscrowTokenActivated(handle, userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDevicePolicyFeatures() {
        boolean changed = false;
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.agent.isConnected()) {
                info.agent.updateDevicePolicyFeatures();
                changed = true;
            }
        }
        if (changed) {
            this.mArchive.logDevicePolicyChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAgentsOfPackage(String packageName) {
        boolean trustMayHaveChanged = false;
        for (int i = this.mActiveAgents.size() - 1; i >= 0; i--) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (packageName.equals(info.component.getPackageName())) {
                Log.i(TAG, "Resetting agent " + info.component.flattenToShortString());
                if (info.agent.isManagingTrust()) {
                    trustMayHaveChanged = true;
                }
                info.agent.destroy();
                this.mActiveAgents.removeAt(i);
            }
        }
        if (trustMayHaveChanged) {
            updateTrustAll();
        }
    }

    public void resetAgent(ComponentName name, int userId) {
        boolean trustMayHaveChanged = false;
        for (int i = this.mActiveAgents.size() - 1; i >= 0; i--) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (name.equals(info.component) && userId == info.userId) {
                Log.i(TAG, "Resetting agent " + info.component.flattenToShortString());
                if (info.agent.isManagingTrust()) {
                    trustMayHaveChanged = true;
                }
                info.agent.destroy();
                this.mActiveAgents.removeAt(i);
            }
        }
        if (trustMayHaveChanged) {
            updateTrust(userId, 0);
        }
        refreshAgentList(userId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007e, code lost:
        if (0 == 0) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0083, code lost:
        if (0 == 0) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0088, code lost:
        if (0 == 0) goto L_0x008b;
     */
    private SettingsAttrs getSettingsAttrs(PackageManager pm, ResolveInfo resolveInfo) {
        int type;
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return null;
        }
        String cn = null;
        boolean canUnlockProfile = false;
        XmlResourceParser parser = null;
        Exception caughtException = null;
        try {
            parser = resolveInfo.serviceInfo.loadXmlMetaData(pm, "android.service.trust.trustagent");
            if (parser == null) {
                Slog.w(TAG, "Can't find android.service.trust.trustagent meta-data");
                if (parser != null) {
                    parser.close();
                }
                return null;
            }
            Resources res = pm.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                type = parser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if (!"trust-agent".equals(parser.getName())) {
                Slog.w(TAG, "Meta-data does not start with trust-agent tag");
                parser.close();
                return null;
            }
            TypedArray sa = res.obtainAttributes(attrs, R.styleable.TrustAgent);
            cn = sa.getString(2);
            canUnlockProfile = sa.getBoolean(3, false);
            sa.recycle();
            parser.close();
            if (caughtException != null) {
                Slog.w(TAG, "Error parsing : " + resolveInfo.serviceInfo.packageName, caughtException);
                return null;
            } else if (cn == null) {
                return null;
            } else {
                if (cn.indexOf(47) < 0) {
                    cn = resolveInfo.serviceInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + cn;
                }
                return new SettingsAttrs(ComponentName.unflattenFromString(cn), canUnlockProfile);
            }
        } catch (PackageManager.NameNotFoundException e) {
            caughtException = e;
        } catch (IOException e2) {
            caughtException = e2;
        } catch (XmlPullParserException e3) {
            caughtException = e3;
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private ComponentName getComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeEnableFactoryTrustAgents(LockPatternUtils utils, int userId) {
        boolean shouldUseDefaultAgent = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 0, userId) == 0) {
            List<ResolveInfo> resolveInfos = resolveAllowedTrustAgents(this.mContext.getPackageManager(), userId);
            ComponentName defaultAgent = getDefaultFactoryTrustAgent(this.mContext);
            if (defaultAgent != null) {
                shouldUseDefaultAgent = true;
            }
            ArraySet<ComponentName> discoveredAgents = new ArraySet<>();
            if (shouldUseDefaultAgent) {
                discoveredAgents.add(defaultAgent);
                Log.i(TAG, "Enabling " + defaultAgent + " because it is a default agent.");
            } else {
                for (ResolveInfo resolveInfo : resolveInfos) {
                    ComponentName componentName = getComponentName(resolveInfo);
                    if ((resolveInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                        Log.i(TAG, "Leaving agent " + componentName + " disabled because package is not a system package.");
                    } else {
                        discoveredAgents.add(componentName);
                    }
                }
            }
            List<ComponentName> previouslyEnabledAgents = utils.getEnabledTrustAgents(userId);
            if (previouslyEnabledAgents != null) {
                discoveredAgents.addAll(previouslyEnabledAgents);
            }
            utils.setEnabledTrustAgents(discoveredAgents, userId);
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 1, userId);
        }
    }

    private static ComponentName getDefaultFactoryTrustAgent(Context context) {
        String defaultTrustAgent = context.getResources().getString(17039832);
        if (TextUtils.isEmpty(defaultTrustAgent)) {
            return null;
        }
        return ComponentName.unflattenFromString(defaultTrustAgent);
    }

    private List<ResolveInfo> resolveAllowedTrustAgents(PackageManager pm, int userId) {
        List<ResolveInfo> resolveInfos = pm.queryIntentServicesAsUser(TRUST_AGENT_INTENT, 786560, userId);
        ArrayList<ResolveInfo> allowedAgents = new ArrayList<>(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!(resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.applicationInfo == null)) {
                if (pm.checkPermission(PERMISSION_PROVIDE_AGENT, resolveInfo.serviceInfo.packageName) != 0) {
                    ComponentName name = getComponentName(resolveInfo);
                    Log.w(TAG, "Skipping agent " + name + " because package does not have permission " + PERMISSION_PROVIDE_AGENT + ".");
                } else {
                    allowedAgents.add(resolveInfo);
                }
            }
        }
        return allowedAgents;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean aggregateIsTrusted(int userId) {
        if (!this.mStrongAuthTracker.isTrustAllowedForUser(userId)) {
            return false;
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId && info.agent.isTrusted()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean aggregateIsTrustManaged(int userId) {
        if (!this.mStrongAuthTracker.isTrustAllowedForUser(userId)) {
            return false;
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId && info.agent.isManagingTrust()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUnlockAttempt(boolean successful, int userId) {
        if (successful) {
            this.mStrongAuthTracker.allowTrustFromUnlock(userId);
            updateTrust(userId, 0, true);
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId) {
                info.agent.onUnlockAttempt(successful);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUnlockLockout(int timeoutMs, int userId) {
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId) {
                info.agent.onUnlockLockout(timeoutMs);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addListener(ITrustListener listener) {
        if (listener != null) {
            for (int i = 0; i < this.mTrustListeners.size(); i++) {
                if (this.mTrustListeners.get(i).asBinder() == listener.asBinder()) {
                    return;
                }
            }
            this.mTrustListeners.add(listener);
            updateTrustAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeListener(ITrustListener listener) {
        if (listener != null) {
            for (int i = 0; i < this.mTrustListeners.size(); i++) {
                if (this.mTrustListeners.get(i).asBinder() == listener.asBinder()) {
                    this.mTrustListeners.remove(i);
                    return;
                }
            }
        }
    }

    private void dispatchOnTrustChanged(boolean enabled, int userId, int flags) {
        if (DEBUG) {
            Log.i(TAG, "onTrustChanged(" + enabled + ", " + userId + ", 0x" + Integer.toHexString(flags) + ")");
        }
        if (!enabled) {
            flags = 0;
        }
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            try {
                this.mTrustListeners.get(i).onTrustChanged(enabled, userId, flags);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead TrustListener.");
                this.mTrustListeners.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying TrustListener.", e2);
            }
            i++;
        }
    }

    private void dispatchOnTrustManagedChanged(boolean managed, int userId) {
        if (DEBUG) {
            Log.i(TAG, "onTrustManagedChanged(" + managed + ", " + userId + ")");
        }
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            try {
                this.mTrustListeners.get(i).onTrustManagedChanged(managed, userId);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead TrustListener.");
                this.mTrustListeners.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying TrustListener.", e2);
            }
            i++;
        }
    }

    private void dispatchOnTrustError(CharSequence message) {
        if (DEBUG) {
            Log.i(TAG, "onTrustError(" + ((Object) message) + ")");
        }
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            try {
                this.mTrustListeners.get(i).onTrustError(message);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead TrustListener.");
                this.mTrustListeners.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying TrustListener.", e2);
            }
            i++;
        }
    }

    @Override // com.android.server.SystemService
    public void onStartUser(int userId) {
        this.mHandler.obtainMessage(7, userId, 0, null).sendToTarget();
    }

    @Override // com.android.server.SystemService
    public void onCleanupUser(int userId) {
        this.mHandler.obtainMessage(8, userId, 0, null).sendToTarget();
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userId) {
        this.mHandler.obtainMessage(9, userId, 0, null).sendToTarget();
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int userId) {
        this.mHandler.obtainMessage(11, userId, 0, null).sendToTarget();
    }

    @Override // com.android.server.SystemService
    public void onStopUser(int userId) {
        this.mHandler.obtainMessage(12, userId, 0, null).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTrustUsuallyManagedInternal(int userId) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            int i = this.mTrustUsuallyManagedForUser.indexOfKey(userId);
            if (i >= 0) {
                return this.mTrustUsuallyManagedForUser.valueAt(i);
            }
        }
        boolean persistedValue = this.mLockPatternUtils.isTrustUsuallyManaged(userId);
        synchronized (this.mTrustUsuallyManagedForUser) {
            int i2 = this.mTrustUsuallyManagedForUser.indexOfKey(userId);
            if (i2 >= 0) {
                return this.mTrustUsuallyManagedForUser.valueAt(i2);
            }
            this.mTrustUsuallyManagedForUser.put(userId, persistedValue);
            return persistedValue;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int resolveProfileParent(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo parent = this.mUserManager.getProfileParent(userId);
            if (parent != null) {
                return parent.getUserHandle().getIdentifier();
            }
            Binder.restoreCallingIdentity(identity);
            return userId;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public static class SettingsAttrs {
        public boolean canUnlockProfile;
        public ComponentName componentName;

        public SettingsAttrs(ComponentName componentName2, boolean canUnlockProfile2) {
            this.componentName = componentName2;
            this.canUnlockProfile = canUnlockProfile2;
        }
    }

    private class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int userId;
            String action = intent.getAction();
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                TrustManagerService.this.refreshAgentList(getSendingUserId());
                TrustManagerService.this.updateDevicePolicyFeatures();
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                int userId2 = getUserId(intent);
                if (userId2 > 0) {
                    TrustManagerService trustManagerService = TrustManagerService.this;
                    trustManagerService.maybeEnableFactoryTrustAgents(trustManagerService.mLockPatternUtils, userId2);
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action) && (userId = getUserId(intent)) > 0) {
                synchronized (TrustManagerService.this.mUserIsTrusted) {
                    TrustManagerService.this.mUserIsTrusted.delete(userId);
                }
                synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                    TrustManagerService.this.mDeviceLockedForUser.delete(userId);
                }
                synchronized (TrustManagerService.this.mTrustUsuallyManagedForUser) {
                    TrustManagerService.this.mTrustUsuallyManagedForUser.delete(userId);
                }
                synchronized (TrustManagerService.this.mUsersUnlockedByBiometric) {
                    TrustManagerService.this.mUsersUnlockedByBiometric.delete(userId);
                }
                TrustManagerService.this.refreshAgentList(userId);
                TrustManagerService.this.refreshDeviceLockedForUser(userId);
            }
        }

        private int getUserId(Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -100);
            if (userId > 0) {
                return userId;
            }
            Slog.wtf(TrustManagerService.TAG, "EXTRA_USER_HANDLE missing or invalid, value=" + userId);
            return -100;
        }

        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        }
    }

    /* access modifiers changed from: private */
    public class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        SparseBooleanArray mStartFromSuccessfulUnlock = new SparseBooleanArray();

        public StrongAuthTracker(Context context) {
            super(context);
        }

        public void onStrongAuthRequiredChanged(int userId) {
            TrustTimeoutAlarmListener alarm;
            this.mStartFromSuccessfulUnlock.delete(userId);
            if (TrustManagerService.DEBUG) {
                Log.i(TrustManagerService.TAG, "onStrongAuthRequiredChanged(" + userId + ") -> trustAllowed=" + isTrustAllowedForUser(userId) + " agentsCanRun=" + canAgentsRunForUser(userId));
            }
            if (!isTrustAllowedForUser(userId) && (alarm = (TrustTimeoutAlarmListener) TrustManagerService.this.mTrustTimeoutAlarmListenerForUser.get(Integer.valueOf(userId))) != null && alarm.isQueued()) {
                alarm.setQueued(false);
                TrustManagerService.this.mAlarmManager.cancel(alarm);
            }
            TrustManagerService.this.refreshAgentList(userId);
            TrustManagerService.this.updateTrust(userId, 0);
        }

        /* access modifiers changed from: package-private */
        public boolean canAgentsRunForUser(int userId) {
            return this.mStartFromSuccessfulUnlock.get(userId) || TrustManagerService.super.isTrustAllowedForUser(userId);
        }

        /* access modifiers changed from: package-private */
        public void allowTrustFromUnlock(int userId) {
            if (userId >= 0) {
                boolean previous = canAgentsRunForUser(userId);
                this.mStartFromSuccessfulUnlock.put(userId, true);
                if (TrustManagerService.DEBUG) {
                    Log.i(TrustManagerService.TAG, "allowTrustFromUnlock(" + userId + ") -> trustAllowed=" + isTrustAllowedForUser(userId) + " agentsCanRun=" + canAgentsRunForUser(userId));
                }
                if (canAgentsRunForUser(userId) != previous) {
                    TrustManagerService.this.refreshAgentList(userId);
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("userId must be a valid user: " + userId);
        }
    }

    /* access modifiers changed from: private */
    public class TrustTimeoutAlarmListener implements AlarmManager.OnAlarmListener {
        private boolean mIsQueued = false;
        private final int mUserId;

        TrustTimeoutAlarmListener(int userId) {
            this.mUserId = userId;
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            this.mIsQueued = false;
            TrustManagerService.this.mStrongAuthTracker.getStrongAuthForUser(this.mUserId);
            if (TrustManagerService.this.mStrongAuthTracker.isTrustAllowedForUser(this.mUserId)) {
                if (TrustManagerService.DEBUG) {
                    Slog.d(TrustManagerService.TAG, "Revoking all trust because of trust timeout");
                }
                LockPatternUtils lockPatternUtils = TrustManagerService.this.mLockPatternUtils;
                StrongAuthTracker unused = TrustManagerService.this.mStrongAuthTracker;
                lockPatternUtils.requireStrongAuth(4, this.mUserId);
            }
            TrustManagerService.this.maybeLockScreen(this.mUserId);
        }

        public void setQueued(boolean isQueued) {
            this.mIsQueued = isQueued;
        }

        public boolean isQueued() {
            return this.mIsQueued;
        }
    }
}
