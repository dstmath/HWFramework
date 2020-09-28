package android.hardware.face;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricFaceConstants;
import android.hardware.biometrics.CryptoObject;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.face.FaceManager;
import android.hardware.face.IFaceServiceReceiver;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.R;
import com.android.internal.os.SomeArgs;
import java.util.List;

public class FaceManager implements BiometricAuthenticator, BiometricFaceConstants {
    private static final boolean DEBUG = true;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final int MSG_GET_FEATURE_COMPLETED = 106;
    private static final int MSG_REMOVED = 105;
    private static final int MSG_SET_FEATURE_COMPLETED = 107;
    private static final String TAG = "FaceManager";
    private AuthenticationCallback mAuthenticationCallback;
    private final Context mContext;
    private CryptoObject mCryptoObject;
    private EnrollmentCallback mEnrollmentCallback;
    private GetFeatureCallback mGetFeatureCallback;
    private Handler mHandler;
    private RemovalCallback mRemovalCallback;
    private Face mRemovalFace;
    private IFaceService mService;
    private IFaceServiceReceiver mServiceReceiver = new IFaceServiceReceiver.Stub() {
        /* class android.hardware.face.FaceManager.AnonymousClass1 */

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onEnrollResult(long deviceId, int faceId, int remaining) {
            FaceManager.this.mHandler.obtainMessage(100, remaining, 0, new Face(null, faceId, deviceId)).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            FaceManager.this.mHandler.obtainMessage(101, acquireInfo, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onAuthenticationSucceeded(long deviceId, Face face, int userId) {
            FaceManager.this.mHandler.obtainMessage(102, userId, 0, face).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onAuthenticationFailed(long deviceId) {
            FaceManager.this.mHandler.obtainMessage(103).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onError(long deviceId, int error, int vendorCode) {
            FaceManager.this.mHandler.obtainMessage(104, error, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onRemoved(long deviceId, int faceId, int remaining) {
            FaceManager.this.mHandler.obtainMessage(105, remaining, 0, new Face(null, faceId, deviceId)).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onEnumerated(long deviceId, int faceId, int remaining) {
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onFeatureSet(boolean success, int feature) {
            FaceManager.this.mHandler.obtainMessage(107, feature, 0, Boolean.valueOf(success)).sendToTarget();
        }

        @Override // android.hardware.face.IFaceServiceReceiver
        public void onFeatureGet(boolean success, int feature, boolean value) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Boolean.valueOf(success);
            args.argi1 = feature;
            args.arg2 = Boolean.valueOf(value);
            FaceManager.this.mHandler.obtainMessage(106, args).sendToTarget();
        }
    };
    private SetFeatureCallback mSetFeatureCallback;
    private IBinder mToken = new Binder();

    public static abstract class GetFeatureCallback {
        public abstract void onCompleted(boolean z, int i, boolean z2);
    }

    public static abstract class SetFeatureCallback {
        public abstract void onCompleted(boolean z, int i);
    }

    public FaceManager(Context context, IFaceService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FaceAuthenticationManagerService was null");
        }
        this.mHandler = new MyHandler(context);
    }

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
        if (callback != null) {
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
                    long sessionId = crypto != null ? crypto.getOpId() : 0;
                    Trace.beginSection("FaceManager#authenticate");
                    this.mService.authenticate(this.mToken, sessionId, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Log.w(TAG, "Remote exception while authenticating: ", e);
                    callback.onAuthenticationError(1, getErrorString(this.mContext, 1, 0));
                } catch (Throwable th) {
                    Trace.endSection();
                    throw th;
                }
                Trace.endSection();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an authentication callback");
    }

    public void enroll(byte[] token, CancellationSignal cancel, EnrollmentCallback callback, int[] disabledFeatures) {
        if (callback != null) {
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
                    Trace.beginSection("FaceManager#enroll");
                    this.mService.enroll(this.mToken, token, this.mServiceReceiver, this.mContext.getOpPackageName(), disabledFeatures);
                } catch (RemoteException e) {
                    Log.w(TAG, "Remote exception in enroll: ", e);
                    callback.onEnrollmentError(1, getErrorString(this.mContext, 1, 0));
                } catch (Throwable th) {
                    Trace.endSection();
                    throw th;
                }
                Trace.endSection();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an enrollment callback");
    }

    public long generateChallenge() {
        IFaceService iFaceService = this.mService;
        if (iFaceService == null) {
            return 0;
        }
        try {
            return iFaceService.generateChallenge(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int revokeChallenge() {
        IFaceService iFaceService = this.mService;
        if (iFaceService == null) {
            return 0;
        }
        try {
            return iFaceService.revokeChallenge(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setFeature(int feature, boolean enabled, byte[] token, SetFeatureCallback callback) {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                this.mSetFeatureCallback = callback;
                iFaceService.setFeature(feature, enabled, token, this.mServiceReceiver);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void getFeature(int feature, GetFeatureCallback callback) {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                this.mGetFeatureCallback = callback;
                iFaceService.getFeature(feature, this.mServiceReceiver);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void userActivity() {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                iFaceService.userActivity();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public void setActiveUser(int userId) {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                iFaceService.setActiveUser(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void remove(Face face, int userId, RemovalCallback callback) {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFace = face;
                iFaceService.remove(this.mToken, face.getBiometricId(), userId, this.mServiceReceiver);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(face, 1, getErrorString(this.mContext, 1, 0));
                }
            }
        }
    }

    public List<Face> getEnrolledFaces(int userId) {
        IFaceService iFaceService = this.mService;
        if (iFaceService == null) {
            return null;
        }
        try {
            return iFaceService.getEnrolledFaces(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<Face> getEnrolledFaces() {
        return getEnrolledFaces(UserHandle.myUserId());
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public boolean hasEnrolledTemplates() {
        IFaceService iFaceService = this.mService;
        if (iFaceService == null) {
            return false;
        }
        try {
            return iFaceService.hasEnrolledFaces(UserHandle.myUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public boolean hasEnrolledTemplates(int userId) {
        IFaceService iFaceService = this.mService;
        if (iFaceService == null) {
            return false;
        }
        try {
            return iFaceService.hasEnrolledFaces(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public boolean isHardwareDetected() {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                return iFaceService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "isFaceHardwareDetected(): Service not connected!");
            return false;
        }
    }

    public long getAuthenticatorId() {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                return iFaceService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "getAuthenticatorId(): Service not connected!");
            return 0;
        }
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IBiometricServiceLockoutResetCallback.Stub() {
                    /* class android.hardware.face.FaceManager.AnonymousClass2 */

                    @Override // android.hardware.biometrics.IBiometricServiceLockoutResetCallback
                    public void onLockoutReset(long deviceId, IRemoteCallback serverCallback) throws RemoteException {
                        try {
                            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(1, "faceLockoutResetCallback");
                            wakeLock.acquire();
                            FaceManager.this.mHandler.post(new Runnable(wakeLock) {
                                /* class android.hardware.face.$$Lambda$FaceManager$2$IVmrd2VOH7JdDdb7PFFlL5bjZ5w */
                                private final /* synthetic */ PowerManager.WakeLock f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    FaceManager.AnonymousClass2.lambda$onLockoutReset$0(FaceManager.LockoutResetCallback.this, this.f$1);
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
            Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
        }
    }

    private int getCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelEnrollment() {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                iFaceService.cancelEnrollment(this.mToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAuthentication(CryptoObject cryptoObject) {
        IFaceService iFaceService = this.mService;
        if (iFaceService != null) {
            try {
                iFaceService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static String getErrorString(Context context, int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return context.getString(R.string.face_error_hw_not_available);
            case 2:
                return context.getString(R.string.face_error_unable_to_process);
            case 3:
                return context.getString(R.string.face_error_timeout);
            case 4:
                return context.getString(R.string.face_error_no_space);
            case 5:
                return context.getString(R.string.face_error_canceled);
            case 7:
                return context.getString(R.string.face_error_lockout);
            case 8:
                String[] msgArray = context.getResources().getStringArray(R.array.face_error_vendor);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 9:
                return context.getString(R.string.face_error_lockout_permanent);
            case 10:
                return context.getString(R.string.face_error_user_canceled);
            case 11:
                return context.getString(R.string.face_error_not_enrolled);
            case 12:
                return context.getString(R.string.face_error_hw_not_present);
        }
        Slog.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
        return null;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static String getAcquiredString(Context context, int acquireInfo, int vendorCode) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return context.getString(R.string.face_acquired_insufficient);
            case 2:
                return context.getString(R.string.face_acquired_too_bright);
            case 3:
                return context.getString(R.string.face_acquired_too_dark);
            case 4:
                return context.getString(R.string.face_acquired_too_close);
            case 5:
                return context.getString(R.string.face_acquired_too_far);
            case 6:
                return context.getString(R.string.face_acquired_too_high);
            case 7:
                return context.getString(R.string.face_acquired_too_low);
            case 8:
                return context.getString(R.string.face_acquired_too_right);
            case 9:
                return context.getString(R.string.face_acquired_too_left);
            case 10:
                return context.getString(R.string.face_acquired_poor_gaze);
            case 11:
                return context.getString(R.string.face_acquired_not_detected);
            case 12:
                return context.getString(R.string.face_acquired_too_much_motion);
            case 13:
                return context.getString(R.string.face_acquired_recalibrate);
            case 14:
                return context.getString(R.string.face_acquired_too_different);
            case 15:
                return context.getString(R.string.face_acquired_too_similar);
            case 16:
                return context.getString(R.string.face_acquired_pan_too_extreme);
            case 17:
                return context.getString(R.string.face_acquired_tilt_too_extreme);
            case 18:
                return context.getString(R.string.face_acquired_roll_too_extreme);
            case 19:
                return context.getString(R.string.face_acquired_obscured);
            case 20:
                return null;
            case 21:
                return context.getString(R.string.face_acquired_sensor_dirty);
            case 22:
                String[] msgArray = context.getResources().getStringArray(R.array.face_acquired_vendor);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
        }
        Slog.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
        return null;
    }

    public static int getMappedAcquiredInfo(int acquireInfo, int vendorCode) {
        if (acquireInfo == 22) {
            return vendorCode + 1000;
        }
        switch (acquireInfo) {
            case 0:
                return 0;
            case 1:
            case 2:
            case 3:
                return 2;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return 1;
            case 10:
            case 11:
            case 12:
            case 13:
                return 2;
            default:
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

    public static abstract class AuthenticationCallback extends BiometricAuthenticator.AuthenticationCallback {
        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationFailed() {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationAcquired(int acquireInfo) {
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

    public static abstract class RemovalCallback {
        public void onRemovalError(Face face, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Face face, int remaining) {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    private class OnEnrollCancelListener implements CancellationSignal.OnCancelListener {
        private OnEnrollCancelListener() {
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            FaceManager.this.cancelEnrollment();
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
            FaceManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Trace.beginSection("FaceManager#handleMessage: " + Integer.toString(msg.what));
            switch (msg.what) {
                case 100:
                    FaceManager.this.sendEnrollResult((Face) msg.obj, msg.arg1);
                    break;
                case 101:
                    FaceManager.this.sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    break;
                case 102:
                    FaceManager.this.sendAuthenticatedSucceeded((Face) msg.obj, msg.arg1);
                    break;
                case 103:
                    FaceManager.this.sendAuthenticatedFailed();
                    break;
                case 104:
                    FaceManager.this.sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    break;
                case 105:
                    FaceManager.this.sendRemovedResult((Face) msg.obj, msg.arg1);
                    break;
                case 106:
                    SomeArgs args = (SomeArgs) msg.obj;
                    FaceManager.this.sendGetFeatureCompleted(((Boolean) args.arg1).booleanValue(), args.argi1, ((Boolean) args.arg2).booleanValue());
                    args.recycle();
                    break;
                case 107:
                    FaceManager.this.sendSetFeatureCompleted(((Boolean) msg.obj).booleanValue(), msg.arg1);
                    break;
                default:
                    Log.w(FaceManager.TAG, "Unknown message: " + msg.what);
                    break;
            }
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSetFeatureCompleted(boolean success, int feature) {
        SetFeatureCallback setFeatureCallback = this.mSetFeatureCallback;
        if (setFeatureCallback != null) {
            setFeatureCallback.onCompleted(success, feature);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendGetFeatureCompleted(boolean success, int feature, boolean value) {
        GetFeatureCallback getFeatureCallback = this.mGetFeatureCallback;
        if (getFeatureCallback != null) {
            getFeatureCallback.onCompleted(success, feature, value);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRemovedResult(Face face, int remaining) {
        RemovalCallback removalCallback = this.mRemovalCallback;
        if (removalCallback != null) {
            if (face == null) {
                Log.e(TAG, "Received MSG_REMOVED, but face is null");
            } else {
                removalCallback.onRemovalSucceeded(face, remaining);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
        int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentError(clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
            return;
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationError(clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
            return;
        }
        RemovalCallback removalCallback = this.mRemovalCallback;
        if (removalCallback != null) {
            removalCallback.onRemovalError(this.mRemovalFace, clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnrollResult(Face face, int remaining) {
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentProgress(remaining);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthenticatedSucceeded(Face face, int userId) {
        if (this.mAuthenticationCallback != null) {
            this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(this.mCryptoObject, face, userId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthenticatedFailed() {
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationFailed();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationAcquired(acquireInfo);
        }
        String msg = getAcquiredString(this.mContext, acquireInfo, vendorCode);
        int clientInfo = acquireInfo == 22 ? vendorCode + 1000 : acquireInfo;
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentHelp(clientInfo, msg);
            return;
        }
        AuthenticationCallback authenticationCallback2 = this.mAuthenticationCallback;
        if (authenticationCallback2 != null && msg != null) {
            authenticationCallback2.onAuthenticationHelp(clientInfo, msg);
        }
    }
}
