package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class NrCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = NrCellInformation.class.getSimpleName();
    private String mcc = "";
    private String mnc = "";
    private long nci = Long.MAX_VALUE;
    private int nrArfcn = Integer.MAX_VALUE;
    private NrSignalInformation nrSignalInformation = new NrSignalInformation();
    private int pci = Integer.MAX_VALUE;
    private int tac = Integer.MAX_VALUE;

    protected NrCellInformation() {
        super(6, false, 0);
    }

    public int getNrArfcn() {
        return this.nrArfcn;
    }

    public int getPci() {
        return this.pci;
    }

    public int getTac() {
        return this.tac;
    }

    public long getNci() {
        return this.nci;
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
    public NrSignalInformation getSignalInformation() {
        return this.nrSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.nrArfcn), Integer.valueOf(this.pci), Integer.valueOf(this.tac), Long.valueOf(this.nci), this.mcc, this.mnc, Integer.valueOf(this.nrSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NrCellInformation)) {
            return false;
        }
        NrCellInformation nrCellInformation = (NrCellInformation) obj;
        return this.nrArfcn == nrCellInformation.nrArfcn && this.pci == nrCellInformation.pci && this.tac == nrCellInformation.tac && this.nci == nrCellInformation.nci && this.mcc.equals(nrCellInformation.mcc) && this.mnc.equals(nrCellInformation.mnc) && this.nrSignalInformation.equals(nrCellInformation.nrSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", nrArfcn=" + this.nrArfcn + ", pci=" + this.pci + ", tac=" + this.tac + ", nci=" + this.nci + ", mcc=" + this.mcc + ", mnc=" + this.mnc + ", nrSignalInformation=" + this.nrSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.nrArfcn);
        parcel.writeInt(this.pci);
        parcel.writeInt(this.tac);
        parcel.writeLong(this.nci);
        parcel.writeString(this.mcc);
        parcel.writeString(this.mnc);
        this.nrSignalInformation.marshalling(parcel);
        return true;
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.nrArfcn = parcel.readInt();
        this.pci = parcel.readInt();
        this.tac = parcel.readInt();
        this.nci = parcel.readLong();
        this.mcc = parcel.readString();
        this.mnc = parcel.readString();
        this.nrSignalInformation.unmarshalling(parcel);
        return true;
    }
}
