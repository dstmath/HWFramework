package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.Objects;

public class NetworkRequest implements Parcelable {
    public static final Creator<NetworkRequest> CREATOR = new Creator<NetworkRequest>() {
        public NetworkRequest createFromParcel(Parcel in) {
            return new NetworkRequest((NetworkCapabilities) in.readParcelable(null), in.readInt(), in.readInt(), Type.valueOf(in.readString()));
        }

        public NetworkRequest[] newArray(int size) {
            return new NetworkRequest[size];
        }
    };
    public final int legacyType;
    public final NetworkCapabilities networkCapabilities;
    public final int requestId;
    public final Type type;

    public static class Builder {
        private final NetworkCapabilities mNetworkCapabilities = new NetworkCapabilities();

        public NetworkRequest build() {
            NetworkCapabilities nc = new NetworkCapabilities(this.mNetworkCapabilities);
            nc.maybeMarkCapabilitiesRestricted();
            return new NetworkRequest(nc, -1, 0, Type.NONE);
        }

        public Builder addCapability(int capability) {
            this.mNetworkCapabilities.addCapability(capability);
            return this;
        }

        public Builder removeCapability(int capability) {
            this.mNetworkCapabilities.removeCapability(capability);
            return this;
        }

        public Builder setCapabilities(NetworkCapabilities nc) {
            this.mNetworkCapabilities.clearAll();
            this.mNetworkCapabilities.combineCapabilities(nc);
            return this;
        }

        public Builder clearCapabilities() {
            this.mNetworkCapabilities.clearAll();
            return this;
        }

        public Builder addTransportType(int transportType) {
            this.mNetworkCapabilities.addTransportType(transportType);
            return this;
        }

        public Builder removeTransportType(int transportType) {
            this.mNetworkCapabilities.removeTransportType(transportType);
            return this;
        }

        public Builder setLinkUpstreamBandwidthKbps(int upKbps) {
            this.mNetworkCapabilities.setLinkUpstreamBandwidthKbps(upKbps);
            return this;
        }

        public Builder setLinkDownstreamBandwidthKbps(int downKbps) {
            this.mNetworkCapabilities.setLinkDownstreamBandwidthKbps(downKbps);
            return this;
        }

        public Builder setNetworkSpecifier(String networkSpecifier) {
            NetworkSpecifier networkSpecifier2;
            if (TextUtils.isEmpty(networkSpecifier)) {
                networkSpecifier2 = null;
            } else {
                networkSpecifier2 = new StringNetworkSpecifier(networkSpecifier);
            }
            return setNetworkSpecifier(networkSpecifier2);
        }

        public Builder setNetworkSpecifier(NetworkSpecifier networkSpecifier) {
            MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(networkSpecifier);
            this.mNetworkCapabilities.setNetworkSpecifier(networkSpecifier);
            return this;
        }

        public Builder setSignalStrength(int signalStrength) {
            this.mNetworkCapabilities.setSignalStrength(signalStrength);
            return this;
        }
    }

    public enum Type {
        NONE,
        LISTEN,
        TRACK_DEFAULT,
        REQUEST,
        BACKGROUND_REQUEST
    }

    public NetworkRequest(NetworkCapabilities nc, int legacyType, int rId, Type type) {
        if (nc == null) {
            throw new NullPointerException();
        }
        this.requestId = rId;
        this.networkCapabilities = nc;
        this.legacyType = legacyType;
        this.type = type;
    }

    public NetworkRequest(NetworkRequest that) {
        this.networkCapabilities = new NetworkCapabilities(that.networkCapabilities);
        this.requestId = that.requestId;
        this.legacyType = that.legacyType;
        this.type = that.type;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.networkCapabilities, flags);
        dest.writeInt(this.legacyType);
        dest.writeInt(this.requestId);
        dest.writeString(this.type.name());
    }

    public boolean isListen() {
        return this.type == Type.LISTEN;
    }

    public boolean isRequest() {
        return !isForegroundRequest() ? isBackgroundRequest() : true;
    }

    public boolean isForegroundRequest() {
        return this.type == Type.TRACK_DEFAULT || this.type == Type.REQUEST;
    }

    public boolean isBackgroundRequest() {
        return this.type == Type.BACKGROUND_REQUEST;
    }

    public String toString() {
        return "NetworkRequest [ " + this.type + " id=" + this.requestId + (this.legacyType != -1 ? ", legacyType=" + this.legacyType : ProxyInfo.LOCAL_EXCL_LIST) + ", " + this.networkCapabilities.toString() + " ]";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkRequest)) {
            return false;
        }
        NetworkRequest that = (NetworkRequest) obj;
        if (that.legacyType == this.legacyType && that.requestId == this.requestId && that.type == this.type) {
            z = Objects.equals(that.networkCapabilities, this.networkCapabilities);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.requestId), Integer.valueOf(this.legacyType), this.networkCapabilities, this.type});
    }
}
