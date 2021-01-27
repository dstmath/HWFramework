package com.android.server.pm.auth;

public class HwCertificationEx {
    public static final int CONTAIN_NO_HW_CERT = 1;
    public static final int CONTAIN_VALID_HW_CERT = 0;
    public static final int HWCERT_SIGNATURE_VERSION = 1;
    public static final int HWCERT_SIGNATURE_VERSION_2 = 2;
    public static final int HWCERT_SIGNATURE_VERSION_3 = 3;
    public static final int RESULT_DEFAULT = 0;
    public static final int RESULT_INVALID = -1;
    public static final int RESULT_MDM_WITHOUT_CERTIFICATE = 6;
    public static final int RESULT_MEDIA = 4;
    public static final int RESULT_NOT_MDM = 5;
    public static final int RESULT_PLATFORM = 1;
    public static final int RESULT_SHARED = 3;
    public static final int RESULT_TESTKEY = 2;
    private HwCertification mHwCertification;

    public HwCertification getHwCertification() {
        return this.mHwCertification;
    }

    public void setmHwCertification(HwCertification hwCertification) {
        this.mHwCertification = hwCertification;
    }
}
