package android.telephony.euicc;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.UiccAccessRule;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DownloadableSubscription implements Parcelable {
    public static final Parcelable.Creator<DownloadableSubscription> CREATOR = new Parcelable.Creator<DownloadableSubscription>() {
        /* class android.telephony.euicc.DownloadableSubscription.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DownloadableSubscription createFromParcel(Parcel in) {
            return new DownloadableSubscription(in);
        }

        @Override // android.os.Parcelable.Creator
        public DownloadableSubscription[] newArray(int size) {
            return new DownloadableSubscription[size];
        }
    };
    private List<UiccAccessRule> accessRules;
    private String carrierName;
    private String confirmationCode;
    @UnsupportedAppUsage
    @Deprecated
    public final String encodedActivationCode;

    public String getEncodedActivationCode() {
        return this.encodedActivationCode;
    }

    private DownloadableSubscription(String encodedActivationCode2) {
        this.encodedActivationCode = encodedActivationCode2;
    }

    private DownloadableSubscription(Parcel in) {
        this.encodedActivationCode = in.readString();
        this.confirmationCode = in.readString();
        this.carrierName = in.readString();
        this.accessRules = new ArrayList();
        in.readTypedList(this.accessRules, UiccAccessRule.CREATOR);
    }

    private DownloadableSubscription(String encodedActivationCode2, String confirmationCode2, String carrierName2, List<UiccAccessRule> accessRules2) {
        this.encodedActivationCode = encodedActivationCode2;
        this.confirmationCode = confirmationCode2;
        this.carrierName = carrierName2;
        this.accessRules = accessRules2;
    }

    @SystemApi
    public static final class Builder {
        List<UiccAccessRule> accessRules;
        private String carrierName;
        private String confirmationCode;
        private String encodedActivationCode;

        public Builder() {
        }

        public Builder(DownloadableSubscription baseSubscription) {
            this.encodedActivationCode = baseSubscription.getEncodedActivationCode();
            this.confirmationCode = baseSubscription.getConfirmationCode();
            this.carrierName = baseSubscription.getCarrierName();
            this.accessRules = baseSubscription.getAccessRules();
        }

        public DownloadableSubscription build() {
            return new DownloadableSubscription(this.encodedActivationCode, this.confirmationCode, this.carrierName, this.accessRules);
        }

        public Builder setEncodedActivationCode(String value) {
            this.encodedActivationCode = value;
            return this;
        }

        public Builder setConfirmationCode(String value) {
            this.confirmationCode = value;
            return this;
        }

        public Builder setCarrierName(String value) {
            this.carrierName = value;
            return this;
        }

        public Builder setAccessRules(List<UiccAccessRule> value) {
            this.accessRules = value;
            return this;
        }
    }

    public static DownloadableSubscription forActivationCode(String encodedActivationCode2) {
        Preconditions.checkNotNull(encodedActivationCode2, "Activation code may not be null");
        return new DownloadableSubscription(encodedActivationCode2);
    }

    @Deprecated
    public void setConfirmationCode(String confirmationCode2) {
        this.confirmationCode = confirmationCode2;
    }

    public String getConfirmationCode() {
        return this.confirmationCode;
    }

    @UnsupportedAppUsage
    @Deprecated
    public void setCarrierName(String carrierName2) {
        this.carrierName = carrierName2;
    }

    @SystemApi
    public String getCarrierName() {
        return this.carrierName;
    }

    @SystemApi
    public List<UiccAccessRule> getAccessRules() {
        return this.accessRules;
    }

    @Deprecated
    public void setAccessRules(List<UiccAccessRule> accessRules2) {
        this.accessRules = accessRules2;
    }

    @UnsupportedAppUsage
    @Deprecated
    public void setAccessRules(UiccAccessRule[] accessRules2) {
        this.accessRules = Arrays.asList(accessRules2);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.encodedActivationCode);
        dest.writeString(this.confirmationCode);
        dest.writeString(this.carrierName);
        dest.writeTypedList(this.accessRules);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
