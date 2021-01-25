package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class WcdmaSignalInformation extends SignalInformation implements Sequenceable {
    private static final String SIGNALTYPENAME = WcdmaSignalInformation.class.getSimpleName();
    private int signalLevel = Integer.MAX_VALUE;
    private int wcdmaEcNo = Integer.MAX_VALUE;
    private int wcdmaRscp = Integer.MAX_VALUE;

    protected WcdmaSignalInformation() {
        super(3);
    }

    @SystemApi
    public int getRscp() {
        return this.wcdmaRscp;
    }

    @SystemApi
    public int getEcNo() {
        return this.wcdmaEcNo;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int getSignalStrength() {
        return this.wcdmaRscp;
    }

    @Override // ohos.telephony.SignalInformation
    public int getSignalLevel() {
        return this.signalLevel;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.wcdmaRscp), Integer.valueOf(this.wcdmaEcNo), Integer.valueOf(this.signalLevel));
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WcdmaSignalInformation)) {
            return false;
        }
        WcdmaSignalInformation wcdmaSignalInformation = (WcdmaSignalInformation) obj;
        return super.equals(obj) && this.wcdmaRscp == wcdmaSignalInformation.wcdmaRscp && this.wcdmaEcNo == wcdmaSignalInformation.wcdmaEcNo && this.signalLevel == wcdmaSignalInformation.signalLevel;
    }

    @SystemApi
    public String toString() {
        return SIGNALTYPENAME + "{ wcdmaRscp=" + this.wcdmaRscp + ", wcdmaEcNo=" + this.wcdmaEcNo + ", signalLevel=" + this.signalLevel + "}";
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    @SystemApi
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.wcdmaRscp);
        parcel.writeInt(this.wcdmaEcNo);
        parcel.writeInt(this.signalLevel);
        return true;
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    @SystemApi
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.wcdmaRscp = parcel.readInt();
        this.wcdmaEcNo = parcel.readInt();
        this.signalLevel = parcel.readInt();
        return true;
    }
}
