package huawei.android.security.facerecognition;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.coauthservice.pool.SecureRegCallBack;
import com.huawei.hwpartsecurity.BuildConfig;
import huawei.android.security.facerecognition.IFaceRecognizeServiceEx;
import huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver;
import java.util.List;
import java.util.Locale;

public class FaceRecognizeManagerImpl {
    public static final int CODE_CALLBACK_ACQUIRE = 3;
    public static final int CODE_CALLBACK_BUSY = 4;
    public static final int CODE_CALLBACK_CANCEL = 2;
    public static final int CODE_CALLBACK_FACEID = 6;
    public static final int CODE_CALLBACK_OUT_OF_MEM = 5;
    public static final int CODE_CALLBACK_RESULT = 1;
    private static final int FACERECOGNITION_OFF = 0;
    private static final int FACERECOGNITION_ON = 1;
    private static final String FACERECOGNIZE_MANAGER = "FaceRecognizeManagerImpl";
    private static final String FACERECOGNIZE_SERVICE = "facerecognition";
    public static final int FACE_MODE_SUPPORT_MASK = -1;
    public static final int FACE_MODE_SUPPORT_POS = 5;
    public static final int FACE_RECONITION_SUPPORT_MASK = 1;
    public static final int FACE_RECONITION_SUPPORT_POS = 4;
    public static final int FACE_SECURE_LEVEL_MASK = 15;
    public static final int FACE_SECURE_LEVEL_POS = 0;
    public static final int FLAG_SHEATH = 1;
    private static final String FORMATE_STR = "****%04x";
    private static final int MASK_CODE = 65535;
    public static final int REQUEST_OK = 0;
    private static final String TAG = FaceRecognizeManagerImpl.class.getSimpleName();
    public static final int TYPE_CALLBACK_AUTH = 2;
    public static final int TYPE_CALLBACK_ENROLL = 1;
    public static final int TYPE_CALLBACK_REMOVE = 3;
    private static volatile Handler sHandler;
    private Context mContext;

    public interface AcquireInfo {
        public static final int FACE_UNLOCK_FACE_BAD_QUALITY = 4;
        public static final int FACE_UNLOCK_FACE_BLUR = 28;
        public static final int FACE_UNLOCK_FACE_DARKLIGHT = 30;
        public static final int FACE_UNLOCK_FACE_DARKPIC = 39;
        public static final int FACE_UNLOCK_FACE_DOWN = 18;
        public static final int FACE_UNLOCK_FACE_EYE_CLOSE = 22;
        public static final int FACE_UNLOCK_FACE_EYE_OCCLUSION = 21;
        public static final int FACE_UNLOCK_FACE_HALF_SHADOW = 32;
        public static final int FACE_UNLOCK_FACE_HIGHTLIGHT = 31;
        public static final int FACE_UNLOCK_FACE_KEEP = 19;
        public static final int FACE_UNLOCK_FACE_MOUTH_OCCLUSION = 23;
        public static final int FACE_UNLOCK_FACE_MULTI = 27;
        public static final int FACE_UNLOCK_FACE_NOT_COMPLETE = 29;
        public static final int FACE_UNLOCK_FACE_NOT_FOUND = 5;
        public static final int FACE_UNLOCK_FACE_OFFSET_BOTTOM = 11;
        public static final int FACE_UNLOCK_FACE_OFFSET_LEFT = 8;
        public static final int FACE_UNLOCK_FACE_OFFSET_RIGHT = 10;
        public static final int FACE_UNLOCK_FACE_OFFSET_TOP = 9;
        public static final int FACE_UNLOCK_FACE_RISE = 16;
        public static final int FACE_UNLOCK_FACE_ROTATED_LEFT = 15;
        public static final int FACE_UNLOCK_FACE_ROTATED_RIGHT = 17;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_LARGE = 7;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_SMALL = 6;
        public static final int FACE_UNLOCK_FAILURE = 3;
        public static final int FACE_UNLOCK_IMAGE_BLUR = 20;
        public static final int FACE_UNLOCK_INVALID_ARGUMENT = 1;
        public static final int FACE_UNLOCK_INVALID_HANDLE = 2;
        public static final int FACE_UNLOCK_LIVENESS_FAILURE = 14;
        public static final int FACE_UNLOCK_LIVENESS_WARNING = 13;
        public static final int FACE_UNLOCK_OK = 0;
        public static final int MG_UNLOCK_COMPARE_FAILURE = 12;
    }

