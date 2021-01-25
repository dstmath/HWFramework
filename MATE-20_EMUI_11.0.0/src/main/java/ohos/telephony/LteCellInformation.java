package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class LteCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = LteCellInformation.class.getSimpleName();
    private int bandwidth = Integer.MAX_VALUE;
    private int cgi = Integer.MAX_VALUE;
    private int earfcn = Integer.MAX_VALUE;
    private boolean isSupportEndc = false;
    private LteSignalInformation lteSignalInformation = new LteSignalInformation();
    private String mcc = "";
    private String mnc = "";
    private int pci = Integer.MAX_VALUE;
    private int tac = Integer.MAX_VALUE;

    protected LteCellInformation() {
        super(5, false, 0);
    }

    public int getCgi() {
        return this.cgi;
    }

    public int getPci() {
        return this.pci;
    }

    public int getTac() {
        return this.tac;
    }

    public int getEarfcn() {
        return this.earfcn;
    }

    public int getBandwidth() {
        return this.bandwidth;
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

    public boolean isSupportEndc() {
        return this.isSupportEndc;
    }

    @Override // ohos.telephony.CellInformation
    public LteSignalInformation getSignalInformation() {
        return this.lteSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.cgi), Integer.valueOf(this.pci), Integer.valueOf(this.tac), Integer.valueOf(this.earfcn), Integer.valueOf(this.bandwidth), this.mcc, this.mnc, Boolean.valueOf(this.isSupportEndc), Integer.valueOf(this.lteSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LteCellInformation)) {
            return false;
        }
        LteCellInformation lteCellInformation = (LteCellInformation) obj;
        return this.cgi == lteCellInformation.cgi && this.pci == lteCellInformation.pci && this.tac == lteCellInformation.tac && this.earfcn == lteCellInformation.earfcn && this.bandwidth == lteCellInformation.bandwidth && this.mcc.equals(lteCellInformation.mcc) && this.mnc.equals(lteCellInformation.mnc) && this.isSupportEndc == lteCellInformation.isSupportEndc && this.lteSignalInformation.equals(lteCellInformation.lteSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", cgi=" + this.cgi + ", pci=" + this.pci + ", tac=" + this.tac + ", earfcn=" + this.earfcn + ", bandwidth=" + this.bandwidth + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", isSupportEndc=" + this.isSupportEndc + ", lteSignalInformation=" + this.lteSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.cgi);
        parcel.writeInt(this.pci);
        parcel.writeInt(this.tac);
        parcel.writeInt(this.earfcn);
        parcel.writeInt(this.bandwidth);
        parcel.writeString(this.mcc);
        parcel.writeString(this.mnc);
        parcel.writeBoolean(this.isSupportEndc);
        this.lteSignalInformation.marshalling(parcel);
        return true;
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.cgi = parcel.readInt();
        this.pci = parcel.readInt();
        this.tac = parcel.readInt();
        this.earfcn = parcel.readInt();
        this.bandwidth = parcel.readInt();
        this.mcc = parcel.readString();
        this.mnc = parcel.readString();
        this.isSupportEndc = parcel.readBoolean();
        this.lteSignalInformation.unmarshalling(parcel);
        return true;
    }
}
