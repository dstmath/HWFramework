package ohos.wifi;

import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

class WifiEnhancerProxy extends WifiCommProxy implements IWifiEnhancer {
    private static final int COMMAND_BIND_MP_NETWORK = 1;
    private static final int COMMAND_CONNECT_DC = 3;
    private static final int COMMAND_DISCONNECT_DC = 5;
    private static final int COMMAND_IS_IN_MP_LINK_STATE = 2;
    private static final int COMMAND_IS_WIFI_DC_ACTIVE = 4;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_ENHANCER, "WifiEnhancerProxy");
    private static final int MINI_TRANSACTION_ID = 1;
    private static final Object PROXY_LOCK = new Object();
    private static volatile WifiEnhancerProxy sInstance = null;

    private WifiEnhancerProxy(int i) {
        super(i);
    }

    public static WifiEnhancerProxy getInstance() {
        if (sInstance == null) {
            synchronized (PROXY_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiEnhancerProxy(SystemAbilityDefinition.WIFI_ENHANCER_SYS_ABILITY_ID);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.wifi.IWifiEnhancer
    public boolean bindMpNetwork(boolean z, WifiMpConfig wifiMpConfig) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.ENHANCER_INTERFACE_TOKEN);
        create.writeInt(z ? 1 : 0);
        create.writeSequenceable(wifiMpConfig);
        if (request(1, create).readInt() == 1) {
            return true;
        }
        return false;
    }

    @Override // ohos.wifi.IWifiEnhancer
    public boolean isInMpLinkState(int i) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.ENHANCER_INTERFACE_TOKEN);
        create.writeInt(i);
        return request(2, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiEnhancer
    public boolean connectDc(WifiDeviceConfig wifiDeviceConfig) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.ENHANCER_INTERFACE_TOKEN);
        create.writeSequenceable(wifiDeviceConfig);
        return request(3, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiEnhancer
    public boolean isWifiDcActive() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.ENHANCER_INTERFACE_TOKEN);
        return request(4, create).readInt() == 1;
    }

    @Override // ohos.wifi.IWifiEnhancer
    public boolean disconnectDc() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.ENHANCER_INTERFACE_TOKEN);
        return request(5, create).readInt() == 1;
    }

    public boolean bindMpNetwork(WifiMpConfig wifiMpConfig) throws RemoteException {
        try {
            return bindMpNetwork(true, wifiMpConfig);
        } catch (RemoteException unused) {
            throw new RemoteException();
        }
    }

    public boolean unBindMpNetwork(WifiMpConfig wifiMpConfig) throws RemoteException {
        try {
            return bindMpNetwork(false, wifiMpConfig);
        } catch (RemoteException unused) {
            throw new RemoteException();
        }
    }

    public boolean bindMpNetwork() throws RemoteException {
        try {
            return bindMpNetwork(true, null);
        } catch (RemoteException unused) {
            throw new RemoteException();
        }
    }

    public boolean unBindMpNetwork() throws RemoteException {
        try {
            return bindMpNetwork(false, null);
        } catch (RemoteException unused) {
            throw new RemoteException();
        }
    }
}
