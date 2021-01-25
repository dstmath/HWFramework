package android.telephony.data;

import android.annotation.SystemApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public final class DataProfile implements Parcelable {
    public static final Parcelable.Creator<DataProfile> CREATOR = new Parcelable.Creator<DataProfile>() {
        /* class android.telephony.data.DataProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataProfile createFromParcel(Parcel source) {
            return new DataProfile(source);
        }

        @Override // android.os.Parcelable.Creator
        public DataProfile[] newArray(int size) {
            return new DataProfile[size];
        }
    };
    public static final int TYPE_3GPP = 1;
    public static final int TYPE_3GPP2 = 2;
    public static final int TYPE_COMMON = 0;
    private final String mApn;
    private int mAuthType;
    private final int mBearerBitmask;
    private final boolean mEnabled;
    private final int mMaxConnections;
    private final int mMaxConnectionsTime;
    private final int mMtu;
    private String mPassword;
    private final boolean mPersistent;
    private final boolean mPreferred;
    private final int mProfileId;
    private final int mProtocolType;
    private final int mRoamingProtocolType;
    private String mSnssai;
    private int mSscMode;
    private final int mSupportedApnTypesBitmask;
    private final int mType;
    private String mUserName;
    private final int mWaitTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    private DataProfile(int profileId, String apn, int protocolType, int authType, String userName, String password, int type, int maxConnectionsTime, int maxConnections, int waitTime, boolean enabled, int supportedApnTypesBitmask, int roamingProtocolType, int bearerBitmask, int mtu, boolean persistent, boolean preferred) {
        int authType2;
        this.mProfileId = profileId;
        this.mApn = apn;
        this.mProtocolType = protocolType;
        if (authType != -1) {
            authType2 = authType;
        } else if (TextUtils.isEmpty(userName)) {
            authType2 = 0;
        } else {
            authType2 = 3;
        }
        this.mAuthType = authType2;
        this.mUserName = userName;
        this.mPassword = password;
        this.mType = type;
        this.mMaxConnectionsTime = maxConnectionsTime;
        this.mMaxConnections = maxConnections;
        this.mWaitTime = waitTime;
        this.mEnabled = enabled;
        this.mSupportedApnTypesBitmask = supportedApnTypesBitmask;
        this.mRoamingProtocolType = roamingProtocolType;
        this.mBearerBitmask = bearerBitmask;
        this.mMtu = mtu;
        this.mPersistent = persistent;
        this.mPreferred = preferred;
    }

    private DataProfile(Parcel source) {
        this.mProfileId = source.readInt();
        this.mApn = source.readString();
        this.mProtocolType = source.readInt();
        this.mAuthType = source.readInt();
        this.mUserName = source.readString();
        this.mPassword = source.readString();
        this.mType = source.readInt();
        this.mMaxConnectionsTime = source.readInt();
        this.mMaxConnections = source.readInt();
        this.mWaitTime = source.readInt();
        this.mEnabled = source.readBoolean();
        this.mSupportedApnTypesBitmask = source.readInt();
        this.mRoamingProtocolType = source.readInt();
        this.mBearerBitmask = source.readInt();
        this.mMtu = source.readInt();
        this.mPersistent = source.readBoolean();
        this.mPreferred = source.readBoolean();
        this.mSnssai = "";
        this.mSscMode = 0;
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public String getApn() {
        return this.mApn;
    }

    public int getProtocolType() {
        return this.mProtocolType;
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

    public int getMaxConnectionsTime() {
        return this.mMaxConnectionsTime;
    }

    public int getMaxConnections() {
        return this.mMaxConnections;
    }

    public int getWaitTime() {
        return this.mWaitTime;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public int getSupportedApnTypesBitmask() {
        return this.mSupportedApnTypesBitmask;
    }

    public int getRoamingProtocolType() {
        return this.mRoamingProtocolType;
    }

    public int getBearerBitmask() {
        return this.mBearerBitmask;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    public boolean isPreferred() {
        return this.mPreferred;
    }

    public String getSnssai() {
        return this.mSnssai;
    }

    public void setSnssai(String snssai) {
        this.mSnssai = snssai;
    }

    public int getSscMode() {
        return this.mSscMode;
    }

    public void setSscMode(int sscMode) {
        this.mSscMode = sscMode;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("DataProfile=");
        sb.append(this.mProfileId);
        sb.append("/");
        sb.append(this.mProtocolType);
        sb.append("/");
        sb.append(this.mAuthType);
        sb.append("/");
        if (Build.IS_USER) {
            str = "***/***/***";
        } else {
            str = this.mApn + "/" + this.mUserName + "/";
        }
        sb.append(str);
        sb.append("/");
        sb.append(this.mType);
        sb.append("/");
        sb.append(this.mMaxConnectionsTime);
        sb.append("/");
        sb.append(this.mMaxConnections);
        sb.append("/");
        sb.append(this.mWaitTime);
        sb.append("/");
        sb.append(this.mEnabled);
        sb.append("/");
        sb.append(this.mSupportedApnTypesBitmask);
        sb.append("/");
        sb.append(this.mRoamingProtocolType);
        sb.append("/");
        sb.append(this.mBearerBitmask);
        sb.append("/");
        sb.append(this.mMtu);
        sb.append("/");
        sb.append(this.mPersistent);
        sb.append("/");
        sb.append(this.mPreferred);
        sb.append("/");
        sb.append(this.mSnssai);
        sb.append("/");
        sb.append(this.mSscMode);
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProfileId);
        dest.writeString(this.mApn);
        dest.writeInt(this.mProtocolType);
        dest.writeInt(this.mAuthType);
        dest.writeString(this.mUserName);
        dest.writeString(this.mPassword);
        dest.writeInt(this.mType);
        dest.writeInt(this.mMaxConnectionsTime);
        dest.writeInt(this.mMaxConnections);
        dest.writeInt(this.mWaitTime);
        dest.writeBoolean(this.mEnabled);
        dest.writeInt(this.mSupportedApnTypesBitmask);
        dest.writeInt(this.mRoamingProtocolType);
        dest.writeInt(this.mBearerBitmask);
        dest.writeInt(this.mMtu);
        dest.writeBoolean(this.mPersistent);
        dest.writeBoolean(this.mPreferred);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataProfile that = (DataProfile) o;
        if (this.mProfileId == that.mProfileId && this.mProtocolType == that.mProtocolType && this.mAuthType == that.mAuthType && this.mType == that.mType && this.mMaxConnectionsTime == that.mMaxConnectionsTime && this.mMaxConnections == that.mMaxConnections && this.mWaitTime == that.mWaitTime && this.mEnabled == that.mEnabled && this.mSupportedApnTypesBitmask == that.mSupportedApnTypesBitmask && this.mRoamingProtocolType == that.mRoamingProtocolType && this.mBearerBitmask == that.mBearerBitmask && this.mMtu == that.mMtu && this.mPersistent == that.mPersistent && this.mPreferred == that.mPreferred && Objects.equals(this.mApn, that.mApn) && Objects.equals(this.mUserName, that.mUserName) && Objects.equals(this.mPassword, that.mPassword) && Objects.equals(this.mSnssai, that.mSnssai) && this.mSscMode == that.mSscMode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mProfileId), this.mApn, Integer.valueOf(this.mProtocolType), Integer.valueOf(this.mAuthType), this.mUserName, this.mPassword, Integer.valueOf(this.mType), Integer.valueOf(this.mMaxConnectionsTime), Integer.valueOf(this.mMaxConnections), Integer.valueOf(this.mWaitTime), Boolean.valueOf(this.mEnabled), Integer.valueOf(this.mSupportedApnTypesBitmask), Integer.valueOf(this.mRoamingProtocolType), Integer.valueOf(this.mBearerBitmask), Integer.valueOf(this.mMtu), Boolean.valueOf(this.mPersistent), Boolean.valueOf(this.mPreferred), this.mSnssai, Integer.valueOf(this.mSscMode));
    }

    public static final class Builder {
        private String mApn;
        private int mAuthType;
        private int mBearerBitmask;
        private boolean mEnabled;
        private int mMaxConnections;
        private int mMaxConnectionsTime;
        private int mMtu;
        private String mPassword;
        private boolean mPersistent;
        private boolean mPreferred;
        private int mProfileId;
        private int mProtocolType;
        private int mRoamingProtocolType;
        private int mSupportedApnTypesBitmask;
        private int mType;
        private String mUserName;
        private int mWaitTime;

        public Builder setProfileId(int profileId) {
            this.mProfileId = profileId;
            return this;
        }

        public Builder setApn(String apn) {
            this.mApn = apn;
            return this;
        }

        public Builder setProtocolType(int protocolType) {
            this.mProtocolType = protocolType;
            return this;
        }

        public Builder setAuthType(int authType) {
            this.mAuthType = authType;
            return this;
        }

        public Builder setUserName(String userName) {
            this.mUserName = userName;
            return this;
        }

        public Builder setPassword(String password) {
            this.mPassword = password;
            return this;
        }

        public Builder setType(int type) {
            this.mType = type;
            return this;
        }

        public Builder setMaxConnectionsTime(int maxConnectionsTime) {
            this.mMaxConnectionsTime = maxConnectionsTime;
            return this;
        }

        public Builder setMaxConnections(int maxConnections) {
            this.mMaxConnections = maxConnections;
            return this;
        }

        public Builder setWaitTime(int waitTime) {
            this.mWaitTime = waitTime;
            return this;
        }

        public Builder enable(boolean isEnabled) {
            this.mEnabled = isEnabled;
            return this;
        }

        public Builder setSupportedApnTypesBitmask(int supportedApnTypesBitmask) {
            this.mSupportedApnTypesBitmask = supportedApnTypesBitmask;
            return this;
        }

        public Builder setRoamingProtocolType(int protocolType) {
            this.mRoamingProtocolType = protocolType;
            return this;
        }

        public Builder setBearerBitmask(int bearerBitmask) {
            this.mBearerBitmask = bearerBitmask;
            return this;
        }

        public Builder setMtu(int mtu) {
            this.mMtu = mtu;
            return this;
        }

        public Builder setPreferred(boolean isPreferred) {
            this.mPreferred = isPreferred;
            return this;
        }

        public Builder setPersistent(boolean isPersistent) {
            this.mPersistent = isPersistent;
            return this;
        }

        public DataProfile build() {
            return new DataProfile(this.mProfileId, this.mApn, this.mProtocolType, this.mAuthType, this.mUserName, this.mPassword, this.mType, this.mMaxConnectionsTime, this.mMaxConnections, this.mWaitTime, this.mEnabled, this.mSupportedApnTypesBitmask, this.mRoamingProtocolType, this.mBearerBitmask, this.mMtu, this.mPersistent, this.mPreferred);
        }
    }
}
