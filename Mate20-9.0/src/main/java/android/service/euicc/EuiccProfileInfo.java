package android.service.euicc;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.carrier.CarrierIdentifier;
import android.telephony.UiccAccessRule;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SystemApi
public final class EuiccProfileInfo implements Parcelable {
    public static final Parcelable.Creator<EuiccProfileInfo> CREATOR = new Parcelable.Creator<EuiccProfileInfo>() {
        public EuiccProfileInfo createFromParcel(Parcel in) {
            return new EuiccProfileInfo(in);
        }

        public EuiccProfileInfo[] newArray(int size) {
            return new EuiccProfileInfo[size];
        }
    };
    public static final int POLICY_RULE_DELETE_AFTER_DISABLING = 4;
    public static final int POLICY_RULE_DO_NOT_DELETE = 2;
    public static final int POLICY_RULE_DO_NOT_DISABLE = 1;
    public static final int PROFILE_CLASS_OPERATIONAL = 2;
    public static final int PROFILE_CLASS_PROVISIONING = 1;
    public static final int PROFILE_CLASS_TESTING = 0;
    public static final int PROFILE_CLASS_UNSET = -1;
    public static final int PROFILE_STATE_DISABLED = 0;
    public static final int PROFILE_STATE_ENABLED = 1;
    public static final int PROFILE_STATE_UNSET = -1;
    /* access modifiers changed from: private */
    public final UiccAccessRule[] mAccessRules;
    /* access modifiers changed from: private */
    public final CarrierIdentifier mCarrierIdentifier;
    /* access modifiers changed from: private */
    public final String mIccid;
    /* access modifiers changed from: private */
    public final String mNickname;
    /* access modifiers changed from: private */
    public final int mPolicyRules;
    /* access modifiers changed from: private */
    public final int mProfileClass;
    /* access modifiers changed from: private */
    public final String mProfileName;
    /* access modifiers changed from: private */
    public final String mServiceProviderName;
    /* access modifiers changed from: private */
    public final int mState;

    public static final class Builder {
        private List<UiccAccessRule> mAccessRules;
        private CarrierIdentifier mCarrierIdentifier;
        private String mIccid;
        private String mNickname;
        private int mPolicyRules;
        private int mProfileClass;
        private String mProfileName;
        private String mServiceProviderName;
        private int mState;

        public Builder(String value) {
            if (TextUtils.isDigitsOnly(value)) {
                this.mIccid = value;
                return;
            }
            throw new IllegalArgumentException("iccid contains invalid characters: " + value);
        }

        public Builder(EuiccProfileInfo baseProfile) {
            this.mIccid = baseProfile.mIccid;
            this.mNickname = baseProfile.mNickname;
            this.mServiceProviderName = baseProfile.mServiceProviderName;
            this.mProfileName = baseProfile.mProfileName;
            this.mProfileClass = baseProfile.mProfileClass;
            this.mState = baseProfile.mState;
            this.mCarrierIdentifier = baseProfile.mCarrierIdentifier;
            this.mPolicyRules = baseProfile.mPolicyRules;
            this.mAccessRules = Arrays.asList(baseProfile.mAccessRules);
        }

        public EuiccProfileInfo build() {
            if (this.mIccid != null) {
                EuiccProfileInfo euiccProfileInfo = new EuiccProfileInfo(this.mIccid, this.mNickname, this.mServiceProviderName, this.mProfileName, this.mProfileClass, this.mState, this.mCarrierIdentifier, this.mPolicyRules, this.mAccessRules);
                return euiccProfileInfo;
            }
            throw new IllegalStateException("ICCID must be set for a profile.");
        }

        public Builder setIccid(String value) {
            if (TextUtils.isDigitsOnly(value)) {
                this.mIccid = value;
                return this;
            }
            throw new IllegalArgumentException("iccid contains invalid characters: " + value);
        }

        public Builder setNickname(String value) {
            this.mNickname = value;
            return this;
        }

        public Builder setServiceProviderName(String value) {
            this.mServiceProviderName = value;
            return this;
        }

        public Builder setProfileName(String value) {
            this.mProfileName = value;
            return this;
        }

        public Builder setProfileClass(int value) {
            this.mProfileClass = value;
            return this;
        }

        public Builder setState(int value) {
            this.mState = value;
            return this;
        }

        public Builder setCarrierIdentifier(CarrierIdentifier value) {
            this.mCarrierIdentifier = value;
            return this;
        }

        public Builder setPolicyRules(int value) {
            this.mPolicyRules = value;
            return this;
        }

