package android.net.wifi.hotspot2.pps;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class UpdateParameter implements Parcelable {
    private static final int CERTIFICATE_SHA256_BYTES = 32;
    public static final Creator<UpdateParameter> CREATOR = new Creator<UpdateParameter>() {
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
            return updateParam;
        }

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

    public UpdateParameter(UpdateParameter source) {
        if (source != null) {
            this.mUpdateIntervalInMinutes = source.mUpdateIntervalInMinutes;
            this.mUpdateMethod = source.mUpdateMethod;
            this.mRestriction = source.mRestriction;
            this.mServerUri = source.mServerUri;
            this.mUsername = source.mUsername;
            this.mBase64EncodedPassword = source.mBase64EncodedPassword;
            this.mTrustRootCertUrl = source.mTrustRootCertUrl;
            if (source.mTrustRootCertSha256Fingerprint != null) {
                this.mTrustRootCertSha256Fingerprint = Arrays.copyOf(source.mTrustRootCertSha256Fingerprint, source.mTrustRootCertSha256Fingerprint.length);
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUpdateIntervalInMinutes);
        dest.writeString(this.mUpdateMethod);
        dest.writeString(this.mRestriction);
        dest.writeString(this.mServerUri);
        dest.writeString(this.mUsername);
        dest.writeString(this.mBase64EncodedPassword);
        dest.writeString(this.mTrustRootCertUrl);
        dest.writeByteArray(this.mTrustRootCertSha256Fingerprint);
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof UpdateParameter)) {
            return false;
        }
        UpdateParameter that = (UpdateParameter) thatObject;
        if (this.mUpdateIntervalInMinutes == that.mUpdateIntervalInMinutes && TextUtils.equals(this.mUpdateMethod, that.mUpdateMethod) && TextUtils.equals(this.mRestriction, that.mRestriction) && TextUtils.equals(this.mServerUri, that.mServerUri) && TextUtils.equals(this.mUsername, that.mUsername) && TextUtils.equals(this.mBase64EncodedPassword, that.mBase64EncodedPassword) && TextUtils.equals(this.mTrustRootCertUrl, that.mTrustRootCertUrl)) {
            z = Arrays.equals(this.mTrustRootCertSha256Fingerprint, that.mTrustRootCertSha256Fingerprint);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.mUpdateIntervalInMinutes), this.mUpdateMethod, this.mRestriction, this.mServerUri, this.mUsername, this.mBase64EncodedPassword, this.mTrustRootCertUrl, this.mTrustRootCertSha256Fingerprint});
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UpdateInterval: ").append(this.mUpdateIntervalInMinutes).append("\n");
        builder.append("UpdateMethod: ").append(this.mUpdateMethod).append("\n");
        builder.append("Restriction: ").append(this.mRestriction).append("\n");
        builder.append("ServerURI: ").append(this.mServerUri).append("\n");
        builder.append("Username: ").append(this.mUsername).append("\n");
        builder.append("TrustRootCertURL: ").append(this.mTrustRootCertUrl).append("\n");
        return builder.toString();
    }

    public boolean validate() {
        if (this.mUpdateIntervalInMinutes == Long.MIN_VALUE) {
            Log.d(TAG, "Update interval not specified");
            return false;
        } else if (this.mUpdateIntervalInMinutes == UPDATE_CHECK_INTERVAL_NEVER) {
            return true;
        } else {
            if (!TextUtils.equals(this.mUpdateMethod, UPDATE_METHOD_OMADM) && (TextUtils.equals(this.mUpdateMethod, UPDATE_METHOD_SSP) ^ 1) != 0) {
                Log.d(TAG, "Unknown update method: " + this.mUpdateMethod);
                return false;
            } else if (!TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_HOMESP) && (TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_ROAMING_PARTNER) ^ 1) != 0 && (TextUtils.equals(this.mRestriction, UPDATE_RESTRICTION_UNRESTRICTED) ^ 1) != 0) {
                Log.d(TAG, "Unknown restriction: " + this.mRestriction);
                return false;
            } else if (TextUtils.isEmpty(this.mServerUri)) {
                Log.d(TAG, "Missing update server URI");
                return false;
            } else if (this.mServerUri.getBytes(StandardCharsets.UTF_8).length > Process.MEDIA_RW_GID) {
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
                    } else if (this.mTrustRootCertUrl.getBytes(StandardCharsets.UTF_8).length > Process.MEDIA_RW_GID) {
                        Log.d(TAG, "Trust root cert URL bytes exceeded the max: " + this.mTrustRootCertUrl.getBytes(StandardCharsets.UTF_8).length);
                        return false;
                    } else if (this.mTrustRootCertSha256Fingerprint == null) {
                        Log.d(TAG, "Missing trust root certificate SHA-256 fingerprint");
                        return false;
                    } else if (this.mTrustRootCertSha256Fingerprint.length == 32) {
                        return true;
                    } else {
                        Log.d(TAG, "Incorrect size of trust root certificate SHA-256 fingerprint: " + this.mTrustRootCertSha256Fingerprint.length);
                        return false;
                    }
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "Invalid encoding for password: " + this.mBase64EncodedPassword);
                    return false;
                }
            }
        }
    }
}
