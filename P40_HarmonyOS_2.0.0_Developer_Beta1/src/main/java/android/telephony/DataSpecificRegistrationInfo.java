package android.telephony;

import android.annotation.SystemApi;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

@SystemApi
public final class DataSpecificRegistrationInfo implements Parcelable {
    public static final Parcelable.Creator<DataSpecificRegistrationInfo> CREATOR = new Parcelable.Creator<DataSpecificRegistrationInfo>() {
        /* class android.telephony.DataSpecificRegistrationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataSpecificRegistrationInfo createFromParcel(Parcel source) {
            return new DataSpecificRegistrationInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DataSpecificRegistrationInfo[] newArray(int size) {
            return new DataSpecificRegistrationInfo[size];
        }
    };
    public final boolean isDcNrRestricted;
    public final boolean isEnDcAvailable;
    public final boolean isNrAvailable;
    public boolean mIsUsingCarrierAggregation;
    private final LteVopsSupportInfo mLteVopsSupportInfo;
    public final int maxDataCalls;

    DataSpecificRegistrationInfo(int maxDataCalls2, boolean isDcNrRestricted2, boolean isNrAvailable2, boolean isEnDcAvailable2, LteVopsSupportInfo lteVops, boolean isUsingCarrierAggregation) {
        this.maxDataCalls = maxDataCalls2;
        this.isDcNrRestricted = isDcNrRestricted2;
        this.isNrAvailable = isNrAvailable2;
        this.isEnDcAvailable = isEnDcAvailable2;
        this.mLteVopsSupportInfo = lteVops;
        this.mIsUsingCarrierAggregation = isUsingCarrierAggregation;
    }

    DataSpecificRegistrationInfo(DataSpecificRegistrationInfo dsri) {
        this.maxDataCalls = dsri.maxDataCalls;
        this.isDcNrRestricted = dsri.isDcNrRestricted;
        this.isNrAvailable = dsri.isNrAvailable;
        this.isEnDcAvailable = dsri.isEnDcAvailable;
        this.mLteVopsSupportInfo = dsri.mLteVopsSupportInfo;
        this.mIsUsingCarrierAggregation = dsri.mIsUsingCarrierAggregation;
    }

    private DataSpecificRegistrationInfo(Parcel source) {
        this.maxDataCalls = source.readInt();
        this.isDcNrRestricted = source.readBoolean();
        this.isNrAvailable = source.readBoolean();
        this.isEnDcAvailable = source.readBoolean();
        this.mLteVopsSupportInfo = LteVopsSupportInfo.CREATOR.createFromParcel(source);
        this.mIsUsingCarrierAggregation = source.readBoolean();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.maxDataCalls);
        dest.writeBoolean(this.isDcNrRestricted);
        dest.writeBoolean(this.isNrAvailable);
        dest.writeBoolean(this.isEnDcAvailable);
        this.mLteVopsSupportInfo.writeToParcel(dest, flags);
        dest.writeBoolean(this.mIsUsingCarrierAggregation);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(" :{");
        sb.append(" maxDataCalls = " + this.maxDataCalls);
        sb.append(" isDcNrRestricted = " + this.isDcNrRestricted);
        sb.append(" isNrAvailable = " + this.isNrAvailable);
        sb.append(" isEnDcAvailable = " + this.isEnDcAvailable);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mLteVopsSupportInfo.toString());
        sb.append(" mIsUsingCarrierAggregation = " + this.mIsUsingCarrierAggregation);
        sb.append(" }");
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.maxDataCalls), Boolean.valueOf(this.isDcNrRestricted), Boolean.valueOf(this.isNrAvailable), Boolean.valueOf(this.isEnDcAvailable), this.mLteVopsSupportInfo, Boolean.valueOf(this.mIsUsingCarrierAggregation));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSpecificRegistrationInfo)) {
            return false;
        }
        DataSpecificRegistrationInfo other = (DataSpecificRegistrationInfo) o;
        if (this.maxDataCalls == other.maxDataCalls && this.isDcNrRestricted == other.isDcNrRestricted && this.isNrAvailable == other.isNrAvailable && this.isEnDcAvailable == other.isEnDcAvailable && this.mLteVopsSupportInfo.equals(other.mLteVopsSupportInfo) && this.mIsUsingCarrierAggregation == other.mIsUsingCarrierAggregation) {
            return true;
        }
        return false;
    }

    public LteVopsSupportInfo getLteVopsSupportInfo() {
        return this.mLteVopsSupportInfo;
    }

    public void setIsUsingCarrierAggregation(boolean isUsingCarrierAggregation) {
        this.mIsUsingCarrierAggregation = isUsingCarrierAggregation;
    }

    public boolean isUsingCarrierAggregation() {
        return this.mIsUsingCarrierAggregation;
    }
}
