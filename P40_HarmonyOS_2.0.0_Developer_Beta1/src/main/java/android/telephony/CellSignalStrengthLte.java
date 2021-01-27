package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.pm.PackageManager;
import android.hardware.radio.V1_0.LteSignalStrength;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import java.util.Objects;

public final class CellSignalStrengthLte extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthLte> CREATOR = new Parcelable.Creator<CellSignalStrengthLte>() {
        /* class android.telephony.CellSignalStrengthLte.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthLte createFromParcel(Parcel in) {
            return new CellSignalStrengthLte(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthLte[] newArray(int size) {
            return new CellSignalStrengthLte[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthLte";
    private static final int MAX_LTE_RSRP = -44;
    private static final int MIN_LTE_RSRP = -140;
    private static final int SIGNAL_STRENGTH_LTE_RSSI_ASU_UNKNOWN = 99;
    private static final int SIGNAL_STRENGTH_LTE_RSSI_VALID_ASU_MAX_VALUE = 31;
    private static final int SIGNAL_STRENGTH_LTE_RSSI_VALID_ASU_MIN_VALUE = 0;
    private static final CellSignalStrengthLte sInvalid = new CellSignalStrengthLte();
    private static final int sRsrpBoost = 0;
    private static final int[] sThresholds = {PackageManager.INSTALL_FAILED_ABORTED, PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, -95, -85};
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mCqi;
    private boolean mIsCdma;
    private int mLevel;
    private int mLevelHw;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mRsrp;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mRsrq;
    private int mRssi;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mRssnr;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mSignalStrength;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mTimingAdvance;

    @UnsupportedAppUsage
    public CellSignalStrengthLte() {
        setDefaultValues();
    }

    public CellSignalStrengthLte(int rssi, int rsrp, int rsrq, int rssnr, int cqi, int timingAdvance) {
        if (FEATURE_VALIDATEINPUT) {
            HwFrameworkFactory.getHwInnerTelephonyManager().validateInput(this, rssi, rsrp, rsrq, rssnr, cqi, timingAdvance);
        } else {
            this.mRssi = inRangeOrUnavailable(rssi, -113, -51);
            this.mSignalStrength = this.mRssi;
            this.mRsrp = inRangeOrUnavailable(rsrp, -140, -43);
            this.mRsrq = inRangeOrUnavailable(rsrq, -20, -3);
            this.mRssnr = inRangeOrUnavailable(rssnr, -200, 300);
            this.mCqi = inRangeOrUnavailable(cqi, 0, 15);
            this.mTimingAdvance = inRangeOrUnavailable(timingAdvance, 0, 1282);
        }
        updateLevel(null, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public CellSignalStrengthLte(LteSignalStrength lte) {
        this(FEATURE_VALIDATEINPUT ? lte.signalStrength : convertRssiAsuToDBm(lte.signalStrength), (!FEATURE_VALIDATEINPUT && lte.rsrp != Integer.MAX_VALUE) ? -lte.rsrp : lte.rsrp, (!FEATURE_VALIDATEINPUT && lte.rsrq != Integer.MAX_VALUE) ? -lte.rsrq : lte.rsrq, lte.rssnr, lte.cqi, lte.timingAdvance);
    }

    public CellSignalStrengthLte(CellSignalStrengthLte s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(CellSignalStrengthLte s) {
        this.mSignalStrength = s.mSignalStrength;
        this.mRssi = s.mRssi;
        this.mRsrp = s.mRsrp;
        this.mRsrq = s.mRsrq;
        this.mRssnr = s.mRssnr;
        this.mCqi = s.mCqi;
        this.mTimingAdvance = s.mTimingAdvance;
        this.mLevel = s.mLevel;
        this.mIsCdma = s.mIsCdma;
        this.mPhoneId = s.mPhoneId;
        this.mLevelHw = s.mLevelHw;
    }

    @Override // android.telephony.CellSignalStrength
    public CellSignalStrengthLte copy() {
        return new CellSignalStrengthLte(this);
    }

    @Override // android.telephony.CellSignalStrength
    public void setDefaultValues() {
        this.mSignalStrength = Integer.MAX_VALUE;
        this.mRssi = Integer.MAX_VALUE;
        this.mRsrp = Integer.MAX_VALUE;
        this.mRsrq = Integer.MAX_VALUE;
        this.mRssnr = Integer.MAX_VALUE;
        this.mCqi = Integer.MAX_VALUE;
        this.mTimingAdvance = Integer.MAX_VALUE;
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
        boolean rsrpOnly;
        int[] thresholds;
        int rsrpIconLevel;
        int rssiIconLevel;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            this.mLevelHw = HwFrameworkFactory.getHwInnerTelephonyManager().getLevelHw(this);
        }
        if (cc == null) {
            thresholds = sThresholds;
            rsrpOnly = false;
        } else {
            rsrpOnly = cc.getBoolean(CarrierConfigManager.KEY_USE_ONLY_RSRP_FOR_LTE_SIGNAL_BAR_BOOL, false);
            thresholds = cc.getIntArray(CarrierConfigManager.KEY_LTE_RSRP_THRESHOLDS_INT_ARRAY);
            if (thresholds == null) {
                thresholds = sThresholds;
            }
        }
        int rsrpBoost = 0;
        if (ss != null) {
            rsrpBoost = ss.getLteEarfcnRsrpBoost();
        }
        int snrIconLevel = -1;
        int rsrp = this.mRsrp + rsrpBoost;
        if (rsrp < -140 || rsrp > -44) {
            rsrpIconLevel = -1;
        } else {
            rsrpIconLevel = thresholds.length;
            while (rsrpIconLevel > 0 && rsrp < thresholds[rsrpIconLevel - 1]) {
                rsrpIconLevel--;
            }
        }
        if (!rsrpOnly || rsrpIconLevel == -1) {
            int i = this.mRssnr;
            if (i > 300) {
                snrIconLevel = -1;
            } else if (i >= 130) {
                snrIconLevel = 4;
            } else if (i >= 45) {
                snrIconLevel = 3;
            } else if (i >= 10) {
                snrIconLevel = 2;
            } else if (i >= -30) {
                snrIconLevel = 1;
            } else if (i >= -200) {
                snrIconLevel = 0;
            }
            if (snrIconLevel != -1 && rsrpIconLevel != -1) {
                this.mLevel = rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel;
            } else if (snrIconLevel != -1) {
                this.mLevel = snrIconLevel;
            } else if (rsrpIconLevel != -1) {
                this.mLevel = rsrpIconLevel;
            } else {
                int i2 = this.mRssi;
                if (i2 > -51) {
                    rssiIconLevel = 0;
                } else if (i2 >= -89) {
                    rssiIconLevel = 4;
                } else if (i2 >= -97) {
                    rssiIconLevel = 3;
                } else if (i2 >= -103) {
                    rssiIconLevel = 2;
                } else if (i2 >= -113) {
                    rssiIconLevel = 1;
                } else {
                    rssiIconLevel = 0;
                }
                this.mLevel = rssiIconLevel;
            }
        } else {
            this.mLevel = rsrpIconLevel;
        }
    }

    public int getRsrq() {
        return this.mRsrq;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getRssnr() {
        return this.mRssnr;
    }

    public int getRsrp() {
        return this.mRsrp;
    }

    public int getCqi() {
        return this.mCqi;
    }

    @Override // android.telephony.CellSignalStrength
    public int getDbm() {
        return this.mRsrp;
    }

    @Override // android.telephony.CellSignalStrength
    public int getAsuLevel() {
        int lteDbm = this.mRsrp;
        if (lteDbm == Integer.MAX_VALUE) {
            return 99;
        }
        if (lteDbm <= -140) {
            return 0;
        }
        if (lteDbm >= -43) {
            return 97;
        }
        return lteDbm + 140;
    }

    public int getTimingAdvance() {
        return this.mTimingAdvance;
    }

    @Override // android.telephony.CellSignalStrength
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mRssi), Integer.valueOf(this.mRsrp), Integer.valueOf(this.mRsrq), Integer.valueOf(this.mRssnr), Integer.valueOf(this.mCqi), Integer.valueOf(this.mTimingAdvance), Integer.valueOf(this.mLevel), Integer.valueOf(this.mLevelHw));
    }

    @Override // android.telephony.CellSignalStrength
    public boolean isValid() {
        return !equals(sInvalid);
    }

    @Override // android.telephony.CellSignalStrength
    public boolean equals(Object o) {
        if (!(o instanceof CellSignalStrengthLte)) {
            return false;
        }
        CellSignalStrengthLte s = (CellSignalStrengthLte) o;
        if (this.mRssi == s.mRssi && this.mRsrp == s.mRsrp && this.mRsrq == s.mRsrq && this.mRssnr == s.mRssnr && this.mCqi == s.mCqi && this.mTimingAdvance == s.mTimingAdvance && this.mLevel == s.mLevel && this.mLevelHw == s.mLevelHw) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "CellSignalStrengthLte: rssi=" + this.mRssi + " rsrp=" + this.mRsrp + " rsrq=" + this.mRsrq + " rssnr=" + this.mRssnr + " cqi=" + this.mCqi + " ta=" + this.mTimingAdvance + " level=" + this.mLevel + " levelHw=" + this.mLevelHw + " mIsCdma=" + this.mIsCdma + " mPhoneId=" + this.mPhoneId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mRsrp);
        dest.writeInt(this.mRsrq);
        dest.writeInt(this.mRssnr);
        dest.writeInt(this.mCqi);
        dest.writeInt(this.mTimingAdvance);
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mLevelHw);
        dest.writeInt(this.mPhoneId);
        dest.writeBoolean(this.mIsCdma);
    }

    private CellSignalStrengthLte(Parcel in) {
        this.mRssi = in.readInt();
        this.mSignalStrength = this.mRssi;
        this.mRsrp = in.readInt();
        this.mRsrq = in.readInt();
        this.mRssnr = in.readInt();
        this.mCqi = in.readInt();
        this.mTimingAdvance = in.readInt();
        this.mLevel = in.readInt();
        this.mLevelHw = in.readInt();
        this.mPhoneId = in.readInt();
        this.mIsCdma = in.readBoolean();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    private static int convertRssiAsuToDBm(int rssiAsu) {
        if (rssiAsu == 99) {
            return Integer.MAX_VALUE;
        }
        if (rssiAsu >= 0 && rssiAsu <= 31) {
            return (rssiAsu * 2) - 113;
        }
        Rlog.e(LOG_TAG, "convertRssiAsuToDBm: invalid RSSI in ASU=" + rssiAsu);
        return Integer.MAX_VALUE;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public void setRsrp(int rsrp) {
        this.mRsrp = rsrp;
    }

    public void setRsrq(int rsrq) {
        this.mRsrq = rsrq;
    }

    public void setRssnr(int rssnr) {
        this.mRssnr = rssnr;
    }

    public boolean isCdma() {
        return this.mIsCdma;
    }

    public void setCdma(boolean isCdma) {
        this.mIsCdma = isCdma;
    }

    public void setCqi(int cqi) {
        this.mCqi = cqi;
    }

    public void setTimingAdvance(int timingAdvance) {
        this.mTimingAdvance = timingAdvance;
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevelHw() {
        return IS_USING_HW_DESIGN ? this.mLevelHw : this.mLevel;
    }
}
