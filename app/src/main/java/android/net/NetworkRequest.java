package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;

public class NetworkRequest implements Parcelable {
    public static final Creator<NetworkRequest> CREATOR = null;
    public final int legacyType;
    public final NetworkCapabilities networkCapabilities;
    public final int requestId;

    public static class Builder {
        private final NetworkCapabilities mNetworkCapabilities;

        public Builder() {
            this.mNetworkCapabilities = new NetworkCapabilities();
        }

        public NetworkRequest build() {
            NetworkCapabilities nc = new NetworkCapabilities(this.mNetworkCapabilities);
            nc.maybeMarkCapabilitiesRestricted();
            return new NetworkRequest(nc, -1, 0);
        }

        public Builder addCapability(int capability) {
            this.mNetworkCapabilities.addCapability(capability);
            return this;
        }

        public Builder removeCapability(int capability) {
            this.mNetworkCapabilities.removeCapability(capability);
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
            if (NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(networkSpecifier)) {
                throw new IllegalArgumentException("Invalid network specifier - must not be '*'");
            }
            this.mNetworkCapabilities.setNetworkSpecifier(networkSpecifier);
            return this;
        }

        public Builder setSignalStrength(int signalStrength) {
            this.mNetworkCapabilities.setSignalStrength(signalStrength);
            return this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkRequest.<clinit>():void");
    }

    public NetworkRequest(NetworkCapabilities nc, int legacyType, int rId) {
        if (nc == null) {
            throw new NullPointerException();
        }
        this.requestId = rId;
        this.networkCapabilities = nc;
        this.legacyType = legacyType;
    }

    public NetworkRequest(NetworkRequest that) {
        this.networkCapabilities = new NetworkCapabilities(that.networkCapabilities);
        this.requestId = that.requestId;
        this.legacyType = that.legacyType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.networkCapabilities, flags);
        dest.writeInt(this.legacyType);
        dest.writeInt(this.requestId);
    }

    public String toString() {
        return "NetworkRequest [ id=" + this.requestId + ", legacyType=" + this.legacyType + ", " + this.networkCapabilities.toString() + " ]";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkRequest)) {
            return false;
        }
        NetworkRequest that = (NetworkRequest) obj;
        if (that.legacyType == this.legacyType && that.requestId == this.requestId) {
            if (that.networkCapabilities == null && this.networkCapabilities == null) {
                z = true;
            } else if (that.networkCapabilities != null) {
                z = that.networkCapabilities.equals(this.networkCapabilities);
            }
        }
        return z;
    }

    public int hashCode() {
        return (this.requestId + (this.legacyType * Process.MEDIA_UID)) + (this.networkCapabilities.hashCode() * 1051);
    }
}
