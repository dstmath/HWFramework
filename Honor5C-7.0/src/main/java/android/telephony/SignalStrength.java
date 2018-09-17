package android.telephony;

import android.common.HwFrameworkFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.huawei.pgmng.log.LogPower;

public class SignalStrength implements Parcelable {
    public static final Creator<SignalStrength> CREATOR = null;
    private static final boolean DBG = false;
    private static final boolean FEATURE_VALIDATEINPUT = false;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final String LOG_TAG = "SignalStrength";
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    private static final int[] RSRP_THRESH_LENIENT = null;
    private static final int[] RSRP_THRESH_STRICT = null;
    private static final int RSRP_THRESH_TYPE_STRICT = 0;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = null;
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
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
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    private int mTdScdmaRscp;
    private int mWcdmaEcio;
    private int mWcdmaRscp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.SignalStrength.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.SignalStrength.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SignalStrength.<clinit>():void");
    }

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
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        this.mLteRsrp = INVALID;
        this.mLteRsrq = INVALID;
        this.mLteRssnr = INVALID;
        this.mLteCqi = INVALID;
        this.mTdScdmaRscp = INVALID;
        this.isGsm = true;
        this.isCdma = FEATURE_VALIDATEINPUT;
    }

    public SignalStrength(boolean gsmFlag) {
        boolean z = FEATURE_VALIDATEINPUT;
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        this.mLteRsrp = INVALID;
        this.mLteRsrq = INVALID;
        this.mLteRssnr = INVALID;
        this.mLteCqi = INVALID;
        this.mTdScdmaRscp = INVALID;
        this.isGsm = gsmFlag;
        if (!this.isGsm) {
            z = true;
        }
        this.isCdma = z;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, gsmFlag);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsmFlag) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, INVALID, INVALID, INVALID, INVALID, gsmFlag);
    }

    public SignalStrength(SignalStrength s) {
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        initialize(gsmSignalStrength, gsmBitErrorRate, wcdmaRscp, wcdmaEcio, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, INVALID, INVALID, INVALID, INVALID, gsm);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int wcdmaRscp, int wcdmaEcio, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsm) {
        boolean z;
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
        this.mTdScdmaRscp = INVALID;
        this.isGsm = gsm;
        if (this.isGsm) {
            z = FEATURE_VALIDATEINPUT;
        } else {
            z = true;
        }
        this.isCdma = z;
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
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.isGsm = s.isGsm;
        this.isCdma = s.isCdma;
    }

    public SignalStrength(Parcel in) {
        boolean z = true;
        this.GSM_STRENGTH_POOR_STD = -109;
        this.GSM_STRENGTH_MODERATE_STD = -103;
        this.GSM_STRENGTH_GOOD_STD = -97;
        this.GSM_STRENGTH_GREAT_STD = -89;
        this.GSM_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.GSM_STRENGTH_UNKOUWN = 99;
        this.WCDMA_STRENGTH_POOR_STD = -112;
        this.WCDMA_STRENGTH_MODERATE_STD = -105;
        this.WCDMA_STRENGTH_GOOD_STD = -98;
        this.WCDMA_STRENGTH_GREAT_STD = -91;
        this.WCDMA_STRENGTH_NONE = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        this.WCDMA_STRENGTH_INVALID = INVALID;
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
        this.mTdScdmaRscp = in.readInt();
        this.isGsm = in.readInt() != 0 ? true : FEATURE_VALIDATEINPUT;
        if (in.readInt() == 0) {
            z = FEATURE_VALIDATEINPUT;
        }
        this.isCdma = z;
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
        int i = SIGNAL_STRENGTH_POOR;
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
        out.writeInt(this.mTdScdmaRscp);
        out.writeInt(this.isGsm ? SIGNAL_STRENGTH_POOR : SIGNAL_STRENGTH_NONE_OR_UNKNOWN);
        if (!this.isCdma) {
            i = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        out.writeInt(i);
    }

    public int describeContents() {
        return SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
    }

    public void validateInput() {
        if (FEATURE_VALIDATEINPUT) {
            HwFrameworkFactory.getHwInnerTelephonyManager().validateInput(this);
        }
    }

    public void setGsm(boolean gsmFlag) {
        this.isGsm = gsmFlag;
    }

    public int getGsmSignalStrength() {
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
            if (dBm == INVALID || dBm == 0) {
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

    public int getGsmDbm() {
        int gsmSignalStrength = getGsmSignalStrength();
        int wcdmaDbm = getWcdmaRscp();
        if (-1 == wcdmaDbm || wcdmaDbm == 0) {
            return gsmSignalStrength == 99 ? -1 : gsmSignalStrength;
        } else {
            return wcdmaDbm;
        }
    }

    public int getGsmLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmLevel(this);
        }
        int level;
        if (-1 == this.mWcdmaRscp || this.mWcdmaRscp == 0) {
            if (this.GSM_STRENGTH_NONE == this.mGsmSignalStrength || this.GSM_STRENGTH_UNKOUWN == this.mGsmSignalStrength) {
                level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_GREAT_STD) {
                level = SIGNAL_STRENGTH_GREAT;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_GOOD_STD) {
                level = SIGNAL_STRENGTH_GOOD;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_MODERATE_STD) {
                level = SIGNAL_STRENGTH_MODERATE;
            } else if (this.mGsmSignalStrength >= this.GSM_STRENGTH_POOR_STD) {
                level = SIGNAL_STRENGTH_POOR;
            } else {
                level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
            }
        } else if (this.WCDMA_STRENGTH_NONE == this.mWcdmaRscp || this.mWcdmaRscp == INVALID) {
            level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_GREAT_STD) {
            level = SIGNAL_STRENGTH_GREAT;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_GOOD_STD) {
            level = SIGNAL_STRENGTH_GOOD;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_MODERATE_STD) {
            level = SIGNAL_STRENGTH_MODERATE;
        } else if (this.mWcdmaRscp >= this.WCDMA_STRENGTH_POOR_STD) {
            level = SIGNAL_STRENGTH_POOR;
        } else {
            level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        return level;
    }

    public int getGsmAsuLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getGsmAsuLevel(this);
        }
        int asu;
        int gsmSignalStrength = getGsmSignalStrength();
        int dbm = gsmSignalStrength == 0 ? -1 : gsmSignalStrength;
        log("gsmSignalStrength=" + gsmSignalStrength + ", mWcdmaRscp" + this.mWcdmaRscp);
        if (dbm == -1 || this.mWcdmaRscp != 0) {
            asu = -1;
        } else {
            asu = (dbm + LogPower.APP_RUN_FRONT) / SIGNAL_STRENGTH_MODERATE;
        }
        return asu;
    }

    public int getCdmaLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getCdmaLevel(this);
        }
        int levelDbm;
        int levelEcio;
        int level;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -75) {
            levelDbm = SIGNAL_STRENGTH_GREAT;
        } else if (cdmaDbm >= -85) {
            levelDbm = SIGNAL_STRENGTH_GOOD;
        } else if (cdmaDbm >= -95) {
            levelDbm = SIGNAL_STRENGTH_MODERATE;
        } else if (cdmaDbm >= -100) {
            levelDbm = SIGNAL_STRENGTH_POOR;
        } else {
            levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        if (cdmaEcio >= -90) {
            levelEcio = SIGNAL_STRENGTH_GREAT;
        } else if (cdmaEcio >= -110) {
            levelEcio = SIGNAL_STRENGTH_GOOD;
        } else if (cdmaEcio >= -130) {
            levelEcio = SIGNAL_STRENGTH_MODERATE;
        } else if (cdmaEcio >= -150) {
            levelEcio = SIGNAL_STRENGTH_POOR;
        } else {
            levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        if (levelDbm < levelEcio) {
            level = levelDbm;
        } else {
            level = levelEcio;
        }
        return level;
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
            cdmaAsuLevel = SIGNAL_STRENGTH_GREAT;
        } else if (cdmaDbm >= -95) {
            cdmaAsuLevel = SIGNAL_STRENGTH_MODERATE;
        } else if (cdmaDbm >= -100) {
            cdmaAsuLevel = SIGNAL_STRENGTH_POOR;
        } else {
            cdmaAsuLevel = 99;
        }
        if (cdmaEcio >= -90) {
            ecioAsuLevel = 16;
        } else if (cdmaEcio >= -100) {
            ecioAsuLevel = 8;
        } else if (cdmaEcio >= -115) {
            ecioAsuLevel = SIGNAL_STRENGTH_GREAT;
        } else if (cdmaEcio >= -130) {
            ecioAsuLevel = SIGNAL_STRENGTH_MODERATE;
        } else if (cdmaEcio >= -150) {
            ecioAsuLevel = SIGNAL_STRENGTH_POOR;
        } else {
            ecioAsuLevel = 99;
        }
        if (cdmaAsuLevel < ecioAsuLevel) {
            return cdmaAsuLevel;
        }
        return ecioAsuLevel;
    }

    public int getEvdoLevel() {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useHwSignalStrength()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().getEvdoLevel(this);
        }
        int levelEvdoDbm;
        int levelEvdoSnr;
        int level;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -65) {
            levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
        } else if (evdoDbm >= -75) {
            levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
        } else if (evdoDbm >= -90) {
            levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = SIGNAL_STRENGTH_POOR;
        } else {
            levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        if (evdoSnr >= 7) {
            levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
        } else if (evdoSnr >= NUM_SIGNAL_STRENGTH_BINS) {
            levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
        } else if (evdoSnr >= SIGNAL_STRENGTH_GOOD) {
            levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
        } else if (evdoSnr >= SIGNAL_STRENGTH_POOR) {
            levelEvdoSnr = SIGNAL_STRENGTH_POOR;
        } else {
            levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        if (levelEvdoDbm < levelEvdoSnr) {
            level = levelEvdoDbm;
        } else {
            level = levelEvdoSnr;
        }
        return level;
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
            levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
        } else if (evdoDbm >= -95) {
            levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = SIGNAL_STRENGTH_POOR;
        } else {
            levelEvdoDbm = 99;
        }
        if (evdoSnr >= 7) {
            levelEvdoSnr = 16;
        } else if (evdoSnr >= 6) {
            levelEvdoSnr = 8;
        } else if (evdoSnr >= NUM_SIGNAL_STRENGTH_BINS) {
            levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
        } else if (evdoSnr >= SIGNAL_STRENGTH_GOOD) {
            levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
        } else if (evdoSnr >= SIGNAL_STRENGTH_POOR) {
            levelEvdoSnr = SIGNAL_STRENGTH_POOR;
        } else {
            levelEvdoSnr = 99;
        }
        if (levelEvdoDbm < levelEvdoSnr) {
            return levelEvdoDbm;
        }
        return levelEvdoSnr;
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
            rsrpIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_GREAT_STD) {
            rsrpIconLevel = SIGNAL_STRENGTH_GREAT;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_GOOD_STD) {
            rsrpIconLevel = SIGNAL_STRENGTH_GOOD;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_MODERATE_STD) {
            rsrpIconLevel = SIGNAL_STRENGTH_MODERATE;
        } else if (this.mLteRsrp >= this.LTE_STRENGTH_POOR_STD) {
            rsrpIconLevel = SIGNAL_STRENGTH_POOR;
        } else {
            rsrpIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        log("getLTELevel - rsrp:" + this.mLteRsrp + " snr:" + this.mLteRssnr + " rsrpIconLevel:" + rsrpIconLevel);
        return rsrpIconLevel;
    }

    public int getLteAsuLevel() {
        int lteDbm = getLteDbm();
        if (lteDbm == INVALID) {
            return MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        }
        return lteDbm + LogPower.MUSIC_AUDIO_PLAY;
    }

    public boolean isGsm() {
        return this.isGsm;
    }

    public int getTdScdmaDbm() {
        return this.mTdScdmaRscp;
    }

    public int getTdScdmaLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm > -25 || tdScdmaDbm == INVALID) {
            return SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        if (tdScdmaDbm >= -49) {
            return SIGNAL_STRENGTH_GREAT;
        }
        if (tdScdmaDbm >= -73) {
            return SIGNAL_STRENGTH_GOOD;
        }
        if (tdScdmaDbm >= -97) {
            return SIGNAL_STRENGTH_MODERATE;
        }
        if (tdScdmaDbm >= -110) {
            return SIGNAL_STRENGTH_POOR;
        }
        return SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
    }

    public int getTdScdmaAsuLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm == INVALID) {
            return MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        }
        return tdScdmaDbm + LogPower.FULL_SCREEN;
    }

    public int hashCode() {
        int i;
        int i2 = (this.mTdScdmaRscp * 31) + ((((((((((((((this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31)) + (this.mWcdmaRscp * 31)) + (this.mWcdmaEcio * 31)) + (this.mCdmaDbm * 31)) + (this.mCdmaEcio * 31)) + (this.mEvdoDbm * 31)) + (this.mEvdoEcio * 31)) + (this.mEvdoSnr * 31)) + (this.mLteSignalStrength * 31)) + (this.mLteRsrp * 31)) + (this.mLteRsrq * 31)) + (this.mLteRssnr * 31)) + (this.mLteCqi * 31));
        if (this.isGsm) {
            i = SIGNAL_STRENGTH_POOR;
        } else {
            i = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = FEATURE_VALIDATEINPUT;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return FEATURE_VALIDATEINPUT;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mWcdmaRscp == s.mWcdmaRscp && this.mWcdmaEcio == s.mWcdmaEcio && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mTdScdmaRscp == s.mTdScdmaRscp && this.isGsm == s.isGsm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return FEATURE_VALIDATEINPUT;
        }
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SignalStrength: ").append(this.mGsmSignalStrength).append(" ").append(this.mGsmBitErrorRate).append(" ").append(this.mWcdmaRscp).append(" ").append(this.mWcdmaEcio).append(" ").append(this.mCdmaDbm).append(" ").append(this.mCdmaEcio).append(" ").append(this.mEvdoDbm).append(" ").append(this.mEvdoEcio).append(" ").append(this.mEvdoSnr).append(" ").append(this.mLteSignalStrength).append(" ").append(this.mLteRsrp).append(" ").append(this.mLteRsrq).append(" ").append(this.mLteRssnr).append(" ").append(this.mLteCqi).append(" ").append(this.mTdScdmaRscp).append(" ").append(this.isCdma).append(" ");
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
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putBoolean("isGsm", Boolean.valueOf(this.isGsm).booleanValue());
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
