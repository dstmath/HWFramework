package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class CdmaSignalInformation extends SignalInformation implements Sequenceable {
    private static final String SIGNALTYPENAME = CdmaSignalInformation.class.getSimpleName();
    private int cdmaEcio = Integer.MAX_VALUE;
    private int cdmaRssi = Integer.MAX_VALUE;
    private int evdoRssi = Integer.MAX_VALUE;
    private int evdoSnr = Integer.MAX_VALUE;
    private int signalLevel = Integer.MAX_VALUE;

    protected CdmaSignalInformation() {
        super(2);
    }

    @SystemApi
    public int getCdmaRssi() {
        return this.cdmaRssi;
    }

    @SystemApi
    public int getCdmaEcio() {
        return this.cdmaEcio;
    }

    @SystemApi
    public int getEvdoRssi() {
        return this.evdoRssi;
    }

    @SystemApi
    public int getEvdoSnr() {
        return this.evdoSnr;
    }

    @Override // ohos.telephony.SignalInformation
    @SystemApi
    public int getSignalStrength() {
        int cdmaRssi2 = getCdmaRssi();
        int evdoRssi2 = getEvdoRssi();
        return cdmaRssi2 > evdoRssi2 ? cdmaRssi2 : evdoRssi2;
    }

    @Override // ohos.telephony.SignalInformation
    public int getSignalLevel() {
        return this.signalLevel;
    }

    @Override // ohos.telephony.SignalInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.cdmaRssi), Integer.valueOf(this.cdmaEcio), Integer.valueOf(this.evdoRssi), Integer.valueOf(this.evdoSnr), Integer.valueOf(this.signalLevel));
    }

    @Override // ohos.telephony.SignalInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CdmaSignalInformation)) {
            return false;
        }
        CdmaSignalInformation cdmaSignalInformation = (CdmaSignalInformation) obj;
        return super.equals(obj) && this.cdmaRssi == cdmaSignalInformation.cdmaRssi && this.cdmaEcio == cdmaSignalInformation.cdmaEcio && this.evdoRssi == cdmaSignalInformation.evdoRssi && this.evdoSnr == cdmaSignalInformation.evdoSnr && this.signalLevel == cdmaSignalInformation.signalLevel;
    }

    public String toString() {
        return SIGNALTYPENAME + "{ cdmaRssi=" + this.cdmaRssi + ", cdmaEcio=" + this.cdmaEcio + ", evdoRssi=" + this.evdoRssi + ", evdoSnr=" + this.evdoSnr + ", signalLevel=" + this.signalLevel + "}";
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.cdmaRssi);
        parcel.writeInt(this.cdmaEcio);
        parcel.writeInt(this.evdoRssi);
        parcel.writeInt(this.evdoSnr);
        parcel.writeInt(this.signalLevel);
        return true;
    }

    @Override // ohos.telephony.SignalInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.cdmaRssi = parcel.readInt();
        this.cdmaEcio = parcel.readInt();
        this.evdoRssi = parcel.readInt();
        this.evdoSnr = parcel.readInt();
        this.signalLevel = parcel.readInt();
        return true;
    }
}
