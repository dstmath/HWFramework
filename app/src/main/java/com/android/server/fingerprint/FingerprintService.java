package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintDaemonCallback;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.FingerprintUnlockDataCollector;
import com.android.server.ServiceThread;
import com.android.server.am.ProcessList;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerprintService extends AbsFingerprintService implements DeathRecipient {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.fingerprint.ACTION_LOCKOUT_RESET";
    private static final long CANCEL_TIMEOUT_LIMIT = 3000;
    static final boolean DEBUG = true;
    private static boolean DEBUG_FPLOG = false;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final String FINGERPRINTD = "android.hardware.fingerprint.IFingerprintDaemon";
    private static final int FINGERPRINT_ACQUIRED_FINGER_DOWN = 2002;
    private static final String FP_DATA_DIR = "fpdata";
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HW_FP_NO_COUNT_FAILED_ATTEMPS = 16777216;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MSG_USER_SWITCHING = 10;
    private static final int PRIMARY_USER_ID = 0;
    private static final int SPECIAL_USER_ID = -101;
    static final String TAG = "FingerprintService";
    private long auTime;
    private long downTime;
    private FingerprintUnlockDataCollector fpDataCollector;
    private AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private Context mContext;
    private long mCurrentAuthenticatorId;
    private ClientMonitor mCurrentClient;
    private int mCurrentUserId;
    private IFingerprintDaemon mDaemon;
    private IFingerprintDaemonCallback mDaemonCallback;
    private int mFailedAttempts;
    private final FingerprintUtils mFingerprintUtils;
    private long mHalDeviceId;
    Handler mHandler;
    int mHwFailedAttempts;
    private final String mKeyguardPackage;
    private final ArrayList<FingerprintServiceLockoutResetMonitor> mLockoutMonitors;
    private final BroadcastReceiver mLockoutReceiver;
    private final Runnable mLockoutReset;
    long mLockoutTime;
    private ClientMonitor mPendingClient;
    private final PowerManager mPowerManager;
    private final Runnable mResetClientState;
    private final UserManager mUserManager;
    private String opPackageName;

    /* renamed from: com.android.server.fingerprint.FingerprintService.5 */
    class AnonymousClass5 extends Handler {
        AnonymousClass5(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FingerprintService.MSG_USER_SWITCHING /*10*/:
                    FingerprintService.this.handleUserSwitching(msg.arg1);
                default:
                    Slog.w(FingerprintService.TAG, "Unknown message:" + msg.what);
            }
        }
    }

    /* renamed from: com.android.server.fingerprint.FingerprintService.6 */
    class AnonymousClass6 extends RemovalClient {
        AnonymousClass6(Context $anonymous0, long $anonymous1, IBinder $anonymous2, IFingerprintServiceReceiver $anonymous3, int $anonymous4, int $anonymous5, int $anonymous6, boolean $anonymous7, String $anonymous8) {
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7, $anonymous8);
        }

        public void notifyUserActivity() {
            FingerprintService.this.userActivity();
        }

        public IFingerprintDaemon getFingerprintDaemon() {
            return FingerprintService.this.getFingerprintDaemon();
        }
    }

    /* renamed from: com.android.server.fingerprint.FingerprintService.7 */
    class AnonymousClass7 extends AuthenticationClient {
        final /* synthetic */ String val$opPackageName;

        AnonymousClass7(Context $anonymous0, long $anonymous1, IBinder $anonymous2, IFingerprintServiceReceiver $anonymous3, int $anonymous4, int $anonymous5, long $anonymous6, boolean $anonymous7, String $anonymous8, int $anonymous9, String val$opPackageName) {
            this.val$opPackageName = val$opPackageName;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7, $anonymous8, $anonymous9);
        }

        public boolean onAuthenticated(int fingerId, int groupId) {
            IFingerprintServiceReceiver receiver = getReceiver();
            boolean authenticated = fingerId != 0 ? FingerprintService.DEBUG : false;
            if (receiver != null) {
                if (authenticated) {
                    Flog.bdReport(FingerprintService.this.mContext, 8, "{pkg:" + this.val$opPackageName + ",ErrorCount:" + FingerprintService.this.mHwFailedAttempts + "}");
                } else if (FingerprintService.this.auTime - FingerprintService.this.downTime > 0) {
                    Flog.bdReport(FingerprintService.this.mContext, 7, "{CostTime:" + (FingerprintService.this.auTime - FingerprintService.this.downTime) + "}");
                }
            }
            return super.onAuthenticated(fingerId, groupId);
        }

        public boolean handleFailedAttempt() {
            boolean z = false;
            boolean noNeedAddFailedAttemps = false;
            if (this.mFlags == FingerprintService.HW_FP_NO_COUNT_FAILED_ATTEMPS && "com.android.settings".equals(getOwnerString())) {
                noNeedAddFailedAttemps = FingerprintService.DEBUG;
                Slog.i(FingerprintService.TAG, "no need count failed attempts");
            }
            if (!noNeedAddFailedAttemps) {
                FingerprintService fingerprintService = FingerprintService.this;
                fingerprintService.mFailedAttempts = fingerprintService.mFailedAttempts + 1;
            }
            if (!inLockoutMode()) {
                return false;
            }
            FingerprintService.this.mLockoutTime = SystemClock.elapsedRealtime();
            FingerprintService.this.scheduleLockoutReset();
            if (!FingerprintService.this.isKeyguard(this.val$opPackageName)) {
                z = FingerprintService.DEBUG;
            }
            return z;
        }

        public void resetFailedAttempts() {
            if (inLockoutMode()) {
                Slog.v(FingerprintService.TAG, "resetFailedAttempts should be called from APP");
            } else {
                FingerprintService.this.resetFailedAttempts();
            }
        }

        public void notifyUserActivity() {
            FingerprintService.this.userActivity();
        }

        public IFingerprintDaemon getFingerprintDaemon() {
            return FingerprintService.this.getFingerprintDaemon();
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            FingerprintService.this.handleHwFailedAttempt(flags, packagesName);
        }

        public boolean inLockoutMode() {
            return FingerprintService.this.inLockoutMode();
        }
    }

    /* renamed from: com.android.server.fingerprint.FingerprintService.8 */
    class AnonymousClass8 extends EnrollClient {
        AnonymousClass8(Context $anonymous0, long $anonymous1, IBinder $anonymous2, IFingerprintServiceReceiver $anonymous3, int $anonymous4, int $anonymous5, byte[] $anonymous6, boolean $anonymous7, String $anonymous8) {
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7, $anonymous8);
        }

        public IFingerprintDaemon getFingerprintDaemon() {
            return FingerprintService.this.getFingerprintDaemon();
        }

        public void notifyUserActivity() {
            FingerprintService.this.userActivity();
        }
    }

    private class FingerprintServiceLockoutResetMonitor {
        private final IFingerprintServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable;

        public FingerprintServiceLockoutResetMonitor(IFingerprintServiceLockoutResetCallback callback) {
            this.mRemoveCallbackRunnable = new Runnable() {
                public void run() {
                    FingerprintService.this.removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor.this);
                }
            };
            this.mCallback = callback;
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mCallback.onLockoutReset(FingerprintService.this.mHalDeviceId);
                } catch (DeadObjectException e) {
                    Slog.w(FingerprintService.TAG, "Death object while invoking onLockoutReset: ", e);
                    FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    Slog.w(FingerprintService.TAG, "Failed to invoke onLockoutReset: ", e2);
                }
            }
        }
    }

    private final class FingerprintServiceWrapper extends Stub {

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ byte[] val$cryptoToken;
            final /* synthetic */ int val$flags;
            final /* synthetic */ String val$opPackageName;
            final /* synthetic */ IFingerprintServiceReceiver val$receiver;
            final /* synthetic */ boolean val$restricted;
            final /* synthetic */ IBinder val$token;
            final /* synthetic */ int val$userId;

            AnonymousClass1(IBinder val$token, byte[] val$cryptoToken, int val$userId, IFingerprintServiceReceiver val$receiver, int val$flags, boolean val$restricted, String val$opPackageName) {
                this.val$token = val$token;
                this.val$cryptoToken = val$cryptoToken;
                this.val$userId = val$userId;
                this.val$receiver = val$receiver;
                this.val$flags = val$flags;
                this.val$restricted = val$restricted;
                this.val$opPackageName = val$opPackageName;
            }

            public void run() {
                FingerprintService.this.startEnrollment(this.val$token, this.val$cryptoToken, this.val$userId, this.val$receiver, this.val$flags, this.val$restricted, this.val$opPackageName);
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ IBinder val$token;

            AnonymousClass2(IBinder val$token) {
                this.val$token = val$token;
            }

            public void run() {
                ClientMonitor client = FingerprintService.this.mCurrentClient;
                if ((client instanceof EnrollClient) && client.getToken() == this.val$token) {
                    client.stop(client.getToken() == this.val$token ? FingerprintService.DEBUG : false);
                }
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ int val$callingUid;
            final /* synthetic */ int val$callingUserId;
            final /* synthetic */ int val$flags;
            final /* synthetic */ int val$groupId;
            final /* synthetic */ long val$opId;
            final /* synthetic */ String val$opPackageName;
            final /* synthetic */ int val$pid;
            final /* synthetic */ IFingerprintServiceReceiver val$receiver;
            final /* synthetic */ boolean val$restricted;
            final /* synthetic */ IBinder val$token;

            AnonymousClass3(long val$opId, String val$opPackageName, int val$callingUid, int val$pid, IBinder val$token, int val$callingUserId, int val$groupId, IFingerprintServiceReceiver val$receiver, int val$flags, boolean val$restricted) {
                this.val$opId = val$opId;
                this.val$opPackageName = val$opPackageName;
                this.val$callingUid = val$callingUid;
                this.val$pid = val$pid;
                this.val$token = val$token;
                this.val$callingUserId = val$callingUserId;
                this.val$groupId = val$groupId;
                this.val$receiver = val$receiver;
                this.val$flags = val$flags;
                this.val$restricted = val$restricted;
            }

            public void run() {
                MetricsLogger.histogram(FingerprintService.this.mContext, "fingerprint_token", this.val$opId != 0 ? 1 : FingerprintService.PRIMARY_USER_ID);
                FingerprintService.this.setLivenessSwitch(this.val$opPackageName);
                if (FingerprintService.this.canUseFingerprint(this.val$opPackageName, FingerprintService.DEBUG, this.val$callingUid, this.val$pid)) {
                    FingerprintService.this.startAuthentication(this.val$token, this.val$opId, this.val$callingUserId, this.val$groupId, this.val$receiver, this.val$flags, this.val$restricted, this.val$opPackageName);
                } else {
                    Slog.v(FingerprintService.TAG, "authenticate(): reject " + this.val$opPackageName);
                }
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ String val$opPackageName;
            final /* synthetic */ int val$pid;
            final /* synthetic */ IBinder val$token;
            final /* synthetic */ int val$uid;

            AnonymousClass4(String val$opPackageName, int val$uid, int val$pid, IBinder val$token) {
                this.val$opPackageName = val$opPackageName;
                this.val$uid = val$uid;
                this.val$pid = val$pid;
                this.val$token = val$token;
            }

            public void run() {
                boolean z = false;
                if (FingerprintService.this.canUseFingerprint(this.val$opPackageName, false, this.val$uid, this.val$pid)) {
                    ClientMonitor client = FingerprintService.this.mCurrentClient;
                    if (client instanceof AuthenticationClient) {
                        if (client.getToken() == this.val$token) {
                            Slog.v(FingerprintService.TAG, "stop client " + client.getOwnerString());
                            if (client.getToken() == this.val$token) {
                                z = FingerprintService.DEBUG;
                            }
                            client.stop(z);
                            return;
                        }
                        Slog.v(FingerprintService.TAG, "can't stop client " + client.getOwnerString() + " since tokens don't match");
                        return;
                    } else if (client != null) {
                        Slog.v(FingerprintService.TAG, "can't cancel non-authenticating client " + client.getOwnerString());
                        return;
                    } else {
                        return;
                    }
                }
                Slog.v(FingerprintService.TAG, "cancelAuthentication(): reject " + this.val$opPackageName);
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ int val$userId;

            AnonymousClass5(int val$userId) {
                this.val$userId = val$userId;
            }

            public void run() {
                FingerprintService.this.updateActiveGroup(this.val$userId, null);
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.6 */
        class AnonymousClass6 implements Runnable {
            final /* synthetic */ int val$fingerId;
            final /* synthetic */ int val$groupId;
            final /* synthetic */ IFingerprintServiceReceiver val$receiver;
            final /* synthetic */ boolean val$restricted;
            final /* synthetic */ IBinder val$token;
            final /* synthetic */ int val$userId;

            AnonymousClass6(IBinder val$token, int val$fingerId, int val$groupId, int val$userId, IFingerprintServiceReceiver val$receiver, boolean val$restricted) {
                this.val$token = val$token;
                this.val$fingerId = val$fingerId;
                this.val$groupId = val$groupId;
                this.val$userId = val$userId;
                this.val$receiver = val$receiver;
                this.val$restricted = val$restricted;
            }

            public void run() {
                FingerprintService.this.startRemove(this.val$token, this.val$fingerId, this.val$groupId, this.val$userId, this.val$receiver, this.val$restricted);
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.7 */
        class AnonymousClass7 implements Runnable {
            final /* synthetic */ int val$fingerId;
            final /* synthetic */ int val$groupId;
            final /* synthetic */ String val$name;

            AnonymousClass7(int val$fingerId, int val$groupId, String val$name) {
                this.val$fingerId = val$fingerId;
                this.val$groupId = val$groupId;
                this.val$name = val$name;
            }

            public void run() {
                FingerprintService.this.mFingerprintUtils.renameFingerprintForUser(FingerprintService.this.mContext, this.val$fingerId, this.val$groupId, this.val$name);
            }
        }

        /* renamed from: com.android.server.fingerprint.FingerprintService.FingerprintServiceWrapper.8 */
        class AnonymousClass8 implements Runnable {
            final /* synthetic */ IFingerprintServiceLockoutResetCallback val$callback;

            AnonymousClass8(IFingerprintServiceLockoutResetCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void run() {
                FingerprintService.this.addLockoutResetMonitor(new FingerprintServiceLockoutResetMonitor(this.val$callback));
            }
        }

        private FingerprintServiceWrapper() {
        }

        public long preEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService preEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPreEnroll(token);
        }

        public int postEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService postEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            Flog.i(1303, "postEnroll client uid = " + Binder.getCallingUid() + ", postEnroll client pid = " + Binder.getCallingPid());
            return FingerprintService.this.startPostEnroll(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            Flog.i(1303, "FingerprintService enroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.getEnrolledFingerprints(userId).size() >= FingerprintService.this.mContext.getResources().getInteger(17694880)) {
                Slog.w(FingerprintService.TAG, "Too many fingerprints registered");
            } else if (FingerprintService.this.isCurrentUserOrProfile(userId)) {
                FingerprintService.this.mHandler.post(new AnonymousClass1(token, cryptoToken, userId, receiver, flags, isRestricted(), opPackageName));
            }
        }

        private boolean isRestricted() {
            return FingerprintService.this.hasPermission("android.permission.MANAGE_FINGERPRINT") ? false : FingerprintService.DEBUG;
        }

        public void cancelEnrollment(IBinder token) {
            Flog.i(1303, "FingerprintService cancelEnrollment");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            Flog.i(1303, "cancelEnrollment client uid = " + Binder.getCallingUid() + ", cancelEnrollment client pid = " + Binder.getCallingPid());
            FingerprintService.this.mHandler.post(new AnonymousClass2(token));
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            Flog.i(1303, "FingerprintService authenticate");
            int callingUid = Binder.getCallingUid();
            int callingUserId = UserHandle.getCallingUserId();
            FingerprintService.this.mHandler.post(new AnonymousClass3(opId, opPackageName, callingUid, Binder.getCallingPid(), token, callingUserId, groupId, receiver, flags, isRestricted()));
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            Flog.i(1303, "FingerprintService cancelAuthentication");
            FingerprintService.this.mHandler.post(new AnonymousClass4(opPackageName, Binder.getCallingUid(), Binder.getCallingPid(), token));
        }

        public void setActiveUser(int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new AnonymousClass5(userId));
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            Flog.i(1303, "FingerprintService remove");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new AnonymousClass6(token, fingerId, groupId, userId, receiver, isRestricted()));
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return false;
            }
            if (FingerprintService.this.mHalDeviceId != 0) {
                z = FingerprintService.DEBUG;
            }
            return z;
        }

        public void rename(int fingerId, int groupId, String name) {
            Flog.i(1303, "FingerprintService rename");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.isCurrentUserOrProfile(groupId)) {
                FingerprintService.this.mHandler.post(new AnonymousClass7(fingerId, groupId, name));
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService getEnrolledFingerprints");
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return Collections.emptyList();
            }
            if (FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return FingerprintService.this.getEnrolledFingerprints(userId);
            }
            return Collections.emptyList();
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService hasEnrolledFingerprints");
            if (FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid()) && FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return FingerprintService.this.hasEnrolledFingerprints(userId);
            }
            return false;
        }

        public long getAuthenticatorId(String opPackageName) {
            Flog.i(1303, "FingerprintService getAuthenticatorId");
            return FingerprintService.this.getAuthenticatorId(opPackageName);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (FingerprintService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Fingerprint from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                FingerprintService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService.this.mHandler.post(FingerprintService.this.mLockoutReset);
        }

        public void addLockoutResetCallback(IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
            FingerprintService.this.mHandler.post(new AnonymousClass8(callback));
        }

        public int getRemainingNum() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            Slog.d(FingerprintService.TAG, " Remaining Num Attempts = " + (5 - FingerprintService.this.mHwFailedAttempts));
            return 5 - FingerprintService.this.mHwFailedAttempts;
        }

        public long getRemainingTime() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            long now = SystemClock.elapsedRealtime();
            long nowToLockout = now - FingerprintService.this.mLockoutTime;
            Slog.d(FingerprintService.TAG, "Remaining Time mLockoutTime = " + FingerprintService.this.mLockoutTime + "  now = " + now);
            if (nowToLockout <= 0 || nowToLockout >= FingerprintService.FAIL_LOCKOUT_TIMEOUT_MS) {
                return 0;
            }
            return FingerprintService.FAIL_LOCKOUT_TIMEOUT_MS - nowToLockout;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.fingerprint.FingerprintService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.fingerprint.FingerprintService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.FingerprintService.<clinit>():void");
    }

    public FingerprintService(Context context) {
        super(context);
        this.mLockoutMonitors = new ArrayList();
        this.mCurrentUserId = -2;
        this.mHandler = null;
        this.mFingerprintUtils = FingerprintUtils.getInstance();
        this.mLockoutReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (FingerprintService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                    FingerprintService.this.resetFailedAttempts();
                }
            }
        };
        this.mHwFailedAttempts = PRIMARY_USER_ID;
        this.mLockoutTime = 0;
        this.mLockoutReset = new Runnable() {
            public void run() {
                FingerprintService.this.resetFailedAttempts();
            }
        };
        this.mResetClientState = new Runnable() {
            public void run() {
                Slog.w(FingerprintService.TAG, "Client " + (FingerprintService.this.mCurrentClient != null ? FingerprintService.this.mCurrentClient.getOwnerString() : "null") + " failed to respond to cancel, starting client " + (FingerprintService.this.mPendingClient != null ? FingerprintService.this.mPendingClient.getOwnerString() : "null"));
                FingerprintService.this.mCurrentClient = null;
                FingerprintService.this.startClient(FingerprintService.this.mPendingClient, false);
                FingerprintService.this.mPendingClient = null;
            }
        };
        this.mDaemonCallback = new IFingerprintDaemonCallback.Stub() {

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ long val$deviceId;
                final /* synthetic */ int val$fingerId;
                final /* synthetic */ int val$groupId;
                final /* synthetic */ int val$remaining;

                AnonymousClass1(long val$deviceId, int val$fingerId, int val$groupId, int val$remaining) {
                    this.val$deviceId = val$deviceId;
                    this.val$fingerId = val$fingerId;
                    this.val$groupId = val$groupId;
                    this.val$remaining = val$remaining;
                }

                public void run() {
                    Slog.w(FingerprintService.TAG, "onEnrollResult 2");
                    FingerprintService.this.handleEnrollResult(this.val$deviceId, this.val$fingerId, this.val$groupId, this.val$remaining);
                }
            }

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.2 */
            class AnonymousClass2 implements Runnable {
                final /* synthetic */ int val$acquiredInfo;
                final /* synthetic */ long val$deviceId;

                AnonymousClass2(long val$deviceId, int val$acquiredInfo) {
                    this.val$deviceId = val$deviceId;
                    this.val$acquiredInfo = val$acquiredInfo;
                }

                public void run() {
                    Slog.w(FingerprintService.TAG, "onAcquired 2");
                    FingerprintService.this.handleAcquired(this.val$deviceId, this.val$acquiredInfo);
                }
            }

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.3 */
            class AnonymousClass3 implements Runnable {
                final /* synthetic */ long val$deviceId;
                final /* synthetic */ int val$fingerId;
                final /* synthetic */ int val$groupId;

                AnonymousClass3(long val$deviceId, int val$fingerId, int val$groupId) {
                    this.val$deviceId = val$deviceId;
                    this.val$fingerId = val$fingerId;
                    this.val$groupId = val$groupId;
                }

                public void run() {
                    Slog.w(FingerprintService.TAG, "onAuthenticated 2");
                    FingerprintService.this.handleAuthenticated(this.val$deviceId, this.val$fingerId, this.val$groupId);
                }
            }

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.4 */
            class AnonymousClass4 implements Runnable {
                final /* synthetic */ long val$deviceId;
                final /* synthetic */ int val$error;

                AnonymousClass4(long val$deviceId, int val$error) {
                    this.val$deviceId = val$deviceId;
                    this.val$error = val$error;
                }

                public void run() {
                    Slog.w(FingerprintService.TAG, "onError 2");
                    FingerprintService.this.handleError(this.val$deviceId, this.val$error);
                }
            }

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.5 */
            class AnonymousClass5 implements Runnable {
                final /* synthetic */ long val$deviceId;
                final /* synthetic */ int val$fingerId;
                final /* synthetic */ int val$groupId;

                AnonymousClass5(long val$deviceId, int val$fingerId, int val$groupId) {
                    this.val$deviceId = val$deviceId;
                    this.val$fingerId = val$fingerId;
                    this.val$groupId = val$groupId;
                }

                public void run() {
                    Slog.w(FingerprintService.TAG, "onRemoved 2");
                    FingerprintService.this.handleRemoved(this.val$deviceId, this.val$fingerId, this.val$groupId);
                }
            }

            /* renamed from: com.android.server.fingerprint.FingerprintService.4.6 */
            class AnonymousClass6 implements Runnable {
                final /* synthetic */ long val$deviceId;
                final /* synthetic */ int[] val$fingerIds;
                final /* synthetic */ int[] val$groupIds;

                AnonymousClass6(long val$deviceId, int[] val$fingerIds, int[] val$groupIds) {
                    this.val$deviceId = val$deviceId;
                    this.val$fingerIds = val$fingerIds;
                    this.val$groupIds = val$groupIds;
                }

                public void run() {
                    FingerprintService.this.handleEnumerate(this.val$deviceId, this.val$fingerIds, this.val$groupIds);
                }
            }

            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
                Slog.w(FingerprintService.TAG, "onEnrollResult 1");
                FingerprintService.this.mHandler.post(new AnonymousClass1(deviceId, fingerId, groupId, remaining));
            }

            public void onAcquired(long deviceId, int acquiredInfo) {
                Slog.w(FingerprintService.TAG, "onAcquired 1");
                if (FingerprintService.DEBUG_FPLOG) {
                    if (acquiredInfo == FingerprintService.FINGERPRINT_ACQUIRED_FINGER_DOWN && FingerprintService.this.fpDataCollector != null) {
                        FingerprintService.this.fpDataCollector.reportFingerDown();
                        FingerprintService.this.downTime = System.currentTimeMillis();
                    } else if (acquiredInfo == 0 && FingerprintService.this.fpDataCollector != null) {
                        FingerprintService.this.fpDataCollector.reportCaptureCompleted();
                    }
                }
                FingerprintService.this.mHandler.post(new AnonymousClass2(deviceId, acquiredInfo));
            }

            public void onAuthenticated(long deviceId, int fingerId, int groupId) {
                Slog.w(FingerprintService.TAG, "onAuthenticated 1");
                if (FingerprintService.DEBUG_FPLOG && FingerprintService.this.fpDataCollector != null) {
                    FingerprintService.this.fpDataCollector.reportFingerprintAuthenticated(fingerId != 0 ? FingerprintService.DEBUG : false);
                    FingerprintService.this.auTime = System.currentTimeMillis();
                }
                FingerprintService.this.mHandler.post(new AnonymousClass3(deviceId, fingerId, groupId));
            }

            public void onError(long deviceId, int error) {
                Slog.w(FingerprintService.TAG, "onError 1");
                FingerprintService.this.mHandler.post(new AnonymousClass4(deviceId, error));
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) {
                Slog.w(FingerprintService.TAG, "onRemoved 1");
                FingerprintService.this.mHandler.post(new AnonymousClass5(deviceId, fingerId, groupId));
            }

            public void onEnumerate(long deviceId, int[] fingerIds, int[] groupIds) {
                FingerprintService.this.mHandler.post(new AnonymousClass6(deviceId, fingerIds, groupIds));
            }
        };
        this.mContext = context;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039463)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FINGERPRINT_LOCKOUT", null);
        this.mUserManager = UserManager.get(this.mContext);
        this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
        ServiceThread fingerprintThread = new ServiceThread("fingerprintServcie", -8, false);
        fingerprintThread.start();
        this.mHandler = new AnonymousClass5(fingerprintThread.getLooper());
    }

    public void binderDied() {
        Slog.v(TAG, "fingerprintd died");
        this.mDaemon = null;
        this.mCurrentUserId = -2;
        handleError(this.mHalDeviceId, 1);
    }

    public IFingerprintDaemon getFingerprintDaemon() {
        if (this.mDaemon == null) {
            this.mDaemon = IFingerprintDaemon.Stub.asInterface(ServiceManager.getService(FINGERPRINTD));
            if (this.mDaemon != null) {
                try {
                    this.mDaemon.asBinder().linkToDeath(this, PRIMARY_USER_ID);
                    this.mDaemon.init(this.mDaemonCallback);
                    this.mHalDeviceId = this.mDaemon.openHal();
                    if (this.mHalDeviceId != 0) {
                        updateActiveGroup(ActivityManager.getCurrentUser(), null);
                    } else {
                        Slog.w(TAG, "Failed to open Fingerprint HAL!");
                        this.mDaemon = null;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to open fingeprintd HAL", e);
                    this.mDaemon = null;
                }
            } else {
                Slog.w(TAG, "fingerprint service not available");
            }
        }
        return this.mDaemon;
    }

    protected void handleEnumerate(long deviceId, int[] fingerIds, int[] groupIds) {
        if (fingerIds.length != groupIds.length) {
            Slog.w(TAG, "fingerIds and groupIds differ in length: f[]=" + Arrays.toString(fingerIds) + ", g[]=" + Arrays.toString(groupIds));
        } else {
            Slog.w(TAG, "Enumerate: f[]=" + fingerIds + ", g[]=" + groupIds);
        }
    }

    protected void handleError(long deviceId, int error) {
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onError(error)) {
            removeClient(client);
        }
        Slog.v(TAG, "handleError(client=" + (client != null ? client.getOwnerString() : "null") + ", error = " + error + ")");
        if (error == MAX_FAILED_ATTEMPTS) {
            this.mHandler.removeCallbacks(this.mResetClientState);
            if (this.mPendingClient != null) {
                Slog.v(TAG, "start pending client " + this.mPendingClient.getOwnerString());
                startClient(this.mPendingClient, false);
                this.mPendingClient = null;
            }
        }
    }

    protected void handleRemoved(long deviceId, int fingerId, int groupId) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client != null && client.onRemoved(fingerId, groupId)) {
            removeClient(client);
        }
    }

    protected void handleAuthenticated(long deviceId, int fingerId, int groupId) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client != null && client.onAuthenticated(fingerId, groupId)) {
            removeClient(client);
        }
    }

    protected void handleAcquired(long deviceId, int acquiredInfo) {
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onAcquired(acquiredInfo)) {
            removeClient(client);
        }
    }

    protected void handleEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client == null || !client.onEnrollResult(fingerId, groupId, remaining)) {
            Slog.w(TAG, "no eroll client, remove erolled fingerprint");
            if (remaining == 0) {
                IFingerprintDaemon daemon = getFingerprintDaemon();
                if (daemon != null) {
                    try {
                        daemon.remove(fingerId, ActivityManager.getCurrentUser());
                    } catch (RemoteException e) {
                    }
                } else {
                    return;
                }
            }
        }
        removeClient(client);
    }

    protected int getRealUserIdForApp(int groupId) {
        if (groupId != HIDDEN_SPACE_ID) {
            return groupId;
        }
        for (UserInfo user : this.mUserManager.getUsers(DEBUG)) {
            if (user != null && user.isHwHiddenSpace()) {
                return user.id;
            }
        }
        Slog.w(TAG, "getRealUserIdForApp error return 0");
        return PRIMARY_USER_ID;
    }

    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, PRIMARY_USER_ID);
    }

    void handleUserSwitching(int userId) {
        updateActiveGroup(userId, null);
    }

    private void removeClient(ClientMonitor client) {
        if (client != null) {
            client.destroy();
            if (!(client == this.mCurrentClient || this.mCurrentClient == null)) {
                Slog.w(TAG, new StringBuilder().append("Unexpected client: ").append(client.getOwnerString()).append("expected: ").append(this.mCurrentClient).toString() != null ? this.mCurrentClient.getOwnerString() : "null");
            }
        }
        if (this.mCurrentClient != null) {
            Slog.v(TAG, "Done with client: " + client.getOwnerString());
            this.mCurrentClient = null;
        }
    }

    private boolean inLockoutMode() {
        return this.mFailedAttempts >= MAX_FAILED_ATTEMPTS ? DEBUG : false;
    }

    private void scheduleLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + FAIL_LOCKOUT_TIMEOUT_MS, getLockoutResetIntent());
    }

    private void cancelLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.cancel(getLockoutResetIntent());
    }

    private PendingIntent getLockoutResetIntent() {
        return PendingIntent.getBroadcast(this.mContext, PRIMARY_USER_ID, new Intent(ACTION_LOCKOUT_RESET), 134217728);
    }

    protected void resetFailedAttempts() {
        if (inLockoutMode()) {
            Slog.v(TAG, "Reset fingerprint lockout");
        }
        this.mFailedAttempts = PRIMARY_USER_ID;
        cancelLockoutReset();
        notifyLockoutResetMonitors();
        this.mHandler.removeCallbacks(this.mLockoutReset);
        this.mLockoutTime = 0;
        this.mHwFailedAttempts = PRIMARY_USER_ID;
    }

    protected void handleHwFailedAttempt(int flags, String packagesName) {
        if (flags == HW_FP_NO_COUNT_FAILED_ATTEMPS && "com.android.settings".equals(packagesName)) {
            Slog.i(TAG, "no need count hw failed attempts");
        } else {
            this.mHwFailedAttempts++;
        }
    }

    public long startPreEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPreEnroll: no fingeprintd!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPreEnroll failed", e);
            return 0;
        }
    }

    public int startPostEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPostEnroll: no fingeprintd!");
            return PRIMARY_USER_ID;
        }
        try {
            return daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPostEnroll failed", e);
            return PRIMARY_USER_ID;
        }
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "father class call setLivenessSwitch");
    }

    private void startClient(ClientMonitor newClient, boolean initiatedByClient) {
        ClientMonitor currentClient = this.mCurrentClient;
        this.mHandler.removeCallbacks(this.mResetClientState);
        if (currentClient != null) {
            Slog.v(TAG, "request stop current client " + currentClient.getOwnerString());
            currentClient.stop(initiatedByClient);
            if (this.mPendingClient != null) {
                this.mPendingClient.destroy();
            }
            this.mPendingClient = newClient;
            this.mHandler.removeCallbacks(this.mResetClientState);
            this.mHandler.postDelayed(this.mResetClientState, CANCEL_TIMEOUT_LIMIT);
        } else if (newClient != null) {
            this.mCurrentClient = newClient;
            Slog.v(TAG, "starting client " + newClient.getClass().getSuperclass().getSimpleName() + "(" + newClient.getOwnerString() + ")" + ", initiatedByClient = " + initiatedByClient + ")");
            newClient.start();
        }
    }

    void startRemove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, boolean restricted) {
        if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startRemove: no fingeprintd!");
            return;
        }
        startClient(new AnonymousClass6(getContext(), this.mHalDeviceId, token, receiver, fingerId, groupId, userId, restricted, token.toString()), DEBUG);
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId);
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size() > 0) {
            return DEBUG;
        }
        return false;
    }

    boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0 ? DEBUG : false;
    }

    void checkPermission(String permission) {
        getContext().enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            userId = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId;
        }
        Slog.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    boolean isCurrentUserOrProfile(int userId) {
        int[] enabledProfileIds = UserManager.get(this.mContext).getEnabledProfileIds(userId);
        int length = enabledProfileIds.length;
        for (int i = PRIMARY_USER_ID; i < length; i++) {
            if (enabledProfileIds[i] == userId) {
                return DEBUG;
            }
        }
        return false;
    }

    private boolean isForegroundActivity(int uid, int pid) {
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            int N = procs.size();
            for (int i = PRIMARY_USER_ID; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.pid == pid && proc.uid == uid && proc.importance == 100) {
                    return DEBUG;
                }
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
        return false;
    }

    private boolean canUseFingerprint(String opPackageName, boolean foregroundOnly, int uid, int pid) {
        checkPermission("android.permission.USE_FINGERPRINT");
        this.opPackageName = opPackageName;
        if (opPackageName != null && (opPackageName.equals("com.huawei.hwasm") || opPackageName.equals("com.huawei.securitymgr") || isKeyguard(opPackageName))) {
            return DEBUG;
        }
        if (!isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; not a current user or profile");
            return false;
        } else if (this.mAppOps.noteOp(55, uid, opPackageName) != 0) {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; permission denied");
            return false;
        } else if (!foregroundOnly || isForegroundActivity(uid, pid)) {
            return DEBUG;
        } else {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; not in foreground");
            return false;
        }
    }

    private boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    private void addLockoutResetMonitor(FingerprintServiceLockoutResetMonitor monitor) {
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    private void removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }

    private void notifyLockoutResetMonitors() {
        for (int i = PRIMARY_USER_ID; i < this.mLockoutMonitors.size(); i++) {
            ((FingerprintServiceLockoutResetMonitor) this.mLockoutMonitors.get(i)).sendLockoutReset();
        }
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        int newGroupId = groupId;
        updateActiveGroup(groupId, opPackageName);
        Slog.v(TAG, "startAuthentication(" + opPackageName + ")");
        if (shouldAuthBothSpaceFingerprints(opPackageName, flags)) {
            Slog.i(TAG, "should authenticate both space fingerprints");
            newGroupId = SPECIAL_USER_ID;
        }
        AuthenticationClient client = new AnonymousClass7(getContext(), this.mHalDeviceId, token, receiver, this.mCurrentUserId, newGroupId, opId, restricted, opPackageName, flags, opPackageName);
        if (!inLockoutMode() || isKeyguard(opPackageName)) {
            startClient(client, DEBUG);
            return;
        }
        Slog.v(TAG, "In lockout mode; disallowing authentication");
        if (!client.onError(7)) {
            Slog.w(TAG, "Cannot send timeout message to client");
        }
    }

    private void startEnrollment(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        updateActiveGroup(userId, opPackageName);
        int groupId = userId;
        startClient(new AnonymousClass8(getContext(), this.mHalDeviceId, token, receiver, userId, userId, cryptoToken, restricted, opPackageName), DEBUG);
    }

    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Fingerprint Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put("count", N);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
    }

    public void onStart() {
        publishBinderService("fingerprint", new FingerprintServiceWrapper());
        IFingerprintDaemon daemon = getFingerprintDaemon();
        Slog.v(TAG, "Fingerprint HAL id: " + this.mHalDeviceId);
        listenForUserSwitches();
    }

    public void onBootPhase(int phase) {
        Slog.d(TAG, "Fingerprint daemon is phase :" + phase);
        if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            Slog.d(TAG, "Fingerprint mDaemon is " + this.mDaemon);
            if (getFingerprintDaemon() == null) {
                Slog.w(TAG, "Fingerprint daemon is null");
            }
        }
    }

    private void updateActiveGroup(int userId, String clientPackage) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                userId = getUserOrWorkProfileId(clientPackage, userId);
                if (userId != this.mCurrentUserId) {
                    File systemDir;
                    int userIdForHal = userId;
                    UserInfo info = this.mUserManager.getUserInfo(userId);
                    if (info != null && info.isHwHiddenSpace()) {
                        userIdForHal = HIDDEN_SPACE_ID;
                        Slog.i(TAG, "userIdForHal is " + HIDDEN_SPACE_ID);
                    }
                    if (userIdForHal == HIDDEN_SPACE_ID) {
                        Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                        systemDir = Environment.getUserSystemDirectory(PRIMARY_USER_ID);
                    } else {
                        systemDir = Environment.getUserSystemDirectory(userId);
                    }
                    File fpDir = new File(systemDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath().getBytes());
                    this.mCurrentUserId = userId;
                    updateFingerprints(userId);
                }
                this.mCurrentAuthenticatorId = daemon.getAuthenticatorId();
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveGroup():", e);
            }
        }
    }

    private int getUserOrWorkProfileId(String clientPackage, int userId) {
        if (isKeyguard(clientPackage) || !isWorkProfile(userId)) {
            return getEffectiveUserId(userId);
        }
        return userId;
    }

    private boolean isWorkProfile(int userId) {
        UserInfo info = this.mUserManager.getUserInfo(userId);
        return info != null ? info.isManagedProfile() : false;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    FingerprintService.this.mHandler.obtainMessage(FingerprintService.MSG_USER_SWITCHING, newUserId, FingerprintService.PRIMARY_USER_ID).sendToTarget();
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            });
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    public long getAuthenticatorId(String opPackageName) {
        return this.mCurrentAuthenticatorId;
    }
}
