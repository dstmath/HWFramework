package com.android.server.pm.auth;

import com.android.server.pm.auth.deviceid.DeviceId;
import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipFile;

public class HwCertification {
    private static final List<String> CERTIFICATE_TYPE_LIST = new ArrayList();
    private static final List<String> HWCERT_KEY_LIST = new ArrayList();
    public static final String IMEI_PREFIX = "IMEI/";
    public static final String KEY_APK_HASH = "ApkHash";
    public static final String KEY_CERTIFICATE = "Certificate";
    public static final String KEY_DATE_FROM = "from";
    public static final String KEY_DATE_TO = "to";
    public static final String KEY_DEVELIOPER = "DeveloperKey";
    public static final String KEY_DEVICE_IDS = "DeviceIds";
    public static final String KEY_EXTENSION = "Extension";
    public static final String KEY_PACKAGE_NAME = "PackageName";
    public static final String KEY_PERMISSIONS = "Permissions";
    public static final String KEY_SIGNATURE = "Signature";
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
    private File mApkFile;
    private String mApkHash;
    private String mCertificate;
    public CertificationData mCertificationData = new CertificationData();
    private String mDelveoperKey;
    private List<DeviceId> mDeviceIds = new ArrayList();
    private String mExtenstion;
    private Date mFromDate;
    private boolean mIsReleased;
    private String mPackageName;
    private List<String> mPermissions = new ArrayList();
    private PrivateKey mPriKey;
    private String mSignature;
    private Date mToDate;
    private String mVersion;
    private ZipFile zfile;

    public static class CertificationData {
        public File mApkFile;
        public String mApkHash;
        public String mCertificate;
        public String mDelveoperKey;
        public String mDeviceIdsString;
        public String mExtenstion;
        public boolean mIsReleased;
        public String mPackageName;
        public String mPeriodString;
        public String mPermissionsString;
        public String mSignature;
        public String mVersion;
    }

    static {
        HWCERT_KEY_LIST.add(KEY_DEVELIOPER);
        HWCERT_KEY_LIST.add("PackageName");
        HWCERT_KEY_LIST.add(KEY_PERMISSIONS);
        HWCERT_KEY_LIST.add(KEY_DEVICE_IDS);
        HWCERT_KEY_LIST.add(KEY_VALID_PERIOD);
        HWCERT_KEY_LIST.add(KEY_APK_HASH);
        HWCERT_KEY_LIST.add(KEY_VERSION);
        HWCERT_KEY_LIST.add(KEY_CERTIFICATE);
        HWCERT_KEY_LIST.add(KEY_EXTENSION);
        HWCERT_KEY_LIST.add(KEY_SIGNATURE);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_TESTKEY);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_PLATFORM);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_SHARED);
        CERTIFICATE_TYPE_LIST.add(SIGNATURE_MEDIA);
        CERTIFICATE_TYPE_LIST.add("null");
    }

    public File getApkFile() {
        return this.mApkFile;
    }

    public void setApkFile(File mApkFile) {
        this.mApkFile = mApkFile;
    }

    public PrivateKey getPriKey() {
        return this.mPriKey;
    }

    public void setPriKey(PrivateKey mPriKey) {
        this.mPriKey = mPriKey;
    }

    public String getDelveoperKey() {
        return this.mDelveoperKey;
    }

    public void setDelveoperKey(String mDelveoperKey) {
        this.mDelveoperKey = mDelveoperKey;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public List<String> getPermissionList() {
        return this.mPermissions;
    }

    public void setPermissionList(List<String> mPermissions) {
        this.mPermissions = mPermissions;
    }

    public List<DeviceId> getDeviceIdList() {
        return this.mDeviceIds;
    }

    public void setDeviceIdList(List<DeviceId> mDeviceIds) {
        this.mDeviceIds = mDeviceIds;
    }

    public Date getFromDate() {
        return this.mFromDate;
    }

    public void setFromDate(Date mFrameDate) {
        this.mFromDate = mFrameDate;
    }

    public Date getToDate() {
        return this.mToDate;
    }

    public void setToDate(Date mToDate) {
        this.mToDate = mToDate;
    }

    public String getApkHash() {
        return this.mApkHash;
    }

    public void setApkHash(String mApkHash) {
        this.mApkHash = mApkHash;
    }

    public String getSignature() {
        return this.mSignature;
    }

    public void setSignature(String mSignature) {
        this.mSignature = mSignature;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public void setVersion(String mVersion) {
        this.mVersion = mVersion;
    }

    public String getCertificate() {
        return this.mCertificate;
    }

    public void setCertificate(String mCertificate) {
        this.mCertificate = mCertificate;
    }

    public String getExtenstion() {
        return this.mExtenstion;
    }

    public void setExtenstion(String mExtenstion) {
        this.mExtenstion = mExtenstion;
    }

    public boolean isReleased() {
        return this.mIsReleased;
    }

    public void setReleaseState(boolean isReleased) {
        this.mIsReleased = isReleased;
    }

    public static boolean isHwCertKeyContainsTag(String keyTag) {
        return HWCERT_KEY_LIST.contains(keyTag);
    }

    public static boolean isContainsCertificateType(String certificate) {
        return CERTIFICATE_TYPE_LIST.contains(certificate);
    }

    public void setZipFile(ZipFile zipfile) {
        this.zfile = zipfile;
    }

    public ZipFile getZipFile() {
        return this.zfile;
    }

    public void resetZipFile() {
        this.zfile = null;
    }
}
