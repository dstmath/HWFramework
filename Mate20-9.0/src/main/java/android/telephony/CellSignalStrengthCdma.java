package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class CellSignalStrengthCdma extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthCdma> CREATOR = new Parcelable.Creator<CellSignalStrengthCdma>() {
        public CellSignalStrengthCdma createFromParcel(Parcel in) {
            return new CellSignalStrengthCdma(in);
        }

        public CellSignalStrengthCdma[] newArray(int size) {
            return new CellSignalStrengthCdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthCdma";
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;

    public CellSignalStrengthCdma() {
        setDefaultValues();
    }

    public CellSignalStrengthCdma(int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr) {
        int i = Integer.MAX_VALUE;
        this.mCdmaDbm = (cdmaDbm <= 0 || cdmaDbm >= 120) ? Integer.MAX_VALUE : -cdmaDbm;
        this.mCdmaEcio = (cdmaEcio <= 0 || cdmaEcio >= 160) ? Integer.MAX_VALUE : -cdmaEcio;
        this.mEvdoDbm = (evdoDbm <= 0 || evdoDbm >= 120) ? Integer.MAX_VALUE : -evdoDbm;
        this.mEvdoEcio = (evdoEcio <= 0 || evdoEcio >= 160) ? Integer.MAX_VALUE : -evdoEcio;
        if (evdoSnr > 0 && evdoSnr <= 8) {
            i = evdoSnr;
        }
        this.mEvdoSnr = i;
    }

    public CellSignalStrengthCdma(CellSignalStrengthCdma s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(CellSignalStrengthCdma s) {
        this.mCdmaDbm = s.mCdmaDbm;
        this.mCdmaEcio = s.mCdmaEcio;
        this.mEvdoDbm = s.mEvdoDbm;
        this.mEvdoEcio = s.mEvdoEcio;
        this.mEvdoSnr = s.mEvdoSnr;
    }

    public CellSignalStrengthCdma copy() {
        return new CellSignalStrengthCdma(this);
    }

    public void setDefaultValues() {
        this.mCdmaDbm = Integer.MAX_VALUE;
        this.mCdmaEcio = Integer.MAX_VALUE;
        this.mEvdoDbm = Integer.MAX_VALUE;
        this.mEvdoEcio = Integer.MAX_VALUE;
        this.mEvdoSnr = Integer.MAX_VALUE;
    }

    public int getLevel() {
        int cdmaLevel = getCdmaLevel();
        int evdoLevel = getEvdoLevel();
        if (evdoLevel == 0) {
            return getCdmaLevel();
        }
        if (cdmaLevel == 0) {
            return getEvdoLevel();
        }
        return cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
    }

    public int getAsuLevel() {
        int cdmaAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        int ecioAsuLevel = 99;
        if (cdmaDbm == Integer.MAX_VALUE) {
            cdmaAsuLevel = 99;
        } else if (cdmaDbm >= -75) {
            cdmaAsuLevel = 16;
        } else if (cdmaDbm >= -82) {
            cdmaAsuLevel = 8;
        } else if (cdmaDbm >= -90) {
            cdmaAsuLevel = 4;
        } else if (cdmaDbm >= -95) {
            cdmaAsuLevel = 2;
        } else if (cdmaDbm >= -100) {
            cdmaAsuLevel = 1;
        } else {
            cdmaAsuLevel = 99;
        }
        if (cdmaEcio == Integer.MAX_VALUE) {
            ecioAsuLevel = 99;
        } else if (cdmaEcio >= -90) {
            ecioAsuLevel = 16;
        } else if (cdmaEcio >= -100) {
            ecioAsuLevel = 8;
        } else if (cdmaEcio >= -115) {
            ecioAsuLevel = 4;
        } else if (cdmaEcio >= -130) {
            ecioAsuLevel = 2;
        } else if (cdmaEcio >= -150) {
            ecioAsuLevel = 1;
        }
        return cdmaAsuLevel < ecioAsuLevel ? cdmaAsuLevel : ecioAsuLevel;
    }

    public int getCdmaLevel() {
        int levelDbm;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        int levelEcio = 0;
        if (cdmaDbm == Integer.MAX_VALUE) {
            levelDbm = 0;
        } else if (cdmaDbm >= -75) {
            levelDbm = 4;
        } else if (cdmaDbm >= -85) {
            levelDbm = 3;
        } else if (cdmaDbm >= -95) {
            levelDbm = 2;
        } else if (cdmaDbm >= -100) {
            levelDbm = 1;
        } else {
            levelDbm = 0;
        }
        if (cdmaEcio == Integer.MAX_VALUE) {
            levelEcio = 0;
        } else if (cdmaEcio >= -90) {
            levelEcio = 4;
        } else if (cdmaEcio >= -110) {
            levelEcio = 3;
        } else if (cdmaEcio >= -130) {
            levelEcio = 2;
        } else if (cdmaEcio >= -150) {
            levelEcio = 1;
        }
        return levelDbm < levelEcio ? levelDbm : levelEcio;
    }

    public int getEvdoLevel() {
        int levelEvdoDbm;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        int levelEvdoSnr = 0;
        if (evdoDbm == Integer.MAX_VALUE) {
            levelEvdoDbm = 0;
        } else if (evdoDbm >= -65) {
            levelEvdoDbm = 4;
        } else if (evdoDbm >= -75) {
            levelEvdoDbm = 3;
        } else if (evdoDbm >= -90) {
            levelEvdoDbm = 2;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = 1;
        } else {
            levelEvdoDbm = 0;
        }
        if (evdoSnr == Integer.MAX_VALUE) {
            levelEvdoSnr = 0;
        } else if (evdoSnr >= 7) {
            levelEvdoSnr = 4;
        } else if (evdoSnr >= 5) {
            levelEvdoSnr = 3;
        } else if (evdoSnr >= 3) {
            levelEvdoSnr = 2;
        } else if (evdoSnr >= 1) {
            levelEvdoSnr = 1;
        }
        return levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
    }

    public int getDbm() {
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        return cdmaDbm < evdoDbm ? cdmaDbm : evdoDbm;
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    public void setCdmaDbm(int cdmaDbm) {
        this.mCdmaDbm = cdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    public void setCdmaEcio(int cdmaEcio) {
        this.mCdmaEcio = cdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    public void setEvdoDbm(int evdoDbm) {
        this.mEvdoDbm = evdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    public void setEvdoEcio(int evdoEcio) {
        this.mEvdoEcio = evdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    public void setEvdoSnr(int evdoSnr) {
        this.mEvdoSnr = evdoSnr;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mCdmaDbm), Integer.valueOf(this.mCdmaEcio), Integer.valueOf(this.mEvdoDbm), Integer.valueOf(this.mEvdoEcio), Integer.valueOf(this.mEvdoSnr)});
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            CellSignalStrengthCdma s = (CellSignalStrengthCdma) o;
            if (o == null) {
                return false;
            }
            if (this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "CellSignalStrengthCdma: cdmaDbm=" + this.mCdmaDbm + " cdmaEcio=" + this.mCdmaEcio + " evdoDbm=" + this.mEvdoDbm + " evdoEcio=" + this.mEvdoEcio + " evdoSnr=" + this.mEvdoSnr;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCdmaDbm);
        dest.writeInt(this.mCdmaEcio);
        dest.writeInt(this.mEvdoDbm);
        dest.writeInt(this.mEvdoEcio);
        dest.writeInt(this.mEvdoSnr);
    }

    private CellSignalStrengthCdma(Parcel in) {
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
