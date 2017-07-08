package android.hardware.fingerprint;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback.Stub;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
import android.service.notification.NotificationListenerService.Ranking;
import android.util.Log;
import android.util.Slog;
import java.security.Signature;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class FingerprintManager {
    private static final boolean DEBUG = true;
    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_CANCELED = 5;
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE = 2000;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END = 3000;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;
    private static final String TAG = "FingerprintManager";
    private AuthenticationCallback mAuthenticationCallback;
    private Context mContext;
    private CryptoObject mCryptoObject;
    private EnrollmentCallback mEnrollmentCallback;
    private Handler mHandler;
    private RemovalCallback mRemovalCallback;
    private Fingerprint mRemovalFingerprint;
    private IFingerprintService mService;
    private IFingerprintServiceReceiver mServiceReceiver;
    private IBinder mToken;

    /* renamed from: android.hardware.fingerprint.FingerprintManager.2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ LockoutResetCallback val$callback;
        final /* synthetic */ PowerManager val$powerManager;

        /* renamed from: android.hardware.fingerprint.FingerprintManager.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ LockoutResetCallback val$callback;
            final /* synthetic */ WakeLock val$wakeLock;

            AnonymousClass1(WakeLock val$wakeLock, LockoutResetCallback val$callback) {
                this.val$wakeLock = val$wakeLock;
                this.val$callback = val$callback;
            }

            public void run() {
                try {
                    this.val$callback.onLockoutReset();
                } finally {
                    this.val$wakeLock.release();
                }
            }
        }

        AnonymousClass2(PowerManager val$powerManager, LockoutResetCallback val$callback) {
            this.val$powerManager = val$powerManager;
            this.val$callback = val$callback;
        }

        public void onLockoutReset(long deviceId) throws RemoteException {
            WakeLock wakeLock = this.val$powerManager.newWakeLock(FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE, "lockoutResetCallback");
            wakeLock.acquire();
            FingerprintManager.this.mHandler.post(new AnonymousClass1(wakeLock, this.val$callback));
        }
    }

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }

        public void onAuthenticationAcquired(int acquireInfo) {
        }
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private Fingerprint mFingerprint;
        private int mUserId;

        public AuthenticationResult(CryptoObject crypto, Fingerprint fingerprint, int userId) {
            this.mCryptoObject = crypto;
            this.mFingerprint = fingerprint;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    public static final class CryptoObject {
        private final Object mCrypto;

        public CryptoObject(Signature signature) {
            this.mCrypto = signature;
        }

        public CryptoObject(Cipher cipher) {
            this.mCrypto = cipher;
        }

        public CryptoObject(Mac mac) {
            this.mCrypto = mac;
        }

        public Signature getSignature() {
            return this.mCrypto instanceof Signature ? (Signature) this.mCrypto : null;
        }

        public Cipher getCipher() {
            return this.mCrypto instanceof Cipher ? (Cipher) this.mCrypto : null;
        }

        public Mac getMac() {
            return this.mCrypto instanceof Mac ? (Mac) this.mCrypto : null;
        }

        public long getOpId() {
            return this.mCrypto != null ? AndroidKeyStoreProvider.getKeyStoreOperationHandle(this.mCrypto) : 0;
        }
    }

    public static abstract class EnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onEnrollmentProgress(int remaining) {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FingerprintManager.MSG_ENROLL_RESULT /*100*/:
                    sendEnrollResult((Fingerprint) msg.obj, msg.arg1);
                case FingerprintManager.MSG_ACQUIRED /*101*/:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1);
                case FingerprintManager.MSG_AUTHENTICATION_SUCCEEDED /*102*/:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                case FingerprintManager.MSG_AUTHENTICATION_FAILED /*103*/:
                    sendAuthenticatedFailed();
                case FingerprintManager.MSG_ERROR /*104*/:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1);
                case FingerprintManager.MSG_REMOVED /*105*/:
                    sendRemovedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                default:
            }
        }

        private void sendRemovedResult(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mRemovalCallback != null) {
                int reqFingerId = FingerprintManager.this.mRemovalFingerprint.getFingerId();
                int reqGroupId = FingerprintManager.this.mRemovalFingerprint.getGroupId();
                if (reqFingerId != 0 && fingerId != 0 && fingerId != reqFingerId) {
                    Log.w(FingerprintManager.TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
                } else if (groupId != reqGroupId) {
                    Log.w(FingerprintManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                } else {
                    FingerprintManager.this.mRemovalCallback.onRemovalSucceeded(new Fingerprint(null, groupId, fingerId, deviceId));
                }
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentError(errMsgId, FingerprintManager.this.getErrorString(errMsgId));
            } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationError(errMsgId, FingerprintManager.this.getErrorString(errMsgId));
            } else if (FingerprintManager.this.mRemovalCallback != null) {
                FingerprintManager.this.mRemovalCallback.onRemovalError(FingerprintManager.this.mRemovalFingerprint, errMsgId, FingerprintManager.this.getErrorString(errMsgId));
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(FingerprintManager.this.mCryptoObject, fp, userId));
            }
        }

        private void sendAuthenticatedFailed() {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo) {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
            String msg = FingerprintManager.this.getAcquiredString(acquireInfo);
            if (msg != null) {
                if (FingerprintManager.this.mEnrollmentCallback != null) {
                    FingerprintManager.this.mEnrollmentCallback.onEnrollmentHelp(acquireInfo, msg);
                } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                    FingerprintManager.this.mAuthenticationCallback.onAuthenticationHelp(acquireInfo, msg);
                }
            }
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private CryptoObject mCrypto;

        public OnAuthenticationCancelListener(CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        public void onCancel() {
            FingerprintManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    private class OnEnrollCancelListener implements OnCancelListener {
        private OnEnrollCancelListener() {
        }

        public void onCancel() {
            FingerprintManager.this.cancelEnrollment();
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Fingerprint fingerprint) {
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId());
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new MyHandler(handler.getLooper(), null);
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this.mContext.getMainLooper(), null);
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an authentication callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "authentication already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(crypto));
        }
        if (this.mService != null) {
            try {
                useHandler(handler);
                this.mAuthenticationCallback = callback;
                this.mCryptoObject = crypto;
                this.mService.authenticate(this.mToken, crypto != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while authenticating: ", e);
                if (callback != null) {
                    callback.onAuthenticationError(FINGERPRINT_ERROR_HW_UNAVAILABLE, getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
                }
            }
        }
    }

    public void enroll(byte[] token, CancellationSignal cancel, int flags, int userId, EnrollmentCallback callback) {
        if (userId == -2) {
            userId = getCurrentUserId();
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an enrollment callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "enrollment already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnEnrollCancelListener());
        }
        if (this.mService != null) {
            try {
                this.mEnrollmentCallback = callback;
                this.mService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
                if (callback != null) {
                    callback.onEnrollmentError(FINGERPRINT_ERROR_HW_UNAVAILABLE, getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
                }
            }
        }
    }

    public long preEnroll() {
        long result = 0;
        if (this.mService != null) {
            try {
                result = this.mService.preEnroll(this.mToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return result;
    }

    public int postEnroll() {
        int result = FINGERPRINT_ACQUIRED_GOOD;
        if (this.mService != null) {
            try {
                result = this.mService.postEnroll(this.mToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return result;
    }

    public void setActiveUser(int userId) {
        if (this.mService != null) {
            try {
                this.mService.setActiveUser(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void remove(Fingerprint fp, int userId, RemovalCallback callback) {
        if (this.mService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFingerprint = fp;
                this.mService.remove(this.mToken, fp.getFingerId(), fp.getGroupId(), userId, this.mServiceReceiver);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(fp, FINGERPRINT_ERROR_HW_UNAVAILABLE, getErrorString(FINGERPRINT_ERROR_HW_UNAVAILABLE));
                }
            }
        }
    }

    public void rename(int fpId, int userId, String newName) {
        if (this.mService != null) {
            try {
                this.mService.rename(fpId, userId, newName);
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "rename(): Service not connected!");
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getEnrolledFingerprints(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<Fingerprint> getEnrolledFingerprints() {
        return getEnrolledFingerprints(UserHandle.myUserId());
    }

    public boolean hasEnrolledFingerprints() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasEnrolledFingerprints(UserHandle.myUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasEnrolledFingerprints(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isHardwareDetected() {
        if (this.mService != null) {
            try {
                return this.mService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
        return false;
    }

    public long getAuthenticatorId() {
        if (this.mService != null) {
            try {
                return this.mService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "getAuthenticatorId(): Service not connected!");
        return 0;
    }

    public void resetTimeout(byte[] token) {
        if (this.mService != null) {
            try {
                this.mService.resetTimeout(token);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in resetTimeout(): ", e);
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "resetTimeout(): Service not connected!");
    }

    public void addLockoutResetCallback(LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                this.mService.addLockoutResetCallback(new AnonymousClass2((PowerManager) this.mContext.getSystemService(PowerManager.class), callback));
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mToken = new Binder();
        this.mServiceReceiver = new IFingerprintServiceReceiver.Stub() {
            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_ENROLL_RESULT, remaining, FingerprintManager.FINGERPRINT_ACQUIRED_GOOD, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
            }

            public void onAcquired(long deviceId, int acquireInfo) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_ACQUIRED, acquireInfo, FingerprintManager.FINGERPRINT_ACQUIRED_GOOD, Long.valueOf(deviceId)).sendToTarget();
            }

            public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_AUTHENTICATION_SUCCEEDED, userId, FingerprintManager.FINGERPRINT_ACQUIRED_GOOD, fp).sendToTarget();
            }

            public void onAuthenticationFailed(long deviceId) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_AUTHENTICATION_FAILED).sendToTarget();
            }

            public void onError(long deviceId, int error) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_ERROR, error, FingerprintManager.FINGERPRINT_ACQUIRED_GOOD, Long.valueOf(deviceId)).sendToTarget();
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) {
                FingerprintManager.this.mHandler.obtainMessage(FingerprintManager.MSG_REMOVED, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
            }
        };
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(context, null);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void cancelEnrollment() {
        if (this.mService != null) {
            try {
                this.mService.cancelEnrollment(this.mToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (this.mService != null) {
            try {
                this.mService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private String getErrorString(int errMsg) {
        switch (errMsg) {
            case FINGERPRINT_ERROR_HW_UNAVAILABLE /*1*/:
                return this.mContext.getString(17039845);
            case FINGERPRINT_ERROR_UNABLE_TO_PROCESS /*2*/:
                return this.mContext.getString(17039850);
            case FINGERPRINT_ERROR_TIMEOUT /*3*/:
                return this.mContext.getString(17039847);
            case FINGERPRINT_ERROR_NO_SPACE /*4*/:
                return this.mContext.getString(17039846);
            case FINGERPRINT_ERROR_CANCELED /*5*/:
                return this.mContext.getString(17039848);
            case FINGERPRINT_ERROR_LOCKOUT /*7*/:
                return this.mContext.getString(17039849);
            default:
                if (errMsg >= FINGERPRINT_ERROR_VENDOR_BASE) {
                    int msgNumber = errMsg + Ranking.VISIBILITY_NO_OVERRIDE;
                    String[] msgArray = this.mContext.getResources().getStringArray(17236050);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private String getAcquiredString(int acquireInfo) {
        switch (acquireInfo) {
            case FINGERPRINT_ACQUIRED_GOOD /*0*/:
                return null;
            case FINGERPRINT_ERROR_HW_UNAVAILABLE /*1*/:
                return this.mContext.getString(17039840);
            case FINGERPRINT_ERROR_UNABLE_TO_PROCESS /*2*/:
                return this.mContext.getString(17039841);
            case FINGERPRINT_ERROR_TIMEOUT /*3*/:
                return this.mContext.getString(17039842);
            case FINGERPRINT_ERROR_NO_SPACE /*4*/:
                return this.mContext.getString(17039844);
            case FINGERPRINT_ERROR_CANCELED /*5*/:
                return this.mContext.getString(17039843);
            default:
                if (acquireInfo >= FINGERPRINT_ERROR_VENDOR_BASE) {
                    if (this.mEnrollmentCallback != null && acquireInfo >= HW_FINGERPRINT_ACQUIRED_VENDOR_BASE && acquireInfo <= HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END) {
                        return ProxyInfo.LOCAL_EXCL_LIST;
                    }
                    int msgNumber = acquireInfo + Ranking.VISIBILITY_NO_OVERRIDE;
                    String[] msgArray = this.mContext.getResources().getStringArray(17236049);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }
}
