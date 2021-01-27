package android.net;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class NetworkState implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<NetworkState> CREATOR = new Parcelable.Creator<NetworkState>() {
        /* class android.net.NetworkState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkState createFromParcel(Parcel in) {
            return new NetworkState(in);
        }

        @Override // android.os.Parcelable.Creator
        public NetworkState[] newArray(int size) {
            return new NetworkState[size];
        }
    };
    public static final NetworkState EMPTY = new NetworkState(null, null, null, null, null, null);
    private static final boolean SANITY_CHECK_ROAMING = false;
    public final LinkProperties linkProperties;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final Network network;
    public final NetworkCapabilities networkCapabilities;
    public final String networkId;
    public final NetworkInfo networkInfo;
    public final String subscriberId;

    public NetworkState(NetworkInfo networkInfo2, LinkProperties linkProperties2, NetworkCapabilities networkCapabilities2, Network network2, String subscriberId2, String networkId2) {
        this.networkInfo = networkInfo2;
        this.linkProperties = linkProperties2;
        this.networkCapabilities = networkCapabilities2;
        this.network = network2;
        this.subscriberId = subscriberId2;
        this.networkId = networkId2;
    }

    @UnsupportedAppUsage
    public NetworkState(Parcel in) {
        this.networkInfo = (NetworkInfo) in.readParcelable(null);
        this.linkProperties = (LinkProperties) in.readParcelable(null);
        this.networkCapabilities = (NetworkCapabilities) in.readParcelable(null);
        this.network = (Network) in.readParcelable(null);
        this.subscriberId = in.readString();
        this.networkId = in.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.networkInfo, flags);
        out.writeParcelable(this.linkProperties, flags);
        out.writeParcelable(this.networkCapabilities, flags);
        out.writeParcelable(this.network, flags);
        out.writeString(this.subscriberId);
        out.writeString(this.networkId);
    }
}
