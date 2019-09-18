package android.telephony.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class DataProfile implements Parcelable {
    public static final Parcelable.Creator<DataProfile> CREATOR = new Parcelable.Creator<DataProfile>() {
        public DataProfile createFromParcel(Parcel source) {
            return new DataProfile(source);
        }

        public DataProfile[] newArray(int size) {
            return new DataProfile[size];
        }
    };
    public static final int TYPE_3GPP = 1;
    public static final int TYPE_3GPP2 = 2;
    public static final int TYPE_COMMON = 0;
    private final String mApn;
    private int mAuthType;
    private final int mBearerBitmap;
    private final boolean mEnabled;
    private final int mMaxConns;
    private final int mMaxConnsTime;
    private final boolean mModemCognitive;
    private final int mMtu;
    private final String mMvnoMatchData;
    private final String mMvnoType;
    private String mPassword;
    private final int mProfileId;
    private final String mProtocol;
    private final String mRoamingProtocol;
    private final int mSupportedApnTypesBitmap;
    private final int mType;
    private String mUserName;
    private final int mWaitTime;

    public DataProfile(int profileId, String apn, String protocol, int authType, String userName, String password, int type, int maxConnsTime, int maxConns, int waitTime, boolean enabled, int supportedApnTypesBitmap, String roamingProtocol, int bearerBitmap, int mtu, String mvnoType, String mvnoMatchData, boolean modemCognitive) {
        int authType2;
        this.mProfileId = profileId;
        this.mApn = apn;
        this.mProtocol = protocol;
        int i = authType;
        if (i != -1) {
            authType2 = i;
        } else if (TextUtils.isEmpty(userName)) {
            authType2 = 0;
        } else {
            authType2 = 3;
        }
        this.mAuthType = authType2;
        this.mUserName = userName;
        this.mPassword = password;
        this.mType = type;
        this.mMaxConnsTime = maxConnsTime;
        this.mMaxConns = maxConns;
        this.mWaitTime = waitTime;
        this.mEnabled = enabled;
        this.mSupportedApnTypesBitmap = supportedApnTypesBitmap;
        this.mRoamingProtocol = roamingProtocol;
        this.mBearerBitmap = bearerBitmap;
        this.mMtu = mtu;
        this.mMvnoType = mvnoType;
        this.mMvnoMatchData = mvnoMatchData;
        this.mModemCognitive = modemCognitive;
    }

    public DataProfile(Parcel source) {
        this.mProfileId = source.readInt();
        this.mApn = source.readString();
        this.mProtocol = source.readString();
        this.mAuthType = source.readInt();
        this.mUserName = source.readString();
        this.mPassword = source.readString();
        this.mType = source.readInt();
        this.mMaxConnsTime = source.readInt();
        this.mMaxConns = source.readInt();
        this.mWaitTime = source.readInt();
        this.mEnabled = source.readBoolean();
        this.mSupportedApnTypesBitmap = source.readInt();
        this.mRoamingProtocol = source.readString();
        this.mBearerBitmap = source.readInt();
        this.mMtu = source.readInt();
        this.mMvnoType = source.readString();
        this.mMvnoMatchData = source.readString();
        this.mModemCognitive = source.readBoolean();
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public String getApn() {
        return this.mApn;
    }

    public String getProtocol() {
        return this.mProtocol;
    }

    public int getAuthType() {
        return this.mAuthType;
    }

    public void setAuthType(int authType) {
        this.mAuthType = authType;
    }

    public String getUserName() {
        return this.mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public int getType() {
        return this.mType;
    }

    public int getMaxConnsTime() {
        return this.mMaxConnsTime;
    }

    public int getMaxConns() {
        return this.mMaxConns;
    }

    public int getWaitTime() {
        return this.mWaitTime;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public int getSupportedApnTypesBitmap() {
        return this.mSupportedApnTypesBitmap;
    }

    public String getRoamingProtocol() {
        return this.mRoamingProtocol;
    }

    public int getBearerBitmap() {
        return this.mBearerBitmap;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public String getMvnoType() {
        return this.mMvnoType;
    }

    public String getMvnoMatchData() {
        return this.mMvnoMatchData;
    }

    public boolean isModemCognitive() {
        return this.mModemCognitive;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "DataProfile=" + this.mProfileId + "/" + this.mApn + "/" + this.mProtocol + "/" + this.mAuthType + "/" + this.mUserName + "/" + this.mType + "/" + this.mMaxConnsTime + "/" + this.mMaxConns + "/" + this.mWaitTime + "/" + this.mEnabled + "/" + this.mSupportedApnTypesBitmap + "/" + this.mRoamingProtocol + "/" + this.mBearerBitmap + "/" + this.mMtu + "/" + this.mMvnoType + "/" + this.mMvnoMatchData + "/" + this.mModemCognitive;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DataProfile)) {
            return false;
        }
        if (o == this || toString().equals(o.toString())) {
            z = true;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProfileId);
        dest.writeString(this.mApn);
        dest.writeString(this.mProtocol);
        dest.writeInt(this.mAuthType);
        dest.writeString(this.mUserName);
        dest.writeString(this.mPassword);
        dest.writeInt(this.mType);
        dest.writeInt(this.mMaxConnsTime);
        dest.writeInt(this.mMaxConns);
        dest.writeInt(this.mWaitTime);
        dest.writeBoolean(this.mEnabled);
        dest.writeInt(this.mSupportedApnTypesBitmap);
        dest.writeString(this.mRoamingProtocol);
        dest.writeInt(this.mBearerBitmap);
        dest.writeInt(this.mMtu);
        dest.writeString(this.mMvnoType);
        dest.writeString(this.mMvnoMatchData);
        dest.writeBoolean(this.mModemCognitive);
    }
}
