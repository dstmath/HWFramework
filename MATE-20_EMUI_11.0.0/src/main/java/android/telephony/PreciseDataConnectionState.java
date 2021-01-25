package android.telephony;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.LinkProperties;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.data.ApnSetting;
import java.util.Objects;

@SystemApi
public final class PreciseDataConnectionState implements Parcelable {
    public static final Parcelable.Creator<PreciseDataConnectionState> CREATOR = new Parcelable.Creator<PreciseDataConnectionState>() {
        /* class android.telephony.PreciseDataConnectionState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PreciseDataConnectionState createFromParcel(Parcel in) {
            return new PreciseDataConnectionState(in);
        }

        @Override // android.os.Parcelable.Creator
        public PreciseDataConnectionState[] newArray(int size) {
            return new PreciseDataConnectionState[size];
        }
    };
    private String mAPN;
    private int mAPNTypes;
    private int mFailCause;
    private LinkProperties mLinkProperties;
    private int mNetworkType;
    private int mState;

    @UnsupportedAppUsage
    public PreciseDataConnectionState(int state, int networkType, int apnTypes, String apn, LinkProperties linkProperties, int failCause) {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mFailCause = 0;
        this.mAPNTypes = 0;
        this.mAPN = "";
        this.mLinkProperties = null;
        this.mState = state;
        this.mNetworkType = networkType;
        this.mAPNTypes = apnTypes;
        this.mAPN = apn;
        this.mLinkProperties = linkProperties;
        this.mFailCause = failCause;
    }

    public PreciseDataConnectionState() {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mFailCause = 0;
        this.mAPNTypes = 0;
        this.mAPN = "";
        this.mLinkProperties = null;
    }

    private PreciseDataConnectionState(Parcel in) {
        this.mState = -1;
        this.mNetworkType = 0;
        this.mFailCause = 0;
        this.mAPNTypes = 0;
        this.mAPN = "";
        this.mLinkProperties = null;
        this.mState = in.readInt();
        this.mNetworkType = in.readInt();
        this.mAPNTypes = in.readInt();
        this.mAPN = in.readString();
        this.mLinkProperties = (LinkProperties) in.readParcelable(null);
        this.mFailCause = in.readInt();
    }

    public int getDataConnectionState() {
        return this.mState;
    }

    public int getDataConnectionNetworkType() {
        return this.mNetworkType;
    }

    public int getDataConnectionApnTypeBitMask() {
        return this.mAPNTypes;
    }

    public String getDataConnectionApn() {
        return this.mAPN;
    }

    @UnsupportedAppUsage
    public LinkProperties getDataConnectionLinkProperties() {
        return this.mLinkProperties;
    }

    public int getDataConnectionFailCause() {
        return this.mFailCause;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mState);
        out.writeInt(this.mNetworkType);
        out.writeInt(this.mAPNTypes);
        out.writeString(this.mAPN);
        out.writeParcelable(this.mLinkProperties, flags);
        out.writeInt(this.mFailCause);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mState), Integer.valueOf(this.mNetworkType), Integer.valueOf(this.mAPNTypes), this.mAPN, this.mLinkProperties, Integer.valueOf(this.mFailCause));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PreciseDataConnectionState)) {
            return false;
        }
        PreciseDataConnectionState other = (PreciseDataConnectionState) obj;
        if (Objects.equals(this.mAPN, other.mAPN) && this.mAPNTypes == other.mAPNTypes && this.mFailCause == other.mFailCause && Objects.equals(this.mLinkProperties, other.mLinkProperties) && this.mNetworkType == other.mNetworkType && this.mState == other.mState) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Connection state: " + this.mState);
        sb.append(", Network type: " + this.mNetworkType);
        sb.append(", APN types: " + ApnSetting.getApnTypesStringFromBitmask(this.mAPNTypes));
        sb.append(", APN: " + this.mAPN);
        sb.append(", Link properties: " + this.mLinkProperties);
        sb.append(", Fail cause: " + DataFailCause.toString(this.mFailCause));
        return sb.toString();
    }
}
