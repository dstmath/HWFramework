package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class TdscdmaCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = TdscdmaCellInformation.class.getSimpleName();
    private int cellId = Integer.MAX_VALUE;
    private int cpid = Integer.MAX_VALUE;
    private int lac = Integer.MAX_VALUE;
    private String mcc = "";
    private String mnc = "";
    private TdscdmaSignalInformation tdscdmaSignalInformation = new TdscdmaSignalInformation();
    private int uarfcn = Integer.MAX_VALUE;

    protected TdscdmaCellInformation() {
        super(4, false, 0);
    }

    public int getLac() {
        return this.lac;
    }

    public int getCellId() {
        return this.cellId;
    }

    public int getCpid() {
        return this.cpid;
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
    public TdscdmaSignalInformation getSignalInformation() {
        return this.tdscdmaSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.lac), Integer.valueOf(this.cellId), Integer.valueOf(this.cpid), Integer.valueOf(this.uarfcn), this.mcc, this.mnc, Integer.valueOf(this.tdscdmaSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TdscdmaCellInformation)) {
            return false;
        }
        TdscdmaCellInformation tdscdmaCellInformation = (TdscdmaCellInformation) obj;
        return this.lac == tdscdmaCellInformation.lac && this.cellId == tdscdmaCellInformation.cellId && this.cpid == tdscdmaCellInformation.cpid && this.uarfcn == tdscdmaCellInformation.uarfcn && this.mcc.equals(tdscdmaCellInformation.mcc) && this.mnc.equals(tdscdmaCellInformation.mnc) && this.tdscdmaSignalInformation.equals(tdscdmaCellInformation.tdscdmaSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", lac=" + this.lac + ", cellId=" + this.cellId + ", cpid=" + this.cpid + ", uarfcn=" + this.uarfcn + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", tdscdmaSignalInformation=" + this.tdscdmaSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.lac);
        parcel.writeInt(this.cellId);
        parcel.writeInt(this.cpid);
        parcel.writeInt(this.uarfcn);
        parcel.writeString(this.mcc);
        parcel.writeString(this.mnc);
        this.tdscdmaSignalInformation.marshalling(parcel);
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
        this.cpid = parcel.readInt();
        this.uarfcn = parcel.readInt();
        this.mcc = parcel.readString();
        this.mnc = parcel.readString();
        this.tdscdmaSignalInformation.unmarshalling(parcel);
        return true;
    }
}
