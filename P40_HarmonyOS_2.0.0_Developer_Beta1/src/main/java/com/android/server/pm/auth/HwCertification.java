package com.android.server.pm.auth;

import com.android.server.pm.auth.deviceid.DeviceId;
import com.android.server.pm.auth.processor.PermissionProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipFile;

public class HwCertification {
    private static final List<String> CERTIFICATE_TYPE_LIST = new ArrayList();
    public static final int CONTAIN_NO_HW_CERT = 1;
    public static final int CONTAIN_VALID_HW_CERT = 0;
    private static final List<String> HWCERT_KEY_LIST = new ArrayList();
    public static final int HWCERT_SIGNATURE_VERSION = 1;
    public static final int HWCERT_SIGNATURE_VERSION_2 = 2;
    public static final int HWCERT_SIGNATURE_VERSION_3 = 3;
    public static final String IMEI_PREFIX = "IMEI/";
    public static final String KEY_APK_HASH = "ApkHash";
    public static final String KEY_CERTIFICATE = "Certificate";
    public static final String KEY_DATE_FROM = "from";
    public static final String KEY_DATE_TO = "to";
    public static final String KEY_DEVELOPER = "DeveloperKey";
    public static final String KEY_DEVICE_IDS = "DeviceIds";
    public static final String KEY_EXTENSION = "Extension";
    public static final String KEY_PACKAGE_NAME = "PackageName";
    public static final String KEY_PERMISSIONS = "Permissions";
    public static final String KEY_SIGNATURE = "Signature";
    public static final String KEY_SIGNATURE2 = "Signature2";
    public static final String KEY_SIGNATURE3 = "Signature3";
    public static final String KEY_VALID_PERIOD = "ValidPeriod";
    public static final String KEY_VERSION = "Version";
    public static final String MAC_PREFIX = "WIFIMAC/";
    public static final String MEID_PREFIX = "MEID/";
    public static final String PERMISSIONS_DEFAULT = "null";
    public static final int RESULT_DEFAULT = 0;
    public static final int RESULT_INVALID = -1;
    public static final int RESULT_MDM_WITHOUT_CERTIFICATE = 6;
    public static final int RESULT_MEDIA = 4;
    public static final int RESULT_NOT_MDM = 5;
    public static final int RESULT_PLATFORM = 1;
    public static final int RESULT_SHARED = 3;
    public static final int RESULT_TESTKEY = 2;
    public static final String SIGNATURE_DEFAULT = "null";
    public static final String SIGNATURE_MEDIA = "media";
    public static final String SIGNATURE_PLATFORM = "platform";
    public static final String SIGNATURE_SHARED = "shared";
    public static final String SIGNATURE_TESTKEY = "testkey";
    public static final String VERSION2_HWCERT = "2";
    private File mApkFile;
    private String mApkHash;
    private String mCertificate;
    private CertificationData mCertificationData = new CertificationData();
    private String mDeveloperKey;
    private List<DeviceId> mDeviceIds = new ArrayList();
    private String mExtension;
    private Date mFromDate;
    private boolean mIsContainSpecialPermissions;
    private boolean mIsCustomized;
    private boolean mIsReleased;
    private String mPackageName;
    private List<String> mPermissions = new ArrayList();
    private String mSignatureV1;
    private String mSignatureV2;
    private String mSignatureV3;
    private Date mToDate;
    private String mVersion;
    private ZipFile mZipFile;

