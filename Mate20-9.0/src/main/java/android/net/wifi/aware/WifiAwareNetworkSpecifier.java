package android.net.wifi.aware;

import android.net.NetworkSpecifier;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.Objects;

public final class WifiAwareNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<WifiAwareNetworkSpecifier> CREATOR = new Parcelable.Creator<WifiAwareNetworkSpecifier>() {
        public WifiAwareNetworkSpecifier createFromParcel(Parcel in) {
            WifiAwareNetworkSpecifier wifiAwareNetworkSpecifier = new WifiAwareNetworkSpecifier(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.createByteArray(), in.createByteArray(), in.readString(), in.readInt());
            return wifiAwareNetworkSpecifier;
        }

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
    public final int requestorUid;
    public final int role;
    public final int sessionId;
    public final int type;

    public WifiAwareNetworkSpecifier(int type2, int role2, int clientId2, int sessionId2, int peerId2, byte[] peerMac2, byte[] pmk2, String passphrase2, int requestorUid2) {
        this.type = type2;
        this.role = role2;
        this.clientId = clientId2;
        this.sessionId = sessionId2;
        this.peerId = peerId2;
        this.peerMac = peerMac2;
        this.pmk = pmk2;
        this.passphrase = passphrase2;
        this.requestorUid = requestorUid2;
    }

    public boolean isOutOfBand() {
        return this.type == 2 || this.type == 3;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.role);
        dest.writeInt(this.clientId);
        dest.writeInt(this.sessionId);
        dest.writeInt(this.peerId);
        dest.writeByteArray(this.peerMac);
        dest.writeByteArray(this.pmk);
        dest.writeString(this.passphrase);
        dest.writeInt(this.requestorUid);
    }

    public boolean satisfiedBy(NetworkSpecifier other) {
        if (other instanceof WifiAwareAgentNetworkSpecifier) {
            return ((WifiAwareAgentNetworkSpecifier) other).satisfiesAwareNetworkSpecifier(this);
        }
        return equals(other);
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 17) + this.type)) + this.role)) + this.clientId)) + this.sessionId)) + this.peerId)) + Arrays.hashCode(this.peerMac))) + Arrays.hashCode(this.pmk))) + Objects.hashCode(this.passphrase))) + this.requestorUid;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiAwareNetworkSpecifier)) {
            return false;
        }
        WifiAwareNetworkSpecifier lhs = (WifiAwareNetworkSpecifier) obj;
        if (!(this.type == lhs.type && this.role == lhs.role && this.clientId == lhs.clientId && this.sessionId == lhs.sessionId && this.peerId == lhs.peerId && Arrays.equals(this.peerMac, lhs.peerMac) && Arrays.equals(this.pmk, lhs.pmk) && Objects.equals(this.passphrase, lhs.passphrase) && this.requestorUid == lhs.requestorUid)) {
            z = false;
        }
        return z;
    }

    public String toString() {
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
        sb.append(this.peerMac == null ? "<null>" : "<non-null>");
        sb.append(", pmk=");
        sb.append(this.pmk == null ? "<null>" : "<non-null>");
        sb.append(", passphrase=");
        sb.append(this.passphrase == null ? "<null>" : "<non-null>");
        sb.append(", requestorUid=");
        sb.append(this.requestorUid);
        sb.append("]");
        return sb.toString();
    }

    public void assertValidFromUid(int requestorUid2) {
        if (this.requestorUid != requestorUid2) {
            throw new SecurityException("mismatched UIDs");
        }
    }
}
