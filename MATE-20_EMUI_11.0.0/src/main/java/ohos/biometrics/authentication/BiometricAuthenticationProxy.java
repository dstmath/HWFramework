package ohos.biometrics.authentication;

import android.content.Context;
import android.hardware.biometrics.CryptoObject;
import android.view.Surface;
import huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.FaceRecognizeManagerFactory;
import huawei.hiview.HiView;
import java.security.Signature;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import ohos.aafwk.ability.Ability;
import ohos.biometrics.authentication.BiometricAuthentication;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class BiometricAuthenticationProxy {
    private static final int BIOMETRIC_DOMAIN = 218113024;
    private static final int CODE_CALLBACK_ACQUIRE = 3;
    private static final int CODE_CALLBACK_FACEID = 6;
    private static final int CODE_CALLBACK_RESULT = 1;
    private static final int FACEID_FACE_MOVED = 33;
    private static final Object FACEID_LOCK = new Object();
    private static final int FACEID_NOT_GAZE = 36;
    private static final HashMap<Integer, Integer> FACE_RESULT_CODE_MAP = new HashMap<Integer, Integer>() {
        /* class ohos.biometrics.authentication.BiometricAuthenticationProxy.AnonymousClass1 */

        {
            put(0, 0);
            put(3, 1);
            put(2, 2);
            put(4, 3);
            put(12, 4);
            put(13, 5);
            put(9, 6);
            put(8, 7);
            put(10, 8);
            put(1, 100);
            put(5, 100);
            put(6, 100);
            put(7, 100);
            put(11, 100);
            put(100, 100);
        }
    };
    private static final HashMap<Integer, Integer> FACE_TIPS_CODE_MAP = new HashMap<Integer, Integer>() {
        /* class ohos.biometrics.authentication.BiometricAuthenticationProxy.AnonymousClass2 */

        {
            put(31, 1);
            put(30, 2);
            put(7, 3);
            put(6, 4);
            put(9, 5);
            put(11, 6);
            put(10, 7);
            put(8, 8);
            put(33, 9);
            put(36, 10);
            put(15, 10);
            put(16, 10);
            put(17, 10);
            put(18, 10);
            put(40, 10);
            put(41, 10);
            put(42, 10);
            put(43, 10);
            put(5, 11);
        }
    };
    private static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_LEFT = 43;
    private static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_RIGHT = 42;
    private static final int FACE_UNLOCK_FACE_ROTATE_TOP_LEFT = 41;
    private static final int FACE_UNLOCK_FACE_ROTATE_TOP_RIGHT = 40;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) BIOMETRIC_DOMAIN, "BiometricAuthenticationProxy");
    private static final int LATCH_COUNT = 1;
    private static final int MASK_CODE = 65535;
    private static final int TYPE_CALLBACK_AUTH = 2;
    private final BiometricAuthentication.AuthenticationTips mAuthenticationTips = new BiometricAuthentication.AuthenticationTips();
    private volatile Cipher mCipher;
    private volatile CryptoObject mCryproObject;
    private volatile int mFaceId = -1;
    private final DefaultFaceRecognizeManagerImpl mFaceManagerImpl;
    private boolean mIsFaceMode3D = false;
    private boolean mIsFaceRecognitionSupport = false;
    private volatile Mac mMac;
    private final String mPkgName;
    private volatile long mReqId;
    private volatile int mResult = 100;
    private volatile Signature mSign;
    private volatile CountDownLatch mSignal;

    private boolean isFaceMode3D(int i) {
        return i == 3 || i == 4 || i == 5;
    }

    static class FaceMode {
        public static final int FACE_MODE_2D_COMMON_CAMERA = 0;
        public static final int FACE_MODE_2D_RGB_NPU = 7;
        public static final int FACE_MODE_2D_SECURE_CAMERA = 1;
        public static final int FACE_MODE_2D_SWING = 6;
        public static final int FACE_MODE_2D_UNSECURE_CAMERA = 2;
        public static final int FACE_MODE_3D_DUAL_CAMERA = 3;
        public static final int FACE_MODE_3D_STRUCT = 4;
        public static final int FACE_MODE_3D_TOF = 5;

        FaceMode() {
        }
    }

    public BiometricAuthenticationProxy(Ability ability) {
        if (ability == null) {
            this.mPkgName = null;
            this.mFaceManagerImpl = null;
            this.mIsFaceRecognitionSupport = false;
            this.mIsFaceMode3D = false;
            HiLog.error(LABEL, "BiometricAuthenticationProxy construct failed! ability null!", new Object[0]);
            return;
        }
        Object hostContext = ability.getContext().getHostContext();
        if (!(hostContext instanceof Context)) {
            this.mPkgName = null;
            this.mFaceManagerImpl = null;
            this.mIsFaceRecognitionSupport = false;
            this.mIsFaceMode3D = false;
            HiLog.error(LABEL, "BiometricAuthenticationProxy construct failed! getHostContext error!", new Object[0]);
            return;
        }
        Context context = (Context) hostContext;
        this.mPkgName = context.getOpPackageName();
        this.mFaceManagerImpl = FaceRecognizeManagerFactory.getInstance().getFaceRecognizeManagerImpl(context, new DefaultFaceRecognizeManagerImpl.FaceRecognizeCallback() {
            /* class ohos.biometrics.authentication.BiometricAuthenticationProxy.AnonymousClass3 */

            public void onCallbackEvent(int i, int i2, int i3, int i4) {
                HiLog.info(BiometricAuthenticationProxy.LABEL, "onCallbackEvent is called type = %{public}d", new Object[]{Integer.valueOf(i2)});
                if (((long) i) == BiometricAuthenticationProxy.this.mReqId && i2 == 2) {
                    BiometricAuthenticationProxy.this.setAuthenticationTips(i3, i4);
                    if (i3 == 1 && BiometricAuthenticationProxy.this.mSignal != null) {
                        HiLog.info(BiometricAuthenticationProxy.LABEL, "get authentication result from callback event! errorCode = %{public}d", new Object[]{Integer.valueOf(i4)});
                        BiometricAuthenticationProxy.this.mResult = BiometricAuthenticationProxy.FACE_RESULT_CODE_MAP.containsKey(Integer.valueOf(i4)) ? ((Integer) BiometricAuthenticationProxy.FACE_RESULT_CODE_MAP.get(Integer.valueOf(i4))).intValue() : 100;
                        BiometricAuthenticationProxy.this.mSignal.countDown();
                    }
                    if (i3 == 6) {
                        HiLog.info(BiometricAuthenticationProxy.LABEL, "get authentication face id from callback event! faceid = ****%{public}04x", new Object[]{Integer.valueOf(65535 & i4)});
                        BiometricAuthenticationProxy.this.setAuthedFaceId(i4);
                        return;
                    }
                    return;
                }
                HiLog.error(BiometricAuthenticationProxy.LABEL, "authentication callback event error: reqId = %{public}d, type = %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            }
        });
        initFaceRecognitionAbility();
    }

    private void initFaceRecognitionAbility() {
        DefaultFaceRecognizeManagerImpl.FaceRecognitionAbility faceRecognitionAbility = this.mFaceManagerImpl.getFaceRecognitionAbility();
        if (faceRecognitionAbility == null) {
            HiLog.error(LABEL, "get face recognition ability failed", new Object[0]);
            return;
        }
        int i = faceRecognitionAbility.faceMode;
        this.mIsFaceRecognitionSupport = faceRecognitionAbility.isFaceRecognitionSupport;
        this.mIsFaceMode3D = isFaceMode3D(i);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.biometrics.authentication.BiometricAuthenticationProxy$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType = new int[BiometricAuthentication.AuthType.values().length];

        static {
            try {
                $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[BiometricAuthentication.AuthType.AUTH_TYPE_BIOMETRIC_ALL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[BiometricAuthentication.AuthType.AUTH_TYPE_BIOMETRIC_FACE_ONLY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[BiometricAuthentication.AuthType.AUTH_TYPE_BIOMETRIC_FINGERPRINT_ONLY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public int checkAuthenticationAvailability(BiometricAuthentication.AuthType authType, BiometricAuthentication.SecureLevel secureLevel, boolean z) {
        if (authType == null || secureLevel == null) {
            HiLog.error(LABEL, "checkAuthenticationAvailability failed: input parameter is null", new Object[0]);
            return 1;
        }
        int i = AnonymousClass4.$SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[authType.ordinal()];
        if (i == 1) {
            HiLog.error(LABEL, "BIOMETRIC_ALL not support check auth ability", new Object[0]);
            return 1;
        } else if (i == 2) {
            return checkFaceRecognition(secureLevel, z);
        } else {
            if (i != 3) {
                HiLog.error(LABEL, "check auth ability type error, type = %{public}d", new Object[]{authType});
                return 1;
            }
            HiLog.error(LABEL, "BIOMETRIC_FINGERPRINT not support check auth ability", new Object[0]);
            return 1;
        }
    }

    private int checkFaceRecognition(BiometricAuthentication.SecureLevel secureLevel, boolean z) {
        if (!z) {
            return 3;
        }
        if (!this.mIsFaceRecognitionSupport) {
            return 1;
        }
        if (this.mIsFaceMode3D) {
            if (BiometricAuthentication.SecureLevel.SECURE_LEVEL_S4.equals(secureLevel)) {
                return 2;
            }
        } else if (BiometricAuthentication.SecureLevel.SECURE_LEVEL_S3.equals(secureLevel) || BiometricAuthentication.SecureLevel.SECURE_LEVEL_S4.equals(secureLevel)) {
            return 2;
        }
        return this.mFaceManagerImpl.getEnrolledFaceIDs().length == 0 ? 4 : 0;
    }

    public int execAuthenticationAction(BiometricAuthentication.AuthType authType, BiometricAuthentication.SecureLevel secureLevel, boolean z, boolean z2, BiometricAuthentication.SystemAuthDialogInfo systemAuthDialogInfo) {
        if (authType == null || secureLevel == null) {
            HiLog.error(LABEL, "execAuthenticationAction failed: input parameter is null", new Object[0]);
            return -1;
        }
        int i = AnonymousClass4.$SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[authType.ordinal()];
        if (i == 1) {
            HiLog.error(LABEL, "BIOMETRIC_ALL not support execute auth", new Object[0]);
            return -1;
        } else if (i == 2) {
            long currentTimeMillis = System.currentTimeMillis();
            int execFaceAuthentication = execFaceAuthentication();
            HiView.report(HiView.byVariadic(940101021, HiView.PayloadMode.KVPairs, new Object[]{"call_pkg", this.mPkgName, "biometric_type", Integer.valueOf(BiometricAuthentication.AuthType.AUTH_TYPE_BIOMETRIC_FACE_ONLY.ordinal()), "sdk_version", "1.0", "auth_result", Integer.valueOf(execFaceAuthentication), "run_time", Long.valueOf(System.currentTimeMillis() - currentTimeMillis), "secure_level", Integer.valueOf(secureLevel.ordinal() + 1), "is_local_auth", Integer.valueOf(z ? 1 : 0), "is_app_auth_dialog", Integer.valueOf(z2 ? 1 : 0)}));
            return execFaceAuthentication;
        } else if (i != 3) {
            HiLog.error(LABEL, "execute auth type error, type = %{public}d", new Object[]{authType});
            return -1;
        } else {
            HiLog.error(LABEL, "BIOMETRIC_FINGERPRINT not support execute auth", new Object[0]);
            return -1;
        }
    }

    private int execFaceAuthentication() {
        DefaultFaceRecognizeManagerImpl defaultFaceRecognizeManagerImpl = this.mFaceManagerImpl;
        if (defaultFaceRecognizeManagerImpl == null) {
            HiLog.error(LABEL, "execFaceAuthentication failed: mFaceManagerImpl is null", new Object[0]);
            return 100;
        } else if (defaultFaceRecognizeManagerImpl.init() != 0) {
            HiLog.error(LABEL, "Authentication initialization failed", new Object[0]);
            return 100;
        } else {
            this.mFaceId = -1;
            this.mSignal = new CountDownLatch(1);
            HiLog.info(LABEL, "execAuthenticationAction start", new Object[0]);
            this.mFaceManagerImpl.authenticate(this.mReqId, 0, (Surface) null);
            try {
                this.mSignal.await();
                this.mFaceManagerImpl.release();
                HiLog.debug(LABEL, "execAuthenticationAction success", new Object[0]);
                return this.mResult;
            } catch (InterruptedException unused) {
                this.mFaceManagerImpl.release();
                HiLog.error(LABEL, "execAuthenticationAction await child thread failed", new Object[0]);
                return 100;
            }
        }
    }

    public int cancelAuthenticationAction(BiometricAuthentication.AuthType authType) {
        if (this.mFaceManagerImpl == null || authType == null) {
            HiLog.error(LABEL, "cancelAuthenticationAction failed: input parameter is null", new Object[0]);
            return -1;
        }
        int i = AnonymousClass4.$SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[authType.ordinal()];
        if (i == 1) {
            HiLog.error(LABEL, "BIOMETRIC_ALL not support cancel auth", new Object[0]);
            return -1;
        } else if (i == 2) {
            HiLog.info(LABEL, "cancelAuthenticationAction start", new Object[0]);
            return this.mFaceManagerImpl.cancelAuthenticate(this.mReqId);
        } else if (i != 3) {
            HiLog.error(LABEL, "cancel auth type error, type = %{public}d", new Object[]{authType});
            return -1;
        } else {
            HiLog.error(LABEL, "BIOMETRIC_FINGERPRINT not support cancel auth", new Object[0]);
            return -1;
        }
    }

    public BiometricAuthentication.AuthenticationTips getAuthenticationTips() {
        BiometricAuthentication.AuthenticationTips authenticationTips;
        synchronized (this.mAuthenticationTips) {
            authenticationTips = new BiometricAuthentication.AuthenticationTips();
            authenticationTips.errorCode = this.mAuthenticationTips.errorCode;
            authenticationTips.tipEvent = this.mAuthenticationTips.tipEvent;
            authenticationTips.tipValue = this.mAuthenticationTips.tipValue;
            authenticationTips.tipInfo = this.mAuthenticationTips.tipInfo;
        }
        return authenticationTips;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAuthenticationTips(int i, int i2) {
        if (this.mFaceManagerImpl != null) {
            if (i == 3 && !FACE_TIPS_CODE_MAP.containsKey(Integer.valueOf(i2))) {
                return;
            }
            if (i != 1 || FACE_RESULT_CODE_MAP.containsKey(Integer.valueOf(i2))) {
                synchronized (this.mAuthenticationTips) {
                    this.mAuthenticationTips.errorCode = 0;
                    this.mAuthenticationTips.tipEvent = i;
                    this.mAuthenticationTips.tipValue = (i == 3 ? FACE_TIPS_CODE_MAP.get(Integer.valueOf(i2)) : FACE_RESULT_CODE_MAP.get(Integer.valueOf(i2))).intValue();
                    this.mAuthenticationTips.tipInfo = DefaultFaceRecognizeManagerImpl.getErrorCodeString(i, i2);
                }
            }
        }
    }

    public int getAuthedFaceId() {
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
    private void setAuthedFaceId(int i) {
        synchronized (FACEID_LOCK) {
            this.mFaceId = i;
        }
    }

    public synchronized void setSecureObjectSignature(Signature signature) {
        this.mSign = signature;
        this.mCryproObject = new CryptoObject(signature);
        this.mReqId = this.mCryproObject.getOpId();
    }

    public synchronized Signature getSecureObjectSignature() {
        return this.mSign;
    }

    public synchronized void setSecureObjectCipher(Cipher cipher) {
        this.mCipher = cipher;
        this.mCryproObject = new CryptoObject(cipher);
        this.mReqId = this.mCryproObject.getOpId();
    }

    public synchronized Cipher getSecureObjectCipher() {
        return this.mCipher;
    }

    public synchronized void setSecureObjectMac(Mac mac) {
        this.mMac = mac;
        this.mCryproObject = new CryptoObject(mac);
        this.mReqId = this.mCryproObject.getOpId();
    }

    public synchronized Mac getSecureObjectMac() {
        return this.mMac;
    }
}
