package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import java.util.Objects;
import java.util.Set;

public class NetworkRequest implements Parcelable {
    public static final Parcelable.Creator<NetworkRequest> CREATOR = new Parcelable.Creator<NetworkRequest>() {
        public NetworkRequest createFromParcel(Parcel in) {
            return new NetworkRequest(NetworkCapabilities.CREATOR.createFromParcel(in), in.readInt(), in.readInt(), Type.valueOf(in.readString()));
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

        public Builder() {
            this.mNetworkCapabilities.setSingleUid(Process.myUid());
        }

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
            this.mNetworkCapabilities.set(nc);
            return this;
        }

        public Builder setUids(Set<UidRange> uids) {
            this.mNetworkCapabilities.setUids(uids);
            return this;
        }

        public Builder addUnwantedCapability(int capability) {
            this.mNetworkCapabilities.addUnwantedCapability(capability);
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
            StringNetworkSpecifier stringNetworkSpecifier;
            if (TextUtils.isEmpty(networkSpecifier)) {
                stringNetworkSpecifier = null;
            } else {
                stringNetworkSpecifier = new StringNetworkSpecifier(networkSpecifier);
            }
            return setNetworkSpecifier((NetworkSpecifier) stringNetworkSpecifier);
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

    public NetworkRequest(NetworkCapabilities nc, int legacyType2, int rId, Type type2) {
        if (nc != null) {
            this.requestId = rId;
            this.networkCapabilities = nc;
            this.legacyType = legacyType2;
            this.type = type2;
            return;
        }
        throw new NullPointerException();
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
        this.networkCapabilities.writeToParcel(dest, flags);
        dest.writeInt(this.legacyType);
        dest.writeInt(this.requestId);
        dest.writeString(this.type.name());
    }

    public boolean isListen() {
        return this.type == Type.LISTEN;
    }

    public boolean isRequest() {
        return isForegroundRequest() || isBackgroundRequest();
    }

    public boolean isForegroundRequest() {
        return this.type == Type.TRACK_DEFAULT || this.type == Type.REQUEST;
    }

    public boolean isBackgroundRequest() {
        return this.type == Type.BACKGROUND_REQUEST;
    }

    public boolean hasCapability(int capability) {
        return this.networkCapabilities.hasCapability(capability);
    }

    public boolean hasUnwantedCapability(int capability) {
        return this.networkCapabilities.hasUnwantedCapability(capability);
    }

    public boolean hasTransport(int transportType) {
        return this.networkCapabilities.hasTransport(transportType);
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("NetworkRequest [ ");
        sb.append(this.type);
        sb.append(" id=");
        sb.append(this.requestId);
        if (this.legacyType != -1) {
            str = ", legacyType=" + this.legacyType;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(", ");
        sb.append(this.networkCapabilities.toString());
        sb.append(" ]");
        return sb.toString();
    }

    private int typeToProtoEnum(Type t) {
        switch (t) {
            case NONE:
                return 1;
            case LISTEN:
                return 2;
            case TRACK_DEFAULT:
                return 3;
            case REQUEST:
                return 4;
            case BACKGROUND_REQUEST:
                return 5;
            default:
                return 0;
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, typeToProtoEnum(this.type));
        proto.write(1120986464258L, this.requestId);
        proto.write(1120986464259L, this.legacyType);
        this.networkCapabilities.writeToProto(proto, 1146756268036L);
        proto.end(token);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkRequest)) {
            return false;
        }
        NetworkRequest that = (NetworkRequest) obj;
        if (that.legacyType == this.legacyType && that.requestId == this.requestId && that.type == this.type && Objects.equals(that.networkCapabilities, this.networkCapabilities)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.requestId), Integer.valueOf(this.legacyType), this.networkCapabilities, this.type});
    }
}
