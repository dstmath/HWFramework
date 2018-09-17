package android.telephony;

import android.common.HwFrameworkFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import com.android.internal.os.PowerProfile;
import huawei.cust.HwCustUtils;

public class SignalStrength implements Parcelable {
    public static final Creator<SignalStrength> CREATOR = new Creator() {
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
    private static final String LOG_TAG = "SignalStrength";
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = new String[]{PowerProfile.POWER_NONE, "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    private static HwCustSignalStrength mHwCustSignalStrength = ((HwCustSignalStrength) HwCustUtils.createObj(HwCustSignalStrength.class, new Object[0]));
    private int GSM_STRENGTH_GOOD_STD;
    private int GSM_STRENGTH_GREAT_STD;
    private int GSM_STRENGTH_MODERATE_STD;
    private int GSM_STRENGTH_NONE;
    private int GSM_STRENGTH_POOR_STD;
    private int GSM_STRENGTH_UNKOUWN;
    private int LTE_STRENGTH_GOOD_STD;
    private int LTE_STRENGTH_GREAT_STD;
    private int LTE_STRENGTH_MODERATE_STD;
    private int LTE_STRENGTH_NONE_STD;
    private int LTE_STRENGTH_POOR_STD;
    private int LTE_STRENGTH_UNKOUWN_STD;
    private int TDS_STRENGTH_GOOD_STD;
    private int TDS_STRENGTH_GREAT_STD;
    private int TDS_STRENGTH_MODERATE_STD;
    private int TDS_STRENGTH_POOR_STD;
    private int WCDMA_STRENGTH_GOOD_STD;
    private int WCDMA_STRENGTH_GREAT_STD;
    private int WCDMA_STRENGTH_INVALID;
    private int WCDMA_STRENGTH_MODERATE_STD;
    private int WCDMA_STRENGTH_NONE;
    private int WCDMA_STRENGTH_POOR_STD;
    private boolean isCdma;
    private boolean isGsm;
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;
    private int mGsmBitErrorRate;
    private int mGsmSignalStrength;
    private int mLteCqi;
    private int mLteRsrp;
    private int mLteRsrpBoost;
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    private int mTdScdmaRscp;
    private int mWcdmaEcio;
    private int mWcdmaRscp;

    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public SignalStrength() {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mWcdmaRscp = -1;
        this.mWcdmaEcio = -1;
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
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = true;
        this.isCdma = false;
    }

    public SignalStrength(boolean gsmFlag) {
        boolean z = false;
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mWcdmaRscp = -1;
        this.mWcdmaEcio = -1;
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
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsmFlag;
        if (!this.isGsm) {
            z = true;
        }
        this.isCdma = z;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, int tdScdmaRscp, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, lteRsrpBoost, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsmFlag);
    }

    public SignalStrength(SignalStrength s) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        copyFrom(s);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsm) {
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsm);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, boolean gsm) {
        this.mGsmSignalStrength = gsmSignalStrength;
        this.mGsmBitErrorRate = gsmBitErrorRate;
        this.mWcdmaRscp = wcdmaRscp;
        this.mWcdmaEcio = wcdmaEcio;
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
        this.mLteRsrpBoost = lteRsrpBoost;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsm;
        this.isCdma = !this.isGsm;
    }

    protected void copyFrom(SignalStrength s) {
        this.mGsmSignalStrength = s.mGsmSignalStrength;
        this.mGsmBitErrorRate = s.mGsmBitErrorRate;
        this.mWcdmaRscp = s.mWcdmaRscp;
        this.mWcdmaEcio = s.mWcdmaEcio;
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
        this.mLteRsrpBoost = s.mLteRsrpBoost;
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.isGsm = s.isGsm;
        this.isCdma = s.isCdma;
    }

    public SignalStrength(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = 0;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = 0;
        this.WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
        this.LTE_STRENGTH_POOR_STD = -115;
        this.LTE_STRENGTH_MODERATE_STD = -105;
        this.LTE_STRENGTH_GOOD_STD = -95;
        this.LTE_STRENGTH_GREAT_STD = -85;
        this.LTE_STRENGTH_NONE_STD = -140;
        this.LTE_STRENGTH_UNKOUWN_STD = -44;
        this.TDS_STRENGTH_POOR_STD = -111;
        this.TDS_STRENGTH_MODERATE_STD = -105;
        this.TDS_STRENGTH_GOOD_STD = -98;
        this.TDS_STRENGTH_GREAT_STD = -91;
        this.mGsmSignalStrength = in.readInt();
        this.mGsmBitErrorRate = in.readInt();
        this.mWcdmaRscp = in.readInt();
        this.mWcdmaEcio = in.readInt();
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
        this.mLteRsrpBoost = in.readInt();
        this.mTdScdmaRscp = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isGsm = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.isCdma = z2;
    }

    public static SignalStrength makeSignalStrengthFromRilParcel(Parcel in) {
        SignalStrength ss = new SignalStrength();
        ss.mGsmSignalStrength = in.readInt();
        ss.mGsmBitErrorRate = in.readInt();
        ss.mWcdmaRscp = in.readInt();
        ss.mWcdmaEcio = in.readInt();
        ss.mCdmaDbm = in.readInt();
        ss.mCdmaEcio = in.readInt();
        ss.mEvdoDbm = in.readInt();
        ss.mEvdoEcio = in.readInt();
        ss.mEvdoSnr = in.readInt();
        ss.mLteSignalStrength = in.readInt();
        ss.mLteRsrp = in.readInt();
        ss.mLteRsrq = in.readInt();
        ss.mLteRssnr = in.readInt();
        ss.mLteCqi = in.readInt();
        ss.mTdScdmaRscp = in.readInt();
        return ss;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeInt(this.mGsmSignalStrength);
        out.writeInt(this.mGsmBitErrorRate);
        out.writeInt(this.mWcdmaRscp);
        out.writeInt(this.mWcdmaEcio);
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
        out.writeInt(this.mLteRsrpBoost);
        out.writeInt(this.mTdScdmaRscp);
        if (this.isGsm) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.isCdma) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public int describeContents() {
        return 0;
    }

    public void validateInput() {
        if (FEATURE_VALIDATEINPUT) {
            HwFrameworkFactory.getHwInnerTelephonyManager().validateInput(this);
        }
    }

    public void setGsm(boolean gsmFlag) {
        this.isGsm = gsmFlag;
    }

    public void setLteRsrpBoost(int lteRsrpBoost) {
        this.mLteRsrpBoost = lteRsrpBoost;
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

    public int getWcdmaRscp() {
        return this.mWcdmaRscp;
    }

    public int getWcdmaEcio() {
        return this.mWcdmaEcio;
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
        if (this.isGsm) {
            int level = getLteLevel();
            if (level != 0) {
                return level;
            }
            level = getTdScdmaLevel();
            if (level == 0) {
                return getGsmLevel();
            }
            return level;
        }
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

    public int getAsuLevel() {
        if (!this.isGsm) {
            int cdmaAsuLevel = getCdmaAsuLevel();
            int evdoAsuLevel = getEvdoAsuLevel();
            if (evdoAsuLevel == 0) {
                return cdmaAsuLevel;
            }
            if (cdmaAsuLevel == 0) {
                return evdoAsuLevel;
            }
            return cdmaAsuLevel < evdoAsuLevel ? cdmaAsuLevel : evdoAsuLevel;
        } else if (getLteLevel() != 0) {
            return getLteAsuLevel();
        } else {
            if (getTdScdmaLevel() == 0) {
                return getGsmAsuLevel();
            }
            return getTdScdmaAsuLevel();
        }
    }

    public int getDbm() {
        if (isGsm()) {
            int dBm = getLteDbm();
            if (dBm == Integer.MAX_VALUE || dBm == 0) {
                if (getTdScdmaLevel() == 0) {
                    dBm = getGsmDbm();
                } else {
                    dBm = getTdScdmaDbm();
                }
            }
            return dBm;
        }
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        if (evdoDbm != -120) {
            if (cdmaDbm == -120) {
                cdmaDbm = evdoDbm;
            } else if (cdmaDbm >= evdoDbm) {
                cdmaDbm = evdoDbm;
            }
        }
        return cdmaDbm;
    }

    public int getOriginalGsmSignalStrength() {
        return this.mGsmSignalStrength;
    }

    public int getGsmDbm() {
        int gsmSignalStrength = getGsmSignalStrength();
        if (mHwCustSignalStrength != null && mHwCustSignalStrength.isDocomo()) {
            gsmSignalStrength = getOriginalGsmSignalStrength();
        }
        int wcdmaDbm = getWcdmaRscp();
        int dBm = (-1 == wcdmaDbm || wcdmaDbm == 0) ? gsmSignalStrength == 99 ? -1 : gsmSignalStrength : wcdmaDbm;
        if (mHwCustSignalStrength == null) {
            return dBm;
        }
        int custDbm = mHwCustSignalStrength.getGsmDbm(gsmSignalStrength);
        if (custDbm != 0) {
            return custDbm;
        }
        return dBm;
    }

    public int getGsmLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmLevel(this);
        }
        int level;
        if (-1 == this.mWcdmaRscp || this.mWcdmaRscp == 0) {
            if (this.GSM_STRENGTH_NONE == this.mGsmSignalStrength || this.GSM_STRENGTH_UNKOUWN == this.mGsmSignalStrength) {
                level = 0;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_GREAT_STD) {
                level = 4;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_GOOD_STD) {
                level = 3;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_MODERATE_STD) {
                level = 2;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_POOR_STD) {
                level = 1;
            } else {
                level = 0;
            }
        } else if (this.WCDMA_STRENGTH_NONE == this.mWcdmaRscp || this.mWcdmaRscp == Integer.MAX_VALUE) {
            level = 0;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_GREAT_STD) {
            level = 4;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_GOOD_STD) {
            level = 3;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_MODERATE_STD) {
            level = 2;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_POOR_STD) {
            level = 1;
        } else {
            level = 0;
        }
        return level;
    }

    public int getGsmAsuLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmAsuLevel(this);
        }
        int asu;
        int gsmSignalStrength = getGsmSignalStrength();
        if (mHwCustSignalStrength != null && mHwCustSignalStrength.isDocomo()) {
            gsmSignalStrength = getOriginalGsmSignalStrength();
        }
        int dbm = gsmSignalStrength == 0 ? -1 : gsmSignalStrength;
        log("gsmSignalStrength=" + gsmSignalStrength + ", mWcdmaRscp" + this.mWcdmaRscp);
        if (dbm == -1 || this.mWcdmaRscp != 0) {
            asu = -1;
        } else {
            asu = (dbm + 113) / 2;
        }
        return asu;
    }

    public int getCdmaLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getCdmaLevel(this);
        }
        int levelDbm;
        int levelEcio;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
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
        } else {
            levelEcio = 0;
        }
        return levelDbm < levelEcio ? levelDbm : levelEcio;
    }

    public int getCdmaAsuLevel() {
        int cdmaAsuLevel;
        int ecioAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
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
        } else {
            ecioAsuLevel = 99;
        }
        return cdmaAsuLevel < ecioAsuLevel ? cdmaAsuLevel : ecioAsuLevel;
    }

    public int getEvdoLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getEvdoLevel(this);
        }
        int levelEvdoDbm;
        int levelEvdoSnr;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
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
        } else {
            levelEvdoSnr = 99;
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
        int rsrpIconLevel;
        if (this.mLteRsrp > this.LTE_STRENGTH_UNKOUWN_STD) {
            rsrpIconLevel = 0;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_GREAT_STD) {
            rsrpIconLevel = 4;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_GOOD_STD) {
            rsrpIconLevel = 3;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_MODERATE_STD) {
            rsrpIconLevel = 2;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_POOR_STD) {
            rsrpIconLevel = 1;
        } else {
            rsrpIconLevel = 0;
        }
        log("getLTELevel - rsrp:" + this.mLteRsrp + " snr:" + this.mLteRssnr + " rsrpIconLevel:" + rsrpIconLevel);
        return rsrpIconLevel;
    }

    public int getLteAsuLevel() {
        int lteDbm = getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return lteDbm + 140;
    }

    public boolean isGsm() {
        return this.isGsm;
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

    public int hashCode() {
        int i;
        int i2 = (this.mTdScdmaRscp * 31) + (((((((((((((((this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31)) + (this.mWcdmaRscp * 31)) + (this.mWcdmaEcio * 31)) + (this.mCdmaDbm * 31)) + (this.mCdmaEcio * 31)) + (this.mEvdoDbm * 31)) + (this.mEvdoEcio * 31)) + (this.mEvdoSnr * 31)) + (this.mLteSignalStrength * 31)) + (this.mLteRsrp * 31)) + (this.mLteRsrq * 31)) + (this.mLteRssnr * 31)) + (this.mLteCqi * 31)) + (this.mLteRsrpBoost * 31));
        if (this.isGsm) {
            i = 1;
        } else {
            i = 0;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return false;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mWcdmaRscp == s.mWcdmaRscp && this.mWcdmaEcio == s.mWcdmaEcio && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mLteRsrpBoost == s.mLteRsrpBoost && this.mTdScdmaRscp == s.mTdScdmaRscp && this.isGsm == s.isGsm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SignalStrength: ").append(this.mGsmSignalStrength).append(" ").append(this.mGsmBitErrorRate).append(" ").append(this.mWcdmaRscp).append(" ").append(this.mWcdmaEcio).append(" ").append(this.mCdmaDbm).append(" ").append(this.mCdmaEcio).append(" ").append(this.mEvdoDbm).append(" ").append(this.mEvdoEcio).append(" ").append(this.mEvdoSnr).append(" ").append(this.mLteSignalStrength).append(" ").append(this.mLteRsrp).append(" ").append(this.mLteRsrq).append(" ").append(this.mLteRssnr).append(" ").append(this.mLteCqi).append(" ").append(this.mLteRsrpBoost).append(" ").append(this.mTdScdmaRscp).append(" ").append(this.isCdma).append(" ");
        if (this.isGsm) {
            str = "gw|lte";
        } else {
            str = "cdma";
        }
        return append.append(str).toString();
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mGsmSignalStrength = m.getInt("GsmSignalStrength");
        this.mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        this.mWcdmaRscp = m.getInt("WcdmaRscp");
        this.mWcdmaEcio = m.getInt("WcdmaEcio");
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
        this.mLteRsrpBoost = m.getInt("lteRsrpBoost");
        this.mTdScdmaRscp = m.getInt("TdScdma");
        this.isGsm = m.getBoolean("isGsm");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", this.mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", this.mGsmBitErrorRate);
        m.putInt("WcdmaRscp", this.mWcdmaRscp);
        m.putInt("WcdmaEcio", this.mWcdmaEcio);
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
        m.putInt("lteRsrpBoost", this.mLteRsrpBoost);
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putBoolean("isGsm", this.isGsm);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
