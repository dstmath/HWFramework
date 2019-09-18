package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

public class NetworkRegistrationState implements Parcelable {
    public static final Parcelable.Creator<NetworkRegistrationState> CREATOR = new Parcelable.Creator<NetworkRegistrationState>() {
        public NetworkRegistrationState createFromParcel(Parcel source) {
            return new NetworkRegistrationState(source);
        }

        public NetworkRegistrationState[] newArray(int size) {
            return new NetworkRegistrationState[size];
        }
    };
    public static final int DOMAIN_CS = 1;
    public static final int DOMAIN_PS = 2;
    public static final int NR_STATUS_UNKNOWN = 0;
    public static final int REG_STATE_DENIED = 3;
    public static final int REG_STATE_HOME = 1;
    public static final int REG_STATE_NOT_REG_NOT_SEARCHING = 0;
    public static final int REG_STATE_NOT_REG_SEARCHING = 2;
    public static final int REG_STATE_ROAMING = 5;
    public static final int REG_STATE_UNKNOWN = 4;
    public static final int SERVICE_TYPE_DATA = 2;
    public static final int SERVICE_TYPE_EMERGENCY = 5;
    public static final int SERVICE_TYPE_SMS = 3;
    public static final int SERVICE_TYPE_VIDEO = 4;
    public static final int SERVICE_TYPE_VOICE = 1;
    private final int mAccessNetworkTechnology;
    private final int[] mAvailableServices;
    private final CellIdentity mCellIdentity;
    private DataSpecificRegistrationStates mDataSpecificStates;
    private final int mDomain;
    private final boolean mEmergencyOnly;
    private int mNsaState;
    private final int mReasonForDenial;
    private final int mRegState;
    private final int mTransportType;
    private VoiceSpecificRegistrationStates mVoiceSpecificStates;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Domain {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RegState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceType {
    }

    public NetworkRegistrationState(int transportType, int domain, int regState, int accessNetworkTechnology, int reasonForDenial, boolean emergencyOnly, int[] availableServices, CellIdentity cellIdentity) {
        this.mNsaState = 0;
        this.mTransportType = transportType;
        this.mDomain = domain;
        this.mRegState = regState;
        this.mAccessNetworkTechnology = accessNetworkTechnology;
        this.mReasonForDenial = reasonForDenial;
        this.mAvailableServices = availableServices;
        this.mCellIdentity = cellIdentity;
        this.mEmergencyOnly = emergencyOnly;
    }

    public NetworkRegistrationState(int transportType, int domain, int regState, int accessNetworkTechnology, int reasonForDenial, boolean emergencyOnly, int[] availableServices, CellIdentity cellIdentity, boolean cssSupported, int roamingIndicator, int systemIsInPrl, int defaultRoamingIndicator) {
        this(transportType, domain, regState, accessNetworkTechnology, reasonForDenial, emergencyOnly, availableServices, cellIdentity);
        this.mVoiceSpecificStates = new VoiceSpecificRegistrationStates(cssSupported, roamingIndicator, systemIsInPrl, defaultRoamingIndicator);
    }

    public NetworkRegistrationState(int transportType, int domain, int regState, int accessNetworkTechnology, int reasonForDenial, boolean emergencyOnly, int[] availableServices, CellIdentity cellIdentity, int maxDataCalls) {
        this(transportType, domain, regState, accessNetworkTechnology, reasonForDenial, emergencyOnly, availableServices, cellIdentity);
        this.mDataSpecificStates = new DataSpecificRegistrationStates(maxDataCalls);
    }

    protected NetworkRegistrationState(Parcel source) {
        this.mNsaState = 0;
        this.mTransportType = source.readInt();
        this.mDomain = source.readInt();
        this.mRegState = source.readInt();
        this.mAccessNetworkTechnology = source.readInt();
        this.mReasonForDenial = source.readInt();
        this.mEmergencyOnly = source.readBoolean();
        this.mAvailableServices = source.createIntArray();
        this.mCellIdentity = (CellIdentity) source.readParcelable(CellIdentity.class.getClassLoader());
        this.mVoiceSpecificStates = (VoiceSpecificRegistrationStates) source.readParcelable(VoiceSpecificRegistrationStates.class.getClassLoader());
        this.mDataSpecificStates = (DataSpecificRegistrationStates) source.readParcelable(DataSpecificRegistrationStates.class.getClassLoader());
        this.mNsaState = source.readInt();
    }

    public int getTransportType() {
        return this.mTransportType;
    }

    public int getDomain() {
        return this.mDomain;
    }

    public int getRegState() {
        return this.mRegState;
    }

    public boolean isEmergencyEnabled() {
        return this.mEmergencyOnly;
    }

    public int[] getAvailableServices() {
        return this.mAvailableServices;
    }

    public int getAccessNetworkTechnology() {
        return this.mAccessNetworkTechnology;
    }

    public int getReasonForDenial() {
        return this.mReasonForDenial;
    }

    public CellIdentity getCellIdentity() {
        return this.mCellIdentity;
    }

    public VoiceSpecificRegistrationStates getVoiceSpecificStates() {
        return this.mVoiceSpecificStates;
    }

    public DataSpecificRegistrationStates getDataSpecificStates() {
        return this.mDataSpecificStates;
    }

    public int describeContents() {
        return 0;
    }

    private static String regStateToString(int regState) {
        switch (regState) {
            case 0:
                return "NOT_REG_NOT_SEARCHING";
            case 1:
                return "HOME";
            case 2:
                return "NOT_REG_SEARCHING";
            case 3:
                return "DENIED";
            case 4:
                return "UNKNOWN";
            case 5:
                return "ROAMING";
            default:
                return "Unknown reg state " + regState;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("NetworkRegistrationState{");
        sb.append("transportType=");
        sb.append(this.mTransportType);
        sb.append(" domain=");
        sb.append(this.mDomain == 1 ? "CS" : "PS");
        sb.append(" regState=");
        sb.append(regStateToString(this.mRegState));
        sb.append(" accessNetworkTechnology=");
        sb.append(TelephonyManager.getNetworkTypeName(this.mAccessNetworkTechnology));
        sb.append(" reasonForDenial=");
        sb.append(this.mReasonForDenial);
        sb.append(" emergencyEnabled=");
        sb.append(this.mEmergencyOnly);
        sb.append(" supportedServices=");
        sb.append(this.mAvailableServices);
        sb.append(" cellIdentity=");
        sb.append(this.mCellIdentity);
        sb.append(" voiceSpecificStates=");
        sb.append(this.mVoiceSpecificStates);
        sb.append(" dataSpecificStates=");
        sb.append(this.mDataSpecificStates);
        sb.append(" nsaState=");
        sb.append(this.mNsaState);
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mTransportType), Integer.valueOf(this.mDomain), Integer.valueOf(this.mRegState), Integer.valueOf(this.mAccessNetworkTechnology), Integer.valueOf(this.mReasonForDenial), Boolean.valueOf(this.mEmergencyOnly), this.mAvailableServices, this.mCellIdentity, this.mVoiceSpecificStates, this.mDataSpecificStates, Integer.valueOf(this.mNsaState)});
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NetworkRegistrationState)) {
            return false;
        }
        NetworkRegistrationState other = (NetworkRegistrationState) o;
        if (!(this.mTransportType == other.mTransportType && this.mDomain == other.mDomain && this.mRegState == other.mRegState && this.mAccessNetworkTechnology == other.mAccessNetworkTechnology && this.mReasonForDenial == other.mReasonForDenial && this.mEmergencyOnly == other.mEmergencyOnly && ((this.mAvailableServices == other.mAvailableServices || Arrays.equals(this.mAvailableServices, other.mAvailableServices)) && equals(this.mCellIdentity, other.mCellIdentity) && equals(this.mVoiceSpecificStates, other.mVoiceSpecificStates) && equals(this.mDataSpecificStates, other.mDataSpecificStates) && equals(Integer.valueOf(this.mNsaState), Integer.valueOf(other.mNsaState))))) {
            z = false;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTransportType);
        dest.writeInt(this.mDomain);
        dest.writeInt(this.mRegState);
        dest.writeInt(this.mAccessNetworkTechnology);
        dest.writeInt(this.mReasonForDenial);
        dest.writeBoolean(this.mEmergencyOnly);
        dest.writeIntArray(this.mAvailableServices);
        dest.writeParcelable(this.mCellIdentity, 0);
        dest.writeParcelable(this.mVoiceSpecificStates, 0);
        dest.writeParcelable(this.mDataSpecificStates, 0);
        dest.writeInt(this.mNsaState);
    }

    private static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    public int getNsaState() {
        return this.mNsaState;
    }

    public void setNsaState(int state) {
        this.mNsaState = state;
    }
}