        public Builder setUiccAccessRule(List<UiccAccessRule> value) {
            this.mAccessRules = value;
            return this;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PolicyRule {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileClass {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileState {
    }

    @Deprecated
    public EuiccProfileInfo(String iccid, UiccAccessRule[] accessRules, String nickname) {
        if (TextUtils.isDigitsOnly(iccid)) {
            this.mIccid = iccid;
            this.mAccessRules = accessRules;
            this.mNickname = nickname;
            this.mServiceProviderName = null;
            this.mProfileName = null;
            this.mProfileClass = -1;
            this.mState = -1;
            this.mCarrierIdentifier = null;
            this.mPolicyRules = 0;
            return;
        }
        throw new IllegalArgumentException("iccid contains invalid characters: " + iccid);
    }

    private EuiccProfileInfo(Parcel in) {
        this.mIccid = in.readString();
        this.mNickname = in.readString();
        this.mServiceProviderName = in.readString();
        this.mProfileName = in.readString();
        this.mProfileClass = in.readInt();
        this.mState = in.readInt();
        if (in.readByte() == 1) {
            this.mCarrierIdentifier = CarrierIdentifier.CREATOR.createFromParcel(in);
        } else {
            this.mCarrierIdentifier = null;
        }
        this.mPolicyRules = in.readInt();
        this.mAccessRules = (UiccAccessRule[]) in.createTypedArray(UiccAccessRule.CREATOR);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mIccid);
        dest.writeString(this.mNickname);
        dest.writeString(this.mServiceProviderName);
        dest.writeString(this.mProfileName);
        dest.writeInt(this.mProfileClass);
        dest.writeInt(this.mState);
        if (this.mCarrierIdentifier != null) {
            dest.writeByte((byte) 1);
            this.mCarrierIdentifier.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeInt(this.mPolicyRules);
        dest.writeTypedArray(this.mAccessRules, flags);
    }

    public int describeContents() {
        return 0;
    }

    private EuiccProfileInfo(String iccid, String nickname, String serviceProviderName, String profileName, int profileClass, int state, CarrierIdentifier carrierIdentifier, int policyRules, List<UiccAccessRule> accessRules) {
        this.mIccid = iccid;
        this.mNickname = nickname;
        this.mServiceProviderName = serviceProviderName;
        this.mProfileName = profileName;
        this.mProfileClass = profileClass;
        this.mState = state;
        this.mCarrierIdentifier = carrierIdentifier;
        this.mPolicyRules = policyRules;
        if (accessRules == null || accessRules.size() <= 0) {
            this.mAccessRules = null;
        } else {
            this.mAccessRules = (UiccAccessRule[]) accessRules.toArray(new UiccAccessRule[accessRules.size()]);
        }
    }

    public String getIccid() {
        return this.mIccid;
    }

    public List<UiccAccessRule> getUiccAccessRules() {
        if (this.mAccessRules == null) {
            return null;
        }
        return Arrays.asList(this.mAccessRules);
    }

    public String getNickname() {
        return this.mNickname;
    }

    public String getServiceProviderName() {
        return this.mServiceProviderName;
    }

    public String getProfileName() {
        return this.mProfileName;
    }

    public int getProfileClass() {
        return this.mProfileClass;
    }

    public int getState() {
        return this.mState;
    }

    public CarrierIdentifier getCarrierIdentifier() {
        return this.mCarrierIdentifier;
    }

    public int getPolicyRules() {
        return this.mPolicyRules;
    }

    public boolean hasPolicyRules() {
        return this.mPolicyRules != 0;
    }

    public boolean hasPolicyRule(int policy) {
        return (this.mPolicyRules & policy) != 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EuiccProfileInfo that = (EuiccProfileInfo) obj;
        if (!Objects.equals(this.mIccid, that.mIccid) || !Objects.equals(this.mNickname, that.mNickname) || !Objects.equals(this.mServiceProviderName, that.mServiceProviderName) || !Objects.equals(this.mProfileName, that.mProfileName) || this.mProfileClass != that.mProfileClass || this.mState != that.mState || !Objects.equals(this.mCarrierIdentifier, that.mCarrierIdentifier) || this.mPolicyRules != that.mPolicyRules || !Arrays.equals(this.mAccessRules, that.mAccessRules)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 1) + Objects.hashCode(this.mIccid))) + Objects.hashCode(this.mNickname))) + Objects.hashCode(this.mServiceProviderName))) + Objects.hashCode(this.mProfileName))) + this.mProfileClass)) + this.mState)) + Objects.hashCode(this.mCarrierIdentifier))) + this.mPolicyRules)) + Arrays.hashCode(this.mAccessRules);
    }

    public String toString() {
        return "EuiccProfileInfo (nickname=" + this.mNickname + ", serviceProviderName=" + this.mServiceProviderName + ", profileName=" + this.mProfileName + ", profileClass=" + this.mProfileClass + ", state=" + this.mState + ", CarrierIdentifier=" + this.mCarrierIdentifier + ", policyRules=" + this.mPolicyRules + ", accessRules=" + Arrays.toString(this.mAccessRules) + ")";
    }
}
