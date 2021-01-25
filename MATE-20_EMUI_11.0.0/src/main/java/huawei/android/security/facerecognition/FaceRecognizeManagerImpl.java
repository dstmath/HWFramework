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
import com.huawei.android.iawareperf.UniPerfEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartsecurityfaceid.BuildConfig;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.IFaceRecognizeServiceEx;
import huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver;
import java.util.List;
import java.util.Locale;

public class FaceRecognizeManagerImpl extends DefaultFaceRecognizeManagerImpl {
    private static final int FACERECOGNITION_OFF = 0;
    private static final int FACERECOGNITION_ON = 1;
    private static final String FACERECOGNIZE_MANAGER = "FaceRecognizeManagerImpl";
    private static final String FACERECOGNIZE_SERVICE = "facerecognition";
    private static final String FORMATE_STR = "****%04x";
    private static final int MASK_CODE = 65535;
    private static final String TAG = FaceRecognizeManagerImpl.class.getSimpleName();
    private static final int UNIPERF_EVENT_CMDID = 13252;
    private static volatile Handler sHandler;
    private Context mContext;

    static {
        SlogEx.d(TAG, "init the message handler!");
        HandlerThread handlerThread = new HandlerThread(FACERECOGNIZE_MANAGER);
        handlerThread.start();
        if (handlerThread.getLooper() != null) {
            sHandler = new FaceRecognizeHandler(handlerThread.getLooper());
        }
    }

    public FaceRecognizeManagerImpl(@NonNull Context context, @Nullable DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback callback) {
        super(context, callback);
        SlogEx.d(TAG, "New FaceRecognize Sdk Instance");
        ServiceHolder.getInstance().init(context);
        CallbackHolder.getInstance().init(callback);
        this.mContext = context;
    }

