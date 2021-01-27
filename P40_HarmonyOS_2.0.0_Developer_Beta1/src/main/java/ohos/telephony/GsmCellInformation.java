package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class GsmCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = GsmCellInformation.class.getSimpleName();
    private int arfcn = Integer.MAX_VALUE;
    private int bsic = Integer.MAX_VALUE;
    private int cellId = Integer.MAX_VALUE;
    private GsmSignalInformation gsmSignalInformation = new GsmSignalInformation();
    private int lac = Integer.MAX_VALUE;
    private String mcc = "";
    private String mnc = "";

    protected GsmCellInformation() {
        super(1, false, 0);
    }

    public int getLac() {
        return this.lac;
    }

    public int getCellId() {
        return this.cellId;
    }

    public int getArfcn() {
        return this.arfcn;
    }

    public int getBsic() {
        return this.bsic;
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
    public GsmSignalInformation getSignalInformation() {
        return this.gsmSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.lac), Integer.valueOf(this.cellId), Integer.valueOf(this.arfcn), Integer.valueOf(this.bsic), this.mcc, this.mnc, Integer.valueOf(this.gsmSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GsmCellInformation)) {
            return false;
        }
        GsmCellInformation gsmCellInformation = (GsmCellInformation) obj;
        return this.lac == gsmCellInformation.lac && this.cellId == gsmCellInformation.cellId && this.arfcn == gsmCellInformation.arfcn && this.bsic == gsmCellInformation.bsic && this.mcc.equals(gsmCellInformation.mcc) && this.mnc.equals(gsmCellInformation.mnc) && this.gsmSignalInformation.equals(gsmCellInformation.gsmSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", lac=" + this.lac + ", cellId=" + this.cellId + ", arfcn=" + this.arfcn + ", bsic=" + this.bsic + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", gsmSignalInformation=" + this.gsmSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.lac);
        parcel.writeInt(this.cellId);
        parcel.writeInt(this.arfcn);
        parcel.writeInt(this.bsic);
        parcel.writeString(this.mcc);
        parcel.writeString(this.mnc);
        this.gsmSignalInformation.marshalling(parcel);
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
        this.arfcn = parcel.readInt();
        this.bsic = parcel.readInt();
        this.mcc = parcel.readString();
        this.mnc = parcel.readString();
        this.gsmSignalInformation.unmarshalling(parcel);
        return true;
    }
}
