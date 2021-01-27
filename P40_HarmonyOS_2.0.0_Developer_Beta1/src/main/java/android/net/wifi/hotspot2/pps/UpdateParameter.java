package android.net.wifi.hotspot2.pps;

import android.net.wifi.ParcelUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;

public final class UpdateParameter implements Parcelable {
    private static final int CERTIFICATE_SHA256_BYTES = 32;
    public static final Parcelable.Creator<UpdateParameter> CREATOR = new Parcelable.Creator<UpdateParameter>() {
        /* class android.net.wifi.hotspot2.pps.UpdateParameter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UpdateParameter createFromParcel(Parcel in) {
            UpdateParameter updateParam = new UpdateParameter();
            updateParam.setUpdateIntervalInMinutes(in.readLong());
            updateParam.setUpdateMethod(in.readString());
            updateParam.setRestriction(in.readString());
            updateParam.setServerUri(in.readString());
            updateParam.setUsername(in.readString());
            updateParam.setBase64EncodedPassword(in.readString());
            updateParam.setTrustRootCertUrl(in.readString());
            updateParam.setTrustRootCertSha256Fingerprint(in.createByteArray());
            updateParam.setCaCertificate(ParcelUtil.readCertificate(in));
            return updateParam;
        }

        @Override // android.os.Parcelable.Creator
        public UpdateParameter[] newArray(int size) {
            return new UpdateParameter[size];
        }
    };
    private static final int MAX_PASSWORD_BYTES = 255;
    private static final int MAX_URI_BYTES = 1023;
    private static final int MAX_URL_BYTES = 1023;
    private static final int MAX_USERNAME_BYTES = 63;
    private static final String TAG = "UpdateParameter";
    public static final long UPDATE_CHECK_INTERVAL_NEVER = 4294967295L;
    public static final String UPDATE_METHOD_OMADM = "OMA-DM-ClientInitiated";
    public static final String UPDATE_METHOD_SSP = "SSP-ClientInitiated";
    public static final String UPDATE_RESTRICTION_HOMESP = "HomeSP";
    public static final String UPDATE_RESTRICTION_ROAMING_PARTNER = "RoamingPartner";
    public static final String UPDATE_RESTRICTION_UNRESTRICTED = "Unrestricted";
    private String mBase64EncodedPassword = null;
    private X509Certificate mCaCertificate;
    private String mRestriction = null;
    private String mServerUri = null;
    private byte[] mTrustRootCertSha256Fingerprint = null;
    private String mTrustRootCertUrl = null;
    private long mUpdateIntervalInMinutes = Long.MIN_VALUE;
    private String mUpdateMethod = null;
    private String mUsername = null;

    public void setUpdateIntervalInMinutes(long updateIntervalInMinutes) {
        this.mUpdateIntervalInMinutes = updateIntervalInMinutes;
    }

    public long getUpdateIntervalInMinutes() {
        return this.mUpdateIntervalInMinutes;
    }

    public void setUpdateMethod(String updateMethod) {
        this.mUpdateMethod = updateMethod;
    }

    public String getUpdateMethod() {
        return this.mUpdateMethod;
    }

    public void setRestriction(String restriction) {
        this.mRestriction = restriction;
    }

    public String getRestriction() {
        return this.mRestriction;
    }

    public void setServerUri(String serverUri) {
        this.mServerUri = serverUri;
    }

    public String getServerUri() {
        return this.mServerUri;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public void setBase64EncodedPassword(String password) {
        this.mBase64EncodedPassword = password;
    }

    public String getBase64EncodedPassword() {
        return this.mBase64EncodedPassword;
    }

    public void setTrustRootCertUrl(String trustRootCertUrl) {
        this.mTrustRootCertUrl = trustRootCertUrl;
    }

    public String getTrustRootCertUrl() {
        return this.mTrustRootCertUrl;
    }

    public void setTrustRootCertSha256Fingerprint(byte[] fingerprint) {
        this.mTrustRootCertSha256Fingerprint = fingerprint;
    }

    public byte[] getTrustRootCertSha256Fingerprint() {
        return this.mTrustRootCertSha256Fingerprint;
    }

    public void setCaCertificate(X509Certificate caCertificate) {
        this.mCaCertificate = caCertificate;
    }

    public X509Certificate getCaCertificate() {
        return this.mCaCertificate;
    }

    public UpdateParameter() {
    }

    public UpdateParameter(UpdateParameter source) {
        if (source != null) {
            this.mUpdateIntervalInMinutes = source.mUpdateIntervalInMinutes;
            this.mUpdateMethod = source.mUpdateMethod;
            this.mRestriction = source.mRestriction;
            this.mServerUri = source.mServerUri;
            this.mUsername = source.mUsername;
            this.mBase64EncodedPassword = source.mBase64EncodedPassword;
            this.mTrustRootCertUrl = source.mTrustRootCertUrl;
            byte[] bArr = source.mTrustRootCertSha256Fingerprint;
            if (bArr != null) {
                this.mTrustRootCertSha256Fingerprint = Arrays.copyOf(bArr, bArr.length);
            }
            this.mCaCertificate = source.mCaCertificate;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUpdateIntervalInMinutes);
        dest.writeString(this.mUpdateMethod);
        dest.writeString(this.mRestriction);
        dest.writeString(this.mServerUri);
        dest.writeString(this.mUsername);
        dest.writeString(this.mBase64EncodedPassword);
        dest.writeString(this.mTrustRootCertUrl);
        dest.writeByteArray(this.mTrustRootCertSha256Fingerprint);
        ParcelUtil.writeCertificate(dest, this.mCaCertificate);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof UpdateParameter)) {
            return false;
        }
        UpdateParameter that = (UpdateParameter) thatObject;
        if (this.mUpdateIntervalInMinutes != that.mUpdateIntervalInMinutes || !TextUtils.equals(this.mUpdateMethod, that.mUpdateMethod) || !TextUtils.equals(this.mRestriction, that.mRestriction) || !TextUtils.equals(this.mServerUri, that.mServerUri) || !TextUtils.equals(this.mUsername, that.mUsername) || !TextUtils.equals(this.mBase64EncodedPassword, that.mBase64EncodedPassword) || !TextUtils.equals(this.mTrustRootCertUrl, that.mTrustRootCertUrl) || !Arrays.equals(this.mTrustRootCertSha256Fingerprint, that.mTrustRootCertSha256Fingerprint) || !Credential.isX509CertificateEquals(this.mCaCertificate, that.mCaCertificate)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.mUpdateIntervalInMinutes), this.mUpdateMethod, this.mRestriction, this.mServerUri, this.mUsername, this.mBase64EncodedPassword, this.mTrustRootCertUrl, Integer.valueOf(Arrays.hashCode(this.mTrustRootCertSha256Fingerprint)), this.mCaCertificate);
    }

    public String toString() {
        return "UpdateInterval: " + this.mUpdateIntervalInMinutes + "\nUpdateMethod: " + this.mUpdateMethod + "\nRestriction: " + this.mRestriction + "\nServerURI: " + this.mServerUri + "\nUsername: " + this.mUsername + "\nTrustRootCertURL: " + this.mTrustRootCertUrl + "\n";
    }

    public boolean validate() {
        long j = this.mUpdateIntervalInMinutes;
        if (j == Long.MIN_VALUE) {
            Log.d(TAG, "Update interval not specified");
            return false;
        } else if (j == 4294967295L) {
            return true;
        } else {
            if (!TextUtils.equals(this.mUpdateMethod, UPDATE_METHOD_OMADM) && !TextUtils.equals(this.mUpdateMethod, UPDATE_METHOD_SSP)) {
                Log.d(TAG, "Unknown update method: " + this.mUpdateMethod);
                return false;
            } else if (!TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_HOMESP) && !TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_ROAMING_PARTNER) && !TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_UNRESTRICTED)) {
                Log.d(TAG, "Unknown restriction: " + this.mRestriction);
                return false;
            } else if (TextUtils.isEmpty(this.mServerUri)) {
                Log.d(TAG, "Missing update server URI");
                return false;
            } else if (this.mServerUri.getBytes(StandardCharsets.UTF_8).length > 1023) {
                Log.d(TAG, "URI bytes exceeded the max: " + this.mServerUri.getBytes(StandardCharsets.UTF_8).length);
                return false;
            } else if (TextUtils.isEmpty(this.mUsername)) {
                Log.d(TAG, "Missing username");
                return false;
            } else if (this.mUsername.getBytes(StandardCharsets.UTF_8).length > 63) {
                Log.d(TAG, "Username bytes exceeded the max: " + this.mUsername.getBytes(StandardCharsets.UTF_8).length);
                return false;
            } else if (TextUtils.isEmpty(this.mBase64EncodedPassword)) {
                Log.d(TAG, "Missing username");
                return false;
            } else if (this.mBase64EncodedPassword.getBytes(StandardCharsets.UTF_8).length > 255) {
                Log.d(TAG, "Password bytes exceeded the max: " + this.mBase64EncodedPassword.getBytes(StandardCharsets.UTF_8).length);
                return false;
            } else {
                try {
                    Base64.decode(this.mBase64EncodedPassword, 0);
                    if (TextUtils.isEmpty(this.mTrustRootCertUrl)) {
                        Log.d(TAG, "Missing trust root certificate URL");
                        return false;
                    } else if (this.mTrustRootCertUrl.getBytes(StandardCharsets.UTF_8).length > 1023) {
                        Log.d(TAG, "Trust root cert URL bytes exceeded the max: " + this.mTrustRootCertUrl.getBytes(StandardCharsets.UTF_8).length);
                        return false;
                    } else {
                        byte[] bArr = this.mTrustRootCertSha256Fingerprint;
                        if (bArr == null) {
                            Log.d(TAG, "Missing trust root certificate SHA-256 fingerprint");
                            return false;
                        } else if (bArr.length == 32) {
                            return true;
                        } else {
                            Log.d(TAG, "Incorrect size of trust root certificate SHA-256 fingerprint: " + this.mTrustRootCertSha256Fingerprint.length);
                            return false;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "Invalid encoding for password: " + this.mBase64EncodedPassword);
                    return false;
                }
            }
        }
    }
}
