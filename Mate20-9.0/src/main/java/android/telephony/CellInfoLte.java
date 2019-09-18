package android.telephony;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;

public final class CellInfoLte extends CellInfo implements Parcelable {
    public static final Parcelable.Creator<CellInfoLte> CREATOR = new Parcelable.Creator<CellInfoLte>() {
        public CellInfoLte createFromParcel(Parcel in) {
            in.readInt();
            return CellInfoLte.createFromParcelBody(in);
        }

        public CellInfoLte[] newArray(int size) {
            return new CellInfoLte[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoLte";
    private CellIdentityLte mCellIdentityLte;
    private CellSignalStrengthLte mCellSignalStrengthLte;

    public CellInfoLte() {
        this.mCellIdentityLte = new CellIdentityLte();
        this.mCellSignalStrengthLte = new CellSignalStrengthLte();
    }

    public CellInfoLte(CellInfoLte ci) {
        super((CellInfo) ci);
        this.mCellIdentityLte = ci.mCellIdentityLte.copy();
        this.mCellSignalStrengthLte = ci.mCellSignalStrengthLte.copy();
    }

    public CellIdentityLte getCellIdentity() {
        return this.mCellIdentityLte;
    }

    public void setCellIdentity(CellIdentityLte cid) {
        this.mCellIdentityLte = cid;
    }

    public CellSignalStrengthLte getCellSignalStrength() {
        return this.mCellSignalStrengthLte;
    }

    public void setCellSignalStrength(CellSignalStrengthLte css) {
        this.mCellSignalStrengthLte = css;
    }

    public int hashCode() {
        return super.hashCode() + this.mCellIdentityLte.hashCode() + this.mCellSignalStrengthLte.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other)) {
            return false;
        }
        try {
            CellInfoLte o = (CellInfoLte) other;
            if (this.mCellIdentityLte.equals(o.mCellIdentityLte) && this.mCellSignalStrengthLte.equals(o.mCellSignalStrengthLte)) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoLte:{");
        sb.append(super.toString());
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellIdentityLte);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellSignalStrengthLte);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 3);
        this.mCellIdentityLte.writeToParcel(dest, flags);
        this.mCellSignalStrengthLte.writeToParcel(dest, flags);
    }

    private CellInfoLte(Parcel in) {
        super(in);
        this.mCellIdentityLte = CellIdentityLte.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthLte = CellSignalStrengthLte.CREATOR.createFromParcel(in);
    }

    protected static CellInfoLte createFromParcelBody(Parcel in) {
        return new CellInfoLte(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
