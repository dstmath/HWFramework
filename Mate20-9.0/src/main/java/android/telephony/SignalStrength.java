package android.telephony;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.Log;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SignalStrength implements Parcelable {
    public static final Parcelable.Creator<SignalStrength> CREATOR = new Parcelable.Creator() {
        public SignalStrength createFromParcel(Parcel in) {
            return new SignalStrength(in);
        }

        public SignalStrength[] newArray(int size) {
            return new SignalStrength[size];
        }
    };
    private static final boolean DBG = false;
    private static final boolean FEATURE_VALIDATEINPUT = SystemProperties.getBoolean("ro.SignalStrength.ValidateInput", false);
    public static final int INVALID = Integer.MAX_VALUE;
    public static final int INVALID_GSM_SS = 99;
    public static final int INVALID_PHONEID = -1;
    public static final int INVALID_WCDMA_DBM1 = 0;
    public static final int INVALID_WCDMA_DBM2 = -1;
    public static final int INVALID_WCDMA_RSCP = -1;
    private static final String LOG_TAG = "SignalStrength";
    private static final int LTE_RSRP_THRESHOLDS_NUM = 4;
    private static final int MAX_LTE_RSRP = -44;
    private static final int MAX_WCDMA_RSCP = -24;
    private static final String MEASUMENT_TYPE_RSCP = "rscp";
    private static final int MIN_LTE_RSRP = -140;
    private static final int MIN_WCDMA_RSCP = -120;
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = {"none", "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    private static final int WCDMA_RSCP_THRESHOLDS_NUM = 4;
    private static HwCustSignalStrength mHwCustSignalStrength = ((HwCustSignalStrength) HwCustUtils.createObj(HwCustSignalStrength.class, new Object[0]));
    private boolean isCdma;
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;
    private int mGsmBitErrorRate;
    private int mGsmSignalStrength;
    private boolean mIsGsm;
    private int mLteCqi;
    private int mLteRsrp;
    private int mLteRsrpBoost;
    private int[] mLteRsrpThresholds;
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    private int mNrCqi;
    private int mNrRsrp;
    private int mNrRsrq;
    private int mNrRssnr;
    private int mNrSignalStrength;
    private int mPhoneId;
    private int mTdScdmaRscp;
    private boolean mUseOnlyRsrpForLteLevel;
    private String mWcdmaDefaultSignalMeasurement;
    private int mWcdmaEcio;
    private int mWcdmaRscp;
    private int mWcdmaRscpAsu;
    private int[] mWcdmaRscpThresholds;
    private int mWcdmaSignalStrength;

    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public SignalStrength() {
        this(true);
    }

    public SignalStrength(boolean gsmFlag) {
        this.mLteRsrpThresholds = new int[4];
        this.mWcdmaRscpThresholds = new int[4];
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.mWcdmaSignalStrength = 99;
        this.mWcdmaRscp = Integer.MAX_VALUE;
        this.mWcdmaEcio = Integer.MAX_VALUE;
        this.mWcdmaRscpAsu = 255;
        boolean z = false;
        this.mLteRsrpBoost = 0;
        this.mIsGsm = gsmFlag;
        this.mUseOnlyRsrpForLteLevel = false;
        this.mWcdmaDefaultSignalMeasurement = MEASUMENT_TYPE_RSCP;
        setLteRsrpThresholds(getDefaultLteRsrpThresholds());
        setWcdmaRscpThresholds(getDefaultWcdmaRscpThresholds());
        this.isCdma = !this.mIsGsm ? true : z;
        this.mPhoneId = -1;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, int wcdmaSignalStrength, int wcdmaRscp, int wcdmaEcio, int lteRsrpBoost, boolean gsmFlag, boolean lteLevelBaseOnRsrp, String wcdmaDefaultMeasurement) {
        this(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, tdScdmaRscp, wcdmaSignalStrength, wcdmaRscp, wcdmaEcio, lteRsrpBoost, gsmFlag, lteLevelBaseOnRsrp, wcdmaDefaultMeasurement, -1);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, int wcdmaSignalStrength, int wcdmaRscp, int wcdmaEcio, int lteRsrpBoost, boolean gsmFlag, boolean lteLevelBaseOnRsrp, String wcdmaDefaultMeasurement, int phoneId) {
        this.mLteRsrpThresholds = new int[4];
        this.mWcdmaRscpThresholds = new int[4];
        this.mGsmSignalStrength = gsmSignalStrength;
        this.mGsmBitErrorRate = gsmBitErrorRate;
        this.mCdmaDbm = cdmaDbm;
        this.mCdmaEcio = cdmaEcio;
        this.mEvdoDbm = evdoDbm;
        this.mEvdoEcio = evdoEcio;
        this.mEvdoSnr = evdoSnr;
        this.mLteSignalStrength = lteSignalStrength;
        this.mLteRsrp = lteRsrp;
        this.mLteRsrq = lteRsrq;
        this.mLteRssnr = lteRssnr;
        this.mLteCqi = lteCqi;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.mWcdmaSignalStrength = wcdmaSignalStrength;
        this.mWcdmaRscpAsu = this.mWcdmaRscp + 120;
        this.mWcdmaRscp = wcdmaRscp;
        this.mWcdmaEcio = wcdmaEcio;
        this.mLteRsrpBoost = lteRsrpBoost;
        this.mIsGsm = gsmFlag;
        this.mUseOnlyRsrpForLteLevel = lteLevelBaseOnRsrp;
        this.mWcdmaDefaultSignalMeasurement = wcdmaDefaultMeasurement;
        setLteRsrpThresholds(getDefaultLteRsrpThresholds());
        setWcdmaRscpThresholds(getDefaultWcdmaRscpThresholds());
        this.isCdma = this.mIsGsm ? false : true;
        this.mPhoneId = phoneId;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp) {
        this(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, tdScdmaRscp, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, true, false, MEASUMENT_TYPE_RSCP, -1);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, int wcdmaSignalStrength, int wcdmaRscp, int wcdmaEcio) {
        this(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, tdScdmaRscp, wcdmaSignalStrength, wcdmaRscp, wcdmaEcio, 0, true, false, MEASUMENT_TYPE_RSCP, -1);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, int phoneId) {
        this(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, tdScdmaRscp, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, true, false, MEASUMENT_TYPE_RSCP, phoneId);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, int wcdmaSignalStrength, int wcdmaRscp, int wcdmaEcio, int phoneId) {
        this(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, tdScdmaRscp, wcdmaSignalStrength, wcdmaRscp, wcdmaEcio, 0, true, false, MEASUMENT_TYPE_RSCP, phoneId);
    }

    public SignalStrength(SignalStrength s) {
        this.mLteRsrpThresholds = new int[4];
        this.mWcdmaRscpThresholds = new int[4];
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(SignalStrength s) {
        this.mGsmSignalStrength = s.mGsmSignalStrength;
        this.mGsmBitErrorRate = s.mGsmBitErrorRate;
        this.mCdmaDbm = s.mCdmaDbm;
        this.mCdmaEcio = s.mCdmaEcio;
        this.mEvdoDbm = s.mEvdoDbm;
        this.mEvdoEcio = s.mEvdoEcio;
        this.mEvdoSnr = s.mEvdoSnr;
        this.mLteSignalStrength = s.mLteSignalStrength;
        this.mLteRsrp = s.mLteRsrp;
        this.mLteRsrq = s.mLteRsrq;
        this.mLteRssnr = s.mLteRssnr;
        this.mLteCqi = s.mLteCqi;
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.mWcdmaSignalStrength = s.mWcdmaSignalStrength;
        this.mWcdmaRscpAsu = s.mWcdmaRscpAsu;
        this.mWcdmaRscp = s.mWcdmaRscp;
        this.mWcdmaEcio = s.mWcdmaEcio;
        this.mLteRsrpBoost = s.mLteRsrpBoost;
        this.mIsGsm = s.mIsGsm;
        this.mUseOnlyRsrpForLteLevel = s.mUseOnlyRsrpForLteLevel;
        this.mWcdmaDefaultSignalMeasurement = s.mWcdmaDefaultSignalMeasurement;
        setLteRsrpThresholds(s.mLteRsrpThresholds);
        setWcdmaRscpThresholds(s.mWcdmaRscpThresholds);
        this.isCdma = s.isCdma;
        this.mPhoneId = s.mPhoneId;
        this.mNrSignalStrength = s.mNrSignalStrength;
        this.mNrRsrp = s.mNrRsrp;
        this.mNrRsrq = s.mNrRsrq;
        this.mNrRssnr = s.mNrRssnr;
        this.mNrCqi = s.mNrCqi;
    }

    public SignalStrength(Parcel in) {
        this.mLteRsrpThresholds = new int[4];
        this.mWcdmaRscpThresholds = new int[4];
        this.mGsmSignalStrength = in.readInt();
        this.mGsmBitErrorRate = in.readInt();
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
        this.mLteSignalStrength = in.readInt();
        this.mLteRsrp = in.readInt();
        this.mLteRsrq = in.readInt();
        this.mLteRssnr = in.readInt();
        this.mLteCqi = in.readInt();
        this.mTdScdmaRscp = in.readInt();
        this.mWcdmaSignalStrength = in.readInt();
        this.mWcdmaRscpAsu = in.readInt();
        this.mWcdmaRscp = in.readInt();
        this.mWcdmaEcio = in.readInt();
        this.mLteRsrpBoost = in.readInt();
        this.mIsGsm = in.readBoolean();
        this.mUseOnlyRsrpForLteLevel = in.readBoolean();
        this.mWcdmaDefaultSignalMeasurement = in.readString();
        in.readIntArray(this.mLteRsrpThresholds);
        in.readIntArray(this.mWcdmaRscpThresholds);
        this.isCdma = in.readInt() != 0;
        this.mPhoneId = in.readInt();
        this.mNrSignalStrength = in.readInt();
        this.mNrRsrp = in.readInt();
        this.mNrRsrq = in.readInt();
        this.mNrRssnr = in.readInt();
        this.mNrCqi = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mGsmSignalStrength);
        out.writeInt(this.mGsmBitErrorRate);
        out.writeInt(this.mCdmaDbm);
        out.writeInt(this.mCdmaEcio);
        out.writeInt(this.mEvdoDbm);
        out.writeInt(this.mEvdoEcio);
        out.writeInt(this.mEvdoSnr);
        out.writeInt(this.mLteSignalStrength);
        out.writeInt(this.mLteRsrp);
        out.writeInt(this.mLteRsrq);
        out.writeInt(this.mLteRssnr);
        out.writeInt(this.mLteCqi);
        out.writeInt(this.mTdScdmaRscp);
        out.writeInt(this.mWcdmaSignalStrength);
        out.writeInt(this.mWcdmaRscpAsu);
        out.writeInt(this.mWcdmaRscp);
        out.writeInt(this.mWcdmaEcio);
        out.writeInt(this.mLteRsrpBoost);
        out.writeBoolean(this.mIsGsm);
        out.writeBoolean(this.mUseOnlyRsrpForLteLevel);
        out.writeString(this.mWcdmaDefaultSignalMeasurement);
        out.writeIntArray(this.mLteRsrpThresholds);
        out.writeIntArray(this.mWcdmaRscpThresholds);
        out.writeInt(this.isCdma ? 1 : 0);
        out.writeInt(this.mPhoneId);
        out.writeInt(this.mNrSignalStrength);
        out.writeInt(this.mNrRsrp);
        out.writeInt(this.mNrRsrq);
        out.writeInt(this.mNrRssnr);
        out.writeInt(this.mNrCqi);
    }

    public int describeContents() {
        return 0;
    }

    public void validateInput() {
        if (FEATURE_VALIDATEINPUT) {
            HwFrameworkFactory.getHwInnerTelephonyManager().validateInput(this);
        }
    }

    public void fixType() {
        this.mIsGsm = getCdmaRelatedSignalStrength() == 0;
    }

    public void setGsm(boolean gsmFlag) {
        this.mIsGsm = gsmFlag;
    }

    public void setUseOnlyRsrpForLteLevel(boolean useOnlyRsrpForLteLevel) {
        this.mUseOnlyRsrpForLteLevel = useOnlyRsrpForLteLevel;
    }

    public void setWcdmaDefaultSignalMeasurement(String defaultMeasurement) {
        this.mWcdmaDefaultSignalMeasurement = defaultMeasurement;
    }

    public void setLteRsrpBoost(int lteRsrpBoost) {
        this.mLteRsrpBoost = lteRsrpBoost;
    }

    public void setLteRsrpThresholds(int[] lteRsrpThresholds) {
        if (lteRsrpThresholds == null || lteRsrpThresholds.length != 4) {
            Log.wtf(LOG_TAG, "setLteRsrpThresholds - lteRsrpThresholds is invalid.");
        } else {
            System.arraycopy(lteRsrpThresholds, 0, this.mLteRsrpThresholds, 0, 4);
        }
    }

    public int getGsmSignalStrength() {
        if (mHwCustSignalStrength != null) {
            return mHwCustSignalStrength.getGsmSignalStrength(this.mGsmSignalStrength);
        }
        return this.mGsmSignalStrength;
    }

    public int getGsmBitErrorRate() {
        return this.mGsmBitErrorRate;
    }

    public void setWcdmaRscpThresholds(int[] wcdmaRscpThresholds) {
        if (wcdmaRscpThresholds == null || wcdmaRscpThresholds.length != 4) {
            Log.wtf(LOG_TAG, "setWcdmaRscpThresholds - wcdmaRscpThresholds is invalid.");
        } else {
            System.arraycopy(wcdmaRscpThresholds, 0, this.mWcdmaRscpThresholds, 0, 4);
        }
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    public int getLteSignalStrength() {
        return this.mLteSignalStrength;
    }

    public int getLteRsrp() {
        return this.mLteRsrp;
    }

    public int getLteRsrq() {
        return this.mLteRsrq;
    }

    public int getLteRssnr() {
        return this.mLteRssnr;
    }

    public int getLteCqi() {
        return this.mLteCqi;
    }

    public int getLteRsrpBoost() {
        return this.mLteRsrpBoost;
    }

    public int getLevel() {
        return this.mIsGsm ? getGsmRelatedSignalStrength() : getCdmaRelatedSignalStrength();
    }

    public int getAsuLevel() {
        if (!this.mIsGsm) {
            int cdmaAsuLevel = getCdmaAsuLevel();
            int evdoAsuLevel = getEvdoAsuLevel();
            if (evdoAsuLevel == 0) {
                return cdmaAsuLevel;
            }
            if (cdmaAsuLevel == 0) {
                return evdoAsuLevel;
            }
            return cdmaAsuLevel < evdoAsuLevel ? cdmaAsuLevel : evdoAsuLevel;
        } else if (this.mLteRsrp != Integer.MAX_VALUE) {
            return getLteAsuLevel();
        } else {
            if (this.mTdScdmaRscp != Integer.MAX_VALUE) {
                return getTdScdmaAsuLevel();
            }
            if (this.mWcdmaRscp == Integer.MAX_VALUE || this.mWcdmaRscp == -1) {
                return getGsmAsuLevel();
            }
            return getWcdmaAsuLevel();
        }
    }

    public int getDbm() {
        if (isGsm()) {
            int dBm = getLteDbm();
            if (dBm == Integer.MAX_VALUE || dBm == 0) {
                if (getTdScdmaLevel() != 0) {
                    dBm = getTdScdmaDbm();
                } else if (getWcdmaDbm() == Integer.MAX_VALUE || getWcdmaDbm() == -1 || dBm == 0) {
                    dBm = getGsmDbm();
                } else {
                    dBm = getWcdmaDbm();
                }
            }
            return dBm;
        }
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        return (evdoDbm != MIN_WCDMA_RSCP && (cdmaDbm == MIN_WCDMA_RSCP || cdmaDbm >= evdoDbm)) ? evdoDbm : cdmaDbm;
    }

    public int getGsmDbm() {
        int gsmSignalStrength = this.mGsmSignalStrength;
        int wcdmaDbm = getWcdmaRscp();
        if (-1 != wcdmaDbm && wcdmaDbm != 0 && Integer.MAX_VALUE != wcdmaDbm) {
            return wcdmaDbm;
        }
        if (gsmSignalStrength == 99) {
            return -1;
        }
        return gsmSignalStrength;
    }

    public int getGsmLevel() {
        int level;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmLevel(this);
        }
        int asu = getGsmSignalStrength();
        if (asu <= 2 || asu == 99) {
            level = 0;
        } else if (asu >= 12) {
            level = 4;
        } else if (asu >= 8) {
            level = 3;
        } else if (asu >= 5) {
            level = 2;
        } else {
            level = 1;
        }
        return level;
    }

    public int getGsmAsuLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmAsuLevel(this);
        }
        int gsmSignalStrength = this.mGsmSignalStrength;
        int asu = -1;
        int dbm = gsmSignalStrength == 0 ? -1 : gsmSignalStrength;
        log("gsmSignalStrength=" + gsmSignalStrength + ", mWcdmaRscp" + this.mWcdmaRscp);
        if (dbm != -1 && this.mWcdmaRscp == 0) {
            asu = (113 + dbm) / 2;
        }
        return asu;
    }

    public int getCdmaLevel() {
        int levelDbm;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getCdmaLevel(this);
        }
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        int levelEcio = 0;
        if (cdmaDbm >= -75) {
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
        if (cdmaEcio >= -90) {
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

    public int getCdmaAsuLevel() {
        int cdmaAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        int ecioAsuLevel = 99;
        if (cdmaDbm == -1) {
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
        if (cdmaEcio >= -90) {
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

    public int getEvdoLevel() {
        int levelEvdoDbm;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getEvdoLevel(this);
        }
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        int levelEvdoSnr = 0;
        if (evdoDbm >= -65) {
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
        if (evdoSnr >= 7) {
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

    public int getEvdoAsuLevel() {
        int levelEvdoDbm;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        int levelEvdoSnr = 99;
        if (evdoDbm == -1) {
            levelEvdoDbm = 99;
        } else if (evdoDbm >= -65) {
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
        }
        return levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
    }

    public int getLteDbm() {
        return this.mLteRsrp;
    }

    public int getLteLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getLteLevel(this);
        }
        int rssiIconLevel = 0;
        int rsrpIconLevel = -1;
        int snrIconLevel = -1;
        if (this.mLteRsrp <= -44 && this.mLteRsrp >= MIN_LTE_RSRP) {
            rsrpIconLevel = this.mLteRsrp >= this.mLteRsrpThresholds[3] - this.mLteRsrpBoost ? 4 : this.mLteRsrp >= this.mLteRsrpThresholds[2] - this.mLteRsrpBoost ? 3 : this.mLteRsrp >= this.mLteRsrpThresholds[1] - this.mLteRsrpBoost ? 2 : this.mLteRsrp >= this.mLteRsrpThresholds[0] - this.mLteRsrpBoost ? 1 : 0;
        } else if (this.mLteRsrp != Integer.MAX_VALUE) {
            Log.wtf(LOG_TAG, "getLteLevel - invalid lte rsrp: mLteRsrp=" + this.mLteRsrp);
        }
        if (useOnlyRsrpForLteLevel()) {
            log("getLTELevel - rsrp = " + rsrpIconLevel);
            if (rsrpIconLevel != -1) {
                return rsrpIconLevel;
            }
        }
        if (this.mLteRssnr > 300) {
            snrIconLevel = -1;
        } else if (this.mLteRssnr >= 130) {
            snrIconLevel = 4;
        } else if (this.mLteRssnr >= 45) {
            snrIconLevel = 3;
        } else if (this.mLteRssnr >= 10) {
            snrIconLevel = 2;
        } else if (this.mLteRssnr >= -30) {
            snrIconLevel = 1;
        } else if (this.mLteRssnr >= -200) {
            snrIconLevel = 0;
        }
        if (snrIconLevel != -1 && rsrpIconLevel != -1) {
            return rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel;
        } else if (snrIconLevel != -1) {
            return snrIconLevel;
        } else {
            if (rsrpIconLevel != -1) {
                return rsrpIconLevel;
            }
            if (this.mLteSignalStrength > 63) {
                rssiIconLevel = 0;
            } else if (this.mLteSignalStrength >= 12) {
                rssiIconLevel = 4;
            } else if (this.mLteSignalStrength >= 8) {
                rssiIconLevel = 3;
            } else if (this.mLteSignalStrength >= 5) {
                rssiIconLevel = 2;
            } else if (this.mLteSignalStrength >= 0) {
                rssiIconLevel = 1;
            }
            return rssiIconLevel;
        }
    }

    public int getLteAsuLevel() {
        int lteDbm = getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return lteDbm + 140;
    }

    public int getWcdmaEcio() {
        return this.mWcdmaEcio;
    }

    public void setGsmSignalStrength(int gsmSignalStrength) {
        this.mGsmSignalStrength = gsmSignalStrength;
    }

    public void setWcdmaRscp(int wcdmaRscp) {
        this.mWcdmaRscp = wcdmaRscp;
    }

    public void setWcdmaEcio(int wcdmaEcio) {
        this.mWcdmaEcio = wcdmaEcio;
    }

    public void setLteRsrp(int lteRsrp) {
        this.mLteRsrp = lteRsrp;
    }

    public void setLteRsrq(int lteRsrq) {
        this.mLteRsrq = lteRsrq;
    }

    public void setLteSignalStrength(int lteSignalStrength) {
        this.mLteSignalStrength = lteSignalStrength;
    }

    public void setLteRssnr(int lteRssnr) {
        this.mLteRssnr = lteRssnr;
    }

    public void setCdmaDbm(int cdmaDbm) {
        this.mCdmaDbm = cdmaDbm;
    }

    public void setCdmaEcio(int cdmaEcio) {
        this.mCdmaEcio = cdmaEcio;
    }

    public void setEvdoDbm(int evdoDbm) {
        this.mEvdoDbm = evdoDbm;
    }

    public void setEvdoEcio(int evdoEcio) {
        this.mEvdoEcio = evdoEcio;
    }

    public void setEvdoSnr(int evdoSnr) {
        this.mEvdoSnr = evdoSnr;
    }

    public void setCdma(boolean cdmaFlag) {
        this.isCdma = cdmaFlag;
    }

    public boolean isCdma() {
        return this.isCdma;
    }

    public boolean isGsm() {
        return this.mIsGsm;
    }

    public boolean useOnlyRsrpForLteLevel() {
        return this.mUseOnlyRsrpForLteLevel;
    }

    public int getTdScdmaDbm() {
        return this.mTdScdmaRscp;
    }

    public int getTdScdmaLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm > -25 || tdScdmaDbm == Integer.MAX_VALUE) {
            return 0;
        }
        if (tdScdmaDbm >= -49) {
            return 4;
        }
        if (tdScdmaDbm >= -73) {
            return 3;
        }
        if (tdScdmaDbm >= -97) {
            return 2;
        }
        if (tdScdmaDbm >= -110) {
            return 1;
        }
        return 0;
    }

    public int getTdScdmaAsuLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return tdScdmaDbm + 120;
    }

    public int getWcdmaRscp() {
        return this.mWcdmaRscp;
    }

    public int getWcdmaAsuLevel() {
        int wcdmaDbm = getWcdmaDbm();
        if (wcdmaDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return wcdmaDbm + 120;
    }

    public int getWcdmaDbm() {
        return this.mWcdmaRscp;
    }

    public int getWcdmaLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmLevel(this);
        }
        int level = 0;
        if (this.mWcdmaDefaultSignalMeasurement == null) {
            Log.wtf(LOG_TAG, "getWcdmaLevel - WCDMA default signal measurement is invalid.");
            return 0;
        }
        String str = this.mWcdmaDefaultSignalMeasurement;
        char c = 65535;
        if (str.hashCode() == 3509870 && str.equals(MEASUMENT_TYPE_RSCP)) {
            c = 0;
        }
        if (c != 0) {
            if (this.mWcdmaSignalStrength < 0 || this.mWcdmaSignalStrength > 31) {
                if (this.mWcdmaSignalStrength != 99) {
                    Log.wtf(LOG_TAG, "getWcdmaLevel - invalid WCDMA RSSI: mWcdmaSignalStrength=" + this.mWcdmaSignalStrength);
                }
            } else if (this.mWcdmaSignalStrength >= 18) {
                level = 4;
            } else if (this.mWcdmaSignalStrength >= 13) {
                level = 3;
            } else if (this.mWcdmaSignalStrength >= 8) {
                level = 2;
            } else if (this.mWcdmaSignalStrength >= 3) {
                level = 1;
            }
        } else if (this.mWcdmaRscp < MIN_WCDMA_RSCP || this.mWcdmaRscp > -24) {
            if (this.mWcdmaRscp != Integer.MAX_VALUE) {
                Log.wtf(LOG_TAG, "getWcdmaLevel - invalid WCDMA RSCP: mWcdmaRscp=" + this.mWcdmaRscp);
            }
        } else if (this.mWcdmaRscp >= this.mWcdmaRscpThresholds[3]) {
            level = 4;
        } else if (this.mWcdmaRscp >= this.mWcdmaRscpThresholds[2]) {
            level = 3;
        } else if (this.mWcdmaRscp >= this.mWcdmaRscpThresholds[1]) {
            level = 2;
        } else if (this.mWcdmaRscp >= this.mWcdmaRscpThresholds[0]) {
            level = 1;
        }
        return level;
    }

    public int hashCode() {
        return (this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31) + (this.mCdmaDbm * 31) + (this.mCdmaEcio * 31) + (this.mEvdoDbm * 31) + (this.mEvdoEcio * 31) + (this.mEvdoSnr * 31) + (this.mLteSignalStrength * 31) + (this.mLteRsrp * 31) + (this.mLteRsrq * 31) + (this.mLteRssnr * 31) + (this.mLteCqi * 31) + (this.mLteRsrpBoost * 31) + (this.mTdScdmaRscp * 31) + (this.mWcdmaSignalStrength * 31) + (this.mWcdmaRscpAsu * 31) + (this.mWcdmaRscp * 31) + (this.mWcdmaEcio * 31) + (this.mIsGsm ? 1 : 0) + (this.mUseOnlyRsrpForLteLevel ? 1 : 0) + Objects.hashCode(this.mWcdmaDefaultSignalMeasurement) + Arrays.hashCode(this.mLteRsrpThresholds) + Arrays.hashCode(this.mWcdmaRscpThresholds) + (this.mNrSignalStrength * 31) + (this.mNrRsrp * 31) + (this.mNrRsrq * 31) + (this.mNrRssnr * 31) + (this.mNrCqi * 31);
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return false;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mLteRsrpBoost == s.mLteRsrpBoost && this.mTdScdmaRscp == s.mTdScdmaRscp && this.mWcdmaSignalStrength == s.mWcdmaSignalStrength && this.mWcdmaRscpAsu == s.mWcdmaRscpAsu && this.mWcdmaRscp == s.mWcdmaRscp && this.mWcdmaEcio == s.mWcdmaEcio && this.mIsGsm == s.mIsGsm && this.mUseOnlyRsrpForLteLevel == s.mUseOnlyRsrpForLteLevel && Objects.equals(this.mWcdmaDefaultSignalMeasurement, s.mWcdmaDefaultSignalMeasurement) && Arrays.equals(this.mLteRsrpThresholds, s.mLteRsrpThresholds) && Arrays.equals(this.mWcdmaRscpThresholds, s.mWcdmaRscpThresholds) && this.mNrSignalStrength == s.mNrSignalStrength && this.mNrRsrp == s.mNrRsrp && this.mNrRsrq == s.mNrRsrq && this.mNrRssnr == s.mNrRssnr && this.mNrCqi == s.mNrCqi) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("SignalStrength: ");
        sb.append(this.mGsmSignalStrength);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mGsmBitErrorRate);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCdmaDbm);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mCdmaEcio);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mEvdoDbm);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mEvdoEcio);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mEvdoSnr);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteSignalStrength);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteRsrp);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteRsrq);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteRssnr);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteCqi);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mLteRsrpBoost);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mTdScdmaRscp);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mWcdmaSignalStrength);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mWcdmaRscpAsu);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mWcdmaRscp);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mWcdmaEcio);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mIsGsm ? "gsm|lte" : "cdma");
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (this.mUseOnlyRsrpForLteLevel) {
            str = "use_only_rsrp_for_lte_level";
        } else {
            str = "use_rsrp_and_rssnr_for_lte_level";
        }
        sb.append(str);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mWcdmaDefaultSignalMeasurement);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(Arrays.toString(this.mLteRsrpThresholds));
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(Arrays.toString(this.mWcdmaRscpThresholds));
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mPhoneId);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mNrSignalStrength);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mNrRsrp);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mNrRsrq);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mNrRssnr);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(this.mNrCqi);
        return sb.toString();
    }

    private int getGsmRelatedSignalStrength() {
        int level = getLteLevel();
        if (level != 0) {
            return level;
        }
        int level2 = getTdScdmaLevel();
        if (level2 != 0) {
            return level2;
        }
        int level3 = getWcdmaLevel();
        if (level3 == 0) {
            return getGsmLevel();
        }
        return level3;
    }

    private int getCdmaRelatedSignalStrength() {
        int cdmaLevel = getCdmaLevel();
        int evdoLevel = getEvdoLevel();
        if (evdoLevel == 0) {
            return cdmaLevel;
        }
        if (cdmaLevel == 0) {
            return evdoLevel;
        }
        return cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mGsmSignalStrength = m.getInt("GsmSignalStrength");
        this.mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        this.mCdmaDbm = m.getInt("CdmaDbm");
        this.mCdmaEcio = m.getInt("CdmaEcio");
        this.mEvdoDbm = m.getInt("EvdoDbm");
        this.mEvdoEcio = m.getInt("EvdoEcio");
        this.mEvdoSnr = m.getInt("EvdoSnr");
        this.mLteSignalStrength = m.getInt("LteSignalStrength");
        this.mLteRsrp = m.getInt("LteRsrp");
        this.mLteRsrq = m.getInt("LteRsrq");
        this.mLteRssnr = m.getInt("LteRssnr");
        this.mLteCqi = m.getInt("LteCqi");
        this.mLteRsrpBoost = m.getInt("LteRsrpBoost");
        this.mTdScdmaRscp = m.getInt("TdScdma");
        this.mWcdmaSignalStrength = m.getInt("WcdmaSignalStrength");
        this.mWcdmaRscpAsu = m.getInt("WcdmaRscpAsu");
        this.mWcdmaRscp = m.getInt("WcdmaRscp");
        this.mWcdmaEcio = m.getInt("mWcdmaEcio");
        this.mIsGsm = m.getBoolean("IsGsm");
        this.mUseOnlyRsrpForLteLevel = m.getBoolean("UseOnlyRsrpForLteLevel");
        this.mWcdmaDefaultSignalMeasurement = m.getString("WcdmaDefaultSignalMeasurement");
        ArrayList<Integer> lteRsrpThresholds = m.getIntegerArrayList("lteRsrpThresholds");
        if (lteRsrpThresholds != null) {
            int lteSize = lteRsrpThresholds.size();
            for (int i = 0; i < lteSize; i++) {
                this.mLteRsrpThresholds[i] = lteRsrpThresholds.get(i).intValue();
            }
        }
        ArrayList<Integer> wcdmaRscpThresholds = m.getIntegerArrayList("wcdmaRscpThresholds");
        if (wcdmaRscpThresholds != null) {
            int wcdmaSize = wcdmaRscpThresholds.size();
            for (int i2 = 0; i2 < wcdmaSize; i2++) {
                this.mWcdmaRscpThresholds[i2] = wcdmaRscpThresholds.get(i2).intValue();
            }
        }
        this.mPhoneId = m.getInt("mPhoneId");
        this.mNrSignalStrength = m.getInt("NrSignalStrength");
        this.mNrRsrp = m.getInt("NrRsrp");
        this.mNrRsrq = m.getInt("NrRsrq");
        this.mNrRssnr = m.getInt("NrRssnr");
        this.mNrCqi = m.getInt("NrCqi");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", this.mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", this.mGsmBitErrorRate);
        m.putInt("CdmaDbm", this.mCdmaDbm);
        m.putInt("CdmaEcio", this.mCdmaEcio);
        m.putInt("EvdoDbm", this.mEvdoDbm);
        m.putInt("EvdoEcio", this.mEvdoEcio);
        m.putInt("EvdoSnr", this.mEvdoSnr);
        m.putInt("LteSignalStrength", this.mLteSignalStrength);
        m.putInt("LteRsrp", this.mLteRsrp);
        m.putInt("LteRsrq", this.mLteRsrq);
        m.putInt("LteRssnr", this.mLteRssnr);
        m.putInt("LteCqi", this.mLteCqi);
        m.putInt("LteRsrpBoost", this.mLteRsrpBoost);
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putInt("WcdmaSignalStrength", this.mWcdmaSignalStrength);
        m.putInt("WcdmaRscpAsu", this.mWcdmaRscpAsu);
        m.putInt("WcdmaRscp", this.mWcdmaRscp);
        m.putInt("mWcdmaEcio", this.mWcdmaEcio);
        m.putBoolean("IsGsm", this.mIsGsm);
        m.putBoolean("UseOnlyRsrpForLteLevel", this.mUseOnlyRsrpForLteLevel);
        m.putString("WcdmaDefaultSignalMeasurement", this.mWcdmaDefaultSignalMeasurement);
        ArrayList<Integer> lteRsrpThresholds = new ArrayList<>();
        for (int value : this.mLteRsrpThresholds) {
            lteRsrpThresholds.add(Integer.valueOf(value));
        }
        m.putIntegerArrayList("lteRsrpThresholds", lteRsrpThresholds);
        ArrayList<Integer> wcdmaRscpThresholds = new ArrayList<>();
        for (int value2 : this.mWcdmaRscpThresholds) {
            wcdmaRscpThresholds.add(Integer.valueOf(value2));
        }
        m.putIntegerArrayList("wcdmaRscpThresholds", wcdmaRscpThresholds);
        m.putInt("mPhoneId", this.mPhoneId);
        m.putInt("NrSignalStrength", this.mNrSignalStrength);
        m.putInt("NrRsrp", this.mNrRsrp);
        m.putInt("NrRsrq", this.mNrRsrq);
        m.putInt("NrRssnr", this.mNrRssnr);
        m.putInt("NrCqi", this.mNrCqi);
    }

    private int[] getDefaultLteRsrpThresholds() {
        return CarrierConfigManager.getDefaultConfig().getIntArray(CarrierConfigManager.KEY_LTE_RSRP_THRESHOLDS_INT_ARRAY);
    }

    private int[] getDefaultWcdmaRscpThresholds() {
        return CarrierConfigManager.getDefaultConfig().getIntArray(CarrierConfigManager.KEY_WCDMA_RSCP_THRESHOLDS_INT_ARRAY);
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    public int getNrLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getNrLevel(this);
        }
        return 0;
    }

    public int getNrAsuLevel() {
        int nrDbm = getNrDbm();
        if (nrDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return nrDbm + 140;
    }

    public int getNrSignalStrength() {
        return this.mNrSignalStrength;
    }

    public int getNrRsrp() {
        return this.mNrRsrp;
    }

    public int getNrRsrq() {
        return this.mNrRsrq;
    }

    public int getNrRssnr() {
        return this.mNrRssnr;
    }

    public int getNrCqi() {
        return this.mNrCqi;
    }

    public int getNrDbm() {
        return this.mNrRsrp;
    }

    public void setNrRsrp(int nrRsrp) {
        this.mNrRsrp = nrRsrp;
    }

    public void setNrRsrq(int nrRsrq) {
        this.mNrRsrq = nrRsrq;
    }

    public void setNrSignalStrength(int nrSignalStrength) {
        this.mNrSignalStrength = nrSignalStrength;
    }

    public void setNrRssnr(int nrRssnr) {
        this.mNrRssnr = nrRssnr;
    }

    public void setNrSigStr(int nrSignalStrength, int nrRsrp, int nrRsrq, int nrRssnr, int nrCqi) {
        this.mNrSignalStrength = nrSignalStrength;
        this.mNrRsrp = nrRsrp;
        this.mNrRsrq = nrRsrq;
        this.mNrRssnr = nrRssnr;
        this.mNrCqi = nrCqi;
    }
}
