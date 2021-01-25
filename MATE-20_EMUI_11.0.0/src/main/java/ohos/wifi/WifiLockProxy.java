package ohos.wifi;

import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class WifiLockProxy extends WifiCommProxy implements IWifiLock {
    private static int COMMAND_ACQUIRE_WIFI_LOCK = 14;
    private static int COMMAND_RELEASE_WIFI_LOCK = 15;
    private static final int ERR_OK = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiLockProxy");
    private static final int MIN_TRANSACTION_ID = 1;
    private static final Object PROXY_LOCK = new Object();
    private static volatile WifiLockProxy sInstance = null;

    private WifiLockProxy(int i) {
        super(i);
    }

    public static WifiLockProxy getInstance() {
        if (sInstance == null) {
            synchronized (PROXY_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiLockProxy(SystemAbilityDefinition.WIFI_DEVICE_SYS_ABILITY_ID);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.wifi.IWifiLock
    public int acquire(IRemoteObject iRemoteObject, String str, int i, String str2) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeRemoteObject(iRemoteObject);
        create.writeString(str);
        create.writeInt(i);
        create.writeString(str2);
        return request(COMMAND_ACQUIRE_WIFI_LOCK, create).readInt();
    }

    @Override // ohos.wifi.IWifiLock
    public int release(IRemoteObject iRemoteObject, int i, String str) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        create.writeString(InnerUtils.WIFI_INTERFACE_TOKEN);
        create.writeRemoteObject(iRemoteObject);
        create.writeInt(i);
        create.writeString(str);
        return request(COMMAND_RELEASE_WIFI_LOCK, create).readInt();
    }
}
