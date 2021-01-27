package com.android.server.biometrics;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppOpsManager;
import android.app.IActivityTaskManager;
import android.app.SynchronousUserSwitchObserver;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricService;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.Fingerprint;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.server.ServiceThread;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.FingerprintUtils;
import com.android.server.fingerprint.AbsFingerprintService;
import com.android.server.slice.SliceClientPermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BiometricServiceBase extends AbsFingerprintService implements IHwBinder.DeathRecipient {
    private static final long CANCEL_TIMEOUT_LIMIT = 3000;
    private static final boolean CLEANUP_UNKNOWN_TEMPLATES = true;
    private static final String COAUTH_SERVICE_PKG = "com.huawei.coauthservice";
    protected static final boolean DEBUG = true;
    private static final int FP_CLOSE = 0;
    protected static final int HIDDEN_SPACE_ID = -100;
    private static final String KEY_LOCKOUT_RESET_USER = "lockout_reset_user";
    private static final int MSG_USER_SWITCHING = 10;
    protected static final int PRIMARY_USER_ID = 0;
    private final IActivityTaskManager mActivityTaskManager;
    protected final AppOpsManager mAppOps;
    protected final Map<Integer, Long> mAuthenticatorIds = Collections.synchronizedMap(new HashMap());
    private IBiometricService mBiometricService;
    private final Context mContext;
    protected HashMap<Integer, PerformanceStats> mCryptoPerformanceMap = new HashMap<>();
    protected ClientMonitor mCurrentClient;
    protected int mCurrentUserId = -10000;
    protected int mHALDeathCount;
    protected long mHalDeviceId;
    protected final H mHandler;
    protected boolean mIsCrypto;
    protected final String mKeyguardPackage;
    private final ArrayList<LockoutResetMonitor> mLockoutMonitors = new ArrayList<>();
    private final MetricsLogger mMetricsLogger;
    protected ClientMonitor mPendingClient;
    protected HashMap<Integer, PerformanceStats> mPerformanceMap = new HashMap<>();
    private PerformanceStats mPerformanceStats;
    private final PowerManager mPowerManager;
    private final ResetClientStateRunnable mResetClientState = new ResetClientStateRunnable();
    protected final IStatusBarService mStatusBarService;
    private final BiometricTaskStackListener mTaskStackListener = new BiometricTaskStackListener();
    private final IBinder mToken = new Binder();
    private final ArrayList<UserTemplate> mUnknownHALTemplates = new ArrayList<>();
    public final UserManager mUserManager;

    /* access modifiers changed from: protected */
    public interface DaemonWrapper {
        public static final int ERROR_ESRCH = 3;

        int authenticate(long j, int i) throws RemoteException;

        int cancel() throws RemoteException;

        int enroll(byte[] bArr, int i, int i2, ArrayList<Integer> arrayList) throws RemoteException;

        int enumerate() throws RemoteException;

        int remove(int i, int i2) throws RemoteException;

        void resetLockout(byte[] bArr) throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract boolean checkAppOps(int i, String str);

    /* access modifiers changed from: protected */
    public abstract void checkUseBiometricPermission();

    /* access modifiers changed from: protected */
    public abstract BiometricUtils getBiometricUtils();

    /* access modifiers changed from: protected */
    public abstract Constants getConstants();

    /* access modifiers changed from: protected */
    public abstract DaemonWrapper getDaemonWrapper();

    /* access modifiers changed from: protected */
    public abstract List<? extends BiometricAuthenticator.Identifier> getEnrolledTemplates(int i);

    /* access modifiers changed from: protected */
    public abstract long getHalDeviceId();

    /* access modifiers changed from: protected */
    public abstract String getLockoutBroadcastPermission();

    /* access modifiers changed from: protected */
    public abstract int getLockoutMode();

    /* access modifiers changed from: protected */
    public abstract String getLockoutResetIntent();

    /* access modifiers changed from: protected */
    public abstract String getManageBiometricPermission();

    /* access modifiers changed from: protected */
    public abstract String getTag();

    /* access modifiers changed from: protected */
    public abstract boolean hasEnrolledBiometrics(int i);

    /* access modifiers changed from: protected */
    public abstract boolean hasReachedEnrollmentLimit(int i);

    /* access modifiers changed from: protected */
    public abstract int statsModality();

    /* access modifiers changed from: protected */
    public abstract void updateActiveGroup(int i, String str);

    /* access modifiers changed from: protected */
    public class PerformanceStats {
        public int accept;
        public int acquire;
        public int lockout;
        public int permanentLockout;
        public int reject;

        protected PerformanceStats() {
        }
    }

    /* access modifiers changed from: protected */
    public void notifyClientActiveCallbacks(boolean isActive) {
    }

    /* access modifiers changed from: protected */
    public abstract class AuthenticationClientImpl extends AuthenticationClient {
        /* access modifiers changed from: protected */
        public boolean isFingerprint() {
            return false;
        }

        public AuthenticationClientImpl(Context context, DaemonWrapper daemon, long halDeviceId, IBinder token, ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
            super(context, BiometricServiceBase.this.getConstants(), daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsClient() {
            if (BiometricServiceBase.this.isKeyguard(getOwnerString())) {
                return 1;
            }
            if (isBiometricPrompt()) {
                return 2;
            }
            if (isFingerprint()) {
                return 3;
            }
            return 0;
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public void onStart() {
            try {
                BiometricServiceBase.this.mActivityTaskManager.registerTaskStackListener(BiometricServiceBase.this.mTaskStackListener);
            } catch (RemoteException e) {
                Slog.e(BiometricServiceBase.this.getTag(), "Could not register task stack listener", e);
            }
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public void onStop() {
            try {
                BiometricServiceBase.this.mActivityTaskManager.unregisterTaskStackListener(BiometricServiceBase.this.mTaskStackListener);
            } catch (RemoteException e) {
                Slog.e(BiometricServiceBase.this.getTag(), "Could not unregister task stack listener", e);
            }
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void notifyUserActivity() {
            BiometricServiceBase.this.userActivity();
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public int handleFailedAttempt() {
            int lockoutMode = BiometricServiceBase.this.getLockoutMode();
            if (lockoutMode == 2) {
                BiometricServiceBase.this.mPerformanceStats.permanentLockout++;
            } else if (lockoutMode == 1) {
                BiometricServiceBase.this.mPerformanceStats.lockout++;
            }
            if (lockoutMode != 0) {
                return lockoutMode;
            }
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public abstract class EnrollClientImpl extends EnrollClient {
        public EnrollClientImpl(Context context, DaemonWrapper daemon, long halDeviceId, IBinder token, ServiceListener listener, int userId, int groupId, byte[] cryptoToken, boolean restricted, String owner, int[] disabledFeatures) {
            super(context, BiometricServiceBase.this.getConstants(), daemon, halDeviceId, token, listener, userId, groupId, cryptoToken, restricted, owner, BiometricServiceBase.this.getBiometricUtils(), disabledFeatures);
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void notifyUserActivity() {
            BiometricServiceBase.this.userActivity();
        }
    }

    /* access modifiers changed from: protected */
    public final class InternalRemovalClient extends RemovalClient {
        InternalRemovalClient(Context context, DaemonWrapper daemon, long halDeviceId, IBinder token, ServiceListener listener, int templateId, int groupId, int userId, boolean restricted, String owner) {
            super(context, BiometricServiceBase.this.getConstants(), daemon, halDeviceId, token, listener, templateId, groupId, userId, restricted, owner, BiometricServiceBase.this.getBiometricUtils());
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return BiometricServiceBase.this.statsModality();
        }
    }

    /* access modifiers changed from: private */
    public final class InternalEnumerateClient extends EnumerateClient {
        private List<? extends BiometricAuthenticator.Identifier> mEnrolledList;
        private List<BiometricAuthenticator.Identifier> mUnknownHALTemplates = new ArrayList();
        private BiometricUtils mUtils;

        InternalEnumerateClient(Context context, DaemonWrapper daemon, long halDeviceId, IBinder token, ServiceListener listener, int groupId, int userId, boolean restricted, String owner, List<? extends BiometricAuthenticator.Identifier> enrolledList, BiometricUtils utils) {
            super(context, BiometricServiceBase.this.getConstants(), daemon, halDeviceId, token, listener, groupId, userId, restricted, owner);
            this.mEnrolledList = enrolledList;
            this.mUtils = utils;
        }

        private void handleEnumeratedTemplate(BiometricAuthenticator.Identifier identifier) {
            if (identifier != null) {
                String tag = BiometricServiceBase.this.getTag();
                Slog.v(tag, "handleEnumeratedTemplate: " + identifier.getBiometricId());
                boolean matched = false;
                int i = 0;
                while (true) {
                    if (i >= this.mEnrolledList.size()) {
                        break;
                    } else if (((BiometricAuthenticator.Identifier) this.mEnrolledList.get(i)).getBiometricId() == identifier.getBiometricId()) {
                        this.mEnrolledList.remove(i);
                        matched = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!matched && identifier.getBiometricId() != 0) {
                    this.mUnknownHALTemplates.add(identifier);
                }
                String tag2 = BiometricServiceBase.this.getTag();
                Slog.v(tag2, "Matched: " + matched);
            }
        }

        private void doTemplateCleanup() {
            if (this.mEnrolledList != null) {
                for (int i = 0; i < this.mEnrolledList.size(); i++) {
                    BiometricAuthenticator.Identifier identifier = (BiometricAuthenticator.Identifier) this.mEnrolledList.get(i);
                    String tag = BiometricServiceBase.this.getTag();
                    Slog.e(tag, "doTemplateCleanup(): Removing dangling template from framework: " + identifier.getBiometricId() + " " + ((Object) identifier.getName()));
                    this.mUtils.removeBiometricForUser(getContext(), getTargetUserId(), identifier.getBiometricId());
                    StatsLog.write(148, statsModality(), 2);
                }
                this.mEnrolledList.clear();
            }
        }

        public List<BiometricAuthenticator.Identifier> getUnknownHALTemplates() {
            return this.mUnknownHALTemplates;
        }

        @Override // com.android.server.biometrics.EnumerateClient, com.android.server.biometrics.ClientMonitor
        public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
            handleEnumeratedTemplate(identifier);
            if (remaining == 0) {
                doTemplateCleanup();
            }
            return remaining == 0;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return BiometricServiceBase.this.statsModality();
        }
    }

    public interface ServiceListener {
        void onAcquired(long j, int i, int i2) throws RemoteException;

        void onError(long j, int i, int i2, int i3) throws RemoteException;

        default void onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
        }

        default void onAuthenticationSucceeded(long deviceId, BiometricAuthenticator.Identifier biometric, int userId) throws RemoteException {
            throw new UnsupportedOperationException("Stub!");
        }

        default void onAuthenticationSucceededInternal(boolean requireConfirmation, byte[] token) throws RemoteException {
            throw new UnsupportedOperationException("Stub!");
        }

        default void onAuthenticationFailed(long deviceId) throws RemoteException {
            throw new UnsupportedOperationException("Stub!");
        }

        default void onAuthenticationFailedInternal(int cookie, boolean requireConfirmation) throws RemoteException {
            throw new UnsupportedOperationException("Stub!");
        }

        default void onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
        }

        default void onEnumerated(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
        }
    }

    /* access modifiers changed from: protected */
    public abstract class BiometricServiceListener implements ServiceListener {
        private IBiometricServiceReceiverInternal mWrapperReceiver;

        public BiometricServiceListener(IBiometricServiceReceiverInternal wrapperReceiver) {
            this.mWrapperReceiver = wrapperReceiver;
        }

        public IBiometricServiceReceiverInternal getWrapperReceiver() {
            return this.mWrapperReceiver;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceededInternal(boolean requireConfirmation, byte[] token) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAuthenticationSucceeded(requireConfirmation, token);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailedInternal(int cookie, boolean requireConfirmation) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAuthenticationFailed(cookie, requireConfirmation);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 10) {
                String tag = BiometricServiceBase.this.getTag();
                Slog.w(tag, "Unknown message:" + msg.what);
                return;
            }
            BiometricServiceBase.this.handleUserSwitching(msg.arg1);
        }
    }

    /* access modifiers changed from: private */
    public final class BiometricTaskStackListener extends TaskStackListener {
        private BiometricTaskStackListener() {
        }

        public void onTaskStackChanged() {
            try {
                if (BiometricServiceBase.this.mCurrentClient instanceof AuthenticationClient) {
                    String currentClient = BiometricServiceBase.this.mCurrentClient.getOwnerString();
                    if (!BiometricServiceBase.this.isKeyguard(currentClient)) {
                        List<ActivityManager.RunningTaskInfo> runningTasks = BiometricServiceBase.this.mActivityTaskManager.getTasks(1);
                        if (!runningTasks.isEmpty()) {
                            String topPackage = runningTasks.get(0).topActivity.getPackageName();
                            if (!topPackage.contentEquals(currentClient) && !BiometricServiceBase.this.mCurrentClient.isAlreadyDone()) {
                                String tag = BiometricServiceBase.this.getTag();
                                Slog.e(tag, "Stopping background authentication, top: " + topPackage + " currentClient: " + currentClient);
                                BiometricServiceBase.this.mCurrentClient.stop(false);
                                BiometricServiceBase.this.notifyAuthCanceled(topPackage);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.e(BiometricServiceBase.this.getTag(), "Unable to get running tasks", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyAuthCanceled(String topPackage) {
    }

    /* access modifiers changed from: private */
    public final class ResetClientStateRunnable implements Runnable {
        private ResetClientStateRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            String tag = BiometricServiceBase.this.getTag();
            StringBuilder sb = new StringBuilder();
            sb.append("Client ");
            String str = "null";
            sb.append(BiometricServiceBase.this.mCurrentClient != null ? BiometricServiceBase.this.mCurrentClient.getOwnerString() : str);
            sb.append(" failed to respond to cancel, starting client ");
            if (BiometricServiceBase.this.mPendingClient != null) {
                str = BiometricServiceBase.this.mPendingClient.getOwnerString();
            }
            sb.append(str);
            Slog.w(tag, sb.toString());
            StatsLog.write(148, BiometricServiceBase.this.statsModality(), 4);
            BiometricServiceBase biometricServiceBase = BiometricServiceBase.this;
            biometricServiceBase.mCurrentClient = null;
            if (biometricServiceBase.mPendingClient != null) {
                BiometricServiceBase biometricServiceBase2 = BiometricServiceBase.this;
                biometricServiceBase2.startClient(biometricServiceBase2.mPendingClient, false);
                BiometricServiceBase.this.mPendingClient = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class LockoutResetMonitor implements IBinder.DeathRecipient {
        private static final long WAKELOCK_TIMEOUT_MS = 2000;
        private final IBiometricServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable = new Runnable() {
            /* class com.android.server.biometrics.BiometricServiceBase.LockoutResetMonitor.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                LockoutResetMonitor.this.releaseWakelock();
                BiometricServiceBase.this.removeLockoutResetCallback(LockoutResetMonitor.this);
            }
        };
        private final PowerManager.WakeLock mWakeLock;

        public LockoutResetMonitor(IBiometricServiceLockoutResetCallback callback) {
            this.mCallback = callback;
            this.mWakeLock = BiometricServiceBase.this.mPowerManager.newWakeLock(1, "lockout reset callback");
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.w(BiometricServiceBase.this.getTag(), "caught remote exception in linkToDeath", e);
            }
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
                    this.mCallback.onLockoutReset(BiometricServiceBase.this.getHalDeviceId(), new IRemoteCallback.Stub() {
                        /* class com.android.server.biometrics.BiometricServiceBase.LockoutResetMonitor.AnonymousClass1 */

                        public void sendResult(Bundle data) throws RemoteException {
                            LockoutResetMonitor.this.releaseWakelock();
                        }
                    });
                } catch (DeadObjectException e) {
                    Slog.w(BiometricServiceBase.this.getTag(), "Death object while invoking onLockoutReset: ", e);
                    BiometricServiceBase.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    Slog.w(BiometricServiceBase.this.getTag(), "Failed to invoke onLockoutReset: ", e2);
                    releaseWakelock();
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.e(BiometricServiceBase.this.getTag(), "Lockout reset callback binder died");
            BiometricServiceBase.this.mHandler.post(this.mRemoveCallbackRunnable);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void releaseWakelock() {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class UserTemplate {
        final BiometricAuthenticator.Identifier mIdentifier;
        final int mUserId;

        UserTemplate(BiometricAuthenticator.Identifier identifier, int userId) {
            this.mIdentifier = identifier;
            this.mUserId = userId;
        }
    }

    public BiometricServiceBase(Context context) {
        super(context);
        this.mContext = context;
        this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039856)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        ActivityTaskManager activityTaskManager = (ActivityTaskManager) context.getSystemService("activity_task");
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        ServiceThread biometricThread = new ServiceThread("biometricServcie", -8, false);
        biometricThread.start();
        this.mHandler = new H(biometricThread.getLooper());
        this.mUserManager = UserManager.get(this.mContext);
        this.mMetricsLogger = new MetricsLogger();
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        listenForUserSwitches();
    }

    public void serviceDied(long cookie) {
        Slog.e(getTag(), "HAL died");
        this.mMetricsLogger.count(getConstants().tagHalDied(), 1);
        this.mHALDeathCount++;
        this.mCurrentUserId = -10000;
        handleError(getHalDeviceId(), 1, 0);
        StatsLog.write(148, statsModality(), 1);
    }

    /* access modifiers changed from: protected */
    public ClientMonitor getCurrentClient() {
        return this.mCurrentClient;
    }

    /* access modifiers changed from: protected */
    public ClientMonitor getPendingClient() {
        return this.mPendingClient;
    }

    /* access modifiers changed from: protected */
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onAcquired(acquiredInfo, vendorCode)) {
            removeClient(client);
        }
        if (this.mPerformanceStats != null && getLockoutMode() == 0 && (client instanceof AuthenticationClient)) {
            this.mPerformanceStats.acquire++;
        }
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticated(BiometricAuthenticator.Identifier identifier, ArrayList<Byte> token) {
        ClientMonitor client = this.mCurrentClient;
        boolean authenticated = identifier.getBiometricId() != 0;
        if (client != null && client.onAuthenticated(identifier, authenticated, token)) {
            notifyFingerRemovedAtAuth(client);
            removeClient(client);
        }
        PerformanceStats performanceStats = this.mPerformanceStats;
        if (performanceStats == null) {
            Slog.w(getTag(), "mPerformanceStats is null");
        } else if (authenticated) {
            performanceStats.accept++;
        } else {
            performanceStats.reject++;
        }
    }

    public void notifyFingerRemovedAtAuth(ClientMonitor client) {
    }

    /* access modifiers changed from: protected */
    public void notifyEnrollCanceled() {
    }

    /* access modifiers changed from: protected */
    public void addHighlightOnAcquired(int acquiredInfo, int vendorCode) {
    }

    /* access modifiers changed from: protected */
    public boolean handleEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        if (client == null || !client.onEnrollResult(identifier, remaining)) {
            return false;
        }
        removeClient(client);
        notifyEnrollCanceled();
        if (identifier instanceof Fingerprint) {
            updateActiveGroup(((Fingerprint) identifier).getGroupId(), null);
            return true;
        }
        updateActiveGroup(this.mCurrentUserId, null);
        return true;
    }

    /* access modifiers changed from: protected */
    public int getRealUserIdForHal(int groupId) {
        UserInfo info = this.mUserManager.getUserInfo(groupId);
        if (info == null || !info.isHwHiddenSpace()) {
            return groupId;
        }
        return HIDDEN_SPACE_ID;
    }

    /* access modifiers changed from: protected */
    public int getRealUserIdForApp(int groupId) {
        if (groupId != HIDDEN_SPACE_ID) {
            return groupId;
        }
        for (UserInfo user : this.mUserManager.getUsers(true)) {
            if (user != null && user.isHwHiddenSpace()) {
                return user.id;
            }
        }
        Slog.w(getTag(), "getRealUserIdForApp error return 0");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void handleError(long deviceId, int error, int vendorCode) {
        ClientMonitor client = this.mCurrentClient;
        String tag = getTag();
        StringBuilder sb = new StringBuilder();
        sb.append("handleError(client=");
        sb.append(client != null ? client.getOwnerString() : "null");
        sb.append(", error = ");
        sb.append(error);
        sb.append(")");
        Slog.v(tag, sb.toString());
        if ((client instanceof InternalRemovalClient) || (client instanceof InternalEnumerateClient)) {
            clearEnumerateState();
        }
        if (client != null && client.onError(deviceId, error, vendorCode)) {
            removeClient(client);
        }
        if (error == 5) {
            this.mHandler.removeCallbacks(this.mResetClientState);
            if (this.mPendingClient != null) {
                String tag2 = getTag();
                Slog.v(tag2, "start pending client " + this.mPendingClient.getOwnerString());
                startClient(this.mPendingClient, false);
                this.mPendingClient = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getTag(), "Removed: fid=" + identifier.getBiometricId() + ", dev=" + identifier.getDeviceId() + ", rem=" + remaining);
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onRemoved(identifier, remaining)) {
            int userId = this.mCurrentUserId;
            if (identifier instanceof Fingerprint) {
                userId = ((Fingerprint) identifier).getGroupId();
            }
            if (getBiometricUtils().isDualFp() && (client instanceof RemovalClient)) {
                RemovalClient removeClient = (RemovalClient) client;
                FingerprintUtils fingerUtil = FingerprintUtils.getInstance();
                boolean hasUDFingerprints = true;
                boolean hasFingerprints = fingerUtil.getFingerprintsForUser(this.mContext, userId, -1).size() > 0;
                if (fingerUtil.getFingerprintsForUser(this.mContext, userId, 1).size() <= 0) {
                    hasUDFingerprints = false;
                }
                if (!hasUDFingerprints) {
                    sendCommandToHal(0);
                    Slog.d(getTag(), "UDFingerprint all removed so TP CLOSE");
                }
                if (removeClient.getBiometricId() == 0 && hasFingerprints) {
                    Slog.d(getTag(), "dualFingerprint-> handleRemoved, but do not destory client.");
                    return;
                }
            }
            removeClient(client);
            if (!hasEnrolledBiometrics(userId)) {
                updateActiveGroup(userId, null);
            }
        }
        if ((client instanceof InternalRemovalClient) && !this.mUnknownHALTemplates.isEmpty()) {
            startCleanupUnknownHALTemplates();
        } else if (client instanceof InternalRemovalClient) {
            clearEnumerateState();
        }
    }

    /* access modifiers changed from: protected */
    public void handleEnumerate(BiometricAuthenticator.Identifier identifier, int remaining) {
        ClientMonitor client = getCurrentClient();
        if (client != null) {
            client.onEnumerationResult(identifier, remaining);
        }
        if (remaining != 0) {
            return;
        }
        if (client instanceof InternalEnumerateClient) {
            List<BiometricAuthenticator.Identifier> unknownHALTemplates = ((InternalEnumerateClient) client).getUnknownHALTemplates();
            if (!unknownHALTemplates.isEmpty()) {
                String tag = getTag();
                Slog.w(tag, "Adding " + unknownHALTemplates.size() + " templates for deletion");
            }
            for (int i = 0; i < unknownHALTemplates.size(); i++) {
                this.mUnknownHALTemplates.add(new UserTemplate(unknownHALTemplates.get(i), client.getTargetUserId()));
            }
            removeClient(client);
            startCleanupUnknownHALTemplates();
            return;
        }
        removeClient(client);
    }

    /* access modifiers changed from: protected */
    public void udFingerprintAllRemoved(ClientMonitor client, int groupId) {
    }

    /* access modifiers changed from: protected */
    public void enrollInternal(EnrollClientImpl client, int userId, int flags, String opPackageName) {
    }

    /* access modifiers changed from: protected */
    public void enrollInternal(EnrollClientImpl client, int userId) {
        if (!hasReachedEnrollmentLimit(userId) && isCurrentUserOrProfile(userId)) {
            this.mHandler.post(new Runnable(client) {
                /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$fmXvpYbcyw65Q8mBLfnQvCCMoK4 */
                private final /* synthetic */ BiometricServiceBase.EnrollClientImpl f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BiometricServiceBase.this.lambda$enrollInternal$0$BiometricServiceBase(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$enrollInternal$0$BiometricServiceBase(EnrollClientImpl client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public void enrollInternal(EnrollClientImpl client) {
        this.mHandler.post(new Runnable(client) {
            /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$Zy4OXo3HMpNNxU1x5VMDe_5Q3vI */
            private final /* synthetic */ BiometricServiceBase.EnrollClientImpl f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BiometricServiceBase.this.lambda$enrollInternal$1$BiometricServiceBase(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$enrollInternal$1$BiometricServiceBase(EnrollClientImpl client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public void cancelEnrollmentInternal(IBinder token) {
        this.mHandler.post(new Runnable(token) {
            /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$yj0NG4umGnnyUerNM_EKxeka05A */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BiometricServiceBase.this.lambda$cancelEnrollmentInternal$2$BiometricServiceBase(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$cancelEnrollmentInternal$2$BiometricServiceBase(IBinder token) {
        ClientMonitor client = this.mCurrentClient;
        if ((client instanceof EnrollClient) && client.getToken() == token) {
            Slog.v(getTag(), "Cancelling enrollment");
            client.stop(client.getToken() == token);
        }
    }

    /* access modifiers changed from: protected */
    public void authenticateInternal(AuthenticationClientImpl client, long opId, String opPackageName) {
        authenticateInternal(client, opId, opPackageName, 0);
    }

    /* access modifiers changed from: protected */
    public void authenticateInternal(AuthenticationClientImpl client, long opId, String opPackageName, int flags) {
        authenticateInternal(client, opId, opPackageName, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), flags);
    }

    /* access modifiers changed from: protected */
    public void authenticateInternal(AuthenticationClientImpl client, long opId, String opPackageName, int callingUid, int callingPid, int callingUserId) {
        authenticateInternal(client, opId, opPackageName, callingUid, callingPid, callingUserId, 0);
    }

    /* access modifiers changed from: protected */
    public void authenticateInternal(AuthenticationClientImpl client, long opId, String opPackageName, int callingUid, int callingPid, int callingUserId, int flags) {
        if (!canUseBiometric(opPackageName, true, callingUid, callingPid, callingUserId)) {
            String tag = getTag();
            Slog.v(tag, "authenticate(): reject " + opPackageName);
            return;
        }
        this.mHandler.post(new Runnable(opId, client, opPackageName, flags) {
            /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$SUkXR6yhFbH_aXlqNl3yfP7SgIQ */
            private final /* synthetic */ long f$1;
            private final /* synthetic */ BiometricServiceBase.AuthenticationClientImpl f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BiometricServiceBase.this.lambda$authenticateInternal$3$BiometricServiceBase(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$authenticateInternal$3$BiometricServiceBase(long opId, AuthenticationClientImpl client, String opPackageName, int flags) {
        boolean z = true;
        this.mMetricsLogger.histogram(getConstants().tagAuthToken(), opId != 0 ? 1 : 0);
        HashMap<Integer, PerformanceStats> pmap = opId == 0 ? this.mPerformanceMap : this.mCryptoPerformanceMap;
        PerformanceStats stats = pmap.get(Integer.valueOf(this.mCurrentUserId));
        if (stats == null) {
            stats = new PerformanceStats();
            pmap.put(Integer.valueOf(this.mCurrentUserId), stats);
        }
        this.mPerformanceStats = stats;
        if (opId == 0) {
            z = false;
        }
        this.mIsCrypto = z;
        startAuthentication(client, opPackageName, flags);
    }

    /* access modifiers changed from: protected */
    public void cancelAuthenticationInternal(IBinder token, String opPackageName) {
        cancelAuthenticationInternal(token, opPackageName, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), true);
    }

    /* access modifiers changed from: protected */
    public void cancelAuthenticationInternal(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
        if (!fromClient || canUseBiometric(opPackageName, false, callingUid, callingPid, callingUserId)) {
            this.mHandler.post(new Runnable(token, fromClient) {
                /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$B1PDNz5plOtQUbeZgXMkI_dh_yQ */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BiometricServiceBase.this.lambda$cancelAuthenticationInternal$4$BiometricServiceBase(this.f$1, this.f$2);
                }
            });
            return;
        }
        String tag = getTag();
        Slog.v(tag, "cancelAuthentication(): reject " + opPackageName);
    }

    public /* synthetic */ void lambda$cancelAuthenticationInternal$4$BiometricServiceBase(IBinder token, boolean fromClient) {
        ClientMonitor client = this.mCurrentClient;
        if (client instanceof AuthenticationClient) {
            if (client.getToken() == token || !fromClient) {
                String tag = getTag();
                Slog.v(tag, "Stopping client " + client.getOwnerString() + ", fromClient: " + fromClient);
                client.stop(client.getToken() == token);
                return;
            }
            String tag2 = getTag();
            Slog.v(tag2, "Can't stop client " + client.getOwnerString() + " since tokens don't match. fromClient: " + fromClient);
        } else if (client != null) {
            String tag3 = getTag();
            Slog.v(tag3, "Can't cancel non-authenticating client " + client.getOwnerString());
        }
    }

    /* access modifiers changed from: protected */
    public void setActiveUserInternal(int userId) {
        updateActiveGroup(userId, null);
    }

    /* access modifiers changed from: protected */
    public void removeInternal(RemovalClient client) {
        this.mHandler.post(new Runnable(client) {
            /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$dzSBKmMm2y9x0FJlzdmvQRJB0s4 */
            private final /* synthetic */ RemovalClient f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BiometricServiceBase.this.lambda$removeInternal$5$BiometricServiceBase(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$removeInternal$5$BiometricServiceBase(RemovalClient client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public void enumerateInternal(EnumerateClient client) {
        this.mHandler.post(new Runnable(client) {
            /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$d8jmYuo4MyZZpxoeouUPEq4DMII */
            private final /* synthetic */ EnumerateClient f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BiometricServiceBase.this.lambda$enumerateInternal$6$BiometricServiceBase(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$enumerateInternal$6$BiometricServiceBase(EnumerateClient client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public int shouldAuthBothSpaceBiometric(AuthenticationClientImpl client, String opPackageName, int flags) {
        return client.getGroupId();
    }

    private void startAuthentication(AuthenticationClientImpl client, String opPackageName, int flags) {
        int errorCode;
        int groupId = client.getGroupId();
        String tag = getTag();
        Slog.v(tag, "startAuthentication(" + opPackageName + ")");
        int newGroupId = shouldAuthBothSpaceBiometric(client, opPackageName, flags);
        if (groupId != newGroupId) {
            client.setGroupId(newGroupId);
        }
        int lockoutMode = getLockoutMode();
        if (lockoutMode != 0) {
            if (!(client instanceof FingerprintService.FingerprintAuthClient) || (!isKeyguard(opPackageName) && !COAUTH_SERVICE_PKG.equals(opPackageName))) {
                String tag2 = getTag();
                Slog.v(tag2, "In lockout mode(" + lockoutMode + ") ; disallowing authentication");
                if (lockoutMode == 1) {
                    errorCode = 7;
                } else {
                    errorCode = 9;
                }
                if (!client.onError(getHalDeviceId(), errorCode, 0)) {
                    Slog.w(getTag(), "Cannot send permanent lockout message to client");
                    return;
                }
                return;
            }
            Slog.v(getTag(), "keyguard and  coauthservice allowing authentication in fingerprint");
        }
        dualFingerprintStartAuth(flags, opPackageName);
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public void dualFingerprintStartAuth(int flags, String opPackageName) {
    }

    /* access modifiers changed from: protected */
    public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) {
        if (callback == null) {
            Log.e(getTag(), " BiometricServiceLockoutResetCallback is null, cannot addLockoutResetMonitor, return");
        } else {
            this.mHandler.post(new Runnable(callback) {
                /* class com.android.server.biometrics.$$Lambda$BiometricServiceBase$F4H2HbJPkB5kHnCG99RJzq63ETk */
                private final /* synthetic */ IBiometricServiceLockoutResetCallback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BiometricServiceBase.this.lambda$addLockoutResetCallback$7$BiometricServiceBase(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$addLockoutResetCallback$7$BiometricServiceBase(IBiometricServiceLockoutResetCallback callback) {
        LockoutResetMonitor monitor = new LockoutResetMonitor(callback);
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    /* access modifiers changed from: protected */
    public boolean canUseBiometric(String opPackageName, boolean requireForeground, int uid, int pid, int userId) {
        return canUseBiometric(opPackageName, requireForeground, uid, pid, userId, false);
    }

    /* access modifiers changed from: protected */
    public boolean canUseBiometric(String opPackageName, boolean requireForeground, int uid, int pid, int userId, boolean isDetected) {
        checkUseBiometricPermission();
        if (Binder.getCallingUid() == 1000 || isKeyguard(opPackageName)) {
            return true;
        }
        if (!isCurrentUserOrProfile(userId)) {
            String tag = getTag();
            Slog.w(tag, "Rejecting " + opPackageName + "; not a current user or profile");
            return false;
        } else if (!checkAppOps(uid, opPackageName)) {
            String tag2 = getTag();
            Slog.w(tag2, "Rejecting " + opPackageName + "; permission denied");
            return false;
        } else if (!requireForeground || isForegroundActivity(uid, pid) || isCurrentClient(opPackageName)) {
            return true;
        } else {
            String tag3 = getTag();
            Slog.w(tag3, "Rejecting " + opPackageName + "; not in foreground");
            return false;
        }
    }

    private boolean isCurrentClient(String opPackageName) {
        ClientMonitor clientMonitor = this.mCurrentClient;
        return clientMonitor != null && clientMonitor.getOwnerString().equals(opPackageName);
    }

    /* access modifiers changed from: protected */
    public boolean isKeyguardCurrentClient() {
        return isCurrentClient(this.mKeyguardPackage);
    }

    /* access modifiers changed from: protected */
    public boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    private boolean isForegroundActivity(int uid, int pid) {
        try {
            List<ActivityManager.RunningAppProcessInfo> procs = ActivityManager.getService().getRunningAppProcesses();
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                ActivityManager.RunningAppProcessInfo proc = procs.get(i);
                if (proc.pid == pid && proc.uid == uid && proc.importance <= 125) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.w(getTag(), "am.getRunningAppProcesses() failed");
            return false;
        }
    }

    private void destroyNewClient(ClientMonitor newClient, boolean isInitiatedByClient) {
        if (newClient != null) {
            String tag = getTag();
            Slog.w(tag, "Internal cleanup in progress but trying to start client (" + newClient.getOwnerString() + "), initiatedByClient = " + isInitiatedByClient);
            newClient.onError(getHalDeviceId(), 5, 0);
            newClient.destroy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startClient(ClientMonitor newClient, boolean initiatedByClient) {
        ClientMonitor currentClient = this.mCurrentClient;
        this.mHandler.removeCallbacks(this.mResetClientState);
        if (currentClient != null) {
            String tag = getTag();
            Slog.v(tag, "request stop current client " + currentClient.getOwnerString());
            if ((currentClient instanceof InternalEnumerateClient) || (currentClient instanceof InternalRemovalClient)) {
                destroyNewClient(newClient, initiatedByClient);
                return;
            }
            currentClient.stop(initiatedByClient);
            ClientMonitor clientMonitor = this.mPendingClient;
            if (clientMonitor != null) {
                clientMonitor.destroy();
            }
            this.mHandler.removeCallbacks(this.mResetClientState);
            this.mPendingClient = newClient;
            this.mHandler.postDelayed(this.mResetClientState, 3000);
        } else if (newClient != null) {
            if (this.mPendingClient != null) {
                this.mPendingClient = null;
                Slog.w(getTag(), "pendingClient destroy");
            }
            if (newClient instanceof AuthenticationClient) {
                AuthenticationClient client = (AuthenticationClient) newClient;
                if (client.isBiometricPrompt()) {
                    String tag2 = getTag();
                    Slog.v(tag2, "Returning cookie: " + client.getCookie());
                    this.mCurrentClient = newClient;
                    if (this.mBiometricService == null) {
                        this.mBiometricService = IBiometricService.Stub.asInterface(ServiceManager.getService("biometric"));
                    }
                    try {
                        this.mBiometricService.onReadyForAuthentication(client.getCookie(), client.getRequireConfirmation(), client.getTargetUserId());
                        return;
                    } catch (RemoteException e) {
                        Slog.e(getTag(), "Remote exception", e);
                        return;
                    }
                }
            }
            this.mCurrentClient = newClient;
            startCurrentClient(this.mCurrentClient.getCookie());
        }
    }

    /* access modifiers changed from: protected */
    public void startCurrentClient(int cookie) {
        if (this.mCurrentClient == null) {
            Slog.e(getTag(), "Trying to start null client!");
            return;
        }
        String tag = getTag();
        Slog.v(tag, "starting client (" + this.mCurrentClient.getOwnerString() + ") cookie: " + cookie + SliceClientPermissions.SliceAuthority.DELIMITER + this.mCurrentClient.getCookie());
        if (cookie != this.mCurrentClient.getCookie()) {
            Slog.e(getTag(), "Mismatched cookie");
            return;
        }
        notifyClientActiveCallbacks(true);
        this.mCurrentClient.start();
    }

    /* access modifiers changed from: protected */
    public void removeClient(ClientMonitor client) {
        if (client != null) {
            client.destroy();
            ClientMonitor clientMonitor = this.mCurrentClient;
            if (!(client == clientMonitor || clientMonitor == null)) {
                String tag = getTag();
                Slog.w(tag, "Unexpected client: " + client.getOwnerString() + "expected: " + this.mCurrentClient.getOwnerString());
            }
        }
        if (this.mCurrentClient != null) {
            String tag2 = getTag();
            Slog.v(tag2, "Done with client: " + client.getOwnerString());
            this.mCurrentClient = null;
        }
        if (this.mPendingClient == null) {
            notifyClientActiveCallbacks(false);
        }
    }

    /* access modifiers changed from: protected */
    public void loadAuthenticatorIds() {
        long t = System.currentTimeMillis();
        this.mAuthenticatorIds.clear();
        for (UserInfo user : UserManager.get(getContext()).getUsers(true)) {
            int userId = getUserOrWorkProfileId(null, user.id);
            if (!this.mAuthenticatorIds.containsKey(Integer.valueOf(userId))) {
                updateActiveGroup(userId, null);
            }
        }
        long t2 = System.currentTimeMillis() - t;
        if (t2 > 1000) {
            String tag = getTag();
            Slog.w(tag, "loadAuthenticatorIds() taking too long: " + t2 + "ms");
        }
    }

    /* access modifiers changed from: protected */
    public int getUserOrWorkProfileId(String clientPackage, int userId) {
        if (isKeyguard(clientPackage) || !isWorkProfile(userId)) {
            return getEffectiveUserId(userId);
        }
        return userId;
    }

    /* access modifiers changed from: protected */
    public boolean isRestricted() {
        return !hasPermission(getManageBiometricPermission());
    }

    /* access modifiers changed from: protected */
    public boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0;
    }

    /* access modifiers changed from: protected */
    public void checkPermission(String permission) {
        Context context = getContext();
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    /* access modifiers changed from: protected */
    /* JADX INFO: finally extract failed */
    public boolean isCurrentUserOrProfile(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um == null) {
            Slog.e(getTag(), "Unable to acquire UserManager");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            for (int profileId : um.getEnabledProfileIds(ActivityManager.getCurrentUser())) {
                if (profileId == userId) {
                    Binder.restoreCallingIdentity(token);
                    return true;
                }
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public long getAuthenticatorId(String opPackageName) {
        return this.mAuthenticatorIds.getOrDefault(Integer.valueOf(getUserOrWorkProfileId(opPackageName, UserHandle.getCallingUserId())), 0L).longValue();
    }

    /* access modifiers changed from: protected */
    public void doTemplateCleanupForUser(int userId) {
        enumerateUser(userId);
    }

    private void clearEnumerateState() {
        Slog.v(getTag(), "clearEnumerateState()");
        this.mUnknownHALTemplates.clear();
    }

    private void startCleanupUnknownHALTemplates() {
        if (!this.mUnknownHALTemplates.isEmpty()) {
            UserTemplate template = this.mUnknownHALTemplates.get(0);
            this.mUnknownHALTemplates.remove(template);
            removeInternal(new InternalRemovalClient(getContext(), getDaemonWrapper(), this.mHalDeviceId, this.mToken, null, template.mIdentifier.getBiometricId(), 0, template.mUserId, !hasPermission(getManageBiometricPermission()), getContext().getPackageName()));
            StatsLog.write(148, statsModality(), 3);
            return;
        }
        clearEnumerateState();
        if (this.mPendingClient != null) {
            Slog.d(getTag(), "Enumerate finished, starting pending client");
            startClient(this.mPendingClient, false);
            this.mPendingClient = null;
        }
    }

    private void enumerateUser(int userId) {
        String tag = getTag();
        Slog.v(tag, "Enumerating user(" + userId + ")");
        List<? extends BiometricAuthenticator.Identifier> enrolledList = getEnrolledTemplates(userId);
        enumerateInternal(new InternalEnumerateClient(getContext(), getDaemonWrapper(), this.mHalDeviceId, this.mToken, null, userId, userId, hasPermission(getManageBiometricPermission()) ^ true, getContext().getOpPackageName(), enrolledList, getBiometricUtils()));
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitching(int userId) {
        if ((getCurrentClient() instanceof InternalRemovalClient) || (getCurrentClient() instanceof InternalEnumerateClient)) {
            Slog.w(getTag(), "User switched while performing cleanup");
        }
        updateActiveGroup(userId, null);
        doTemplateCleanupForUser(userId);
    }

    /* access modifiers changed from: protected */
    public void notifyLockoutResetMonitors() {
        for (int i = 0; i < this.mLockoutMonitors.size(); i++) {
            this.mLockoutMonitors.get(i).sendLockoutReset();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
    }

    private boolean isWorkProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = this.mUserManager.getUserInfo(userId);
            return userInfo != null && userInfo.isManagedProfile();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: protected */
    public int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            int userId2 = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId2;
        }
        Slog.e(getTag(), "Unable to acquire UserManager");
        return userId;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class com.android.server.biometrics.BiometricServiceBase.AnonymousClass1 */

                public void onUserSwitching(int newUserId) throws RemoteException {
                    BiometricServiceBase.this.mHandler.obtainMessage(10, newUserId, 0).sendToTarget();
                }
            }, getTag());
        } catch (RemoteException e) {
            Slog.w(getTag(), "Failed to listen for user switching event", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeLockoutResetCallback(LockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }
}
