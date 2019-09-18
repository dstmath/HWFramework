package android.hardware.fingerprint;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricFingerprintConstants;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.R;
import java.security.Signature;
import java.util.List;
import java.util.concurrent.Executor;
import javax.crypto.Cipher;
import javax.crypto.Mac;

@Deprecated
public class FingerprintManager implements BiometricFingerprintConstants {
    private static final boolean DEBUG = true;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE = 2000;
    public static final int HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END = 3000;
    public static final int HW_UD_FINGERPRINT_HELP_PAUSE_VENDORCODE = 1011;
    public static final int HW_UD_FINGERPRINT_HELP_RESUME_VENDORCODE = 1012;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ENUMERATED = 106;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;
    private static final String TAG = "FingerprintManager";
    private BiometricAuthenticator.AuthenticationCallback mAuthenticationCallback;
    private Context mContext;
    private android.hardware.biometrics.CryptoObject mCryptoObject;
    /* access modifiers changed from: private */
    public EnrollmentCallback mEnrollmentCallback;
    /* access modifiers changed from: private */
    public EnumerateCallback mEnumerateCallback;
    /* access modifiers changed from: private */
    public Executor mExecutor;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public RemovalCallback mRemovalCallback;
    /* access modifiers changed from: private */
    public Fingerprint mRemovalFingerprint;
    private IFingerprintService mService;
    private IFingerprintServiceReceiver mServiceReceiver = new IFingerprintServiceReceiver.Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            Handler access$400 = FingerprintManager.this.mHandler;
            Fingerprint fingerprint = new Fingerprint(null, groupId, fingerId, deviceId);
            access$400.obtainMessage(100, remaining, 0, fingerprint).sendToTarget();
        }

        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            if (FingerprintManager.this.mExecutor != null) {
                Executor access$1400 = FingerprintManager.this.mExecutor;
                $$Lambda$FingerprintManager$2$CkUh5EAfiFsfsEamQtkeaLZq6M r1 = new Runnable(deviceId, acquireInfo, vendorCode) {
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r4;
                        this.f$3 = r5;
                    }

                    public final void run() {
                        FingerprintManager.this.sendAcquiredResult(this.f$1, this.f$2, this.f$3);
                    }
                };
                access$1400.execute(r1);
                return;
            }
            FingerprintManager.this.mHandler.obtainMessage(101, acquireInfo, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            Log.d(FingerprintManager.TAG, "binder call and send onAuthenticationSucceeded msg");
            if (FingerprintManager.this.mExecutor != null) {
                FingerprintManager.this.mExecutor.execute(new Runnable(fp, userId) {
                    private final /* synthetic */ Fingerprint f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        FingerprintManager.this.sendAuthenticatedSucceeded(this.f$1, this.f$2);
                    }
                });
                return;
            }
            Message msg = FingerprintManager.this.mHandler.obtainMessage(102, userId, 0, fp);
            if (FingerprintManager.this.mHandler.hasMessages(101)) {
                msg.sendToTarget();
            } else {
                FingerprintManager.this.mHandler.sendMessageAtFrontOfQueue(msg);
            }
        }

        public void onAuthenticationFailed(long deviceId) {
            if (FingerprintManager.this.mExecutor != null) {
                FingerprintManager.this.mExecutor.execute(new Runnable() {
                    public final void run() {
                        FingerprintManager.this.sendAuthenticatedFailed();
                    }
                });
            } else {
                FingerprintManager.this.mHandler.obtainMessage(103).sendToTarget();
            }
        }

        public void onError(long deviceId, int error, int vendorCode) {
            if (FingerprintManager.this.mExecutor == null) {
                FingerprintManager.this.mHandler.obtainMessage(104, error, vendorCode, Long.valueOf(deviceId)).sendToTarget();
            } else if (error == 10 || error == 5) {
                Executor access$1400 = FingerprintManager.this.mExecutor;
                $$Lambda$FingerprintManager$2$iiSGvjInjtzVqJwXw4RQIjKDs r1 = new Runnable(deviceId, error, vendorCode) {
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r4;
                        this.f$3 = r5;
                    }

                    public final void run() {
                        FingerprintManager.this.sendErrorResult(this.f$1, this.f$2, this.f$3);
                    }
                };
                access$1400.execute(r1);
            } else {
                Handler access$400 = FingerprintManager.this.mHandler;
                $$Lambda$FingerprintManager$2$n67wlbYWr0PNZwBB3xLLO4RgAq4 r12 = new Runnable(deviceId, error, vendorCode) {
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r4;
                        this.f$3 = r5;
                    }

                    public final void run() {
                        FingerprintManager.AnonymousClass2.lambda$onError$5(FingerprintManager.AnonymousClass2.this, this.f$1, this.f$2, this.f$3);
                    }
                };
                access$400.postDelayed(r12, 2000);
            }
        }

        public static /* synthetic */ void lambda$onError$5(AnonymousClass2 r8, long deviceId, int error, int vendorCode) {
            Executor access$1400 = FingerprintManager.this.mExecutor;
            $$Lambda$FingerprintManager$2$DBgvtkIDrK5T5S0UgEanHtoKmOA r1 = new Runnable(deviceId, error, vendorCode) {
                private final /* synthetic */ long f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                public final void run() {
                    FingerprintManager.this.sendErrorResult(this.f$1, this.f$2, this.f$3);
                }
            };
            access$1400.execute(r1);
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            Handler access$400 = FingerprintManager.this.mHandler;
            Fingerprint fingerprint = new Fingerprint(null, groupId, fingerId, deviceId);
            access$400.obtainMessage(105, remaining, 0, fingerprint).sendToTarget();
        }

        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(106, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
        }
    };
    private IBinder mToken = new Binder();

    @Deprecated
    public static abstract class AuthenticationCallback extends BiometricAuthenticator.AuthenticationCallback {
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

        public void onAuthenticationSucceeded(BiometricAuthenticator.AuthenticationResult result) {
            onAuthenticationSucceeded(new AuthenticationResult((CryptoObject) result.getCryptoObject(), (Fingerprint) result.getId(), result.getUserId()));
        }
    }

    @Deprecated
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

    @Deprecated
    public static final class CryptoObject extends android.hardware.biometrics.CryptoObject {
        public CryptoObject(Signature signature) {
            super(signature);
        }

        public CryptoObject(Cipher cipher) {
            super(cipher);
        }

        public CryptoObject(Mac mac) {
            super(mac);
        }

        public Signature getSignature() {
            return super.getSignature();
        }

        public Cipher getCipher() {
            return super.getCipher();
        }

        public Mac getMac() {
            return super.getMac();
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
                    FingerprintManager.this.sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 102:
                    FingerprintManager.this.sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 103:
                    FingerprintManager.this.sendAuthenticatedFailed();
                    return;
                case 104:
                    FingerprintManager.this.sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
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
                    Slog.e(FingerprintManager.TAG, "Received MSG_REMOVED, but fingerprint is null");
                    return;
                }
                int fingerId = fingerprint.getFingerId();
                int reqFingerId = FingerprintManager.this.mRemovalFingerprint.getFingerId();
                if (reqFingerId == 0 || fingerId == 0 || fingerId == reqFingerId) {
                    int groupId = fingerprint.getGroupId();
                    int reqGroupId = FingerprintManager.this.mRemovalFingerprint.getGroupId();
                    if (groupId != reqGroupId) {
                        Slog.w(FingerprintManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                        return;
                    }
                    FingerprintManager.this.mRemovalCallback.onRemovalSucceeded(fingerprint, remaining);
                    return;
                }
                Slog.w(FingerprintManager.TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
            }
        }

        private void sendEnumeratedResult(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mEnumerateCallback != null) {
                EnumerateCallback access$1100 = FingerprintManager.this.mEnumerateCallback;
                Fingerprint fingerprint = new Fingerprint(null, groupId, fingerId, deviceId);
                access$1100.onEnumerate(fingerprint);
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }
    }

    private class OnAuthenticationCancelListener implements CancellationSignal.OnCancelListener {
        private android.hardware.biometrics.CryptoObject mCrypto;

        public OnAuthenticationCancelListener(android.hardware.biometrics.CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        public void onCancel() {
            FingerprintManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    private class OnEnrollCancelListener implements CancellationSignal.OnCancelListener {
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

    @Deprecated
    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, this.mContext.getUserId());
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new MyHandler(handler.getLooper());
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this.mContext.getMainLooper());
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId) {
        CryptoObject cryptoObject = crypto;
        CancellationSignal cancellationSignal = cancel;
        AuthenticationCallback authenticationCallback = callback;
        if (authenticationCallback != null) {
            if (cancellationSignal != null) {
                if (cancel.isCanceled()) {
                    Slog.w(TAG, "authentication already canceled");
                    return;
                }
                cancellationSignal.setOnCancelListener(new OnAuthenticationCancelListener(cryptoObject));
            }
            if (this.mService != null) {
                try {
                    useHandler(handler);
                    this.mAuthenticationCallback = authenticationCallback;
                    this.mCryptoObject = cryptoObject;
                    this.mService.authenticate(this.mToken, cryptoObject != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName(), null, null);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Remote exception while authenticating: ", e);
                    if (authenticationCallback != null) {
                        authenticationCallback.onAuthenticationError(1, getErrorString(1, 0));
                    }
                }
            } else {
                Handler handler2 = handler;
            }
            return;
        }
        Handler handler3 = handler;
        throw new IllegalArgumentException("Must supply an authentication callback");
    }

    private void authenticate(int userId, android.hardware.biometrics.CryptoObject crypto, CancellationSignal cancel, Bundle bundle, Executor executor, IBiometricPromptReceiver receiver, BiometricAuthenticator.AuthenticationCallback callback) {
        android.hardware.biometrics.CryptoObject cryptoObject = crypto;
        BiometricAuthenticator.AuthenticationCallback authenticationCallback = callback;
        this.mCryptoObject = cryptoObject;
        if (cancel.isCanceled()) {
            Slog.w(TAG, "authentication already canceled");
            return;
        }
        cancel.setOnCancelListener(new OnAuthenticationCancelListener(cryptoObject));
        if (this.mService != null) {
            try {
                this.mExecutor = executor;
                this.mAuthenticationCallback = authenticationCallback;
                this.mService.authenticate(this.mToken, cryptoObject != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, 0, this.mContext.getOpPackageName(), bundle, receiver);
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception while authenticating", e);
                this.mExecutor.execute(new Runnable(authenticationCallback) {
                    private final /* synthetic */ BiometricAuthenticator.AuthenticationCallback f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        this.f$1.onAuthenticationError(1, FingerprintManager.this.getErrorString(1, 0));
                    }
                });
            }
        } else {
            Executor executor2 = executor;
        }
    }

    public void authenticate(CancellationSignal cancel, Bundle bundle, Executor executor, IBiometricPromptReceiver receiver, BiometricAuthenticator.AuthenticationCallback callback) {
        if (cancel == null) {
            throw new IllegalArgumentException("Must supply a cancellation signal");
        } else if (bundle == null) {
            throw new IllegalArgumentException("Must supply a bundle");
        } else if (executor == null) {
            throw new IllegalArgumentException("Must supply an executor");
        } else if (receiver == null) {
            throw new IllegalArgumentException("Must supply a receiver");
        } else if (callback != null) {
            authenticate(this.mContext.getUserId(), null, cancel, bundle, executor, receiver, callback);
        } else {
            throw new IllegalArgumentException("Must supply a calback");
        }
    }

    public void authenticate(android.hardware.biometrics.CryptoObject crypto, CancellationSignal cancel, Bundle bundle, Executor executor, IBiometricPromptReceiver receiver, BiometricAuthenticator.AuthenticationCallback callback) {
        if (crypto == null) {
            throw new IllegalArgumentException("Must supply a crypto object");
        } else if (cancel == null) {
            throw new IllegalArgumentException("Must supply a cancellation signal");
        } else if (bundle == null) {
            throw new IllegalArgumentException("Must supply a bundle");
        } else if (executor == null) {
            throw new IllegalArgumentException("Must supply an executor");
        } else if (receiver == null) {
            throw new IllegalArgumentException("Must supply a receiver");
        } else if (callback != null) {
            authenticate(this.mContext.getUserId(), crypto, cancel, bundle, executor, receiver, callback);
        } else {
            throw new IllegalArgumentException("Must supply a callback");
        }
    }

    public void enroll(byte[] token, CancellationSignal cancel, int flags, int userId, EnrollmentCallback callback) {
        if (userId == -2) {
            userId = getCurrentUserId();
        }
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Slog.w(TAG, "enrollment already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnEnrollCancelListener());
            }
            if (this.mService != null) {
                try {
                    this.mEnrollmentCallback = callback;
                    this.mService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.w(TAG, "Remote exception in enroll: ", e);
                    if (callback != null) {
                        callback.onEnrollmentError(1, getErrorString(1, 0));
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an enrollment callback");
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
                Slog.w(TAG, "Remote exception in remove: ", e);
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
                Slog.w(TAG, "Remote exception in enumerate: ", e);
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
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "rename(): Service not connected!");
        }
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
        return getEnrolledFingerprints(this.mContext.getUserId());
    }

    @Deprecated
    public boolean hasEnrolledFingerprints() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasEnrolledFingerprints(this.mContext.getUserId(), this.mContext.getOpPackageName());
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

    @Deprecated
    public boolean isHardwareDetected() {
        if (this.mService != null) {
            try {
                return this.mService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
            return false;
        }
    }

    public long getAuthenticatorId() {
        if (this.mService != null) {
            try {
                return this.mService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "getAuthenticatorId(): Service not connected!");
            return 0;
        }
    }

    public void resetTimeout(byte[] token) {
        if (this.mService != null) {
            try {
                this.mService.resetTimeout(token);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in resetTimeout(): ", e);
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "resetTimeout(): Service not connected!");
        }
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IFingerprintServiceLockoutResetCallback.Stub() {
                    public void onLockoutReset(long deviceId, IRemoteCallback serverCallback) throws RemoteException {
                        try {
                            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                            wakeLock.acquire();
                            FingerprintManager.this.mHandler.post(new Runnable(wakeLock) {
                                private final /* synthetic */ PowerManager.WakeLock f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    FingerprintManager.AnonymousClass1.lambda$onLockoutReset$0(FingerprintManager.LockoutResetCallback.this, this.f$1);
                                }
                            });
                        } finally {
                            serverCallback.sendResult(null);
                        }
                    }

                    static /* synthetic */ void lambda$onLockoutReset$0(LockoutResetCallback callback, PowerManager.WakeLock wakeLock) {
                        try {
                            callback.onLockoutReset();
                        } finally {
                            wakeLock.release();
                        }
                    }
                });
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "addLockoutResetCallback(): Service not connected!");
        }
    }

    /* access modifiers changed from: private */
    public void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
        if (this.mAuthenticationCallback != null) {
            BiometricAuthenticator.AuthenticationResult result = new BiometricAuthenticator.AuthenticationResult(this.mCryptoObject, fp, userId);
            Log.v(TAG, "callback sendAuthenticatedSucceeded");
            this.mAuthenticationCallback.onAuthenticationSucceeded(result);
            return;
        }
        Log.w(TAG, "mAuthenticationCallback is null send Failed");
    }

    /* access modifiers changed from: private */
    public void sendAuthenticatedFailed() {
        if (this.mAuthenticationCallback != null) {
            this.mAuthenticationCallback.onAuthenticationFailed();
        }
    }

    /* access modifiers changed from: private */
    public void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
        int clientAcquireInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
        if (this.mAuthenticationCallback != null) {
            this.mAuthenticationCallback.onAuthenticationAcquired(clientAcquireInfo);
        }
        String msg = getAcquiredString(acquireInfo, vendorCode);
        if (msg != null) {
            int clientInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
            if (this.mEnrollmentCallback != null) {
                this.mEnrollmentCallback.onEnrollmentHelp(clientInfo, msg);
            } else if (this.mAuthenticationCallback != null) {
                Log.e(TAG, "sendAcquiredResult,clientInfo = " + clientInfo);
                this.mAuthenticationCallback.onAuthenticationHelp(clientInfo, msg);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
        int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
        if (this.mEnrollmentCallback != null) {
            this.mEnrollmentCallback.onEnrollmentError(clientErrMsgId, getErrorString(errMsgId, vendorCode));
        } else if (this.mAuthenticationCallback != null) {
            this.mAuthenticationCallback.onAuthenticationError(clientErrMsgId, getErrorString(errMsgId, vendorCode));
        } else if (this.mRemovalCallback != null) {
            this.mRemovalCallback.onRemovalError(this.mRemovalFingerprint, clientErrMsgId, getErrorString(errMsgId, vendorCode));
        } else if (this.mEnumerateCallback != null) {
            this.mEnumerateCallback.onEnumerateError(clientErrMsgId, getErrorString(errMsgId, vendorCode));
        }
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(context);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public void cancelEnrollment() {
        if (this.mService != null) {
            try {
                this.mService.cancelEnrollment(this.mToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: private */
    public void cancelAuthentication(android.hardware.biometrics.CryptoObject cryptoObject) {
        if (this.mService != null) {
            try {
                this.mService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String getErrorString(int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return this.mContext.getString(R.string.fingerprint_error_hw_not_available);
            case 2:
                return this.mContext.getString(R.string.fingerprint_error_unable_to_process);
            case 3:
                return this.mContext.getString(R.string.fingerprint_error_timeout);
            case 4:
                return this.mContext.getString(R.string.fingerprint_error_no_space);
            case 5:
                return this.mContext.getString(R.string.fingerprint_error_canceled);
            case 7:
                return this.mContext.getString(R.string.fingerprint_error_lockout);
            case 8:
                String[] msgArray = this.mContext.getResources().getStringArray(R.array.fingerprint_error_vendor);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 9:
                return this.mContext.getString(R.string.fingerprint_error_lockout_permanent);
            case 10:
                return this.mContext.getString(R.string.fingerprint_error_user_canceled);
            case 11:
                return this.mContext.getString(R.string.fingerprint_error_no_fingerprints);
            case 12:
                return this.mContext.getString(R.string.fingerprint_error_hw_not_present);
        }
        Slog.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
        return null;
    }

    public String getAcquiredString(int acquireInfo, int vendorCode) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return this.mContext.getString(R.string.fingerprint_acquired_partial);
            case 2:
                return this.mContext.getString(R.string.fingerprint_acquired_insufficient);
            case 3:
                return this.mContext.getString(R.string.fingerprint_acquired_imager_dirty);
            case 4:
                return this.mContext.getString(R.string.fingerprint_acquired_too_slow);
            case 5:
                return this.mContext.getString(R.string.fingerprint_acquired_too_fast);
            case 6:
                if (this.mEnrollmentCallback != null && vendorCode + 1000 >= 2000 && vendorCode + 1000 <= 3000) {
                    return "";
                }
                if (this.mAuthenticationCallback == null || !(vendorCode + 1000 == 1011 || vendorCode + 1000 == 1012)) {
                    String[] msgArray = this.mContext.getResources().getStringArray(R.array.fingerprint_acquired_vendor);
                    if (vendorCode < msgArray.length) {
                        return msgArray[vendorCode];
                    }
                } else {
                    Slog.w(TAG, "ud fingerprint send mask hide or resume helpcode");
                    return "";
                }
                break;
        }
        Slog.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
        return null;
    }
}
