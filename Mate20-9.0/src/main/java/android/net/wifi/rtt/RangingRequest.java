package android.net.wifi.rtt;

import android.annotation.SystemApi;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.aware.PeerHandle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class RangingRequest implements Parcelable {
    public static final Parcelable.Creator<RangingRequest> CREATOR = new Parcelable.Creator<RangingRequest>() {
        public RangingRequest[] newArray(int size) {
            return new RangingRequest[size];
        }

        public RangingRequest createFromParcel(Parcel in) {
            return new RangingRequest(in.readArrayList(null));
        }
    };
    private static final int MAX_PEERS = 10;
    public final List<ResponderConfig> mRttPeers;

    public static final class Builder {
        private List<ResponderConfig> mRttPeers = new ArrayList();

        public Builder addAccessPoint(ScanResult apInfo) {
            if (apInfo != null) {
                return addResponder(ResponderConfig.fromScanResult(apInfo));
            }
            throw new IllegalArgumentException("Null ScanResult!");
        }

        public Builder addAccessPoints(List<ScanResult> apInfos) {
            if (apInfos != null) {
                for (ScanResult scanResult : apInfos) {
                    addAccessPoint(scanResult);
                }
                return this;
            }
            throw new IllegalArgumentException("Null list of ScanResults!");
        }

        public Builder addWifiAwarePeer(MacAddress peerMacAddress) {
            if (peerMacAddress != null) {
                return addResponder(ResponderConfig.fromWifiAwarePeerMacAddressWithDefaults(peerMacAddress));
            }
            throw new IllegalArgumentException("Null peer MAC address");
        }

        public Builder addWifiAwarePeer(PeerHandle peerHandle) {
            if (peerHandle != null) {
                return addResponder(ResponderConfig.fromWifiAwarePeerHandleWithDefaults(peerHandle));
            }
            throw new IllegalArgumentException("Null peer handler (identifier)");
        }

        @SystemApi
        public Builder addResponder(ResponderConfig responder) {
            if (responder != null) {
                this.mRttPeers.add(responder);
                return this;
            }
            throw new IllegalArgumentException("Null Responder!");
        }

        public RangingRequest build() {
            return new RangingRequest(this.mRttPeers);
        }
    }

    public static int getMaxPeers() {
        return 10;
    }

    private RangingRequest(List<ResponderConfig> rttPeers) {
        this.mRttPeers = rttPeers;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mRttPeers);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "RangingRequest: mRttPeers=[", "]");
        for (ResponderConfig rc : this.mRttPeers) {
            sj.add(rc.toString());
        }
        return sj.toString();
    }

    public void enforceValidity(boolean awareSupported) {
        if (this.mRttPeers.size() <= 10) {
            for (ResponderConfig peer : this.mRttPeers) {
                if (!peer.isValid(awareSupported)) {
                    throw new IllegalArgumentException("Invalid Responder specification");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Ranging to too many peers requested. Use getMaxPeers() API to get limit.");
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof RangingRequest)) {
            return false;
        }
        RangingRequest lhs = (RangingRequest) o;
        if (this.mRttPeers.size() != lhs.mRttPeers.size() || !this.mRttPeers.containsAll(lhs.mRttPeers)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.mRttPeers.hashCode();
    }
}
