package com.android.server.pm.auth;

import com.android.server.pm.auth.deviceid.DeviceId;
import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipFile;

public class HwCertification {
    private static final List<String> CERTIFICATE_TYPE_LIST = null;
    private static final List<String> HWCERT_KEY_LIST = null;
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
    public CertificationData mCertificationData;
    private String mDelveoperKey;
    private List<DeviceId> mDeviceIds;
    private String mExtenstion;
    private Date mFromDate;
    private boolean mIsReleased;
    private String mPackageName;
    private List<String> mPermissions;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.auth.HwCertification.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.auth.HwCertification.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.auth.HwCertification.<clinit>():void");
    }

    public HwCertification() {
        this.mPermissions = new ArrayList();
        this.mDeviceIds = new ArrayList();
        this.mCertificationData = new CertificationData();
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
