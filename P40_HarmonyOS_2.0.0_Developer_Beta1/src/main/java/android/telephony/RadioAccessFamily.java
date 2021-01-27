package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.RILConstants;

public class RadioAccessFamily implements Parcelable {
    private static final int CDMA = 72;
    public static final Parcelable.Creator<RadioAccessFamily> CREATOR = new Parcelable.Creator<RadioAccessFamily>() {
        /* class android.telephony.RadioAccessFamily.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RadioAccessFamily createFromParcel(Parcel in) {
            return new RadioAccessFamily(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public RadioAccessFamily[] newArray(int size) {
            if (size > 3) {
                return new RadioAccessFamily[3];
            }
            return new RadioAccessFamily[size];
        }
    };
    private static final int EVDO = 10288;
    private static final int GSM = 32771;
    private static final int HS = 17280;
    private static final int LTE = 266240;
    private static final int NR = 524288;
    private static final int PHONE_MAX_NUM = 3;
    public static final int RAF_1xRTT = 64;
    public static final int RAF_EDGE = 2;
    public static final int RAF_EHRPD = 8192;
    public static final int RAF_EVDO_0 = 16;
    public static final int RAF_EVDO_A = 32;
    public static final int RAF_EVDO_B = 2048;
    public static final int RAF_GPRS = 1;
    public static final int RAF_GSM = 32768;
    public static final int RAF_HSDPA = 128;
    public static final int RAF_HSPA = 512;
    public static final int RAF_HSPAP = 16384;
    public static final int RAF_HSUPA = 256;
    public static final int RAF_IS95A = 8;
    public static final int RAF_IS95B = 8;
    public static final int RAF_LTE = 4096;
    public static final int RAF_LTE_CA = 262144;
    public static final int RAF_NR = 524288;
    public static final int RAF_TD_SCDMA = 65536;
    public static final int RAF_UMTS = 4;
    public static final int RAF_UNKNOWN = 0;
    private static final int WCDMA = 17284;
    private int mPhoneId;
    private int mRadioAccessFamily;

    @UnsupportedAppUsage
    public RadioAccessFamily(int phoneId, int radioAccessFamily) {
        this.mPhoneId = phoneId;
        this.mRadioAccessFamily = radioAccessFamily;
    }

    @UnsupportedAppUsage
    public int getPhoneId() {
        return this.mPhoneId;
    }

    @UnsupportedAppUsage
    public int getRadioAccessFamily() {
        return this.mRadioAccessFamily;
    }

    public String toString() {
        return "{ mPhoneId = " + this.mPhoneId + ", mRadioAccessFamily = " + this.mRadioAccessFamily + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(this.mPhoneId);
        outParcel.writeInt(this.mRadioAccessFamily);
    }

    @UnsupportedAppUsage
    public static int getRafFromNetworkType(int type) {
        switch (type) {
            case 0:
                return 50055;
            case 1:
                return 32771;
            case 2:
                return WCDMA;
            case 3:
                return 50055;
            case 4:
                return 10360;
            case 5:
                return 72;
            case 6:
                return EVDO;
            case 7:
                return 60415;
            case 8:
                return 276600;
            case 9:
                return 316295;
            case 10:
                return 326655;
            case 11:
                return 266240;
            case 12:
                return 283524;
            case 13:
                return 65536;
            case 14:
                return 82820;
            case 15:
                return 331776;
            case 16:
                return 98307;
            case 17:
                return 364547;
            case 18:
                return 115591;
            case 19:
                return 349060;
            case 20:
                return 381831;
            case 21:
                return 125951;
            case 22:
                return 392191;
            case 23:
                return 524288;
            case 24:
                return 790528;
            case 25:
                return 800888;
            case 26:
                return 840583;
            case 27:
                return 850943;
            case 28:
                return 807812;
            case 29:
                return 856064;
            case 30:
                return 888835;
            case 31:
                return 873348;
            case 32:
                return 906119;
            case 33:
                return 916479;
            default:
                switch (type) {
                    case 63:
                        return 316367;
                    case 64:
                        return 800888;
                    case 65:
                        return 840583;
                    case 66:
                        return 524288;
                    case 67:
                        return 790528;
                    case 68:
                        return 807812;
                    case 69:
                        return 850943;
                    default:
                        return 0;
                }
        }
    }

    private static int getAdjustedRaf(int raf) {
        int raf2 = (raf & 32771) > 0 ? 32771 | raf : raf;
        int raf3 = (raf2 & WCDMA) > 0 ? raf2 | WCDMA : raf2;
        int raf4 = (raf3 & 72) > 0 ? raf3 | 72 : raf3;
        int raf5 = (raf4 & EVDO) > 0 ? raf4 | EVDO : raf4;
        int raf6 = (raf5 & 266240) > 0 ? 266240 | raf5 : raf5;
        return (raf6 & 524288) > 0 ? 524288 | raf6 : raf6;
    }

    public static int getHighestRafCapability(int raf) {
        if ((266240 & raf) > 0) {
            return 3;
        }
        if (((raf & WCDMA) | 27568) > 0) {
            return 2;
        }
        if ((32771 | (raf & 72)) > 0) {
            return 1;
        }
        return 0;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static int getNetworkTypeFromRaf(int raf) {
        switch (getAdjustedRaf(raf)) {
            case 72:
                return 5;
            case EVDO /* 10288 */:
                return 6;
            case 10360:
                return 4;
            case WCDMA /* 17284 */:
                return 2;
            case 32771:
                return 1;
            case 50055:
                return 3;
            case 60415:
                return 7;
            case 65536:
                return 13;
            case 82820:
                return 14;
            case 98307:
                return 16;
            case 115591:
                return 18;
            case 125951:
                return 21;
            case 266240:
                return 11;
            case 276600:
                return 8;
            case 283524:
                return 12;
            case 316295:
                return 9;
            case 316367:
                return 63;
            case 326655:
                return 10;
            case 331776:
                return 15;
            case 349060:
                return 19;
            case 364547:
                return 17;
            case 381831:
                return 20;
            case 392191:
                return 22;
            case 524288:
                return 66;
            case 790528:
                return 67;
            case 800888:
                return 64;
            case 807812:
                return 68;
            case 840583:
                return 65;
            case 850943:
                return 69;
            case 856064:
                return 29;
            case 873348:
                return 31;
            case 888835:
                return 30;
            case 906119:
                return 32;
            case 916479:
                return 33;
            default:
                return RILConstants.PREFERRED_NETWORK_MODE;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int singleRafTypeFromString(String rafString) {
        char c;
        switch (rafString.hashCode()) {
            case -2039427040:
                if (rafString.equals("LTE_CA")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -908593671:
                if (rafString.equals("TD_SCDMA")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 2315:
                if (rafString.equals("HS")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case PreciseDisconnectCause.EPDG_TUNNEL_ESTABLISH_FAILURE /* 2500 */:
                if (rafString.equals("NR")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 70881:
                if (rafString.equals("GSM")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 75709:
                if (rafString.equals("LTE")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 2063797:
                if (rafString.equals("CDMA")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 2123197:
                if (rafString.equals("EDGE")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 2140412:
                if (rafString.equals("EVDO")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 2194666:
                if (rafString.equals("GPRS")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2227260:
                if (rafString.equals("HSPA")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 2608919:
                if (rafString.equals("UMTS")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 47955627:
                if (rafString.equals("1XRTT")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 65949251:
                if (rafString.equals("EHRPD")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 69034058:
                if (rafString.equals("HSDPA")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 69045140:
                if (rafString.equals("HSPAP")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 69050395:
                if (rafString.equals("HSUPA")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 69946171:
                if (rafString.equals("IS95A")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 69946172:
                if (rafString.equals("IS95B")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 82410124:
                if (rafString.equals("WCDMA")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 2056938925:
                if (rafString.equals("EVDO_0")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 2056938942:
                if (rafString.equals("EVDO_A")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 2056938943:
                if (rafString.equals("EVDO_B")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 8;
            case 5:
                return 64;
            case 6:
                return 16;
            case 7:
                return 32;
            case '\b':
                return 128;
            case '\t':
                return 256;
            case '\n':
                return 512;
            case 11:
                return 2048;
            case '\f':
                return 8192;
            case '\r':
                return 4096;
            case 14:
                return 16384;
            case 15:
                return 32768;
            case 16:
                return 65536;
            case 17:
                return HS;
            case 18:
                return 72;
            case 19:
                return EVDO;
            case 20:
                return WCDMA;
            case 21:
                return 262144;
            case 22:
                return 524288;
            default:
                return 0;
        }
    }

    public static int rafTypeFromString(String rafList) {
        int result = 0;
        for (String raf : rafList.toUpperCase().split("\\|")) {
            int rafType = singleRafTypeFromString(raf.trim());
            if (rafType == 0) {
                return rafType;
            }
            result |= rafType;
        }
        return result;
    }
}
