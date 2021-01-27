package android.telephony;

import android.common.HwFrameworkFactory;
import android.content.pm.PackageManager;
import android.hardware.radio.V1_0.CdmaSignalStrength;
import android.hardware.radio.V1_0.EvdoSignalStrength;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import java.util.Objects;

public final class CellSignalStrengthCdma extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthCdma> CREATOR = new Parcelable.Creator<CellSignalStrengthCdma>() {
        /* class android.telephony.CellSignalStrengthCdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthCdma createFromParcel(Parcel in) {
            return new CellSignalStrengthCdma(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthCdma[] newArray(int size) {
            return new CellSignalStrengthCdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthCdma";
    private static final CellSignalStrengthCdma sInvalid = new CellSignalStrengthCdma();
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;
    private int mLevel;
    private int mLevelHw;

    public CellSignalStrengthCdma() {
        setDefaultValues();
    }

    public CellSignalStrengthCdma(int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr) {
        fillDataForConstructer(cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
    }

    public CellSignalStrengthCdma(CdmaSignalStrength cdma, EvdoSignalStrength evdo) {
        fillDataForConstructer(FEATURE_VALIDATEINPUT ? cdma.dbm : takeOppositeExceptInvaildValue(cdma.dbm), FEATURE_VALIDATEINPUT ? cdma.ecio : takeOppositeExceptInvaildValue(cdma.ecio), FEATURE_VALIDATEINPUT ? evdo.dbm : takeOppositeExceptInvaildValue(evdo.dbm), FEATURE_VALIDATEINPUT ? evdo.ecio : takeOppositeExceptInvaildValue(evdo.ecio), evdo.signalNoiseRatio);
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
        this.mLevel = s.mLevel;
        this.mPhoneId = s.mPhoneId;
        this.mLevelHw = s.mLevelHw;
    }

    @Override // android.telephony.CellSignalStrength
    public CellSignalStrengthCdma copy() {
        return new CellSignalStrengthCdma(this);
    }

    @Override // android.telephony.CellSignalStrength
    public void setDefaultValues() {
        this.mCdmaDbm = Integer.MAX_VALUE;
        this.mCdmaEcio = Integer.MAX_VALUE;
        this.mEvdoDbm = Integer.MAX_VALUE;
        this.mEvdoEcio = Integer.MAX_VALUE;
        this.mEvdoSnr = Integer.MAX_VALUE;
        this.mLevel = 0;
        this.mPhoneId = -1;
        this.mLevelHw = 0;
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevel() {
        return this.mLevel;
    }

    @Override // android.telephony.CellSignalStrength
    public void updateLevel(PersistableBundle cc, ServiceState ss) {
        int cdmaLevel = getCdmaLevel();
        int evdoLevel = getEvdoLevel();
        if (evdoLevel == 0) {
            this.mLevel = getCdmaLevel();
        } else if (cdmaLevel == 0) {
            this.mLevel = getEvdoLevel();
        } else {
            this.mLevel = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
        }
        updateLevelHw(cc, ss);
    }

    @Override // android.telephony.CellSignalStrength
    public int getAsuLevel() {
        int cdmaAsuLevel;
        int ecioAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
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
        } else {
            ecioAsuLevel = 99;
        }
        return cdmaAsuLevel < ecioAsuLevel ? cdmaAsuLevel : ecioAsuLevel;
    }

    public int getCdmaLevel() {
        int levelDbm;
        int levelEcio;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
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
        } else {
            levelEcio = 0;
        }
        return levelDbm < levelEcio ? levelDbm : levelEcio;
    }

    public int getEvdoLevel() {
        int levelEvdoDbm;
        int levelEvdoSnr;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
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
        } else {
            levelEvdoSnr = 0;
        }
        return levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
    }

    public int getEvdoAsuLevel() {
        int levelEvdoDbm;
        int levelEvdoSnr;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -65) {
            levelEvdoDbm = 16;
        } else if (evdoDbm >= -75) {
            levelEvdoDbm = 8;
        } else if (evdoDbm >= -85) {
            levelEvdoDbm = 4;
        } else if (evdoDbm >= -95) {
            levelEvdoDbm = 2;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = 1;
        } else {
            levelEvdoDbm = 99;
        }
        if (evdoSnr >= 7) {
            levelEvdoSnr = 16;
        } else if (evdoSnr >= 6) {
            levelEvdoSnr = 8;
        } else if (evdoSnr >= 5) {
            levelEvdoSnr = 4;
        } else if (evdoSnr >= 3) {
            levelEvdoSnr = 2;
        } else if (evdoSnr >= 1) {
            levelEvdoSnr = 1;
        } else {
            levelEvdoSnr = 99;
        }
        return levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
    }

    @Override // android.telephony.CellSignalStrength
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

    @Override // android.telephony.CellSignalStrength
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mCdmaDbm), Integer.valueOf(this.mCdmaEcio), Integer.valueOf(this.mEvdoDbm), Integer.valueOf(this.mEvdoEcio), Integer.valueOf(this.mEvdoSnr), Integer.valueOf(this.mLevel), Integer.valueOf(this.mLevelHw));
    }

    @Override // android.telephony.CellSignalStrength
    public boolean isValid() {
        return !equals(sInvalid);
    }

    @Override // android.telephony.CellSignalStrength
    public boolean equals(Object o) {
        if (!(o instanceof CellSignalStrengthCdma)) {
            return false;
        }
        CellSignalStrengthCdma s = (CellSignalStrengthCdma) o;
        if (this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLevel == s.mLevel && this.mLevelHw == s.mLevelHw) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "CellSignalStrengthCdma: cdmaDbm=" + this.mCdmaDbm + " cdmaEcio=" + this.mCdmaEcio + " evdoDbm=" + this.mEvdoDbm + " evdoEcio=" + this.mEvdoEcio + " evdoSnr=" + this.mEvdoSnr + " level=" + this.mLevel + " mLevelHw=" + this.mLevelHw + " phoneId=" + this.mPhoneId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCdmaDbm);
        dest.writeInt(this.mCdmaEcio);
        dest.writeInt(this.mEvdoDbm);
        dest.writeInt(this.mEvdoEcio);
        dest.writeInt(this.mEvdoSnr);
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mLevelHw);
        dest.writeInt(this.mPhoneId);
    }

    private CellSignalStrengthCdma(Parcel in) {
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
        this.mLevel = in.readInt();
        this.mLevelHw = in.readInt();
        this.mPhoneId = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    private int takeOppositeExceptInvaildValue(int originValue) {
        if (originValue == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return -originValue;
    }

    private void fillDataForConstructer(int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr) {
        if (FEATURE_VALIDATEINPUT) {
            HwFrameworkFactory.getHwInnerTelephonyManager().validateInput(this, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
        } else {
            this.mCdmaDbm = inRangeOrUnavailable(cdmaDbm, PackageManager.INSTALL_FAILED_MULTIPACKAGE_INCONSISTENCY, 0);
            this.mCdmaEcio = inRangeOrUnavailable(cdmaEcio, -160, 0);
            this.mEvdoDbm = inRangeOrUnavailable(evdoDbm, PackageManager.INSTALL_FAILED_MULTIPACKAGE_INCONSISTENCY, 0);
            this.mEvdoEcio = inRangeOrUnavailable(evdoEcio, -160, 0);
            this.mEvdoSnr = inRangeOrUnavailable(evdoSnr, 0, 8);
        }
        updateLevel(null, null);
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevelHw() {
        return this.mLevelHw;
    }

    private void updateLevelHw(PersistableBundle cc, ServiceState ss) {
        int cdmaLevel = getCdmaLevelHw();
        int evdoLevel = getEvdoLevelHw();
        if (evdoLevel == 0) {
            this.mLevelHw = getCdmaLevelHw();
        } else if (cdmaLevel == 0) {
            this.mLevelHw = getEvdoLevelHw();
        } else {
            this.mLevelHw = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
        }
    }

    public int getCdmaLevelHw() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getCdmaLevel(this);
        }
        return getCdmaLevel();
    }

    public int getEvdoLevelHw() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getEvdoLevel(this);
        }
        return getEvdoLevel();
    }
}
