package android.net.wifi.hotspot2.pps;

import android.net.wifi.ParcelUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Credential implements Parcelable {
    public static final Creator<Credential> CREATOR = new Creator<Credential>() {
        public Credential createFromParcel(Parcel in) {
            boolean z = false;
            Credential credential = new Credential();
            credential.setCreationTimeInMillis(in.readLong());
            credential.setExpirationTimeInMillis(in.readLong());
            credential.setRealm(in.readString());
            if (in.readInt() != 0) {
                z = true;
            }
            credential.setCheckAaaServerCertStatus(z);
            credential.setUserCredential((UserCredential) in.readParcelable(null));
            credential.setCertCredential((CertificateCredential) in.readParcelable(null));
            credential.setSimCredential((SimCredential) in.readParcelable(null));
            credential.setCaCertificate(ParcelUtil.readCertificate(in));
            credential.setClientCertificateChain(ParcelUtil.readCertificates(in));
            credential.setClientPrivateKey(ParcelUtil.readPrivateKey(in));
            return credential;
        }

        public Credential[] newArray(int size) {
            return new Credential[size];
        }
    };
    private static final int MAX_REALM_BYTES = 253;
    private static final String TAG = "Credential";
    private X509Certificate mCaCertificate = null;
    private CertificateCredential mCertCredential = null;
    private boolean mCheckAaaServerCertStatus = false;
    private X509Certificate[] mClientCertificateChain = null;
    private PrivateKey mClientPrivateKey = null;
    private long mCreationTimeInMillis = Long.MIN_VALUE;
    private long mExpirationTimeInMillis = Long.MIN_VALUE;
    private String mRealm = null;
    private SimCredential mSimCredential = null;
    private UserCredential mUserCredential = null;

    public static final class CertificateCredential implements Parcelable {
        private static final int CERT_SHA256_FINGER_PRINT_LENGTH = 32;
        public static final String CERT_TYPE_X509V3 = "x509v3";
        public static final Creator<CertificateCredential> CREATOR = new Creator<CertificateCredential>() {
            public CertificateCredential createFromParcel(Parcel in) {
                CertificateCredential certCredential = new CertificateCredential();
                certCredential.setCertType(in.readString());
                certCredential.setCertSha256Fingerprint(in.createByteArray());
                return certCredential;
            }

            public CertificateCredential[] newArray(int size) {
                return new CertificateCredential[size];
            }
        };
        private byte[] mCertSha256Fingerprint = null;
        private String mCertType = null;

        public void setCertType(String certType) {
            this.mCertType = certType;
        }

        public String getCertType() {
            return this.mCertType;
        }

        public void setCertSha256Fingerprint(byte[] certSha256Fingerprint) {
            this.mCertSha256Fingerprint = certSha256Fingerprint;
        }

        public byte[] getCertSha256Fingerprint() {
            return this.mCertSha256Fingerprint;
        }

        public CertificateCredential(CertificateCredential source) {
            if (source != null) {
                this.mCertType = source.mCertType;
                if (source.mCertSha256Fingerprint != null) {
                    this.mCertSha256Fingerprint = Arrays.copyOf(source.mCertSha256Fingerprint, source.mCertSha256Fingerprint.length);
                }
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mCertType);
            dest.writeByteArray(this.mCertSha256Fingerprint);
        }

        public boolean equals(Object thatObject) {
            boolean z = false;
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof CertificateCredential)) {
                return false;
            }
            CertificateCredential that = (CertificateCredential) thatObject;
            if (TextUtils.equals(this.mCertType, that.mCertType)) {
                z = Arrays.equals(this.mCertSha256Fingerprint, that.mCertSha256Fingerprint);
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mCertType, this.mCertSha256Fingerprint});
        }

        public String toString() {
            return "CertificateType: " + this.mCertType + "\n";
        }

        public boolean validate() {
            if (!TextUtils.equals(CERT_TYPE_X509V3, this.mCertType)) {
                Log.d(Credential.TAG, "Unsupported certificate type: " + this.mCertType);
                return false;
            } else if (this.mCertSha256Fingerprint != null && this.mCertSha256Fingerprint.length == 32) {
                return true;
            } else {
                Log.d(Credential.TAG, "Invalid SHA-256 fingerprint");
                return false;
            }
        }
    }

    public static final class SimCredential implements Parcelable {
        public static final Creator<SimCredential> CREATOR = new Creator<SimCredential>() {
            public SimCredential createFromParcel(Parcel in) {
                SimCredential simCredential = new SimCredential();
                simCredential.setImsi(in.readString());
                simCredential.setEapType(in.readInt());
                return simCredential;
            }

            public SimCredential[] newArray(int size) {
                return new SimCredential[size];
            }
        };
        private static final int MAX_IMSI_LENGTH = 15;
        private int mEapType = Integer.MIN_VALUE;
        private String mImsi = null;

        public void setImsi(String imsi) {
            this.mImsi = imsi;
        }

        public String getImsi() {
            return this.mImsi;
        }

        public void setEapType(int eapType) {
            this.mEapType = eapType;
        }

        public int getEapType() {
            return this.mEapType;
        }

        public SimCredential(SimCredential source) {
            if (source != null) {
                this.mImsi = source.mImsi;
                this.mEapType = source.mEapType;
            }
        }

        public int describeContents() {
            return 0;
        }

        public boolean equals(Object thatObject) {
            boolean z = true;
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof SimCredential)) {
                return false;
            }
            SimCredential that = (SimCredential) thatObject;
            if (!TextUtils.equals(this.mImsi, that.mImsi)) {
                z = false;
            } else if (this.mEapType != that.mEapType) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mImsi, Integer.valueOf(this.mEapType)});
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("IMSI: ").append(this.mImsi).append("\n");
            builder.append("EAPType: ").append(this.mEapType).append("\n");
            return builder.toString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mImsi);
            dest.writeInt(this.mEapType);
        }

        public boolean validate() {
            if (!verifyImsi()) {
                return false;
            }
            if (this.mEapType == 18 || this.mEapType == 23 || this.mEapType == 50) {
                return true;
            }
            Log.d(Credential.TAG, "Invalid EAP Type for SIM credential: " + this.mEapType);
            return false;
        }

        private boolean verifyImsi() {
            if (TextUtils.isEmpty(this.mImsi)) {
                Log.d(Credential.TAG, "Missing IMSI");
                return false;
            } else if (this.mImsi.length() > 15) {
                Log.d(Credential.TAG, "IMSI exceeding maximum length: " + this.mImsi.length());
                return false;
            } else {
                char stopChar = 0;
                int nonDigit = 0;
                while (nonDigit < this.mImsi.length()) {
                    stopChar = this.mImsi.charAt(nonDigit);
                    if (stopChar < '0' || stopChar > '9') {
                        break;
                    }
                    nonDigit++;
                }
                if (nonDigit == this.mImsi.length()) {
                    return true;
                }
                return nonDigit == this.mImsi.length() + -1 && stopChar == '*';
            }
        }
    }

    public static final class UserCredential implements Parcelable {
        public static final String AUTH_METHOD_MSCHAP = "MS-CHAP";
        public static final String AUTH_METHOD_MSCHAPV2 = "MS-CHAP-V2";
        public static final String AUTH_METHOD_PAP = "PAP";
        public static final Creator<UserCredential> CREATOR = new Creator<UserCredential>() {
            public UserCredential createFromParcel(Parcel in) {
                boolean z = true;
                UserCredential userCredential = new UserCredential();
                userCredential.setUsername(in.readString());
                userCredential.setPassword(in.readString());
                userCredential.setMachineManaged(in.readInt() != 0);
                userCredential.setSoftTokenApp(in.readString());
                if (in.readInt() == 0) {
                    z = false;
                }
                userCredential.setAbleToShare(z);
                userCredential.setEapType(in.readInt());
                userCredential.setNonEapInnerMethod(in.readString());
                return userCredential;
            }

            public UserCredential[] newArray(int size) {
                return new UserCredential[size];
            }
        };
        private static final int MAX_PASSWORD_BYTES = 255;
        private static final int MAX_USERNAME_BYTES = 63;
        private static final Set<String> SUPPORTED_AUTH = new HashSet(Arrays.asList(new String[]{AUTH_METHOD_PAP, AUTH_METHOD_MSCHAP, AUTH_METHOD_MSCHAPV2}));
        private boolean mAbleToShare = false;
        private int mEapType = Integer.MIN_VALUE;
        private boolean mMachineManaged = false;
        private String mNonEapInnerMethod = null;
        private String mPassword = null;
        private String mSoftTokenApp = null;
        private String mUsername = null;

        public void setUsername(String username) {
            this.mUsername = username;
        }

        public String getUsername() {
            return this.mUsername;
        }

        public void setPassword(String password) {
            this.mPassword = password;
        }

        public String getPassword() {
            return this.mPassword;
        }

        public void setMachineManaged(boolean machineManaged) {
            this.mMachineManaged = machineManaged;
        }

        public boolean getMachineManaged() {
            return this.mMachineManaged;
        }

        public void setSoftTokenApp(String softTokenApp) {
            this.mSoftTokenApp = softTokenApp;
        }

        public String getSoftTokenApp() {
            return this.mSoftTokenApp;
        }

        public void setAbleToShare(boolean ableToShare) {
            this.mAbleToShare = ableToShare;
        }

        public boolean getAbleToShare() {
            return this.mAbleToShare;
        }

        public void setEapType(int eapType) {
            this.mEapType = eapType;
        }

        public int getEapType() {
            return this.mEapType;
        }

        public void setNonEapInnerMethod(String nonEapInnerMethod) {
            this.mNonEapInnerMethod = nonEapInnerMethod;
        }

        public String getNonEapInnerMethod() {
            return this.mNonEapInnerMethod;
        }

        public UserCredential(UserCredential source) {
            if (source != null) {
                this.mUsername = source.mUsername;
                this.mPassword = source.mPassword;
                this.mMachineManaged = source.mMachineManaged;
                this.mSoftTokenApp = source.mSoftTokenApp;
                this.mAbleToShare = source.mAbleToShare;
                this.mEapType = source.mEapType;
                this.mNonEapInnerMethod = source.mNonEapInnerMethod;
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
            dest.writeString(this.mUsername);
            dest.writeString(this.mPassword);
            dest.writeInt(this.mMachineManaged ? 1 : 0);
            dest.writeString(this.mSoftTokenApp);
            if (!this.mAbleToShare) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.mEapType);
            dest.writeString(this.mNonEapInnerMethod);
        }

        public boolean equals(Object thatObject) {
            boolean z = false;
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof UserCredential)) {
                return false;
            }
            UserCredential that = (UserCredential) thatObject;
            if (TextUtils.equals(this.mUsername, that.mUsername) && TextUtils.equals(this.mPassword, that.mPassword) && this.mMachineManaged == that.mMachineManaged && TextUtils.equals(this.mSoftTokenApp, that.mSoftTokenApp) && this.mAbleToShare == that.mAbleToShare && this.mEapType == that.mEapType) {
                z = TextUtils.equals(this.mNonEapInnerMethod, that.mNonEapInnerMethod);
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mUsername, this.mPassword, Boolean.valueOf(this.mMachineManaged), this.mSoftTokenApp, Boolean.valueOf(this.mAbleToShare), Integer.valueOf(this.mEapType), this.mNonEapInnerMethod});
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Username: ").append(this.mUsername).append("\n");
            builder.append("MachineManaged: ").append(this.mMachineManaged).append("\n");
            builder.append("SoftTokenApp: ").append(this.mSoftTokenApp).append("\n");
            builder.append("AbleToShare: ").append(this.mAbleToShare).append("\n");
            builder.append("EAPType: ").append(this.mEapType).append("\n");
            builder.append("AuthMethod: ").append(this.mNonEapInnerMethod).append("\n");
            return builder.toString();
        }

        public boolean validate() {
            if (TextUtils.isEmpty(this.mUsername)) {
                Log.d(Credential.TAG, "Missing username");
                return false;
            } else if (this.mUsername.getBytes(StandardCharsets.UTF_8).length > 63) {
                Log.d(Credential.TAG, "username exceeding maximum length: " + this.mUsername.getBytes(StandardCharsets.UTF_8).length);
                return false;
            } else if (TextUtils.isEmpty(this.mPassword)) {
                Log.d(Credential.TAG, "Missing password");
                return false;
            } else if (this.mPassword.getBytes(StandardCharsets.UTF_8).length > 255) {
                Log.d(Credential.TAG, "password exceeding maximum length: " + this.mPassword.getBytes(StandardCharsets.UTF_8).length);
                return false;
            } else if (this.mEapType != 21) {
                Log.d(Credential.TAG, "Invalid EAP Type for user credential: " + this.mEapType);
                return false;
            } else if (SUPPORTED_AUTH.contains(this.mNonEapInnerMethod)) {
                return true;
            } else {
                Log.d(Credential.TAG, "Invalid non-EAP inner method for EAP-TTLS: " + this.mNonEapInnerMethod);
                return false;
            }
        }
    }

    public void setCreationTimeInMillis(long creationTimeInMillis) {
        this.mCreationTimeInMillis = creationTimeInMillis;
    }

    public long getCreationTimeInMillis() {
        return this.mCreationTimeInMillis;
    }

    public void setExpirationTimeInMillis(long expirationTimeInMillis) {
        this.mExpirationTimeInMillis = expirationTimeInMillis;
    }

    public long getExpirationTimeInMillis() {
        return this.mExpirationTimeInMillis;
    }

    public void setRealm(String realm) {
        this.mRealm = realm;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public void setCheckAaaServerCertStatus(boolean checkAaaServerCertStatus) {
        this.mCheckAaaServerCertStatus = checkAaaServerCertStatus;
    }

    public boolean getCheckAaaServerCertStatus() {
        return this.mCheckAaaServerCertStatus;
    }

    public void setUserCredential(UserCredential userCredential) {
        this.mUserCredential = userCredential;
    }

    public UserCredential getUserCredential() {
        return this.mUserCredential;
    }

    public void setCertCredential(CertificateCredential certCredential) {
        this.mCertCredential = certCredential;
    }

    public CertificateCredential getCertCredential() {
        return this.mCertCredential;
    }

    public void setSimCredential(SimCredential simCredential) {
        this.mSimCredential = simCredential;
    }

    public SimCredential getSimCredential() {
        return this.mSimCredential;
    }

    public void setCaCertificate(X509Certificate caCertificate) {
        this.mCaCertificate = caCertificate;
    }

    public X509Certificate getCaCertificate() {
        return this.mCaCertificate;
    }

    public void setClientCertificateChain(X509Certificate[] certificateChain) {
        this.mClientCertificateChain = certificateChain;
    }

    public X509Certificate[] getClientCertificateChain() {
        return this.mClientCertificateChain;
    }

    public void setClientPrivateKey(PrivateKey clientPrivateKey) {
        this.mClientPrivateKey = clientPrivateKey;
    }

    public PrivateKey getClientPrivateKey() {
        return this.mClientPrivateKey;
    }

    public Credential(Credential source) {
        if (source != null) {
            this.mCreationTimeInMillis = source.mCreationTimeInMillis;
            this.mExpirationTimeInMillis = source.mExpirationTimeInMillis;
            this.mRealm = source.mRealm;
            this.mCheckAaaServerCertStatus = source.mCheckAaaServerCertStatus;
            if (source.mUserCredential != null) {
                this.mUserCredential = new UserCredential(source.mUserCredential);
            }
            if (source.mCertCredential != null) {
                this.mCertCredential = new CertificateCredential(source.mCertCredential);
            }
            if (source.mSimCredential != null) {
                this.mSimCredential = new SimCredential(source.mSimCredential);
            }
            if (source.mClientCertificateChain != null) {
                this.mClientCertificateChain = (X509Certificate[]) Arrays.copyOf(source.mClientCertificateChain, source.mClientCertificateChain.length);
            }
            this.mCaCertificate = source.mCaCertificate;
            this.mClientPrivateKey = source.mClientPrivateKey;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCreationTimeInMillis);
        dest.writeLong(this.mExpirationTimeInMillis);
        dest.writeString(this.mRealm);
        dest.writeInt(this.mCheckAaaServerCertStatus ? 1 : 0);
        dest.writeParcelable(this.mUserCredential, flags);
        dest.writeParcelable(this.mCertCredential, flags);
        dest.writeParcelable(this.mSimCredential, flags);
        ParcelUtil.writeCertificate(dest, this.mCaCertificate);
        ParcelUtil.writeCertificates(dest, this.mClientCertificateChain);
        ParcelUtil.writePrivateKey(dest, this.mClientPrivateKey);
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof Credential)) {
            return false;
        }
        Credential that = (Credential) thatObject;
        if (TextUtils.equals(this.mRealm, that.mRealm) && this.mCreationTimeInMillis == that.mCreationTimeInMillis && this.mExpirationTimeInMillis == that.mExpirationTimeInMillis && this.mCheckAaaServerCertStatus == that.mCheckAaaServerCertStatus && (this.mUserCredential != null ? !this.mUserCredential.equals(that.mUserCredential) : that.mUserCredential != null) && (this.mCertCredential != null ? !this.mCertCredential.equals(that.mCertCredential) : that.mCertCredential != null) && (this.mSimCredential != null ? !this.mSimCredential.equals(that.mSimCredential) : that.mSimCredential != null) && isX509CertificateEquals(this.mCaCertificate, that.mCaCertificate) && isX509CertificatesEquals(this.mClientCertificateChain, that.mClientCertificateChain)) {
            z = isPrivateKeyEquals(this.mClientPrivateKey, that.mClientPrivateKey);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mRealm, Long.valueOf(this.mCreationTimeInMillis), Long.valueOf(this.mExpirationTimeInMillis), Boolean.valueOf(this.mCheckAaaServerCertStatus), this.mUserCredential, this.mCertCredential, this.mSimCredential, this.mCaCertificate, this.mClientCertificateChain, this.mClientPrivateKey});
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Realm: ").append(this.mRealm).append("\n");
        builder.append("CreationTime: ").append(this.mCreationTimeInMillis != Long.MIN_VALUE ? new Date(this.mCreationTimeInMillis) : "Not specified").append("\n");
        builder.append("ExpirationTime: ").append(this.mExpirationTimeInMillis != Long.MIN_VALUE ? new Date(this.mExpirationTimeInMillis) : "Not specified").append("\n");
        builder.append("CheckAAAServerStatus: ").append(this.mCheckAaaServerCertStatus).append("\n");
        if (this.mUserCredential != null) {
            builder.append("UserCredential Begin ---\n");
            builder.append(this.mUserCredential);
            builder.append("UserCredential End ---\n");
        }
        if (this.mCertCredential != null) {
            builder.append("CertificateCredential Begin ---\n");
            builder.append(this.mCertCredential);
            builder.append("CertificateCredential End ---\n");
        }
        if (this.mSimCredential != null) {
            builder.append("SIMCredential Begin ---\n");
            builder.append(this.mSimCredential);
            builder.append("SIMCredential End ---\n");
        }
        return builder.toString();
    }

    public boolean validate() {
        if (TextUtils.isEmpty(this.mRealm)) {
            Log.d(TAG, "Missing realm");
            return false;
        } else if (this.mRealm.getBytes(StandardCharsets.UTF_8).length > 253) {
            Log.d(TAG, "realm exceeding maximum length: " + this.mRealm.getBytes(StandardCharsets.UTF_8).length);
            return false;
        } else {
            if (this.mUserCredential != null) {
                if (!verifyUserCredential()) {
                    return false;
                }
            } else if (this.mCertCredential != null) {
                if (!verifyCertCredential()) {
                    return false;
                }
            } else if (this.mSimCredential == null) {
                Log.d(TAG, "Missing required credential");
                return false;
            } else if (!verifySimCredential()) {
                return false;
            }
            return true;
        }
    }

    private boolean verifyUserCredential() {
        if (this.mUserCredential == null) {
            Log.d(TAG, "Missing user credential");
            return false;
        } else if (this.mCertCredential != null || this.mSimCredential != null) {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        } else if (!this.mUserCredential.validate()) {
            return false;
        } else {
            if (this.mCaCertificate != null) {
                return true;
            }
            Log.d(TAG, "Missing CA Certificate for user credential");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x005e A:{ExcHandler: java.security.NoSuchAlgorithmException (r0_0 'e' java.security.GeneralSecurityException), Splitter: B:21:0x0046} */
    /* JADX WARNING: Missing block: B:26:0x005e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x005f, code:
            android.util.Log.d(TAG, "Failed to verify SHA-256 fingerprint: " + r0.getMessage());
     */
    /* JADX WARNING: Missing block: B:28:0x007d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean verifyCertCredential() {
        if (this.mCertCredential == null) {
            Log.d(TAG, "Missing certificate credential");
            return false;
        } else if (this.mUserCredential != null || this.mSimCredential != null) {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        } else if (!this.mCertCredential.validate()) {
            return false;
        } else {
            if (this.mCaCertificate == null) {
                Log.d(TAG, "Missing CA Certificate for certificate credential");
                return false;
            } else if (this.mClientPrivateKey == null) {
                Log.d(TAG, "Missing client private key for certificate credential");
                return false;
            } else {
                try {
                    if (verifySha256Fingerprint(this.mClientCertificateChain, this.mCertCredential.getCertSha256Fingerprint())) {
                        return true;
                    }
                    Log.d(TAG, "SHA-256 fingerprint mismatch");
                    return false;
                } catch (GeneralSecurityException e) {
                }
            }
        }
    }

    private boolean verifySimCredential() {
        if (this.mSimCredential == null) {
            Log.d(TAG, "Missing SIM credential");
            return false;
        } else if (this.mUserCredential == null && this.mCertCredential == null) {
            return this.mSimCredential.validate();
        } else {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        }
    }

    private static boolean isPrivateKeyEquals(PrivateKey key1, PrivateKey key2) {
        boolean z = false;
        if (key1 == null && key2 == null) {
            return true;
        }
        if (key1 == null || key2 == null) {
            return false;
        }
        if (TextUtils.equals(key1.getAlgorithm(), key2.getAlgorithm())) {
            z = Arrays.equals(key1.getEncoded(), key2.getEncoded());
        }
        return z;
    }

    private static boolean isX509CertificateEquals(X509Certificate cert1, X509Certificate cert2) {
        if (cert1 == null && cert2 == null) {
            return true;
        }
        if (cert1 == null || cert2 == null) {
            return false;
        }
        boolean result = false;
        try {
            result = Arrays.equals(cert1.getEncoded(), cert2.getEncoded());
        } catch (CertificateEncodingException e) {
            Log.d(TAG, "Failed to equals result " + e.getMessage());
        }
        return result;
    }

    /* JADX WARNING: Missing block: B:6:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isX509CertificatesEquals(X509Certificate[] certs1, X509Certificate[] certs2) {
        if (certs1 == null && certs2 == null) {
            return true;
        }
        if (certs1 == null || certs2 == null || certs1.length != certs2.length) {
            return false;
        }
        for (int i = 0; i < certs1.length; i++) {
            if (!isX509CertificateEquals(certs1[i], certs2[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean verifySha256Fingerprint(X509Certificate[] certChain, byte[] expectedFingerprint) throws NoSuchAlgorithmException, CertificateEncodingException {
        if (certChain == null) {
            return false;
        }
        MessageDigest digester = MessageDigest.getInstance("SHA-256");
        for (X509Certificate certificate : certChain) {
            digester.reset();
            if (Arrays.equals(expectedFingerprint, digester.digest(certificate.getEncoded()))) {
                return true;
            }
        }
        return false;
    }
}
