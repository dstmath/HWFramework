package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class CellInfoGsm extends CellInfo implements Parcelable {
    public static final Creator<CellInfoGsm> CREATOR = new Creator<CellInfoGsm>() {
        public CellInfoGsm createFromParcel(Parcel in) {
            in.readInt();
            return CellInfoGsm.createFromParcelBody(in);
        }

        public CellInfoGsm[] newArray(int size) {
            return new CellInfoGsm[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoGsm";
    private CellIdentityGsm mCellIdentityGsm;
    private CellSignalStrengthGsm mCellSignalStrengthGsm;

    public CellInfoGsm() {
        this.mCellIdentityGsm = new CellIdentityGsm();
        this.mCellSignalStrengthGsm = new CellSignalStrengthGsm();
    }

    public CellInfoGsm(CellInfoGsm ci) {
        super((CellInfo) ci);
        this.mCellIdentityGsm = ci.mCellIdentityGsm.copy();
        this.mCellSignalStrengthGsm = ci.mCellSignalStrengthGsm.copy();
    }

    public CellIdentityGsm getCellIdentity() {
        return this.mCellIdentityGsm;
    }

    public void setCellIdentity(CellIdentityGsm cid) {
        this.mCellIdentityGsm = cid;
    }

    public CellSignalStrengthGsm getCellSignalStrength() {
        return this.mCellSignalStrengthGsm;
    }

    public void setCellSignalStrength(CellSignalStrengthGsm css) {
        this.mCellSignalStrengthGsm = css;
    }

    public int hashCode() {
        return (super.hashCode() + this.mCellIdentityGsm.hashCode()) + this.mCellSignalStrengthGsm.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other)) {
            return false;
        }
        try {
            CellInfoGsm o = (CellInfoGsm) other;
            if (this.mCellIdentityGsm.equals(o.mCellIdentityGsm)) {
                z = this.mCellSignalStrengthGsm.equals(o.mCellSignalStrengthGsm);
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoGsm:{");
        sb.append(super.toString());
        sb.append(" ").append(this.mCellIdentityGsm);
        sb.append(" ").append(this.mCellSignalStrengthGsm);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 1);
        this.mCellIdentityGsm.writeToParcel(dest, flags);
        this.mCellSignalStrengthGsm.writeToParcel(dest, flags);
    }

    private CellInfoGsm(Parcel in) {
        super(in);
        this.mCellIdentityGsm = (CellIdentityGsm) CellIdentityGsm.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthGsm = (CellSignalStrengthGsm) CellSignalStrengthGsm.CREATOR.createFromParcel(in);
    }

    protected static CellInfoGsm createFromParcelBody(Parcel in) {
        return new CellInfoGsm(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
