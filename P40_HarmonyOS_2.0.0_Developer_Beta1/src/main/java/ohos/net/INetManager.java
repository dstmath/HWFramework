package ohos.net;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface INetManager extends IRemoteBroker {
    NetSpecifier addDefaultNetStatusCallback(NetRemoteEvent netRemoteEvent) throws RemoteException;

    NetSpecifier addNetStatusCallback(NetSpecifier netSpecifier, NetRemoteEvent netRemoteEvent) throws RemoteException;

    boolean enableAirplaneMode(boolean z) throws RemoteException;

    boolean enableUsbHotspot(boolean z, String str) throws RemoteException;

    NetHandle[] getAllNets() throws RemoteException;

    long getAllStatis(int i) throws RemoteException;

    int getBackgroundPolicy() throws RemoteException;

    String[] getCellularIfaces() throws RemoteException;

    ConnectionProperties getConnectionProperties(NetHandle netHandle) throws RemoteException;

    NetHandle getDefaultNet() throws RemoteException;

    long getHotspotStats(int i) throws RemoteException;

    HttpProxy getHttpProxyForNet(NetHandle netHandle) throws RemoteException;

    long getIfaceStatis(String str, int i) throws RemoteException;

    NetCapabilities getNetCapabilities(NetHandle netHandle) throws RemoteException;

    String[] getNetHotspotAbleIfaces() throws RemoteException;

    String[] getNetHotspotIfaces() throws RemoteException;

    long getUidStatis(int i, int i2) throws RemoteException;

    boolean hasDefaultNet() throws RemoteException;

    boolean isDefaultNetMetered() throws RemoteException;

    boolean isHotspotSupported(String str) throws RemoteException;

    void releaseNetworkRequest(NetSpecifier netSpecifier) throws RemoteException;

    NetSpecifier setupSpecificNet(NetSpecifier netSpecifier, NetRemoteEvent netRemoteEvent) throws RemoteException;

    boolean startLegacyVpn(VpnProfile vpnProfile) throws RemoteException;
}
