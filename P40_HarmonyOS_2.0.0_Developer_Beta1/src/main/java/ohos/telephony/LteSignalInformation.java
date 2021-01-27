package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class LteSignalInformation extends SignalInformation implements Sequenceable {
    private static final String SIGNALTYPENAME = LteSignalInformation.class.getSimpleName();
    private int lteRsrp = Integer.MAX_VALUE;
    private int lteRsrq = Integer.MAX_VALUE;
    private int lteRssnr = Integer.MAX_VALUE;
    private int signalLevel = Integer.MAX_VALUE;

    protected LteSignalInformation() {
        super(5);
    }

    @SystemApi
    public int getRsrp() {
        return this.lteRsrp;
    }

    @SystemApi
    public int getRsrq() {
        return this.lteRsrq;
    }

    @SystemApi
    public int getRssnr() {
        return this.lteRssnr;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int getSignalStrength() {
        return this.lteRsrp;
    }

    @Override // ohos.telephony.SignalInformation
    public int getSignalLevel() {
        return this.signalLevel;
    }

    @Override // ohos.telephony.SignalInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.lteRsrp), Integer.valueOf(this.lteRsrq), Integer.valueOf(this.lteRssnr), Integer.valueOf(this.signalLevel));
    }

    @Override // ohos.telephony.SignalInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LteSignalInformation)) {
            return false;
        }
        LteSignalInformation lteSignalInformation = (LteSignalInformation) obj;
        return super.equals(obj) && this.lteRsrp == lteSignalInformation.lteRsrp && this.lteRsrq == lteSignalInformation.lteRsrq && this.lteRssnr == lteSignalInformation.lteRssnr && this.signalLevel == lteSignalInformation.signalLevel;
    }

    public String toString() {
        return SIGNALTYPENAME + "{ lteRsrp=" + this.lteRsrp + ", lteRsrq=" + this.lteRsrq + ", lteRssnr=" + this.lteRssnr + ", signalLevel=" + this.signalLevel + "}";
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.lteRsrp);
        parcel.writeInt(this.lteRsrq);
        parcel.writeInt(this.lteRssnr);
        parcel.writeInt(this.signalLevel);
        return true;
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.lteRsrp = parcel.readInt();
        this.lteRsrq = parcel.readInt();
        this.lteRssnr = parcel.readInt();
        this.signalLevel = parcel.readInt();
        return true;
    }
}
