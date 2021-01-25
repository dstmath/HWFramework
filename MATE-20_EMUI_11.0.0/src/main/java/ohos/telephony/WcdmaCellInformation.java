package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class WcdmaCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = WcdmaCellInformation.class.getSimpleName();
    private int cellId = Integer.MAX_VALUE;
    private int lac = Integer.MAX_VALUE;
    private String mcc = "";
    private String mnc = "";
    private int psc = Integer.MAX_VALUE;
    private int uarfcn = Integer.MAX_VALUE;
    private WcdmaSignalInformation wcdmaSignalInformation = new WcdmaSignalInformation();

    protected WcdmaCellInformation() {
        super(3, false, 0);
    }

    public int getLac() {
        return this.lac;
    }

    public int getCellId() {
        return this.cellId;
    }

    public int getPsc() {
        return this.psc;
    }

    public int getUarfcn() {
        return this.uarfcn;
    }

    public String getMcc() {
        return this.mcc;
    }

    public String getMnc() {
        return this.mnc;
    }

    public String getPlmn() {
        return this.mcc + this.mnc;
    }

    @Override // ohos.telephony.CellInformation
    public WcdmaSignalInformation getSignalInformation() {
        return this.wcdmaSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.lac), Integer.valueOf(this.cellId), Integer.valueOf(this.psc), Integer.valueOf(this.uarfcn), this.mcc, this.mnc, Integer.valueOf(this.wcdmaSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WcdmaCellInformation)) {
            return false;
        }
        WcdmaCellInformation wcdmaCellInformation = (WcdmaCellInformation) obj;
        return this.lac == wcdmaCellInformation.lac && this.cellId == wcdmaCellInformation.cellId && this.psc == wcdmaCellInformation.psc && this.uarfcn == wcdmaCellInformation.uarfcn && this.mcc.equals(wcdmaCellInformation.mcc) && this.mnc.equals(wcdmaCellInformation.mnc) && this.wcdmaSignalInformation.equals(wcdmaCellInformation.wcdmaSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", lac=" + this.lac + ", cellId=" + this.cellId + ", psc=" + this.psc + ", uarfcn=" + this.uarfcn + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", wcdmaSignalInformation=" + this.wcdmaSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.lac);
        parcel.writeInt(this.cellId);
        parcel.writeInt(this.psc);
        parcel.writeInt(this.uarfcn);
        parcel.writeString(this.mcc);
        parcel.writeString(this.mnc);
        this.wcdmaSignalInformation.marshalling(parcel);
        return true;
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.lac = parcel.readInt();
        this.cellId = parcel.readInt();
        this.psc = parcel.readInt();
        this.uarfcn = parcel.readInt();
        this.mcc = parcel.readString();
        this.mnc = parcel.readString();
        this.wcdmaSignalInformation.unmarshalling(parcel);
        return true;
    }
}