    static {
        HWCERT_KEY_LIST.add(KEY_DEVELOPER);
        HWCERT_KEY_LIST.add(KEY_PACKAGE_NAME);
        HWCERT_KEY_LIST.add(KEY_PERMISSIONS);
        HWCERT_KEY_LIST.add(KEY_DEVICE_IDS);
        HWCERT_KEY_LIST.add(KEY_VALID_PERIOD);
        HWCERT_KEY_LIST.add(KEY_APK_HASH);
        HWCERT_KEY_LIST.add(KEY_VERSION);
        HWCERT_KEY_LIST.add(KEY_CERTIFICATE);
        HWCERT_KEY_LIST.add(KEY_EXTENSION);
        HWCERT_KEY_LIST.add(KEY_SIGNATURE);
        HWCERT_KEY_LIST.add(KEY_SIGNATURE2);
        HWCERT_KEY_LIST.add(KEY_SIGNATURE3);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_TESTKEY);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_PLATFORM);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_SHARED);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_MEDIA);
        CERTIFICATE_TYPE_LIST.add("null");
    }

    public static boolean isHwCertKeyContainsTag(String keyTag) {
        return HWCERT_KEY_LIST.contains(keyTag);
    }

    public static boolean isContainsCertificateType(String certificate) {
        return CERTIFICATE_TYPE_LIST.contains(certificate);
    }

    public boolean isCustomized() {
        return this.mIsCustomized;
    }

    public void setCustomized(boolean isCustomized) {
        this.mIsCustomized = isCustomized;
    }

    public File getApkFile() {
        return this.mApkFile;
    }

    public void setApkFile(File apkFile) {
        this.mApkFile = apkFile;
    }

    public String getDeveloperKey() {
        return this.mDeveloperKey;
    }

    public void setDeveloperKey(String developerKey) {
        this.mDeveloperKey = developerKey;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public List<String> getPermissions() {
        return this.mPermissions;
    }

    public void setPermissions(List<String> permissions) {
        this.mPermissions = permissions;
    }

    public List<DeviceId> getDeviceIds() {
        return this.mDeviceIds;
    }

    public Date getFromDate() {
        return this.mFromDate;
    }

    public void setFromDate(Date fromDate) {
        this.mFromDate = fromDate;
    }

    public Date getToDate() {
        return this.mToDate;
    }

    public void setToDate(Date toDate) {
        this.mToDate = toDate;
    }

    public String getApkHash() {
        return this.mApkHash;
    }

    public void setApkHash(String apkHash) {
        this.mApkHash = apkHash;
    }

    public String getSignatureV1() {
        return this.mSignatureV1;
    }

    public void setSignatureV1(String signatureV1) {
        this.mSignatureV1 = signatureV1;
    }

    public String getSignatureV2() {
        return this.mSignatureV2;
    }

    public void setSignatureV2(String signatureV2) {
        this.mSignatureV2 = signatureV2;
    }

    public String getSignatureV3() {
        return this.mSignatureV3;
    }

    public void setSignatureV3(String signatureV3) {
        this.mSignatureV3 = signatureV3;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public void setVersion(String version) {
        this.mVersion = version;
    }

    public String getCertificate() {
        return this.mCertificate;
    }

    public void setCertificate(String certificate) {
        this.mCertificate = certificate;
    }

    public String getExtension() {
        return this.mExtension;
    }

    public void setExtension(String extension) {
        this.mExtension = extension;
    }

    public boolean isReleased() {
        return this.mIsReleased;
    }

    public void setReleased(boolean isReleased) {
        this.mIsReleased = isReleased;
    }

    public boolean isContainSpecialPermissions() {
        return this.mIsContainSpecialPermissions || this.mPermissions.contains(PermissionProcessor.SPECIAL_PERMISSION);
    }

    public void setContainSpecialPermissions(boolean isContainSpecialPermissions) {
        this.mIsContainSpecialPermissions = isContainSpecialPermissions;
    }

    public CertificationData getCertificationData() {
        return this.mCertificationData;
    }

    public ZipFile getZipFile() {
        return this.mZipFile;
    }

    public void setZipFile(ZipFile zipfile) {
        this.mZipFile = zipfile;
    }

    public void resetZipFile() {
        this.mZipFile = null;
    }

    public static class CertificationData {
        private File mApkFile;
        private String mApkHash;
        private String mCertificate;
        private String mDeveloperKey;
        private String mDeviceIdsString;
        private String mExtension;
        private boolean mIsReleased;
        private String mPackageName;
        private String mPeriodString;
        private String mPermissionsString;
        private String mSignatureV1;
        private String mSignatureV2;
        private String mSignatureV3;
        private String mVersion;

        public void setApkFile(File file) {
            this.mApkFile = file;
        }

        public String getDeveloperKey() {
            return this.mDeveloperKey;
        }

        public void setDeveloperKey(String developerKey) {
            this.mDeveloperKey = developerKey;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        public String getPermissionsString() {
            return this.mPermissionsString;
        }

        public void setPermissionsString(String permissionsString) {
            this.mPermissionsString = permissionsString;
        }

        public String getDeviceIdsString() {
            return this.mDeviceIdsString;
        }

        public void setDeviceIdsString(String deviceIdsString) {
            this.mDeviceIdsString = deviceIdsString;
        }

        public String getPeriodString() {
            return this.mPeriodString;
        }

        public void setPeriodString(String periodString) {
            this.mPeriodString = periodString;
        }

        public String getApkHash() {
            return this.mApkHash;
        }

        public void setApkHash(String apkHash) {
            this.mApkHash = apkHash;
        }

        public String getSignatureV1() {
            return this.mSignatureV1;
        }

        public void setSignatureV1(String signatureV1) {
            this.mSignatureV1 = signatureV1;
        }

        public String getSignatureV2() {
            return this.mSignatureV2;
        }

        public void setSignatureV2(String signatureV2) {
            this.mSignatureV2 = signatureV2;
        }

        public void setSignatureV3(String signatureV3) {
            this.mSignatureV3 = signatureV3;
        }

        public String getSignatureV3() {
            return this.mSignatureV3;
        }

        public String getVersion() {
            return this.mVersion;
        }

        public void setVersion(String version) {
            this.mVersion = version;
        }

        public String getCertificate() {
            return this.mCertificate;
        }

        public void setCertificate(String certificate) {
            this.mCertificate = certificate;
        }

        public String getExtension() {
            return this.mExtension;
        }

        public void setExtension(String extension) {
            this.mExtension = extension;
        }
    }
}
