package com.huawei.hardware.face;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.security.keystore.AndroidKeyStoreProviderEx;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.FaceRecognizeManagerFactory;
import java.security.Signature;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class FaceAuthenticationManager {
    private static final HashMap<Integer, Integer> ACQUIRED_CODE_MAP = new HashMap<Integer, Integer>() {
        /* class com.huawei.hardware.face.FaceAuthenticationManager.AnonymousClass2 */

        {
            put(0, 0);
            put(1, 13);
            put(2, 13);
            put(3, 1);
            put(4, 1);
            put(5, 12);
            put(6, 5);
            put(7, 4);
            put(8, 9);
            put(9, 6);
            put(10, 8);
            put(11, 7);
            put(12, 13);
            put(13, 13);
            put(14, 13);
            put(15, 13);
            put(16, 13);
            put(17, 13);
            put(18, 13);
            put(19, 10);
            put(20, 13);
            put(21, 11);
            put(22, 11);
            put(23, 13);
            put(27, 13);
            put(28, 10);
            put(29, 13);
            put(30, 13);
            put(31, 13);
            put(32, 13);
        }
    };
    private static final HashMap<Integer, Integer> ERROR_CODE_MAP = new HashMap<Integer, Integer>() {
        /* class com.huawei.hardware.face.FaceAuthenticationManager.AnonymousClass1 */

        {
            put(1, 8);
            put(2, 5);
            put(3, 8);
            put(4, 3);
            put(5, 2);
            put(6, 2);
            put(7, 4);
            put(8, 7);
            put(9, 8);
            put(10, 11);
            put(11, 2);
            put(13, 13);
        }
    };
    private static final int FACERECOGNITION_OFF = 0;
    private static final int FACERECOGNITION_ON = 1;
    public static final int FACE_ACQUIRED_GOOD = 0;
    public static final int FACE_ACQUIRED_INSUFFICIENT = 1;
    public static final int FACE_ACQUIRED_NOT_DETECTED = 12;
    public static final int FACE_ACQUIRED_POOR_GAZE = 11;
    public static final int FACE_ACQUIRED_TOO_BRIGHT = 2;
    public static final int FACE_ACQUIRED_TOO_CLOSE = 4;
    public static final int FACE_ACQUIRED_TOO_DARK = 3;
    public static final int FACE_ACQUIRED_TOO_FAR = 5;
    public static final int FACE_ACQUIRED_TOO_HIGH = 6;
    public static final int FACE_ACQUIRED_TOO_LEFT = 9;
    public static final int FACE_ACQUIRED_TOO_LOW = 7;
    public static final int FACE_ACQUIRED_TOO_MUCH_MOTION = 10;
    public static final int FACE_ACQUIRED_TOO_RIGHT = 8;
    public static final int FACE_ACQUIRED_VENDOR = 13;
    public static final int FACE_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FACE_ERROR_BUSY = 13;
    public static final int FACE_ERROR_CANCELED = 5;
    public static final int FACE_ERROR_HW_NOT_PRESENT = 12;
    public static final int FACE_ERROR_HW_UNAVAILABLE = 1;
    public static final int FACE_ERROR_LOCKOUT = 7;
    public static final int FACE_ERROR_LOCKOUT_PERMANENT = 9;
    public static final int FACE_ERROR_NOT_ENROLLED = 11;
    public static final int FACE_ERROR_NO_SPACE = 4;
    public static final int FACE_ERROR_TIMEOUT = 3;
    public static final int FACE_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FACE_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FACE_ERROR_USER_CANCELED = 10;
    public static final int FACE_ERROR_VENDOR = 8;
    public static final int FACE_ERROR_VENDOR_BASE = 1000;
    private static final int FACE_HARDWARE_3D_STRUCT = 4;
    private static final int FACE_HARDWARE_3D_TOF = 5;
    private static final int FACE_SECURE_LEVEL_PAY = 1;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final String TAG = "FaceManager";
    public static final String USE_FACE_AUTHENTICATION = "com.huawei.permission.USE_FACERECOGNITION";
    private static final int VENDOR_CODE = 0;
    private AuthenticationCallback mAuthenticationCallback;
    private final Object mAuthenticationLock = new Object();
    private final Context mContext;
    private CryptoObject mCryptoObject;
    private DefaultFaceRecognizeManagerImpl mFaceManagerImpl;
    private Handler mHandler = null;

    public static final class FaceRecognitionAbility {
        public int faceMode;
        public boolean isFaceRecognitionSupport;
        public int reserve;
        public int secureLevel;
    }

    public FaceAuthenticationManager(@NonNull Context context) {
        this.mContext = context;
        initFaceAuthManagerHandler(context);
        this.mAuthenticationCallback = null;
        this.mFaceManagerImpl = FaceRecognizeManagerFactory.getInstance().getFaceRecognizeManagerImpl(context, new DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback() {
            /* class com.huawei.hardware.face.FaceAuthenticationManager.AnonymousClass3 */

            @Override // huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback
            public void onCallbackEvent(int reqId, int type, int code, int errorCode) {
                if (type == 2) {
                    synchronized (FaceAuthenticationManager.this.mAuthenticationLock) {
                        if (FaceAuthenticationManager.this.mAuthenticationCallback != null) {
                            if (code == 1) {
                                FaceAuthenticationManager.this.handleCodeCallbackResult(errorCode);
                            } else if (code == 3) {
                                FaceAuthenticationManager.this.handleCodeCallbackAcquire(errorCode);
                            } else {
                                SlogEx.i(FaceAuthenticationManager.TAG, "Unknow callback code.");
                            }
                        }
                    }
                    if (code == 1 && FaceAuthenticationManager.this.mFaceManagerImpl.release() != 0) {
                        SlogEx.w(FaceAuthenticationManager.TAG, "Authentication release failed.");
                    }
                }
            }
        });
    }

    private void initFaceAuthManagerHandler(Context context) {
        if (context == null) {
            SlogEx.e(TAG, "context is null.");
        } else {
            this.mHandler = new FaceAuthManagerHandler(context);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCodeCallbackResult(int errorCode) {
        Handler handler = this.mHandler;
        if (handler == null) {
            SlogEx.e(TAG, "handler is null for the context is null.");
        } else if (errorCode == 0) {
            this.mHandler.obtainMessage(MSG_AUTHENTICATION_SUCCEEDED, UserHandleEx.myUserId(), 0, null).sendToTarget();
        } else if (errorCode != 3) {
            int error = 8;
            Integer result = ERROR_CODE_MAP.get(Integer.valueOf(errorCode));
            if (result != null) {
                error = result.intValue();
            }
            this.mHandler.obtainMessage(MSG_ERROR, error, errorCode, 0L).sendToTarget();
        } else {
            handler.obtainMessage(MSG_AUTHENTICATION_FAILED).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCodeCallbackAcquire(int errorCode) {
        if (this.mHandler == null) {
            SlogEx.e(TAG, "handler is null for the context is null.");
            return;
        }
        int acquireInfo = 13;
        Integer result = ACQUIRED_CODE_MAP.get(Integer.valueOf(errorCode));
        if (result != null) {
            acquireInfo = result.intValue();
        }
        this.mHandler.obtainMessage(101, acquireInfo, errorCode, 0L).sendToTarget();
    }

    public void authenticate(@Nullable CryptoObject crypto, @Nullable CancellationSignal cancel, int flags, @NonNull AuthenticationCallback callback, @Nullable Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandleEx.myUserId());
    }

    public void authenticate(@Nullable CryptoObject crypto, @Nullable CancellationSignal cancel, int flags, @NonNull AuthenticationCallback callback, Handler handler, int userId) {
        if (cancel != null) {
            if (cancel.isCanceled()) {
                SlogEx.w(TAG, "authentication already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(crypto));
        }
        if (this.mFaceManagerImpl != null && callback != null) {
            useHandler(handler);
            synchronized (this.mAuthenticationLock) {
                if (this.mAuthenticationCallback != null) {
                    SlogEx.w(TAG, "Authentication is in running, do not accept dup request.");
                    callback.onAuthenticationError(1, getErrorString(1, 0));
                    return;
                }
                this.mAuthenticationCallback = callback;
            }
            if (this.mFaceManagerImpl.init() != 0) {
                SlogEx.w(TAG, "Authentication initialization failed.");
                callback.onAuthenticationError(1, getErrorString(1, 0));
                return;
            }
            this.mCryptoObject = crypto;
            this.mFaceManagerImpl.authenticate(crypto != null ? crypto.getOpId() : 0, flags, null);
        }
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new FaceAuthManagerHandler(handler.getLooper());
        } else if (this.mContext == null || this.mHandler.getLooper() == this.mContext.getMainLooper()) {
            SlogEx.i(TAG, "Unsupported handler.");
        } else {
            this.mHandler = new FaceAuthManagerHandler(this.mContext.getMainLooper());
        }
    }

    public boolean isOpenApiSupported(AuthenticationCallback authenticationCallback) {
        FaceRecognitionAbility ability = getFaceRecognitionAbility();
        if (ability != null && ability.isFaceRecognitionSupport && (ability.faceMode == 4 || ability.faceMode == 5)) {
            return true;
        }
        if (authenticationCallback != null) {
            SlogEx.e(TAG, "Face recognize is not supported.");
            authenticationCallback.onAuthenticationError(1, getErrorString(1, 0));
        }
        return false;
    }

    public boolean isSoterApiSupported(AuthenticationCallback authenticationCallback) {
        FaceRecognitionAbility ability = getFaceRecognitionAbility();
        if (ability != null && ability.isFaceRecognitionSupport && ((ability.faceMode == 4 || ability.faceMode == 5) && ability.secureLevel == 1)) {
            return true;
        }
        if (authenticationCallback != null) {
            SlogEx.e(TAG, "Face recognize is not supported.");
            authenticationCallback.onAuthenticationError(1, getErrorString(1, 0));
        }
        return false;
    }

    public boolean hasEnrolledFace() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null || defaultFaceRecognizeManagerImpl.getEnrolledFaceIDs().length <= 0) {
            return false;
        }
        return true;
    }

    public int getEnrolledFaceID() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null) {
            return 0;
        }
        int[] faceIds = defaultFaceRecognizeManagerImpl.getEnrolledFaceIDs();
        if (faceIds.length == 0) {
            return 0;
        }
        return faceIds[0];
    }

    public boolean isHardwareDetected() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null || (defaultFaceRecognizeManagerImpl.getHardwareSupportType() & 1) != 1) {
            return false;
        }
        return true;
    }

    public FaceRecognitionAbility getFaceRecognitionAbility() {
        DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility ability = this.mFaceManagerImpl.getFaceRecognitionAbility();
        if (ability == null) {
            return null;
        }
        FaceRecognitionAbility faceAbility = new FaceRecognitionAbility();
        faceAbility.isFaceRecognitionSupport = ability.isFaceRecognitionSupport;
        faceAbility.faceMode = ability.faceMode;
        faceAbility.secureLevel = ability.secureLevel;
        faceAbility.reserve = ability.reserve;
        return faceAbility;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (this.mFaceManagerImpl != null) {
            CryptoObject cryptoObject2 = this.mCryptoObject;
            this.mFaceManagerImpl.cancelAuthenticate(cryptoObject2 != null ? cryptoObject2.getOpId() : 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getErrorString(int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return "face_error_hw_not_available";
            case 2:
                return "face_error_unable_to_process";
            case 3:
                return "face_error_timeout";
            case 4:
                return "face_error_no_space";
            case 5:
                return "face_error_canceled";
            case 6:
            case 10:
            default:
                SlogEx.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
                return null;
            case 7:
                return "face_error_lockout";
            case 8:
                return "face_error_vendor: code " + String.valueOf(vendorCode);
            case 9:
                return "face_error_lockout_permanent";
            case 11:
                return "face_error_not_enrolled";
            case 12:
                return "face_error_hw_not_present";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getAcquiredString(int acquireInfo, int vendorCode) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return "face_acquired_insufficient";
            case 2:
                return "face_acquired_too_bright";
            case 3:
                return "face_acquired_too_dark";
            case 4:
                return "face_acquired_too_close";
            case 5:
                return "face_acquired_too_far";
            case 6:
                return "face_acquired_too_high";
            case 7:
                return "face_acquired_too_low";
            case 8:
                return "face_acquired_too_right";
            case 9:
                return "face_acquired_too_left";
            case 10:
                return "face_acquired_too_much_motion";
            case 11:
                return "face_acquired_poor_gaze";
            case 12:
                return "face_acquired_not_detected";
            case 13:
                return "face_acquired_vendor: code " + String.valueOf(vendorCode);
            default:
                SlogEx.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
                return null;
        }
    }

    public static final class CryptoObject {
        private final Object mCrypto;

        public CryptoObject(@NonNull Signature signature) {
            this.mCrypto = signature;
        }

        public CryptoObject(@NonNull Cipher cipher) {
            this.mCrypto = cipher;
        }

        public CryptoObject(@NonNull Mac mac) {
            this.mCrypto = mac;
        }

        public Signature getSignature() {
            Object obj = this.mCrypto;
            if (obj instanceof Signature) {
                return (Signature) obj;
            }
            return null;
        }

        public Cipher getCipher() {
            Object obj = this.mCrypto;
            if (obj instanceof Cipher) {
                return (Cipher) obj;
            }
            return null;
        }

        public Mac getMac() {
            Object obj = this.mCrypto;
            if (obj instanceof Mac) {
                return (Mac) obj;
            }
            return null;
        }

        public long getOpId() {
            Object obj = this.mCrypto;
            if (obj != null) {
                return AndroidKeyStoreProviderEx.getKeyStoreOperationHandle(obj);
            }
            return 0;
        }
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private Face mFace;
        private int mUserId;

        public AuthenticationResult(CryptoObject crypto, Face face, int userId) {
            this.mCryptoObject = crypto;
            this.mFace = face;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public Face getFace() {
            return this.mFace;
        }

        public int getUserId() {
            return this.mUserId;
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

    public static abstract class EnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onEnrollmentProgress(int remaining, long vendorMsg) {
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(Face face, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Face face) {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    /* access modifiers changed from: private */
    public class OnAuthenticationCancelListener implements CancellationSignal.OnCancelListener {
        private CryptoObject mCrypto;

        OnAuthenticationCancelListener(CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            FaceAuthenticationManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    /* access modifiers changed from: private */
    public class FaceAuthManagerHandler extends Handler {
        private FaceAuthManagerHandler(Context context) {
            super(context.getMainLooper());
        }

        private FaceAuthManagerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case FaceAuthenticationManager.MSG_AUTHENTICATION_SUCCEEDED /* 102 */:
                    sendAuthenticatedSucceeded((Face) msg.obj, msg.arg1);
                    return;
                case FaceAuthenticationManager.MSG_AUTHENTICATION_FAILED /* 103 */:
                    sendAuthenticatedFailed();
                    return;
                case FaceAuthenticationManager.MSG_ERROR /* 104 */:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
            int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
            synchronized (FaceAuthenticationManager.this.mAuthenticationLock) {
                if (FaceAuthenticationManager.this.mAuthenticationCallback != null) {
                    FaceAuthenticationManager.this.mAuthenticationCallback.onAuthenticationError(clientErrMsgId, FaceAuthenticationManager.this.getErrorString(errMsgId, vendorCode));
                    FaceAuthenticationManager.this.mAuthenticationCallback = null;
                }
            }
        }

        private void sendAuthenticatedSucceeded(Face face, int userId) {
            synchronized (FaceAuthenticationManager.this.mAuthenticationLock) {
                if (FaceAuthenticationManager.this.mAuthenticationCallback != null) {
                    FaceAuthenticationManager.this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(FaceAuthenticationManager.this.mCryptoObject, face, userId));
                    FaceAuthenticationManager.this.mAuthenticationCallback = null;
                }
            }
        }

        private void sendAuthenticatedFailed() {
            synchronized (FaceAuthenticationManager.this.mAuthenticationLock) {
                if (FaceAuthenticationManager.this.mAuthenticationCallback != null) {
                    FaceAuthenticationManager.this.mAuthenticationCallback.onAuthenticationFailed();
                    FaceAuthenticationManager.this.mAuthenticationCallback = null;
                }
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
            String msg = FaceAuthenticationManager.this.getAcquiredString(acquireInfo, vendorCode);
            if (msg != null) {
                int clientInfo = acquireInfo == 13 ? vendorCode + 1000 : acquireInfo;
                synchronized (FaceAuthenticationManager.this.mAuthenticationLock) {
                    if (FaceAuthenticationManager.this.mAuthenticationCallback != null) {
                        FaceAuthenticationManager.this.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
                        FaceAuthenticationManager.this.mAuthenticationCallback.onAuthenticationHelp(clientInfo, msg);
                    }
                }
            }
        }
    }
}