    private static class FaceRecognizeHandler extends Handler {
        private FaceRecognizeHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof Long)) {
                long reqId = ((Long) msg.obj).longValue();
                if (CallbackHolder.getInstance() != null) {
                    CallbackHolder.getInstance().onCallbackEvent(reqId, msg.what, msg.arg1, msg.arg2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ServiceHolder implements IBinder.DeathRecipient {
        private static final int CURRENT_USER_ID = 0;
        private static final int HAS_ALTERNATE_APPEARANCE = 1;
        private static final int NOT_HAVE_ALTERNATE_APPEARANCE = 0;
        private static final int OP_FAILED = -1;
        private static final int OP_OK = 0;
        private static final int RANDOM_FAILED = 0;
        private Context mContext;
        private final IFaceRecognizeServiceReceiver mReceiver;
        private IFaceRecognizeServiceEx mService;
        private final IBinder mToken;

        private ServiceHolder() {
            this.mToken = new Binder();
            this.mReceiver = new IFaceRecognizeServiceReceiver.Stub() {
                /* class huawei.android.security.facerecognition.FaceRecognizeManagerImpl.ServiceHolder.AnonymousClass1 */

                public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
                    Message msg = Message.obtain(FaceRecognizeManagerImpl.sHandler, type, code, errorCode);
                    msg.obj = Long.valueOf(reqId);
                    msg.sendToTarget();
                }

                /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.security.facerecognition.FaceRecognizeManagerImpl$ServiceHolder$1 */
                /* JADX WARN: Multi-variable type inference failed */
                public IBinder asBinder() {
                    return this;
                }
            };
            getFaceRecognizeService();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: private */
        public static ServiceHolder getInstance() {
            return SingletonInstance.INSTANCE;
        }

        /* access modifiers changed from: private */
        public static class SingletonInstance {
            private static final ServiceHolder INSTANCE = new ServiceHolder();

            private SingletonInstance() {
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int authenticate(long opId, int flags) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                SlogEx.d("auth receiver", this.mReceiver.toString());
                service.authenticate(this.mToken, new AuthParam(flags, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, opId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int cancelAuthentication(long reqId) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                service.cancelAuthentication(this.mToken, new AuthParam((int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            if (aaid == null) {
                aaid = new byte[FaceRecognizeManagerImpl.FACERECOGNITION_OFF];
            }
            if (nonce == null) {
                nonce = new byte[FaceRecognizeManagerImpl.FACERECOGNITION_OFF];
            }
            if (extra == null) {
                extra = new byte[FaceRecognizeManagerImpl.FACERECOGNITION_OFF];
            }
            try {
                return service.preparePayInfo(aaid, nonce, extra);
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            if (faceId == null || token == null || tokenLen == null) {
                SlogEx.e("getPayResult", "param null");
                return OP_FAILED;
            }
            if (reserve == null) {
                reserve = new byte[FaceRecognizeManagerImpl.FACERECOGNITION_OFF];
            }
            try {
                return service.getPayResult(faceId, token, tokenLen, reserve);
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int enroll(byte[] authToken, int reqId, int flags, Surface preview) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                service.enroll(this.mToken, new EnrollParam(authToken, flags, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, (long) reqId, ContextEx.getOpPackageName(this.mContext)), preview, this.mReceiver);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int cancelEnrollment(int reqId) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                service.cancelEnrollment(this.mToken, new EnrollParam(new byte[FaceRecognizeManagerImpl.FACERECOGNITION_OFF], (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, (long) reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int remove(int faceId, int reqId) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                service.remove(this.mToken, new RemoveParam(faceId, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, (long) reqId, ContextEx.getOpPackageName(this.mContext)), this.mReceiver);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int initAlgo() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.init(ContextEx.getOpPackageName(this.mContext));
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int releaseAlgo() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.release(ContextEx.getOpPackageName(this.mContext));
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int[] getEnrolledFaceRecognizes() {
            int[] ret = new int[FaceRecognizeManagerImpl.FACERECOGNITION_OFF];
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return ret;
            }
            try {
                List<FaceRecognition> faces = service.getEnrolledFaceRecognizes((int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, ContextEx.getOpPackageName(this.mContext));
                if (faces != null && !faces.isEmpty()) {
                    int size = faces.size();
                    ret = new int[size];
                    for (int i = FaceRecognizeManagerImpl.FACERECOGNITION_OFF; i < size; i++) {
                        ret[i] = faces.get(i).getFaceId();
                    }
                }
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getEnrolledFaceRecognizes occurs error.");
            }
            return ret;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private DefaultFaceRecognizeManagerImpl.FaceInfo getFaceInfo(int faceId) {
            boolean hasAlternateAppearance;
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return null;
            }
            try {
                List<FaceRecognition> faces = service.getEnrolledFaceRecognizes((int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, ContextEx.getOpPackageName(this.mContext));
                if (faces != null) {
                    if (!faces.isEmpty()) {
                        CharSequence name = null;
                        int size = faces.size();
                        for (int i = FaceRecognizeManagerImpl.FACERECOGNITION_OFF; i < size; i++) {
                            if (faces.get(i).getFaceId() == faceId) {
                                name = faces.get(i).getName();
                            }
                        }
                        if (name == null) {
                            SlogEx.e(FaceRecognizeManagerImpl.TAG, "faceId not found in FaceRecognition");
                            return null;
                        }
                        int result = service.hasAlternateAppearance(faceId);
                        if (result == 1) {
                            hasAlternateAppearance = true;
                        } else if (result == 0) {
                            hasAlternateAppearance = false;
                        } else {
                            DefaultFaceRecognizeManagerImpl.FaceInfo faceInfo = FaceRecognizeManagerImpl.TAG;
                            SlogEx.e(faceInfo, "hasAlternateAppearance fail " + result);
                            return null;
                        }
                        DefaultFaceRecognizeManagerImpl.FaceInfo faceInfo2 = new DefaultFaceRecognizeManagerImpl.FaceInfo();
                        faceInfo2.faceId = faceId;
                        faceInfo2.hasAlternateAppearance = hasAlternateAppearance;
                        faceInfo2.name = name.toString();
                        return faceInfo2;
                    }
                }
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getEnrolledFaceRecognizes fail");
                return null;
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "hasAlternateAppearance occuers error.");
                return null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int rename(int faceId, String name) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return OP_FAILED;
            }
            try {
                service.rename(faceId, (int) FaceRecognizeManagerImpl.FACERECOGNITION_OFF, name);
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            } catch (RemoteException e) {
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility getFaceRecognitionAbility() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get face recognize service fail");
                return null;
            }
            try {
                int result = service.getHardwareSupportType();
                DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility ability = new DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility();
                ability.secureLevel = (result >>> FaceRecognizeManagerImpl.FACERECOGNITION_OFF) & 15;
                boolean z = true;
                if (((result >>> 4) & 1) != 1) {
                    z = FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
                }
                ability.isFaceRecognitionSupport = z;
                ability.faceMode = (result >>> 5) & OP_FAILED;
                ability.reserve = FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
                return ability;
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getFaceRecognitionAbility occuers error.");
                return null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getHardwareSupportType() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            }
            try {
                return service.getHardwareSupportType() >>> 4;
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getHardwareSupportType occuers error.");
                return FaceRecognizeManagerImpl.FACERECOGNITION_OFF;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getAngleDim() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.getAngleDim();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getAngleDim occuers error.");
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long preEnroll() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return 0;
            }
            try {
                return service.preEnroll();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "preEnroll occuers error.");
                return 0;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int setEnrollInfo(int[] enrollInfo) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.setEnrollInfo(enrollInfo);
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "send the enroll info failed.");
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int registerSecureRegistryCallback(IBinder callback) {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "service is null");
                return OP_FAILED;
            }
            try {
                return service.registerSecureRegistryCallback(callback);
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "register SecureRegistryCallback failed.");
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getAuthenticatorId() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return 0;
            }
            try {
                return service.getAuthenticatorId();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "get the authenticator id failed.");
                return 0;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void postEnroll() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service != null) {
                try {
                    service.postEnroll();
                } catch (RemoteException e) {
                    SlogEx.e(FaceRecognizeManagerImpl.TAG, "postEnroll failed.");
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetTimeout() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service != null) {
                try {
                    service.resetTimeout();
                } catch (RemoteException e) {
                    SlogEx.e(FaceRecognizeManagerImpl.TAG, "resetTimeout failed.");
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getRemainingNum() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.getRemainingNum();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getRemainingNum failed.");
                return OP_FAILED;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getRemainingTime() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return -1;
            }
            try {
                return service.getRemainingTime();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getRemainingTime failed.");
                return -1;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getTotalAuthFailedTimes() {
            IFaceRecognizeServiceEx service = getFaceRecognizeService();
            if (service == null) {
                return OP_FAILED;
            }
            try {
                return service.getTotalAuthFailedTimes();
            } catch (RemoteException e) {
                SlogEx.e(FaceRecognizeManagerImpl.TAG, "getTotalAuthFailedTimes failed.");
                return OP_FAILED;
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            SlogEx.e(FaceRecognizeManagerImpl.TAG, "FaceService died");
            synchronized (this) {
                this.mService = null;
            }
        }

        private synchronized IFaceRecognizeServiceEx getFaceRecognizeService() {
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
                        this.mService.asBinder().linkToDeath(this, FaceRecognizeManagerImpl.FACERECOGNITION_OFF);
                    } catch (RemoteException e) {
                        SlogEx.w(FaceRecognizeManagerImpl.TAG, "failed to linkToDeath.");
                    }
                }
            }
            return this.mService;
        }
    }

    /* access modifiers changed from: private */
    public static class CallbackHolder {
        private DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback mCallback;

        private CallbackHolder() {
        }

        /* access modifiers changed from: private */
        public static CallbackHolder getInstance() {
            return SingletonInstance.INSTANCE;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init(DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback callback) {
            this.mCallback = callback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onCallbackEvent(long reqId, int type, int code, int errorCode) {
            String str = FaceRecognizeManagerImpl.TAG;
            SlogEx.i(str, "reqId(" + FaceRecognizeManagerImpl.printFormattedId(reqId) + "), type(" + DefaultFaceRecognizeManagerImpl.getTypeString(type) + "), code(" + DefaultFaceRecognizeManagerImpl.getCodeString(code) + "), result(" + DefaultFaceRecognizeManagerImpl.getErrorCodeString(code, errorCode) + ")");
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

        /* access modifiers changed from: private */
        public static class SingletonInstance {
            private static final CallbackHolder INSTANCE = new CallbackHolder();

            private SingletonInstance() {
            }
        }
    }

    public int authenticate(long opId, int flags, @Nullable Surface preview) {
        SlogEx.d("PerformanceTime", "Time 1. start auth --- " + System.nanoTime());
        UniPerfEx.getInstance().uniPerfEvent((int) UNIPERF_EVENT_CMDID, BuildConfig.FLAVOR, new int[FACERECOGNITION_OFF]);
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

    public int registerSecureRegistryCallback(IBinder callback) {
        return ServiceHolder.getInstance().registerSecureRegistryCallback(callback);
    }

    public void postEnroll() {
        ServiceHolder.getInstance().postEnroll();
    }

    public int remove(int reqId, int faceId) {
        String str = TAG;
        SlogEx.d(str, "start remove: faceId = " + printFormattedId((long) faceId));
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

    public DefaultFaceRecognizeManagerImpl.FaceInfo getFaceInfo(int faceId) {
        return ServiceHolder.getInstance().getFaceInfo(faceId);
    }

    public int rename(int faceId, String name) {
        return ServiceHolder.getInstance().rename(faceId, name);
    }

    public DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility getFaceRecognitionAbility() {
        return ServiceHolder.getInstance().getFaceRecognitionAbility();
    }

    public int getHardwareSupportType() {
        return ServiceHolder.getInstance().getHardwareSupportType();
    }

    public int getAngleDim() {
        return ServiceHolder.getInstance().getAngleDim();
    }

    public void setCallback(DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback callback) {
        SlogEx.i(TAG, "setCallback: init callback");
        CallbackHolder.getInstance().init(callback);
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

    /* access modifiers changed from: private */
    public static String printFormattedId(long id) {
        return String.format(Locale.ROOT, FORMATE_STR, Long.valueOf(65535 & id));
    }
}