    public interface FaceErrorCode {
        public static final int ALGORITHM_NOT_INIT = 5;
        public static final int BUSY = 13;
        public static final int CAMERA_FAIL = 12;
        public static final int CANCELED = 2;
        public static final int COMPARE_FAIL = 3;
        public static final int FAILED = 1;
        public static final int HAL_INVALIDE = 6;
        public static final int INVALID_PARAMETERS = 9;
        public static final int IN_LOCKOUT_MODE = 8;
        public static final int LOW_TEMP_CAP = 11;
        public static final int NO_FACE_DATA = 10;
        public static final int OVER_MAX_FACES = 7;
        public static final int SUCCESS = 0;
        public static final int TIMEOUT = 4;
        public static final int UNKNOWN = 100;
    }

    public static final class FaceInfo {
        public int faceId;
        public boolean hasAlternateAppearance;
        public String name;
    }

    public static final class FaceRecognitionAbility {
        public int faceMode;
        public boolean isFaceRecognitionSupport;
        public int reserve;
        public int secureLevel;
    }

    public interface FaceRecognizeCallback {
        void onCallbackEvent(int i, int i2, int i3, int i4);
    }

    static {
        SlogEx.d(TAG, "init the message handler!");
        HandlerThread handlerThread = new HandlerThread(FACERECOGNIZE_MANAGER);
        handlerThread.start();
        if (handlerThread.getLooper() != null) {
            sHandler = new MyHandler(handlerThread.getLooper());
        }
    }

    public FaceRecognizeManagerImpl(@NonNull Context context, @Nullable FaceRecognizeCallback callback) {
        SlogEx.d(TAG, "New FaceRecognize Sdk Instance");
        ServiceHolder.getInstance().init(context);
        CallbackHolder.getInstance().init(callback);
        this.mContext = context;
    }

