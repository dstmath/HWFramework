package com.android.server.fingerprint;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.security.GateKeeper;
import android.util.Log;
import android.util.Slog;
import com.android.server.fingerprint.HwFingerprintSets.HwFingerprintGroup;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.os.UserManagerEx;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticatorListener;
import com.huawei.fingerprint.IFidoAuthenticationCallback;
import com.huawei.fingerprint.IFidoAuthenticationCallback.Stub;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class HwFingerprintService extends FingerprintService {
    public static final int CHECK_NEED_REENROLL_FINGER = 1003;
    private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintDaemon";
    private static final String FIDO_ASM = "com.huawei.hwasm";
    private static final String FINGERPRINTD = "android.hardware.fingerprint.IFingerprintDaemon";
    public static final int GET_OLD_DATA = 100;
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HW_FP_AUTH_BOTH_SPACE = 100;
    private static final int PRIMARY_USER_ID = 0;
    public static final int REMOVE_USER_DATA = 101;
    private static final String SECURE_USER_ID_UPDATED = "is_secure_user_id_updated";
    public static final int SET_LIVENESS_SWITCH = 1002;
    private static final String TAG = "HwFingerprintService";
    private static final int UPDATE_SECURITY_USER_ID = 102;
    public static final int VERIFY_USER = 1001;
    private static boolean mCheckNeedEnroll;
    private static boolean mLivenessNeedBetaQualification;
    private final Context mContext;
    private IAuthenticator mIAuthenticator;
    private BroadcastReceiver mUserDeletedMonitor;

    /* renamed from: com.android.server.fingerprint.HwFingerprintService.3 */
    static class AnonymousClass3 implements PrivilegedAction {
        final /* synthetic */ Field val$field;

        AnonymousClass3(Field val$field) {
            this.val$field = val$field;
        }

        public Object run() {
            this.val$field.setAccessible(true);
            return null;
        }
    }

    /* renamed from: com.android.server.fingerprint.HwFingerprintService.4 */
    static class AnonymousClass4 implements PrivilegedAction {
        final /* synthetic */ Field val$field;

        AnonymousClass4(Field val$field) {
            this.val$field = val$field;
        }

        public Object run() {
            this.val$field.setAccessible(true);
            return null;
        }
    }

    /* renamed from: com.android.server.fingerprint.HwFingerprintService.5 */
    static class AnonymousClass5 implements PrivilegedAction {
        final /* synthetic */ Method val$med;

        AnonymousClass5(Method val$med) {
            this.val$med = val$med;
        }

        public Object run() {
            this.val$med.setAccessible(true);
            return null;
        }
    }

    private class HwFIDOAuthenticationClient extends AuthenticationClient {
        private String aaid;
        private int groupId;
        private IAuthenticatorListener listener;
        private IFidoAuthenticationCallback mFidoAuthenticationCallback;
        private byte[] nonce;
        private String pkgName;

        public HwFIDOAuthenticationClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int callingUserId, int groupId, long opId, boolean restricted, String owner, IAuthenticatorListener listener, String aaid, byte[] nonce) {
            super(context, halDeviceId, token, receiver, callingUserId, groupId, opId, restricted, owner);
            this.mFidoAuthenticationCallback = new Stub() {

                /* renamed from: com.android.server.fingerprint.HwFingerprintService.HwFIDOAuthenticationClient.1.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ byte[] val$encapsulatedResult;
                    final /* synthetic */ int val$result;
                    final /* synthetic */ byte[] val$userId;

                    AnonymousClass1(int val$result, byte[] val$userId, byte[] val$encapsulatedResult) {
                        this.val$result = val$result;
                        this.val$userId = val$userId;
                        this.val$encapsulatedResult = val$encapsulatedResult;
                    }

                    public void run() {
                        Log.d(HwFingerprintService.TAG, "onUserVerificationResult-run");
                        if (HwFIDOAuthenticationClient.this.listener != null) {
                            try {
                                HwFIDOAuthenticationClient.this.listener.onUserVerificationResult(this.val$result, this.val$userId, this.val$encapsulatedResult);
                            } catch (RemoteException e) {
                                Log.w(HwFingerprintService.TAG, "onUserVerificationResult RemoteException");
                            }
                        }
                    }
                }

                public int onUserVerificationResult(int result, long opId, byte[] userId, byte[] encapsulatedResult) {
                    Log.d(HwFingerprintService.TAG, "onUserVerificationResult");
                    HwFingerprintService.this.mHandler.post(new AnonymousClass1(result, userId, encapsulatedResult));
                    return HwFingerprintService.PRIMARY_USER_ID;
                }
            };
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

        public boolean handleFailedAttempt() {
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mFailedAttempts", Integer.valueOf(((Integer) HwFingerprintService.getParentPrivateField(HwFingerprintService.this, "mFailedAttempts")).intValue() + 1));
            if (!inLockoutMode()) {
                return false;
            }
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mLockoutTime", Long.valueOf(SystemClock.elapsedRealtime()));
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "scheduleLockoutReset", null, null);
            onError(7);
            stop(true);
            return !((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "isKeyguard", new Class[]{String.class}, new Object[]{this.pkgName})).booleanValue();
        }

        public void resetFailedAttempts() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "resetFailedAttempts", null, null);
        }

        public void notifyUserActivity() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "userActivity", null, null);
        }

        public IFingerprintDaemon getFingerprintDaemon() {
            return (IFingerprintDaemon) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "getFingerprintDaemon", null, null);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "handleHwFailedAttempt", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(HwFingerprintService.PRIMARY_USER_ID), null});
        }

        public boolean inLockoutMode() {
            return ((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "inLockoutMode", null, null)).booleanValue();
        }

        public int start() {
            try {
                doVerifyUser(this.groupId, this.aaid, this.nonce);
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "call fingerprintD verify user failed");
            }
            return HwFingerprintService.PRIMARY_USER_ID;
        }

        private void doVerifyUser(int groupId, String aaid, byte[] nonce) throws RemoteException {
            if (HwFingerprintService.this.isFingerprintDReady()) {
                IBinder remote = ServiceManager.getService(HwFingerprintService.FINGERPRINTD);
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(HwFingerprintService.FINGERPRINTD);
                    data.writeStrongBinder(this.mFidoAuthenticationCallback.asBinder());
                    data.writeInt(groupId);
                    data.writeString(aaid);
                    data.writeByteArray(nonce);
                    remote.transact(HwFingerprintService.VERIFY_USER, data, reply, HwFingerprintService.PRIMARY_USER_ID);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }
    }

    static {
        mLivenessNeedBetaQualification = false;
        mCheckNeedEnroll = true;
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName, IAuthenticatorListener listener, String aaid, byte[] nonce) {
        invokeParentPrivateFunction(this, "updateActiveGroup", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(groupId), opPackageName});
        Log.v(TAG, "HwFingerprintService-startAuthentication(" + opPackageName + ")");
        AuthenticationClient client = new HwFIDOAuthenticationClient(getContext(), 0, token, receiver, callingUserId, groupId, opId, restricted, opPackageName, listener, aaid, nonce);
        if (((Boolean) invokeParentPrivateFunction(this, "inLockoutMode", null, null)).booleanValue()) {
            if (!((Boolean) invokeParentPrivateFunction(this, "isKeyguard", new Class[]{String.class}, new Object[]{opPackageName})).booleanValue()) {
                Log.v(TAG, "In lockout mode; disallowing authentication");
                if (!client.onError(7)) {
                    Log.w(TAG, "Cannot send timeout message to client");
                }
                return;
            }
        }
        invokeParentPrivateFunction(this, "startClient", new Class[]{ClientMonitor.class, Boolean.TYPE}, new Object[]{client, Boolean.valueOf(true)});
    }

    private static Object getParentPrivateField(Object instance, String variableName) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new AnonymousClass3(field));
            return field.get(superInst);
        } catch (Exception e) {
            Log.v(TAG, "getParentPrivateField error", e);
            return null;
        }
    }

    private static void setParentPrivateField(Object instance, String variableName, Object value) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new AnonymousClass4(field));
            field.set(superInst, value);
        } catch (Exception e) {
            Log.v(TAG, "setParentPrivateField error", e);
        }
    }

    private static Object invokeParentPrivateFunction(Object instance, String method, Class[] paramTypes, Object[] params) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            Method med = targetClass.getDeclaredMethod(method, paramTypes);
            AccessController.doPrivileged(new AnonymousClass5(med));
            return med.invoke(superInst, params);
        } catch (Exception e) {
            Log.v(TAG, "invokeParentPrivateFunction error", e);
            return null;
        }
    }

    private boolean isBetaUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", PRIMARY_USER_ID);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    private void intentOthers(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.android.settings.fingerprint.FingerprintMainSettings");
        intent.setPackage(WifiProCommonUtils.HUAWEI_SETTINGS);
        intent.addFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found");
        }
    }

    public HwFingerprintService(Context context) {
        super(context);
        this.mIAuthenticator = new IAuthenticator.Stub() {

            /* renamed from: com.android.server.fingerprint.HwFingerprintService.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ String val$aaid;
                final /* synthetic */ int val$callingUserId;
                final /* synthetic */ int val$effectiveGroupId;
                final /* synthetic */ IAuthenticatorListener val$listener;
                final /* synthetic */ byte[] val$nonce;
                final /* synthetic */ IFingerprintServiceReceiver val$receiver;

                AnonymousClass1(IFingerprintServiceReceiver val$receiver, int val$callingUserId, int val$effectiveGroupId, IAuthenticatorListener val$listener, String val$aaid, byte[] val$nonce) {
                    this.val$receiver = val$receiver;
                    this.val$callingUserId = val$callingUserId;
                    this.val$effectiveGroupId = val$effectiveGroupId;
                    this.val$listener = val$listener;
                    this.val$aaid = val$aaid;
                    this.val$nonce = val$nonce;
                }

                public void run() {
                    HwFingerprintService.this.setLivenessSwitch("fido");
                    HwFingerprintService.this.startAuthentication(this.val$receiver.asBinder(), 0, this.val$callingUserId, this.val$effectiveGroupId, this.val$receiver, HwFingerprintService.PRIMARY_USER_ID, true, HwFingerprintService.FIDO_ASM, this.val$listener, this.val$aaid, this.val$nonce);
                }
            }

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
                    if (((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "canUseFingerprint", new Class[]{String.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{HwFingerprintService.FIDO_ASM, Boolean.valueOf(true), Integer.valueOf(uid), Integer.valueOf(pid)})).booleanValue()) {
                        int effectiveGroupId = HwFingerprintService.this.getEffectiveUserId(userid);
                        int callingUserId = UserHandle.getCallingUserId();
                        Handler handler = HwFingerprintService.this.mHandler;
                        r17.post(new AnonymousClass1(receiver, callingUserId, effectiveGroupId, listener, aaid, nonce));
                        return HwFingerprintService.PRIMARY_USER_ID;
                    }
                    Log.w(HwFingerprintService.TAG, "FIDO_ASM can't use fingerprint");
                    return -1;
                }
            }
        };
        this.mUserDeletedMonitor = new BroadcastReceiver() {
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
                        if (UserManagerEx.isHwHiddenSpace(UserManager.get(HwFingerprintService.this.mContext).getUserInfo(userId))) {
                            newUserId = HwFingerprintService.HIDDEN_SPACE_ID;
                            newPathId = HwFingerprintService.PRIMARY_USER_ID;
                        }
                        File fpDir = new File(Environment.getUserSystemDirectory(newPathId), FP_DATA_DIR);
                        if (fpDir.exists()) {
                            try {
                                HwFingerprintService.this.removeUserData(newUserId, fpDir.getAbsolutePath().getBytes("utf-8"));
                            } catch (UnsupportedEncodingException e) {
                                Log.e(HwFingerprintService.TAG, "UnsupportedEncodingException");
                            }
                        } else {
                            Slog.v(HwFingerprintService.TAG, "no fpdata!");
                        }
                    } else if (action.equals("android.intent.action.USER_PRESENT")) {
                        if (HwFingerprintService.mCheckNeedEnroll) {
                            int checkVal = HwFingerprintService.this.checkNeedReEnrollFingerPrints();
                            Log.e(HwFingerprintService.TAG, "mUserDeletedMonitor onReceiver | intent.action: " + intent.getAction() + "is need enrol result: " + checkVal);
                            if (checkVal == 1) {
                                HwFingerprintService.this.intentOthers(context);
                            }
                            HwFingerprintService.mCheckNeedEnroll = false;
                        }
                        if (Secure.getInt(context.getContentResolver(), HwFingerprintService.SECURE_USER_ID_UPDATED, HwFingerprintService.PRIMARY_USER_ID) != 1) {
                            try {
                                if (HwFingerprintService.this.updateSecurityUserId(GateKeeper.getSecureUserId()) == 0) {
                                    Secure.putInt(context.getContentResolver(), HwFingerprintService.SECURE_USER_ID_UPDATED, 1);
                                }
                            } catch (IllegalStateException ex) {
                                Slog.e(HwFingerprintService.TAG, "getSecureUserId failed ex = " + ex);
                            }
                        }
                    }
                }
            }
        };
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
                for (i = PRIMARY_USER_ID; i < hwFpSets.mFingerprintGroups.size(); i++) {
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
                for (i = PRIMARY_USER_ID; i < mNewFingerprints.size(); i++) {
                    Fingerprint fp = (Fingerprint) mNewFingerprints.get(i);
                    utils.addFingerprintForUser(this.mContext, fp.getFingerId(), userId);
                    CharSequence fpName = fp.getName();
                    if (!(fpName == null || fpName.toString().isEmpty())) {
                        utils.renameFingerprintForUser(this.mContext, fp.getFingerId(), userId, fpName);
                    }
                }
            }
        }
    }

    public int removeUserData(int groupId, byte[] path) {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService(FINGERPRINTD);
        if (remote == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(FINGERPRINTD);
            data.writeInt(groupId);
            data.writeByteArray(path);
            remote.transact(REMOVE_USER_DATA, data, reply, PRIMARY_USER_ID);
            reply.readException();
            reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "removeUserData RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        return PRIMARY_USER_ID;
    }

    private int checkNeedReEnrollFingerPrints() {
        Log.w(TAG, "checkNeedReEnrollFingerPrints");
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService(FINGERPRINTD);
        if (remote == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        Log.w(TAG, "pacel  packaged ");
        try {
            data.writeInterfaceToken(FINGERPRINTD);
            remote.transact(CHECK_NEED_REENROLL_FINGER, data, reply, PRIMARY_USER_ID);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Log.w(TAG, "framework setLivenessSwitch is finish return = " + result);
        return result;
    }

    private int checkForegroundNeedLiveness() {
        Slog.w(TAG, "checkForegroundNeedLiveness:start");
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            if (procs == null) {
                return PRIMARY_USER_ID;
            }
            int N = procs.size();
            for (int i = PRIMARY_USER_ID; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.importance == HW_FP_AUTH_BOTH_SPACE) {
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
            return PRIMARY_USER_ID;
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed in checkForegroundNeedLiveness");
        }
    }

    private int checkNeedLivenessList(String opPackageName) {
        Slog.w(TAG, "checkNeedLivenessList:start");
        if (opPackageName == null || opPackageName.equals("com.android.keyguard")) {
            return PRIMARY_USER_ID;
        }
        if (opPackageName.equals("com.huawei.securitymgr")) {
            return checkForegroundNeedLiveness();
        }
        return (opPackageName.equals("com.eg.android.AlipayGphone") || opPackageName.equals("fido") || opPackageName.equals("com.alipay.security.mobile.authentication.huawei") || opPackageName.equals("com.huawei.wallet") || opPackageName.equals("com.huawei.android.hwpay")) ? 1 : PRIMARY_USER_ID;
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "setLivenessSwitch:start");
        if ((!mLivenessNeedBetaQualification || isBetaUser()) && isFingerprintDReady()) {
            int NEED_LIVENESS_AUTHENTICATION = checkNeedLivenessList(opPackageName);
            Slog.w(TAG, "NEED_LIVENESS_AUTHENTICATION = " + NEED_LIVENESS_AUTHENTICATION);
            IBinder remote = ServiceManager.getService(FINGERPRINTD);
            if (remote == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(FINGERPRINTD);
                data.writeInt(NEED_LIVENESS_AUTHENTICATION);
                remote.transact(SET_LIVENESS_SWITCH, data, reply, PRIMARY_USER_ID);
                reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
            } finally {
                reply.recycle();
                data.recycle();
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
        if (checkPackageName(opPackageName) && flags == HW_FP_AUTH_BOTH_SPACE) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HwFingerprintSets remoteGetOldData() {
        if (!isFingerprintDReady()) {
            return null;
        }
        IBinder remote = ServiceManager.getService(FINGERPRINTD);
        if (remote == null) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        HwFingerprintSets hwFingerprintSets = null;
        try {
            _data.writeInterfaceToken(FINGERPRINTD);
            remote.transact(HW_FP_AUTH_BOTH_SPACE, _data, _reply, PRIMARY_USER_ID);
            _reply.readException();
            if (_reply.readInt() != 0) {
                hwFingerprintSets = (HwFingerprintSets) HwFingerprintSets.CREATOR.createFromParcel(_reply);
            } else {
                hwFingerprintSets = null;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e) {
            Slog.e(TAG, "remoteGetOldData RemoteException:" + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return hwFingerprintSets;
    }

    private static boolean checkItemExist(int oldFpId, ArrayList<Fingerprint> fingerprints) {
        for (int i = PRIMARY_USER_ID; i < fingerprints.size(); i++) {
            if (((Fingerprint) fingerprints.get(i)).getFingerId() == oldFpId) {
                fingerprints.remove(i);
                return true;
            }
        }
        return false;
    }

    private int updateSecurityUserId(long securityId) {
        int result = -1;
        if (!isFingerprintDReady()) {
            return result;
        }
        IBinder remote = ServiceManager.getService(FINGERPRINTD);
        if (remote == null) {
            Slog.e(TAG, "updateSecurityUserId getService is null");
            return result;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(FINGERPRINTD);
            data.writeLong(securityId);
            remote.transact(UPDATE_SECURITY_USER_ID, data, reply, PRIMARY_USER_ID);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "updateSecurityId RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Slog.i(TAG, "updateSecurityUserId result = " + result);
        return result;
    }

    private boolean isFingerprintDReady() {
        if (getFingerprintDaemon() != null) {
            return true;
        }
        Slog.w(TAG, "isFingerprintDReady: no fingeprintd!");
        return false;
    }
}
