package android.service.euicc;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.carrier.CarrierIdentifier;
import android.telephony.UiccAccessRule;
import android.text.TextUtils;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SystemApi
public final class EuiccProfileInfo implements Parcelable {
    public static final Parcelable.Creator<EuiccProfileInfo> CREATOR = new Parcelable.Creator<EuiccProfileInfo>() {
        /* class android.service.euicc.EuiccProfileInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EuiccProfileInfo createFromParcel(Parcel in) {
            return new EuiccProfileInfo(in);
        }

        @Override // android.os.Parcelable.Creator
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
    private final UiccAccessRule[] mAccessRules;
    private final CarrierIdentifier mCarrierIdentifier;
    private final String mIccid;
    private final String mNickname;
    private final int mPolicyRules;
    private final int mProfileClass;
    private final String mProfileName;
    private final String mServiceProviderName;
    private final int mState;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PolicyRule {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileClass {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileState {
    }

    @UnsupportedAppUsage
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

    @Override // android.os.Parcelable
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

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
            String str = this.mIccid;
            if (str != null) {
                return new EuiccProfileInfo(str, this.mNickname, this.mServiceProviderName, this.mProfileName, this.mProfileClass, this.mState, this.mCarrierIdentifier, this.mPolicyRules, this.mAccessRules);
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
        UiccAccessRule[] uiccAccessRuleArr = this.mAccessRules;
        if (uiccAccessRuleArr == null) {
            return null;
        }
        return Arrays.asList(uiccAccessRuleArr);
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
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EuiccProfileInfo that = (EuiccProfileInfo) obj;
        if (!Objects.equals(this.mIccid, that.mIccid) || !Objects.equals(this.mNickname, that.mNickname) || !Objects.equals(this.mServiceProviderName, that.mServiceProviderName) || !Objects.equals(this.mProfileName, that.mProfileName) || this.mProfileClass != that.mProfileClass || this.mState != that.mState || !Objects.equals(this.mCarrierIdentifier, that.mCarrierIdentifier) || this.mPolicyRules != that.mPolicyRules || !Arrays.equals(this.mAccessRules, that.mAccessRules)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((((((((((((((1 * 31) + Objects.hashCode(this.mIccid)) * 31) + Objects.hashCode(this.mNickname)) * 31) + Objects.hashCode(this.mServiceProviderName)) * 31) + Objects.hashCode(this.mProfileName)) * 31) + this.mProfileClass) * 31) + this.mState) * 31) + Objects.hashCode(this.mCarrierIdentifier)) * 31) + this.mPolicyRules) * 31) + Arrays.hashCode(this.mAccessRules);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EuiccProfileInfo (nickname=");
        sb.append(this.mNickname);
        sb.append(", serviceProviderName=");
        sb.append(Log.HWINFO ? this.mServiceProviderName : "***");
        sb.append(", profileName=");
        sb.append(this.mProfileName);
        sb.append(", profileClass=");
        sb.append(this.mProfileClass);
        sb.append(", state=");
        sb.append(this.mState);
        sb.append(", CarrierIdentifier=");
        sb.append(this.mCarrierIdentifier);
        sb.append(", policyRules=");
        sb.append(this.mPolicyRules);
        sb.append(", accessRules=");
        sb.append(Arrays.toString(this.mAccessRules));
        sb.append(")");
        return sb.toString();
    }
}