    private static class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof Long)) {
                long reqId = ((Long) msg.obj).longValue();
                if (CallbackHolder.getInstance() != null) {
                    CallbackHolder.getInstance().onCallbackEvent(reqId, msg.what, msg.arg1, msg.arg2);
                }
            }
        }
    }

    public static class ServiceHolder implements IBinder.DeathRecipient {
        public static final int HAS_ALTERNATE_APPEARANCE = 1;
        public static final int NOT_HAVE_ALTERNATE_APPEARANCE = 0;
        public static final int OP_FAILED = -1;
        public static final int OP_OK = 0;
        public static final int RANDOM_FAILED = 0;
        private Context mContext;
        private final IFaceRecognizeServiceReceiver mReceiver;
        private IFaceRecognizeServiceEx mService;
        private final IBinder mToken;

        private ServiceHolder() {
            this.mToken = new Binder();
            this.mReceiver = new IFaceRecognizeServiceReceiver.Stub() {
                /* class huawei.android.security.facerecognition.FaceRecognizeManagerImpl.ServiceHolder.AnonymousClass1 */

                public void onEnrollResult(int faceId, int userId, int errorCode) throws RemoteException {
                    SlogEx.i(FaceRecognizeManagerImpl.TAG, "enroll result is errorCode");
                }

                public void onEnrollCancel() throws RemoteException {
                    SlogEx.i(FaceRecognizeManagerImpl.TAG, "enroll is canceld");
                }

                public void onEnrollAcquired(int acquiredInfo, int process) throws RemoteException {
                    String str = FaceRecognizeManagerImpl.TAG;
                    SlogEx.i(str, "enroll acquiredinfo is is " + acquiredInfo);
                }

                public void onAuthenticationResult(int userId, int errorCode) throws RemoteException {
                    String str = FaceRecognizeManagerImpl.TAG;
                    SlogEx.i(str, "authentication result is " + errorCode);
                }

                public void onAuthenticationCancel() throws RemoteException {
                    SlogEx.i(FaceRecognizeManagerImpl.TAG, "authentication is canceld");
                }

                public void onAuthenticationAcquired(int acquiredInfo) throws RemoteException {
                    String str = FaceRecognizeManagerImpl.TAG;
                    SlogEx.i(str, "authentication acquiredinfo is " + acquiredInfo);
                }

                public void onRemovedResult(int userId, int errorCode) throws RemoteException {
                    String str = FaceRecognizeManagerImpl.TAG;
                    SlogEx.i(str, "remove result: errorCode is " + errorCode);
                }

                public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
                    Message msg = Message.obtain(FaceRecognizeManagerImpl.sHandler, type, code, errorCode);
                    msg.obj = Long.valueOf(reqId);
                    msg.sendToTarget();
                }

                public void onCallbackResult(int reqId, int type, int code, int errorCode) throws RemoteException {
                    Message msg = Message.obtain(FaceRecognizeManagerImpl.sHandler, type, code, errorCode);
                    msg.obj = Long.valueOf((long) reqId);
                    msg.sendToTarget();
                }

                /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.security.facerecognition.FaceRecognizeManagerImpl$ServiceHolder$1 */
                /* JADX WARN: Multi-variable type inference failed */
                public IBinder asBinder() {
                    return this;
                }
            };
            getFRService();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init(Context context) {
            this.mContext = context;
        }

        public static ServiceHolder getInstance() {
            return SingletonInstance.instance;
        }

        /* access modifiers changed from: private */
        public static class SingletonInstance {
            private static final ServiceHolder instance = new ServiceHolder();

            private SingletonInstance() {
            }
        }

        public int authenticate(long opId, int flags) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                SlogEx.d("auth receiver", this.mReceiver.toString());
                service.authenticate(this.mToken, new AuthParam(flags, getCurrentUserId(), opId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int cancelAuthentication(long reqId) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                service.cancelAuthentication(this.mToken, new AuthParam(0, 0, reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            if (aaid == null) {
                aaid = new byte[0];
            }
            if (nonce == null) {
                nonce = new byte[0];
            }
            if (extra == null) {
                extra = new byte[0];
            }
            try {
                return service.preparePayInfo(aaid, nonce, extra);
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            if (faceId == null || token == null || tokenLen == null) {
                SlogEx.e("getPayResult", "param null");
                return -1;
            }
            if (reserve == null) {
                reserve = new byte[0];
            }
            try {
                return service.getPayResult(faceId, token, tokenLen, reserve);
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int enroll(byte[] authToken, int reqId, int flags, Surface preview) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                service.enroll(this.mToken, new EnrollParam(authToken, flags, getCurrentUserId(), (long) reqId, ContextEx.getOpPackageName(this.mContext)), preview, this.mReceiver);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int cancelEnrollment(int reqId) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                service.cancelEnrollment(this.mToken, new EnrollParam(new byte[0], 0, getCurrentUserId(), (long) reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int remove(int faceId, int reqId) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                service.remove(this.mToken, new RemoveParam(faceId, getCurrentUserId(), (long) reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int initAlgo() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.init(ContextEx.getOpPackageName(this.mContext));
            } catch (RemoteException e) {
                return -1;
            }
        }

        public int releaseAlgo() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.release(ContextEx.getOpPackageName(this.mContext));
            } catch (RemoteException e) {
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public int[] getEnrolledFaceRecognizes() {
            int[] ret = new int[0];
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return ret;
            }
            try {
                List<FaceRecognition> faces = service.getEnrolledFaceRecognizes(getCurrentUserId(), ContextEx.getOpPackageName(this.mContext));
                if (faces != null && !faces.isEmpty()) {
                    int size = faces.size();
                    ret = new int[size];
                    for (int i = 0; i < size; i++) {
                        ret[i] = faces.get(i).getFaceId();
                    }
                }
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
            }
            return ret;
        }

        /* access modifiers changed from: package-private */
        public FaceInfo getFaceInfo(int faceId) {
            boolean hasAlternateAppearance;
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return null;
            }
            try {
                List<FaceRecognition> faces = service.getEnrolledFaceRecognizes(getCurrentUserId(), ContextEx.getOpPackageName(this.mContext));
                if (faces != null) {
                    if (!faces.isEmpty()) {
                        CharSequence name = null;
                        int size = faces.size();
                        for (int i = 0; i < size; i++) {
                            if (faces.get(i).getFaceId() == faceId) {
                                name = faces.get(i).getName();
                            }
                        }
                        if (name == null) {
                            SlogEx.e(FaceRecognizeManagerImpl.TAG, "faceid not found in FaceRecognition");
                            return null;
                        }
                        int result = service.hasAlternateAppearance(faceId);
                        if (result == 1) {
                            hasAlternateAppearance = true;
                        } else if (result == 0) {
                            hasAlternateAppearance = false;
                        } else {
                            String str = FaceRecognizeManagerImpl.TAG;
                            SlogEx.e(str, "hasAlternateAppearance fail " + result);
                            return null;
                        }
                        FaceInfo faceInfo = new FaceInfo();
                        faceInfo.faceId = faceId;
                        faceInfo.hasAlternateAppearance = hasAlternateAppearance;
                        faceInfo.name = name.toString();
                        return faceInfo;
                    }
                }
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getEnrolledFaceRecognizes fail");
                return null;
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return null;
            }
        }

        public int rename(int faceId, String name) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return -1;
            }
            try {
                service.rename(faceId, getCurrentUserId(), name);
                return 0;
            } catch (RemoteException e) {
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public FaceRecognitionAbility getFaceRecognitionAbility() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return null;
            }
            try {
                int result = service.getHardwareSupportType();
                FaceRecognitionAbility ability = new FaceRecognitionAbility();
                ability.secureLevel = (result >>> 0) & 15;
                boolean z = true;
                if (((result >>> 4) & 1) != 1) {
                    z = false;
                }
                ability.isFaceRecognitionSupport = z;
                ability.faceMode = (result >>> 5) & -1;
                ability.reserve = 0;
                return ability;
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return null;
            }
        }

        /* access modifiers changed from: package-private */
        public int getHardwareSupportType() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return 0;
            }
            try {
                return service.getHardwareSupportType() >>> 4;
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return 0;
            }
        }

        /* access modifiers changed from: package-private */
        public int getAngleDim() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.getAngleDim();
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public long preEnroll() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return 0;
            }
            try {
                return service.preEnroll();
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return 0;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int setEnrollInfo(int[] enrollInfo) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.setEnrollInfo(enrollInfo);
            } catch (RemoteException ex) {
                String str = FaceRecognizeManagerImpl.TAG;
                SlogEx.e(str, "send the enroll info failed" + ex.getMessage());
                return -1;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int registerSecureRegCallBack(SecureRegCallBack callback) {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "service is null");
                return -1;
            }
            try {
                return service.registerSecureRegCallBack(callback);
            } catch (RemoteException ex) {
                String str = FaceRecognizeManagerImpl.TAG;
                SlogEx.e(str, "register SecureRegCallBack failed" + ex.getMessage());
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public long getAuthenticatorId() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return 0;
            }
            try {
                return service.getAuthenticatorId();
            } catch (RemoteException ex) {
                String str = FaceRecognizeManagerImpl.TAG;
                SlogEx.e(str, "get the authenticator id failed" + ex.getMessage());
                return 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void postEnroll() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service != null) {
                try {
                    service.postEnroll();
                } catch (RemoteException ex) {
                    SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void resetTimeout() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service != null) {
                try {
                    service.resetTimeout();
                } catch (RemoteException ex) {
                    SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public int getRemainingNum() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.getRemainingNum();
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public long getRemainingTime() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.getRemainingTime();
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public int getTotalAuthFailedTimes() {
            IFaceRecognizeServiceEx service = getFRService();
            if (service == null) {
                return -1;
            }
            try {
                return service.getTotalAuthFailedTimes();
            } catch (RemoteException ex) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, ex.getMessage());
                return -1;
            }
        }

        public void binderDied() {
            SlogEx.e(FaceRecognizeManagerImpl.TAG, "FaceService died");
            synchronized (this) {
                this.mService = null;
            }
        }

        private int getCurrentUserId() {
            return 0;
        }

        private synchronized IFaceRecognizeServiceEx getFRService() {
            if (this.mService == null) {
                IBinder binder = ServiceManagerEx.getService(FaceRecognizeManagerImpl.FACERECOGNIZE_SERVICE);
                if (binder == null) {
                    SlogEx.w(FaceRecognizeManagerImpl.TAG, "getService binder null");
                    return null;
                }
                this.mService = IFaceRecognizeServiceEx.Stub.asInterface(binder);
                if (this.mService == null) {
                    SlogEx.w(FaceRecognizeManagerImpl.TAG, "getService Service null");
                } else {
                    try {
                        this.mService.asBinder().linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        String str = FaceRecognizeManagerImpl.TAG;
                        SlogEx.w(str, "failed to linkToDeath" + e.getMessage());
                    }
                }
            }
            return this.mService;
        }
    }

    public static class CallbackHolder {
        private FaceRecognizeCallback mCallback;

        public static CallbackHolder getInstance() {
            return SingletonInstance.instance;
        }

        public void init(FaceRecognizeCallback callback) {
            this.mCallback = callback;
        }

        public void onCallbackEvent(long reqId, int type, int code, int errorCode) {
            String str = FaceRecognizeManagerImpl.TAG;
            SlogEx.i(str, "reqId(" + printRequestId(reqId) + "), type(" + FaceRecognizeManagerImpl.getTypeString(type) + "), code(" + FaceRecognizeManagerImpl.getCodeString(code) + "), result(" + FaceRecognizeManagerImpl.getErrorCodeString(code, errorCode) + ")");
            if (this.mCallback != null) {
                if (type == 2 && code == 1 && errorCode == 0) {
                    long current = System.nanoTime();
                    SlogEx.d("PerformanceTime", "Time 6. Authenticate Success --- " + current);
                }
                this.mCallback.onCallbackEvent((int) reqId, type, code, errorCode);
                return;
            }
            SlogEx.w(FaceRecognizeManagerImpl.TAG, "callback is null, construct FaceRecognizeManager with correct Callback!");
        }

        private String printRequestId(long id) {
            return String.format(Locale.ROOT, FaceRecognizeManagerImpl.FORMATE_STR, Long.valueOf(65535 & id));
        }

        /* access modifiers changed from: private */
        public static class SingletonInstance {
            private static final CallbackHolder instance = new CallbackHolder();

            private SingletonInstance() {
            }
        }
    }

    public int authenticate(long opId, int flags, @Nullable Surface preview) {
        SlogEx.d("PerformanceTime", "Time 1. start auth --- " + System.nanoTime());
        return ServiceHolder.getInstance().authenticate(opId, flags);
    }

    public int cancelAuthenticate(long opId) {
        return ServiceHolder.getInstance().cancelAuthentication(opId);
    }

    public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
        return ServiceHolder.getInstance().preparePayInfo(aaid, nonce, extra);
    }

    public int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
        return ServiceHolder.getInstance().getPayResult(faceId, token, tokenLen, reserve);
    }

    public int enroll(int reqId, int flags, byte[] token, @Nullable Surface preview) {
        SlogEx.d("PerformanceTime", "Time 1 start enroll --- " + System.nanoTime());
        return ServiceHolder.getInstance().enroll(token, reqId, flags, preview);
    }

    public int cancelEnroll(int reqId) {
        return ServiceHolder.getInstance().cancelEnrollment(reqId);
    }

    public long preEnroll() {
        return ServiceHolder.getInstance().preEnroll();
    }

    public int setEnrollInfo(int[] enrollInfo) {
        return ServiceHolder.getInstance().setEnrollInfo(enrollInfo);
    }

    public long getAuthenticatorId() {
        return ServiceHolder.getInstance().getAuthenticatorId();
    }

    public int registerSecureRegCallBack(SecureRegCallBack callback) {
        return ServiceHolder.getInstance().registerSecureRegCallBack(callback);
    }

    public void postEnroll() {
        ServiceHolder.getInstance().postEnroll();
    }

    public int remove(int reqId, int faceId) {
        String str = TAG;
        SlogEx.d(str, "start remove: faceId = " + faceId);
        return ServiceHolder.getInstance().remove(faceId, reqId);
    }

    public int init() {
        return ServiceHolder.getInstance().initAlgo();
    }

    public int release() {
        return ServiceHolder.getInstance().releaseAlgo();
    }

    public int[] getEnrolledFaceIDs() {
        return ServiceHolder.getInstance().getEnrolledFaceRecognizes();
    }

    public FaceInfo getFaceInfo(int faceId) {
        return ServiceHolder.getInstance().getFaceInfo(faceId);
    }

    public int rename(int faceId, String name) {
        return ServiceHolder.getInstance().rename(faceId, name);
    }

    public FaceRecognitionAbility getFaceRecognitionAbility() {
        return ServiceHolder.getInstance().getFaceRecognitionAbility();
    }

    public int getHardwareSupportType() {
        return ServiceHolder.getInstance().getHardwareSupportType();
    }

    public int getAngleDim() {
        return ServiceHolder.getInstance().getAngleDim();
    }

    public void resetTimeout() {
        ServiceHolder.getInstance().resetTimeout();
    }

    public int getRemainingNum() {
        return ServiceHolder.getInstance().getRemainingNum();
    }

    public long getRemainingTime() {
        return ServiceHolder.getInstance().getRemainingTime();
    }

    public int getTotalAuthFailedTimes() {
        return ServiceHolder.getInstance().getTotalAuthFailedTimes();
    }

    public static String getTypeString(int type) {
        if (type == 1) {
            return "ENROLL";
        }
        if (type == 2) {
            return "AUTH";
        }
        if (type == 3) {
            return "REMOVE";
        }
        return BuildConfig.FLAVOR + type;
    }

    public static String getCodeString(int code) {
        if (code == 1) {
            return "result";
        }
        if (code == 2) {
            return "cancel";
        }
        if (code == 3) {
            return "acquire";
        }
        if (code == 4) {
            return "request busy";
        }
        if (code == 6) {
            return "faceId";
        }
        return BuildConfig.FLAVOR + code;
    }

    public static String getErrorCodeString(int code, int errorCode) {
        if (code == 1) {
            switch (errorCode) {
                case 0:
                    return "success";
                case 1:
                    return "failed";
                case 2:
                    return "cancelled";
                case 3:
                    return "compare fail";
                case 4:
                    return "time out";
                case 5:
                    return "invoke init first";
                case 6:
                    return "hal invalid";
                case 7:
                    return "over max faces";
                case 8:
                    return "in lockout mode";
                case 9:
                    return "invalid parameters";
                case 10:
                    return "no face data";
                case 11:
                    return "low temp & cap";
                case 12:
                    return "camera fail";
                case 13:
                    return "busy";
            }
        } else if (code == 3) {
            switch (errorCode) {
                case 4:
                    return "bad quality";
                case 5:
                    return "no face detected";
                case 6:
                    return "face too small";
                case 7:
                    return "face too large";
                case 8:
                    return "offset left";
                case 9:
                    return "offset top";
                case 10:
                    return "offset right";
                case 11:
                    return "offset bottom";
                case 13:
                    return "aliveness warning";
                case 14:
                    return "aliveness failure";
                case 15:
                    return "rotate left";
                case 16:
                    return "face rise to high";
                case 17:
                    return "rotate right";
                case 18:
                    return "face too low";
                case 19:
                    return "keep still";
                case 21:
                    return "eyes occlusion";
                case 22:
                    return "eyes closed";
                case 23:
                    return "mouth occlusion";
                case 27:
                    return "multi faces";
                case 28:
                    return "face blur";
                case 29:
                    return "face not complete";
                case 30:
                    return "too dark";
                case 31:
                    return "too light";
                case 32:
                    return "half shadow";
                case 39:
                    return "picture too dark";
            }
        }
        return BuildConfig.FLAVOR + errorCode;
    }
}
