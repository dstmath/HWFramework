package android.net;

import android.content.Context;
import android.content.LocaleProto;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.WorkSourceProto.WorkSourceContentProto;
import android.telephony.TelephonyManager;
import android.util.proto.ProtoOutputStream;
import java.util.Objects;

public class NetworkIdentity implements Comparable<NetworkIdentity> {
    @Deprecated
    public static final boolean COMBINE_SUBTYPE_ENABLED = false;
    public static final int SUBTYPE_COMBINED = -1;
    private static final String TAG = "NetworkIdentity";
    final boolean mMetered;
    final String mNetworkId;
    final boolean mRoaming;
    final int mSubType;
    final String mSubscriberId;
    final int mType;

    public NetworkIdentity(int type, int subType, String subscriberId, String networkId, boolean roaming, boolean metered) {
        this.mType = type;
        this.mSubType = subType;
        this.mSubscriberId = subscriberId;
        this.mNetworkId = networkId;
        this.mRoaming = roaming;
        this.mMetered = metered;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mType), Integer.valueOf(this.mSubType), this.mSubscriberId, this.mNetworkId, Boolean.valueOf(this.mRoaming), Boolean.valueOf(this.mMetered)});
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkIdentity)) {
            return false;
        }
        NetworkIdentity ident = (NetworkIdentity) obj;
        if (this.mType == ident.mType && this.mSubType == ident.mSubType && this.mRoaming == ident.mRoaming && Objects.equals(this.mSubscriberId, ident.mSubscriberId) && Objects.equals(this.mNetworkId, ident.mNetworkId) && this.mMetered == ident.mMetered) {
            z = true;
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("type=").append(ConnectivityManager.getNetworkTypeName(this.mType));
        builder.append(", subType=");
        if (ConnectivityManager.isNetworkTypeMobile(this.mType)) {
            builder.append(TelephonyManager.getNetworkTypeName(this.mSubType));
        } else {
            builder.append(this.mSubType);
        }
        if (this.mNetworkId != null) {
            builder.append(", networkId=").append(this.mNetworkId);
        }
        if (this.mRoaming) {
            builder.append(", ROAMING");
        }
        builder.append(", metered=").append(this.mMetered);
        return builder.append("}").toString();
    }

    public void writeToProto(ProtoOutputStream proto, long tag) {
        long start = proto.start(tag);
        proto.write(WorkSourceContentProto.UID, this.mType);
        if (this.mSubscriberId != null) {
            proto.write(1159641169922L, scrubSubscriberId(this.mSubscriberId));
        }
        proto.write(LocaleProto.VARIANT, this.mNetworkId);
        proto.write(1155346202628L, this.mRoaming);
        proto.write(1155346202629L, this.mMetered);
        proto.end(start);
    }

    public int getType() {
        return this.mType;
    }

    public int getSubType() {
        return this.mSubType;
    }

    public String getSubscriberId() {
        return this.mSubscriberId;
    }

    public String getNetworkId() {
        return this.mNetworkId;
    }

    public boolean getRoaming() {
        return this.mRoaming;
    }

    public boolean getMetered() {
        return this.mMetered;
    }

    public static String scrubSubscriberId(String subscriberId) {
        if ("eng".equals(Build.TYPE)) {
            return subscriberId;
        }
        if (subscriberId != null) {
            return subscriberId.substring(0, Math.min(6, subscriberId.length())) + "...";
        }
        return "null";
    }

    public static String[] scrubSubscriberId(String[] subscriberId) {
        if (subscriberId == null) {
            return null;
        }
        String[] res = new String[subscriberId.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = scrubSubscriberId(subscriberId[i]);
        }
        return res;
    }

    public static NetworkIdentity buildNetworkIdentity(Context context, NetworkState state) {
        int type = state.networkInfo.getType();
        int subType = state.networkInfo.getSubtype();
        String subscriberId = null;
        String networkId = null;
        boolean roaming = false;
        boolean metered = state.networkCapabilities.hasCapability(11) ^ 1;
        if (ConnectivityManager.isNetworkTypeMobile(type)) {
            subscriberId = state.subscriberId;
            roaming = state.networkInfo.isRoaming();
        } else if (type == 1) {
            if (state.networkId != null) {
                networkId = state.networkId;
            } else {
                WifiInfo info = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                networkId = info != null ? info.getSSID() : null;
            }
        }
        return new NetworkIdentity(type, subType, subscriberId, networkId, roaming, metered);
    }

    public int compareTo(NetworkIdentity another) {
        int res = Integer.compare(this.mType, another.mType);
        if (res == 0) {
            res = Integer.compare(this.mSubType, another.mSubType);
        }
        if (!(res != 0 || this.mSubscriberId == null || another.mSubscriberId == null)) {
            res = this.mSubscriberId.compareTo(another.mSubscriberId);
        }
        if (!(res != 0 || this.mNetworkId == null || another.mNetworkId == null)) {
            res = this.mNetworkId.compareTo(another.mNetworkId);
        }
        if (res == 0) {
            res = Boolean.compare(this.mRoaming, another.mRoaming);
        }
        if (res == 0) {
            return Boolean.compare(this.mMetered, another.mMetered);
        }
        return res;
    }
}
