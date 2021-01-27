package ohos.net;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.ReliableFileDescriptor;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

class NetManagerProxy implements INetManager {
    private static final String DESCRIPTOR = "android.net.IConnectivityManager";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NETMGRPROXY");
    private static final int LISTEN = 1;
    private static final String NETMGRDESCRIPTOR = "android.os.INetworkManagementService";
    private static final String REMOTE_DESCRIPTOR = "ohos.net.NetManager";
    private static final int REQUEST = 2;
    private static final int SERVICE_RESPONSE_SUCCESS = 1;
    private static final int STRICTPOLICY = 1;
    private static final int TRANSACT_ADD_NET_STATUSCALLBACK = 3;
    private static final int TRANSACT_ENABLE_AIRPLANE_MODE = 0;
    private static final int TRANSACT_ENABLE_USB_HOTSPOT = 21;
    private static final int TRANSACT_GET_ALL_NETS = 5;
    private static final int TRANSACT_GET_ALL_STATIS = 16;
    private static final int TRANSACT_GET_BACKGROUND_POLICY = 13;
    private static final int TRANSACT_GET_CELLULAR_INTERFACE_ARRAY = 14;
    private static final int TRANSACT_GET_CONNECTION_PROPERTIES = 12;
    private static final int TRANSACT_GET_DEFAULT_NET = 4;
    private static final int TRANSACT_GET_HOTSPOT_ABLED_IFACE = 18;
    private static final int TRANSACT_GET_HOTSPOT_IFACE = 19;
    private static final int TRANSACT_GET_HOTSPOT_STATS = 20;
    private static final int TRANSACT_GET_HTTP_PROXY_FOR_NET = 7;
    private static final int TRANSACT_GET_INTERFACE_STATIS = 15;
    private static final int TRANSACT_GET_NET_CAPABILITIES = 6;
    private static final int TRANSACT_GET_UID_STATIS = 17;
    private static final int TRANSACT_HAS_DEFAULT_NET = 10;
    private static final int TRANSACT_IS_DEFAULT_METERED = 11;
    private static final int TRANSACT_IS_HOTSPOT_SUPPORTED = 22;
    private static final int TRANSACT_PRE_CREATE_VPN = 24;
    private static final int TRANSACT_SEND_NET_CONNECT_STATE = 23;
    private static final int TRANSACT_SETUP_SPECIFIC_NET = 2;
    private static final int TRANSACT_SET_DEFAULT_NET_HTTP_PROXY = 1;
    private static final int TRANSACT_SET_UP_VPN = 26;
    private static final int TRANSACT_SET_VPN_PACKAGE_AUTHORIZATION = 25;
    private static final int TRANSACT_START_LEGACY_VPN = 8;
    private static final int TRANSACT_TEARDOWN_NET = 9;
    private static final int TYPE_MOBILE = 0;
    private static final int TYPE_MOBILE_BIP0 = 38;
    private static final int TYPE_MOBILE_BIP1 = 39;
    private static final int TYPE_MOBILE_BIP2 = 40;
    private static final int TYPE_MOBILE_BIP3 = 41;
    private static final int TYPE_MOBILE_BIP4 = 42;
    private static final int TYPE_MOBILE_BIP5 = 43;
    private static final int TYPE_MOBILE_BIP6 = 44;
    private static final int TYPE_MOBILE_CBS = 12;
    private static final int TYPE_MOBILE_DUN = 4;
    private static final int TYPE_MOBILE_FOTA = 10;
    private static final int TYPE_MOBILE_HIPRI = 5;
    private static final int TYPE_MOBILE_IMS = 11;
    private static final int TYPE_MOBILE_INTERNAL_DEFAULT = 48;
    private static final int TYPE_MOBILE_SUPL = 3;
    private static final int TYPE_MOBILE_XCAP = 45;
    private static final int TYPE_NONE = -1;
    private static final int TYPE_WIFI = 1;
    private static final int WORKSOURCE = 1;
    private static NetManagerProxy sInstance = null;
    private final Object mLock = new Object();
    private IRemoteObject mNetManagerRemote = null;

