package ohos.wifi;

import java.util.ArrayList;
import java.util.List;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

class WifiDeviceProxy extends WifiCommProxy implements IWifiDevice {
    private static final int COMMAND_ADD_DEVICE_CONFIG = 10;
    private static final int COMMAND_ADD_STREAM_LISTENER = 22;
    private static final int COMMAND_ADD_UNTRUSTED_CONFIG = 24;
    private static final int COMMAND_CONNECT_TO_DEVICE = 6;
    private static final int COMMAND_DISABLE_NETWORK = 18;
    private static final int COMMAND_DISCONNECT = 8;
    private static final int COMMAND_FACTORY_RESET = 19;
    private static final int COMMAND_GET_COUNTRY_CODE = 20;
    private static final int COMMAND_GET_DEVICE_CONFIGS = 11;
    private static final int COMMAND_GET_DEVICE_MAC_ADDRESS = 13;
    private static final int COMMAND_GET_IP_INFO = 7;
    private static final int COMMAND_GET_LINKED_INFO = 5;
    private static final int COMMAND_GET_SCAN_INFO_LIST = 4;
    private static final int COMMAND_GET_SUPPORTED_FEATURES = 21;
    private static final int COMMAND_GET_WIFI_POWER_STATE = 2;
    private static final int COMMAND_IS_FEATURE_SUPPORTED = 12;
    private static final int COMMAND_REASSOCIATE = 16;
    private static final int COMMAND_RECONNECT = 17;
    private static final int COMMAND_REMOVE_DEVICE = 9;
    private static final int COMMAND_REMOVE_STREAM_LISTENER = 23;
    private static final int COMMAND_REMOVE_UNTRUSTED_CONFIG = 25;
    private static final int COMMAND_SCAN = 3;
    private static final int COMMAND_SET_WIFI_POWER_STATE = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiDeviceProxy");
    private static final int MAX_DEVICE_MAC_SIZE = 8;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final Object PROXY_LOCK = new Object();
    private static volatile WifiDeviceProxy sInstance = null;

    private WifiDeviceProxy(int i) {
        super(i);
    }

    public static WifiDeviceProxy getInstance() {
        if (sInstance == null) {
            synchronized (PROXY_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiDeviceProxy(SystemAbilityDefinition.WIFI_DEVICE_SYS_ABILITY_ID);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean setWifiPowerState(String str, boolean z) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        create.writeInt(z ? 1 : 0);
        if (request(1, create).readInt() == 1) {
            return true;
        }
        return false;
    }

    @Override // ohos.wifi.IWifiDevice
    public int getWifiPowerState() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        return request(2, create).readInt();
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean scan(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        return request(3, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public List<WifiScanInfo> getScanInfoList(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        MessageParcel request = request(4, create);
        ArrayList arrayList = new ArrayList();
        int readInt = request.readInt();
        for (int i = 0; i < readInt; i++) {
            if (request.getReadableBytes() <= 0) {
                HiLog.warn(LABEL, "read parcel failed in proxy, Scan Info Size = %{public}d", Integer.valueOf(readInt));
                return new ArrayList();
            }
            WifiScanInfo wifiScanInfo = new WifiScanInfo();
            request.readSequenceable(wifiScanInfo);
            arrayList.add(wifiScanInfo);
        }
        return arrayList;
    }

    @Override // ohos.wifi.IWifiDevice
    public WifiLinkedInfo getLinkedInfo(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        MessageParcel request = request(5, create);
        WifiLinkedInfo wifiLinkedInfo = new WifiLinkedInfo();
        request.readSequenceable(wifiLinkedInfo);
        return wifiLinkedInfo;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean connectToDevice(int i, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeInt(i);
        create.writeString(str);
        return request(6, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public IpInfo getIpInfo() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        MessageParcel request = request(7, create);
        IpInfo ipInfo = new IpInfo();
        request.readSequenceable(ipInfo);
        return ipInfo;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean disconnect(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        return request(8, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean removeDevice(int i, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeInt(i);
        create.writeString(str);
        return request(9, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public int addDeviceConfig(WifiDeviceConfig wifiDeviceConfig, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeSequenceable(wifiDeviceConfig);
        create.writeString(str);
        return request(10, create).readInt();
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean isConnected() throws RemoteException {
        IpInfo ipInfo = getIpInfo();
        return (ipInfo == null || ipInfo.getIpAddress() == 0) ? false : true;
    }

    @Override // ohos.wifi.IWifiDevice
    public List<WifiDeviceConfig> getDeviceConfigs(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        MessageParcel request = request(11, create);
        ArrayList arrayList = new ArrayList();
        int readInt = request.readInt();
        for (int i = 0; i < readInt; i++) {
            if (request.getReadableBytes() <= 0) {
                HiLog.warn(LABEL, "read parcel failed in proxy, Wifi device configs Size = %{public}d", Integer.valueOf(readInt));
                return new ArrayList();
            }
            WifiDeviceConfig wifiDeviceConfig = new WifiDeviceConfig();
            request.readSequenceable(wifiDeviceConfig);
            arrayList.add(wifiDeviceConfig);
        }
        return arrayList;
    }

    @Override // ohos.wifi.IWifiDevice
    public String[] getDeviceMacAddress() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        MessageParcel request = request(13, create);
        int readInt = request.readInt();
        if (readInt < 0 || readInt > 8) {
            HiLog.warn(LABEL, "Illegal mac size from reply! Size = %{public}d", Integer.valueOf(readInt));
            return new String[0];
        }
        String[] strArr = new String[readInt];
        for (int i = 0; i < readInt; i++) {
            if (request.getReadableBytes() <= 0) {
                HiLog.warn(LABEL, "read parcel failed in proxy, Wifi device mac Size = %{public}d", Integer.valueOf(readInt));
                return new String[0];
            }
            strArr[i] = request.readString();
        }
        return strArr;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean reassociate(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        return request(16, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean reconnect(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        return request(17, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean disableNetwork(String str, int i) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        create.writeInt(i);
        return request(18, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public void factoryReset(String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        request(19, create);
    }

    @Override // ohos.wifi.IWifiDevice
    public String getCountryCode() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        return request(20, create).readString();
    }

    @Override // ohos.wifi.IWifiDevice
    public long getSupportedFeatures() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        return request(21, create).readLong();
    }

    @Override // ohos.wifi.IWifiDevice
    public void addStreamListener(StreamListener streamListener, EventRunner eventRunner, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeRemoteObject(new RemoteObject(str));
        create.writeRemoteObject(new StreamListenerProxy(eventRunner, streamListener, str).asObject());
        create.writeInt(streamListener.hashCode());
        request(22, create);
    }

    @Override // ohos.wifi.IWifiDevice
    public void removeStreamListener(StreamListener streamListener) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeInt(streamListener.hashCode());
        request(23, create);
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean addUntrustedConfig(WifiDeviceConfig wifiDeviceConfig, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        create.writeSequenceable(wifiDeviceConfig);
        return request(24, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiDevice
    public boolean removeUntrustedConfig(WifiDeviceConfig wifiDeviceConfig, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeString(str);
        create.writeSequenceable(wifiDeviceConfig);
        return request(25, create).readInt() == 1;
    }
}
