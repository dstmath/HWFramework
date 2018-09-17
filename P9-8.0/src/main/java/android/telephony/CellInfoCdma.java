package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class CellInfoCdma extends CellInfo implements Parcelable {
    public static final Creator<CellInfoCdma> CREATOR = new Creator<CellInfoCdma>() {
        public CellInfoCdma createFromParcel(Parcel in) {
            in.readInt();
            return CellInfoCdma.createFromParcelBody(in);
        }

        public CellInfoCdma[] newArray(int size) {
            return new CellInfoCdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoCdma";
    private CellIdentityCdma mCellIdentityCdma;
    private CellSignalStrengthCdma mCellSignalStrengthCdma;

    public CellInfoCdma() {
        this.mCellIdentityCdma = new CellIdentityCdma();
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma();
    }

    public CellInfoCdma(CellInfoCdma ci) {
        super((CellInfo) ci);
        this.mCellIdentityCdma = ci.mCellIdentityCdma.copy();
        this.mCellSignalStrengthCdma = ci.mCellSignalStrengthCdma.copy();
    }

    public CellIdentityCdma getCellIdentity() {
        return this.mCellIdentityCdma;
    }

    public void setCellIdentity(CellIdentityCdma cid) {
        this.mCellIdentityCdma = cid;
    }

    public CellSignalStrengthCdma getCellSignalStrength() {
        return this.mCellSignalStrengthCdma;
    }

    public void setCellSignalStrength(CellSignalStrengthCdma css) {
        this.mCellSignalStrengthCdma = css;
    }

    public int hashCode() {
        return (super.hashCode() + this.mCellIdentityCdma.hashCode()) + this.mCellSignalStrengthCdma.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other)) {
            return false;
        }
        try {
            CellInfoCdma o = (CellInfoCdma) other;
            if (this.mCellIdentityCdma.equals(o.mCellIdentityCdma)) {
                z = this.mCellSignalStrengthCdma.equals(o.mCellSignalStrengthCdma);
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoCdma:{");
        sb.append(super.toString());
        sb.append(" ").append(this.mCellIdentityCdma);
        sb.append(" ").append(this.mCellSignalStrengthCdma);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 2);
        this.mCellIdentityCdma.writeToParcel(dest, flags);
        this.mCellSignalStrengthCdma.writeToParcel(dest, flags);
    }

    private CellInfoCdma(Parcel in) {
        super(in);
        this.mCellIdentityCdma = (CellIdentityCdma) CellIdentityCdma.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthCdma = (CellSignalStrengthCdma) CellSignalStrengthCdma.CREATOR.createFromParcel(in);
    }

    protected static CellInfoCdma createFromParcelBody(Parcel in) {
        return new CellInfoCdma(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
