package ohos.wifi.p2p;

import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;

public final class WifiP2pController {
    private static final int BASE = 139264;
    private static final int CANCEL_CONNECT = 139274;
    private static final int CONNECT = 139271;
    private static final int CREATE_GROUP = 139277;
    private static final String DEFAULT_CALLER = "ohos";
    private static final int DELETE_PERSISTENT_GROUP = 139318;
    public static final int DEVICE_INFO_REQUEST = 2;
    public static final int DEVICE_LIST_REQUEST = 6;
    private static final Object DEVICE_LOCK = new Object();
    private static final int DISCOVER_PEERS = 139265;
    public static final int GROUP_INFO_REQUEST = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pController");
    @SystemApi
    public static final int LINKED_INFO_REQUEST = 3;
    public static final int NETWORK_INFO_REQUEST = 4;
    @SystemApi
    public static final int PERSISTENT_GROUP_INFO_REQUEST = 5;
    private static final int REMOVE_GROUP = 139280;
    private static final int REQUEST_CONNECTION_INFO = 139285;
    private static final int REQUEST_DEVICE_INFO = 139361;
    private static final int REQUEST_GROUP_INFO = 139287;
    private static final int REQUEST_NETWORK_INFO = 139358;
    private static final int REQUEST_PEERS = 139283;
    private static final int REQUEST_PERSISTENT_GROUP_INFO = 139321;
    private static final int SET_DEVICE_NAME = 139315;
    private static final int START_LISTEN = 139329;
    private static final int STOP_DISCOVERY = 139268;
    private static final int STOP_LISTEN = 139332;
    private static volatile WifiP2pController sInstance = null;
    private int mCallbackCount = 0;
    private final Context mContext;
    private WifiP2pMessenger mLocalMessenger;
    private IRemoteObject mRemoteMessenger;
    private final WifiP2pProxy mWifiP2pProxy;

    private WifiP2pController(Context context) {
        this.mContext = context;
        this.mWifiP2pProxy = WifiP2pProxy.getInstance();
    }

