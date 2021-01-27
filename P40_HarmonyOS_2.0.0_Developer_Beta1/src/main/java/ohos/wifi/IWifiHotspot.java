package ohos.wifi;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

interface IWifiHotspot extends IRemoteBroker {
    boolean enableHotspot(boolean z) throws RemoteException;

    HotspotConfig getHotspotConfig() throws RemoteException;

    List<StationInfo> getStationList() throws RemoteException;

    boolean isHotspotActive() throws RemoteException;

    boolean isHotspotDualBandSupported() throws RemoteException;

    boolean setHotspotConfig(HotspotConfig hotspotConfig, String str) throws RemoteException;
}
