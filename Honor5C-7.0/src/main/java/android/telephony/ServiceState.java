package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.IccCardConstants;

public class ServiceState implements Parcelable {
    public static final Creator<ServiceState> CREATOR = null;
    static final boolean DBG = false;
    static final String LOG_TAG = "PHONE";
    public static final int REGISTRATION_STATE_HOME_NETWORK = 1;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING = 0;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING = 2;
    public static final int REGISTRATION_STATE_REGISTRATION_DENIED = 3;
    public static final int REGISTRATION_STATE_ROAMING = 5;
    public static final int REGISTRATION_STATE_UNKNOWN = 4;
    public static final int RIL_RADIO_CDMA_TECHNOLOGY_BITMASK = 6392;
    public static final int RIL_RADIO_TECHNOLOGY_1xRTT = 6;
    public static final int RIL_RADIO_TECHNOLOGY_DCHSPAP = 30;
    public static final int RIL_RADIO_TECHNOLOGY_EDGE = 2;
    public static final int RIL_RADIO_TECHNOLOGY_EHRPD = 13;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_0 = 7;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_A = 8;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_B = 12;
    public static final int RIL_RADIO_TECHNOLOGY_GPRS = 1;
    public static final int RIL_RADIO_TECHNOLOGY_GSM = 16;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPA = 9;
    public static final int RIL_RADIO_TECHNOLOGY_HSPA = 11;
    public static final int RIL_RADIO_TECHNOLOGY_HSPAP = 15;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPA = 10;
    public static final int RIL_RADIO_TECHNOLOGY_IS95A = 4;
    public static final int RIL_RADIO_TECHNOLOGY_IS95B = 5;
    public static final int RIL_RADIO_TECHNOLOGY_IWLAN = 18;
    public static final int RIL_RADIO_TECHNOLOGY_LTE = 14;
    public static final int RIL_RADIO_TECHNOLOGY_LTE_CA = 31;
    public static final int RIL_RADIO_TECHNOLOGY_TD_SCDMA = 17;
    public static final int RIL_RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int RIL_REG_STATE_DENIED = 3;
    public static final int RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED = 13;
    public static final int RIL_REG_STATE_HOME = 1;
    public static final int RIL_REG_STATE_NOT_REG = 0;
    public static final int RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED = 10;
    public static final int RIL_REG_STATE_ROAMING = 5;
    public static final int RIL_REG_STATE_SEARCHING = 2;
    public static final int RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED = 12;
    public static final int RIL_REG_STATE_UNKNOWN = 4;
    public static final int RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED = 14;
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    public static final int STATE_EMERGENCY_ONLY = 2;
    public static final int STATE_IN_SERVICE = 0;
    public static final int STATE_OUT_OF_SERVICE = 1;
    public static final int STATE_POWER_OFF = 3;
    static final boolean VDBG = false;
    private int mCdmaDefaultRoamingIndicator;
    private int mCdmaEriIconIndex;
    private int mCdmaEriIconMode;
    private int mCdmaRoamingIndicator;
    private boolean mCssIndicator;
    private String mDataOperatorAlphaLong;
    private String mDataOperatorAlphaShort;
    private String mDataOperatorNumeric;
    private int mDataRegState;
    private int mDataRoamingType;
    private boolean mIsDataRoamingFromRegistration;
    private boolean mIsEmergencyOnly;
    private boolean mIsManualNetworkSelection;
    private int mNetworkId;
    private int mRilDataRadioTechnology;
    private int mRilVoiceRadioTechnology;
    private int mSystemId;
    private String mVoiceOperatorAlphaLong;
    private String mVoiceOperatorAlphaShort;
    private String mVoiceOperatorNumeric;
    private int mVoiceRegState;
    private int mVoiceRoamingType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.ServiceState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.ServiceState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.ServiceState.<clinit>():void");
    }

    public static final String getRoamingLogString(int roamingType) {
        switch (roamingType) {
            case STATE_IN_SERVICE /*0*/:
                return "home";
            case STATE_OUT_OF_SERVICE /*1*/:
                return "roaming";
            case STATE_EMERGENCY_ONLY /*2*/:
                return "Domestic Roaming";
            case STATE_POWER_OFF /*3*/:
                return "International Roaming";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public static ServiceState newFromBundle(Bundle m) {
        ServiceState ret = new ServiceState();
        if (m != null) {
            ret.setFromNotifierBundle(m);
        }
        return ret;
    }

    public ServiceState() {
        this.mVoiceRegState = STATE_OUT_OF_SERVICE;
        this.mDataRegState = STATE_OUT_OF_SERVICE;
    }

    public ServiceState(ServiceState s) {
        this.mVoiceRegState = STATE_OUT_OF_SERVICE;
        this.mDataRegState = STATE_OUT_OF_SERVICE;
        copyFrom(s);
    }

    protected void copyFrom(ServiceState s) {
        this.mVoiceRegState = s.mVoiceRegState;
        this.mDataRegState = s.mDataRegState;
        this.mVoiceRoamingType = s.mVoiceRoamingType;
        this.mDataRoamingType = s.mDataRoamingType;
        this.mVoiceOperatorAlphaLong = s.mVoiceOperatorAlphaLong;
        this.mVoiceOperatorAlphaShort = s.mVoiceOperatorAlphaShort;
        this.mVoiceOperatorNumeric = s.mVoiceOperatorNumeric;
        this.mDataOperatorAlphaLong = s.mDataOperatorAlphaLong;
        this.mDataOperatorAlphaShort = s.mDataOperatorAlphaShort;
        this.mDataOperatorNumeric = s.mDataOperatorNumeric;
        this.mIsManualNetworkSelection = s.mIsManualNetworkSelection;
        this.mRilVoiceRadioTechnology = s.mRilVoiceRadioTechnology;
        this.mRilDataRadioTechnology = s.mRilDataRadioTechnology;
        this.mCssIndicator = s.mCssIndicator;
        this.mNetworkId = s.mNetworkId;
        this.mSystemId = s.mSystemId;
        this.mCdmaRoamingIndicator = s.mCdmaRoamingIndicator;
        this.mCdmaDefaultRoamingIndicator = s.mCdmaDefaultRoamingIndicator;
        this.mCdmaEriIconIndex = s.mCdmaEriIconIndex;
        this.mCdmaEriIconMode = s.mCdmaEriIconMode;
        this.mIsEmergencyOnly = s.mIsEmergencyOnly;
        this.mIsDataRoamingFromRegistration = s.mIsDataRoamingFromRegistration;
    }

    public ServiceState(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mVoiceRegState = STATE_OUT_OF_SERVICE;
        this.mDataRegState = STATE_OUT_OF_SERVICE;
        this.mVoiceRegState = in.readInt();
        this.mDataRegState = in.readInt();
        this.mVoiceRoamingType = in.readInt();
        this.mDataRoamingType = in.readInt();
        this.mVoiceOperatorAlphaLong = in.readString();
        this.mVoiceOperatorAlphaShort = in.readString();
        this.mVoiceOperatorNumeric = in.readString();
        this.mDataOperatorAlphaLong = in.readString();
        this.mDataOperatorAlphaShort = in.readString();
        this.mDataOperatorNumeric = in.readString();
        this.mIsManualNetworkSelection = in.readInt() != 0 ? true : DBG;
        this.mRilVoiceRadioTechnology = in.readInt();
        this.mRilDataRadioTechnology = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = DBG;
        }
        this.mCssIndicator = z;
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mCdmaRoamingIndicator = in.readInt();
        this.mCdmaDefaultRoamingIndicator = in.readInt();
        this.mCdmaEriIconIndex = in.readInt();
        this.mCdmaEriIconMode = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = DBG;
        }
        this.mIsEmergencyOnly = z;
        if (in.readInt() == 0) {
            z2 = DBG;
        }
        this.mIsDataRoamingFromRegistration = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = STATE_OUT_OF_SERVICE;
        out.writeInt(this.mVoiceRegState);
        out.writeInt(this.mDataRegState);
        out.writeInt(this.mVoiceRoamingType);
        out.writeInt(this.mDataRoamingType);
        out.writeString(this.mVoiceOperatorAlphaLong);
        out.writeString(this.mVoiceOperatorAlphaShort);
        out.writeString(this.mVoiceOperatorNumeric);
        out.writeString(this.mDataOperatorAlphaLong);
        out.writeString(this.mDataOperatorAlphaShort);
        out.writeString(this.mDataOperatorNumeric);
        out.writeInt(this.mIsManualNetworkSelection ? STATE_OUT_OF_SERVICE : STATE_IN_SERVICE);
        out.writeInt(this.mRilVoiceRadioTechnology);
        out.writeInt(this.mRilDataRadioTechnology);
        if (this.mCssIndicator) {
            i = STATE_OUT_OF_SERVICE;
        } else {
            i = STATE_IN_SERVICE;
        }
        out.writeInt(i);
        out.writeInt(this.mNetworkId);
        out.writeInt(this.mSystemId);
        out.writeInt(this.mCdmaRoamingIndicator);
        out.writeInt(this.mCdmaDefaultRoamingIndicator);
        out.writeInt(this.mCdmaEriIconIndex);
        out.writeInt(this.mCdmaEriIconMode);
        if (this.mIsEmergencyOnly) {
            i = STATE_OUT_OF_SERVICE;
        } else {
            i = STATE_IN_SERVICE;
        }
        out.writeInt(i);
        if (!this.mIsDataRoamingFromRegistration) {
            i2 = STATE_IN_SERVICE;
        }
        out.writeInt(i2);
    }

    public int describeContents() {
        return STATE_IN_SERVICE;
    }

    public int getState() {
        return getVoiceRegState();
    }

    public int getVoiceRegState() {
        return this.mVoiceRegState;
    }

    public int getDataRegState() {
        return this.mDataRegState;
    }

    public boolean getRoaming() {
        return !getVoiceRoaming() ? getDataRoaming() : true;
    }

    public boolean getVoiceRoaming() {
        return this.mVoiceRoamingType != 0 ? true : DBG;
    }

    public int getVoiceRoamingType() {
        return this.mVoiceRoamingType;
    }

    public boolean getDataRoaming() {
        return this.mDataRoamingType != 0 ? true : DBG;
    }

    public void setDataRoamingFromRegistration(boolean dataRoaming) {
        this.mIsDataRoamingFromRegistration = dataRoaming;
    }

    public boolean getDataRoamingFromRegistration() {
        return this.mIsDataRoamingFromRegistration;
    }

    public int getDataRoamingType() {
        return this.mDataRoamingType;
    }

    public boolean isEmergencyOnly() {
        return this.mIsEmergencyOnly;
    }

    public int getCdmaRoamingIndicator() {
        return this.mCdmaRoamingIndicator;
    }

    public int getCdmaDefaultRoamingIndicator() {
        return this.mCdmaDefaultRoamingIndicator;
    }

    public int getCdmaEriIconIndex() {
        return this.mCdmaEriIconIndex;
    }

    public int getCdmaEriIconMode() {
        return this.mCdmaEriIconMode;
    }

    public String getOperatorAlphaLong() {
        return this.mVoiceOperatorAlphaLong;
    }

    public String getVoiceOperatorAlphaLong() {
        return this.mVoiceOperatorAlphaLong;
    }

    public String getDataOperatorAlphaLong() {
        return this.mDataOperatorAlphaLong;
    }

    public String getOperatorAlphaShort() {
        return this.mVoiceOperatorAlphaShort;
    }

    public String getVoiceOperatorAlphaShort() {
        return this.mVoiceOperatorAlphaShort;
    }

    public String getDataOperatorAlphaShort() {
        return this.mDataOperatorAlphaShort;
    }

    public String getOperatorNumeric() {
        return this.mVoiceOperatorNumeric;
    }

    public String getVoiceOperatorNumeric() {
        return this.mVoiceOperatorNumeric;
    }

    public String getDataOperatorNumeric() {
        return this.mDataOperatorNumeric;
    }

    public boolean getIsManualSelection() {
        return this.mIsManualNetworkSelection;
    }

    public int hashCode() {
        int i;
        int i2 = STATE_OUT_OF_SERVICE;
        int i3 = this.mDataRoamingType + (((this.mVoiceRegState * RIL_RADIO_TECHNOLOGY_LTE_CA) + (this.mDataRegState * 37)) + this.mVoiceRoamingType);
        if (this.mIsManualNetworkSelection) {
            i = STATE_OUT_OF_SERVICE;
        } else {
            i = STATE_IN_SERVICE;
        }
        i3 += i;
        if (this.mVoiceOperatorAlphaLong == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mVoiceOperatorAlphaLong.hashCode();
        }
        i3 += i;
        if (this.mVoiceOperatorAlphaShort == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mVoiceOperatorAlphaShort.hashCode();
        }
        i3 += i;
        if (this.mVoiceOperatorNumeric == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mVoiceOperatorNumeric.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorAlphaLong == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mDataOperatorAlphaLong.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorAlphaShort == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mDataOperatorAlphaShort.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorNumeric == null) {
            i = STATE_IN_SERVICE;
        } else {
            i = this.mDataOperatorNumeric.hashCode();
        }
        i3 = this.mCdmaDefaultRoamingIndicator + ((i + i3) + this.mCdmaRoamingIndicator);
        if (this.mIsEmergencyOnly) {
            i = STATE_OUT_OF_SERVICE;
        } else {
            i = STATE_IN_SERVICE;
        }
        i += i3;
        if (!this.mIsDataRoamingFromRegistration) {
            i2 = STATE_IN_SERVICE;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = DBG;
        try {
            ServiceState s = (ServiceState) o;
            if (o == null) {
                return DBG;
            }
            if (this.mVoiceRegState == s.mVoiceRegState && this.mDataRegState == s.mDataRegState && this.mIsManualNetworkSelection == s.mIsManualNetworkSelection && this.mVoiceRoamingType == s.mVoiceRoamingType && this.mDataRoamingType == s.mDataRoamingType && equalsHandlesNulls(this.mVoiceOperatorAlphaLong, s.mVoiceOperatorAlphaLong) && equalsHandlesNulls(this.mVoiceOperatorAlphaShort, s.mVoiceOperatorAlphaShort) && equalsHandlesNulls(this.mVoiceOperatorNumeric, s.mVoiceOperatorNumeric) && equalsHandlesNulls(this.mDataOperatorAlphaLong, s.mDataOperatorAlphaLong) && equalsHandlesNulls(this.mDataOperatorAlphaShort, s.mDataOperatorAlphaShort) && equalsHandlesNulls(this.mDataOperatorNumeric, s.mDataOperatorNumeric) && equalsHandlesNulls(Integer.valueOf(this.mRilVoiceRadioTechnology), Integer.valueOf(s.mRilVoiceRadioTechnology)) && equalsHandlesNulls(Integer.valueOf(this.mRilDataRadioTechnology), Integer.valueOf(s.mRilDataRadioTechnology)) && equalsHandlesNulls(Boolean.valueOf(this.mCssIndicator), Boolean.valueOf(s.mCssIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mNetworkId), Integer.valueOf(s.mNetworkId)) && equalsHandlesNulls(Integer.valueOf(this.mSystemId), Integer.valueOf(s.mSystemId)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaRoamingIndicator), Integer.valueOf(s.mCdmaRoamingIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaDefaultRoamingIndicator), Integer.valueOf(s.mCdmaDefaultRoamingIndicator)) && this.mIsEmergencyOnly == s.mIsEmergencyOnly && this.mIsDataRoamingFromRegistration == s.mIsDataRoamingFromRegistration) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return DBG;
        }
    }

    public static String rilRadioTechnologyToString(int rt) {
        if (TelephonyManager.IS_CHINA_TELECOM) {
            switch (rt) {
                case STATE_IN_SERVICE /*0*/:
                    return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                case STATE_OUT_OF_SERVICE /*1*/:
                    return "GSM";
                case STATE_EMERGENCY_ONLY /*2*/:
                    return "GSM";
                case STATE_POWER_OFF /*3*/:
                    return "WCDMA";
                case RIL_REG_STATE_UNKNOWN /*4*/:
                    return "CDMA 1x";
                case RIL_REG_STATE_ROAMING /*5*/:
                    return "CDMA 1x";
                case RIL_RADIO_TECHNOLOGY_1xRTT /*6*/:
                    return "CDMA 1x";
                case RIL_RADIO_TECHNOLOGY_EVDO_0 /*7*/:
                    return "CDMA EVDO";
                case RIL_RADIO_TECHNOLOGY_EVDO_A /*8*/:
                    return "CDMA EVDO";
                case RIL_RADIO_TECHNOLOGY_HSDPA /*9*/:
                    return "HSPA";
                case RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED /*10*/:
                    return "HSPA";
                case RIL_RADIO_TECHNOLOGY_HSPA /*11*/:
                    return "HSPA";
                case RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED /*12*/:
                    return "CDMA EVDO";
                case RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED /*13*/:
                    return "eHRPD";
                case RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED /*14*/:
                    return "LTE";
                case RIL_RADIO_TECHNOLOGY_HSPAP /*15*/:
                    return "HSPA+";
                case RIL_RADIO_TECHNOLOGY_GSM /*16*/:
                    return "GSM";
                default:
                    String rtString = "Unexpected";
                    Rlog.w(LOG_TAG, "Unexpected radioTechnology=" + rt);
                    return rtString;
            }
        }
        switch (rt) {
            case STATE_IN_SERVICE /*0*/:
                return "Unknown";
            case STATE_OUT_OF_SERVICE /*1*/:
                return "GPRS";
            case STATE_EMERGENCY_ONLY /*2*/:
                return "EDGE";
            case STATE_POWER_OFF /*3*/:
                return "UMTS";
            case RIL_REG_STATE_UNKNOWN /*4*/:
                return "CDMA-IS95A";
            case RIL_REG_STATE_ROAMING /*5*/:
                return "CDMA-IS95B";
            case RIL_RADIO_TECHNOLOGY_1xRTT /*6*/:
                return "1xRTT";
            case RIL_RADIO_TECHNOLOGY_EVDO_0 /*7*/:
                return "EvDo-rev.0";
            case RIL_RADIO_TECHNOLOGY_EVDO_A /*8*/:
                return "EvDo-rev.A";
            case RIL_RADIO_TECHNOLOGY_HSDPA /*9*/:
                return "HSDPA";
            case RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED /*10*/:
                return "HSUPA";
            case RIL_RADIO_TECHNOLOGY_HSPA /*11*/:
                return "HSPA";
            case RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED /*12*/:
                return "EvDo-rev.B";
            case RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED /*13*/:
                return "eHRPD";
            case RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED /*14*/:
                return "LTE";
            case RIL_RADIO_TECHNOLOGY_HSPAP /*15*/:
                return "HSPAP";
            case RIL_RADIO_TECHNOLOGY_GSM /*16*/:
                return "GSM";
            case RIL_RADIO_TECHNOLOGY_TD_SCDMA /*17*/:
                return "TD-SCDMA";
            case RIL_RADIO_TECHNOLOGY_IWLAN /*18*/:
                return "IWLAN";
            case RIL_RADIO_TECHNOLOGY_DCHSPAP /*30*/:
                return "DC-HSPA+";
            case RIL_RADIO_TECHNOLOGY_LTE_CA /*31*/:
                return "LTE-CA";
            default:
                rtString = "Unexpected";
                Rlog.w(LOG_TAG, "Unexpected radioTechnology=" + rt);
                return rtString;
        }
    }

    public String toString() {
        String str;
        String radioTechnology = rilRadioTechnologyToString(this.mRilVoiceRadioTechnology);
        String dataRadioTechnology = rilRadioTechnologyToString(this.mRilDataRadioTechnology);
        StringBuilder append = new StringBuilder().append(this.mVoiceRegState).append(" ").append(this.mDataRegState).append(" ").append("voice ").append(getRoamingLogString(this.mVoiceRoamingType)).append(" ").append("data ").append(getRoamingLogString(this.mDataRoamingType)).append(" ").append(this.mVoiceOperatorAlphaLong).append(" ").append(this.mVoiceOperatorAlphaShort).append(" ").append(this.mVoiceOperatorNumeric).append(" ").append(this.mDataOperatorAlphaLong).append(" ").append(this.mDataOperatorAlphaShort).append(" ").append(this.mDataOperatorNumeric).append(" ");
        if (this.mIsManualNetworkSelection) {
            str = "(manual)";
        } else {
            str = "";
        }
        append = append.append(str).append(" ").append(radioTechnology).append(" ").append(dataRadioTechnology).append(" ");
        if (this.mCssIndicator) {
            str = "CSS supported";
        } else {
            str = "CSS not supported";
        }
        return append.append(str).append(" ").append(this.mNetworkId).append(" ").append(this.mSystemId).append(" RoamInd=").append(this.mCdmaRoamingIndicator).append(" DefRoamInd=").append(this.mCdmaDefaultRoamingIndicator).append(" EmergOnly=").append(this.mIsEmergencyOnly).append(" IsDataRoamingFromRegistration=").append(this.mIsDataRoamingFromRegistration).toString();
    }

    private void setNullState(int state) {
        this.mVoiceRegState = state;
        this.mDataRegState = state;
        this.mVoiceRoamingType = STATE_IN_SERVICE;
        this.mDataRoamingType = STATE_IN_SERVICE;
        this.mVoiceOperatorAlphaLong = null;
        this.mVoiceOperatorAlphaShort = null;
        this.mVoiceOperatorNumeric = null;
        this.mDataOperatorAlphaLong = null;
        this.mDataOperatorAlphaShort = null;
        this.mDataOperatorNumeric = null;
        this.mIsManualNetworkSelection = DBG;
        this.mRilVoiceRadioTechnology = STATE_IN_SERVICE;
        this.mRilDataRadioTechnology = STATE_IN_SERVICE;
        this.mCssIndicator = DBG;
        this.mNetworkId = -1;
        this.mSystemId = -1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mIsEmergencyOnly = DBG;
        this.mIsDataRoamingFromRegistration = DBG;
    }

    public void setStateOutOfService() {
        setNullState(STATE_OUT_OF_SERVICE);
    }

    public void setStateOff() {
        setNullState(STATE_POWER_OFF);
    }

    public void setState(int state) {
        setVoiceRegState(state);
    }

    public void setVoiceRegState(int state) {
        this.mVoiceRegState = state;
    }

    public void setDataRegState(int state) {
        this.mDataRegState = state;
    }

    public void setRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming ? STATE_OUT_OF_SERVICE : STATE_IN_SERVICE;
        this.mDataRoamingType = this.mVoiceRoamingType;
    }

    public void setVoiceRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming ? STATE_OUT_OF_SERVICE : STATE_IN_SERVICE;
    }

    public void setVoiceRoamingType(int type) {
        this.mVoiceRoamingType = type;
    }

    public void setDataRoaming(boolean dataRoaming) {
        this.mDataRoamingType = dataRoaming ? STATE_OUT_OF_SERVICE : STATE_IN_SERVICE;
    }

    public void setDataRoamingType(int type) {
        this.mDataRoamingType = type;
    }

    public void setEmergencyOnly(boolean emergencyOnly) {
        this.mIsEmergencyOnly = emergencyOnly;
    }

    public void setCdmaRoamingIndicator(int roaming) {
        this.mCdmaRoamingIndicator = roaming;
    }

    public void setCdmaDefaultRoamingIndicator(int roaming) {
        this.mCdmaDefaultRoamingIndicator = roaming;
    }

    public void setCdmaEriIconIndex(int index) {
        this.mCdmaEriIconIndex = index;
    }

    public void setCdmaEriIconMode(int mode) {
        this.mCdmaEriIconMode = mode;
    }

    public void setOperatorName(String longName, String shortName, String numeric) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mVoiceOperatorAlphaShort = shortName;
        this.mVoiceOperatorNumeric = numeric;
        this.mDataOperatorAlphaLong = longName;
        this.mDataOperatorAlphaShort = shortName;
        this.mDataOperatorNumeric = numeric;
    }

    public void setVoiceOperatorName(String longName, String shortName, String numeric) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mVoiceOperatorAlphaShort = shortName;
        this.mVoiceOperatorNumeric = numeric;
    }

    public void setDataOperatorName(String longName, String shortName, String numeric) {
        this.mDataOperatorAlphaLong = longName;
        this.mDataOperatorAlphaShort = shortName;
        this.mDataOperatorNumeric = numeric;
    }

    public void setOperatorAlphaLong(String longName) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mDataOperatorAlphaLong = longName;
    }

    public void setVoiceOperatorAlphaLong(String longName) {
        this.mVoiceOperatorAlphaLong = longName;
    }

    public void setDataOperatorAlphaLong(String longName) {
        this.mDataOperatorAlphaLong = longName;
    }

    public void setIsManualSelection(boolean isManual) {
        this.mIsManualNetworkSelection = isManual;
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null ? true : DBG;
        } else {
            return a.equals(b);
        }
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mVoiceRegState = m.getInt("voiceRegState");
        this.mDataRegState = m.getInt("dataRegState");
        this.mVoiceRoamingType = m.getInt("voiceRoamingType");
        this.mDataRoamingType = m.getInt("dataRoamingType");
        this.mVoiceOperatorAlphaLong = m.getString("operator-alpha-long");
        this.mVoiceOperatorAlphaShort = m.getString("operator-alpha-short");
        this.mVoiceOperatorNumeric = m.getString("operator-numeric");
        this.mDataOperatorAlphaLong = m.getString("data-operator-alpha-long");
        this.mDataOperatorAlphaShort = m.getString("data-operator-alpha-short");
        this.mDataOperatorNumeric = m.getString("data-operator-numeric");
        this.mIsManualNetworkSelection = m.getBoolean("manual");
        this.mRilVoiceRadioTechnology = m.getInt("radioTechnology");
        this.mRilDataRadioTechnology = m.getInt("dataRadioTechnology");
        this.mCssIndicator = m.getBoolean("cssIndicator");
        this.mNetworkId = m.getInt("networkId");
        this.mSystemId = m.getInt("systemId");
        this.mCdmaRoamingIndicator = m.getInt("cdmaRoamingIndicator");
        this.mCdmaDefaultRoamingIndicator = m.getInt("cdmaDefaultRoamingIndicator");
        this.mIsEmergencyOnly = m.getBoolean("emergencyOnly");
        this.mIsDataRoamingFromRegistration = m.getBoolean("isDataRoamingFromRegistration");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("voiceRegState", this.mVoiceRegState);
        m.putInt("dataRegState", this.mDataRegState);
        m.putInt("voiceRoamingType", this.mVoiceRoamingType);
        m.putInt("dataRoamingType", this.mDataRoamingType);
        m.putString("operator-alpha-long", this.mVoiceOperatorAlphaLong);
        m.putString("operator-alpha-short", this.mVoiceOperatorAlphaShort);
        m.putString("operator-numeric", this.mVoiceOperatorNumeric);
        m.putString("data-operator-alpha-long", this.mDataOperatorAlphaLong);
        m.putString("data-operator-alpha-short", this.mDataOperatorAlphaShort);
        m.putString("data-operator-numeric", this.mDataOperatorNumeric);
        m.putBoolean("manual", Boolean.valueOf(this.mIsManualNetworkSelection).booleanValue());
        m.putInt("radioTechnology", this.mRilVoiceRadioTechnology);
        m.putInt("dataRadioTechnology", this.mRilDataRadioTechnology);
        m.putBoolean("cssIndicator", this.mCssIndicator);
        m.putInt("networkId", this.mNetworkId);
        m.putInt("systemId", this.mSystemId);
        m.putInt("cdmaRoamingIndicator", this.mCdmaRoamingIndicator);
        m.putInt("cdmaDefaultRoamingIndicator", this.mCdmaDefaultRoamingIndicator);
        m.putBoolean("emergencyOnly", Boolean.valueOf(this.mIsEmergencyOnly).booleanValue());
        m.putBoolean("isDataRoamingFromRegistration", Boolean.valueOf(this.mIsDataRoamingFromRegistration).booleanValue());
    }

    public void setRilVoiceRadioTechnology(int rt) {
        this.mRilVoiceRadioTechnology = rt;
    }

    public void setRilDataRadioTechnology(int rt) {
        this.mRilDataRadioTechnology = rt;
    }

    public void setCssIndicator(int css) {
        boolean z = DBG;
        if (css != 0) {
            z = true;
        }
        this.mCssIndicator = z;
    }

    public void setSystemAndNetworkId(int systemId, int networkId) {
        this.mSystemId = systemId;
        this.mNetworkId = networkId;
    }

    public int getRilVoiceRadioTechnology() {
        return this.mRilVoiceRadioTechnology;
    }

    public int getRilDataRadioTechnology() {
        return this.mRilDataRadioTechnology;
    }

    public int getRadioTechnology() {
        if (getRilDataRadioTechnology() == 0) {
            return getRilVoiceRadioTechnology();
        }
        return getRilDataRadioTechnology();
    }

    private int rilRadioTechnologyToNetworkType(int rt) {
        switch (rt) {
            case STATE_OUT_OF_SERVICE /*1*/:
                return STATE_OUT_OF_SERVICE;
            case STATE_EMERGENCY_ONLY /*2*/:
                return STATE_EMERGENCY_ONLY;
            case STATE_POWER_OFF /*3*/:
                return STATE_POWER_OFF;
            case RIL_REG_STATE_UNKNOWN /*4*/:
            case RIL_REG_STATE_ROAMING /*5*/:
                return RIL_REG_STATE_UNKNOWN;
            case RIL_RADIO_TECHNOLOGY_1xRTT /*6*/:
                return RIL_RADIO_TECHNOLOGY_EVDO_0;
            case RIL_RADIO_TECHNOLOGY_EVDO_0 /*7*/:
                return RIL_REG_STATE_ROAMING;
            case RIL_RADIO_TECHNOLOGY_EVDO_A /*8*/:
                return RIL_RADIO_TECHNOLOGY_1xRTT;
            case RIL_RADIO_TECHNOLOGY_HSDPA /*9*/:
                return RIL_RADIO_TECHNOLOGY_EVDO_A;
            case RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED /*10*/:
                return RIL_RADIO_TECHNOLOGY_HSDPA;
            case RIL_RADIO_TECHNOLOGY_HSPA /*11*/:
                return RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED;
            case RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED /*12*/:
                return RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED;
            case RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED /*13*/:
                return RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED;
            case RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED /*14*/:
                return RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED;
            case RIL_RADIO_TECHNOLOGY_HSPAP /*15*/:
                return RIL_RADIO_TECHNOLOGY_HSPAP;
            case RIL_RADIO_TECHNOLOGY_GSM /*16*/:
                return RIL_RADIO_TECHNOLOGY_GSM;
            case RIL_RADIO_TECHNOLOGY_TD_SCDMA /*17*/:
                return RIL_RADIO_TECHNOLOGY_TD_SCDMA;
            case RIL_RADIO_TECHNOLOGY_IWLAN /*18*/:
                return RIL_RADIO_TECHNOLOGY_IWLAN;
            case RIL_RADIO_TECHNOLOGY_DCHSPAP /*30*/:
                return RIL_RADIO_TECHNOLOGY_DCHSPAP;
            case RIL_RADIO_TECHNOLOGY_LTE_CA /*31*/:
                return RIL_RADIO_TECHNOLOGY_LTE_CA;
            default:
                return STATE_IN_SERVICE;
        }
    }

    public int getNetworkType() {
        if (this.mRilDataRadioTechnology != 0) {
            return rilRadioTechnologyToNetworkType(this.mRilDataRadioTechnology);
        }
        return rilRadioTechnologyToNetworkType(this.mRilVoiceRadioTechnology);
    }

    public int getDataNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilDataRadioTechnology);
    }

    public int getVoiceNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilVoiceRadioTechnology);
    }

    public int getCssIndicator() {
        return this.mCssIndicator ? STATE_OUT_OF_SERVICE : STATE_IN_SERVICE;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public int getSystemId() {
        return this.mSystemId;
    }

    public static boolean isGsm(int radioTechnology) {
        if (radioTechnology == STATE_OUT_OF_SERVICE || radioTechnology == STATE_EMERGENCY_ONLY || radioTechnology == STATE_POWER_OFF || radioTechnology == RIL_RADIO_TECHNOLOGY_HSDPA || radioTechnology == RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED || radioTechnology == RIL_RADIO_TECHNOLOGY_HSPA || radioTechnology == RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED || radioTechnology == RIL_RADIO_TECHNOLOGY_HSPAP || radioTechnology == RIL_RADIO_TECHNOLOGY_GSM || radioTechnology == RIL_RADIO_TECHNOLOGY_TD_SCDMA || radioTechnology == RIL_RADIO_TECHNOLOGY_IWLAN || radioTechnology == RIL_RADIO_TECHNOLOGY_LTE_CA || radioTechnology == RIL_RADIO_TECHNOLOGY_DCHSPAP || radioTechnology == RIL_RADIO_TECHNOLOGY_IWLAN) {
            return true;
        }
        return DBG;
    }

    public static boolean isCdma(int radioTechnology) {
        if (radioTechnology == RIL_REG_STATE_UNKNOWN || radioTechnology == RIL_REG_STATE_ROAMING || radioTechnology == RIL_RADIO_TECHNOLOGY_1xRTT || radioTechnology == RIL_RADIO_TECHNOLOGY_EVDO_0 || radioTechnology == RIL_RADIO_TECHNOLOGY_EVDO_A || radioTechnology == RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED || radioTechnology == RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED) {
            return true;
        }
        return DBG;
    }

    public static boolean bearerBitmapHasCdma(int radioTechnologyBitmap) {
        return (radioTechnologyBitmap & RIL_RADIO_CDMA_TECHNOLOGY_BITMASK) != 0 ? true : DBG;
    }

    public static boolean bitmaskHasTech(int bearerBitmask, int radioTech) {
        boolean z = true;
        if (bearerBitmask == 0) {
            return true;
        }
        if (radioTech < STATE_OUT_OF_SERVICE) {
            return DBG;
        }
        if (((STATE_OUT_OF_SERVICE << (radioTech - 1)) & bearerBitmask) == 0) {
            z = DBG;
        }
        return z;
    }

    public static int getBitmaskForTech(int radioTech) {
        if (radioTech >= STATE_OUT_OF_SERVICE) {
            return STATE_OUT_OF_SERVICE << (radioTech - 1);
        }
        return STATE_IN_SERVICE;
    }

    public static int getBitmaskFromString(String bearerList) {
        String[] bearers = bearerList.split("\\|");
        int bearerBitmask = STATE_IN_SERVICE;
        int length = bearers.length;
        int i = STATE_IN_SERVICE;
        while (i < length) {
            try {
                int bearerInt = Integer.parseInt(bearers[i].trim());
                if (bearerInt == 0) {
                    return STATE_IN_SERVICE;
                }
                bearerBitmask |= getBitmaskForTech(bearerInt);
                i += STATE_OUT_OF_SERVICE;
            } catch (NumberFormatException e) {
                return STATE_IN_SERVICE;
            }
        }
        return bearerBitmask;
    }

    public static boolean isHrpd1X(int radioTechnology) {
        if (radioTechnology == RIL_REG_STATE_UNKNOWN || radioTechnology == RIL_REG_STATE_ROAMING || radioTechnology == RIL_RADIO_TECHNOLOGY_1xRTT || radioTechnology == RIL_RADIO_TECHNOLOGY_EVDO_0 || radioTechnology == RIL_RADIO_TECHNOLOGY_EVDO_A || radioTechnology == RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED) {
            return true;
        }
        return DBG;
    }

    public static ServiceState mergeServiceStates(ServiceState baseSs, ServiceState voiceSs) {
        if (voiceSs.mVoiceRegState != 0) {
            return baseSs;
        }
        ServiceState newSs = new ServiceState(baseSs);
        newSs.mVoiceRegState = voiceSs.mVoiceRegState;
        newSs.mIsEmergencyOnly = DBG;
        return newSs;
    }
}
