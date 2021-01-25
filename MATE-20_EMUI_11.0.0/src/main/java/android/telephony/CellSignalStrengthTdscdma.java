package android.telephony;

import android.hardware.radio.V1_0.TdScdmaSignalStrength;
import android.hardware.radio.V1_2.TdscdmaSignalStrength;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import java.util.Objects;

public final class CellSignalStrengthTdscdma extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthTdscdma> CREATOR = new Parcelable.Creator<CellSignalStrengthTdscdma>() {
        /* class android.telephony.CellSignalStrengthTdscdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthTdscdma createFromParcel(Parcel in) {
            return new CellSignalStrengthTdscdma(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthTdscdma[] newArray(int size) {
            return new CellSignalStrengthTdscdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthTdscdma";
    private static final int TDSCDMA_RSCP_GOOD = -73;
    private static final int TDSCDMA_RSCP_GREAT = -49;
    private static final int TDSCDMA_RSCP_MAX = -24;
    private static final int TDSCDMA_RSCP_MIN = -120;
    private static final int TDSCDMA_RSCP_MODERATE = -97;
    private static final int TDSCDMA_RSCP_POOR = -110;
    private static final CellSignalStrengthTdscdma sInvalid = new CellSignalStrengthTdscdma();
    private int mBitErrorRate;
    private int mLevel;
    private int mRscp;
    private int mRssi;

    public CellSignalStrengthTdscdma() {
        setDefaultValues();
    }

    public CellSignalStrengthTdscdma(int rssi, int ber, int rscp) {
        this.mRssi = inRangeOrUnavailable(rssi, -113, -51);
        this.mBitErrorRate = inRangeOrUnavailable(ber, 0, 7, 99);
        this.mRscp = inRangeOrUnavailable(rscp, -120, -24);
        updateLevel(null, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public CellSignalStrengthTdscdma(TdScdmaSignalStrength tdscdma) {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE, tdscdma.rscp != Integer.MAX_VALUE ? -tdscdma.rscp : tdscdma.rscp);
        if (this.mRssi == Integer.MAX_VALUE && this.mRscp == Integer.MAX_VALUE) {
            setDefaultValues();
        }
    }

    public CellSignalStrengthTdscdma(TdscdmaSignalStrength tdscdma) {
        this(getRssiDbmFromAsu(tdscdma.signalStrength), tdscdma.bitErrorRate, getRscpDbmFromAsu(tdscdma.rscp));
        if (this.mRssi == Integer.MAX_VALUE && this.mRscp == Integer.MAX_VALUE) {
            setDefaultValues();
        }
    }

    public CellSignalStrengthTdscdma(CellSignalStrengthTdscdma s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(CellSignalStrengthTdscdma s) {
        this.mRssi = s.mRssi;
        this.mBitErrorRate = s.mBitErrorRate;
        this.mRscp = s.mRscp;
        this.mLevel = s.mLevel;
        this.mPhoneId = s.mPhoneId;
    }

    @Override // android.telephony.CellSignalStrength
    public CellSignalStrengthTdscdma copy() {
        return new CellSignalStrengthTdscdma(this);
    }

    @Override // android.telephony.CellSignalStrength
    public void setDefaultValues() {
        this.mRssi = Integer.MAX_VALUE;
        this.mBitErrorRate = Integer.MAX_VALUE;
        this.mRscp = Integer.MAX_VALUE;
        this.mLevel = 0;
        this.mPhoneId = -1;
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevel() {
        return this.mLevel;
    }

    @Override // android.telephony.CellSignalStrength
    public void updateLevel(PersistableBundle cc, ServiceState ss) {
        int i = this.mRscp;
        if (i > -24) {
            this.mLevel = 0;
        } else if (i >= -49) {
            this.mLevel = 4;
        } else if (i >= TDSCDMA_RSCP_GOOD) {
            this.mLevel = 3;
        } else if (i >= -97) {
            this.mLevel = 2;
        } else if (i >= -110) {
            this.mLevel = 1;
        } else {
            this.mLevel = 0;
        }
    }

    @Override // android.telephony.CellSignalStrength
    public int getDbm() {
        return this.mRscp;
    }

    public int getRscp() {
        return this.mRscp;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getBitErrorRate() {
        return this.mBitErrorRate;
    }

    @Override // android.telephony.CellSignalStrength
    public int getAsuLevel() {
        int i = this.mRscp;
        if (i != Integer.MAX_VALUE) {
            return getAsuFromRscpDbm(i);
        }
        int i2 = this.mRssi;
        if (i2 != Integer.MAX_VALUE) {
            return getAsuFromRssiDbm(i2);
        }
        return getAsuFromRscpDbm(Integer.MAX_VALUE);
    }

    @Override // android.telephony.CellSignalStrength
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mRssi), Integer.valueOf(this.mBitErrorRate), Integer.valueOf(this.mRscp), Integer.valueOf(this.mLevel));
    }

    @Override // android.telephony.CellSignalStrength
    public boolean isValid() {
        return !equals(sInvalid);
    }

    @Override // android.telephony.CellSignalStrength
    public boolean equals(Object o) {
        if (!(o instanceof CellSignalStrengthTdscdma)) {
            return false;
        }
        CellSignalStrengthTdscdma s = (CellSignalStrengthTdscdma) o;
        if (this.mRssi == s.mRssi && this.mBitErrorRate == s.mBitErrorRate && this.mRscp == s.mRscp && this.mLevel == s.mLevel) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "CellSignalStrengthTdscdma: rssi=" + this.mRssi + " ber=" + this.mBitErrorRate + " rscp=" + this.mRscp + " level=" + this.mLevel + " mPhoneId=" + this.mPhoneId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mBitErrorRate);
        dest.writeInt(this.mRscp);
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mPhoneId);
    }

    private CellSignalStrengthTdscdma(Parcel in) {
        this.mRssi = in.readInt();
        this.mBitErrorRate = in.readInt();
        this.mRscp = in.readInt();
        this.mLevel = in.readInt();
        this.mPhoneId = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevelHw() {
        return getLevel();
    }
}
