package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.EnumMap;

public class NetworkInfo implements Parcelable {
    public static final Creator<NetworkInfo> CREATOR = new Creator<NetworkInfo>() {
        public NetworkInfo createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            NetworkInfo netInfo = new NetworkInfo(in.readInt(), in.readInt(), in.readString(), in.readString());
            netInfo.mState = State.valueOf(in.readString());
            netInfo.mDetailedState = DetailedState.valueOf(in.readString());
            netInfo.mIsFailover = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            netInfo.mIsAvailable = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            netInfo.mIsRoaming = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            netInfo.mIsMetered = z2;
            netInfo.mReason = in.readString();
            netInfo.mExtraInfo = in.readString();
            return netInfo;
        }

        public NetworkInfo[] newArray(int size) {
            return new NetworkInfo[size];
        }
    };
    private static final EnumMap<DetailedState, State> stateMap = new EnumMap(DetailedState.class);
    private DetailedState mDetailedState;
    private String mExtraInfo;
    private boolean mIsAvailable;
    private boolean mIsFailover;
    private boolean mIsMetered;
    private boolean mIsRoaming;
    private int mNetworkType;
    private String mReason;
    private State mState;
    private int mSubtype;
    private String mSubtypeName;
    private String mTypeName;

    public enum DetailedState {
        IDLE,
        SCANNING,
        CONNECTING,
        AUTHENTICATING,
        OBTAINING_IPADDR,
        CONNECTED,
        SUSPENDED,
        DISCONNECTING,
        DISCONNECTED,
        FAILED,
        BLOCKED,
        VERIFYING_POOR_LINK,
        CAPTIVE_PORTAL_CHECK
    }

    public enum State {
        CONNECTING,
        CONNECTED,
        SUSPENDED,
        DISCONNECTING,
        DISCONNECTED,
        UNKNOWN
    }

    static {
        stateMap.put(DetailedState.IDLE, State.DISCONNECTED);
        stateMap.put(DetailedState.SCANNING, State.DISCONNECTED);
        stateMap.put(DetailedState.CONNECTING, State.CONNECTING);
        stateMap.put(DetailedState.AUTHENTICATING, State.CONNECTING);
        stateMap.put(DetailedState.OBTAINING_IPADDR, State.CONNECTING);
        stateMap.put(DetailedState.VERIFYING_POOR_LINK, State.CONNECTING);
        stateMap.put(DetailedState.CAPTIVE_PORTAL_CHECK, State.CONNECTING);
        stateMap.put(DetailedState.CONNECTED, State.CONNECTED);
        stateMap.put(DetailedState.SUSPENDED, State.SUSPENDED);
        stateMap.put(DetailedState.DISCONNECTING, State.DISCONNECTING);
        stateMap.put(DetailedState.DISCONNECTED, State.DISCONNECTED);
        stateMap.put(DetailedState.FAILED, State.DISCONNECTED);
        stateMap.put(DetailedState.BLOCKED, State.DISCONNECTED);
    }

    public NetworkInfo(int type, int subtype, String typeName, String subtypeName) {
        if (ConnectivityManager.isNetworkTypeValid(type)) {
            this.mNetworkType = type;
            this.mSubtype = subtype;
            this.mTypeName = typeName;
            this.mSubtypeName = subtypeName;
            setDetailedState(DetailedState.IDLE, null, null);
            this.mState = State.UNKNOWN;
            return;
        }
        throw new IllegalArgumentException("Invalid network type: " + type);
    }

    public NetworkInfo(NetworkInfo source) {
        if (source != null) {
            synchronized (source) {
                this.mNetworkType = source.mNetworkType;
                this.mSubtype = source.mSubtype;
                this.mTypeName = source.mTypeName;
                this.mSubtypeName = source.mSubtypeName;
                this.mState = source.mState;
                this.mDetailedState = source.mDetailedState;
                this.mReason = source.mReason;
                this.mExtraInfo = source.mExtraInfo;
                this.mIsFailover = source.mIsFailover;
                this.mIsAvailable = source.mIsAvailable;
                this.mIsRoaming = source.mIsRoaming;
                this.mIsMetered = source.mIsMetered;
            }
        }
    }

    public int getType() {
        int i;
        synchronized (this) {
            i = this.mNetworkType;
        }
        return i;
    }

    public void setType(int type) {
        synchronized (this) {
            this.mNetworkType = type;
        }
    }

    public int getSubtype() {
        int i;
        synchronized (this) {
            i = this.mSubtype;
        }
        return i;
    }

    public void setSubtype(int subtype, String subtypeName) {
        synchronized (this) {
            this.mSubtype = subtype;
            this.mSubtypeName = subtypeName;
        }
    }

    public String getTypeName() {
        String str;
        synchronized (this) {
            str = this.mTypeName;
        }
        return str;
    }

    public String getSubtypeName() {
        String str;
        synchronized (this) {
            str = this.mSubtypeName;
        }
        return str;
    }

    public boolean isConnectedOrConnecting() {
        boolean z = true;
        synchronized (this) {
            if (!(this.mState == State.CONNECTED || this.mState == State.CONNECTING)) {
                z = false;
            }
        }
        return z;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this) {
            z = this.mState == State.CONNECTED;
        }
        return z;
    }

    public boolean isAvailable() {
        boolean z;
        synchronized (this) {
            z = this.mIsAvailable;
        }
        return z;
    }

    public void setIsAvailable(boolean isAvailable) {
        synchronized (this) {
            this.mIsAvailable = isAvailable;
        }
    }

    public boolean isFailover() {
        boolean z;
        synchronized (this) {
            z = this.mIsFailover;
        }
        return z;
    }

    public void setFailover(boolean isFailover) {
        synchronized (this) {
            this.mIsFailover = isFailover;
        }
    }

    public boolean isRoaming() {
        boolean z;
        synchronized (this) {
            z = this.mIsRoaming;
        }
        return z;
    }

    public void setRoaming(boolean isRoaming) {
        synchronized (this) {
            this.mIsRoaming = isRoaming;
        }
    }

    public boolean isMetered() {
        boolean z;
        synchronized (this) {
            z = this.mIsMetered;
        }
        return z;
    }

    public void setMetered(boolean isMetered) {
        synchronized (this) {
            this.mIsMetered = isMetered;
        }
    }

    public State getState() {
        State state;
        synchronized (this) {
            state = this.mState;
        }
        return state;
    }

    public DetailedState getDetailedState() {
        DetailedState detailedState;
        synchronized (this) {
            detailedState = this.mDetailedState;
        }
        return detailedState;
    }

    public void setDetailedState(DetailedState detailedState, String reason, String extraInfo) {
        synchronized (this) {
            this.mDetailedState = detailedState;
            this.mState = (State) stateMap.get(detailedState);
            this.mReason = reason;
            this.mExtraInfo = extraInfo;
        }
    }

    public void setExtraInfo(String extraInfo) {
        synchronized (this) {
            this.mExtraInfo = extraInfo;
        }
    }

    public String getReason() {
        String str;
        synchronized (this) {
            str = this.mReason;
        }
        return str;
    }

    public String getExtraInfo() {
        String str;
        synchronized (this) {
            str = this.mExtraInfo;
        }
        return str;
    }

    public String toString() {
        String stringBuilder;
        synchronized (this) {
            StringBuilder builder = new StringBuilder("[");
            builder.append("type: ").append(getTypeName()).append("[").append(getSubtypeName()).append("], state: ").append(this.mState).append("/").append(this.mDetailedState).append(", reason: ").append(this.mReason == null ? "(unspecified)" : this.mReason).append(", extra: ").append(this.mExtraInfo == null ? "(none)" : this.mExtraInfo).append(", failover: ").append(this.mIsFailover).append(", available: ").append(this.mIsAvailable).append(", roaming: ").append(this.mIsRoaming).append(", metered: ").append(this.mIsMetered).append("]");
            stringBuilder = builder.toString();
        }
        return stringBuilder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        synchronized (this) {
            int i2;
            dest.writeInt(this.mNetworkType);
            dest.writeInt(this.mSubtype);
            dest.writeString(this.mTypeName);
            dest.writeString(this.mSubtypeName);
            dest.writeString(this.mState.name());
            dest.writeString(this.mDetailedState.name());
            dest.writeInt(this.mIsFailover ? 1 : 0);
            if (this.mIsAvailable) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeInt(i2);
            if (this.mIsRoaming) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeInt(i2);
            if (!this.mIsMetered) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeString(this.mReason);
            dest.writeString(this.mExtraInfo);
        }
    }
}
