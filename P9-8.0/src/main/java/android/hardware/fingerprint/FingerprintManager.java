package android.hardware.fingerprint;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.fingerprint.IFingerprintServiceReceiver.Stub;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
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
    public static final int FINGERPRINT_ACQUIRED_VENDOR = 6;
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_CANCELED = 5;
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;
    public static final int FINGERPRINT_ERROR_LOCKOUT_PERMANENT = 9;
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FINGERPRINT_ERROR_VENDOR = 8;
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE = 2000;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END = 3000;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ENUMERATED = 106;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;
    private static final String TAG = "FingerprintManager";
    private AuthenticationCallback mAuthenticationCallback;
    private Context mContext;
    private CryptoObject mCryptoObject;
    private EnrollmentCallback mEnrollmentCallback;
    private EnumerateCallback mEnumerateCallback;
    private Handler mHandler;
    private RemovalCallback mRemovalCallback;
    private Fingerprint mRemovalFingerprint;
    private IFingerprintService mService;
    private IFingerprintServiceReceiver mServiceReceiver = new Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(100, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
        }

        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            FingerprintManager.this.mHandler.obtainMessage(101, acquireInfo, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            Log.d(FingerprintManager.TAG, "binder call and send onAuthenticationSucceeded msg");
            Message msg = FingerprintManager.this.mHandler.obtainMessage(102, userId, 0, fp);
            if (FingerprintManager.this.mHandler.hasMessages(101)) {
                msg.sendToTarget();
            } else {
                FingerprintManager.this.mHandler.sendMessageAtFrontOfQueue(msg);
            }
        }

        public void onAuthenticationFailed(long deviceId) {
            FingerprintManager.this.mHandler.obtainMessage(103).sendToTarget();
        }

        public void onError(long deviceId, int error, int vendorCode) {
            FingerprintManager.this.mHandler.obtainMessage(104, error, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(105, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
        }

        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(106, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
        }
    };
    private IBinder mToken = new Binder();

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

    public static abstract class EnumerateCallback {
        public void onEnumerateError(int errMsgId, CharSequence errString) {
        }

        public void onEnumerate(Fingerprint fingerprint) {
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
                case 100:
                    sendEnrollResult((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 102:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 103:
                    sendAuthenticatedFailed();
                    return;
                case 104:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 105:
                    sendRemovedResult((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 106:
                    sendEnumeratedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }

        private void sendRemovedResult(Fingerprint fingerprint, int remaining) {
            if (FingerprintManager.this.mRemovalCallback != null) {
                if (fingerprint == null) {
                    Log.e(FingerprintManager.TAG, "Received MSG_REMOVED, but fingerprint is null");
                    return;
                }
                int fingerId = fingerprint.getFingerId();
                int reqFingerId = FingerprintManager.this.mRemovalFingerprint.getFingerId();
                if (reqFingerId == 0 || fingerId == 0 || fingerId == reqFingerId) {
                    int groupId = fingerprint.getGroupId();
                    int reqGroupId = FingerprintManager.this.mRemovalFingerprint.getGroupId();
                    if (groupId != reqGroupId) {
                        Log.w(FingerprintManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                        return;
                    } else {
                        FingerprintManager.this.mRemovalCallback.onRemovalSucceeded(fingerprint, remaining);
                        return;
                    }
                }
                Log.w(FingerprintManager.TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
            }
        }

        private void sendEnumeratedResult(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mEnumerateCallback != null) {
                FingerprintManager.this.mEnumerateCallback.onEnumerate(new Fingerprint(null, groupId, fingerId, deviceId));
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
            int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentError(clientErrMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationError(clientErrMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mRemovalCallback != null) {
                FingerprintManager.this.mRemovalCallback.onRemovalError(FingerprintManager.this.mRemovalFingerprint, clientErrMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mEnumerateCallback != null) {
                FingerprintManager.this.mEnumerateCallback.onEnumerateError(clientErrMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                AuthenticationResult result = new AuthenticationResult(FingerprintManager.this.mCryptoObject, fp, userId);
                Log.v(FingerprintManager.TAG, "callback sendAuthenticatedSucceeded");
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationSucceeded(result);
                return;
            }
            Log.w(FingerprintManager.TAG, "mAuthenticationCallback is null send Failed");
        }

        private void sendAuthenticatedFailed() {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
            int clientAcquireInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationAcquired(clientAcquireInfo);
            }
            String msg = FingerprintManager.this.getAcquiredString(acquireInfo, vendorCode);
            if (msg != null) {
                int clientInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
                if (FingerprintManager.this.mEnrollmentCallback != null) {
                    FingerprintManager.this.mEnrollmentCallback.onEnrollmentHelp(clientInfo, msg);
                } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                    FingerprintManager.this.mAuthenticationCallback.onAuthenticationHelp(clientInfo, msg);
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
        /* synthetic */ OnEnrollCancelListener(FingerprintManager this$0, OnEnrollCancelListener -this1) {
            this();
        }

        private OnEnrollCancelListener() {
        }

        public void onCancel() {
            FingerprintManager.this.cancelEnrollment();
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Fingerprint fp, int remaining) {
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId());
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new MyHandler(this, handler.getLooper(), null);
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null);
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
                    callback.onAuthenticationError(1, getErrorString(1, 0));
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
            cancel.setOnCancelListener(new OnEnrollCancelListener(this, null));
        }
        if (this.mService != null) {
            try {
                this.mEnrollmentCallback = callback;
                this.mService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
                if (callback != null) {
                    callback.onEnrollmentError(1, getErrorString(1, 0));
                }
            }
        }
    }

    public long preEnroll() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.preEnroll(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int postEnroll() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.postEnroll(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
                    callback.onRemovalError(fp, 1, getErrorString(1, 0));
                }
            }
        }
    }

    public void enumerate(int userId, EnumerateCallback callback) {
        if (this.mService != null) {
            try {
                this.mEnumerateCallback = callback;
                this.mService.enumerate(this.mToken, userId, this.mServiceReceiver);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enumerate: ", e);
                if (callback != null) {
                    callback.onEnumerateError(1, getErrorString(1, 0));
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

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IFingerprintServiceLockoutResetCallback.Stub() {
                    public void onLockoutReset(long deviceId, IRemoteCallback serverCallback) throws RemoteException {
                        try {
                            WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                            wakeLock.acquire();
                            FingerprintManager.this.mHandler.post(new -$Lambda$mAn4tgSaQL9G4z96dzSedXu3vvs(wakeLock, callback));
                        } finally {
                            serverCallback.sendResult(null);
                        }
                    }

                    static /* synthetic */ void lambda$-android_hardware_fingerprint_FingerprintManager$2_32064(WakeLock wakeLock, LockoutResetCallback callback) {
                        try {
                            callback.onLockoutReset();
                        } finally {
                            wakeLock.release();
                        }
                    }
                });
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(this, context, null);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
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

    private String getErrorString(int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return this.mContext.getString(17040020);
            case 2:
                return this.mContext.getString(17040025);
            case 3:
                return this.mContext.getString(17040024);
            case 4:
                return this.mContext.getString(17040023);
            case 5:
                return this.mContext.getString(17040019);
            case 7:
                return this.mContext.getString(17040021);
            case 8:
                String[] msgArray = this.mContext.getResources().getStringArray(17236050);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 9:
                return this.mContext.getString(17040022);
        }
        Slog.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
        return null;
    }

    private String getAcquiredString(int acquireInfo, int vendorCode) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return this.mContext.getString(17040016);
            case 2:
                return this.mContext.getString(17040015);
            case 3:
                return this.mContext.getString(17040014);
            case 4:
                return this.mContext.getString(17040018);
            case 5:
                return this.mContext.getString(17040017);
            case 6:
                if (this.mEnrollmentCallback != null && vendorCode + 1000 >= 2000 && vendorCode + 1000 <= HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END) {
                    return ProxyInfo.LOCAL_EXCL_LIST;
                }
                String[] msgArray = this.mContext.getResources().getStringArray(17236049);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
        }
        Slog.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
        return null;
    }
}
