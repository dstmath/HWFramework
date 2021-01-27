package android.net.wifi.aware;

import android.annotation.SystemApi;
import android.net.NetworkSpecifier;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.Objects;

public final class WifiAwareNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<WifiAwareNetworkSpecifier> CREATOR = new Parcelable.Creator<WifiAwareNetworkSpecifier>() {
        /* class android.net.wifi.aware.WifiAwareNetworkSpecifier.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiAwareNetworkSpecifier createFromParcel(Parcel in) {
            return new WifiAwareNetworkSpecifier(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.createByteArray(), in.createByteArray(), in.readString(), in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public WifiAwareNetworkSpecifier[] newArray(int size) {
            return new WifiAwareNetworkSpecifier[size];
        }
    };
    public static final int NETWORK_SPECIFIER_TYPE_IB = 0;
    public static final int NETWORK_SPECIFIER_TYPE_IB_ANY_PEER = 1;
    public static final int NETWORK_SPECIFIER_TYPE_MAX_VALID = 3;
    public static final int NETWORK_SPECIFIER_TYPE_OOB = 2;
    public static final int NETWORK_SPECIFIER_TYPE_OOB_ANY_PEER = 3;
    public final int clientId;
    public final String passphrase;
    public final int peerId;
    public final byte[] peerMac;
    public final byte[] pmk;
    public final int port;
    public final int requestorUid;
    public final int role;
    public final int sessionId;
    public final int transportProtocol;
    public final int type;

    public WifiAwareNetworkSpecifier(int type2, int role2, int clientId2, int sessionId2, int peerId2, byte[] peerMac2, byte[] pmk2, String passphrase2, int port2, int transportProtocol2, int requestorUid2) {
        this.type = type2;
        this.role = role2;
        this.clientId = clientId2;
        this.sessionId = sessionId2;
        this.peerId = peerId2;
        this.peerMac = peerMac2;
        this.pmk = pmk2;
        this.passphrase = passphrase2;
        this.port = port2;
        this.transportProtocol = transportProtocol2;
        this.requestorUid = requestorUid2;
    }

    public boolean isOutOfBand() {
        int i = this.type;
        return i == 2 || i == 3;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.role);
        dest.writeInt(this.clientId);
        dest.writeInt(this.sessionId);
        dest.writeInt(this.peerId);
        dest.writeByteArray(this.peerMac);
        dest.writeByteArray(this.pmk);
        dest.writeString(this.passphrase);
        dest.writeInt(this.port);
        dest.writeInt(this.transportProtocol);
        dest.writeInt(this.requestorUid);
    }

    @Override // android.net.NetworkSpecifier
    public boolean satisfiedBy(NetworkSpecifier other) {
        if (other instanceof WifiAwareAgentNetworkSpecifier) {
            return ((WifiAwareAgentNetworkSpecifier) other).satisfiesAwareNetworkSpecifier(this);
        }
        return equals(other);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.type), Integer.valueOf(this.role), Integer.valueOf(this.clientId), Integer.valueOf(this.sessionId), Integer.valueOf(this.peerId), Integer.valueOf(Arrays.hashCode(this.peerMac)), Integer.valueOf(Arrays.hashCode(this.pmk)), this.passphrase, Integer.valueOf(this.port), Integer.valueOf(this.transportProtocol), Integer.valueOf(this.requestorUid));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiAwareNetworkSpecifier)) {
            return false;
        }
        WifiAwareNetworkSpecifier lhs = (WifiAwareNetworkSpecifier) obj;
        if (this.type == lhs.type && this.role == lhs.role && this.clientId == lhs.clientId && this.sessionId == lhs.sessionId && this.peerId == lhs.peerId && Arrays.equals(this.peerMac, lhs.peerMac) && Arrays.equals(this.pmk, lhs.pmk) && Objects.equals(this.passphrase, lhs.passphrase) && this.port == lhs.port && this.transportProtocol == lhs.transportProtocol && this.requestorUid == lhs.requestorUid) {
            return true;
        }
        return false;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder("WifiAwareNetworkSpecifier [");
        sb.append("type=");
        sb.append(this.type);
        sb.append(", role=");
        sb.append(this.role);
        sb.append(", clientId=");
        sb.append(this.clientId);
        sb.append(", sessionId=");
        sb.append(this.sessionId);
        sb.append(", peerId=");
        sb.append(this.peerId);
        sb.append(", peerMac=");
        String str2 = "<null>";
        sb.append(this.peerMac == null ? str2 : "<non-null>");
        sb.append(", pmk=");
        if (this.pmk == null) {
            str = str2;
        } else {
            str = "<non-null>";
        }
        sb.append(str);
        sb.append(", passphrase=");
        if (this.passphrase != null) {
            str2 = "<non-null>";
        }
        sb.append(str2);
        sb.append(", port=");
        sb.append(this.port);
        sb.append(", transportProtocol=");
        sb.append(this.transportProtocol);
        sb.append(", requestorUid=");
        sb.append(this.requestorUid);
        sb.append("]");
        return sb.toString();
    }

    @Override // android.net.NetworkSpecifier
    public void assertValidFromUid(int requestorUid2) {
        if (this.requestorUid != requestorUid2) {
            throw new SecurityException("mismatched UIDs");
        }
    }

    public static final class Builder {
        private DiscoverySession mDiscoverySession;
        private PeerHandle mPeerHandle;
        private byte[] mPmk;
        private int mPort = 0;
        private String mPskPassphrase;
        private int mTransportProtocol = -1;

        public Builder(DiscoverySession discoverySession, PeerHandle peerHandle) {
            if (discoverySession == null) {
                throw new IllegalArgumentException("Non-null discoverySession required");
            } else if (peerHandle != null) {
                this.mDiscoverySession = discoverySession;
                this.mPeerHandle = peerHandle;
            } else {
                throw new IllegalArgumentException("Non-null peerHandle required");
            }
        }

        public Builder setPskPassphrase(String pskPassphrase) {
            if (WifiAwareUtils.validatePassphrase(pskPassphrase)) {
                this.mPskPassphrase = pskPassphrase;
                return this;
            }
            throw new IllegalArgumentException("Passphrase must meet length requirements");
        }

        @SystemApi
        public Builder setPmk(byte[] pmk) {
            if (WifiAwareUtils.validatePmk(pmk)) {
                this.mPmk = pmk;
                return this;
            }
            throw new IllegalArgumentException("PMK must 32 bytes");
        }

        public Builder setPort(int port) {
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("The port must be a positive value (0, 65535]");
            }
            this.mPort = port;
            return this;
        }

        public Builder setTransportProtocol(int transportProtocol) {
            if (transportProtocol < 0 || transportProtocol > 255) {
                throw new IllegalArgumentException("The transport protocol must be in range [0, 255]");
            }
            this.mTransportProtocol = transportProtocol;
            return this;
        }

        public WifiAwareNetworkSpecifier build() {
            if (this.mDiscoverySession == null) {
                throw new IllegalStateException("Null discovery session!?");
            } else if (this.mPeerHandle != null) {
                int role = 0;
                if (!(this.mPskPassphrase != null) || !(this.mPmk != null)) {
                    if (!(this.mDiscoverySession instanceof SubscribeDiscoverySession)) {
                        role = 1;
                    }
                    if (!(this.mPort == 0 && this.mTransportProtocol == -1)) {
                        if (role != 1) {
                            throw new IllegalStateException("Port and transport protocol information can only be specified on the Publisher device (which is the server");
                        } else if (TextUtils.isEmpty(this.mPskPassphrase) && this.mPmk == null) {
                            throw new IllegalStateException("Port and transport protocol information can only be specified on a secure link");
                        }
                    }
                    return new WifiAwareNetworkSpecifier(0, role, this.mDiscoverySession.mClientId, this.mDiscoverySession.mSessionId, this.mPeerHandle.peerId, null, this.mPmk, this.mPskPassphrase, this.mPort, this.mTransportProtocol, Process.myUid());
                }
                throw new IllegalStateException("Can only specify a Passphrase or a PMK - not both!");
            } else {
                throw new IllegalStateException("Null peerHandle!?");
            }
        }
    }
}
