package ohos.net;

public class NetStatusCallback {
    public static final int NETID_UNSET = 0;
    public NetSpecifier networkRequest;

    public void onAvailable(NetHandle netHandle) {
    }

    public void onBlockedStatusChanged(NetHandle netHandle, boolean z) {
    }

    public void onCapabilitiesChanged(NetHandle netHandle, NetCapabilities netCapabilities) {
    }

    public void onConnectionPropertiesChanged(NetHandle netHandle, ConnectionProperties connectionProperties) {
    }

    public void onLosing(NetHandle netHandle, long j) {
    }

    public void onLost(NetHandle netHandle) {
    }

    public void onUnavailable() {
    }

    public void onAvailable(NetHandle netHandle, NetCapabilities netCapabilities, ConnectionProperties connectionProperties) {
        onAvailable(netHandle);
        onCapabilitiesChanged(netHandle, netCapabilities);
        onConnectionPropertiesChanged(netHandle, connectionProperties);
    }
}
