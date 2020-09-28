package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.hardware.radio.V1_0.CellInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;

public final class CellInfoCdma extends CellInfo implements Parcelable {
    public static final Parcelable.Creator<CellInfoCdma> CREATOR = new Parcelable.Creator<CellInfoCdma>() {
        /* class android.telephony.CellInfoCdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellInfoCdma createFromParcel(Parcel in) {
            in.readInt();
            return CellInfoCdma.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellInfoCdma[] newArray(int size) {
            return new CellInfoCdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellInfoCdma";
    private CellIdentityCdma mCellIdentityCdma;
    private CellSignalStrengthCdma mCellSignalStrengthCdma;

    @UnsupportedAppUsage
    public CellInfoCdma() {
        this.mCellIdentityCdma = new CellIdentityCdma();
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma();
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public CellInfoCdma(CellInfoCdma ci) {
        super(ci);
        this.mCellIdentityCdma = ci.mCellIdentityCdma.copy();
        this.mCellSignalStrengthCdma = ci.mCellSignalStrengthCdma.copy();
    }

    public CellInfoCdma(CellInfo ci) {
        super(ci);
        android.hardware.radio.V1_0.CellInfoCdma cic = ci.cdma.get(0);
        this.mCellIdentityCdma = new CellIdentityCdma(cic.cellIdentityCdma);
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma(cic.signalStrengthCdma, cic.signalStrengthEvdo);
    }

    public CellInfoCdma(android.hardware.radio.V1_2.CellInfo ci) {
        super(ci);
        android.hardware.radio.V1_2.CellInfoCdma cic = ci.cdma.get(0);
        this.mCellIdentityCdma = new CellIdentityCdma(cic.cellIdentityCdma);
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma(cic.signalStrengthCdma, cic.signalStrengthEvdo);
    }

    public CellInfoCdma(android.hardware.radio.V1_4.CellInfo ci, long timeStamp) {
        super(ci, timeStamp);
        android.hardware.radio.V1_2.CellInfoCdma cic = ci.info.cdma();
        this.mCellIdentityCdma = new CellIdentityCdma(cic.cellIdentityCdma);
        this.mCellSignalStrengthCdma = new CellSignalStrengthCdma(cic.signalStrengthCdma, cic.signalStrengthEvdo);
    }

    @Override // android.telephony.CellInfo
    public CellIdentityCdma getCellIdentity() {
        return this.mCellIdentityCdma;
    }

    @UnsupportedAppUsage
    public void setCellIdentity(CellIdentityCdma cid) {
        this.mCellIdentityCdma = cid;
    }

    @Override // android.telephony.CellInfo
    public CellSignalStrengthCdma getCellSignalStrength() {
        return this.mCellSignalStrengthCdma;
    }

    @Override // android.telephony.CellInfo
    public CellInfo sanitizeLocationInfo() {
        CellInfoCdma result = new CellInfoCdma(this);
        result.mCellIdentityCdma = this.mCellIdentityCdma.sanitizeLocationInfo();
        return result;
    }

    public void setCellSignalStrength(CellSignalStrengthCdma css) {
        this.mCellSignalStrengthCdma = css;
    }

    @Override // android.telephony.CellInfo
    public int hashCode() {
        return super.hashCode() + this.mCellIdentityCdma.hashCode() + this.mCellSignalStrengthCdma.hashCode();
    }

    @Override // android.telephony.CellInfo
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        try {
            CellInfoCdma o = (CellInfoCdma) other;
            if (!this.mCellIdentityCdma.equals(o.mCellIdentityCdma) || !this.mCellSignalStrengthCdma.equals(o.mCellSignalStrengthCdma)) {
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
        sb.append("CellInfoCdma:{");
        sb.append(super.toString());
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellIdentityCdma);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCellSignalStrengthCdma);
        sb.append("}");
        return sb.toString();
    }

    @Override // android.os.Parcelable, android.telephony.CellInfo
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.telephony.CellInfo
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags, 2);
        this.mCellIdentityCdma.writeToParcel(dest, flags);
        this.mCellSignalStrengthCdma.writeToParcel(dest, flags);
    }

    private CellInfoCdma(Parcel in) {
        super(in);
        this.mCellIdentityCdma = CellIdentityCdma.CREATOR.createFromParcel(in);
        this.mCellSignalStrengthCdma = CellSignalStrengthCdma.CREATOR.createFromParcel(in);
    }

    protected static CellInfoCdma createFromParcelBody(Parcel in) {
        return new CellInfoCdma(in);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
