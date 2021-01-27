package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class CdmaCellInformation extends CellInformation implements Sequenceable {
    private static final String CELLTYPENAME = CdmaCellInformation.class.getSimpleName();
    private int baseId = Integer.MAX_VALUE;
    private CdmaSignalInformation cdmaSignalInformation = new CdmaSignalInformation();
    private int latitude = Integer.MAX_VALUE;
    private int longitude = Integer.MAX_VALUE;
    private int nid = Integer.MAX_VALUE;
    private int sid = Integer.MAX_VALUE;

    protected CdmaCellInformation() {
        super(2, false, 0);
    }

    public int getNid() {
        return this.nid;
    }

    public int getSid() {
        return this.sid;
    }

    public int getBaseId() {
        return this.baseId;
    }

    public int getLongitude() {
        return this.longitude;
    }

    public int getLatitude() {
        return this.latitude;
    }

    @Override // ohos.telephony.CellInformation
    public CdmaSignalInformation getSignalInformation() {
        return this.cdmaSignalInformation;
    }

    @Override // ohos.telephony.CellInformation
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.nid), Integer.valueOf(this.sid), Integer.valueOf(this.baseId), Integer.valueOf(this.longitude), Integer.valueOf(this.latitude), Integer.valueOf(this.cdmaSignalInformation.hashCode()), Integer.valueOf(super.hashCode()));
    }

    @Override // ohos.telephony.CellInformation
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CdmaCellInformation)) {
            return false;
        }
        CdmaCellInformation cdmaCellInformation = (CdmaCellInformation) obj;
        return this.nid == cdmaCellInformation.nid && this.sid == cdmaCellInformation.sid && this.baseId == cdmaCellInformation.baseId && this.longitude == cdmaCellInformation.longitude && this.latitude == cdmaCellInformation.latitude && this.cdmaSignalInformation.equals(cdmaCellInformation.cdmaSignalInformation) && super.equals(obj);
    }

    @Override // ohos.telephony.CellInformation
    public String toString() {
        return CELLTYPENAME + super.toString() + ", nid=" + this.nid + ", sid=" + this.sid + ", baseId=" + this.baseId + ", longitude=" + this.longitude + ", latitude=" + this.latitude + ", cdmaSignalInformation=" + this.cdmaSignalInformation + "}";
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.marshalling(parcel);
        parcel.writeInt(this.nid);
        parcel.writeInt(this.sid);
        parcel.writeInt(this.baseId);
        parcel.writeInt(this.longitude);
        parcel.writeInt(this.latitude);
        this.cdmaSignalInformation.marshalling(parcel);
        return true;
    }

    @Override // ohos.telephony.CellInformation, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        super.unmarshalling(parcel);
        this.nid = parcel.readInt();
        this.sid = parcel.readInt();
        this.baseId = parcel.readInt();
        this.longitude = parcel.readInt();
        this.latitude = parcel.readInt();
        this.cdmaSignalInformation.unmarshalling(parcel);
        return true;
    }
}
