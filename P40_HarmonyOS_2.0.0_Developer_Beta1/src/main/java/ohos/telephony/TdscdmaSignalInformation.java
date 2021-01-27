package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class TdscdmaSignalInformation extends SignalInformation implements Sequenceable {
    private static final String SIGNALTYPENAME = TdscdmaSignalInformation.class.getSimpleName();
    private int signalLevel = Integer.MAX_VALUE;
    private int tdscdmaRscp = Integer.MAX_VALUE;

    protected TdscdmaSignalInformation() {
        super(4);
    }

    @SystemApi
    public int getRscp() {
        return this.tdscdmaRscp;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int getSignalStrength() {
        return this.tdscdmaRscp;
    }

    @Override // ohos.telephony.SignalInformation
    public int getSignalLevel() {
        return this.signalLevel;
    }

    @Override // ohos.telephony.SignalInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.tdscdmaRscp), Integer.valueOf(this.signalLevel));
    }

    @Override // ohos.telephony.SignalInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TdscdmaSignalInformation)) {
            return false;
        }
        TdscdmaSignalInformation tdscdmaSignalInformation = (TdscdmaSignalInformation) obj;
        return super.equals(obj) && this.tdscdmaRscp == tdscdmaSignalInformation.tdscdmaRscp && this.signalLevel == tdscdmaSignalInformation.signalLevel;
    }

    public String toString() {
        return SIGNALTYPENAME + "{ tdscdmaRscp=" + this.tdscdmaRscp + ", signalLevel=" + this.signalLevel + "}";
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.tdscdmaRscp);
        parcel.writeInt(this.signalLevel);
        return true;
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.tdscdmaRscp = parcel.readInt();
        this.signalLevel = parcel.readInt();
        return true;
    }
}
