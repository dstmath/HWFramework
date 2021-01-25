package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NrSignalInformation extends SignalInformation implements Sequenceable {
    private static final String SIGNALTYPENAME = NrSignalInformation.class.getSimpleName();
    private static final int TYPE_CSI_REFERENCE_SIGNAL = 1;
    private static final int TYPE_SS_REFERENCE_SIGNAL = 0;
    private int measuringType = 0;
    private int nrRsrp = Integer.MAX_VALUE;
    private int nrRsrq = Integer.MAX_VALUE;
    private int nrSinr = Integer.MAX_VALUE;
    private int signalLevel = Integer.MAX_VALUE;

    protected NrSignalInformation() {
        super(6);
    }

    @SystemApi
    public int getRsrp() {
        return this.nrRsrp;
    }

    @SystemApi
    public int getRsrq() {
        return this.nrRsrq;
    }

    @SystemApi
    public int getSinr() {
        return this.nrSinr;
    }

    @SystemApi
    public int getMeasuringType() {
        return this.measuringType;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int getSignalStrength() {
        return this.nrRsrp;
    }

    @Override // ohos.telephony.SignalInformation
    public int getSignalLevel() {
        return this.signalLevel;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.nrRsrp), Integer.valueOf(this.nrRsrq), Integer.valueOf(this.nrSinr), Integer.valueOf(this.measuringType), Integer.valueOf(this.signalLevel));
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NrSignalInformation)) {
            return false;
        }
        NrSignalInformation nrSignalInformation = (NrSignalInformation) obj;
        return super.equals(obj) && this.nrRsrp == nrSignalInformation.nrRsrp && this.nrRsrq == nrSignalInformation.nrRsrq && this.nrSinr == nrSignalInformation.nrSinr && this.measuringType == nrSignalInformation.measuringType && this.signalLevel == nrSignalInformation.signalLevel;
    }

    @SystemApi
    public String toString() {
        return SIGNALTYPENAME + "{ nrRsrp=" + this.nrRsrp + ", nrRsrq=" + this.nrRsrq + ", nrSinr=" + this.nrSinr + ", measuringType=" + this.measuringType + ", signalLevel=" + this.signalLevel + "}";
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    @SystemApi
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.nrRsrp);
        parcel.writeInt(this.nrRsrq);
        parcel.writeInt(this.nrSinr);
        parcel.writeInt(this.measuringType);
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
        this.nrRsrp = parcel.readInt();
        this.nrRsrq = parcel.readInt();
        this.nrSinr = parcel.readInt();
        this.measuringType = parcel.readInt();
        this.signalLevel = parcel.readInt();
        return true;
    }
}
