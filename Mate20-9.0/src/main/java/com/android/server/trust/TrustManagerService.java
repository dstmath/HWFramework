package com.android.server.trust;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.ITrustListener;
import android.app.trust.ITrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
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
import android.text.TextUtils;
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
import huawei.cust.HwCustUtils;
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
    private static final int MSG_START_USER = 7;
    private static final int MSG_STOP_USER = 12;
    private static final int MSG_SWITCH_USER = 9;
    private static final int MSG_UNLOCK_USER = 11;
    private static final int MSG_UNREGISTER_LISTENER = 2;
    private static final String PERMISSION_PROVIDE_AGENT = "android.permission.PROVIDE_TRUST_AGENT";
    private static final String TAG = "TrustManagerService";
    private static final Intent TRUST_AGENT_INTENT = new Intent("android.service.trust.TrustAgentService");
    private static final int TRUST_USUALLY_MANAGED_FLUSH_DELAY = 120000;
    /* access modifiers changed from: private */
    public final ArraySet<AgentInfo> mActiveAgents = new ArraySet<>();
    private final ActivityManager mActivityManager;
    final TrustArchive mArchive = new TrustArchive();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUser = 0;
    private HwCustTrustManagerService mCust;
    /* access modifiers changed from: private */
    @GuardedBy("mDeviceLockedForUser")
    public final SparseBooleanArray mDeviceLockedForUser = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
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
                case 6:
                    TrustManagerService.this.refreshDeviceLockedForUser(TrustManagerService.this.mCurrentUser);
                    return;
                case 7:
                case 8:
                case 11:
                    TrustManagerService.this.refreshAgentList(msg.arg1);
                    return;
                case 9:
                    int unused = TrustManagerService.this.mCurrentUser = msg.arg1;
                    TrustManagerService.this.refreshDeviceLockedForUser(-1);
                    return;
                case 10:
                    synchronized (TrustManagerService.this.mTrustUsuallyManagedForUser) {
                        usuallyManaged = TrustManagerService.this.mTrustUsuallyManagedForUser.clone();
                    }
                    while (true) {
                        int i = z;
                        if (i < usuallyManaged.size()) {
                            int userId = usuallyManaged.keyAt(i);
                            boolean value = usuallyManaged.valueAt(i);
                            if (value != TrustManagerService.this.mLockPatternUtils.isTrustUsuallyManaged(userId)) {
                                TrustManagerService.this.mLockPatternUtils.setTrustUsuallyManaged(value, userId);
                            }
                            z = i + 1;
                        } else {
                            return;
                        }
                    }
                case 12:
                    TrustManagerService.this.setDeviceLockedForUser(msg.arg1, true);
                    return;
                case 13:
                    TrustManagerService.this.dispatchUnlockLockout(msg.arg1, msg.arg2);
                    return;
                case 14:
                    TrustManagerService.this.refreshDeviceLockedForUser(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public final LockPatternUtils mLockPatternUtils;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
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
        public void reportUnlockAttempt(boolean authenticated, int userId) throws RemoteException {
            enforceReportPermission();
            TrustManagerService.this.mHandler.obtainMessage(3, authenticated, userId).sendToTarget();
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
        public void dumpUser(PrintWriter fout, UserInfo user, boolean isCurrent) {
            fout.printf(" User \"%s\" (id=%d, flags=%#x)", new Object[]{user.name, Integer.valueOf(user.id), Integer.valueOf(user.flags)});
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
                        fout.println("      message=\"" + info.agent.getMessage() + "\"");
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
                if (TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
                    synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                        TrustManagerService.this.mDeviceLockedForUser.put(userId, locked);
                    }
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public boolean isTrustUsuallyManaged(int userId) {
            TrustManagerService.this.mContext.enforceCallingPermission("android.permission.TRUST_LISTENER", "query trust state");
            return TrustManagerService.this.isTrustUsuallyManagedInternal(userId);
        }

        public void unlockedByFingerprintForUser(int userId) {
            enforceReportPermission();
            synchronized (TrustManagerService.this.mUsersUnlockedByFingerprint) {
                TrustManagerService.this.mUsersUnlockedByFingerprint.put(userId, true);
            }
            TrustManagerService.this.mHandler.obtainMessage(14, userId, 0).sendToTarget();
        }

        public void clearAllFingerprints() {
            enforceReportPermission();
            synchronized (TrustManagerService.this.mUsersUnlockedByFingerprint) {
                TrustManagerService.this.mUsersUnlockedByFingerprint.clear();
            }
            TrustManagerService.this.mHandler.obtainMessage(14, -1, 0).sendToTarget();
        }
    };
    /* access modifiers changed from: private */
    public final StrongAuthTracker mStrongAuthTracker;
    /* access modifiers changed from: private */
    public boolean mTrustAgentsCanRun = false;
    private final ArrayList<ITrustListener> mTrustListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    @GuardedBy("mTrustUsuallyManagedForUser")
    public final SparseBooleanArray mTrustUsuallyManagedForUser = new SparseBooleanArray();
    /* access modifiers changed from: private */
    @GuardedBy("mUserIsTrusted")
    public final SparseBooleanArray mUserIsTrusted = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public final UserManager mUserManager;
    /* access modifiers changed from: private */
    @GuardedBy("mUsersUnlockedByFingerprint")
    public final SparseBooleanArray mUsersUnlockedByFingerprint = new SparseBooleanArray();

    private static final class AgentInfo {
        TrustAgentWrapper agent;
        ComponentName component;
        Drawable icon;
        CharSequence label;
        SettingsAttrs settings;
        int userId;

        private AgentInfo() {
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof AgentInfo)) {
                return false;
            }
            AgentInfo o = (AgentInfo) other;
            if (this.component.equals(o.component) && this.userId == o.userId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.component.hashCode() * 31) + this.userId;
        }
    }

    private class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                TrustManagerService.this.refreshAgentList(getSendingUserId());
                TrustManagerService.this.updateDevicePolicyFeatures();
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                int userId = getUserId(intent);
                if (userId > 0) {
                    TrustManagerService.this.maybeEnableFactoryTrustAgents(TrustManagerService.this.mLockPatternUtils, userId);
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                int userId2 = getUserId(intent);
                if (userId2 > 0) {
                    synchronized (TrustManagerService.this.mUserIsTrusted) {
                        TrustManagerService.this.mUserIsTrusted.delete(userId2);
                    }
                    synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                        TrustManagerService.this.mDeviceLockedForUser.delete(userId2);
                    }
                    synchronized (TrustManagerService.this.mTrustUsuallyManagedForUser) {
                        TrustManagerService.this.mTrustUsuallyManagedForUser.delete(userId2);
                    }
                    synchronized (TrustManagerService.this.mUsersUnlockedByFingerprint) {
                        TrustManagerService.this.mUsersUnlockedByFingerprint.delete(userId2);
                    }
                    TrustManagerService.this.refreshAgentList(userId2);
                    TrustManagerService.this.refreshDeviceLockedForUser(userId2);
                }
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

    private static class SettingsAttrs {
        public boolean canUnlockProfile;
        public ComponentName componentName;

        public SettingsAttrs(ComponentName componentName2, boolean canUnlockProfile2) {
            this.componentName = componentName2;
            this.canUnlockProfile = canUnlockProfile2;
        }
    }

    private class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        SparseBooleanArray mStartFromSuccessfulUnlock = new SparseBooleanArray();

        public StrongAuthTracker(Context context) {
            super(context);
        }

        public void onStrongAuthRequiredChanged(int userId) {
            this.mStartFromSuccessfulUnlock.delete(userId);
            if (TrustManagerService.DEBUG) {
                Log.i(TrustManagerService.TAG, "onStrongAuthRequiredChanged(" + userId + ") -> trustAllowed=" + isTrustAllowedForUser(userId) + " agentsCanRun=" + canAgentsRunForUser(userId));
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

    /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.trust.TrustManagerService$1, android.os.IBinder] */
    public TrustManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mStrongAuthTracker = new StrongAuthTracker(context);
        this.mCust = (HwCustTrustManagerService) HwCustUtils.createObj(HwCustTrustManagerService.class, new Object[0]);
    }

    public void onStart() {
        publishBinderService("trust", this.mService);
    }

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

    private void updateTrustAll() {
        for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
            updateTrust(userInfo.id, 0);
        }
    }

    public void updateTrust(int userId, int flags) {
        boolean changed;
        boolean managed = aggregateIsTrustManaged(userId);
        dispatchOnTrustManagedChanged(managed, userId);
        if (this.mStrongAuthTracker.isTrustAllowedForUser(userId) && isTrustUsuallyManagedInternal(userId) != managed) {
            updateTrustUsuallyManaged(userId, managed);
        }
        boolean trusted = aggregateIsTrusted(userId);
        synchronized (this.mUserIsTrusted) {
            changed = this.mUserIsTrusted.get(userId) != trusted;
            this.mUserIsTrusted.put(userId, trusted);
        }
        dispatchOnTrustChanged(trusted, userId, flags);
        if (changed) {
            refreshDeviceLockedForUser(userId);
        }
    }

    private void updateTrustUsuallyManaged(int userId, boolean managed) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            this.mTrustUsuallyManagedForUser.put(userId, managed);
        }
        this.mHandler.removeMessages(10);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
    }

    public long addEscrowToken(byte[] token, int userId) {
        return this.mLockPatternUtils.addEscrowToken(token, userId);
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
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0328  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0346  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x034c  */
    public void refreshAgentList(int userIdOrAll) {
        List<UserInfo> userInfos;
        List<ResolveInfo> resolveInfos;
        List<UserInfo> userInfos2;
        LockPatternUtils lockPatternUtils;
        PackageManager pm;
        List<UserInfo> userInfos3;
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
            LockPatternUtils lockPatternUtils2 = this.mLockPatternUtils;
            ArraySet<AgentInfo> obsoleteAgents = new ArraySet<>();
            obsoleteAgents.addAll(this.mActiveAgents);
            for (UserInfo userInfo : userInfos) {
                if (userInfo != null && !userInfo.partial && userInfo.isEnabled() && !userInfo.guestToRemove) {
                    if (!userInfo.supportsSwitchToByUser()) {
                        if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": switchToByUser=false");
                        }
                    } else if (!this.mActivityManager.isUserRunning(userInfo.id)) {
                        if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": user not started");
                        }
                    } else if (lockPatternUtils2.isSecure(userInfo.id)) {
                        DevicePolicyManager dpm = lockPatternUtils2.getDevicePolicyManager();
                        boolean disableTrustAgents = (dpm.getKeyguardDisabledFeatures(null, userInfo.id) & 16) != 0 ? z : false;
                        List<ComponentName> enabledAgents = lockPatternUtils2.getEnabledTrustAgents(userInfo.id);
                        if (enabledAgents != null) {
                            List<ResolveInfo> resolveInfos2 = resolveAllowedTrustAgents(pm2, userInfo.id);
                            for (ResolveInfo resolveInfo : resolveInfos2) {
                                ComponentName name = getComponentName(resolveInfo);
                                if (enabledAgents.contains(name)) {
                                    resolveInfos = resolveInfos2;
                                    userInfos2 = userInfos;
                                    lockPatternUtils = lockPatternUtils2;
                                    if (disableTrustAgents) {
                                        List<PersistableBundle> config = dpm.getTrustAgentConfiguration(null, name, userInfo.id);
                                        if (config == null || config.isEmpty()) {
                                            if (DEBUG) {
                                                StringBuilder sb = new StringBuilder();
                                                List<PersistableBundle> list = config;
                                                sb.append("refreshAgentList: skipping ");
                                                sb.append(name.flattenToShortString());
                                                sb.append(" u");
                                                sb.append(userInfo.id);
                                                sb.append(": not allowed by DPM");
                                                Slog.d(TAG, sb.toString());
                                            }
                                        }
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
                                    boolean directUnlock = resolveInfo.serviceInfo.directBootAware && agentInfo.settings.canUnlockProfile;
                                    if (!directUnlock || !DEBUG) {
                                        pm = pm2;
                                        ResolveInfo resolveInfo2 = resolveInfo;
                                    } else {
                                        pm = pm2;
                                        StringBuilder sb2 = new StringBuilder();
                                        ResolveInfo resolveInfo3 = resolveInfo;
                                        sb2.append("refreshAgentList: trustagent ");
                                        sb2.append(name);
                                        sb2.append("of user ");
                                        sb2.append(userInfo.id);
                                        sb2.append("can unlock user profile.");
                                        Slog.d(TAG, sb2.toString());
                                    }
                                    if (this.mUserManager.isUserUnlockingOrUnlocked(userInfo.id) || directUnlock) {
                                        if (!this.mStrongAuthTracker.canAgentsRunForUser(userInfo.id)) {
                                            int flag = this.mStrongAuthTracker.getStrongAuthForUser(userInfo.id);
                                            if (flag != 8) {
                                                if (flag == 1 && directUnlock) {
                                                    boolean z2 = directUnlock;
                                                    if (agentInfo.agent == null) {
                                                        agentInfo.agent = new TrustAgentWrapper(this.mContext, this, new Intent().setComponent(name), userInfo.getUserHandle());
                                                    }
                                                    if (this.mActiveAgents.contains(agentInfo)) {
                                                        this.mActiveAgents.add(agentInfo);
                                                    } else {
                                                        obsoleteAgents.remove(agentInfo);
                                                    }
                                                } else if (DEBUG) {
                                                    StringBuilder sb3 = new StringBuilder();
                                                    int i = flag;
                                                    sb3.append("refreshAgentList: skipping user ");
                                                    sb3.append(userInfo.id);
                                                    sb3.append(": prevented by StrongAuthTracker = 0x");
                                                    boolean z3 = directUnlock;
                                                    sb3.append(Integer.toHexString(this.mStrongAuthTracker.getStrongAuthForUser(userInfo.id)));
                                                    Slog.d(TAG, sb3.toString());
                                                } else {
                                                    resolveInfos2 = resolveInfos;
                                                    userInfos3 = userInfos2;
                                                    lockPatternUtils2 = lockPatternUtils;
                                                    pm2 = pm;
                                                }
                                            }
                                        }
                                        boolean z4 = directUnlock;
                                        if (agentInfo.agent == null) {
                                        }
                                        if (this.mActiveAgents.contains(agentInfo)) {
                                        }
                                    } else if (DEBUG) {
                                        Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + "'s trust agent " + name + ": FBE still locked and  the agent cannot unlock user profile.");
                                    }
                                    resolveInfos2 = resolveInfos;
                                    userInfos3 = userInfos2;
                                    lockPatternUtils2 = lockPatternUtils;
                                    pm2 = pm;
                                } else if (DEBUG) {
                                    resolveInfos = resolveInfos2;
                                    userInfos2 = userInfos;
                                    StringBuilder sb4 = new StringBuilder();
                                    lockPatternUtils = lockPatternUtils2;
                                    sb4.append("refreshAgentList: skipping ");
                                    sb4.append(name.flattenToShortString());
                                    sb4.append(" u");
                                    sb4.append(userInfo.id);
                                    sb4.append(": not enabled by user");
                                    Slog.d(TAG, sb4.toString());
                                }
                                resolveInfos2 = resolveInfos;
                                userInfos3 = userInfos2;
                                lockPatternUtils2 = lockPatternUtils;
                            }
                            List<UserInfo> list2 = userInfos;
                            LockPatternUtils lockPatternUtils3 = lockPatternUtils2;
                            z = true;
                        } else if (DEBUG) {
                            Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": no agents enabled by user");
                        }
                    } else if (DEBUG) {
                        Slog.d(TAG, "refreshAgentList: skipping user " + userInfo.id + ": no secure credential");
                    }
                }
            }
            List<UserInfo> list3 = userInfos;
            LockPatternUtils lockPatternUtils4 = lockPatternUtils2;
            boolean trustMayHaveChanged = false;
            for (int i2 = 0; i2 < obsoleteAgents.size(); i2++) {
                AgentInfo info = obsoleteAgents.valueAt(i2);
                if (userIdOrAll2 == -1 || userIdOrAll2 == info.userId) {
                    if (info.agent.isManagingTrust()) {
                        trustMayHaveChanged = true;
                    }
                    info.agent.destroy();
                    this.mActiveAgents.remove(info);
                }
            }
            if (trustMayHaveChanged) {
                if (userIdOrAll2 == -1) {
                    updateTrustAll();
                } else {
                    updateTrust(userIdOrAll2, 0);
                }
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
    public void refreshDeviceLockedForUser(int userId) {
        List<UserInfo> userInfos;
        boolean deviceLocked;
        int i = userId;
        if (i != -1 && i < 0) {
            Log.e(TAG, "refreshDeviceLockedForUser(userId=" + i + "): Invalid user handle, must be USER_ALL or a specific user.", new Throwable("here"));
            i = -1;
        }
        int userId2 = i;
        boolean z = true;
        if (userId2 == -1) {
            userInfos = this.mUserManager.getUsers(true);
        } else {
            userInfos = new ArrayList<>();
            userInfos.add(this.mUserManager.getUserInfo(userId2));
        }
        List<UserInfo> userInfos2 = userInfos;
        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < userInfos2.size()) {
                UserInfo info = userInfos2.get(i3);
                if (info != null && !info.partial && info.isEnabled() && !info.guestToRemove && info.supportsSwitchToByUser()) {
                    int id = info.id;
                    boolean secure = this.mLockPatternUtils.isSecure(id);
                    boolean trusted = aggregateIsTrusted(id);
                    boolean showingKeyguard = true;
                    boolean fingerprintAuthenticated = false;
                    if (this.mCurrentUser == id) {
                        synchronized (this.mUsersUnlockedByFingerprint) {
                            fingerprintAuthenticated = this.mUsersUnlockedByFingerprint.get(id, false);
                        }
                        try {
                            showingKeyguard = wm.isKeyguardLocked();
                        } catch (RemoteException e) {
                        }
                    }
                    Log.d(TAG, "refreshDeviceLockedForUser : " + deviceLocked + " " + showingKeyguard);
                    setDeviceLockedForUser(id, deviceLocked);
                }
                i2 = i3 + 1;
                z = true;
            } else {
                return;
            }
        }
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    public void setDeviceLockedForUser(int userId, boolean locked) {
        boolean changed;
        synchronized (this.mDeviceLockedForUser) {
            changed = isDeviceLockedInner(userId) != locked;
            this.mDeviceLockedForUser.put(userId, locked);
        }
        if (changed) {
            dispatchDeviceLocked(userId, locked);
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
    public void removeAgentsOfPackage(String packageName) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0074, code lost:
        if (r3 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0076, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0083, code lost:
        if (r3 == null) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0088, code lost:
        if (r3 == null) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008d, code lost:
        if (r3 == null) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0090, code lost:
        if (r4 == null) goto L_0x00ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0092, code lost:
        android.util.Slog.w(TAG, "Error parsing : " + r14.serviceInfo.packageName, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ac, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ad, code lost:
        if (r1 != null) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00af, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b6, code lost:
        if (r1.indexOf(47) >= 0) goto L_0x00d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b8, code lost:
        r1 = r14.serviceInfo.packageName + com.android.server.slice.SliceClientPermissions.SliceAuthority.DELIMITER + r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d9, code lost:
        return new com.android.server.trust.TrustManagerService.SettingsAttrs(android.content.ComponentName.unflattenFromString(r1), r2);
     */
    private SettingsAttrs getSettingsAttrs(PackageManager pm, ResolveInfo resolveInfo) {
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
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || type == 2) {
                }
            }
            if (!"trust-agent".equals(parser.getName())) {
                Slog.w(TAG, "Meta-data does not start with trust-agent tag");
                if (parser != null) {
                    parser.close();
                }
                return null;
            }
            TypedArray sa = res.obtainAttributes(attrs, R.styleable.TrustAgent);
            cn = sa.getString(2);
            canUnlockProfile = sa.getBoolean(3, false);
            sa.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            caughtException = e;
        } catch (IOException e2) {
            caughtException = e2;
        } catch (XmlPullParserException e3) {
            caughtException = e3;
        } catch (Throwable th) {
            if (parser != null) {
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
    public void maybeEnableFactoryTrustAgents(LockPatternUtils utils, int userId) {
        int i = userId;
        boolean shouldUseDefaultAgent = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 0, i) == 0) {
            ComponentName mGoogleSmartLock = new ComponentName("com.google.android.gms", "com.google.android.gms.auth.trustagent.GoogleTrustAgent");
            List<ResolveInfo> resolveInfos = resolveAllowedTrustAgents(this.mContext.getPackageManager(), i);
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
            if (discoveredAgents.contains(mGoogleSmartLock) && (this.mCust == null || !this.mCust.isShowSmartLcok())) {
                discoveredAgents.remove(mGoogleSmartLock);
            }
            List<ComponentName> previouslyEnabledAgents = utils.getEnabledTrustAgents(userId);
            if (previouslyEnabledAgents != null) {
                discoveredAgents.addAll(previouslyEnabledAgents);
            }
            utils.setEnabledTrustAgents(discoveredAgents, i);
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 1, i);
        }
    }

    private static ComponentName getDefaultFactoryTrustAgent(Context context) {
        String defaultTrustAgent = context.getResources().getString(17039792);
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
    public boolean aggregateIsTrusted(int userId) {
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
    public boolean aggregateIsTrustManaged(int userId) {
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
    public void dispatchUnlockAttempt(boolean successful, int userId) {
        if (successful) {
            this.mStrongAuthTracker.allowTrustFromUnlock(userId);
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId) {
                info.agent.onUnlockAttempt(successful);
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchUnlockLockout(int timeoutMs, int userId) {
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = this.mActiveAgents.valueAt(i);
            if (info.userId == userId) {
                info.agent.onUnlockLockout(timeoutMs);
            }
        }
    }

    /* access modifiers changed from: private */
    public void addListener(ITrustListener listener) {
        if (listener == null) {
            Log.i(TAG, "addListener, listener is null, just return");
            return;
        }
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            if (this.mTrustListeners.get(i).asBinder() != listener.asBinder()) {
                i++;
            } else {
                return;
            }
        }
        this.mTrustListeners.add(listener);
        updateTrustAll();
    }

    /* access modifiers changed from: private */
    public void removeListener(ITrustListener listener) {
        if (listener == null) {
            Log.i(TAG, "removeListener, listener is null, just return");
            return;
        }
        for (int i = 0; i < this.mTrustListeners.size(); i++) {
            if (this.mTrustListeners.get(i).asBinder() == listener.asBinder()) {
                this.mTrustListeners.remove(i);
                return;
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
            Log.i(TAG, "onTrustError(" + message + ")");
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

    public void onStartUser(int userId) {
        this.mHandler.obtainMessage(7, userId, 0, null).sendToTarget();
    }

    public void onCleanupUser(int userId) {
        this.mHandler.obtainMessage(8, userId, 0, null).sendToTarget();
    }

    public void onSwitchUser(int userId) {
        this.mHandler.obtainMessage(9, userId, 0, null).sendToTarget();
    }

    public void onUnlockUser(int userId) {
        this.mHandler.obtainMessage(11, userId, 0, null).sendToTarget();
    }

    public void onStopUser(int userId) {
        this.mHandler.obtainMessage(12, userId, 0, null).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0 = r4.mTrustUsuallyManagedForUser.indexOfKey(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        if (r0 < 0) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        r3 = r4.mTrustUsuallyManagedForUser.valueAt(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        r4.mTrustUsuallyManagedForUser.put(r5, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0032, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        r1 = r4.mLockPatternUtils.isTrustUsuallyManaged(r5);
        r2 = r4.mTrustUsuallyManagedForUser;
     */
    public boolean isTrustUsuallyManagedInternal(int userId) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            int i = this.mTrustUsuallyManagedForUser.indexOfKey(userId);
            if (i >= 0) {
                boolean valueAt = this.mTrustUsuallyManagedForUser.valueAt(i);
                return valueAt;
            }
        }
    }

    /* access modifiers changed from: private */
    public int resolveProfileParent(int userId) {
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
}