    private NetManagerProxy() {
    }

    public static NetManagerProxy getInstance() {
        if (sInstance == null) {
            sInstance = new NetManagerProxy();
        }
        return sInstance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.mLock) {
            if (this.mNetManagerRemote != null) {
                return this.mNetManagerRemote;
            }
            this.mNetManagerRemote = SysAbilityManager.getSysAbility(SystemAbilityDefinition.NET_MANAGER_SYS_ABILITY_ID);
            if (this.mNetManagerRemote != null) {
                this.mNetManagerRemote.addDeathRecipient(new NetManagerDeathRecipient(), 0);
            } else {
                HiLog.error(LABEL, "getSysAbility(NetManager) failed.", new Object[0]);
            }
            return this.mNetManagerRemote;
        }
    }

    @Override // ohos.net.INetManager
    public boolean enableAirplaneMode(boolean z) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                boolean z2 = true;
                create.writeInt(z ? 1 : 0);
                asObject.sendRequest(0, create, create2, messageOption);
                if (create2.readInt() != 1) {
                    z2 = false;
                }
                create.reclaim();
                create2.reclaim();
                return z2;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to enableAirplaneMode", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for enableAirplaneMode", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public NetSpecifier setupSpecificNet(NetSpecifier netSpecifier, NetRemoteEvent netRemoteEvent) throws RemoteException {
        if (netSpecifier == null) {
            return null;
        }
        NetCapabilities netCapabilities = netSpecifier.netCapabilities;
        HiLog.warn(LABEL, "setupSpecificNet", new Object[0]);
        return sendNetRequest(netCapabilities, 2, netRemoteEvent);
    }

    @Override // ohos.net.INetManager
    public NetSpecifier addNetStatusCallback(NetSpecifier netSpecifier, NetRemoteEvent netRemoteEvent) throws RemoteException {
        NetCapabilities netCapabilities;
        if (netSpecifier == null || (netCapabilities = netSpecifier.netCapabilities) == null) {
            return null;
        }
        HiLog.warn(LABEL, "addNetStatusCallback", new Object[0]);
        return sendNetRequest(netCapabilities, 1, netRemoteEvent);
    }

    @Override // ohos.net.INetManager
    public NetSpecifier addDefaultNetStatusCallback(NetRemoteEvent netRemoteEvent) throws RemoteException {
        HiLog.warn(LABEL, "addDefaultNetStatusCallback", new Object[0]);
        return sendNetRequest(null, 2, netRemoteEvent);
    }

    @Override // ohos.net.INetManager
    public void releaseNetworkRequest(NetSpecifier netSpecifier) throws RemoteException {
        HiLog.warn(LABEL, "releaseNetworkRequest", new Object[0]);
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                create.writeInt(1);
                netSpecifier.marshalling(create);
                asObject.sendRequest(9, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to releaseNetworkRequest", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for releaseNetworkRequest", new Object[0]);
            throw new RemoteException();
        }
    }

    private int getLegacyType(NetCapabilities netCapabilities) {
        if (netCapabilities == null || !netCapabilities.hasBearer(0)) {
            return -1;
        }
        if (netCapabilities.hasCap(5)) {
            return 12;
        }
        if (netCapabilities.hasCap(4)) {
            return 11;
        }
        if (netCapabilities.hasCap(3)) {
            return 10;
        }
        if (netCapabilities.hasCap(2)) {
            return 4;
        }
        if (netCapabilities.hasCap(1)) {
            return 3;
        }
        if (netCapabilities.hasCap(12)) {
            return 5;
        }
        if (netCapabilities.hasCap(25)) {
            return 38;
        }
        if (netCapabilities.hasCap(26)) {
            return 39;
        }
        if (netCapabilities.hasCap(27)) {
            return 40;
        }
        if (netCapabilities.hasCap(28)) {
            return 41;
        }
        if (netCapabilities.hasCap(29)) {
            return 42;
        }
        if (netCapabilities.hasCap(30)) {
            return 43;
        }
        if (netCapabilities.hasCap(31)) {
            return 44;
        }
        if (netCapabilities.hasCap(9)) {
            return TYPE_MOBILE_XCAP;
        }
        if (netCapabilities.hasCap(32)) {
            return TYPE_MOBILE_INTERNAL_DEFAULT;
        }
        return -1;
    }

    private void writeNetCapabilities(NetCapabilities netCapabilities, MessageParcel messageParcel) {
        if (netCapabilities != null) {
            messageParcel.writeInt(1);
            netCapabilities.marshalling(messageParcel);
            return;
        }
        messageParcel.writeInt(0);
    }

    private NetSpecifier sendNetRequest(NetCapabilities netCapabilities, int i, NetRemoteEvent netRemoteEvent) throws RemoteException {
        NetSpecifier netSpecifier;
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            RemoteObject remoteObject = new RemoteObject("netmanager");
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                writeNetCapabilities(netCapabilities, create);
                create.writeInt(1);
                create.writeRemoteObject(netRemoteEvent.asObject());
                if (i == 1) {
                    create.writeRemoteObject(remoteObject);
                    asObject.sendRequest(3, create, create2, messageOption);
                } else {
                    create.writeInt(0);
                    create.writeRemoteObject(remoteObject);
                    create.writeInt(getLegacyType(netCapabilities));
                    asObject.sendRequest(2, create, create2, messageOption);
                }
                if (create2.readInt() == 1) {
                    create2.readInt();
                    netSpecifier = new NetSpecifier();
                    netSpecifier.unmarshalling(create2);
                } else {
                    netSpecifier = null;
                }
                create.reclaim();
                create2.reclaim();
                return netSpecifier;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to sendNetRequest", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for sendNetRequest", new Object[0]);
            throw new RemoteException();
        }
    }

    /* access modifiers changed from: private */
    public class NetManagerDeathRecipient implements IRemoteObject.DeathRecipient {
        private NetManagerDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(NetManagerProxy.LABEL, "NetManagerDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (NetManagerProxy.this.mLock) {
                NetManagerProxy.this.mNetManagerRemote = null;
            }
        }
    }

    private void writeInterfaceToken(String str, MessageParcel messageParcel) {
        messageParcel.writeInt(1);
        messageParcel.writeInt(1);
        messageParcel.writeString(str);
    }

    @Override // ohos.net.INetManager
    public NetHandle getDefaultNet() throws RemoteException {
        NetHandle netHandle;
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                asObject.sendRequest(4, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    netHandle = new NetHandle();
                    create2.readSequenceable(netHandle);
                } else {
                    netHandle = null;
                }
                create.reclaim();
                create2.reclaim();
                return netHandle;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getDefaultNet", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getDefaultNet", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public NetHandle[] getAllNets() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                asObject.sendRequest(5, create, create2, messageOption);
                if (create2.readInt() != 1) {
                    create.reclaim();
                    create2.reclaim();
                    return null;
                }
                int readInt = create2.readInt();
                NetHandle[] netHandleArr = new NetHandle[readInt];
                for (int i = 0; i < readInt; i++) {
                    netHandleArr[i] = new NetHandle();
                    create2.readSequenceable(netHandleArr[i]);
                }
                create.reclaim();
                create2.reclaim();
                return netHandleArr;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getAllNets", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getAllNets", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public boolean hasDefaultNet() throws RemoteException {
        IRemoteObject asObject = asObject();
        boolean z = false;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(NETMGRDESCRIPTOR, create);
                asObject.sendRequest(10, create, create2, messageOption);
                if (create2.readInt() == 1 && create2.readInt() == 1) {
                    z = true;
                }
                create.reclaim();
                create2.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to hasDefaultNet", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for hasDefaultNet", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public NetCapabilities getNetCapabilities(NetHandle netHandle) throws RemoteException {
        NetCapabilities netCapabilities;
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                if (netHandle != null) {
                    create.writeInt(1);
                    netHandle.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                asObject.sendRequest(6, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    netCapabilities = new NetCapabilities();
                    create2.readSequenceable(netCapabilities);
                } else {
                    netCapabilities = null;
                }
                create.reclaim();
                create2.reclaim();
                return netCapabilities;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getNetCapabilities", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getNetCapabilities", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public HttpProxy getHttpProxyForNet(NetHandle netHandle) throws RemoteException {
        HttpProxy httpProxy;
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                if (netHandle != null) {
                    create.writeInt(1);
                    netHandle.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                asObject.sendRequest(7, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    httpProxy = new HttpProxy();
                    create2.readSequenceable(httpProxy);
                } else {
                    httpProxy = null;
                }
                create.reclaim();
                create2.reclaim();
                return httpProxy;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getHttpProxyForNet", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getHttpProxyForNet", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public boolean startLegacyVpn(VpnProfile vpnProfile) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                boolean z = true;
                if (vpnProfile != null) {
                    create.writeInt(1);
                    vpnProfile.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                asObject.sendRequest(8, create, create2, messageOption);
                if (create2.readInt() != 1) {
                    z = false;
                }
                create.reclaim();
                create2.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to startLegacyVpn", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for startLegacyVpn", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public boolean isDefaultNetMetered() throws RemoteException {
        IRemoteObject asObject = asObject();
        boolean z = false;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                asObject.sendRequest(11, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    z = create2.readBoolean();
                }
                create.reclaim();
                create2.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to isDefaultNetMetered", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for isDefaultNetMetered", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public ConnectionProperties getConnectionProperties(NetHandle netHandle) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            ConnectionProperties connectionProperties = null;
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                if (netHandle != null) {
                    create.writeInt(1);
                    netHandle.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                asObject.sendRequest(12, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    connectionProperties = new ConnectionProperties();
                    create2.readSequenceable(connectionProperties);
                }
                create.reclaim();
                create2.reclaim();
                return connectionProperties;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getConnectionProperties", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getConnectionProperties", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public int getBackgroundPolicy() throws RemoteException {
        IRemoteObject asObject = asObject();
        int i = 0;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                asObject.sendRequest(13, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    i = create2.readInt();
                }
                create.reclaim();
                create2.reclaim();
                return i;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getBackgroundPolicy", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getBackgroundPolicy", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public String[] getCellularIfaces() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            String[] strArr = new String[0];
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                asObject.sendRequest(14, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    strArr = create2.readStringArray();
                }
                create.reclaim();
                create2.reclaim();
                return strArr;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getCellularIfaces", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getCellularIfaces", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public long getIfaceStatis(String str, int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            long j = 0;
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeString(str);
                create.writeInt(i);
                asObject.sendRequest(15, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    j = create2.readLong();
                }
                create.reclaim();
                create2.reclaim();
                return j;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getIfaceStatis", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getIfaceStatis", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public long getAllStatis(int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            long j = 0;
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeInt(i);
                asObject.sendRequest(16, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    j = create2.readLong();
                }
                create.reclaim();
                create2.reclaim();
                return j;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getAllStatis", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getAllStatis", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public long getUidStatis(int i, int i2) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            long j = 0;
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeInt(i);
                create.writeInt(i2);
                asObject.sendRequest(17, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    j = create2.readLong();
                }
                create.reclaim();
                create2.reclaim();
                return j;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getUidStatis", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getUidStatis", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public String[] getNetHotspotAbleIfaces() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            String[] strArr = new String[0];
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                asObject.sendRequest(18, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    strArr = create2.readStringArray();
                }
                create.reclaim();
                create2.reclaim();
                return strArr;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getNetHotspotAbleIfaces", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getNetHotspotAbleIfaces", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public String[] getNetHotspotIfaces() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            String[] strArr = new String[0];
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                asObject.sendRequest(19, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    strArr = create2.readStringArray();
                }
                create.reclaim();
                create2.reclaim();
                return strArr;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getNetHotspotIfaces", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getNetHotspotIfaces", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public boolean isHotspotSupported(String str) throws RemoteException {
        IRemoteObject asObject = asObject();
        boolean z = false;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeString(str);
                asObject.sendRequest(22, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    z = create2.readBoolean();
                }
                create.reclaim();
                create2.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to isHotspotSupported", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for isHotspotSupported", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public long getHotspotStats(int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            long j = 0;
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeInt(i);
                asObject.sendRequest(20, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    j = create2.readLong();
                }
                create.reclaim();
                create2.reclaim();
                return j;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to getHotspotStats", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for getHotspotStats", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.net.INetManager
    public boolean enableUsbHotspot(boolean z, String str) throws RemoteException {
        IRemoteObject asObject = asObject();
        boolean z2 = false;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                create.writeBoolean(z);
                create.writeString(str);
                asObject.sendRequest(21, create, create2, messageOption);
                if (create2.readInt() == 1 && create2.readInt() == 0) {
                    z2 = true;
                }
                create.reclaim();
                create2.reclaim();
                return z2;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to enableUsbHotspot", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for enableUsbHotspot", new Object[0]);
            throw new RemoteException();
        }
    }

    public boolean sendNetConnectState(NetHandle netHandle, boolean z) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(REMOTE_DESCRIPTOR, create);
                boolean z2 = true;
                if (netHandle != null) {
                    create.writeInt(1);
                    netHandle.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                create.writeBoolean(z);
                asObject.sendRequest(23, create, create2, messageOption);
                if (create2.readInt() != 1) {
                    z2 = false;
                }
                create.reclaim();
                create2.reclaim();
                return z2;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to sendNetConnectState", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for sendNetConnectState", new Object[0]);
            throw new RemoteException();
        }
    }

    public boolean preCreateVpn(String str, String str2, int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        boolean z = false;
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                create.writeString(str);
                create.writeString(str2);
                create.writeInt(i);
                asObject.sendRequest(24, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    z = create2.readBoolean();
                }
                create.reclaim();
                create2.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to preCreateVpn", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for pre create vpn", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setVpnPackageAuthorization(String str, int i, boolean z) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                create.writeString(str);
                create.writeInt(i);
                create.writeInt(z ? 1 : 0);
                asObject.sendRequest(25, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to setVpnPackageAuthorization", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for pre create vpn", new Object[0]);
            throw new RemoteException();
        }
    }

    public ReliableFileDescriptor setUpVpn(VpnConfig vpnConfig) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            ReliableFileDescriptor reliableFileDescriptor = null;
            try {
                writeInterfaceToken(DESCRIPTOR, create);
                if (vpnConfig != null) {
                    create.writeInt(1);
                    vpnConfig.marshalling(create);
                } else {
                    create.writeInt(0);
                }
                asObject.sendRequest(26, create, create2, messageOption);
                if (create2.readInt() == 1) {
                    if (create2.readInt() == 1) {
                        reliableFileDescriptor = new ReliableFileDescriptor(create2.readFileDescriptor(), create2.readFileDescriptor());
                    } else {
                        reliableFileDescriptor = new ReliableFileDescriptor(create2.readFileDescriptor());
                    }
                }
                create.reclaim();
                create2.reclaim();
                return reliableFileDescriptor;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "Failed to setUpVpn", new Object[0]);
                throw e;
            } catch (Throwable th) {
                create.reclaim();
                create2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "Failed to get remote object for set up vpn", new Object[0]);
            throw new RemoteException();
        }
    }
}
