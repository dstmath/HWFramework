package com.android.server.fingerprint;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.fingerprint.HwFingerprintSets.HwFingerprintGroup;
import com.android.server.policy.PickUpWakeScreenManager;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.huawei.android.os.UserManagerEx;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticator.Stub;
import com.huawei.fingerprint.IAuthenticatorListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.fingerprint.V2_1.IExtBiometricsFingerprint;
import vendor.huawei.hardware.biometrics.fingerprint.V2_1.IFidoAuthenticationCallback;

public class HwFingerprintService extends FingerprintService {
    public static final int CHECK_NEED_REENROLL_FINGER = 1003;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final String FIDO_ASM = "com.huawei.hwasm";
    public static final int GET_OLD_DATA = 100;
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HW_FP_AUTH_BOTH_SPACE = 100;
    private static final String KEY_DB_CHILDREN_MODE_FPID = "fp_children_mode_fp_id";
    private static final String KEY_DB_CHILDREN_MODE_STATUS = "fp_children_enabled";
    private static final String PATH_CHILDMODE_STATUS = "childmode_status";
    private static final long POWER_PUSH_DOWN_TIME_THR = 430;
    private static final int PRIMARY_USER_ID = 0;
    public static final int REMOVE_USER_DATA = 101;
    public static final int SET_LIVENESS_SWITCH = 1002;
    private static final int STATUS_KIDS_FINGER_NO_NEED_SET = 2;
    private static final int STATUS_KIDS_FINGER_SET_FAIL = 1;
    private static final int STATUS_KIDS_FINGER_SET_SUCCESS = 0;
    private static final int STATUS_PARENT_CTRL_OFF = 0;
    private static final int STATUS_PARENT_CTRL_STUDENT = 1;
    private static final String TAG = "HwFingerprintService";
    private static final int UPDATE_SECURITY_USER_ID = 102;
    public static final int VERIFY_USER = 1001;
    private static boolean mCheckNeedEnroll = true;
    private static boolean mLivenessNeedBetaQualification = false;
    private final Context mContext;
    private IAuthenticator mIAuthenticator = new Stub() {
        public int verifyUser(IFingerprintServiceReceiver receiver, IAuthenticatorListener listener, int userid, byte[] nonce, String aaid) {
            Log.d(HwFingerprintService.TAG, "verifyUser");
            if (!HwFingerprintService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                Log.w(HwFingerprintService.TAG, "Can't authenticate non-current user");
                return -1;
            } else if (receiver == null || listener == null || nonce == null || aaid == null) {
                Log.e(HwFingerprintService.TAG, "wrong paramers.");
                return -1;
            } else {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Log.d(HwFingerprintService.TAG, "uid =" + uid);
                if (uid != 1000) {
                    Log.e(HwFingerprintService.TAG, "permission denied.");
                    return -1;
                }
                if (((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "canUseFingerprint", new Class[]{String.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{HwFingerprintService.FIDO_ASM, Boolean.valueOf(true), Integer.valueOf(uid), Integer.valueOf(pid), Integer.valueOf(userid)})).booleanValue()) {
                    final int effectiveGroupId = HwFingerprintService.this.getEffectiveUserId(userid);
                    final int callingUserId = UserHandle.getCallingUserId();
                    final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                    final IAuthenticatorListener iAuthenticatorListener = listener;
                    final String str = aaid;
                    final byte[] bArr = nonce;
                    HwFingerprintService.this.mHandler.post(new Runnable() {
                        public void run() {
                            HwFingerprintService.this.setLivenessSwitch("fido");
                            HwFingerprintService.this.startAuthentication(iFingerprintServiceReceiver.asBinder(), 0, callingUserId, effectiveGroupId, iFingerprintServiceReceiver, 0, true, HwFingerprintService.FIDO_ASM, iAuthenticatorListener, str, bArr);
                        }
                    });
                    return 0;
                }
                Log.w(HwFingerprintService.TAG, "FIDO_ASM can't use fingerprint");
                return -1;
            }
        }
    };
    private BroadcastReceiver mUserDeletedMonitor = new BroadcastReceiver() {
        private static final String FP_DATA_DIR = "fpdata";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.USER_REMOVED")) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    Slog.i(HwFingerprintService.TAG, "user deleted:" + userId);
                    if (userId == -1) {
                        Slog.i(HwFingerprintService.TAG, "get User id failed");
                        return;
                    }
                    int newUserId = userId;
                    int newPathId = userId;
                    if (UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx(UserManager.get(HwFingerprintService.this.mContext), userId))) {
                        newUserId = HwFingerprintService.HIDDEN_SPACE_ID;
                        newPathId = 0;
                    }
                    File fpDir = new File(Environment.getUserSystemDirectory(newPathId), FP_DATA_DIR);
                    if (fpDir.exists()) {
                        HwFingerprintService.this.removeUserData(newUserId, fpDir.getAbsolutePath());
                    } else {
                        Slog.v(HwFingerprintService.TAG, "no fpdata!");
                    }
                } else if (action.equals("android.intent.action.USER_PRESENT") && HwFingerprintService.mCheckNeedEnroll) {
                    int checkValReEnroll = HwFingerprintService.this.checkNeedReEnrollFingerPrints();
                    int checkValCalibrate = HwFingerprintService.this.checkNeedCalibrateFingerPrint();
                    Log.e(HwFingerprintService.TAG, "USER_PRESENT mUserDeletedMonitor need enrol : " + checkValReEnroll + "need calibrate:" + checkValCalibrate);
                    if (checkValReEnroll == 1 && checkValCalibrate != 1) {
                        HwFingerprintService.this.intentOthers(context);
                    }
                    HwFingerprintService.mCheckNeedEnroll = false;
                }
            }
        }
    };
    private boolean mflagFirstIn = true;
    private long mtimeStart = 0;

    private class HwFIDOAuthenticationClient extends AuthenticationClient {
        private String aaid;
        private int groupId;
        private IAuthenticatorListener listener;
        private IFidoAuthenticationCallback mFidoAuthenticationCallback = new IFidoAuthenticationCallback.Stub() {
            public void onUserVerificationResult(final int result, long opId, final ArrayList<Byte> userId, final ArrayList<Byte> encapsulatedResult) {
                Log.d(HwFingerprintService.TAG, "onUserVerificationResult");
                HwFingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        Log.d(HwFingerprintService.TAG, "onUserVerificationResult-run");
                        if (HwFIDOAuthenticationClient.this.listener != null) {
                            try {
                                int i;
                                byte[] byteUserId = new byte[userId.size()];
                                int userIdLen = userId.size();
                                for (i = 0; i < userIdLen; i++) {
                                    byteUserId[i] = ((Byte) userId.get(i)).byteValue();
                                }
                                byte[] byteEncapsulatedResult = new byte[encapsulatedResult.size()];
                                int encapsulatedResultLen = encapsulatedResult.size();
                                for (i = 0; i < encapsulatedResultLen; i++) {
                                    byteEncapsulatedResult[i] = ((Byte) encapsulatedResult.get(i)).byteValue();
                                }
                                HwFIDOAuthenticationClient.this.listener.onUserVerificationResult(result, byteUserId, byteEncapsulatedResult);
                            } catch (RemoteException e) {
                                Log.w(HwFingerprintService.TAG, "onUserVerificationResult RemoteException");
                            }
                        }
                    }
                });
            }
        };
        private byte[] nonce;
        private String pkgName;

        public HwFIDOAuthenticationClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int callingUserId, int groupId, long opId, boolean restricted, String owner, IAuthenticatorListener listener, String aaid, byte[] nonce) {
            super(context, halDeviceId, token, receiver, callingUserId, groupId, opId, restricted, owner);
            this.pkgName = owner;
            this.listener = listener;
            this.groupId = groupId;
            this.aaid = aaid;
            this.nonce = nonce;
        }

        public boolean onAuthenticated(int fingerId, int groupId) {
            if (fingerId != 0) {
            }
            return super.onAuthenticated(fingerId, groupId);
        }

        public int handleFailedAttempt() {
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mFailedAttempts", Integer.valueOf(((Integer) HwFingerprintService.getParentPrivateField(HwFingerprintService.this, "mFailedAttempts")).intValue() + 1));
            int lockoutMode = HwFingerprintService.this.getLockoutMode();
            if (!inLockoutMode()) {
                return 0;
            }
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mLockoutTime", Long.valueOf(SystemClock.elapsedRealtime()));
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "scheduleLockoutReset", null, null);
            onError(7, 0);
            stop(true);
            return lockoutMode;
        }

        public void resetFailedAttempts() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "resetFailedAttempts", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(true)});
        }

        public void notifyUserActivity() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "userActivity", null, null);
        }

        public IBiometricsFingerprint getFingerprintDaemon() {
            return (IBiometricsFingerprint) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "getFingerprintDaemon", null, null);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "handleHwFailedAttempt", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(0), null});
        }

        public boolean inLockoutMode() {
            return ((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "inLockoutMode", null, null)).booleanValue();
        }

        public int start() {
            Slog.d("FingerprintService", "start pkgName:" + this.pkgName);
            try {
                doVerifyUser(this.groupId, this.aaid, this.nonce);
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "call fingerprintD verify user failed");
            }
            return 0;
        }

        private void doVerifyUser(int groupId, String aaid, byte[] nonce) throws RemoteException {
            if (HwFingerprintService.this.isFingerprintDReady()) {
                IExtBiometricsFingerprint daemon = HwFingerprintService.this.getFingerprintDaemonEx();
                if (daemon == null) {
                    Slog.e("FingerprintService", "Fingerprintd is not available!");
                    return;
                }
                ArrayList<Byte> arrayNonce = new ArrayList();
                for (byte valueOf : nonce) {
                    arrayNonce.add(Byte.valueOf(valueOf));
                }
                try {
                    daemon.verifyUser(this.mFidoAuthenticationCallback, groupId, aaid, arrayNonce);
                } catch (RemoteException e) {
                    Slog.e("FingerprintService", "doVerifyUser RemoteException:" + e);
                }
            }
        }
    }

    private IExtBiometricsFingerprint getFingerprintDaemonEx() {
        IExtBiometricsFingerprint mDaemonEx = null;
        try {
            mDaemonEx = IExtBiometricsFingerprint.getService();
        } catch (NoSuchElementException e) {
        } catch (RemoteException e2) {
            Slog.e(TAG, "Failed to get biometric interface", e2);
        }
        if (mDaemonEx != null) {
            return mDaemonEx;
        }
        Slog.w(TAG, "fingerprint HIDL not available");
        return null;
    }

    private int getkidsFingerId(String whichMode, int userID, Context context) {
        if (context != null) {
            return Secure.getIntForUser(context.getContentResolver(), whichMode, 0, userID);
        }
        Slog.w(TAG, "getkidsFingerId - context = null");
        return 0;
    }

    private boolean isKidSwitchOn(int userID, Context context) {
        if (1 == Secure.getIntForUser(context.getContentResolver(), KEY_DB_CHILDREN_MODE_STATUS, 0, userID)) {
            return true;
        }
        return false;
    }

    private boolean isParentControl(int userID, Context context) {
        boolean isInStudent = false;
        if (context == null || context.getContentResolver() == null) {
            return false;
        }
        int status = Secure.getIntForUser(context.getContentResolver(), PATH_CHILDMODE_STATUS, 0, userID);
        Slog.d(TAG, "ParentControl status is " + status);
        if (status == 1) {
            isInStudent = true;
        }
        return isInStudent;
    }

    protected int setKidsFingerprint(int userID, boolean isKeyguard) {
        Slog.d(TAG, "setKidsFingerprint:start");
        int isSetOk = 1;
        int kidFpId = getkidsFingerId(KEY_DB_CHILDREN_MODE_FPID, userID, this.mContext);
        if (kidFpId == 0) {
            return 2;
        }
        boolean isParent = isParentControl(userID, this.mContext);
        boolean isPcCastMode = HwPCUtils.isPcCastModeInServer();
        Slog.d(TAG, "setKidsFingerprint-isParent = " + isParent + ", isPcCastMode =" + isPcCastMode);
        if (isKeyguard && isKidSwitchOn(userID, this.mContext) && (isParent ^ 1) != 0 && (isPcCastMode ^ 1) != 0) {
            kidFpId = 0;
        }
        Slog.d(TAG, "setKidsFingerprint-kidFpId = " + kidFpId);
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return isSetOk;
        }
        try {
            isSetOk = daemon.setKidsFingerprint(kidFpId);
        } catch (RemoteException e) {
            Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
        }
        if (isSetOk != 0) {
            isSetOk = 1;
        }
        Slog.d(TAG, "framework setLivenessSwitch ---end");
        return isSetOk;
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName, IAuthenticatorListener listener, String aaid, byte[] nonce) {
        invokeParentPrivateFunction(this, "updateActiveGroup", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(groupId), opPackageName});
        Log.v(TAG, "HwFingerprintService-startAuthentication(" + opPackageName + ")");
        AuthenticationClient client = new HwFIDOAuthenticationClient(getContext(), 0, token, receiver, callingUserId, groupId, opId, restricted, opPackageName, listener, aaid, nonce);
        if (((Boolean) invokeParentPrivateFunction(this, "inLockoutMode", null, null)).booleanValue()) {
            if ((((Boolean) invokeParentPrivateFunction(this, "isKeyguard", new Class[]{String.class}, new Object[]{opPackageName})).booleanValue() ^ 1) != 0) {
                Log.v(TAG, "In lockout mode; disallowing authentication");
                if (!client.onError(7, 0)) {
                    Log.w(TAG, "Cannot send timeout message to client");
                }
                return;
            }
        }
        invokeParentPrivateFunction(this, "startClient", new Class[]{ClientMonitor.class, Boolean.TYPE}, new Object[]{client, Boolean.valueOf(true)});
    }

    private static Field getAccessibleField(Class targetClass, String variableName) {
        try {
            final Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return field;
        } catch (Exception e) {
            Log.v(TAG, "getAccessibleField error", e);
            return null;
        }
    }

    private static Object getParentPrivateField(Object instance, String variableName) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        Field field = getAccessibleField(targetClass, variableName);
        if (field != null) {
            try {
                return field.get(superInst);
            } catch (IllegalAccessException e) {
                Log.v(TAG, "getParentPrivateField error", e);
            }
        }
        return null;
    }

    private static void setParentPrivateField(Object instance, String variableName, Object value) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        Field field = getAccessibleField(targetClass, variableName);
        if (field != null) {
            try {
                field.set(superInst, value);
            } catch (IllegalAccessException e) {
                Log.v(TAG, "setParentPrivateField error", e);
            }
        }
    }

    private static Object invokeParentPrivateFunction(Object instance, String method, Class[] paramTypes, Object[] params) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            final Method med = targetClass.getDeclaredMethod(method, paramTypes);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    med.setAccessible(true);
                    return null;
                }
            });
            return med.invoke(superInst, params);
        } catch (Exception e) {
            Log.v(TAG, "invokeParentPrivateFunction error", e);
            return null;
        }
    }

    private boolean isBetaUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    private void intentOthers(Context context) {
        Intent intent = new Intent();
        if (SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false)) {
            intent.setAction("com.android.settings.fingerprint.FingerprintSettings");
        } else {
            intent.setAction("com.android.settings.fingerprint.FingerprintMainSettings");
        }
        intent.setPackage("com.android.settings");
        intent.addFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found");
        }
    }

    public HwFingerprintService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        super.onStart();
        publishBinderService("fido_authenticator", this.mIAuthenticator.asBinder());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUserDeletedMonitor, filter);
        Slog.v(TAG, "HwFingerprintService onstart");
    }

    public void updateFingerprints(int userId) {
        HwFingerprintSets hwFpSets = remoteGetOldData();
        if (hwFpSets != null) {
            FingerprintUtils utils = FingerprintUtils.getInstance();
            if (utils != null) {
                int i;
                ArrayList mNewFingerprints = null;
                int fingerprintGpSize = hwFpSets.mFingerprintGroups.size();
                for (i = 0; i < fingerprintGpSize; i++) {
                    HwFingerprintGroup fpGroup = (HwFingerprintGroup) hwFpSets.mFingerprintGroups.get(i);
                    int realGroupId = fpGroup.mGroupId;
                    if (fpGroup.mGroupId == HIDDEN_SPACE_ID) {
                        realGroupId = getRealUserIdForApp(fpGroup.mGroupId);
                    }
                    if (realGroupId == userId) {
                        mNewFingerprints = fpGroup.mFingerprints;
                    }
                }
                if (mNewFingerprints == null) {
                    mNewFingerprints = new ArrayList();
                }
                for (Fingerprint oldFp : utils.getFingerprintsForUser(this.mContext, userId)) {
                    if (!checkItemExist(oldFp.getFingerId(), mNewFingerprints)) {
                        utils.removeFingerprintIdForUser(this.mContext, oldFp.getFingerId(), userId);
                    }
                }
                int size = mNewFingerprints.size();
                for (i = 0; i < size; i++) {
                    Fingerprint fp = (Fingerprint) mNewFingerprints.get(i);
                    utils.addFingerprintForUser(this.mContext, fp.getFingerId(), userId);
                    CharSequence fpName = fp.getName();
                    if (!(fpName == null || (fpName.toString().isEmpty() ^ 1) == 0)) {
                        utils.renameFingerprintForUser(this.mContext, fp.getFingerId(), userId, fpName);
                    }
                }
            }
        }
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        if (!UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx(UserManager.get(this.mContext), userId)) || currentUserId != 0) {
            return false;
        }
        Slog.v(TAG, "enroll privacy fingerprint in primary user ");
        return true;
    }

    public boolean checkNeedPowerpush() {
        if (this.mflagFirstIn) {
            this.mtimeStart = System.currentTimeMillis();
            this.mflagFirstIn = false;
            return true;
        }
        long timePassed = System.currentTimeMillis() - this.mtimeStart;
        Slog.v(TAG, "timepassed is  " + timePassed);
        this.mtimeStart = System.currentTimeMillis();
        return POWER_PUSH_DOWN_TIME_THR < timePassed;
    }

    public int removeUserData(int groupId, String storePath) {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        try {
            daemon.removeUserData(groupId, storePath);
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        }
        return 0;
    }

    private int checkNeedReEnrollFingerPrints() {
        int result = -1;
        Log.w(TAG, "checkNeedReEnrollFingerPrints");
        if (!isFingerprintDReady()) {
            return result;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return result;
        }
        try {
            result = daemon.checkNeedReEnrollFinger();
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        }
        Log.w(TAG, "framework checkNeedReEnrollFingerPrints is finish return = " + result);
        return result;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int result;
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE) {
            Slog.d(TAG, "code == CODE_IS_FP_NEED_CALIBRATE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            result = checkNeedCalibrateFingerPrint();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (code == CODE_SET_CALIBRATE_MODE_RULE) {
            Slog.d(TAG, "code == CODE_SET_CALIBRATE_MODE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setCalibrateMode(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code != CODE_GET_TOKEN_LEN_RULE) {
            return super.onHwTransact(code, data, reply, flags);
        } else {
            Slog.d(TAG, "code == CODE_GET_TOKEN_LEN_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            result = getTokenLen();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        }
    }

    public int checkNeedCalibrateFingerPrint() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Slog.d(TAG, "pacel  packaged :checkNeedCalibrateFingerPrint");
        int result = -1;
        try {
            result = daemon.checkNeedCalibrateFingerPrint();
        } catch (RemoteException e) {
            Slog.e(TAG, "checkNeedCalibrateFingerPrint RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd calibrate return = " + result);
        return result;
    }

    public void setCalibrateMode(int mode) {
        if (isFingerprintDReady()) {
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Slog.d(TAG, "pacel  packaged setCalibrateMode: " + mode);
            try {
                daemon.setCalibrateMode(mode);
            } catch (RemoteException e) {
                Slog.e(TAG, "setCalibrateMode RemoteException:" + e);
            }
            return;
        }
        Log.w(TAG, "FingerprintD is not Ready");
    }

    public int getTokenLen() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        int result = -1;
        Slog.d(TAG, "pacel  packaged :getTokenLen");
        try {
            result = daemon.getTokenLen();
        } catch (RemoteException e) {
            Slog.e(TAG, "getTokenLen RemoteException:" + e);
        }
        Slog.d(TAG, "fingerprintd getTokenLen token len = " + result);
        return result;
    }

    private int checkForegroundNeedLiveness() {
        Slog.w(TAG, "checkForegroundNeedLiveness:start");
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            if (procs == null) {
                return 0;
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.importance == 100) {
                    if ("com.alipay.security.mobile.authentication.huawei".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.wallet".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.android.hwpay".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    }
                }
            }
            return 0;
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed in checkForegroundNeedLiveness");
        }
    }

    private int checkNeedLivenessList(String opPackageName) {
        Slog.w(TAG, "checkNeedLivenessList:start");
        if (opPackageName == null || opPackageName.equals("com.android.keyguard")) {
            return 0;
        }
        if (opPackageName.equals("com.huawei.securitymgr")) {
            return checkForegroundNeedLiveness();
        }
        return (opPackageName.equals("com.eg.android.AlipayGphone") || opPackageName.equals("fido") || opPackageName.equals("com.alipay.security.mobile.authentication.huawei") || opPackageName.equals("com.huawei.wallet") || opPackageName.equals("com.huawei.android.hwpay")) ? 1 : 0;
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "setLivenessSwitch:start");
        if ((!mLivenessNeedBetaQualification || (isBetaUser() ^ 1) == 0) && isFingerprintDReady()) {
            int NEED_LIVENESS_AUTHENTICATION = checkNeedLivenessList(opPackageName);
            Slog.w(TAG, "NEED_LIVENESS_AUTHENTICATION = " + NEED_LIVENESS_AUTHENTICATION);
            IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
            if (daemon == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            try {
                daemon.setLivenessSwitch(NEED_LIVENESS_AUTHENTICATION);
            } catch (RemoteException e) {
                Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
            }
            Slog.w(TAG, "framework setLivenessSwitch is ok ---end");
        }
    }

    private boolean checkPackageName(String opPackageName) {
        if (opPackageName == null || !opPackageName.equals("com.android.systemui")) {
            return false;
        }
        return true;
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        if (checkPackageName(opPackageName) && flags == 100) {
            return true;
        }
        return false;
    }

    private HwFingerprintSets remoteGetOldData() {
        Slog.i(TAG, "remoteGetOldData:start");
        if (!isFingerprintDReady()) {
            return null;
        }
        IExtBiometricsFingerprint daemon = getFingerprintDaemonEx();
        if (daemon == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return null;
        }
        HwFingerprintSets _result;
        ArrayList<Integer> fingerprintInfo = new ArrayList();
        try {
            fingerprintInfo = daemon.getFpOldData();
        } catch (RemoteException e) {
            Slog.e(TAG, "remoteGetOldData RemoteException:" + e);
        }
        Parcel _reply = Parcel.obtain();
        int fingerprintInfoLen = fingerprintInfo.size();
        for (int i = 0; i < fingerprintInfoLen; i++) {
            int intValue = ((Integer) fingerprintInfo.get(i)).intValue();
            if (intValue != -1) {
                _reply.writeInt(intValue);
            }
        }
        _reply.setDataPosition(0);
        if (_reply.readInt() != 0) {
            _result = (HwFingerprintSets) HwFingerprintSets.CREATOR.createFromParcel(_reply);
        } else {
            _result = null;
        }
        _reply.recycle();
        return _result;
    }

    private static boolean checkItemExist(int oldFpId, ArrayList<Fingerprint> fingerprints) {
        int size = fingerprints.size();
        for (int i = 0; i < size; i++) {
            if (((Fingerprint) fingerprints.get(i)).getFingerId() == oldFpId) {
                fingerprints.remove(i);
                return true;
            }
        }
        return false;
    }

    private boolean isFingerprintDReady() {
        if (getFingerprintDaemon() != null) {
            return true;
        }
        Slog.w(TAG, "isFingerprintDReady: no fingeprintd!");
        return false;
    }

    protected void handleAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        if (fingerId != 0) {
            AwareFakeActivityRecg.self().setFingerprintWakeup(true);
        }
        super.handleAuthenticated(deviceId, fingerId, groupId, token);
    }

    protected void stopPickupTrunOff() {
        if (PickUpWakeScreenManager.isPickupSensorSupport(this.mContext) && PickUpWakeScreenManager.getInstance() != null) {
            PickUpWakeScreenManager.getInstance().stopTrunOffScrren();
        }
    }
}
