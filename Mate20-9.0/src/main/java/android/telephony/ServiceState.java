package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.CalendarContract;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceState implements Parcelable {
    public static final Parcelable.Creator<ServiceState> CREATOR = new Parcelable.Creator<ServiceState>() {
        public ServiceState createFromParcel(Parcel in) {
            return new ServiceState(in);
        }

        public ServiceState[] newArray(int size) {
            return new ServiceState[size];
        }
    };
    static final boolean DBG = false;
    public static final int DUPLEX_MODE_FDD = 1;
    public static final int DUPLEX_MODE_TDD = 2;
    public static final int DUPLEX_MODE_UNKNOWN = 0;
    static final String LOG_TAG = "PHONE";
    private static final int NEXT_RIL_RADIO_TECHNOLOGY = 21;
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
    public static final int RIL_RADIO_TECHNOLOGY_LTE_CA = 19;
    public static final int RIL_RADIO_TECHNOLOGY_NR = 20;
    public static final int RIL_RADIO_TECHNOLOGY_TD_SCDMA = 17;
    public static final int RIL_RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    public static final int STATE_EMERGENCY_ONLY = 2;
    public static final int STATE_IN_SERVICE = 0;
    public static final int STATE_OUT_OF_SERVICE = 1;
    public static final int STATE_POWER_OFF = 3;
    public static final int UNKNOWN_ID = -1;
    static final boolean VDBG = false;
    private int mCdmaDefaultRoamingIndicator;
    private int mCdmaEriIconIndex;
    private int mCdmaEriIconMode;
    private int mCdmaRoamingIndicator;
    private int[] mCellBandwidths = new int[0];
    private int mChannelNumber;
    private boolean mCssIndicator;
    private String mDataOperatorAlphaLong;
    private String mDataOperatorAlphaShort;
    private String mDataOperatorNumeric;
    private int mDataRegState = 1;
    private int mDataRoamingType;
    private boolean mIsDataRoamingFromRegistration;
    private boolean mIsEmergencyOnly;
    private boolean mIsManualNetworkSelection;
    private boolean mIsUsingCarrierAggregation;
    private int mLteEarfcnRsrpBoost = 0;
    private int mNetworkId;
    private List<NetworkRegistrationState> mNetworkRegistrationStates = new ArrayList();
    private int mRilDataRadioTechnology;
    private int mRilVoiceRadioTechnology;
    private int mSystemId;
    private String mVoiceOperatorAlphaLong;
    private String mVoiceOperatorAlphaShort;
    private String mVoiceOperatorNumeric;
    private int mVoiceRegState = 1;
    private int mVoiceRoamingType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DuplexMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RilRadioTechnology {
    }

    public static final String getRoamingLogString(int roamingType) {
        switch (roamingType) {
            case 0:
                return CalendarContract.CalendarCache.TIMEZONE_TYPE_HOME;
            case 1:
                return "roaming";
            case 2:
                return "Domestic Roaming";
            case 3:
                return "International Roaming";
            default:
                return "UNKNOWN";
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
    }

    public ServiceState(ServiceState s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(ServiceState s) {
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
        this.mIsUsingCarrierAggregation = s.mIsUsingCarrierAggregation;
        this.mChannelNumber = s.mChannelNumber;
        this.mCellBandwidths = Arrays.copyOf(s.mCellBandwidths, s.mCellBandwidths.length);
        this.mLteEarfcnRsrpBoost = s.mLteEarfcnRsrpBoost;
        this.mNetworkRegistrationStates = new ArrayList(s.mNetworkRegistrationStates);
    }

    public ServiceState(Parcel in) {
        boolean z = true;
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
        this.mIsManualNetworkSelection = in.readInt() != 0;
        this.mRilVoiceRadioTechnology = in.readInt();
        this.mRilDataRadioTechnology = in.readInt();
        this.mCssIndicator = in.readInt() != 0;
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mCdmaRoamingIndicator = in.readInt();
        this.mCdmaDefaultRoamingIndicator = in.readInt();
        this.mCdmaEriIconIndex = in.readInt();
        this.mCdmaEriIconMode = in.readInt();
        this.mIsEmergencyOnly = in.readInt() != 0;
        this.mIsDataRoamingFromRegistration = in.readInt() != 0;
        this.mIsUsingCarrierAggregation = in.readInt() == 0 ? false : z;
        this.mLteEarfcnRsrpBoost = in.readInt();
        this.mNetworkRegistrationStates = new ArrayList();
        in.readList(this.mNetworkRegistrationStates, NetworkRegistrationState.class.getClassLoader());
        this.mChannelNumber = in.readInt();
        this.mCellBandwidths = in.createIntArray();
    }

    public void writeToParcel(Parcel out, int flags) {
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
        out.writeInt(this.mIsManualNetworkSelection ? 1 : 0);
        out.writeInt(this.mRilVoiceRadioTechnology);
        out.writeInt(this.mRilDataRadioTechnology);
        out.writeInt(this.mCssIndicator ? 1 : 0);
        out.writeInt(this.mNetworkId);
        out.writeInt(this.mSystemId);
        out.writeInt(this.mCdmaRoamingIndicator);
        out.writeInt(this.mCdmaDefaultRoamingIndicator);
        out.writeInt(this.mCdmaEriIconIndex);
        out.writeInt(this.mCdmaEriIconMode);
        out.writeInt(this.mIsEmergencyOnly ? 1 : 0);
        out.writeInt(this.mIsDataRoamingFromRegistration ? 1 : 0);
        out.writeInt(this.mIsUsingCarrierAggregation ? 1 : 0);
        out.writeInt(this.mLteEarfcnRsrpBoost);
        out.writeList(this.mNetworkRegistrationStates);
        out.writeInt(this.mChannelNumber);
        out.writeIntArray(this.mCellBandwidths);
    }

    public int describeContents() {
        return 0;
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

    public int getDuplexMode() {
        if (!isLte(this.mRilDataRadioTechnology)) {
            return 0;
        }
        return AccessNetworkUtils.getDuplexModeForEutranBand(AccessNetworkUtils.getOperatingBandForEarfcn(this.mChannelNumber));
    }

    public int getChannelNumber() {
        return this.mChannelNumber;
    }

    public int[] getCellBandwidths() {
        return this.mCellBandwidths == null ? new int[0] : this.mCellBandwidths;
    }

    public boolean getRoaming() {
        return getVoiceRoaming() || getDataRoaming();
    }

    public boolean getVoiceRoaming() {
        return this.mVoiceRoamingType != 0;
    }

    public int getVoiceRoamingType() {
        return this.mVoiceRoamingType;
    }

    public boolean getDataRoaming() {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.national_r_always_on", false)) {
            if (!(this.mDataRoamingType == 0 || this.mDataRoamingType == 2)) {
                z = true;
            }
            return z;
        }
        if (this.mDataRoamingType != 0) {
            z = true;
        }
        return z;
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

    public String getOperatorAlpha() {
        if (TextUtils.isEmpty(this.mVoiceOperatorAlphaLong)) {
            return this.mVoiceOperatorAlphaShort;
        }
        return this.mVoiceOperatorAlphaLong;
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
        int i = 0;
        int hashCode = (this.mVoiceRegState * 31) + (this.mDataRegState * 37) + this.mVoiceRoamingType + this.mDataRoamingType + this.mChannelNumber + Arrays.hashCode(this.mCellBandwidths) + (this.mIsManualNetworkSelection ? 1 : 0) + (this.mVoiceOperatorAlphaLong == null ? 0 : this.mVoiceOperatorAlphaLong.hashCode()) + (this.mVoiceOperatorAlphaShort == null ? 0 : this.mVoiceOperatorAlphaShort.hashCode()) + (this.mVoiceOperatorNumeric == null ? 0 : this.mVoiceOperatorNumeric.hashCode()) + (this.mDataOperatorAlphaLong == null ? 0 : this.mDataOperatorAlphaLong.hashCode()) + (this.mDataOperatorAlphaShort == null ? 0 : this.mDataOperatorAlphaShort.hashCode());
        if (this.mDataOperatorNumeric != null) {
            i = this.mDataOperatorNumeric.hashCode();
        }
        return hashCode + i + this.mCdmaRoamingIndicator + this.mCdmaDefaultRoamingIndicator + (this.mIsEmergencyOnly ? 1 : 0) + (this.mIsDataRoamingFromRegistration ? 1 : 0);
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            ServiceState s = (ServiceState) o;
            if (o == null) {
                return false;
            }
            if (this.mVoiceRegState == s.mVoiceRegState && this.mDataRegState == s.mDataRegState && this.mIsManualNetworkSelection == s.mIsManualNetworkSelection && this.mVoiceRoamingType == s.mVoiceRoamingType && this.mDataRoamingType == s.mDataRoamingType && this.mChannelNumber == s.mChannelNumber && Arrays.equals(this.mCellBandwidths, s.mCellBandwidths) && equalsHandlesNulls(this.mVoiceOperatorAlphaLong, s.mVoiceOperatorAlphaLong) && equalsHandlesNulls(this.mVoiceOperatorAlphaShort, s.mVoiceOperatorAlphaShort) && equalsHandlesNulls(this.mVoiceOperatorNumeric, s.mVoiceOperatorNumeric) && equalsHandlesNulls(this.mDataOperatorAlphaLong, s.mDataOperatorAlphaLong) && equalsHandlesNulls(this.mDataOperatorAlphaShort, s.mDataOperatorAlphaShort) && equalsHandlesNulls(this.mDataOperatorNumeric, s.mDataOperatorNumeric) && equalsHandlesNulls(Integer.valueOf(this.mRilVoiceRadioTechnology), Integer.valueOf(s.mRilVoiceRadioTechnology)) && equalsHandlesNulls(Integer.valueOf(this.mRilDataRadioTechnology), Integer.valueOf(s.mRilDataRadioTechnology)) && equalsHandlesNulls(Boolean.valueOf(this.mCssIndicator), Boolean.valueOf(s.mCssIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mNetworkId), Integer.valueOf(s.mNetworkId)) && equalsHandlesNulls(Integer.valueOf(this.mSystemId), Integer.valueOf(s.mSystemId)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaRoamingIndicator), Integer.valueOf(s.mCdmaRoamingIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaDefaultRoamingIndicator), Integer.valueOf(s.mCdmaDefaultRoamingIndicator)) && this.mIsEmergencyOnly == s.mIsEmergencyOnly && this.mIsDataRoamingFromRegistration == s.mIsDataRoamingFromRegistration && this.mIsUsingCarrierAggregation == s.mIsUsingCarrierAggregation && this.mNetworkRegistrationStates.containsAll(s.mNetworkRegistrationStates)) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static String rilRadioTechnologyToString(int rt) {
        if (rt == 30) {
            return "DC-HSPA+";
        }
        switch (rt) {
            case 0:
                return "Unknown";
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA-IS95A";
            case 5:
                return "CDMA-IS95B";
            case 6:
                return "1xRTT";
            case 7:
                return "EvDo-rev.0";
            case 8:
                return "EvDo-rev.A";
            case 9:
                return "HSDPA";
            case 10:
                return "HSUPA";
            case 11:
                return "HSPA";
            case 12:
                return "EvDo-rev.B";
            case 13:
                return "eHRPD";
            case 14:
                return "LTE";
            case 15:
                return "HSPAP";
            case 16:
                return "GSM";
            case 17:
                return "TD-SCDMA";
            case 18:
                return "IWLAN";
            case 19:
                return "LTE-CA";
            case 20:
                return "NR";
            default:
                Rlog.w(LOG_TAG, "Unexpected radioTechnology=" + rt);
                return "Unexpected";
        }
    }

    public static String rilServiceStateToString(int serviceState) {
        switch (serviceState) {
            case 0:
                return "IN_SERVICE";
            case 1:
                return "OUT_OF_SERVICE";
            case 2:
                return "EMERGENCY_ONLY";
            case 3:
                return "POWER_OFF";
            default:
                return "UNKNOWN";
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{mVoiceRegState=");
        sb.append(this.mVoiceRegState);
        sb.append("(" + rilServiceStateToString(this.mVoiceRegState) + ")");
        sb.append(", mDataRegState=");
        sb.append(this.mDataRegState);
        sb.append("(" + rilServiceStateToString(this.mDataRegState) + ")");
        sb.append(", mChannelNumber=");
        sb.append(this.mChannelNumber);
        sb.append(", duplexMode()=");
        sb.append(getDuplexMode());
        sb.append(", mCellBandwidths=");
        sb.append(Arrays.toString(this.mCellBandwidths));
        sb.append(", mVoiceRoamingType=");
        sb.append(getRoamingLogString(this.mVoiceRoamingType));
        sb.append(", mDataRoamingType=");
        sb.append(getRoamingLogString(this.mDataRoamingType));
        sb.append(", mVoiceOperatorAlphaLong=");
        sb.append(this.mVoiceOperatorAlphaLong);
        sb.append(", mVoiceOperatorAlphaShort=");
        sb.append(this.mVoiceOperatorAlphaShort);
        sb.append(", mDataOperatorAlphaLong=");
        sb.append(this.mDataOperatorAlphaLong);
        sb.append(", mDataOperatorAlphaShort=");
        sb.append(this.mDataOperatorAlphaShort);
        sb.append(", isManualNetworkSelection=");
        sb.append(this.mIsManualNetworkSelection);
        sb.append(this.mIsManualNetworkSelection ? "(manual)" : "(automatic)");
        sb.append(", mRilVoiceRadioTechnology=");
        sb.append(this.mRilVoiceRadioTechnology);
        sb.append("(" + rilRadioTechnologyToString(this.mRilVoiceRadioTechnology) + ")");
        sb.append(", mRilDataRadioTechnology=");
        sb.append(this.mRilDataRadioTechnology);
        sb.append("(" + rilRadioTechnologyToString(this.mRilDataRadioTechnology) + ")");
        sb.append(", mCssIndicator=");
        sb.append(this.mCssIndicator ? "supported" : "unsupported");
        sb.append(", mNetworkId=");
        sb.append(this.mNetworkId);
        sb.append(", mSystemId=");
        sb.append(this.mSystemId);
        sb.append(", mCdmaRoamingIndicator=");
        sb.append(this.mCdmaRoamingIndicator);
        sb.append(", mCdmaDefaultRoamingIndicator=");
        sb.append(this.mCdmaDefaultRoamingIndicator);
        sb.append(", mIsEmergencyOnly=");
        sb.append(this.mIsEmergencyOnly);
        sb.append(", mIsDataRoamingFromRegistration=");
        sb.append(this.mIsDataRoamingFromRegistration);
        sb.append(", mIsUsingCarrierAggregation=");
        sb.append(this.mIsUsingCarrierAggregation);
        sb.append(", mLteEarfcnRsrpBoost=");
        sb.append(this.mLteEarfcnRsrpBoost);
        sb.append(", mNetworkRegistrationStates=");
        sb.append(this.mNetworkRegistrationStates);
        sb.append("}");
        return sb.toString();
    }

    private void setNullState(int state) {
        this.mVoiceRegState = state;
        this.mDataRegState = state;
        this.mVoiceRoamingType = 0;
        this.mDataRoamingType = 0;
        this.mChannelNumber = -1;
        this.mCellBandwidths = new int[0];
        this.mVoiceOperatorAlphaLong = null;
        this.mVoiceOperatorAlphaShort = null;
        this.mVoiceOperatorNumeric = null;
        this.mDataOperatorAlphaLong = null;
        this.mDataOperatorAlphaShort = null;
        this.mDataOperatorNumeric = null;
        this.mIsManualNetworkSelection = false;
        this.mRilVoiceRadioTechnology = 0;
        this.mRilDataRadioTechnology = 0;
        this.mCssIndicator = false;
        this.mNetworkId = -1;
        this.mSystemId = -1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mIsEmergencyOnly = false;
        this.mIsDataRoamingFromRegistration = false;
        this.mIsUsingCarrierAggregation = false;
        this.mLteEarfcnRsrpBoost = 0;
        this.mNetworkRegistrationStates = new ArrayList();
    }

    public void setStateOutOfService() {
        setNullState(1);
    }

    public void setStateOff() {
        setNullState(3);
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

    public void setCellBandwidths(int[] bandwidths) {
        this.mCellBandwidths = bandwidths;
    }

    public void setChannelNumber(int channelNumber) {
        this.mChannelNumber = channelNumber;
    }

    public void setRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming;
        this.mDataRoamingType = this.mVoiceRoamingType;
    }

    public void setVoiceRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming;
    }

    public void setVoiceRoamingType(int type) {
        this.mVoiceRoamingType = type;
    }

    public void setDataRoaming(boolean dataRoaming) {
        this.mDataRoamingType = dataRoaming;
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
            return b == null;
        }
        return a.equals(b);
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
        this.mIsUsingCarrierAggregation = m.getBoolean("isUsingCarrierAggregation");
        this.mLteEarfcnRsrpBoost = m.getInt("LteEarfcnRsrpBoost");
        this.mChannelNumber = m.getInt("ChannelNumber");
        this.mCellBandwidths = m.getIntArray("CellBandwidths");
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
        m.putBoolean("manual", this.mIsManualNetworkSelection);
        m.putInt("radioTechnology", this.mRilVoiceRadioTechnology);
        m.putInt("dataRadioTechnology", this.mRilDataRadioTechnology);
        m.putBoolean("cssIndicator", this.mCssIndicator);
        m.putInt("networkId", this.mNetworkId);
        m.putInt("systemId", this.mSystemId);
        m.putInt("cdmaRoamingIndicator", this.mCdmaRoamingIndicator);
        m.putInt("cdmaDefaultRoamingIndicator", this.mCdmaDefaultRoamingIndicator);
        m.putBoolean("emergencyOnly", this.mIsEmergencyOnly);
        m.putBoolean("isDataRoamingFromRegistration", this.mIsDataRoamingFromRegistration);
        m.putBoolean("isUsingCarrierAggregation", this.mIsUsingCarrierAggregation);
        m.putInt("LteEarfcnRsrpBoost", this.mLteEarfcnRsrpBoost);
        m.putInt("ChannelNumber", this.mChannelNumber);
        m.putIntArray("CellBandwidths", this.mCellBandwidths);
    }

    public void setRilVoiceRadioTechnology(int rt) {
        if (rt == 19) {
            rt = 14;
        }
        this.mRilVoiceRadioTechnology = rt;
    }

    public void setRilDataRadioTechnology(int rt) {
        if (rt == 19) {
            rt = 14;
            this.mIsUsingCarrierAggregation = true;
        } else {
            this.mIsUsingCarrierAggregation = false;
        }
        this.mRilDataRadioTechnology = rt;
    }

    public boolean isUsingCarrierAggregation() {
        return this.mIsUsingCarrierAggregation;
    }

    public void setIsUsingCarrierAggregation(boolean ca) {
        this.mIsUsingCarrierAggregation = ca;
    }

    public int getLteEarfcnRsrpBoost() {
        return this.mLteEarfcnRsrpBoost;
    }

    public void setLteEarfcnRsrpBoost(int LteEarfcnRsrpBoost) {
        this.mLteEarfcnRsrpBoost = LteEarfcnRsrpBoost;
    }

    public void setCssIndicator(int css) {
        this.mCssIndicator = css != 0;
    }

    public void setCdmaSystemAndNetworkId(int systemId, int networkId) {
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

    public static int rilRadioTechnologyToNetworkType(int rt) {
        if (rt == 30) {
            return 30;
        }
        switch (rt) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
            case 5:
                return 4;
            case 6:
                return 7;
            case 7:
                return 5;
            case 8:
                return 6;
            case 9:
                return 8;
            case 10:
                return 9;
            case 11:
                return 10;
            case 12:
                return 12;
            case 13:
                return 14;
            case 14:
                return 13;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 17;
            case 18:
                return 18;
            case 19:
                return 19;
            case 20:
                return 20;
            default:
                return 0;
        }
    }

    public static int rilRadioTechnologyToAccessNetworkType(int rt) {
        switch (rt) {
            case 1:
            case 2:
            case 16:
                return 1;
            case 3:
            case 9:
            case 10:
            case 11:
            case 15:
            case 17:
                return 2;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 12:
            case 13:
                return 4;
            case 14:
            case 19:
                return 3;
            case 18:
                return 5;
            case 20:
                return 6;
            default:
                return 0;
        }
    }

    public static int networkTypeToRilRadioTechnology(int networkType) {
        switch (networkType) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 7;
            case 6:
                return 8;
            case 7:
                return 6;
            case 8:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 12:
                return 12;
            case 13:
                return 14;
            case 14:
                return 13;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 17;
            case 18:
                return 18;
            case 19:
                return 19;
            case 20:
                return 20;
            default:
                return 0;
        }
    }

    public int getDataNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilDataRadioTechnology);
    }

    public int getVoiceNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilVoiceRadioTechnology);
    }

    public int getCssIndicator() {
        return this.mCssIndicator ? 1 : 0;
    }

    public int getCdmaNetworkId() {
        return this.mNetworkId;
    }

    public int getCdmaSystemId() {
        return this.mSystemId;
    }

    public static boolean isGsm(int radioTechnology) {
        return radioTechnology == 1 || radioTechnology == 2 || radioTechnology == 3 || radioTechnology == 9 || radioTechnology == 10 || radioTechnology == 11 || radioTechnology == 14 || radioTechnology == 15 || radioTechnology == 16 || radioTechnology == 17 || radioTechnology == 18 || radioTechnology == 19 || radioTechnology == 30 || radioTechnology == 18 || radioTechnology == 20;
    }

    public static boolean isCdma(int radioTechnology) {
        return radioTechnology == 4 || radioTechnology == 5 || radioTechnology == 6 || radioTechnology == 7 || radioTechnology == 8 || radioTechnology == 12 || radioTechnology == 13;
    }

    public static boolean isLte(int radioTechnology) {
        return radioTechnology == 14 || radioTechnology == 19;
    }

    public static boolean bearerBitmapHasCdma(int radioTechnologyBitmap) {
        return (6392 & radioTechnologyBitmap) != 0;
    }

    public static boolean bitmaskHasTech(int bearerBitmask, int radioTech) {
        boolean z = true;
        if (bearerBitmask == 0) {
            return true;
        }
        if (radioTech < 1) {
            return false;
        }
        if (((1 << (radioTech - 1)) & bearerBitmask) == 0) {
            z = false;
        }
        return z;
    }

    public static int getBitmaskForTech(int radioTech) {
        if (radioTech >= 1) {
            return 1 << (radioTech - 1);
        }
        return 0;
    }

    public static int getBitmaskFromString(String bearerList) {
        String[] bearers = bearerList.split("\\|");
        int length = bearers.length;
        int bearerBitmask = 0;
        int bearerBitmask2 = 0;
        while (bearerBitmask2 < length) {
            try {
                int bearerInt = Integer.parseInt(bearers[bearerBitmask2].trim());
                if (bearerInt == 0) {
                    return 0;
                }
                bearerBitmask |= getBitmaskForTech(bearerInt);
                bearerBitmask2++;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return bearerBitmask;
    }

    public static boolean isHrpd1X(int radioTechnology) {
        return radioTechnology == 4 || radioTechnology == 5 || radioTechnology == 6 || radioTechnology == 7 || radioTechnology == 8 || radioTechnology == 12;
    }

    public static int convertNetworkTypeBitmaskToBearerBitmask(int networkTypeBitmask) {
        if (networkTypeBitmask == 0) {
            return 0;
        }
        int bearerBitmask = 0;
        for (int bearerInt = 0; bearerInt < 21; bearerInt++) {
            if (bitmaskHasTech(networkTypeBitmask, rilRadioTechnologyToNetworkType(bearerInt))) {
                bearerBitmask |= getBitmaskForTech(bearerInt);
            }
        }
        return bearerBitmask;
    }

    public static int convertBearerBitmaskToNetworkTypeBitmask(int bearerBitmask) {
        if (bearerBitmask == 0) {
            return 0;
        }
        int networkTypeBitmask = 0;
        for (int bearerInt = 0; bearerInt < 21; bearerInt++) {
            if (bitmaskHasTech(bearerBitmask, bearerInt)) {
                networkTypeBitmask |= getBitmaskForTech(rilRadioTechnologyToNetworkType(bearerInt));
            }
        }
        return networkTypeBitmask;
    }

    public static ServiceState mergeServiceStates(ServiceState baseSs, ServiceState voiceSs) {
        if (voiceSs.mVoiceRegState != 0) {
            return baseSs;
        }
        ServiceState newSs = new ServiceState(baseSs);
        newSs.mVoiceRegState = voiceSs.mVoiceRegState;
        newSs.mIsEmergencyOnly = false;
        return newSs;
    }

    public List<NetworkRegistrationState> getNetworkRegistrationStates() {
        ArrayList arrayList;
        synchronized (this.mNetworkRegistrationStates) {
            arrayList = new ArrayList(this.mNetworkRegistrationStates);
        }
        return arrayList;
    }

    public List<NetworkRegistrationState> getNetworkRegistrationStates(int transportType) {
        List<NetworkRegistrationState> list = new ArrayList<>();
        synchronized (this.mNetworkRegistrationStates) {
            for (NetworkRegistrationState networkRegistrationState : this.mNetworkRegistrationStates) {
                if (networkRegistrationState.getTransportType() == transportType) {
                    list.add(networkRegistrationState);
                }
            }
        }
        return list;
    }

    public NetworkRegistrationState getNetworkRegistrationStates(int transportType, int domain) {
        synchronized (this.mNetworkRegistrationStates) {
            for (NetworkRegistrationState networkRegistrationState : this.mNetworkRegistrationStates) {
                if (networkRegistrationState.getTransportType() == transportType && networkRegistrationState.getDomain() == domain) {
                    return networkRegistrationState;
                }
            }
            return null;
        }
    }

    public void addNetworkRegistrationState(NetworkRegistrationState regState) {
        if (regState != null) {
            synchronized (this.mNetworkRegistrationStates) {
                int i = 0;
                while (true) {
                    if (i >= this.mNetworkRegistrationStates.size()) {
                        break;
                    }
                    NetworkRegistrationState curRegState = this.mNetworkRegistrationStates.get(i);
                    if (curRegState.getTransportType() == regState.getTransportType() && curRegState.getDomain() == regState.getDomain()) {
                        this.mNetworkRegistrationStates.remove(i);
                        break;
                    }
                    i++;
                }
                this.mNetworkRegistrationStates.add(regState);
            }
        }
    }

    public int getNsaState() {
        NetworkRegistrationState regState = getNetworkRegistrationStates(1, 2);
        if (regState != null) {
            return regState.getNsaState();
        }
        Rlog.d(LOG_TAG, "reg state is null");
        return 0;
    }
}
