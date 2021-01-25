package android.telephony;

import android.hardware.radio.V1_4.CellInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class CellInfoNr extends CellInfo {
    public static final Parcelable.Creator<CellInfoNr> CREATOR = new Parcelable.Creator<CellInfoNr>() {
        /* class android.telephony.CellInfoNr.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellInfoNr createFromParcel(Parcel in) {
            in.readInt();
            return new CellInfoNr(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellInfoNr[] newArray(int size) {
            return new CellInfoNr[size];
        }
    };
    private static final String TAG = "CellInfoNr";
    private final CellIdentityNr mCellIdentity;
    private final CellSignalStrengthNr mCellSignalStrength;

    private CellInfoNr(Parcel in) {
        super(in);
        this.mCellIdentity = CellIdentityNr.CREATOR.createFromParcel(in);
        this.mCellSignalStrength = CellSignalStrengthNr.CREATOR.createFromParcel(in);
    }

    public CellInfoNr(CellInfo ci, long timeStamp) {
        super(ci, timeStamp);
        android.hardware.radio.V1_4.CellInfoNr cig = ci.info.nr();
        this.mCellIdentity = new CellIdentityNr(cig.cellidentity);
        this.mCellSignalStrength = new CellSignalStrengthNr(cig.signalStrength);
    }

    private CellInfoNr(CellInfoNr other, boolean sanitizeLocationInfo) {
        super(other);
        CellIdentityNr cellIdentityNr;
        if (sanitizeLocationInfo) {
            cellIdentityNr = other.mCellIdentity.sanitizeLocationInfo();
        } else {
            cellIdentityNr = other.mCellIdentity;
        }
        this.mCellIdentity = cellIdentityNr;
        this.mCellSignalStrength = other.mCellSignalStrength;
    }

    @Override // android.telephony.CellInfo
    public CellIdentity getCellIdentity() {
        return this.mCellIdentity;
    }

    @Override // android.telephony.CellInfo
    public CellSignalStrength getCellSignalStrength() {
        return this.mCellSignalStrength;
    }

    @Override // android.telephony.CellInfo
    public CellInfo sanitizeLocationInfo() {
        return new CellInfoNr(this, true);
    }

    @Override // android.telephony.CellInfo
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), this.mCellIdentity, this.mCellSignalStrength);
    }

    @Override // android.telephony.CellInfo
    public boolean equals(Object other) {
        if (!(other instanceof CellInfoNr)) {
            return false;
        }
        CellInfoNr o = (CellInfoNr) other;
        if (!super.equals(o) || !this.mCellIdentity.equals(o.mCellIdentity) || !this.mCellSignalStrength.equals(o.mCellSignalStrength)) {
            return false;
        }
        return true;
    }

    @Override // android.telephony.CellInfo
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CellInfoNr:{");
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + super.toString());
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mCellIdentity);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mCellSignalStrength);
        sb.append(" }");
        return sb.toString();
    }

    @Override // android.telephony.CellInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 6);
        this.mCellIdentity.writeToParcel(dest, flags);
        this.mCellSignalStrength.writeToParcel(dest, flags);
    }

    protected static CellInfoNr createFromParcelBody(Parcel in) {
        return new CellInfoNr(in);
    }
}
