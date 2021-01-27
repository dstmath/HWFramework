package android.telephony;

import android.hardware.radio.V1_0.CellInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class CellInfoWcdma extends CellInfo implements Parcelable {
    public static final Parcelable.Creator<CellInfoWcdma> CREATOR = new Parcelable.Creator<CellInfoWcdma>() {
        /* class android.telephony.CellInfoWcdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellInfoWcdma createFromParcel(Parcel in) {
            in.readInt();
            return CellInfoWcdma.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellInfoWcdma[] newArray(int size) {
            return new CellInfoWcdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoWcdma";
    private CellIdentityWcdma mCellIdentityWcdma;
    private CellSignalStrengthWcdma mCellSignalStrengthWcdma;

    public CellInfoWcdma() {
        this.mCellIdentityWcdma = new CellIdentityWcdma();
        this.mCellSignalStrengthWcdma = new CellSignalStrengthWcdma();
    }

    public CellInfoWcdma(CellInfoWcdma ci) {
        super(ci);
        this.mCellIdentityWcdma = ci.mCellIdentityWcdma.copy();
        this.mCellSignalStrengthWcdma = ci.mCellSignalStrengthWcdma.copy();
    }

    public CellInfoWcdma(CellInfo ci) {
        super(ci);
        android.hardware.radio.V1_0.CellInfoWcdma ciw = ci.wcdma.get(0);
        this.mCellIdentityWcdma = new CellIdentityWcdma(ciw.cellIdentityWcdma);
        this.mCellSignalStrengthWcdma = new CellSignalStrengthWcdma(ciw.signalStrengthWcdma);
    }

    public CellInfoWcdma(android.hardware.radio.V1_2.CellInfo ci) {
        super(ci);
        android.hardware.radio.V1_2.CellInfoWcdma ciw = ci.wcdma.get(0);
        this.mCellIdentityWcdma = new CellIdentityWcdma(ciw.cellIdentityWcdma);
        this.mCellSignalStrengthWcdma = new CellSignalStrengthWcdma(ciw.signalStrengthWcdma);
    }

    public CellInfoWcdma(android.hardware.radio.V1_4.CellInfo ci, long timeStamp) {
        super(ci, timeStamp);
        android.hardware.radio.V1_2.CellInfoWcdma ciw = ci.info.wcdma();
        this.mCellIdentityWcdma = new CellIdentityWcdma(ciw.cellIdentityWcdma);
        this.mCellSignalStrengthWcdma = new CellSignalStrengthWcdma(ciw.signalStrengthWcdma);
    }

    @Override // android.telephony.CellInfo
    public CellIdentityWcdma getCellIdentity() {
        return this.mCellIdentityWcdma;
    }

    public void setCellIdentity(CellIdentityWcdma cid) {
        this.mCellIdentityWcdma = cid;
    }

    @Override // android.telephony.CellInfo
    public CellSignalStrengthWcdma getCellSignalStrength() {
        return this.mCellSignalStrengthWcdma;
    }

    @Override // android.telephony.CellInfo
    public CellInfo sanitizeLocationInfo() {
        CellInfoWcdma result = new CellInfoWcdma(this);
        result.mCellIdentityWcdma = this.mCellIdentityWcdma.sanitizeLocationInfo();
        return result;
    }

    public void setCellSignalStrength(CellSignalStrengthWcdma css) {
        this.mCellSignalStrengthWcdma = css;
    }

    @Override // android.telephony.CellInfo
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), this.mCellIdentityWcdma, this.mCellSignalStrengthWcdma);
    }

    @Override // android.telephony.CellInfo
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        try {
            CellInfoWcdma o = (CellInfoWcdma) other;
            if (!this.mCellIdentityWcdma.equals(o.mCellIdentityWcdma) || !this.mCellSignalStrengthWcdma.equals(o.mCellSignalStrengthWcdma)) {
                return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override // android.telephony.CellInfo
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CellInfoWcdma:{");
        sb.append(super.toString());
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellIdentityWcdma);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellSignalStrengthWcdma);
        sb.append("}");
        return sb.toString();
    }

    @Override // android.telephony.CellInfo, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.telephony.CellInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 4);
        this.mCellIdentityWcdma.writeToParcel(dest, flags);
        this.mCellSignalStrengthWcdma.writeToParcel(dest, flags);
    }

    private CellInfoWcdma(Parcel in) {
        super(in);
        this.mCellIdentityWcdma = CellIdentityWcdma.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthWcdma = CellSignalStrengthWcdma.CREATOR.createFromParcel(in);
    }

    protected static CellInfoWcdma createFromParcelBody(Parcel in) {
        return new CellInfoWcdma(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
