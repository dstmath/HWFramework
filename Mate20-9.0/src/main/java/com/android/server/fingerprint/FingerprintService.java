package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyStore;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.DumpUtils;
import com.android.server.FingerprintDataInterface;
import com.android.server.ServiceThread;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.am.AssistDataRequester;
import com.android.server.utils.PriorityDump;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerprintService extends AbsFingerprintService implements IHwBinder.DeathRecipient {
    private static final String ACTION_AUTH_FINGER_UP = "com.huawei.finger.action_up";
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.fingerprint.ACTION_LOCKOUT_RESET";
    private static final int BASE_BRIGHTNESS = 3000;
    private static final long CANCEL_TIMEOUT_LIMIT = 3000;
    private static final boolean CLEANUP_UNUSED_FP = true;
    private static final int CODE_DISABLE_FINGERPRINT_VIEW = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW = 1115;
    private static final int CODE_FINGERPRINT_THREESIDESAUTH = 1125;
    private static final int CODE_GET_FINGERPRINT_LIST_ENROLLED = 1118;
    private static final int CODE_GET_HARDWARE_POSITION = 1110;
    private static final int CODE_GET_HARDWARE_TYPE = 1109;
    private static final int CODE_GET_HIGHLIGHT_SPOT_RADIUS = 1122;
    private static final int CODE_GET_HOVER_SUPPORT = 1113;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FINGERPRINT_HARDWARE_DETECTED = 1119;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_IS_SUPPORT_DUAL_FINGERPRINT = 1120;
    private static final int CODE_IS_WAIT_AUTHEN = 1127;
    private static final int CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION = 1116;
    private static final int CODE_NOTIFY_OPTICAL_CAPTURE = 1111;
    private static final int CODE_POWER_KEYCODE = 1126;
    private static final int CODE_REMOVE_FINGERPRINT = 1107;
    private static final int CODE_REMOVE_MASK_AND_SHOW_BUTTON = 1117;
    private static final int CODE_SEND_UNLOCK_LIGHTBRIGHT = 1121;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    private static final int CODE_SET_FINGERPRINT_MASK_VIEW = 1104;
    private static final int CODE_SET_HOVER_SWITCH = 1112;
    private static final int CODE_SHOW_FINGERPRINT_BUTTON = 1106;
    private static final int CODE_SHOW_FINGERPRINT_VIEW = 1105;
    private static final int CODE_SUSPEND_AUTHENTICATE = 1108;
    private static final int CODE_SUSPEND_ENROLL = 1123;
    private static final int CODE_UDFINGERPRINT_SPOTCOLOR = 1124;
    static final boolean DEBUG = true;
    private static boolean DEBUG_FPLOG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String DOCOMO_PACKAGE_NAME = "com.nttdocomo.android.idmanager";
    protected static final int ENROLL_UD = 4096;
    private static final int ERROR_CODE_COMMEN_ERROR = 8;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final int FINGERPRINT_ACQUIRED_FINGER_DOWN = 2002;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING = 1;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING_SETTINGS = 2;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING_SYSTEMUI = 3;
    protected static final int FINGER_DOWN_TYPE_ENROLLING = 0;
    private static final int FP_CLOSE = 0;
    private static final String FP_DATA_DIR = "fpdata";
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HUAWEI_FINGERPRINT_CAPTURE_COMPLETE = 0;
    private static final int HUAWEI_FINGERPRINT_DOWN = 2002;
    private static final int HUAWEI_FINGERPRINT_DOWN_UD = 2102;
    private static final int HUAWEI_FINGERPRINT_TRIGGER_FACE_RECOGNIZATION = 2104;
    private static final int HUAWEI_FINGERPRINT_UP = 2003;
    private static final String HWUI_PACKAGE_NAME = "com.huawei.hwid";
    protected static final int HW_FP_AUTH_BOTH_SPACE = 33554432;
    protected static final int HW_FP_AUTH_UD = 134217728;
    protected static final int HW_FP_AUTH_UG = 67108864;
    private static final int HW_FP_NO_COUNT_FAILED_ATTEMPS = 16777216;
    private static final String KEY_LOCKOUT_RESET_USER = "lockout_reset_user";
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT = 20;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT_FP = 50;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    private static final int MSG_USER_SWITCHING = 10;
    private static final String PERM_AUTH_FINGER_UP = "com.huawei.authentication.HW_ACCESS_AUTH_SERVICE";
    private static final int PRIMARY_USER_ID = 0;
    protected static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final int SPECIAL_USER_ID = -101;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    static final String TAG = "FingerprintService";
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_FAIL = 1;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_SUCCESS = 0;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_UNCHECKED = 2;
    /* access modifiers changed from: private */
    public long auTime;
    /* access modifiers changed from: private */
    public long downTime;
    /* access modifiers changed from: private */
    public FingerprintDataInterface fpDataCollector;
    /* access modifiers changed from: private */
    public final IActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private final Map<Integer, Long> mAuthenticatorIds = Collections.synchronizedMap(new HashMap());
    /* access modifiers changed from: private */
    public final CopyOnWriteArrayList<IFingerprintClientActiveCallback> mClientActiveCallbacks = new CopyOnWriteArrayList<>();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public HashMap<Integer, PerformanceStats> mCryptoPerformanceMap = new HashMap<>();
    /* access modifiers changed from: private */
    public int mCurrentAuthFpDev;
    private long mCurrentAuthenticatorId;
    protected ClientMonitor mCurrentClient;
    /* access modifiers changed from: private */
    public int mCurrentUserId = -10000;
    @GuardedBy("this")
    private IBiometricsFingerprint mDaemon;
    private IBiometricsFingerprintClientCallback mDaemonCallback = new IBiometricsFingerprintClientCallback.Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            Slog.w(FingerprintService.TAG, "onEnrollResult 1");
            Handler handler = FingerprintService.this.mHandler;
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            AnonymousClass1 r1 = new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onEnrollResult 2");
                    FingerprintService.this.handleEnrollResult(j, i, i2, i3);
                }
            };
            handler.post(r1);
        }

        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) {
            Slog.w(FingerprintService.TAG, "onAcquired 1");
            Handler handler = FingerprintService.this.mHandler;
            final long j = deviceId;
            final int i = acquiredInfo;
            final int i2 = vendorCode;
            AnonymousClass2 r1 = new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onAcquired 2");
                    FingerprintService.this.handleAcquired(j, i, i2);
                }
            };
            handler.post(r1);
            int clientAcquireInfo = acquiredInfo == 6 ? vendorCode + 1000 : acquiredInfo;
            if (clientAcquireInfo == 2002 && FingerprintService.this.fpDataCollector != null) {
                FingerprintService.this.fpDataCollector.reportFingerDown();
            } else if (clientAcquireInfo == 0 && FingerprintService.this.fpDataCollector != null) {
                FingerprintService.this.fpDataCollector.reportCaptureCompleted();
            }
            if (clientAcquireInfo == 2002) {
                long unused = FingerprintService.this.downTime = System.currentTimeMillis();
            }
            if (clientAcquireInfo == 2002 && FingerprintService.this.currentClient(FingerprintService.this.mKeyguardPackage)) {
                LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_MINUS);
            }
            if (FingerprintService.this.mCurrentClient == null) {
                Log.e(FingerprintService.TAG, "mCurrentClient is null notifyFinger failed");
                return;
            }
            if (clientAcquireInfo == 2002) {
                Log.d(FingerprintService.TAG, "onAcquired set mCurrentAuthFpDev DEVICE_BACK");
                int unused2 = FingerprintService.this.mCurrentAuthFpDev = 0;
            }
            String currentOpName = FingerprintService.this.mCurrentClient.getOwnerString();
            if (FingerprintService.this.isSupportPowerFp() && FingerprintService.this.currentClient(FingerprintService.this.mKeyguardPackage)) {
                if (clientAcquireInfo == 2002) {
                    FingerprintService.this.setKeyguardAuthenScreenOn();
                } else if (clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_UP) {
                    FingerprintService.this.setNoWaitPowerEvent();
                }
            }
            if (clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_DOWN_UD) {
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    Log.d(FingerprintService.TAG, "notify that AuthenticationClient finger down:" + currentOpName);
                    int unused3 = FingerprintService.this.mCurrentAuthFpDev = 1;
                    if (FingerprintService.SYSTEMUI_PACKAGE_NAME.equals(currentOpName)) {
                        FingerprintService.this.notifyFingerDown(3);
                    } else {
                        FingerprintService.this.notifyFingerDown(1);
                    }
                } else if (FingerprintService.this.mCurrentClient instanceof EnrollClient) {
                    Log.d(FingerprintService.TAG, "notify that EnrollClient finger down");
                    FingerprintService.this.notifyFingerDown(0);
                }
            } else if (clientAcquireInfo == 5 || clientAcquireInfo == 1 || clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_UP) {
                if (clientAcquireInfo == 5) {
                    Log.d(FingerprintService.TAG, "FINGERPRINT_ACQUIRED_TOO_FAST notifyCaptureFinished");
                    FingerprintService.this.notifyCaptureFinished(1);
                }
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    Log.d(FingerprintService.TAG, "clientAcquireInfo = " + clientAcquireInfo);
                    FingerprintService.this.notifyAuthenticationFinished(currentOpName, 2, FingerprintService.this.mHwFailedAttempts);
                }
            } else if (clientAcquireInfo == 0) {
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    if (FingerprintService.SETTINGS_PACKAGE_NAME.equals(currentOpName)) {
                        FingerprintService.this.notifyCaptureFinished(2);
                    } else {
                        FingerprintService.this.notifyCaptureFinished(1);
                    }
                }
            } else if (clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_TRIGGER_FACE_RECOGNIZATION) {
                Log.d(FingerprintService.TAG, "clientAcquireInfo = " + clientAcquireInfo);
                FingerprintService.this.triggerFaceRecognization();
            }
        }

        public void onAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
            Slog.w(FingerprintService.TAG, "onAuthenticated 1");
            long delayTime = FingerprintService.this.getPowerDelayFpTime();
            Slog.i(FingerprintService.TAG, "onAuthenticated run delay time:" + delayTime);
            if (delayTime >= 0) {
                Handler handler = FingerprintService.this.mHandler;
                final long j = deviceId;
                final int i = fingerId;
                final int i2 = groupId;
                final ArrayList<Byte> arrayList = token;
                AnonymousClass3 r0 = new Runnable() {
                    public void run() {
                        Slog.w(FingerprintService.TAG, "onAuthenticated 2");
                        FingerprintService.this.handleAuthenticated(j, i, i2, arrayList);
                    }
                };
                handler.postDelayed(r0, delayTime);
                return;
            }
            FingerprintService.this.saveWaitRunonAuthenticated(deviceId, fingerId, groupId, token);
        }

        public void onError(long deviceId, int error, int vendorCode) {
            Slog.w(FingerprintService.TAG, "onError 1");
            Handler handler = FingerprintService.this.mHandler;
            final long j = deviceId;
            final int i = error;
            final int i2 = vendorCode;
            AnonymousClass4 r1 = new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onError 2");
                    FingerprintService.this.handleError(j, i, i2);
                }
            };
            handler.post(r1);
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            Slog.w(FingerprintService.TAG, "onRemoved 1");
            Handler handler = FingerprintService.this.mHandler;
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            AnonymousClass5 r1 = new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onRemoved 2");
                    FingerprintService.this.handleRemoved(j, i, i2, i3);
                }
            };
            handler.post(r1);
        }

        public void onEnumerate(long deviceId, int fingerId, int groupId, int remaining) {
            Handler handler = FingerprintService.this.mHandler;
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            AnonymousClass6 r1 = new Runnable() {
                public void run() {
                    FingerprintService.this.handleEnumerate(j, i, i2, i3);
                }
            };
            handler.post(r1);
        }
    };
    /* access modifiers changed from: private */
    public SparseIntArray mFailedAttempts;
    /* access modifiers changed from: private */
    public final FingerprintUtils mFingerprintUtils = FingerprintUtils.getInstance();
    /* access modifiers changed from: private */
    public long mHalDeviceId;
    Handler mHandler = null;
    /* access modifiers changed from: private */
    public HwCustFingerprintService mHwCust = null;
    int mHwFailedAttempts = 0;
    /* access modifiers changed from: private */
    public final String mKeyguardPackage;
    private final ArrayList<FingerprintServiceLockoutResetMonitor> mLockoutMonitors = new ArrayList<>();
    private final BroadcastReceiver mLockoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (FingerprintService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                FingerprintService.this.resetFailedAttemptsForUser(true, intent.getIntExtra(FingerprintService.KEY_LOCKOUT_RESET_USER, 0));
            }
        }
    };
    long mLockoutTime = 0;
    /* access modifiers changed from: private */
    public ClientMonitor mPendingClient;
    /* access modifiers changed from: private */
    public HashMap<Integer, PerformanceStats> mPerformanceMap = new HashMap<>();
    /* access modifiers changed from: private */
    public PerformanceStats mPerformanceStats;
    protected final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public final Runnable mResetClientState = new Runnable() {
        public void run() {
            StringBuilder sb = new StringBuilder();
            sb.append("Client ");
            sb.append(FingerprintService.this.mCurrentClient != null ? FingerprintService.this.mCurrentClient.getOwnerString() : "null");
            sb.append(" failed to respond to cancel, starting client ");
            sb.append(FingerprintService.this.mPendingClient != null ? FingerprintService.this.mPendingClient.getOwnerString() : "null");
            Slog.w(FingerprintService.TAG, sb.toString());
            FingerprintService.this.mCurrentClient = null;
            FingerprintService.this.startClient(FingerprintService.this.mPendingClient, false);
            ClientMonitor unused = FingerprintService.this.mPendingClient = null;
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mResetFailedAttemptsForCurrentUserRunnable = new Runnable() {
        public void run() {
            FingerprintService.this.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
        }
    };
    private IStatusBarService mStatusBarService;
    /* access modifiers changed from: private */
    public boolean mSupportKids = SystemProperties.getBoolean("ro.config.kidsfinger_enable", false);
    /* access modifiers changed from: private */
    public final TaskStackListener mTaskStackListener = new TaskStackListener() {
        public void onTaskStackChanged() {
            try {
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    String currentClient = FingerprintService.this.mCurrentClient.getOwnerString();
                    if (!FingerprintService.this.isKeyguard(currentClient)) {
                        List<ActivityManager.RunningTaskInfo> runningTasks = FingerprintService.this.mActivityManager.getTasks(1);
                        if (!runningTasks.isEmpty()) {
                            String topPackage = runningTasks.get(0).topActivity.getPackageName();
                            if (!topPackage.contentEquals(currentClient)) {
                                Slog.e(FingerprintService.TAG, "Stopping background authentication, top: " + topPackage + " currentClient: " + currentClient);
                                FingerprintService.this.mCurrentClient.stop(false);
                                FingerprintService.this.notifyAuthenticationCanceled(topPackage);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.e(FingerprintService.TAG, "Unable to get running tasks", e);
            }
        }
    };
    /* access modifiers changed from: private */
    public SparseBooleanArray mTimedLockoutCleared;
    private IBinder mToken = new Binder();
    private long mUDHalDeviceId;
    private ArrayList<UserFingerprint> mUnknownFingerprints = new ArrayList<>();
    private final UserManager mUserManager;
    private ClientMonitor mWaitupClient;
    private String opPackageName;

    private class FingerprintServiceLockoutResetMonitor implements IBinder.DeathRecipient {
        private static final long WAKELOCK_TIMEOUT_MS = 2000;
        private final IFingerprintServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable = new Runnable() {
            public void run() {
                FingerprintServiceLockoutResetMonitor.this.releaseWakelock();
                FingerprintService.this.removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor.this);
            }
        };
        private final PowerManager.WakeLock mWakeLock;

        public FingerprintServiceLockoutResetMonitor(IFingerprintServiceLockoutResetCallback callback) {
            this.mCallback = callback;
            this.mWakeLock = FingerprintService.this.mPowerManager.newWakeLock(1, "lockout reset callback");
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.w(FingerprintService.TAG, "caught remote exception in linkToDeath", e);
            }
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
                    this.mCallback.onLockoutReset(FingerprintService.this.mHalDeviceId, new IRemoteCallback.Stub() {
                        public void sendResult(Bundle data) throws RemoteException {
                            FingerprintServiceLockoutResetMonitor.this.releaseWakelock();
                        }
                    });
                } catch (DeadObjectException e) {
                    Slog.w(FingerprintService.TAG, "Death object while invoking onLockoutReset: ", e);
                    FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    Slog.w(FingerprintService.TAG, "Failed to invoke onLockoutReset: ", e2);
                    releaseWakelock();
                }
            }
        }

        public void binderDied() {
            Slog.e(FingerprintService.TAG, "Lockout reset callback binder died");
            FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
        }

        /* access modifiers changed from: private */
        public void releaseWakelock() {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    private final class FingerprintServiceWrapper extends IFingerprintService.Stub {
        private FingerprintServiceWrapper() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (!FingerprintService.this.isHwTransactInterest(code)) {
                return FingerprintService.super.onTransact(code, data, reply, flags);
            }
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            return FingerprintService.this.onHwTransact(code, data, reply, flags);
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
            String str;
            int i;
            int enrolled;
            int i2 = userId;
            Flog.i(1303, "FingerprintService enroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            int limit = FingerprintService.this.mContext.getResources().getInteger(17694789);
            if (FingerprintService.this.mFingerprintUtils.isDualFp()) {
                i = flags;
                str = opPackageName;
                enrolled = FingerprintService.this.getEnrolledFingerprintsEx(str, i == 4096 ? 1 : 0, i2).size();
            } else {
                i = flags;
                str = opPackageName;
                enrolled = FingerprintService.this.getEnrolledFingerprints(i2).size();
            }
            if (enrolled >= limit) {
                Slog.w(FingerprintService.TAG, "Too many fingerprints registered");
                return;
            }
            boolean isPrivacyUser = FingerprintService.this.checkPrivacySpaceEnroll(i2, ActivityManager.getCurrentUser());
            if (FingerprintService.this.isCurrentUserOrProfile(i2) || isPrivacyUser) {
                final IBinder iBinder = token;
                final byte[] bArr = cryptoToken;
                final int i3 = i2;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i4 = i;
                AnonymousClass1 r9 = r0;
                final boolean isRestricted = isRestricted();
                Handler handler = FingerprintService.this.mHandler;
                final String str2 = str;
                AnonymousClass1 r0 = new Runnable() {
                    public void run() {
                        FingerprintService.this.startEnrollment(iBinder, bArr, i3, iFingerprintServiceReceiver, i4, isRestricted, str2);
                    }
                };
                handler.post(r9);
                return;
            }
            Flog.w(1303, "user invalid enroll error");
        }

        private boolean isRestricted() {
            return !FingerprintService.this.hasPermission("android.permission.MANAGE_FINGERPRINT");
        }

        public void cancelEnrollment(final IBinder token) {
            Flog.i(1303, "FingerprintService cancelEnrollment");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.notifyEnrollmentCanceled();
            Flog.i(1303, "cancelEnrollment client uid = " + Binder.getCallingUid() + ", cancelEnrollment client pid = " + Binder.getCallingPid());
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    ClientMonitor client = FingerprintService.this.mCurrentClient;
                    if ((client instanceof EnrollClient) && client.getToken() == token) {
                        client.stop(client.getToken() == token);
                    }
                }
            });
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName, Bundle bundle, IBiometricPromptReceiver dialogReceiver) {
            final int hwGroupId;
            int i = groupId;
            String str = opPackageName;
            Flog.i(1303, "FingerprintService authenticate");
            if (i == 0 || !FingerprintService.this.isClonedProfile(i)) {
                hwGroupId = i;
            } else {
                Log.i(FingerprintService.TAG, "Clone profile authenticate,change userid to 0");
                hwGroupId = 0;
            }
            if (HwDeviceManager.disallowOp(50)) {
                Slog.i(FingerprintService.TAG, "MDM forbid fingerprint authentication");
                FingerprintService.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(FingerprintService.this.mContext, 33686051, 0);
                        toast.getWindowParams().type = 2010;
                        toast.getWindowParams().privateFlags |= 16;
                        toast.show();
                    }
                }, 300);
                return;
            }
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int callingUserId = UserHandle.getCallingUserId();
            boolean restricted = isRestricted();
            FingerprintService.this.setLivenessSwitch(str);
            if (FingerprintService.this.mSupportKids) {
                Slog.i(FingerprintService.TAG, "mSupportKids=" + FingerprintService.this.mSupportKids);
                FingerprintService.this.setKidsFingerprint(i, FingerprintService.this.isKeyguard(str));
            }
            if (!FingerprintService.this.canUseFingerprint(str, true, callingUid, callingPid, callingUserId)) {
                Slog.v(FingerprintService.TAG, "authenticate(): reject " + str);
                return;
            }
            final long j = opId;
            final String str2 = str;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            final int i2 = flags;
            final Bundle bundle2 = bundle;
            final IBiometricPromptReceiver iBiometricPromptReceiver = dialogReceiver;
            final IBinder iBinder = token;
            AnonymousClass4 r13 = r0;
            final int i3 = callingUserId;
            Handler handler = FingerprintService.this.mHandler;
            final boolean z = restricted;
            AnonymousClass4 r0 = new Runnable() {
                public void run() {
                    Slog.i(FingerprintService.TAG, "authenticate run");
                    MetricsLogger.histogram(FingerprintService.this.mContext, "fingerprint_token", j != 0 ? 1 : 0);
                    HashMap<Integer, PerformanceStats> pmap = j == 0 ? FingerprintService.this.mPerformanceMap : FingerprintService.this.mCryptoPerformanceMap;
                    PerformanceStats stats = pmap.get(Integer.valueOf(FingerprintService.this.mCurrentUserId));
                    if (stats == null) {
                        stats = new PerformanceStats();
                        pmap.put(Integer.valueOf(FingerprintService.this.mCurrentUserId), stats);
                    }
                    PerformanceStats unused = FingerprintService.this.mPerformanceStats = stats;
                    FingerprintService.this.notifyAuthenticationStarted(str2, iFingerprintServiceReceiver, i2, hwGroupId, bundle2, iBiometricPromptReceiver);
                    FingerprintService.this.startAuthentication(iBinder, j, i3, hwGroupId, iFingerprintServiceReceiver, i2, z, str2, bundle2, iBiometricPromptReceiver);
                }
            };
            handler.post(r13);
        }

        public void cancelAuthentication(final IBinder token, final String opPackageName) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int callingUserId = UserHandle.getCallingUserId();
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, callingUid, callingPid, callingUserId)) {
                Slog.v(FingerprintService.TAG, "cancelAuthentication(): reject " + opPackageName);
                return;
            }
            Flog.i(1303, "FingerprintService cancelAuthentication client uid = " + callingUid + ", cancelAuthentication client pid = " + callingPid + " callingUserId = " + callingUserId);
            if (!(opPackageName == null || FingerprintService.this.mPendingClient == null || !opPackageName.equals(FingerprintService.this.mPendingClient.getOwnerString()))) {
                FingerprintService.this.mHandler.removeCallbacks(FingerprintService.this.mResetClientState);
            }
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.notifyAuthenticationCanceled(opPackageName);
                    ClientMonitor client = FingerprintService.this.mCurrentClient;
                    if (client instanceof AuthenticationClient) {
                        if (client.getToken() == token) {
                            Slog.v(FingerprintService.TAG, "stop client " + client.getOwnerString());
                            client.stop(client.getToken() == token);
                            return;
                        }
                        Slog.v(FingerprintService.TAG, "can't stop client " + client.getOwnerString() + " since tokens don't match");
                    } else if (client != null) {
                        Slog.v(FingerprintService.TAG, "can't cancel non-authenticating client " + client.getOwnerString());
                    }
                }
            });
        }

        public void setActiveUser(final int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.updateActiveGroup(userId, null);
                }
            });
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            Flog.i(1303, "FingerprintService remove");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            boolean restricted = isRestricted();
            Handler handler = FingerprintService.this.mHandler;
            final IBinder iBinder = token;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            final boolean z = restricted;
            AnonymousClass7 r0 = new Runnable() {
                public void run() {
                    FingerprintService.this.startRemove(iBinder, i, i2, i3, iFingerprintServiceReceiver, z, false);
                }
            };
            handler.post(r0);
        }

        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            boolean restricted = isRestricted();
            Handler handler = FingerprintService.this.mHandler;
            final IBinder iBinder = token;
            final int i = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            final boolean z = restricted;
            AnonymousClass8 r2 = new Runnable() {
                public void run() {
                    FingerprintService.this.startEnumerate(iBinder, i, iFingerprintServiceReceiver, z, false);
                }
            };
            handler.post(r2);
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), true)) {
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
            Flog.i(1303, "FingerprintService rename");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            boolean isPrivacyUser = FingerprintService.this.checkPrivacySpaceEnroll(groupId, ActivityManager.getCurrentUser());
            if (FingerprintService.this.isCurrentUserOrProfile(groupId) || isPrivacyUser) {
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.mFingerprintUtils.renameFingerprintForUser(FingerprintService.this.mContext, fingerId, groupId, name);
                    }
                });
            } else {
                Flog.w(1303, "user invalid rename error");
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService getEnrolledFingerprints");
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return Collections.emptyList();
            }
            if (!FingerprintService.this.mFingerprintUtils.isDualFp()) {
                return FingerprintService.this.getEnrolledFingerprints(userId);
            }
            Slog.d(FingerprintService.TAG, "dualFingerprint getEnrolledFingerprints and userId is: " + userId);
            return FingerprintService.this.getEnrolledFingerprintsEx(opPackageName, -1, userId);
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService hasEnrolledFingerprints");
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            if (!FingerprintService.this.mFingerprintUtils.isDualFp()) {
                return FingerprintService.this.hasEnrolledFingerprints(userId);
            }
            Slog.d(FingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints and userId is: " + userId);
            if (FingerprintService.this.getEnrolledFingerprintsEx(opPackageName, -1, userId).size() > 0) {
                z = true;
            }
            boolean hasEnrollFingerprints = z;
            Slog.d(FingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints: " + hasEnrollFingerprints);
            return hasEnrollFingerprints;
        }

        public long getAuthenticatorId(String opPackageName) {
            Flog.i(1303, "FingerprintService getAuthenticatorId");
            return FingerprintService.this.getAuthenticatorId(opPackageName);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(FingerprintService.this.mContext, FingerprintService.TAG, pw)) {
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

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService.this.mHandler.post(FingerprintService.this.mResetFailedAttemptsForCurrentUserRunnable);
        }

        public void addLockoutResetCallback(final IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
            if (callback == null) {
                Log.e(FingerprintService.TAG, " FingerprintServiceLockoutResetCallback is null, cannot addLockoutResetMonitor, return");
            } else {
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.addLockoutResetMonitor(new FingerprintServiceLockoutResetMonitor(callback));
                    }
                });
            }
        }

        public boolean isClientActive() {
            boolean z;
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            synchronized (FingerprintService.this) {
                if (FingerprintService.this.mCurrentClient == null) {
                    if (FingerprintService.this.mPendingClient == null) {
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
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            if (FingerprintService.this.mHwCust != null && FingerprintService.this.mHwCust.isAtt()) {
                return FingerprintService.this.mHwCust.getRemainingNum(FingerprintService.this.mHwFailedAttempts, FingerprintService.this.mContext);
            }
            Slog.d(FingerprintService.TAG, " Remaining Num Attempts = " + (5 - FingerprintService.this.mHwFailedAttempts));
            return 5 - FingerprintService.this.mHwFailedAttempts;
        }

        public long getRemainingTime() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            long now = SystemClock.elapsedRealtime();
            long nowToLockout = now - FingerprintService.this.mLockoutTime;
            Slog.d(FingerprintService.TAG, "Remaining Time mLockoutTime = " + FingerprintService.this.mLockoutTime + "  now = " + now);
            if (nowToLockout <= 0 || nowToLockout >= 30000) {
                return 0;
            }
            return 30000 - nowToLockout;
        }
    }

    private class PerformanceStats {
        int accept;
        int acquire;
        int lockout;
        int permanentLockout;
        int reject;

        private PerformanceStats() {
        }
    }

    private class UserFingerprint {
        Fingerprint f;
        int userId;

        public UserFingerprint(Fingerprint f2, int userId2) {
            this.f = f2;
            this.userId = userId2;
        }
    }

    public FingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039825)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FINGERPRINT_LOCKOUT", null);
        this.mUserManager = UserManager.get(this.mContext);
        this.mTimedLockoutCleared = new SparseBooleanArray();
        this.mFailedAttempts = new SparseIntArray();
        this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        this.mActivityManager = ActivityManager.getService();
        this.fpDataCollector = FingerprintDataInterface.getInstance();
        ServiceThread fingerprintThread = new ServiceThread("fingerprintServcie", -8, false);
        fingerprintThread.start();
        this.mHandler = new Handler(fingerprintThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what != 10) {
                    Slog.w(FingerprintService.TAG, "Unknown message:" + msg.what);
                    return;
                }
                Slog.i(FingerprintService.TAG, "MSG_USER_SWITCHING");
                FingerprintService.this.handleUserSwitching(msg.arg1);
            }
        };
    }

    public void serviceDied(long cookie) {
        Slog.v(TAG, "fingerprint HAL died");
        MetricsLogger.count(this.mContext, "fingerprintd_died", 1);
        handleError(this.mHalDeviceId, 1, 0);
    }

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
            if (this.mHalDeviceId == 0) {
                if (!FingerprintUtils.getInstance().isDualFp() || this.mUDHalDeviceId == 0) {
                    Slog.w(TAG, "Failed to open Fingerprint HAL!");
                    MetricsLogger.count(this.mContext, "fingerprintd_openhal_error", 1);
                    this.mDaemon.asBinder().unlinkToDeath(this);
                    this.mDaemon = null;
                }
            }
            loadAuthenticatorIds();
            updateActiveGroup(ActivityManager.getCurrentUser(), null);
            doFingerprintCleanupForUser(ActivityManager.getCurrentUser());
        }
        return this.mDaemon;
    }

    /* access modifiers changed from: protected */
    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    public long getUdHalDeviceId() {
        return this.mUDHalDeviceId;
    }

    private void loadAuthenticatorIds() {
        long t = System.currentTimeMillis();
        this.mAuthenticatorIds.clear();
        for (UserInfo user : UserManager.get(this.mContext).getUsers(true)) {
            int userId = getUserOrWorkProfileId(null, user.id);
            if (!this.mAuthenticatorIds.containsKey(Integer.valueOf(userId))) {
                updateActiveGroup(userId, null);
            }
        }
        long t2 = System.currentTimeMillis() - t;
        if (t2 > 1000) {
            Slog.w(TAG, "loadAuthenticatorIds() taking too long: " + t2 + "ms");
        }
    }

    private void doFingerprintCleanupForUser(int userId) {
        enumerateUser(userId);
    }

    private void clearEnumerateState() {
        Slog.v(TAG, "clearEnumerateState()");
        this.mUnknownFingerprints.clear();
    }

    private void enumerateUser(int userId) {
        Slog.v(TAG, "Enumerating user(" + userId + ")");
        startEnumerate(this.mToken, userId, null, hasPermission("android.permission.MANAGE_FINGERPRINT") ^ true, true);
    }

    private void cleanupUnknownFingerprints() {
        if (!this.mUnknownFingerprints.isEmpty()) {
            UserFingerprint uf = this.mUnknownFingerprints.get(0);
            this.mUnknownFingerprints.remove(uf);
            startRemove(this.mToken, uf.f.getFingerId(), uf.f.getGroupId(), uf.userId, null, !hasPermission("android.permission.MANAGE_FINGERPRINT"), true);
            return;
        }
        clearEnumerateState();
    }

    /* access modifiers changed from: protected */
    public void handleEnumerate(long deviceId, int fingerId, int groupId, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        if ((client instanceof InternalRemovalClient) || (client instanceof EnumerateClient)) {
            client.onEnumerationResult(fingerId, groupId, remaining);
            if (remaining == 0) {
                if (client instanceof InternalEnumerateClient) {
                    List<Fingerprint> unknownFingerprints = ((InternalEnumerateClient) client).getUnknownFingerprints();
                    if (!unknownFingerprints.isEmpty()) {
                        Slog.w(TAG, "Adding " + unknownFingerprints.size() + " fingerprints for deletion");
                    }
                    for (Fingerprint f : unknownFingerprints) {
                        this.mUnknownFingerprints.add(new UserFingerprint(f, client.getTargetUserId()));
                    }
                    removeClient(client);
                    cleanupUnknownFingerprints();
                } else {
                    removeClient(client);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleError(long deviceId, int error, int vendorCode) {
        ClientMonitor client = this.mCurrentClient;
        if (client instanceof EnrollClient) {
            notifyEnrollmentCanceled();
        }
        if (error == 8 && vendorCode > BASE_BRIGHTNESS) {
            int vendorCode2 = vendorCode - 3000;
            int i = 255;
            if (vendorCode2 < 255) {
                i = vendorCode2;
            }
            vendorCode = i;
            Slog.w(TAG, "change brightness to " + vendorCode);
            notifyFingerCalibrarion(vendorCode);
        }
        if ((client instanceof InternalRemovalClient) || (client instanceof InternalEnumerateClient)) {
            clearEnumerateState();
        }
        if (client != null && client.onError(error, vendorCode)) {
            removeClient(client);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("handleError(client=");
        sb.append(client != null ? client.getOwnerString() : "null");
        sb.append(", error = ");
        sb.append(error);
        sb.append(")");
        Slog.v(TAG, sb.toString());
        if (error == 5) {
            this.mHandler.removeCallbacks(this.mResetClientState);
            if (this.mPendingClient != null) {
                Slog.v(TAG, "start pending client " + this.mPendingClient.getOwnerString());
                startClient(this.mPendingClient, false);
                this.mPendingClient = null;
            }
        } else if (error == 1) {
            Slog.w(TAG, "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
            synchronized (this) {
                if (this.mDaemon != null) {
                    this.mDaemon.asBinder().unlinkToDeath(this);
                }
                this.mDaemon = null;
                this.mHalDeviceId = 0;
                this.mCurrentUserId = -10000;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleRemoved(long deviceId, int fingerId, int groupId, int remaining) {
        Slog.w(TAG, "Removed: fid=" + fingerId + ", gid=" + groupId + ", dev=" + deviceId + ", rem=" + remaining);
        ClientMonitor client = this.mCurrentClient;
        int groupId2 = getRealUserIdForApp(groupId);
        if (client != null && client.onRemoved(fingerId, groupId2, remaining)) {
            if (this.mFingerprintUtils.isDualFp() && (client instanceof RemovalClient)) {
                RemovalClient removeClient = (RemovalClient) client;
                boolean hasUDFingerprints = true;
                boolean hasFingerprints = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, groupId2, -1).size() > 0;
                if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, groupId2, 1).size() <= 0) {
                    hasUDFingerprints = false;
                }
                if (!hasUDFingerprints) {
                    sendCommandToHal(0);
                    Slog.d(TAG, "UDFingerprint all removed so TP CLOSE");
                }
                if (removeClient.getFingerId() == 0 && hasFingerprints) {
                    Slog.d(TAG, "dualFingerprint-> handleRemoved, but do not destory client.");
                    return;
                }
            }
            removeClient(client);
            if (!hasEnrolledFingerprints(groupId2)) {
                updateActiveGroup(groupId2, null);
            }
        }
        if ((client instanceof InternalRemovalClient) && !this.mUnknownFingerprints.isEmpty()) {
            cleanupUnknownFingerprints();
        } else if (client instanceof InternalRemovalClient) {
            clearEnumerateState();
        }
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        boolean authenticated = fingerId != 0;
        if (isPowerFpAbandonAuthenticated()) {
            Slog.i(TAG, "discard onAuthenticated:" + authenticated);
            return;
        }
        if (this.fpDataCollector != null) {
            this.fpDataCollector.reportFingerprintAuthenticated(authenticated);
        }
        this.auTime = System.currentTimeMillis();
        ClientMonitor client = this.mCurrentClient;
        int groupId2 = getRealUserIdForApp(groupId);
        if (fingerId != 0) {
            byte[] byteToken = new byte[token.size()];
            for (int i = 0; i < token.size(); i++) {
                byteToken[i] = token.get(i).byteValue();
            }
            KeyStore.getInstance().addAuthToken(byteToken);
        }
        if (client != null && client.onAuthenticated(fingerId, groupId2)) {
            if ((((AuthenticationClient) client).mFlags & 4096) != 0) {
                Slog.w(TAG, "AuthenticationClient remvoe with waitup");
                this.mWaitupClient = client;
            }
            if (isSupportPowerFp()) {
                Slog.i(TAG, "power finger client.stop");
                client.stop(true);
            }
            removeClient(client);
        }
        if (this.mPerformanceStats == null) {
            Slog.w(TAG, "mPerformanceStats is null");
            return;
        }
        if (fingerId != 0) {
            this.mPerformanceStats.accept++;
        } else {
            this.mPerformanceStats.reject++;
        }
    }

    /* access modifiers changed from: protected */
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        int clientCode;
        stopPickupTrunOff();
        if (this.mWaitupClient != null) {
            Slog.w(TAG, "handleAcquired for waitClient AC:" + acquiredInfo + " VC:" + vendorCode + " CC:" + clientCode);
            if (clientCode == HUAWEI_FINGERPRINT_UP) {
                this.mWaitupClient = null;
                Slog.w(TAG, "wait clint ActionUp.");
                this.mContext.sendBroadcastAsUser(new Intent(ACTION_AUTH_FINGER_UP), new UserHandle(0), PERM_AUTH_FINGER_UP);
            }
        }
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onAcquired(acquiredInfo, vendorCode)) {
            removeClient(client);
        }
        if (this.mPerformanceStats != null && getLockoutMode() == 0 && (client instanceof AuthenticationClient)) {
            this.mPerformanceStats.acquire++;
        }
    }

    /* access modifiers changed from: protected */
    public void handleEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        int groupId2 = getRealUserIdForApp(groupId);
        if (client == null || !client.onEnrollResult(fingerId, groupId2, remaining)) {
            notifyEnrollingFingerUp();
            Slog.w(TAG, "no eroll client, remove erolled fingerprint");
            if (remaining == 0) {
                IBiometricsFingerprint daemon = getFingerprintDaemon();
                if (daemon != null) {
                    try {
                        daemon.remove(fingerId, ActivityManager.getCurrentUser());
                    } catch (RemoteException e) {
                    }
                }
            }
        } else {
            removeClient(client);
            notifyEnrollmentCanceled();
            updateActiveGroup(groupId2, null);
        }
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
        Slog.w(TAG, "getRealUserIdForApp error return 0");
        return 0;
    }

    /* access modifiers changed from: private */
    public void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
    }

    /* access modifiers changed from: package-private */
    public void handleUserSwitching(int userId) {
        if ((this.mCurrentClient instanceof InternalRemovalClient) || (this.mCurrentClient instanceof InternalEnumerateClient)) {
            Slog.w(TAG, "User switched while performing cleanup");
            removeClient(this.mCurrentClient);
            clearEnumerateState();
        }
        updateActiveGroup(userId, null);
        doFingerprintCleanupForUser(userId);
    }

    private void removeClient(ClientMonitor client) {
        String owner = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if (client != null) {
            owner = client.getOwnerString();
            client.destroy();
            if (!(client == this.mCurrentClient || this.mCurrentClient == null)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unexpected client: ");
                sb.append(client.getOwnerString());
                sb.append("expected: ");
                sb.append(this.mCurrentClient);
                Slog.w(TAG, sb.toString() != null ? this.mCurrentClient.getOwnerString() : "null");
            }
        }
        if (this.mCurrentClient != null) {
            Slog.v(TAG, "Done with client: " + client.getOwnerString());
            if (!HWUI_PACKAGE_NAME.equals(owner) && !DOCOMO_PACKAGE_NAME.equals(owner)) {
                notifyAuthenticationCanceled(owner);
            }
            this.mCurrentClient = null;
        }
        if (this.mPendingClient == null) {
            notifyClientActiveCallbacks(false);
        }
    }

    /* access modifiers changed from: private */
    public boolean inLockoutMode() {
        boolean z = false;
        int failedAttempts = this.mFailedAttempts.get(ActivityManager.getCurrentUser(), 0);
        if (this.mHwCust != null && this.mHwCust.isAtt()) {
            return this.mHwCust.inLockoutMode(failedAttempts, this.mContext);
        }
        if (failedAttempts >= 5) {
            z = true;
        }
        return z;
    }

    private int getMaxFailedAttempsOfPermanentLockout() {
        return isSupportPowerFp() ? 50 : 20;
    }

    /* access modifiers changed from: protected */
    public int getLockoutMode() {
        int currentUser = ActivityManager.getCurrentUser();
        int failedAttempts = this.mFailedAttempts.get(currentUser, 0);
        if (failedAttempts >= getMaxFailedAttempsOfPermanentLockout()) {
            return 2;
        }
        if (failedAttempts > 0 && !this.mTimedLockoutCleared.get(currentUser, false)) {
            if (this.mHwCust == null || !this.mHwCust.isAtt()) {
                if (failedAttempts % 5 == 0) {
                    return 1;
                }
            } else if (this.mHwCust.isLockoutMode(failedAttempts, this.mContext)) {
                return 1;
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void scheduleLockoutResetForUser(int userId) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 30000, getLockoutResetIntentForUser(userId));
    }

    private void cancelLockoutResetForUser(int userId) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.cancel(getLockoutResetIntentForUser(userId));
    }

    private PendingIntent getLockoutResetIntentForUser(int userId) {
        return PendingIntent.getBroadcast(this.mContext, userId, new Intent(ACTION_LOCKOUT_RESET).putExtra(KEY_LOCKOUT_RESET_USER, userId), HW_FP_AUTH_UD);
    }

    /* access modifiers changed from: protected */
    public void handleHwFailedAttempt(int flags, String packagesName) {
        if ((16777216 & flags) == 0 || !SETTINGS_PACKAGE_NAME.equals(packagesName)) {
            this.mHwFailedAttempts++;
        } else {
            Slog.i(TAG, "no need count hw failed attempts");
        }
    }

    public long startPreEnroll(IBinder token) {
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

    public int startPostEnroll(IBinder token) {
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
    public void setLivenessSwitch(String opPackageName2) {
        Slog.w(TAG, "father class call setLivenessSwitch");
    }

    /* access modifiers changed from: private */
    public void startClient(ClientMonitor newClient, boolean initiatedByClient) {
        ClientMonitor currentClient = this.mCurrentClient;
        this.mHandler.removeCallbacks(this.mResetClientState);
        if (currentClient != null) {
            Slog.v(TAG, "request stop current client " + currentClient.getOwnerString());
            if (!(currentClient instanceof InternalEnumerateClient) && !(currentClient instanceof InternalRemovalClient)) {
                currentClient.stop(initiatedByClient);
                if (this.mPendingClient != null) {
                    this.mPendingClient.destroy();
                }
            } else if (newClient != null) {
                Slog.w(TAG, "Internal cleanup in progress but trying to start client " + newClient.getClass().getSuperclass().getSimpleName() + "(" + newClient.getOwnerString() + "), initiatedByClient = " + initiatedByClient);
            }
            this.mPendingClient = newClient;
            this.mHandler.removeCallbacks(this.mResetClientState);
            this.mHandler.postDelayed(this.mResetClientState, CANCEL_TIMEOUT_LIMIT);
        } else if (newClient != null) {
            this.mCurrentClient = newClient;
            Slog.v(TAG, "starting client " + newClient.getClass().getSuperclass().getSimpleName() + "(" + newClient.getOwnerString() + "), initiatedByClient = " + initiatedByClient);
            notifyClientActiveCallbacks(true);
            newClient.start();
        }
    }

    /* access modifiers changed from: package-private */
    public void startRemove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, boolean restricted, boolean internal) {
        int i;
        int i2 = fingerId;
        if (token == null) {
            Slog.w(TAG, "startRemove: token is null");
        } else if (receiver == null) {
            Slog.w(TAG, "startRemove: receiver is null");
        } else if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startRemove: no fingerprint HAL!");
        } else {
            if (!this.mFingerprintUtils.isDualFp() || i2 == 0) {
                i = userId;
            } else {
                i = userId;
                List<Fingerprint> finerprints = FingerprintUtils.getInstance().getFingerprintsForUser(getContext(), i, 1);
                int fingerprintSize = finerprints.size();
                int i3 = 0;
                while (true) {
                    if (i3 >= fingerprintSize) {
                        break;
                    } else if (finerprints.get(i3).getFingerId() == i2) {
                        Slog.d(TAG, "dualFingerprint send MSG_REMOVE_UD");
                        sendCommandToHal(104);
                        break;
                    } else {
                        i3++;
                    }
                }
            }
            if (this.mFingerprintUtils.isDualFp() && i2 == 0) {
                Slog.d(TAG, "dualFingerprint send MSG_REMOVE_ALL");
                sendCommandToHal(107);
            }
            if (internal) {
                Context context = getContext();
                Context context2 = context;
                AnonymousClass6 r0 = new InternalRemovalClient(this, context, this.mHalDeviceId, token, receiver, i2, groupId, i, restricted, context.getOpPackageName()) {
                    final /* synthetic */ FingerprintService this$0;

                    {
                        this.this$0 = this$0;
                    }

                    public void notifyUserActivity() {
                    }

                    public IBiometricsFingerprint getFingerprintDaemon() {
                        return this.this$0.getFingerprintDaemon();
                    }
                };
                startClient(r0, true);
            } else if (token == null) {
                Slog.e(TAG, "startRemove error: token null!");
            } else {
                AnonymousClass7 r02 = new RemovalClient(this, getContext(), this.mHalDeviceId, token, receiver, fingerId, groupId, userId, restricted, token.toString()) {
                    final /* synthetic */ FingerprintService this$0;

                    {
                        this.this$0 = this$0;
                    }

                    public void notifyUserActivity() {
                        this.this$0.userActivity();
                    }

                    public IBiometricsFingerprint getFingerprintDaemon() {
                        return this.this$0.getFingerprintDaemon();
                    }
                };
                startClient(r02, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startEnumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver, boolean restricted, boolean internal) {
        if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startEnumerate: no fingerprint HAL!");
            return;
        }
        if (internal) {
            int i = userId;
            List<Fingerprint> enrolledList = getEnrolledFingerprints(i);
            Context context = getContext();
            Context context2 = context;
            AnonymousClass8 r0 = new InternalEnumerateClient(this, context, this.mHalDeviceId, token, receiver, i, i, restricted, context.getOpPackageName(), enrolledList) {
                final /* synthetic */ FingerprintService this$0;

                {
                    this.this$0 = this$0;
                }

                public void notifyUserActivity() {
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return this.this$0.getFingerprintDaemon();
                }
            };
            startClient(r0, true);
        } else if (token == null) {
            Slog.e(TAG, "startEnumerate error: token null!");
        } else {
            AnonymousClass9 r02 = new EnumerateClient(this, getContext(), this.mHalDeviceId, token, receiver, userId, userId, restricted, token.toString()) {
                final /* synthetic */ FingerprintService this$0;

                {
                    this.this$0 = this$0;
                }

                public void notifyUserActivity() {
                    this.this$0.userActivity();
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return this.this$0.getFingerprintDaemon();
                }
            };
            startClient(r02, true);
        }
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId);
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        if (userId != 0 && isClonedProfile(userId)) {
            Log.i(TAG, "Clone profile get Enrolled Fingerprints,change userid to 0");
            userId = 0;
        }
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0;
    }

    /* access modifiers changed from: package-private */
    public void checkPermission(String permission) {
        Context context = getContext();
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    /* access modifiers changed from: package-private */
    public int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            int userId2 = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId2;
        }
        Slog.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public boolean isCurrentUserOrProfile(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um == null) {
            Slog.e(TAG, "Unable to acquire UserManager");
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
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean canUseFingerprint(String opPackageName2, boolean requireForeground, int uid, int pid, int userId) {
        return canUseFingerprint(opPackageName2, requireForeground, uid, pid, userId, false);
    }

    /* access modifiers changed from: private */
    public boolean canUseFingerprint(String opPackageName2, boolean requireForeground, int uid, int pid, int userId, boolean isDetected) {
        if (opPackageName2 == null || opPackageName2.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
            Slog.i(TAG, "opPackageName is null or opPackageName is invalid");
            return false;
        }
        if (getContext().checkCallingPermission("android.permission.USE_FINGERPRINT") != 0) {
            checkPermission("android.permission.USE_BIOMETRIC");
        }
        this.opPackageName = opPackageName2;
        if (opPackageName2 != null && (opPackageName2.equals("com.huawei.hwasm") || opPackageName2.equals("com.huawei.securitymgr") || opPackageName2.equals("com.huawei.aod") || ((isDetected && opPackageName2.equals("com.tencent.mm")) || isKeyguard(opPackageName2)))) {
            return true;
        }
        if (!isCurrentUserOrProfile(userId)) {
            Slog.w(TAG, "Rejecting " + opPackageName2 + " ; not a current user or profile");
            return false;
        }
        try {
            if (this.mAppOps.noteOp(55, uid, opPackageName2) != 0) {
                Slog.w(TAG, "Rejecting " + opPackageName2 + " ; permission denied");
                return false;
            } else if (!requireForeground || isForegroundActivity(uid, pid) || currentClient(opPackageName2)) {
                return true;
            } else {
                Slog.w(TAG, "Rejecting " + opPackageName2 + " ; not in foreground");
                return false;
            }
        } catch (SecurityException e) {
            Slog.w(TAG, "AppOpsManager noteOp error:" + e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean currentClient(String opPackageName2) {
        return this.mCurrentClient != null && this.mCurrentClient.getOwnerString().equals(opPackageName2);
    }

    /* access modifiers changed from: private */
    public boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    /* access modifiers changed from: private */
    public void addLockoutResetMonitor(FingerprintServiceLockoutResetMonitor monitor) {
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    /* access modifiers changed from: private */
    public void removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }

    private void notifyLockoutResetMonitors() {
        for (int i = 0; i < this.mLockoutMonitors.size(); i++) {
            this.mLockoutMonitors.get(i).sendLockoutReset();
        }
    }

    private void notifyClientActiveCallbacks(boolean isActive) {
        List<IFingerprintClientActiveCallback> callbacks = this.mClientActiveCallbacks;
        for (int i = 0; i < callbacks.size(); i++) {
            try {
                callbacks.get(i).onClientActiveChanged(isActive);
            } catch (RemoteException e) {
                this.mClientActiveCallbacks.remove(callbacks.get(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName2, Bundle bundle, IBiometricPromptReceiver dialogReceiver) {
        String str;
        FingerprintService fingerprintService;
        int errorCode;
        int i = flags;
        String str2 = opPackageName2;
        int newGroupId = groupId;
        updateActiveGroup(groupId, str2);
        Slog.v(TAG, "startAuthentication(" + str2 + ")");
        if (shouldAuthBothSpaceFingerprints(str2, i)) {
            Slog.i(TAG, "should authenticate both space fingerprints");
            newGroupId = SPECIAL_USER_ID;
        }
        final String str3 = opPackageName2;
        AnonymousClass10 r0 = new AuthenticationClient(this, getContext(), this.mHalDeviceId, token, receiver, this.mCurrentUserId, newGroupId, opId, restricted, str2, i, bundle, dialogReceiver, this.mStatusBarService) {
            final /* synthetic */ FingerprintService this$0;

            {
                this.this$0 = this$0;
            }

            public boolean onAuthenticated(int fingerId, int groupId) {
                IFingerprintServiceReceiver receiver = getReceiver();
                boolean authenticated = fingerId != 0;
                if (receiver != null) {
                    if (!authenticated) {
                        Log.e(FingerprintService.TAG, "onAuthenticated,fail ,mHwFailedAttempts = " + this.this$0.mHwFailedAttempts);
                        this.this$0.notifyAuthenticationFinished(str3, 1, this.this$0.mHwFailedAttempts + 1);
                        if (this.this$0.auTime - this.this$0.downTime > 0) {
                            Context access$700 = this.this$0.mContext;
                            Flog.bdReport(access$700, 7, "{CostTime:" + (this.this$0.auTime - this.this$0.downTime) + ",pkg:" + str3 + ",DeviceType:" + this.this$0.mCurrentAuthFpDev + "}");
                            Log.i(FingerprintService.TAG, "onAuthenticated fail:{CostTime:" + (this.this$0.auTime - this.this$0.downTime) + ",pkg:" + str3 + ",DeviceType:" + this.this$0.mCurrentAuthFpDev + "}");
                        } else {
                            Log.i(FingerprintService.TAG, "Fingerprint authenticate time less than equal to or equal to Fingerprint down time");
                        }
                    } else {
                        Log.e(FingerprintService.TAG, "onAuthenticated, pass");
                        this.this$0.notifyAuthenticationFinished(str3, 0, 0);
                        Context access$7002 = this.this$0.mContext;
                        Flog.bdReport(access$7002, 8, "{pkg:" + str3 + ",ErrorCount:" + this.this$0.mHwFailedAttempts + ",DeviceType:" + this.this$0.mCurrentAuthFpDev + "}");
                        Log.i(FingerprintService.TAG, "onAuthenticated success:{pkg:" + str3 + ",ErrorCount:" + this.this$0.mHwFailedAttempts + ",DeviceType:" + this.this$0.mCurrentAuthFpDev + "}");
                    }
                }
                return super.onAuthenticated(fingerId, groupId);
            }

            public void onStart() {
                try {
                    this.this$0.mActivityManager.registerTaskStackListener(this.this$0.mTaskStackListener);
                } catch (RemoteException e) {
                    Slog.e(FingerprintService.TAG, "Could not register task stack listener", e);
                }
            }

            public void onStop() {
                try {
                    this.this$0.mActivityManager.unregisterTaskStackListener(this.this$0.mTaskStackListener);
                } catch (RemoteException e) {
                    Slog.e(FingerprintService.TAG, "Could not unregister task stack listener", e);
                }
            }

            public int handleFailedAttempt() {
                boolean noNeedAddFailedAttemps = false;
                if ((this.mFlags & 16777216) != 0 && FingerprintService.SETTINGS_PACKAGE_NAME.equals(getOwnerString())) {
                    noNeedAddFailedAttemps = true;
                    Slog.i(FingerprintService.TAG, "no need count failed attempts");
                }
                int currentUser = ActivityManager.getCurrentUser();
                if (!noNeedAddFailedAttemps) {
                    this.this$0.mFailedAttempts.put(currentUser, this.this$0.mFailedAttempts.get(currentUser, 0) + 1);
                }
                this.this$0.mTimedLockoutCleared.put(ActivityManager.getCurrentUser(), false);
                int lockoutMode = this.this$0.getLockoutMode();
                if (lockoutMode == 2) {
                    this.this$0.mPerformanceStats.permanentLockout++;
                } else if (lockoutMode == 1) {
                    this.this$0.mPerformanceStats.lockout++;
                }
                if (lockoutMode == 0) {
                    return 0;
                }
                this.this$0.mLockoutTime = SystemClock.elapsedRealtime();
                this.this$0.scheduleLockoutResetForUser(currentUser);
                if (this.this$0.isKeyguard(str3)) {
                    return 0;
                }
                return lockoutMode;
            }

            public void resetFailedAttempts() {
                if (inLockoutMode()) {
                    Slog.v(FingerprintService.TAG, "resetFailedAttempts should be called from APP");
                } else {
                    this.this$0.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
                }
            }

            public void notifyUserActivity() {
                this.this$0.userActivity();
            }

            public IBiometricsFingerprint getFingerprintDaemon() {
                return this.this$0.getFingerprintDaemon();
            }

            public void handleHwFailedAttempt(int flags, String packagesName) {
                this.this$0.handleHwFailedAttempt(flags, packagesName);
            }

            public boolean inLockoutMode() {
                return this.this$0.inLockoutMode();
            }
        };
        int lockoutMode = getLockoutMode();
        if (lockoutMode != 0) {
            fingerprintService = this;
            str = opPackageName2;
            if (!fingerprintService.isKeyguard(str)) {
                Slog.v(TAG, "In lockout mode(" + lockoutMode + ") ; disallowing authentication");
                if (lockoutMode == 1) {
                    errorCode = 7;
                } else {
                    errorCode = 9;
                }
                if (!r0.onError(errorCode, 0)) {
                    Slog.w(TAG, "Cannot send permanent lockout message to client");
                }
                return;
            }
        } else {
            fingerprintService = this;
            str = opPackageName2;
        }
        if (fingerprintService.mFingerprintUtils.isDualFp()) {
            StringBuilder sb = new StringBuilder();
            sb.append("dualFingerprint startAuthentication and flag is: ");
            int i2 = flags;
            sb.append(i2);
            Slog.d(TAG, sb.toString());
            if (i2 == 0) {
                if (fingerprintService.canUseUdFingerprint(str)) {
                    Slog.d(TAG, "dualFingerprint send MSG_AUTH_ALL");
                    fingerprintService.sendCommandToHal(103);
                }
            } else if ((HW_FP_AUTH_UD & i2) != 0) {
                if ((HW_FP_AUTH_UG & i2) != 0) {
                    Slog.d(TAG, "dualFingerprint send MSG_AUTH_ALL");
                    fingerprintService.sendCommandToHal(103);
                } else {
                    Slog.d(TAG, "dualFingerprint send MSG_AUTH_UD");
                    fingerprintService.sendCommandToHal(102);
                }
            }
        } else {
            int i3 = flags;
        }
        fingerprintService.startClient(r0, true);
    }

    /* access modifiers changed from: private */
    public void startEnrollment(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName2) {
        int i = flags;
        String str = opPackageName2;
        int groupId = userId;
        updateActiveGroup(groupId, str);
        AnonymousClass11 r0 = new EnrollClient(this, getContext(), this.mHalDeviceId, token, receiver, groupId, groupId, cryptoToken, restricted, str) {
            final /* synthetic */ FingerprintService this$0;

            {
                this.this$0 = this$0;
            }

            public IBiometricsFingerprint getFingerprintDaemon() {
                return this.this$0.getFingerprintDaemon();
            }

            public void notifyUserActivity() {
                this.this$0.userActivity();
            }
        };
        if (this.mFingerprintUtils.isDualFp() && SETTINGS_PACKAGE_NAME.equals(str)) {
            int targetDevice = i == 4096 ? 1 : 0;
            Slog.d(TAG, "dualFingerprint enroll targetDevice is: " + targetDevice);
            if (targetDevice == 1) {
                Slog.d(TAG, "dualFingerprint send MSG_ENROLL_UD");
                sendCommandToHal(101);
                r0.setTargetDevice(1);
            }
        }
        notifyEnrollmentStarted(i);
        startClient(r0, true);
    }

    /* access modifiers changed from: protected */
    public void resetFailedAttemptsForUser(boolean clearAttemptCounter, int userId) {
        if (getLockoutMode() != 0) {
            Slog.v(TAG, "Reset fingerprint lockout, clearAttemptCounter=" + clearAttemptCounter);
        }
        if (clearAttemptCounter) {
            this.mFailedAttempts.put(userId, 0);
        }
        this.mLockoutTime = 0;
        this.mHwFailedAttempts = 0;
        this.mTimedLockoutCleared.put(userId, true);
        cancelLockoutResetForUser(userId);
        notifyLockoutResetMonitors();
    }

    /* access modifiers changed from: private */
    public boolean isHwTransactInterest(int code) {
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE || code == CODE_SET_CALIBRATE_MODE_RULE || code == CODE_GET_TOKEN_LEN_RULE || code == CODE_SET_FINGERPRINT_MASK_VIEW || code == CODE_SHOW_FINGERPRINT_VIEW || code == CODE_SHOW_FINGERPRINT_BUTTON || code == 1107 || code == 1110 || code == CODE_GET_HARDWARE_TYPE || code == CODE_NOTIFY_OPTICAL_CAPTURE || code == CODE_SUSPEND_AUTHENTICATE || code == CODE_SET_HOVER_SWITCH || code == CODE_GET_HOVER_SUPPORT || code == CODE_DISABLE_FINGERPRINT_VIEW || code == CODE_ENABLE_FINGERPRINT_VIEW || code == CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION || code == CODE_REMOVE_MASK_AND_SHOW_BUTTON || code == CODE_IS_SUPPORT_DUAL_FINGERPRINT || code == CODE_GET_FINGERPRINT_LIST_ENROLLED || code == CODE_IS_FINGERPRINT_HARDWARE_DETECTED || code == CODE_SEND_UNLOCK_LIGHTBRIGHT || code == CODE_GET_HIGHLIGHT_SPOT_RADIUS || code == CODE_SUSPEND_ENROLL || code == CODE_UDFINGERPRINT_SPOTCOLOR || code == CODE_FINGERPRINT_THREESIDESAUTH || code == CODE_POWER_KEYCODE || code == CODE_IS_WAIT_AUTHEN) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Fingerprint Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
                PerformanceStats stats = this.mPerformanceMap.get(Integer.valueOf(userId));
                PerformanceStats cryptoStats = this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, N);
                int i = 0;
                set.put("accept", stats != null ? stats.accept : 0);
                set.put("reject", stats != null ? stats.reject : 0);
                set.put("acquire", stats != null ? stats.acquire : 0);
                set.put("lockout", stats != null ? stats.lockout : 0);
                set.put("permanentLockout", stats != null ? stats.permanentLockout : 0);
                set.put("acceptCrypto", cryptoStats != null ? cryptoStats.accept : 0);
                set.put("rejectCrypto", cryptoStats != null ? cryptoStats.reject : 0);
                set.put("acquireCrypto", cryptoStats != null ? cryptoStats.acquire : 0);
                set.put("lockoutCrypto", cryptoStats != null ? cryptoStats.lockout : 0);
                if (cryptoStats != null) {
                    i = cryptoStats.permanentLockout;
                }
                set.put("permanentLockoutCrypto", i);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
    }

    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            long userToken = proto.start(2246267895809L);
            proto.write(1120986464257L, userId);
            proto.write(1120986464258L, this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size());
            PerformanceStats normal = this.mPerformanceMap.get(Integer.valueOf(userId));
            if (normal != null) {
                long countsToken = proto.start(1146756268035L);
                proto.write(1120986464257L, normal.accept);
                proto.write(1120986464258L, normal.reject);
                proto.write(1120986464259L, normal.acquire);
                proto.write(1120986464260L, normal.lockout);
                proto.write(1120986464261L, normal.permanentLockout);
                proto.end(countsToken);
            }
            PerformanceStats crypto = this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
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

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.fingerprint.FingerprintService$FingerprintServiceWrapper, android.os.IBinder] */
    public void onStart() {
        publishBinderService("fingerprint", new FingerprintServiceWrapper());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            public final void run() {
                FingerprintService.this.getFingerprintDaemon();
            }
        }, "FingerprintService.onStart");
        listenForUserSwitches();
    }

    public void onBootPhase(int phase) {
        Slog.d(TAG, "Fingerprint daemon is phase :" + phase);
        if (phase == 1000) {
            Slog.d(TAG, "Fingerprint mDaemon is " + this.mDaemon);
            if (getFingerprintDaemon() == null) {
                Slog.w(TAG, "Fingerprint daemon is null");
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateActiveGroup(int userId, String clientPackage) {
        File baseDir;
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                int userId2 = getUserOrWorkProfileId(clientPackage, userId);
                boolean hasFingerprints = false;
                if (userId2 != this.mCurrentUserId) {
                    if (Build.VERSION.FIRST_SDK_INT < 1) {
                        Slog.e(TAG, "First SDK version " + firstSdkInt + " is invalid; must be at least VERSION_CODES.BASE");
                    }
                    int userIdForHal = userId2;
                    UserInfo info = this.mUserManager.getUserInfo(userId2);
                    if (info != null && info.isHwHiddenSpace()) {
                        userIdForHal = HIDDEN_SPACE_ID;
                        Slog.i(TAG, "userIdForHal is " + HIDDEN_SPACE_ID);
                    }
                    Slog.i(TAG, "FIRST_SDK_INT:" + firstSdkInt);
                    if (userIdForHal == HIDDEN_SPACE_ID) {
                        Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                        baseDir = Environment.getFingerprintFileDirectory(0);
                    } else {
                        baseDir = Environment.getFingerprintFileDirectory(userId2);
                    }
                    File fpDir = new File(baseDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath());
                    this.mCurrentUserId = userId2;
                    updateFingerprints(userId2);
                }
                long authenticatorId = 0;
                if (this.mFingerprintUtils.isDualFp()) {
                    if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId2, -1).size() > 0) {
                        hasFingerprints = true;
                    }
                    if (hasFingerprints) {
                        authenticatorId = daemon.getAuthenticatorId();
                    }
                    Slog.d(TAG, "daemon getAuthenticatorId = " + authenticatorId + " userId = " + userId2);
                    this.mAuthenticatorIds.put(Integer.valueOf(userId2), Long.valueOf(authenticatorId));
                } else {
                    Map<Integer, Long> map = this.mAuthenticatorIds;
                    Integer valueOf = Integer.valueOf(userId2);
                    if (hasEnrolledFingerprints(userId2)) {
                        authenticatorId = daemon.getAuthenticatorId();
                    }
                    map.put(valueOf, Long.valueOf(authenticatorId));
                }
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
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = this.mUserManager.getUserInfo(userId);
            return userInfo != null && userInfo.isManagedProfile();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isClonedProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = this.mUserManager.getUserInfo(userId);
            return userInfo != null && userInfo.isClonedProfile();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    Slog.w(FingerprintService.TAG, "onUserSwitching");
                    FingerprintService.this.mHandler.obtainMessage(10, newUserId, 0).sendToTarget();
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    public long getAuthenticatorId(String opPackageName2) {
        return this.mAuthenticatorIds.getOrDefault(Integer.valueOf(getUserOrWorkProfileId(opPackageName2, UserHandle.getCallingUserId())), 0L).longValue();
    }
}
