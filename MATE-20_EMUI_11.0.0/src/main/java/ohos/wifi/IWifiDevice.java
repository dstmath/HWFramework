package ohos.wifi;

import java.util.List;
import ohos.eventhandler.EventRunner;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

interface IWifiDevice extends IRemoteBroker {
    int addDeviceConfig(WifiDeviceConfig wifiDeviceConfig, String str) throws RemoteException;

    void addStreamListener(StreamListener streamListener, EventRunner eventRunner, String str) throws RemoteException;

    boolean connectToDevice(int i, String str) throws RemoteException;

    boolean disableNetwork(String str, int i) throws RemoteException;

    boolean disconnect(String str) throws RemoteException;

    void factoryReset(String str) throws RemoteException;

    String getCountryCode() throws RemoteException;

    List<WifiDeviceConfig> getDeviceConfigs(String str) throws RemoteException;

    String[] getDeviceMacAddress() throws RemoteException;

    IpInfo getIpInfo() throws RemoteException;

    WifiLinkedInfo getLinkedInfo(String str) throws RemoteException;

    List<WifiScanInfo> getScanInfoList(String str) throws RemoteException;

    long getSupportedFeatures() throws RemoteException;

    int getWifiPowerState() throws RemoteException;

    boolean isConnected() throws RemoteException;

    boolean reassociate(String str) throws RemoteException;

    boolean reconnect(String str) throws RemoteException;

    boolean removeDevice(int i, String str) throws RemoteException;

    void removeStreamListener(StreamListener streamListener) throws RemoteException;

    boolean scan(String str) throws RemoteException;

    boolean setWifiPowerState(String str, boolean z) throws RemoteException;
}
