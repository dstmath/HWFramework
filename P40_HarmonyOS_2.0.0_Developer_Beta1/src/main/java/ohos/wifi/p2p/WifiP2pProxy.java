package ohos.wifi.p2p;

import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Sequenceable;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;
import ohos.wifi.InnerUtils;
import ohos.wifi.WifiCommProxy;

class WifiP2pProxy extends WifiCommProxy implements IWifiP2pController {
    private static final int COMMAND_DELETE_PERSISTENT_GROUP = 3;
    private static final int COMMAND_P2P_INIT = 1;
    private static final int COMMAND_P2P_REQUEST = 2;
    private static final int COMMAND_SET_DEVICE_NAME = 4;
    private static final int ERR_OK = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pProxy");
    private static final int MIN_TRANSACTION_ID = 1;
    private static final Object PROXY_LOCK = new Object();
    private static volatile WifiP2pProxy sInstance = null;

    private WifiP2pProxy(int i) {
        super(i);
    }

    public static WifiP2pProxy getInstance() {
        if (sInstance == null) {
            synchronized (PROXY_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiP2pProxy(SystemAbilityDefinition.WIFI_P2P_SYS_ABILITY_ID);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.wifi.p2p.IWifiP2pController
    public IRemoteObject init(IRemoteObject iRemoteObject, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.P2P_INTERFACE_TOKEN);
        create.writeRemoteObject(iRemoteObject);
        create.writeString(str);
        return request(1, create).readRemoteObject();
    }

    @Override // ohos.wifi.p2p.IWifiP2pController
    public void sendP2pRequest(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, Sequenceable sequenceable, int i2, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.P2P_INTERFACE_TOKEN);
        create.writeInt(i);
        create.writeRemoteObject(iRemoteObject);
        create.writeRemoteObject(iRemoteObject2);
        create.writeSequenceable(sequenceable);
        create.writeInt(i2);
        create.writeString(str);
        request(2, create);
    }

    @Override // ohos.wifi.p2p.IWifiP2pController
    public void deletePersistentGroup(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, int i2, int i3, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.P2P_INTERFACE_TOKEN);
        create.writeInt(i);
        create.writeRemoteObject(iRemoteObject);
        create.writeRemoteObject(iRemoteObject2);
        create.writeInt(i2);
        create.writeInt(i3);
        create.writeString(str);
        request(3, create);
    }

    @Override // ohos.wifi.p2p.IWifiP2pController
    public void setDeviceName(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, String str, int i2, String str2) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.P2P_INTERFACE_TOKEN);
        create.writeInt(i);
        create.writeRemoteObject(iRemoteObject);
        create.writeRemoteObject(iRemoteObject2);
        create.writeString(str);
        create.writeInt(i2);
        create.writeString(str2);
        request(4, create);
    }
}
