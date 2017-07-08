package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

public class RadioAccessFamily implements Parcelable {
    private static final int CDMA = 112;
    public static final Creator<RadioAccessFamily> CREATOR = null;
    private static final int EVDO = 12672;
    private static final int GSM = 65542;
    private static final int HS = 36352;
    public static final int RAF_1xRTT = 64;
    public static final int RAF_EDGE = 4;
    public static final int RAF_EHRPD = 8192;
    public static final int RAF_EVDO_0 = 128;
    public static final int RAF_EVDO_A = 256;
    public static final int RAF_EVDO_B = 4096;
    public static final int RAF_GPRS = 2;
    public static final int RAF_GSM = 65536;
    public static final int RAF_HSDPA = 512;
    public static final int RAF_HSPA = 2048;
    public static final int RAF_HSPAP = 32768;
    public static final int RAF_HSUPA = 1024;
    public static final int RAF_IS95A = 16;
    public static final int RAF_IS95B = 32;
    public static final int RAF_LTE = 16384;
    public static final int RAF_TD_SCDMA = 131072;
    public static final int RAF_UMTS = 8;
    public static final int RAF_UNKNOWN = 1;
    private static final int WCDMA = 36360;
    private int mPhoneId;
    private int mRadioAccessFamily;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.RadioAccessFamily.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.RadioAccessFamily.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.RadioAccessFamily.<clinit>():void");
    }

    public RadioAccessFamily(int phoneId, int radioAccessFamily) {
        this.mPhoneId = phoneId;
        this.mRadioAccessFamily = radioAccessFamily;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getRadioAccessFamily() {
        return this.mRadioAccessFamily;
    }

    public String toString() {
        return "{ mPhoneId = " + this.mPhoneId + ", mRadioAccessFamily = " + this.mRadioAccessFamily + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(this.mPhoneId);
        outParcel.writeInt(this.mRadioAccessFamily);
    }

    public static int getRafFromNetworkType(int type) {
        switch (type) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                return 101902;
            case RAF_UNKNOWN /*1*/:
                return GSM;
            case RAF_GPRS /*2*/:
                return WCDMA;
            case HwCfgFilePolicy.BASE /*3*/:
                return 101902;
            case RAF_EDGE /*4*/:
                return 12784;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                return CDMA;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                return EVDO;
            case HwCfgFilePolicy.CLOUD_APN /*7*/:
                return 114686;
            case RAF_UMTS /*8*/:
                return 29168;
            case PGSdk.TYPE_SCRLOCK /*9*/:
                return 118286;
            case PGSdk.TYPE_CLOCK /*10*/:
                return 131070;
            case PGSdk.TYPE_IM /*11*/:
                return RAF_LTE;
            case PGSdk.TYPE_MUSIC /*12*/:
                return 52744;
            case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                return RAF_TD_SCDMA;
            case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                return 167432;
            case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                return Protocol.BASE_WIFI_MONITOR;
            case RAF_IS95A /*16*/:
                return 196614;
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                return 212998;
            case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                return 232974;
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                return 183816;
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                return 249358;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                return 245758;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                return 262142;
            default:
                return RAF_UNKNOWN;
        }
    }

    private static int getAdjustedRaf(int raf) {
        if ((GSM & raf) > 0) {
            raf |= GSM;
        }
        if ((WCDMA & raf) > 0) {
            raf |= WCDMA;
        }
        if ((raf & CDMA) > 0) {
            raf |= CDMA;
        }
        if ((raf & EVDO) > 0) {
            return raf | EVDO;
        }
        return raf;
    }

    public static int getNetworkTypeFromRaf(int raf) {
        switch (getAdjustedRaf(raf)) {
            case CDMA /*112*/:
                return 5;
            case EVDO /*12672*/:
                return 6;
            case 12784:
                return RAF_EDGE;
            case RAF_LTE /*16384*/:
                return 11;
            case 29168:
                return RAF_UMTS;
            case WCDMA /*36360*/:
                return RAF_GPRS;
            case 52744:
                return 12;
            case GSM /*65542*/:
                return RAF_UNKNOWN;
            case 101902:
                return 0;
            case 114686:
                return 7;
            case 118286:
                return 9;
            case 131070:
                return 10;
            case RAF_TD_SCDMA /*131072*/:
                return 13;
            case Protocol.BASE_WIFI_MONITOR /*147456*/:
                return 15;
            case 167432:
                return 14;
            case 183816:
                return 19;
            case 196614:
                return RAF_IS95A;
            case 212998:
                return 17;
            case 232974:
                return 18;
            case 245758:
                return 21;
            case 249358:
                return 20;
            case 262142:
                return 22;
            default:
                return RILConstants.PREFERRED_NETWORK_MODE;
        }
    }

    public static int singleRafTypeFromString(String rafString) {
        if (rafString.equals("GPRS")) {
            return RAF_GPRS;
        }
        if (rafString.equals("EDGE")) {
            return RAF_EDGE;
        }
        if (rafString.equals("UMTS")) {
            return RAF_UMTS;
        }
        if (rafString.equals("IS95A")) {
            return RAF_IS95A;
        }
        if (rafString.equals("IS95B")) {
            return RAF_IS95B;
        }
        if (rafString.equals("1XRTT")) {
            return RAF_1xRTT;
        }
        if (rafString.equals("EVDO_0")) {
            return RAF_EVDO_0;
        }
        if (rafString.equals("EVDO_A")) {
            return RAF_EVDO_A;
        }
        if (rafString.equals("HSDPA")) {
            return RAF_HSDPA;
        }
        if (rafString.equals("HSUPA")) {
            return RAF_HSUPA;
        }
        if (rafString.equals("HSPA")) {
            return RAF_HSPA;
        }
        if (rafString.equals("EVDO_B")) {
            return RAF_EVDO_B;
        }
        if (rafString.equals("EHRPD")) {
            return RAF_EHRPD;
        }
        if (rafString.equals("LTE")) {
            return RAF_LTE;
        }
        if (rafString.equals("HSPAP")) {
            return RAF_HSPAP;
        }
        if (rafString.equals("GSM")) {
            return RAF_GSM;
        }
        if (rafString.equals("TD_SCDMA")) {
            return RAF_TD_SCDMA;
        }
        if (rafString.equals("HS")) {
            return HS;
        }
        if (rafString.equals("CDMA")) {
            return CDMA;
        }
        if (rafString.equals("EVDO")) {
            return EVDO;
        }
        if (rafString.equals("WCDMA")) {
            return WCDMA;
        }
        return RAF_UNKNOWN;
    }

    public static int rafTypeFromString(String rafList) {
        String[] rafs = rafList.toUpperCase().split("\\|");
        int result = 0;
        int length = rafs.length;
        for (int i = 0; i < length; i += RAF_UNKNOWN) {
            int rafType = singleRafTypeFromString(rafs[i].trim());
            if (rafType == RAF_UNKNOWN) {
                return rafType;
            }
            result |= rafType;
        }
        return result;
    }
}
