package ohos.wifi;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

interface IWifiEnhancer extends IRemoteBroker {
    boolean bindMpNetwork(boolean z, WifiMpConfig wifiMpConfig) throws RemoteException;

    boolean connectDc(WifiDeviceConfig wifiDeviceConfig) throws RemoteException;

    boolean disconnectDc() throws RemoteException;

    boolean isInMpLinkState(int i) throws RemoteException;

    boolean isWifiDcActive() throws RemoteException;
}
