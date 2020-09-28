package android.telephony;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import com.android.internal.telephony.IccCardConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SystemApi
public final class NetworkRegistrationInfo implements Parcelable {
    public static final Parcelable.Creator<NetworkRegistrationInfo> CREATOR = new Parcelable.Creator<NetworkRegistrationInfo>() {
        /* class android.telephony.NetworkRegistrationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkRegistrationInfo createFromParcel(Parcel source) {
            return new NetworkRegistrationInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public NetworkRegistrationInfo[] newArray(int size) {
            return new NetworkRegistrationInfo[size];
        }
    };
    public static final int DOMAIN_CS = 1;
    public static final int DOMAIN_PS = 2;
    public static final int NR_STATE_CONNECTED = 3;
    public static final int NR_STATE_NONE = -1;
    public static final int NR_STATE_NOT_RESTRICTED = 2;
    public static final int NR_STATE_RESTRICTED = 1;
    public static final int NR_STATUS_UNKNOWN = 0;
    public static final int REGISTRATION_STATE_DENIED = 3;
    public static final int REGISTRATION_STATE_HOME = 1;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_OR_SEARCHING = 0;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_SEARCHING = 2;
    public static final int REGISTRATION_STATE_ROAMING = 5;
    public static final int REGISTRATION_STATE_UNKNOWN = 4;
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int SERVICE_TYPE_DATA = 2;
    public static final int SERVICE_TYPE_EMERGENCY = 5;
    public static final int SERVICE_TYPE_SMS = 3;
    public static final int SERVICE_TYPE_UNKNOWN = 0;
    public static final int SERVICE_TYPE_VIDEO = 4;
    public static final int SERVICE_TYPE_VOICE = 1;
    private int mAccessNetworkTechnology;
    private final ArrayList<Integer> mAvailableServices;
    private CellIdentity mCellIdentity;
    private int mConfigRadioTech;
    private DataSpecificRegistrationInfo mDataSpecificInfo;
    private final int mDomain;
    private final boolean mEmergencyOnly;
    private int mNrState;
    private int mNsaState;
    private final int mRegistrationState;
    private final int mRejectCause;
    private int mRoamingType;
    private final int mTransportType;
    private VoiceSpecificRegistrationInfo mVoiceSpecificInfo;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Domain {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface NRState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RegistrationState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceType {
    }

    private NetworkRegistrationInfo(int domain, int transportType, int registrationState, int accessNetworkTechnology, int rejectCause, boolean emergencyOnly, List<Integer> availableServices, CellIdentity cellIdentity) {
        int i = 0;
        this.mNsaState = 0;
        this.mConfigRadioTech = 0;
        this.mDomain = domain;
        this.mTransportType = transportType;
        this.mRegistrationState = registrationState;
        this.mRoamingType = registrationState == 5 ? 1 : i;
        this.mAccessNetworkTechnology = accessNetworkTechnology;
        this.mRejectCause = rejectCause;
        this.mAvailableServices = availableServices != null ? new ArrayList<>(availableServices) : new ArrayList<>();
        this.mCellIdentity = cellIdentity;
        this.mEmergencyOnly = emergencyOnly;
        this.mNrState = -1;
    }

    public NetworkRegistrationInfo(int domain, int transportType, int registrationState, int accessNetworkTechnology, int rejectCause, boolean emergencyOnly, List<Integer> availableServices, CellIdentity cellIdentity, boolean cssSupported, int roamingIndicator, int systemIsInPrl, int defaultRoamingIndicator) {
        this(domain, transportType, registrationState, accessNetworkTechnology, rejectCause, emergencyOnly, availableServices, cellIdentity);
        this.mVoiceSpecificInfo = new VoiceSpecificRegistrationInfo(cssSupported, roamingIndicator, systemIsInPrl, defaultRoamingIndicator);
    }

    public NetworkRegistrationInfo(int domain, int transportType, int registrationState, int accessNetworkTechnology, int rejectCause, boolean emergencyOnly, List<Integer> availableServices, CellIdentity cellIdentity, int maxDataCalls, boolean isDcNrRestricted, boolean isNrAvailable, boolean isEndcAvailable, LteVopsSupportInfo lteVopsSupportInfo, boolean isUsingCarrierAggregation) {
        this(domain, transportType, registrationState, accessNetworkTechnology, rejectCause, emergencyOnly, availableServices, cellIdentity);
        this.mDataSpecificInfo = new DataSpecificRegistrationInfo(maxDataCalls, isDcNrRestricted, isNrAvailable, isEndcAvailable, lteVopsSupportInfo, isUsingCarrierAggregation);
        updateNrState(this.mDataSpecificInfo);
    }

    private NetworkRegistrationInfo(Parcel source) {
        this.mNsaState = 0;
        this.mConfigRadioTech = 0;
        this.mDomain = source.readInt();
        this.mTransportType = source.readInt();
        this.mRegistrationState = source.readInt();
        this.mRoamingType = source.readInt();
        this.mAccessNetworkTechnology = source.readInt();
        this.mRejectCause = source.readInt();
        this.mEmergencyOnly = source.readBoolean();
        this.mAvailableServices = new ArrayList<>();
        source.readList(this.mAvailableServices, Integer.class.getClassLoader());
        this.mCellIdentity = (CellIdentity) source.readParcelable(CellIdentity.class.getClassLoader());
        this.mVoiceSpecificInfo = (VoiceSpecificRegistrationInfo) source.readParcelable(VoiceSpecificRegistrationInfo.class.getClassLoader());
        this.mDataSpecificInfo = (DataSpecificRegistrationInfo) source.readParcelable(DataSpecificRegistrationInfo.class.getClassLoader());
        this.mNrState = source.readInt();
        this.mNsaState = source.readInt();
        this.mConfigRadioTech = source.readInt();
    }

    public NetworkRegistrationInfo(NetworkRegistrationInfo nri) {
        this.mNsaState = 0;
        this.mConfigRadioTech = 0;
        this.mDomain = nri.mDomain;
        this.mTransportType = nri.mTransportType;
        this.mRegistrationState = nri.mRegistrationState;
        this.mRoamingType = nri.mRoamingType;
        this.mAccessNetworkTechnology = nri.mAccessNetworkTechnology;
        this.mRejectCause = nri.mRejectCause;
        this.mEmergencyOnly = nri.mEmergencyOnly;
        this.mAvailableServices = new ArrayList<>(nri.mAvailableServices);
        if (nri.mCellIdentity != null) {
            Parcel p = Parcel.obtain();
            nri.mCellIdentity.writeToParcel(p, 0);
            p.setDataPosition(0);
            this.mCellIdentity = CellIdentity.CREATOR.createFromParcel(p);
        }
        VoiceSpecificRegistrationInfo voiceSpecificRegistrationInfo = nri.mVoiceSpecificInfo;
        if (voiceSpecificRegistrationInfo != null) {
            this.mVoiceSpecificInfo = new VoiceSpecificRegistrationInfo(voiceSpecificRegistrationInfo);
        }
        DataSpecificRegistrationInfo dataSpecificRegistrationInfo = nri.mDataSpecificInfo;
        if (dataSpecificRegistrationInfo != null) {
            this.mDataSpecificInfo = new DataSpecificRegistrationInfo(dataSpecificRegistrationInfo);
        }
        this.mNrState = nri.mNrState;
        this.mNsaState = nri.mNsaState;
        this.mConfigRadioTech = nri.mConfigRadioTech;
    }

    public int getTransportType() {
        return this.mTransportType;
    }

    public int getDomain() {
        return this.mDomain;
    }

    public int getNrState() {
        return this.mNrState;
    }

    public void setNrState(int nrState) {
        this.mNrState = nrState;
    }

    public int getRegistrationState() {
        return this.mRegistrationState;
    }

    public boolean isRoaming() {
        return this.mRoamingType != 0;
    }

    public boolean isInService() {
        int i = this.mRegistrationState;
        return i == 1 || i == 5;
    }

    public void setRoamingType(int roamingType) {
        this.mRoamingType = roamingType;
    }

    public int getRoamingType() {
        return this.mRoamingType;
    }

    public boolean isEmergencyEnabled() {
        return this.mEmergencyOnly;
    }

    public List<Integer> getAvailableServices() {
        return Collections.unmodifiableList(this.mAvailableServices);
    }

    public int getAccessNetworkTechnology() {
        return this.mAccessNetworkTechnology;
    }

    public void setAccessNetworkTechnology(int tech) {
        if (tech == 19) {
            tech = 13;
            DataSpecificRegistrationInfo dataSpecificRegistrationInfo = this.mDataSpecificInfo;
            if (dataSpecificRegistrationInfo != null) {
                dataSpecificRegistrationInfo.setIsUsingCarrierAggregation(true);
            }
        }
        this.mAccessNetworkTechnology = tech;
    }

    public int getRejectCause() {
        return this.mRejectCause;
    }

    public CellIdentity getCellIdentity() {
        return this.mCellIdentity;
    }

    public VoiceSpecificRegistrationInfo getVoiceSpecificInfo() {
        return this.mVoiceSpecificInfo;
    }

    public DataSpecificRegistrationInfo getDataSpecificInfo() {
        return this.mDataSpecificInfo;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static String serviceTypeToString(int serviceType) {
        if (serviceType == 1) {
            return "VOICE";
        }
        if (serviceType == 2) {
            return "DATA";
        }
        if (serviceType == 3) {
            return "SMS";
        }
        if (serviceType == 4) {
            return "VIDEO";
        }
        if (serviceType == 5) {
            return "EMERGENCY";
        }
        return "Unknown service type " + serviceType;
    }

    public static String registrationStateToString(int registrationState) {
        if (registrationState == 0) {
            return "NOT_REG_OR_SEARCHING";
        }
        if (registrationState == 1) {
            return "HOME";
        }
        if (registrationState == 2) {
            return "NOT_REG_SEARCHING";
        }
        if (registrationState == 3) {
            return "DENIED";
        }
        if (registrationState == 4) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (registrationState == 5) {
            return "ROAMING";
        }
        return "Unknown reg state " + registrationState;
    }

    private static String nrStateToString(int nrState) {
        if (nrState == 1) {
            return "RESTRICTED";
        }
        if (nrState == 2) {
            return "NOT_RESTRICTED";
        }
        if (nrState != 3) {
            return KeyProperties.DIGEST_NONE;
        }
        return "CONNECTED";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("NetworkRegistrationInfo{");
        sb.append(" domain=");
        sb.append(this.mDomain == 1 ? "CS" : "PS");
        sb.append(" transportType=");
        sb.append(AccessNetworkConstants.transportTypeToString(this.mTransportType));
        sb.append(" registrationState=");
        sb.append(registrationStateToString(this.mRegistrationState));
        sb.append(" roamingType=");
        sb.append(ServiceState.roamingTypeToString(this.mRoamingType));
        sb.append(" accessNetworkTechnology=");
        sb.append(TelephonyManager.getNetworkTypeName(this.mAccessNetworkTechnology));
        sb.append(" rejectCause=");
        sb.append(this.mRejectCause);
        sb.append(" emergencyEnabled=");
        sb.append(this.mEmergencyOnly);
        sb.append(" availableServices=");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("[");
        ArrayList<Integer> arrayList = this.mAvailableServices;
        sb2.append(arrayList != null ? (String) arrayList.stream().map($$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0.INSTANCE).collect(Collectors.joining(SmsManager.REGEX_PREFIX_DELIMITER)) : null);
        sb2.append("]");
        sb.append(sb2.toString());
        sb.append(" cellIdentity=");
        sb.append(this.mCellIdentity);
        sb.append(" voiceSpecificInfo=");
        sb.append(this.mVoiceSpecificInfo);
        sb.append(" dataSpecificInfo=");
        sb.append(this.mDataSpecificInfo);
        sb.append(" nrState=");
        sb.append(nrStateToString(this.mNrState));
        sb.append(" nsaState=");
        sb.append(this.mNsaState);
        sb.append(" mConfigRadioTech=");
        sb.append(this.mConfigRadioTech);
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mDomain), Integer.valueOf(this.mTransportType), Integer.valueOf(this.mRegistrationState), Integer.valueOf(this.mRoamingType), Integer.valueOf(this.mAccessNetworkTechnology), Integer.valueOf(this.mRejectCause), Boolean.valueOf(this.mEmergencyOnly), this.mAvailableServices, this.mCellIdentity, this.mVoiceSpecificInfo, this.mDataSpecificInfo, Integer.valueOf(this.mNrState), Integer.valueOf(this.mNsaState), Integer.valueOf(this.mConfigRadioTech));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkRegistrationInfo)) {
            return false;
        }
        NetworkRegistrationInfo other = (NetworkRegistrationInfo) o;
        if (this.mDomain == other.mDomain && this.mTransportType == other.mTransportType && this.mRegistrationState == other.mRegistrationState && this.mRoamingType == other.mRoamingType && this.mAccessNetworkTechnology == other.mAccessNetworkTechnology && this.mRejectCause == other.mRejectCause && this.mEmergencyOnly == other.mEmergencyOnly && this.mAvailableServices.equals(other.mAvailableServices) && Objects.equals(this.mCellIdentity, other.mCellIdentity) && Objects.equals(this.mVoiceSpecificInfo, other.mVoiceSpecificInfo) && Objects.equals(this.mDataSpecificInfo, other.mDataSpecificInfo) && this.mNrState == other.mNrState && this.mNsaState == other.mNsaState && this.mConfigRadioTech == other.mConfigRadioTech) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDomain);
        dest.writeInt(this.mTransportType);
        dest.writeInt(this.mRegistrationState);
        dest.writeInt(this.mRoamingType);
        dest.writeInt(this.mAccessNetworkTechnology);
        dest.writeInt(this.mRejectCause);
        dest.writeBoolean(this.mEmergencyOnly);
        dest.writeList(this.mAvailableServices);
        dest.writeParcelable(this.mCellIdentity, 0);
        dest.writeParcelable(this.mVoiceSpecificInfo, 0);
        dest.writeParcelable(this.mDataSpecificInfo, 0);
        dest.writeInt(this.mNrState);
        dest.writeInt(this.mNsaState);
        dest.writeInt(this.mConfigRadioTech);
    }

    private void updateNrState(DataSpecificRegistrationInfo state) {
        this.mNrState = -1;
        if (!state.isEnDcAvailable) {
            return;
        }
        if (state.isDcNrRestricted || !state.isNrAvailable) {
            this.mNrState = 1;
        } else {
            this.mNrState = 2;
        }
    }

    public NetworkRegistrationInfo sanitizeLocationInfo() {
        NetworkRegistrationInfo result = copy();
        result.mCellIdentity = null;
        return result;
    }

    private NetworkRegistrationInfo copy() {
        Parcel p = Parcel.obtain();
        writeToParcel(p, 0);
        p.setDataPosition(0);
        NetworkRegistrationInfo result = new NetworkRegistrationInfo(p);
        p.recycle();
        return result;
    }

    public static final class Builder {
        private int mAccessNetworkTechnology;
        private List<Integer> mAvailableServices;
        private CellIdentity mCellIdentity;
        private int mDomain;
        private boolean mEmergencyOnly;
        private int mRegistrationState;
        private int mRejectCause;
        private int mTransportType;

        public Builder setDomain(int domain) {
            this.mDomain = domain;
            return this;
        }

        public Builder setTransportType(int transportType) {
            this.mTransportType = transportType;
            return this;
        }

        public Builder setRegistrationState(int registrationState) {
            this.mRegistrationState = registrationState;
            return this;
        }

        public Builder setAccessNetworkTechnology(int accessNetworkTechnology) {
            this.mAccessNetworkTechnology = accessNetworkTechnology;
            return this;
        }

        public Builder setRejectCause(int rejectCause) {
            this.mRejectCause = rejectCause;
            return this;
        }

        public Builder setEmergencyOnly(boolean emergencyOnly) {
            this.mEmergencyOnly = emergencyOnly;
            return this;
        }

        public Builder setAvailableServices(List<Integer> availableServices) {
            this.mAvailableServices = availableServices;
            return this;
        }

        public Builder setCellIdentity(CellIdentity cellIdentity) {
            this.mCellIdentity = cellIdentity;
            return this;
        }

        public NetworkRegistrationInfo build() {
            return new NetworkRegistrationInfo(this.mDomain, this.mTransportType, this.mRegistrationState, this.mAccessNetworkTechnology, this.mRejectCause, this.mEmergencyOnly, this.mAvailableServices, this.mCellIdentity);
        }
    }

    public int getNsaState() {
        return this.mNsaState;
    }

    public void setNsaState(int state) {
        this.mNsaState = state;
    }

    public int getConfigRadioTechnology() {
        return this.mConfigRadioTech;
    }

    public void setConfigRadioTechnology(int radioTech) {
        this.mConfigRadioTech = radioTech;
    }
}
