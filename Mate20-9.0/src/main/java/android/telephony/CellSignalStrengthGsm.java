package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class CellSignalStrengthGsm extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthGsm> CREATOR = new Parcelable.Creator<CellSignalStrengthGsm>() {
        public CellSignalStrengthGsm createFromParcel(Parcel in) {
            return new CellSignalStrengthGsm(in);
        }

        public CellSignalStrengthGsm[] newArray(int size) {
            return new CellSignalStrengthGsm[size];
        }
    };
    private static final boolean DBG = false;
    private static final int GSM_SIGNAL_STRENGTH_GOOD = 8;
    private static final int GSM_SIGNAL_STRENGTH_GREAT = 12;
    private static final int GSM_SIGNAL_STRENGTH_MODERATE = 5;
    private static final String LOG_TAG = "CellSignalStrengthGsm";
    private int mBitErrorRate;
    private int mSignalStrength;
    private int mTimingAdvance;

    public CellSignalStrengthGsm() {
        setDefaultValues();
    }

    public CellSignalStrengthGsm(int ss, int ber) {
        this(ss, ber, Integer.MAX_VALUE);
    }

    public CellSignalStrengthGsm(int ss, int ber, int ta) {
        this.mSignalStrength = ss;
        this.mBitErrorRate = ber;
        this.mTimingAdvance = ta;
    }

    public CellSignalStrengthGsm(CellSignalStrengthGsm s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(CellSignalStrengthGsm s) {
        this.mSignalStrength = s.mSignalStrength;
        this.mBitErrorRate = s.mBitErrorRate;
        this.mTimingAdvance = s.mTimingAdvance;
    }

    public CellSignalStrengthGsm copy() {
        return new CellSignalStrengthGsm(this);
    }

    public void setDefaultValues() {
        this.mSignalStrength = Integer.MAX_VALUE;
        this.mBitErrorRate = Integer.MAX_VALUE;
        this.mTimingAdvance = Integer.MAX_VALUE;
    }

    public int getLevel() {
        int asu = this.mSignalStrength;
        if (asu <= 2 || asu == 99) {
            return 0;
        }
        if (asu >= 12) {
            return 4;
        }
        if (asu >= 8) {
            return 3;
        }
        if (asu >= 5) {
            return 2;
        }
        return 1;
    }

    public int getTimingAdvance() {
        return this.mTimingAdvance;
    }

    public int getDbm() {
        int level = this.mSignalStrength;
        int asu = level == 99 ? Integer.MAX_VALUE : level;
        if (asu != Integer.MAX_VALUE) {
            return -113 + (2 * asu);
        }
        return Integer.MAX_VALUE;
    }

    public int getAsuLevel() {
        return this.mSignalStrength;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mSignalStrength), Integer.valueOf(this.mBitErrorRate), Integer.valueOf(this.mTimingAdvance)});
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            CellSignalStrengthGsm s = (CellSignalStrengthGsm) o;
            if (o == null) {
                return false;
            }
            if (this.mSignalStrength == s.mSignalStrength && this.mBitErrorRate == s.mBitErrorRate && s.mTimingAdvance == this.mTimingAdvance) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "CellSignalStrengthGsm: ss=" + this.mSignalStrength + " ber=" + this.mBitErrorRate + " mTa=" + this.mTimingAdvance;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSignalStrength);
        dest.writeInt(this.mBitErrorRate);
        dest.writeInt(this.mTimingAdvance);
    }

    private CellSignalStrengthGsm(Parcel in) {
        this.mSignalStrength = in.readInt();
        this.mBitErrorRate = in.readInt();
        this.mTimingAdvance = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
