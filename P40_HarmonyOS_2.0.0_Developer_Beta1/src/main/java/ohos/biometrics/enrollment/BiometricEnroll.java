package ohos.biometrics.enrollment;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.agp.graphics.Surface;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BiometricEnroll {
    private static final String ACCESS_PERMISSION = "ohos.permission.ACCESS_BIOMETRIC";
    public static final int ALL_BIOMETRIC_ID = 0;
    protected static final int DFT_EVENT_ID_BIOMETRIC_ENROLL = 940101020;
    private static final String ENROLL_PERMISSION = "ohos.permission.ENROLL_BIOMETRIC";
    public static final int FAILED = -1;
    public static final int INVALID_BIOMETRIC_ID = -1;
    public static final int INVALID_CHALLENGE_VALUE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN_ID, "BiometricEnroll");
    protected static final int LOG_DOMAIN_ID = 218113024;
    private static final int PERMISSION_GRANTED = 0;
    public static final int SUCCESS = 0;
    private static volatile BiometricEnroll sInstance = null;
    private volatile BiometricType mBiometricType = BiometricType.BIOMETRICTYPE_NONE;
    private final Context mContext;

    public enum BiometricType {
        BIOMETRICTYPE_NONE,
        BIOMETRICTYPE_FINGERPRINT,
        BIOMETRICTYPE_FACE,
        BIOMETRICTYPE_VOICEID
    }

    private BiometricEnroll(Context context) {
        this.mContext = context;
    }

    public static BiometricEnroll getInstance(Ability ability) throws IllegalAccessException {
        if (ability != null) {
            Context applicationContext = ability.getApplicationContext();
            if (applicationContext == null) {
                throw new IllegalArgumentException("Must supply a valid ability, GetApplicationContext is null");
            } else if (applicationContext.verifySelfPermission(ACCESS_PERMISSION) != 0) {
                throw new IllegalAccessException("No permission to access biometric");
            } else if (applicationContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                if (sInstance == null) {
                    synchronized (BiometricEnroll.class) {
                        if (sInstance == null) {
                            sInstance = new BiometricEnroll(applicationContext);
                            HiLog.info(LABEL, "new instance OK!", new Object[0]);
                        }
                    }
                }
                return sInstance;
            } else {
                throw new IllegalAccessException("No permission to enroll biometric");
            }
        } else {
            throw new IllegalArgumentException("Must supply a valid ability");
        }
    }

    public synchronized EnrollInitResult initEnroll(BiometricType biometricType) {
        EnrollInitResult enrollInitResult = new EnrollInitResult();
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                if (biometricType == BiometricType.BIOMETRICTYPE_NONE) {
                    HiLog.error(LABEL, "BiometricType invalid, initEnroll failed!", new Object[0]);
                    return enrollInitResult;
                }
                if (this.mBiometricType == BiometricType.BIOMETRICTYPE_NONE || this.mBiometricType == biometricType) {
                    if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] != 1) {
                        HiLog.error(LABEL, "this BiometricType is currently not supported: %{public}d", new Object[]{biometricType});
                    } else {
                        enrollInitResult = FaceEnroll.getInstance(this.mContext).initEnroll();
                    }
                    if (enrollInitResult.getErrorCode() == 0) {
                        this.mBiometricType = biometricType;
                    }
                } else {
                    HiLog.error(LABEL, "last enoll is not finished: BiometricType = %{public}d", new Object[]{this.mBiometricType});
                }
                return enrollInitResult;
            }
        }
        HiLog.error(LABEL, "permission denied, initEnroll failed!", new Object[0]);
        return enrollInitResult;
    }

    /* renamed from: ohos.biometrics.enrollment.BiometricEnroll$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType = new int[BiometricType.values().length];

        static {
            try {
                $SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[BiometricType.BIOMETRICTYPE_FACE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[BiometricType.BIOMETRICTYPE_NONE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public synchronized int execEnroll(byte[] bArr, long j, int i, Surface surface) {
        int i2 = -1;
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                BiometricType biometricType = this.mBiometricType;
                if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] != 1) {
                    HiLog.error(LABEL, "BiometricType status error, please initEnroll first! BiometricType = %{public}d", new Object[]{biometricType});
                } else {
                    i2 = FaceEnroll.getInstance(this.mContext).execEnroll(bArr, j, i, surface);
                }
                return i2;
            }
        }
        HiLog.error(LABEL, "permission denied, execEnroll failed!", new Object[0]);
        return -1;
    }

    public synchronized int finishEnroll() {
        int i = -1;
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                int i2 = AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[this.mBiometricType.ordinal()];
                if (i2 == 1) {
                    this.mBiometricType = BiometricType.BIOMETRICTYPE_NONE;
                    i = FaceEnroll.getInstance(this.mContext).finishEnroll();
                } else if (i2 != 2) {
                    HiLog.error(LABEL, "this BiometricType is currently not supported: %{public}d", new Object[]{this.mBiometricType});
                } else {
                    HiLog.error(LABEL, "finishEnroll already!", new Object[0]);
                }
                return i;
            }
        }
        HiLog.error(LABEL, "permission denied, finishEnroll failed!", new Object[0]);
        return -1;
    }

    public int cancelEnroll() {
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0 && this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
            BiometricType biometricType = this.mBiometricType;
            if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] == 1) {
                return FaceEnroll.getInstance(this.mContext).cancelEnroll();
            }
            HiLog.error(LABEL, "BiometricType status error! please check initEnroll: BiometricType = %{public}d", new Object[]{biometricType});
            return -1;
        }
        HiLog.error(LABEL, "permission denied, cancelEnroll failed!", new Object[0]);
        return -1;
    }

    public synchronized int removeEnrolledInfo(int i) {
        int i2 = -1;
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                BiometricType biometricType = this.mBiometricType;
                if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] != 1) {
                    HiLog.error(LABEL, "BiometricType status error! please check initEnroll: BiometricType = %{public}d", new Object[]{biometricType});
                } else {
                    i2 = FaceEnroll.getInstance(this.mContext).removeEnrolledInfo(i);
                }
                return i2;
            }
        }
        HiLog.error(LABEL, "permission denied, removeEnrolledInfo failed!", new Object[0]);
        return -1;
    }

    public synchronized List<EnrollInfo> getEnrolledInfo(BiometricType biometricType) {
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                List<EnrollInfo> arrayList = new ArrayList<>(0);
                if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] != 1) {
                    HiLog.error(LABEL, "this BiometricType is currently not supported: %{public}d", new Object[]{biometricType});
                } else {
                    arrayList = FaceEnroll.getInstance(this.mContext).getEnrolledInfo();
                }
                return arrayList;
            }
        }
        HiLog.error(LABEL, "permission denied, getEnrolledInfo failed!", new Object[0]);
        return new ArrayList(0);
    }

    public synchronized int renameEnrolledInfo(BiometricType biometricType, int i, String str) {
        int i2 = -1;
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            if (this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
                if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] != 1) {
                    HiLog.error(LABEL, "this BiometricType is currently not supported: %{public}d", new Object[]{biometricType});
                } else {
                    i2 = FaceEnroll.getInstance(this.mContext).renameEnrolledInfo(i, str);
                }
                return i2;
            }
        }
        HiLog.error(LABEL, "permission denied, renameEnrolledInfo failed!", new Object[0]);
        return -1;
    }

    public EnrolledTips getEnrolledTips() {
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0 && this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
            EnrolledTips enrolledTips = new EnrolledTips();
            BiometricType biometricType = this.mBiometricType;
            if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] == 1) {
                return FaceEnroll.getInstance(this.mContext).getEnrolledTips();
            }
            HiLog.error(LABEL, "BiometricType status error! please check initEnroll: BiometricType = %{public}d", new Object[]{biometricType});
            return enrolledTips;
        }
        HiLog.error(LABEL, "permission denied, getEnrolledTips failed!", new Object[0]);
        return new EnrolledTips();
    }

    public static class EnrollInitResult {
        private long mChallenge = 0;
        private int mErrorCode = -1;
        private long mSessionId = 0;

        public int getErrorCode() {
            return this.mErrorCode;
        }

        /* access modifiers changed from: package-private */
        public void setErrorCode(int i) {
            this.mErrorCode = i;
        }

        public long getSessionId() {
            return this.mSessionId;
        }

        /* access modifiers changed from: package-private */
        public void setSessionId(long j) {
            this.mSessionId = j;
        }

        public long getChallenge() {
            return this.mChallenge;
        }

        /* access modifiers changed from: package-private */
        public void setChallenge(long j) {
            this.mChallenge = j;
        }
    }

    public static class EnrollInfo {
        private int mBiometricId = -1;
        private boolean mHasAlternateData = false;
        private String mName = "";

        public int getBiometricId() {
            return this.mBiometricId;
        }

        /* access modifiers changed from: package-private */
        public void setBiometricId(int i) {
            this.mBiometricId = i;
        }

        public boolean isHasAlternateData() {
            return this.mHasAlternateData;
        }

        /* access modifiers changed from: package-private */
        public void setHasAlternateData(boolean z) {
            this.mHasAlternateData = z;
        }

        public String getName() {
            return this.mName;
        }

        /* access modifiers changed from: package-private */
        public void setName(String str) {
            this.mName = str;
        }
    }

    public static class EnrolledTips {
        private int mErrorCode = -1;
        private int mTipEvent = 0;
        private String mTipInfo = "";
        private int mTipValue = 0;

        public int getErrorCode() {
            return this.mErrorCode;
        }

        /* access modifiers changed from: package-private */
        public void setErrorCode(int i) {
            this.mErrorCode = i;
        }

        public int getTipEvent() {
            return this.mTipEvent;
        }

        /* access modifiers changed from: package-private */
        public void setTipEvent(int i) {
            this.mTipEvent = i;
        }

        public int getTipValue() {
            return this.mTipValue;
        }

        /* access modifiers changed from: package-private */
        public void setTipValue(int i) {
            this.mTipValue = i;
        }

        public String getTipInfo() {
            return this.mTipInfo;
        }

        /* access modifiers changed from: package-private */
        public void setTipInfo(String str) {
            this.mTipInfo = str;
        }
    }

    public int getEnrolledBiometricId() {
        if (this.mContext.verifySelfPermission(ACCESS_PERMISSION) == 0 && this.mContext.verifySelfPermission(ENROLL_PERMISSION) == 0) {
            BiometricType biometricType = this.mBiometricType;
            if (AnonymousClass1.$SwitchMap$ohos$biometrics$enrollment$BiometricEnroll$BiometricType[biometricType.ordinal()] == 1) {
                return FaceEnroll.getInstance(this.mContext).getEnrolledFaceId();
            }
            HiLog.error(LABEL, "BiometricType status error, please initEnroll first! BiometricType = %{public}d", new Object[]{biometricType});
            return -1;
        }
        HiLog.error(LABEL, "permission denied, getEnrolledBiometricId failed!", new Object[0]);
        return -1;
    }
}