    public static WifiP2pController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DEVICE_LOCK) {
                if (sInstance == null) {
                    sInstance = new WifiP2pController(context);
                }
            }
        }
        return sInstance;
    }

    public IRemoteObject init(EventRunner eventRunner, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        IRemoteObject iRemoteObject;
        HiLog.info(LABEL, "init", new Object[0]);
        Context context = this.mContext;
        String bundleName = (context == null || context.getAbilityInfo() == null) ? DEFAULT_CALLER : this.mContext.getAbilityInfo().getBundleName();
        synchronized (DEVICE_LOCK) {
            this.mLocalMessenger = new WifiP2pMessenger(eventRunner);
            this.mRemoteMessenger = this.mWifiP2pProxy.init(this.mLocalMessenger.asObject(), bundleName);
            iRemoteObject = this.mRemoteMessenger;
        }
        return iRemoteObject;
    }

    public void discoverDevices(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "discoverDevices", new Object[0]);
            sendP2pRequest(DISCOVER_PEERS, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to discoverDevices", new Object[0]);
        }
    }

    public void stopDeviceDiscovery(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "stopDeviceDiscovery", new Object[0]);
            sendP2pRequest(STOP_DISCOVERY, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to stopDeviceDiscovery", new Object[0]);
        }
    }

    public void createGroup(WifiP2pConfig wifiP2pConfig, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "createGroup", new Object[0]);
            sendP2pRequest(CREATE_GROUP, wifiP2pCallback, wifiP2pConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to createGroup", new Object[0]);
        }
    }

    public void removeGroup(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "removeGroup", new Object[0]);
            sendP2pRequest(REMOVE_GROUP, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to removeGroup", new Object[0]);
        }
    }

    public void requestP2pInfo(int i, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        int i2;
        try {
            HiLog.info(LABEL, "requestP2pInfo, requestType is %{public}d", Integer.valueOf(i));
            switch (i) {
                case 1:
                    i2 = REQUEST_GROUP_INFO;
                    break;
                case 2:
                    i2 = REQUEST_DEVICE_INFO;
                    break;
                case 3:
                    i2 = REQUEST_CONNECTION_INFO;
                    break;
                case 4:
                    i2 = REQUEST_NETWORK_INFO;
                    break;
                case 5:
                    i2 = REQUEST_PERSISTENT_GROUP_INFO;
                    break;
                case 6:
                    i2 = REQUEST_PEERS;
                    break;
                default:
                    HiLog.warn(LABEL, "ignored requestType: %{public}d", Integer.valueOf(i));
                    i2 = 0;
                    break;
            }
            sendP2pRequest(i2, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to requestP2pInfo", new Object[0]);
        }
    }

    public void connect(WifiP2pConfig wifiP2pConfig, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "connect", new Object[0]);
            sendP2pRequest(CONNECT, wifiP2pCallback, wifiP2pConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to connect", new Object[0]);
        }
    }

    public void cancelConnect(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "cancelConnect", new Object[0]);
            sendP2pRequest(CANCEL_CONNECT, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to cancelConnect", new Object[0]);
        }
    }

    @SystemApi
    public void startListen(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "startListen", new Object[0]);
            sendP2pRequest(START_LISTEN, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to startListen", new Object[0]);
        }
    }

    @SystemApi
    public void stopListen(WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "stopListen", new Object[0]);
            sendP2pRequest(STOP_LISTEN, wifiP2pCallback, null);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to stopListen", new Object[0]);
        }
    }

    @SystemApi
    public void deletePersistentGroup(int i, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "deletePersistentGroup", new Object[0]);
            synchronized (DEVICE_LOCK) {
                if (this.mLocalMessenger == null || this.mRemoteMessenger == null) {
                    HiLog.warn(LABEL, "mLocalMessenger, mRemoteMessenger or wifiP2pConfig is null", new Object[0]);
                    throw new RemoteException();
                }
            }
            String str = DEFAULT_CALLER;
            if (!(this.mContext == null || this.mContext.getAbilityInfo() == null)) {
                str = this.mContext.getAbilityInfo().getBundleName();
            }
            synchronized (DEVICE_LOCK) {
                WifiP2pMessenger wifiP2pMessenger = this.mLocalMessenger;
                int i2 = this.mCallbackCount + 1;
                this.mCallbackCount = i2;
                wifiP2pMessenger.addCallback(i2, wifiP2pCallback);
                this.mWifiP2pProxy.deletePersistentGroup(DELETE_PERSISTENT_GROUP, this.mLocalMessenger.asObject(), this.mRemoteMessenger, i, this.mCallbackCount, str);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to deletePersistentGroup", new Object[0]);
        }
    }

    @SystemApi
    public void setDeviceName(String str, WifiP2pCallback wifiP2pCallback) throws RemoteException {
        try {
            HiLog.info(LABEL, "setDeviceName", new Object[0]);
            synchronized (DEVICE_LOCK) {
                if (this.mLocalMessenger == null || this.mRemoteMessenger == null) {
                    HiLog.warn(LABEL, "mLocalMessenger, mRemoteMessenger or wifiP2pConfig is null", new Object[0]);
                    throw new RemoteException();
                }
            }
            String str2 = DEFAULT_CALLER;
            if (!(this.mContext == null || this.mContext.getAbilityInfo() == null)) {
                str2 = this.mContext.getAbilityInfo().getBundleName();
            }
            synchronized (DEVICE_LOCK) {
                WifiP2pMessenger wifiP2pMessenger = this.mLocalMessenger;
                int i = this.mCallbackCount + 1;
                this.mCallbackCount = i;
                wifiP2pMessenger.addCallback(i, wifiP2pCallback);
                this.mWifiP2pProxy.setDeviceName(SET_DEVICE_NAME, this.mLocalMessenger.asObject(), this.mRemoteMessenger, str, this.mCallbackCount, str2);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Exception to setDeviceName", new Object[0]);
        }
    }

    private void sendP2pRequest(int i, WifiP2pCallback wifiP2pCallback, Sequenceable sequenceable) throws RemoteException {
        synchronized (DEVICE_LOCK) {
            if (this.mLocalMessenger == null || this.mRemoteMessenger == null) {
                HiLog.warn(LABEL, "mLocalMessenger or mRemoteMessenger is null", new Object[0]);
                throw new RemoteException();
            }
        }
        Context context = this.mContext;
        String bundleName = (context == null || context.getAbilityInfo() == null) ? DEFAULT_CALLER : this.mContext.getAbilityInfo().getBundleName();
        synchronized (DEVICE_LOCK) {
            WifiP2pMessenger wifiP2pMessenger = this.mLocalMessenger;
            int i2 = this.mCallbackCount + 1;
            this.mCallbackCount = i2;
            wifiP2pMessenger.addCallback(i2, wifiP2pCallback);
            this.mWifiP2pProxy.sendP2pRequest(i, this.mLocalMessenger.asObject(), this.mRemoteMessenger, sequenceable, this.mCallbackCount, bundleName);
        }
    }
}
