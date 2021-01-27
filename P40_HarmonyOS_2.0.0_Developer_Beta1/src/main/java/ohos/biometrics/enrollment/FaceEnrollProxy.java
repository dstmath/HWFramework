package ohos.biometrics.enrollment;

import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.FaceRecognizeManagerFactory;
import huawei.hiview.HiView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import ohos.agp.components.surfaceview.adapter.SurfaceUtils;
import ohos.agp.graphics.Surface;
import ohos.app.Context;
import ohos.biometrics.enrollment.BiometricEnroll;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class FaceEnrollProxy implements IFaceEnroll {
    private static final int CODE_CALLBACK_ACQUIRE = 3;
    private static final int CODE_CALLBACK_CANCEL = 2;
    private static final int CODE_CALLBACK_FACEID = 6;
    private static final int CODE_CALLBACK_RESULT = 1;
    private static final Object FACEID_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113024, "FaceEnrollProxy");
    private static final int MASK_CODE = 65535;
    private static final int TYPE_CALLBACK_AUTH = 2;
    private static final int TYPE_CALLBACK_ENROLL = 1;
    private static final int TYPE_CALLBACK_REMOVE = 3;
    private final DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback mCallback = new DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback() {
        /* class ohos.biometrics.enrollment.FaceEnrollProxy.AnonymousClass1 */

        public void onCallbackEvent(int i, int i2, int i3, int i4) {
            if (i == FaceEnrollProxy.this.mReqId && i2 == 1) {
                FaceEnrollProxy.this.setEnrolledTips(i3, i4);
                if (i3 == 1 && FaceEnrollProxy.this.mSignal != null) {
                    HiLog.info(FaceEnrollProxy.LABEL, "get enroll result from callback event! errorCode = %{public}d", new Object[]{Integer.valueOf(i4)});
                    FaceEnrollProxy.this.mResult = i4;
                    FaceEnrollProxy.this.mSignal.countDown();
                }
                if (i3 == 6) {
                    HiLog.info(FaceEnrollProxy.LABEL, "get enroll face id from callback event! faceid = ****%{public}04x", new Object[]{Integer.valueOf(65535 & i4)});
                    FaceEnrollProxy.this.setEnrolledFaceId(i4);
                    return;
                }
                return;
            }
            HiLog.info(FaceEnrollProxy.LABEL, "callback event ignored: reqId = %{public}d, type = %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        }
    };
    private final BiometricEnroll.EnrolledTips mEnrolledTips = new BiometricEnroll.EnrolledTips();
    private volatile int mFaceId = -1;
    private final DefaultFaceRecognizeManagerImpl mFaceManagerImpl;
    private volatile int mReqId = 1;
    private volatile int mResult = 1;
    private volatile CountDownLatch mSignal = null;

    FaceEnrollProxy(Context context) {
        if (context == null) {
            this.mFaceManagerImpl = null;
            HiLog.error(LABEL, "FaceEnrollProxy structure failed! context error", new Object[0]);
            return;
        }
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            this.mFaceManagerImpl = null;
            HiLog.error(LABEL, "FaceEnrollProxy structure failed! getHostContext error!", new Object[0]);
            return;
        }
        this.mFaceManagerImpl = FaceRecognizeManagerFactory.getInstance().getFaceRecognizeManagerImpl((android.content.Context) hostContext, this.mCallback);
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public BiometricEnroll.EnrollInitResult initEnroll() {
        BiometricEnroll.EnrollInitResult enrollInitResult = new BiometricEnroll.EnrollInitResult();
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null) {
            HiLog.error(LABEL, "initEnroll failed: mFaceManagerImpl null", new Object[0]);
            return enrollInitResult;
        }
        int init = defaultFaceRecognizeManagerImpl.init();
        if (init != 0) {
            HiLog.error(LABEL, "initAlgo failed: %{public}d", new Object[]{Integer.valueOf(init)});
            return enrollInitResult;
        }
        long preEnroll = this.mFaceManagerImpl.preEnroll();
        if (preEnroll == 0) {
            HiLog.error(LABEL, "preEnroll failed!", new Object[0]);
            return enrollInitResult;
        }
        enrollInitResult.setSessionId((long) this.mReqId);
        enrollInitResult.setChallenge(preEnroll);
        enrollInitResult.setErrorCode(0);
        return enrollInitResult;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int execEnroll(byte[] bArr, long j, int i, Surface surface) {
        android.view.Surface surface2;
        if (this.mFaceManagerImpl == null) {
            HiLog.error(LABEL, "execEnroll failed: mFaceManagerImpl null", new Object[0]);
            return -1;
        }
        int i2 = this.mReqId;
        if (((int) j) != i2) {
            HiLog.error(LABEL, "sessionId error: %{public}d", new Object[]{Long.valueOf(j)});
            return -1;
        }
        long currentTimeMillis = System.currentTimeMillis();
        int length = this.mFaceManagerImpl.getEnrolledFaceIDs().length;
        this.mResult = 1;
        this.mFaceId = -1;
        this.mSignal = new CountDownLatch(1);
        this.mFaceManagerImpl.setCallback(this.mCallback);
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (surface == null) {
            surface2 = null;
        } else {
            surface2 = SurfaceUtils.getSurfaceImpl(surface);
        }
        int enroll = defaultFaceRecognizeManagerImpl.enroll(i2, i, bArr, surface2);
        if (enroll != 0) {
            HiLog.error(LABEL, "mFaceManagerImpl.enroll failed: %{public}d", new Object[]{Integer.valueOf(enroll)});
            return -1;
        }
        try {
            this.mSignal.await();
        } catch (InterruptedException unused) {
            HiLog.error(LABEL, "execEnroll await failed", new Object[0]);
        }
        this.mReqId = i2 + 1;
        HiView.report(HiView.byVariadic(940101020, HiView.PayloadMode.KVPairs, new Object[]{"biometric_type", Integer.valueOf(BiometricEnroll.BiometricType.BIOMETRICTYPE_FACE.ordinal()), "enroll_result", Integer.valueOf(this.mResult), "run_time", Long.valueOf(System.currentTimeMillis() - currentTimeMillis), "is_enrolling_again", Integer.valueOf(length != 0 ? 1 : 0)}));
        HiLog.info(LABEL, "execEnroll end: result = %{public}d", new Object[]{Integer.valueOf(this.mResult)});
        return this.mResult;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int finishEnroll() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null) {
            HiLog.error(LABEL, "finishEnroll failed: mFaceManagerImpl null", new Object[0]);
            return -1;
        }
        defaultFaceRecognizeManagerImpl.postEnroll();
        int release = this.mFaceManagerImpl.release();
        if (release == 0) {
            return 0;
        }
        HiLog.error(LABEL, "releaseAlgo failed: %{public}d", new Object[]{Integer.valueOf(release)});
        return -1;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int cancelEnroll() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl != null) {
            return defaultFaceRecognizeManagerImpl.cancelEnroll(this.mReqId);
        }
        HiLog.error(LABEL, "cancelEnroll failed: mFaceManagerImpl null", new Object[0]);
        return -1;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int removeEnrolledInfo(int i) {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl != null) {
            return defaultFaceRecognizeManagerImpl.remove(this.mReqId, i);
        }
        HiLog.error(LABEL, "removeEnrolledInfo failed: mFaceManagerImpl null", new Object[0]);
        return -1;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public List<BiometricEnroll.EnrollInfo> getEnrolledInfo() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null) {
            HiLog.error(LABEL, "getEnrolledInfo failed: mFaceManagerImpl null", new Object[0]);
            return new ArrayList(0);
        }
        int[] enrolledFaceIDs = defaultFaceRecognizeManagerImpl.getEnrolledFaceIDs();
        ArrayList arrayList = new ArrayList(enrolledFaceIDs.length);
        for (int i : enrolledFaceIDs) {
            DefaultFaceRecognizeManagerImpl.FaceInfo faceInfo = this.mFaceManagerImpl.getFaceInfo(i);
            if (faceInfo != null) {
                BiometricEnroll.EnrollInfo enrollInfo = new BiometricEnroll.EnrollInfo();
                enrollInfo.setBiometricId(faceInfo.faceId);
                enrollInfo.setHasAlternateData(faceInfo.hasAlternateAppearance);
                enrollInfo.setName(faceInfo.name);
                arrayList.add(enrollInfo);
            } else {
                HiLog.error(LABEL, "getFaceInfo null! faceId = %{public}d", new Object[]{Integer.valueOf(i)});
            }
        }
        return arrayList;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int renameEnrolledInfo(int i, String str) {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl != null) {
            return defaultFaceRecognizeManagerImpl.rename(i, str);
        }
        HiLog.error(LABEL, "renameEnrolledInfo failed: mFaceManagerImpl null", new Object[0]);
        return -1;
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public BiometricEnroll.EnrolledTips getEnrolledTips() {
        BiometricEnroll.EnrolledTips enrolledTips;
        synchronized (this.mEnrolledTips) {
            enrolledTips = new BiometricEnroll.EnrolledTips();
            enrolledTips.setErrorCode(this.mEnrolledTips.getErrorCode());
            enrolledTips.setTipEvent(this.mEnrolledTips.getTipEvent());
            enrolledTips.setTipValue(this.mEnrolledTips.getTipValue());
            enrolledTips.setTipInfo(this.mEnrolledTips.getTipInfo());
        }
        return enrolledTips;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setEnrolledTips(int i, int i2) {
        synchronized (this.mEnrolledTips) {
            this.mEnrolledTips.setErrorCode(0);
            this.mEnrolledTips.setTipEvent(i);
            this.mEnrolledTips.setTipValue(i2);
            this.mEnrolledTips.setTipInfo(DefaultFaceRecognizeManagerImpl.getErrorCodeString(i, i2));
        }
    }

    @Override // ohos.biometrics.enrollment.IFaceEnroll
    public int getEnrolledFaceId() {
        int i;
        if (this.mResult != 0) {
            return -1;
        }
        synchronized (FACEID_LOCK) {
            i = this.mFaceId;
        }
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setEnrolledFaceId(int i) {
        synchronized (FACEID_LOCK) {
            this.mFaceId = i;
        }
    }
}
