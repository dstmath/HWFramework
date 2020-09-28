package com.huawei.fingerprint;

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticatorListener;
import com.huawei.uikit.effect.BuildConfig;

public class Authenticator {
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;
    public static final String SERVICE_NAME = "fido_authenticator";
    public FingerprintManager.AuthenticationCallback mAuthenticationCallback;
    private AuthenticatorListener mAuthenticatorListener;
    private IFingerprintServiceReceiver mFpServiceReceiver = new IFingerprintServiceReceiver.Stub() {
        /* class com.huawei.fingerprint.Authenticator.AnonymousClass1 */

        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            }
        }

        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            } else {
                Authenticator.this.mHandler.obtainMessage(101, acquireInfo, 0, Long.valueOf(deviceId)).sendToTarget();
            }
        }

        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            } else {
                Authenticator.this.mHandler.obtainMessage(Authenticator.MSG_AUTHENTICATION_SUCCEEDED, userId, 0, fp).sendToTarget();
            }
        }

        public void onAuthenticationFailed(long deviceId) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            } else {
                Authenticator.this.mHandler.obtainMessage(Authenticator.MSG_AUTHENTICATION_FAILED).sendToTarget();
            }
        }

        public void onError(long deviceId, int error, int vendorCode) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            } else {
                Authenticator.this.mHandler.obtainMessage(Authenticator.MSG_ERROR, error, 0, Long.valueOf(deviceId)).sendToTarget();
            }
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            }
        }

        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) {
            if (1000 != Binder.getCallingUid()) {
                Log.e("HwFingerprintService", "aidl check permission failed");
            }
        }
    };
    private Handler mHandler;
    private IAuthenticatorListener mIAuthenticatorListener = new IAuthenticatorListener.Stub() {
        /* class com.huawei.fingerprint.Authenticator.AnonymousClass2 */

        @Override // com.huawei.fingerprint.IAuthenticatorListener
        public void onUserVerificationResult(final int result, final byte[] userid, final byte[] uvt) throws RemoteException {
            Authenticator.this.mHandler.post(new Runnable() {
                /* class com.huawei.fingerprint.Authenticator.AnonymousClass2.AnonymousClass1 */

                public void run() {
                    Log.d("HwFingerprintService", "Authenticator-onUserVerificationResult-run");
                    if (Authenticator.this.mAuthenticatorListener != null) {
                        Log.d("HwFingerprintService", "Authenticator-mAuthenticatorListener != null");
                        Authenticator.this.mAuthenticatorListener.onUserVerificationResult(result, userid, uvt);
                        Authenticator.this.mAuthenticatorListener = null;
                    }
                }
            });
        }
    };
    private IAuthenticator mService;

    public interface AuthenticatorListener {
        void onIsUserIDValidResult(boolean z);

        void onUserVerificationResult(int i, byte[] bArr, byte[] bArr2);
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                default:
                    return;
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case Authenticator.MSG_AUTHENTICATION_SUCCEEDED /*{ENCODED_INT: 102}*/:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case Authenticator.MSG_AUTHENTICATION_FAILED /*{ENCODED_INT: 103}*/:
                    sendAuthenticatedFailed();
                    return;
                case Authenticator.MSG_ERROR /*{ENCODED_INT: 104}*/:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId) {
            if (Authenticator.this.mAuthenticationCallback != null) {
                Authenticator.this.mAuthenticationCallback.onAuthenticationError(errMsgId, BuildConfig.FLAVOR);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (Authenticator.this.mAuthenticationCallback != null) {
                Authenticator.this.mAuthenticationCallback.onAuthenticationSucceeded(new FingerprintManager.AuthenticationResult(null, fp, userId));
            }
        }

        private void sendAuthenticatedFailed() {
            if (Authenticator.this.mAuthenticationCallback != null) {
                Authenticator.this.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo) {
            if (Authenticator.this.mAuthenticationCallback != null) {
                Authenticator.this.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
        }
    }

    public static Authenticator getAuthenticator() {
        IBinder service = ServiceManager.getService(SERVICE_NAME);
        if (service != null) {
            return new Authenticator(service, new Binder(), null);
        }
        return null;
    }

    public void setAuthenticationCallback(FingerprintManager.AuthenticationCallback callbak) {
        this.mAuthenticationCallback = callbak;
    }

    private Authenticator(IBinder service, IBinder client, Handler handler) {
        this.mService = IAuthenticator.Stub.asInterface(service);
        this.mHandler = handler == null ? new MyHandler(Looper.getMainLooper()) : handler;
    }

    public int verifyUser(FingerprintManager.AuthenticationCallback callbak, byte[] finalChallenge, String aaid, AuthenticatorListener listener) {
        this.mAuthenticationCallback = callbak;
        return verifyUser(finalChallenge, aaid, listener);
    }

    public int verifyUser(byte[] finalChallenge, String aaid, AuthenticatorListener listener) {
        this.mAuthenticatorListener = listener;
        try {
            this.mService.verifyUser(this.mFpServiceReceiver, this.mIAuthenticatorListener, UserHandle.myUserId(), finalChallenge, aaid);
            return 0;
        } catch (RemoteException e) {
            Log.e("HwFingerprintService", "RemoteException-verifyUser");
            return 0;
        }
    }

    public int cancelVerify() {
        try {
            this.mService.cancelVerifyUser(this.mFpServiceReceiver, UserHandle.myUserId());
            return 0;
        } catch (RemoteException e) {
            Log.e("HwFingerprintService", "RemoteException-cancelVerifyUser");
            return 0;
        }
    }
}
