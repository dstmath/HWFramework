package ohos.biometrics.enrollment;

import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.app.Context;
import ohos.biometrics.enrollment.BiometricEnroll;

class FaceEnroll {
    private static volatile FaceEnroll sInstance;
    private final IFaceEnroll mProxy;

    private FaceEnroll(Context context) {
        this.mProxy = new FaceEnrollProxy(context);
    }

    static FaceEnroll getInstance(Context context) {
        if (sInstance == null) {
            synchronized (FaceEnroll.class) {
                if (sInstance == null) {
                    sInstance = new FaceEnroll(context);
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    public BiometricEnroll.EnrollInitResult initEnroll() {
        return this.mProxy.initEnroll();
    }

    /* access modifiers changed from: package-private */
    public int execEnroll(byte[] bArr, long j, int i, Surface surface) {
        return this.mProxy.execEnroll(bArr, j, i, surface);
    }

    /* access modifiers changed from: package-private */
    public int finishEnroll() {
        return this.mProxy.finishEnroll();
    }

    /* access modifiers changed from: package-private */
    public int cancelEnroll() {
        return this.mProxy.cancelEnroll();
    }

    /* access modifiers changed from: package-private */
    public int removeEnrolledInfo(int i) {
        return this.mProxy.removeEnrolledInfo(i);
    }

    /* access modifiers changed from: package-private */
    public List<BiometricEnroll.EnrollInfo> getEnrolledInfo() {
        return this.mProxy.getEnrolledInfo();
    }

    /* access modifiers changed from: package-private */
    public int renameEnrolledInfo(int i, String str) {
        return this.mProxy.renameEnrolledInfo(i, str);
    }

    /* access modifiers changed from: package-private */
    public BiometricEnroll.EnrolledTips getEnrolledTips() {
        return this.mProxy.getEnrolledTips();
    }

    /* access modifiers changed from: package-private */
    public int getEnrolledFaceId() {
        return this.mProxy.getEnrolledFaceId();
    }
}
