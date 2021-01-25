package android.net.wifi.hotspot2.pps;

import android.net.wifi.ParcelUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import java.nio.charset.StandardCharsets;
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
    public static final Parcelable.Creator<Credential> CREATOR = new Parcelable.Creator<Credential>() {
        /* class android.net.wifi.hotspot2.pps.Credential.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Credential createFromParcel(Parcel in) {
            Credential credential = new Credential();
            credential.setCreationTimeInMillis(in.readLong());
            credential.setExpirationTimeInMillis(in.readLong());
            credential.setRealm(in.readString());
            credential.setCheckAaaServerCertStatus(in.readInt() != 0);
            credential.setUserCredential((UserCredential) in.readParcelable(null));
            credential.setCertCredential((CertificateCredential) in.readParcelable(null));
            credential.setSimCredential((SimCredential) in.readParcelable(null));
            credential.setCaCertificates(ParcelUtil.readCertificates(in));
            credential.setClientCertificateChain(ParcelUtil.readCertificates(in));
            credential.setClientPrivateKey(ParcelUtil.readPrivateKey(in));
            return credential;
        }

        @Override // android.os.Parcelable.Creator
        public Credential[] newArray(int size) {
            return new Credential[size];
        }
    };
    private static final int MAX_REALM_BYTES = 253;
    private static final String TAG = "Credential";
    private X509Certificate[] mCaCertificates = null;
    private CertificateCredential mCertCredential = null;
    private boolean mCheckAaaServerCertStatus = false;
    private X509Certificate[] mClientCertificateChain = null;
    private PrivateKey mClientPrivateKey = null;
    private long mCreationTimeInMillis = Long.MIN_VALUE;
    private long mExpirationTimeInMillis = Long.MIN_VALUE;
    private String mRealm = null;
    private SimCredential mSimCredential = null;
    private UserCredential mUserCredential = null;

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

    public static final class UserCredential implements Parcelable {
        public static final String AUTH_METHOD_MSCHAP = "MS-CHAP";
        public static final String AUTH_METHOD_MSCHAPV2 = "MS-CHAP-V2";
        public static final String AUTH_METHOD_PAP = "PAP";
        public static final Parcelable.Creator<UserCredential> CREATOR = new Parcelable.Creator<UserCredential>() {
            /* class android.net.wifi.hotspot2.pps.Credential.UserCredential.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public UserCredential createFromParcel(Parcel in) {
                UserCredential userCredential = new UserCredential();
                userCredential.setUsername(in.readString());
                userCredential.setPassword(in.readString());
                boolean z = true;
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

            @Override // android.os.Parcelable.Creator
            public UserCredential[] newArray(int size) {
                return new UserCredential[size];
            }
        };
        private static final int MAX_PASSWORD_BYTES = 255;
        private static final int MAX_USERNAME_BYTES = 63;
        private static final Set<String> SUPPORTED_AUTH = new HashSet(Arrays.asList(AUTH_METHOD_PAP, AUTH_METHOD_MSCHAP, AUTH_METHOD_MSCHAPV2));
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

        public UserCredential() {
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

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mUsername);
            dest.writeString(this.mPassword);
            dest.writeInt(this.mMachineManaged ? 1 : 0);
            dest.writeString(this.mSoftTokenApp);
            dest.writeInt(this.mAbleToShare ? 1 : 0);
            dest.writeInt(this.mEapType);
            dest.writeString(this.mNonEapInnerMethod);
        }

        public boolean equals(Object thatObject) {
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof UserCredential)) {
                return false;
            }
            UserCredential that = (UserCredential) thatObject;
            if (!TextUtils.equals(this.mUsername, that.mUsername) || !TextUtils.equals(this.mPassword, that.mPassword) || this.mMachineManaged != that.mMachineManaged || !TextUtils.equals(this.mSoftTokenApp, that.mSoftTokenApp) || this.mAbleToShare != that.mAbleToShare || this.mEapType != that.mEapType || !TextUtils.equals(this.mNonEapInnerMethod, that.mNonEapInnerMethod)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mUsername, this.mPassword, Boolean.valueOf(this.mMachineManaged), this.mSoftTokenApp, Boolean.valueOf(this.mAbleToShare), Integer.valueOf(this.mEapType), this.mNonEapInnerMethod);
        }

        public String toString() {
            return "Username: " + this.mUsername + "\nMachineManaged: " + this.mMachineManaged + "\nSoftTokenApp: " + this.mSoftTokenApp + "\nAbleToShare: " + this.mAbleToShare + "\nEAPType: " + this.mEapType + "\nAuthMethod: " + this.mNonEapInnerMethod + "\n";
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

    public void setUserCredential(UserCredential userCredential) {
        this.mUserCredential = userCredential;
    }

    public UserCredential getUserCredential() {
        return this.mUserCredential;
    }

    public static final class CertificateCredential implements Parcelable {
        private static final int CERT_SHA256_FINGER_PRINT_LENGTH = 32;
        public static final String CERT_TYPE_X509V3 = "x509v3";
        public static final Parcelable.Creator<CertificateCredential> CREATOR = new Parcelable.Creator<CertificateCredential>() {
            /* class android.net.wifi.hotspot2.pps.Credential.CertificateCredential.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public CertificateCredential createFromParcel(Parcel in) {
                CertificateCredential certCredential = new CertificateCredential();
                certCredential.setCertType(in.readString());
                certCredential.setCertSha256Fingerprint(in.createByteArray());
                return certCredential;
            }

            @Override // android.os.Parcelable.Creator
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

        public CertificateCredential() {
        }

        public CertificateCredential(CertificateCredential source) {
            if (source != null) {
                this.mCertType = source.mCertType;
                byte[] bArr = source.mCertSha256Fingerprint;
                if (bArr != null) {
                    this.mCertSha256Fingerprint = Arrays.copyOf(bArr, bArr.length);
                }
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mCertType);
            dest.writeByteArray(this.mCertSha256Fingerprint);
        }

        public boolean equals(Object thatObject) {
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof CertificateCredential)) {
                return false;
            }
            CertificateCredential that = (CertificateCredential) thatObject;
            if (!TextUtils.equals(this.mCertType, that.mCertType) || !Arrays.equals(this.mCertSha256Fingerprint, that.mCertSha256Fingerprint)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mCertType, Integer.valueOf(Arrays.hashCode(this.mCertSha256Fingerprint)));
        }

        public String toString() {
            return "CertificateType: " + this.mCertType + "\n";
        }

        public boolean validate() {
            if (!TextUtils.equals(CERT_TYPE_X509V3, this.mCertType)) {
                Log.d(Credential.TAG, "Unsupported certificate type: " + this.mCertType);
                return false;
            }
            byte[] bArr = this.mCertSha256Fingerprint;
            if (bArr != null && bArr.length == 32) {
                return true;
            }
            Log.d(Credential.TAG, "Invalid SHA-256 fingerprint");
            return false;
        }
    }

    public void setCertCredential(CertificateCredential certCredential) {
        this.mCertCredential = certCredential;
    }

    public CertificateCredential getCertCredential() {
        return this.mCertCredential;
    }

    public static final class SimCredential implements Parcelable {
        public static final Parcelable.Creator<SimCredential> CREATOR = new Parcelable.Creator<SimCredential>() {
            /* class android.net.wifi.hotspot2.pps.Credential.SimCredential.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SimCredential createFromParcel(Parcel in) {
                SimCredential simCredential = new SimCredential();
                simCredential.setImsi(in.readString());
                simCredential.setEapType(in.readInt());
                return simCredential;
            }

            @Override // android.os.Parcelable.Creator
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

        public SimCredential() {
        }

        public SimCredential(SimCredential source) {
            if (source != null) {
                this.mImsi = source.mImsi;
                this.mEapType = source.mEapType;
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public boolean equals(Object thatObject) {
            if (this == thatObject) {
                return true;
            }
            if (!(thatObject instanceof SimCredential)) {
                return false;
            }
            SimCredential that = (SimCredential) thatObject;
            if (!TextUtils.equals(this.mImsi, that.mImsi) || this.mEapType != that.mEapType) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mImsi, Integer.valueOf(this.mEapType));
        }

        public String toString() {
            return "IMSI: " + this.mImsi + "\nEAPType: " + this.mEapType + "\n";
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mImsi);
            dest.writeInt(this.mEapType);
        }

        public boolean validate() {
            if (!verifyImsi()) {
                return false;
            }
            int i = this.mEapType;
            if (i == 18 || i == 23 || i == 50) {
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
                while (nonDigit < this.mImsi.length() && (stopChar = this.mImsi.charAt(nonDigit)) >= '0' && stopChar <= '9') {
                    nonDigit++;
                }
                if (nonDigit == this.mImsi.length()) {
                    return true;
                }
                if (nonDigit == this.mImsi.length() - 1 && stopChar == '*') {
                    return true;
                }
                return false;
            }
        }
    }

    public void setSimCredential(SimCredential simCredential) {
        this.mSimCredential = simCredential;
    }

    public SimCredential getSimCredential() {
        return this.mSimCredential;
    }

    public void setCaCertificate(X509Certificate caCertificate) {
        this.mCaCertificates = null;
        if (caCertificate != null) {
            this.mCaCertificates = new X509Certificate[]{caCertificate};
        }
    }

    public void setCaCertificates(X509Certificate[] caCertificates) {
        this.mCaCertificates = caCertificates;
    }

    public X509Certificate getCaCertificate() {
        X509Certificate[] x509CertificateArr = this.mCaCertificates;
        if (x509CertificateArr == null || x509CertificateArr.length > 1) {
            return null;
        }
        return x509CertificateArr[0];
    }

    public X509Certificate[] getCaCertificates() {
        return this.mCaCertificates;
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

    public Credential() {
    }

    public Credential(Credential source) {
        if (source != null) {
            this.mCreationTimeInMillis = source.mCreationTimeInMillis;
            this.mExpirationTimeInMillis = source.mExpirationTimeInMillis;
            this.mRealm = source.mRealm;
            this.mCheckAaaServerCertStatus = source.mCheckAaaServerCertStatus;
            UserCredential userCredential = source.mUserCredential;
            if (userCredential != null) {
                this.mUserCredential = new UserCredential(userCredential);
            }
            CertificateCredential certificateCredential = source.mCertCredential;
            if (certificateCredential != null) {
                this.mCertCredential = new CertificateCredential(certificateCredential);
            }
            SimCredential simCredential = source.mSimCredential;
            if (simCredential != null) {
                this.mSimCredential = new SimCredential(simCredential);
            }
            X509Certificate[] x509CertificateArr = source.mClientCertificateChain;
            if (x509CertificateArr != null) {
                this.mClientCertificateChain = (X509Certificate[]) Arrays.copyOf(x509CertificateArr, x509CertificateArr.length);
            }
            X509Certificate[] x509CertificateArr2 = source.mCaCertificates;
            if (x509CertificateArr2 != null) {
                this.mCaCertificates = (X509Certificate[]) Arrays.copyOf(x509CertificateArr2, x509CertificateArr2.length);
            }
            this.mClientPrivateKey = source.mClientPrivateKey;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCreationTimeInMillis);
        dest.writeLong(this.mExpirationTimeInMillis);
        dest.writeString(this.mRealm);
        dest.writeInt(this.mCheckAaaServerCertStatus ? 1 : 0);
        dest.writeParcelable(this.mUserCredential, flags);
        dest.writeParcelable(this.mCertCredential, flags);
        dest.writeParcelable(this.mSimCredential, flags);
        ParcelUtil.writeCertificates(dest, this.mCaCertificates);
        ParcelUtil.writeCertificates(dest, this.mClientCertificateChain);
        ParcelUtil.writePrivateKey(dest, this.mClientPrivateKey);
    }

    public boolean equals(Object thatObject) {
        UserCredential userCredential;
        CertificateCredential certificateCredential;
        SimCredential simCredential;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof Credential)) {
            return false;
        }
        Credential that = (Credential) thatObject;
        if (!TextUtils.equals(this.mRealm, that.mRealm) || this.mCreationTimeInMillis != that.mCreationTimeInMillis || this.mExpirationTimeInMillis != that.mExpirationTimeInMillis || this.mCheckAaaServerCertStatus != that.mCheckAaaServerCertStatus || ((userCredential = this.mUserCredential) != null ? !userCredential.equals(that.mUserCredential) : that.mUserCredential != null) || ((certificateCredential = this.mCertCredential) != null ? !certificateCredential.equals(that.mCertCredential) : that.mCertCredential != null) || ((simCredential = this.mSimCredential) != null ? !simCredential.equals(that.mSimCredential) : that.mSimCredential != null) || !isX509CertificatesEquals(this.mCaCertificates, that.mCaCertificates) || !isX509CertificatesEquals(this.mClientCertificateChain, that.mClientCertificateChain) || !isPrivateKeyEquals(this.mClientPrivateKey, that.mClientPrivateKey)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.mCreationTimeInMillis), Long.valueOf(this.mExpirationTimeInMillis), this.mRealm, Boolean.valueOf(this.mCheckAaaServerCertStatus), this.mUserCredential, this.mCertCredential, this.mSimCredential, this.mClientPrivateKey, Integer.valueOf(Arrays.hashCode(this.mCaCertificates)), Integer.valueOf(Arrays.hashCode(this.mClientCertificateChain)));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Realm: ");
        builder.append(this.mRealm);
        builder.append("\n");
        builder.append("CreationTime: ");
        long j = this.mCreationTimeInMillis;
        Object obj = "Not specified";
        builder.append(j != Long.MIN_VALUE ? new Date(j) : obj);
        builder.append("\n");
        builder.append("ExpirationTime: ");
        long j2 = this.mExpirationTimeInMillis;
        if (j2 != Long.MIN_VALUE) {
            obj = new Date(j2);
        }
        builder.append(obj);
        builder.append("\n");
        builder.append("CheckAAAServerStatus: ");
        builder.append(this.mCheckAaaServerCertStatus);
        builder.append("\n");
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

    public boolean validate(boolean isR1) {
        if (TextUtils.isEmpty(this.mRealm)) {
            Log.d(TAG, "Missing realm");
            return false;
        } else if (this.mRealm.getBytes(StandardCharsets.UTF_8).length > 253) {
            Log.d(TAG, "realm exceeding maximum length: " + this.mRealm.getBytes(StandardCharsets.UTF_8).length);
            return false;
        } else if (this.mUserCredential != null) {
            if (!verifyUserCredential(isR1)) {
                return false;
            }
            return true;
        } else if (this.mCertCredential != null) {
            if (!verifyCertCredential(isR1)) {
                return false;
            }
            return true;
        } else if (this.mSimCredential == null) {
            Log.d(TAG, "Missing required credential");
            return false;
        } else if (!verifySimCredential()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean verifyUserCredential(boolean isR1) {
        UserCredential userCredential = this.mUserCredential;
        if (userCredential == null) {
            Log.d(TAG, "Missing user credential");
            return false;
        } else if (this.mCertCredential != null || this.mSimCredential != null) {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        } else if (!userCredential.validate()) {
            return false;
        } else {
            if (!isR1 || this.mCaCertificates != null) {
                return true;
            }
            Log.d(TAG, "Missing CA Certificate for user credential");
            return false;
        }
    }

    private boolean verifyCertCredential(boolean isR1) {
        CertificateCredential certificateCredential = this.mCertCredential;
        if (certificateCredential == null) {
            Log.d(TAG, "Missing certificate credential");
            return false;
        } else if (this.mUserCredential != null || this.mSimCredential != null) {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        } else if (!certificateCredential.validate()) {
            return false;
        } else {
            if (isR1 && this.mCaCertificates == null) {
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
                } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
                    Log.d(TAG, "Failed to verify SHA-256 fingerprint: " + e.getMessage());
                    return false;
                }
            }
        }
    }

    private boolean verifySimCredential() {
        SimCredential simCredential = this.mSimCredential;
        if (simCredential == null) {
            Log.d(TAG, "Missing SIM credential");
            return false;
        } else if (this.mUserCredential == null && this.mCertCredential == null) {
            return simCredential.validate();
        } else {
            Log.d(TAG, "Contained more than one type of credential");
            return false;
        }
    }

    private static boolean isPrivateKeyEquals(PrivateKey key1, PrivateKey key2) {
        if (key1 == null && key2 == null) {
            return true;
        }
        if (key1 == null || key2 == null) {
            return false;
        }
        if (!TextUtils.equals(key1.getAlgorithm(), key2.getAlgorithm()) || !Arrays.equals(key1.getEncoded(), key2.getEncoded())) {
            return false;
        }
        return true;
    }

    public static boolean isX509CertificateEquals(X509Certificate cert1, X509Certificate cert2) {
        if (cert1 == null && cert2 == null) {
            return true;
        }
        if (cert1 == null || cert2 == null) {
            return false;
        }
        try {
            return Arrays.equals(cert1.getEncoded(), cert2.getEncoded());
        } catch (CertificateEncodingException e) {
            Log.d(TAG, "Failed to equals result " + e.getMessage());
            return false;
        }
    }

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
        MessageDigest digester = MessageDigest.getInstance(KeyProperties.DIGEST_SHA256);
        for (X509Certificate certificate : certChain) {
            digester.reset();
            if (Arrays.equals(expectedFingerprint, digester.digest(certificate.getEncoded()))) {
                return true;
            }
        }
        return false;
    }
}
