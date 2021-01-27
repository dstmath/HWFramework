package com.android.server.biometrics.fingerprint;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.am.AssistDataRequester;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.biometrics.ClientMonitor;
import com.android.server.biometrics.Constants;
import com.android.server.biometrics.EnumerateClient;
import com.android.server.biometrics.RemovalClient;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerprintService extends BiometricServiceBase {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.biometrics.fingerprint.ACTION_LOCKOUT_RESET";
    public static final boolean DEBUG = true;
    protected static final int ENROLL_UD = 4096;
    protected static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING = 1;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING_SETTINGS = 2;
    protected static final int FINGER_DOWN_TYPE_ENROLLING = 0;
    private static final String FP_DATA_DIR = "fpdata";
    protected static final int HW_FP_AUTH_BOTH_SPACE = 33554432;
    protected static final int HW_FP_AUTH_UD = 134217728;
    protected static final int HW_FP_AUTH_UG = 67108864;
    private static final String KEY_LOCKOUT_RESET_USER = "lockout_reset_user";
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT = 20;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT_FP = 50;
    protected static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    protected static final int SPECIAL_USER_ID = -101;
    protected static final String TAG = "FingerprintService";
    protected long auTime;
    private AlarmManager mAlarmManager;
    private final CopyOnWriteArrayList<IFingerprintClientActiveCallback> mClientActiveCallbacks = new CopyOnWriteArrayList<>();
    private final Context mContext;
    @GuardedBy({"this"})
    protected IBiometricsFingerprint mDaemon;
    protected IBiometricsFingerprintClientCallback mDaemonCallback = new IBiometricsFingerprintClientCallback.Stub() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass1 */

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintService.this.mHandler.post(new Runnable(groupId, fingerId, deviceId, remaining) {
                /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$7RPI0PwwgOAZtsXq2j72pQWwMc */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ long f$3;
                private final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r6;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FingerprintService.AnonymousClass1.this.lambda$onEnrollResult$0$FingerprintService$1(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onEnrollResult$0$FingerprintService$1(int groupId, int fingerId, long deviceId, int remaining) {
            int fingerGroupId = FingerprintService.this.getRealUserIdForApp(groupId);
            FingerprintService.this.handleEnrollResult(new Fingerprint(FingerprintService.this.getBiometricUtils().getUniqueName(FingerprintService.this.getContext(), fingerGroupId), fingerGroupId, fingerId, deviceId), remaining);
        }

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) {
            Slog.w(FingerprintService.TAG, "onAcquired 1");
            FingerprintService.this.mHandler.post(new Runnable(deviceId, acquiredInfo, vendorCode) {
                /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$N1Y2Zwqqx5yDKQsDTj2KQ5q7g4 */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FingerprintService.AnonymousClass1.this.lambda$onAcquired$1$FingerprintService$1(this.f$1, this.f$2, this.f$3);
                }
            });
            FingerprintService.this.addHighlightOnAcquired(acquiredInfo, vendorCode);
        }

        public /* synthetic */ void lambda$onAcquired$1$FingerprintService$1(long deviceId, int acquiredInfo, int vendorCode) {
            FingerprintService.this.handleAcquired(deviceId, acquiredInfo, vendorCode);
        }

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
            long delayTime = FingerprintService.this.getPowerDelayFpTime(fingerId != 0);
            Slog.i(FingerprintService.TAG, "onAuthenticated run delay time:" + delayTime);
            if (delayTime >= 0) {
                FingerprintService.this.mHandler.post(new Runnable(groupId, fingerId, deviceId, token) {
                    /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$7nMWCt41OE3k8ihjPNPqB0O8POU */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ long f$3;
                    private final /* synthetic */ ArrayList f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r6;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        FingerprintService.AnonymousClass1.this.lambda$onAuthenticated$2$FingerprintService$1(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            } else {
                FingerprintService.this.saveWaitRunonAuthenticated(deviceId, fingerId, FingerprintService.this.getRealUserIdForApp(groupId), token);
            }
        }

        public /* synthetic */ void lambda$onAuthenticated$2$FingerprintService$1(int groupId, int fingerId, long deviceId, ArrayList token) {
            FingerprintService.this.handleAuthenticated(new Fingerprint("", FingerprintService.this.getRealUserIdForApp(groupId), fingerId, deviceId), token);
        }

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onError(long deviceId, int error, int vendorCode) {
            FingerprintService.this.mHandler.post(new Runnable(deviceId, error, vendorCode) {
                /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$cO88ecWuvWIBecLAEccxr5yeJK4 */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FingerprintService.AnonymousClass1.this.lambda$onError$3$FingerprintService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onError$3$FingerprintService$1(long deviceId, int error, int vendorCode) {
            FingerprintService.this.handleError(deviceId, error, vendorCode);
            if (error == 1) {
                Slog.w(FingerprintService.TAG, "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
                synchronized (this) {
                    if (FingerprintService.this.mDaemon != null) {
                        FingerprintService.this.mDaemon.asBinder().unlinkToDeath(FingerprintService.this);
                    }
                    FingerprintService.this.mDaemon = null;
                    FingerprintService.this.mHalDeviceId = 0;
                    FingerprintService.this.mCurrentUserId = -10000;
                }
            }
        }

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintService.this.mHandler.post(new Runnable(groupId, fingerId, deviceId, remaining) {
                /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$BJntfNoFTejPmUJ_45WFIwis8Nw */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ long f$3;
                private final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r6;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FingerprintService.AnonymousClass1.this.lambda$onRemoved$4$FingerprintService$1(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onRemoved$4$FingerprintService$1(int groupId, int fingerId, long deviceId, int remaining) {
            FingerprintService.this.getCurrentClient();
            FingerprintService.super.handleRemoved(new Fingerprint("", FingerprintService.this.getRealUserIdForApp(groupId), fingerId, deviceId), remaining);
        }

        @Override // android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onEnumerate(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintService.this.mHandler.post(new Runnable(groupId, fingerId, deviceId, remaining) {
                /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$1$3I9ge5BoesXZUovbayCOCR754fc */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ long f$3;
                private final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r6;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FingerprintService.AnonymousClass1.this.lambda$onEnumerate$5$FingerprintService$1(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onEnumerate$5$FingerprintService$1(int groupId, int fingerId, long deviceId, int remaining) {
            FingerprintService.super.handleEnumerate(new Fingerprint("", groupId, fingerId, deviceId), remaining);
        }
    };
    protected final BiometricServiceBase.DaemonWrapper mDaemonWrapper = new BiometricServiceBase.DaemonWrapper() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass2 */

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int authenticate(long operationId, int groupId) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon == null) {
                Slog.w(FingerprintService.TAG, "authenticate(): no fingerprint HAL!");
                return 3;
            }
            int newGroupId = groupId;
            if (newGroupId != FingerprintService.SPECIAL_USER_ID) {
                newGroupId = FingerprintService.this.getRealUserIdForHal(groupId);
            }
            return daemon.authenticate(operationId, newGroupId);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int cancel() throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.cancel();
            }
            Slog.w(FingerprintService.TAG, "cancel(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int remove(int groupId, int biometricId) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.remove(FingerprintService.this.getRealUserIdForHal(groupId), biometricId);
            }
            Slog.w(FingerprintService.TAG, "remove(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enumerate() throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.enumerate();
            }
            Slog.w(FingerprintService.TAG, "enumerate(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enroll(byte[] cryptoToken, int groupId, int timeout, ArrayList<Integer> arrayList) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.enroll(cryptoToken, FingerprintService.this.getRealUserIdForHal(groupId), timeout);
            }
            Slog.w(FingerprintService.TAG, "enroll(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public void resetLockout(byte[] token) throws RemoteException {
            Slog.e(FingerprintService.TAG, "Not supported");
        }
    };
    protected int mEnrolled = 0;
    protected final SparseIntArray mFailedAttempts;
    private final FingerprintConstants mFingerprintConstants = new FingerprintConstants();
    private final Lock mLockUpdateActive = new ReentrantLock();
    private final LockoutReceiver mLockoutReceiver = new LockoutReceiver();
    protected SparseLongArray mLockoutTime;
    protected final ResetFailedAttemptsForUserRunnable mResetFailedAttemptsForCurrentUserRunnable = new ResetFailedAttemptsForUserRunnable();
    protected final SparseBooleanArray mTimedLockoutCleared;
    protected long mUDHalDeviceId;

    private final class ResetFailedAttemptsForUserRunnable implements Runnable {
        private ResetFailedAttemptsForUserRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            FingerprintService.this.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
        }
    }

    private final class LockoutReceiver extends BroadcastReceiver {
        private LockoutReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String tag = FingerprintService.this.getTag();
            Slog.v(tag, "Resetting lockout: " + intent.getAction());
            if (FingerprintService.this.getLockoutResetIntent().equals(intent.getAction())) {
                FingerprintService.this.resetFailedAttemptsForUser(true, intent.getIntExtra(FingerprintService.KEY_LOCKOUT_RESET_USER, 0));
            }
        }
    }

    /* access modifiers changed from: protected */
    public BiometricServiceBase.AuthenticationClientImpl creatAuthenticationClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation, int flag) {
        return new FingerprintAuthClient(context, daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
    }

    public class FingerprintAuthClient extends BiometricServiceBase.AuthenticationClientImpl {
        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl, com.android.server.biometrics.ClientMonitor
        public /* bridge */ /* synthetic */ void notifyUserActivity() {
            super.notifyUserActivity();
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl, com.android.server.biometrics.AuthenticationClient
        public /* bridge */ /* synthetic */ void onStart() {
            super.onStart();
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl, com.android.server.biometrics.AuthenticationClient
        public /* bridge */ /* synthetic */ void onStop() {
            super.onStop();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl
        public boolean isFingerprint() {
            return true;
        }

        public FingerprintAuthClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
            super(context, daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return FingerprintService.this.statsModality();
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public void resetFailedAttempts() {
            FingerprintService.this.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean shouldFrameworkHandleLockout() {
            return true;
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean wasUserDetected() {
            return false;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl, com.android.server.biometrics.AuthenticationClient
        public int handleFailedAttempt() {
            int currentUser = ActivityManager.getCurrentUser();
            FingerprintService.this.mFailedAttempts.put(currentUser, FingerprintService.this.mFailedAttempts.get(currentUser, 0) + 1);
            FingerprintService.this.mTimedLockoutCleared.put(ActivityManager.getCurrentUser(), false);
            if (FingerprintService.this.getLockoutMode() != 0) {
                FingerprintService.this.mLockoutTime.put(currentUser, SystemClock.elapsedRealtime());
                FingerprintService.this.scheduleLockoutResetForUser(currentUser);
            }
            return super.handleFailedAttempt();
        }
    }

    public class FingerprintServiceWrapper extends IFingerprintService.Stub {
        public FingerprintServiceWrapper() {
        }

        public long preEnroll(IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPreEnroll(token);
        }

        public int postEnroll(IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPostEnroll(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.enrollInternal(new BiometricServiceBase.EnrollClientImpl(FingerprintService.this.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), userId, userId, cryptoToken, FingerprintService.this.isRestricted(), opPackageName, new int[0]) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass1 */

                @Override // com.android.server.biometrics.EnrollClient
                public boolean shouldVibrate() {
                    return true;
                }

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }
            }, userId, flags, opPackageName);
        }

        public void cancelEnrollment(IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.cancelEnrollmentInternal(token);
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            FingerprintService.this.updateActiveGroup(groupId, opPackageName);
            boolean restricted = FingerprintService.this.isRestricted();
            FingerprintService fingerprintService = FingerprintService.this;
            FingerprintService.this.authenticateInternal(fingerprintService.creatAuthenticationClient(fingerprintService.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FingerprintService.this.mCurrentUserId, groupId, opId, restricted, opPackageName, 0, false, flags), opId, opPackageName, flags);
        }

        public void prepareForAuthentication(IBinder token, long opId, int groupId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FingerprintService.this.updateActiveGroup(groupId, opPackageName);
            FingerprintService fingerprintService = FingerprintService.this;
            FingerprintService.this.authenticateInternal(fingerprintService.creatAuthenticationClient(fingerprintService.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new BiometricPromptServiceListenerImpl(wrapperReceiver), FingerprintService.this.mCurrentUserId, groupId, opId, true, opPackageName, cookie, false, 0), opId, opPackageName, callingUid, callingPid, callingUserId);
        }

        public void startPreparedClient(int cookie) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FingerprintService.this.startCurrentClient(cookie);
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            FingerprintService.this.cancelAuthenticationInternal(token, opPackageName);
        }

        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FingerprintService.this.cancelAuthenticationInternal(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
        }

        public void setActiveUser(int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.setActiveUserInternal(userId);
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (token == null) {
                Slog.w(FingerprintService.TAG, "remove(): token is null");
                return;
            }
            FingerprintService.this.removeInternal(new RemovalClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), fingerId, groupId, userId, FingerprintService.this.isRestricted(), token.toString(), FingerprintService.this.getBiometricUtils()) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass2 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }
            });
        }

        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.enumerateInternal(new EnumerateClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), userId, userId, FingerprintService.this.isRestricted(), FingerprintService.this.getContext().getOpPackageName()) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass3 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }
            });
        }

        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
            FingerprintService.super.addLockoutResetCallback(callback);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(FingerprintService.this.getContext(), FingerprintService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                        FingerprintService.this.dumpInternal(pw);
                    } else {
                        FingerprintService.this.dumpProto(fd);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), true)) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (!(FingerprintService.this.getFingerprintDaemon() == null || FingerprintService.this.mHalDeviceId == 0)) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void rename(final int fingerId, final int groupId, final String name) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            boolean isPrivacyUser = FingerprintService.this.checkPrivacySpaceEnroll(groupId, ActivityManager.getCurrentUser());
            if (FingerprintService.this.isCurrentUserOrProfile(groupId) || isPrivacyUser) {
                FingerprintService.this.mHandler.post(new Runnable() {
                    /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        FingerprintService.this.getBiometricUtils().renameBiometricForUser(FingerprintService.this.getContext(), groupId, fingerId, name);
                    }
                });
            } else {
                Flog.w(1303, "user invalid rename error");
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            return FingerprintService.this.getEnrolledTemplates(userId);
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            return FingerprintService.this.hasEnrolledBiometrics(userId);
        }

        public long getAuthenticatorId(String opPackageName) {
            return FingerprintService.super.getAuthenticatorId(opPackageName);
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService fingerprintService = FingerprintService.this;
            if (!fingerprintService.hasEnrolledBiometrics(fingerprintService.mCurrentUserId)) {
                Slog.w(FingerprintService.TAG, "Ignoring lockout reset, no templates enrolled");
            } else {
                FingerprintService.this.mHandler.post(FingerprintService.this.mResetFailedAttemptsForCurrentUserRunnable);
            }
        }

        public boolean isClientActive() {
            boolean z;
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            synchronized (FingerprintService.this) {
                if (FingerprintService.this.getCurrentClient() == null) {
                    if (FingerprintService.this.getPendingClient() == null) {
                        z = false;
                    }
                }
                z = true;
            }
            return z;
        }

        public void addClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.add(callback);
        }

        public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.remove(callback);
        }

        public int getRemainingNum() {
            return 0;
        }

        public long getRemainingTime() {
            return 0;
        }
    }

    private class BiometricPromptServiceListenerImpl extends BiometricServiceBase.BiometricServiceListener {
        BiometricPromptServiceListenerImpl(IBiometricServiceReceiverInternal wrapperReceiver) {
            super(wrapperReceiver);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAcquired(acquiredInfo, FingerprintManager.getAcquiredString(FingerprintService.this.getContext(), acquiredInfo, vendorCode));
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onError(cookie, error, FingerprintManager.getErrorString(FingerprintService.this.getContext(), error, vendorCode));
            }
        }
    }

    protected class ServiceListenerImpl implements BiometricServiceBase.ServiceListener {
        private IFingerprintServiceReceiver mFingerprintServiceReceiver;

        public ServiceListenerImpl(IFingerprintServiceReceiver receiver) {
            this.mFingerprintServiceReceiver = receiver;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onEnrollResult(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onAcquired(deviceId, acquiredInfo, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceeded(long deviceId, BiometricAuthenticator.Identifier biometric, int userId) throws RemoteException {
            if (this.mFingerprintServiceReceiver == null) {
                return;
            }
            if (biometric == null || (biometric instanceof Fingerprint)) {
                this.mFingerprintServiceReceiver.onAuthenticationSucceeded(deviceId, (Fingerprint) biometric, userId);
            } else {
                Slog.e(FingerprintService.TAG, "onAuthenticationSucceeded received non-fingerprint biometric");
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailed(long deviceId) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onAuthenticationFailed(deviceId);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onError(deviceId, error, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onRemoved(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnumerated(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onEnumerated(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }
    }

    public FingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mTimedLockoutCleared = new SparseBooleanArray();
        this.mFailedAttempts = new SparseIntArray();
        this.mLockoutTime = new SparseLongArray();
        context.registerReceiver(this.mLockoutReceiver, new IntentFilter(getLockoutResetIntent()), getLockoutBroadcastPermission(), null);
    }

    /* access modifiers changed from: protected */
    public FingerprintServiceWrapper creatFingerprintServiceWrapper() {
        return new FingerprintServiceWrapper();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.biometrics.fingerprint.FingerprintService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.biometrics.fingerprint.FingerprintService$FingerprintServiceWrapper, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.biometrics.BiometricServiceBase, com.android.server.SystemService
    public void onStart() {
        super.onStart();
        publishBinderService("fingerprint", creatFingerprintServiceWrapper());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$63nwn0dhn2TOgSZsFs18vTAUjP8 */

            @Override // java.lang.Runnable
            public final void run() {
                FingerprintService.this.getFingerprintDaemon();
            }
        }, "FingerprintService.onStart");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemonWrapper;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricUtils getBiometricUtils() {
        return FingerprintUtils.getInstance();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public Constants getConstants() {
        return this.mFingerprintConstants;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasReachedEnrollmentLimit(int userId) {
        int enrolled;
        int limit = getContext().getResources().getInteger(17694813);
        if (getBiometricUtils().isDualFp()) {
            enrolled = this.mEnrolled;
        } else {
            enrolled = getEnrolledTemplates(userId).size();
        }
        if (enrolled < limit) {
            return false;
        }
        Slog.w(TAG, "Too many fingerprints registered");
        return true;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void serviceDied(long cookie) {
        super.serviceDied(cookie);
        this.mDaemon = null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateActiveGroup(int userId, String clientPackage) {
        File baseDir;
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon != null) {
            this.mLockUpdateActive.lock();
            try {
                int userId2 = getUserOrWorkProfileId(clientPackage, userId);
                boolean hasFingerprints = true;
                if (userId2 != this.mCurrentUserId) {
                    int firstSdkInt = Build.VERSION.FIRST_SDK_INT;
                    if (firstSdkInt < 1) {
                        Slog.e(TAG, "First SDK version " + firstSdkInt + " is invalid; must be at least VERSION_CODES.BASE");
                    }
                    int userIdForHal = userId2;
                    UserInfo info = this.mUserManager.getUserInfo(userId2);
                    if (info != null && info.isHwHiddenSpace()) {
                        userIdForHal = -100;
                        Slog.i(TAG, "userIdForHal is -100");
                    }
                    Slog.i(TAG, "FIRST_SDK_INT:" + firstSdkInt);
                    int newUserId = userId2;
                    if (userIdForHal == -100) {
                        Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                        newUserId = 0;
                    }
                    if (firstSdkInt <= 27) {
                        baseDir = Environment.getUserSystemDirectory(newUserId);
                    } else {
                        baseDir = Environment.getDataVendorDeDirectory(newUserId);
                    }
                    File fpDir = new File(baseDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            this.mLockUpdateActive.unlock();
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            this.mLockUpdateActive.unlock();
                            return;
                        }
                    }
                    daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath());
                    this.mCurrentUserId = userId2;
                    updateFingerprints(userId2);
                }
                long authenticatorId = 0;
                if (FingerprintUtils.getInstance().isDualFp()) {
                    if (FingerprintUtils.getInstance().getFingerprintsForUser(this.mContext, userId2, -1).size() <= 0) {
                        hasFingerprints = false;
                    }
                    if (hasFingerprints) {
                        authenticatorId = daemon.getAuthenticatorId();
                    }
                    Slog.d(TAG, "daemon getAuthenticatorId = " + authenticatorId + " userId = " + userId2);
                    this.mAuthenticatorIds.put(Integer.valueOf(userId2), Long.valueOf(authenticatorId));
                } else {
                    Map map = this.mAuthenticatorIds;
                    Integer valueOf = Integer.valueOf(userId2);
                    if (hasEnrolledBiometrics(userId2)) {
                        authenticatorId = daemon.getAuthenticatorId();
                    }
                    map.put(valueOf, Long.valueOf(authenticatorId));
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveGroup():", e);
            } catch (Throwable th) {
                this.mLockUpdateActive.unlock();
                throw th;
            }
            this.mLockUpdateActive.unlock();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutResetIntent() {
        return ACTION_LOCKOUT_RESET;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutBroadcastPermission() {
        return "android.permission.RESET_FINGERPRINT_LOCKOUT";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasEnrolledBiometrics(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId).size() > 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getManageBiometricPermission() {
        return "android.permission.MANAGE_FINGERPRINT";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void checkUseBiometricPermission() {
        if (getContext().checkCallingPermission("android.permission.USE_FINGERPRINT") != 0) {
            checkPermission("android.permission.USE_BIOMETRIC");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean checkAppOps(int uid, String opPackageName) {
        if (this.mAppOps.noteOp(78, uid, opPackageName) == 0 || this.mAppOps.noteOp(55, uid, opPackageName) == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public List<Fingerprint> getEnrolledTemplates(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void notifyClientActiveCallbacks(boolean isActive) {
        List<IFingerprintClientActiveCallback> callbacks = this.mClientActiveCallbacks;
        for (int i = 0; i < callbacks.size(); i++) {
            try {
                IFingerprintClientActiveCallback fingerprintClientCallback = callbacks.get(i);
                if (fingerprintClientCallback != null) {
                    fingerprintClientCallback.onClientActiveChanged(isActive);
                }
            } catch (RemoteException e) {
                this.mClientActiveCallbacks.remove(callbacks.get(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int statsModality() {
        return 1;
    }

    private int getMaxFailedAttempsOfPermanentLockout() {
        return isSupportPowerFp() ? 50 : 20;
    }

    public boolean isSupportPowerFp() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getLockoutMode() {
        int currentUser = ActivityManager.getCurrentUser();
        int failedAttempts = this.mFailedAttempts.get(currentUser, 0);
        if (failedAttempts >= getMaxFailedAttempsOfPermanentLockout()) {
            return 2;
        }
        if (failedAttempts <= 0 || this.mTimedLockoutCleared.get(currentUser, false) || failedAttempts % 5 != 0) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public synchronized IBiometricsFingerprint getFingerprintDaemon() {
        if (this.mDaemon == null) {
            Slog.v(TAG, "mDaemon was null, reconnect to fingerprint");
            try {
                this.mDaemon = IBiometricsFingerprint.getService();
            } catch (NoSuchElementException e) {
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to get biometric interface", e2);
            }
            if (this.mDaemon == null) {
                Slog.w(TAG, "fingerprint HIDL not available");
                return null;
            }
            this.mDaemon.asBinder().linkToDeath(this, 0);
            try {
                this.mHalDeviceId = this.mDaemon.setNotify(this.mDaemonCallback);
            } catch (RemoteException e3) {
                Slog.e(TAG, "Failed to open fingerprint HAL", e3);
                this.mDaemon.asBinder().unlinkToDeath(this);
                this.mDaemon = null;
            }
            if (isSupportDualFingerprint() && this.mDaemon != null && sendCommandToHal(100) == 0) {
                try {
                    this.mUDHalDeviceId = this.mDaemon.setNotify(this.mDaemonCallback);
                    Slog.d(TAG, "dualFingerprint:mUDHalDeviceId is " + this.mUDHalDeviceId);
                } catch (RemoteException e4) {
                    Slog.e(TAG, "dualFingerprint Failed to setNotify callback for UD" + e4);
                }
            }
            Slog.v(TAG, "Fingerprint HAL id: " + this.mHalDeviceId);
            if (this.mHalDeviceId != 0) {
                loadAuthenticatorIds();
                updateActiveGroup(ActivityManager.getCurrentUser(), null);
                doTemplateCleanupForUser(ActivityManager.getCurrentUser());
            } else {
                Slog.w(TAG, "Failed to open Fingerprint HAL!");
                MetricsLogger.count(getContext(), "fingerprintd_openhal_error", 1);
                this.mDaemon.asBinder().unlinkToDeath(this);
                this.mDaemon = null;
            }
        }
        return this.mDaemon;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long startPreEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPreEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPreEnroll failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startPostEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPostEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPostEnroll failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void resetFailedAttemptsForUser(boolean clearAttemptCounter, int userId) {
        if (getLockoutMode() != 0) {
            String tag = getTag();
            Slog.v(tag, "Reset biometric lockout, clearAttemptCounter=" + clearAttemptCounter);
        }
        if (clearAttemptCounter) {
            this.mFailedAttempts.put(userId, 0);
        }
        this.mLockoutTime.put(userId, 0);
        this.mTimedLockoutCleared.put(userId, true);
        cancelLockoutResetForUser(userId);
        notifyLockoutResetMonitors();
    }

    private void cancelLockoutResetForUser(int userId) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.cancel(getLockoutResetIntentForUser(userId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleLockoutResetForUser(int userId) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 30000, getLockoutResetIntentForUser(userId));
    }

    private PendingIntent getLockoutResetIntentForUser(int userId) {
        return PendingIntent.getBroadcast(getContext(), userId, new Intent(getLockoutResetIntent()).putExtra(KEY_LOCKOUT_RESET_USER, userId), 134217728);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Fingerprint Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = getBiometricUtils().getBiometricsForUser(getContext(), userId).size();
                BiometricServiceBase.PerformanceStats stats = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
                BiometricServiceBase.PerformanceStats cryptoStats = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, N);
                set.put("accept", stats != null ? stats.accept : 0);
                set.put("reject", stats != null ? stats.reject : 0);
                set.put("acquire", stats != null ? stats.acquire : 0);
                set.put("lockout", stats != null ? stats.lockout : 0);
                set.put("permanentLockout", stats != null ? stats.permanentLockout : 0);
                set.put("acceptCrypto", cryptoStats != null ? cryptoStats.accept : 0);
                set.put("rejectCrypto", cryptoStats != null ? cryptoStats.reject : 0);
                set.put("acquireCrypto", cryptoStats != null ? cryptoStats.acquire : 0);
                set.put("lockoutCrypto", cryptoStats != null ? cryptoStats.lockout : 0);
                set.put("permanentLockoutCrypto", cryptoStats != null ? cryptoStats.permanentLockout : 0);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
        pw.println("HAL Deaths: " + this.mHALDeathCount);
        this.mHALDeathCount = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            long userToken = proto.start(2246267895809L);
            proto.write(1120986464257L, userId);
            proto.write(1120986464258L, getBiometricUtils().getBiometricsForUser(getContext(), userId).size());
            BiometricServiceBase.PerformanceStats normal = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
            if (normal != null) {
                long countsToken = proto.start(1146756268035L);
                proto.write(1120986464257L, normal.accept);
                proto.write(1120986464258L, normal.reject);
                proto.write(1120986464259L, normal.acquire);
                proto.write(1120986464260L, normal.lockout);
                proto.write(1120986464261L, normal.permanentLockout);
                proto.end(countsToken);
            }
            BiometricServiceBase.PerformanceStats crypto = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
            if (crypto != null) {
                long countsToken2 = proto.start(1146756268036L);
                proto.write(1120986464257L, crypto.accept);
                proto.write(1120986464258L, crypto.reject);
                proto.write(1120986464259L, crypto.acquire);
                proto.write(1120986464260L, crypto.lockout);
                proto.write(1120986464261L, crypto.permanentLockout);
                proto.end(countsToken2);
            }
            proto.end(userToken);
        }
        proto.flush();
        this.mPerformanceMap.clear();
        this.mCryptoPerformanceMap.clear();
    }

    /* access modifiers changed from: protected */
    public void saveWaitRunonAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> arrayList) {
    }

    /* access modifiers changed from: protected */
    public long getPowerDelayFpTime(boolean authenticated) {
        return 0;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void notifyFingerRemovedAtAuth(ClientMonitor client) {
        if (client == null) {
            Slog.e(TAG, "ClientMonitor is null");
        } else if (isSupportPowerFp()) {
            Slog.i(TAG, "power finger client.stop");
            client.stop(true);
        }
    }
}
