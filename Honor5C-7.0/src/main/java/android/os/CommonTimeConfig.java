package android.os;

import android.net.ProxyInfo;
import android.os.IBinder.DeathRecipient;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

public class CommonTimeConfig {
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -4;
    public static final int ERROR_DEAD_OBJECT = -7;
    public static final long INVALID_GROUP_ID = -1;
    private static final int METHOD_FORCE_NETWORKLESS_MASTER_MODE = 17;
    private static final int METHOD_GET_AUTO_DISABLE = 15;
    private static final int METHOD_GET_CLIENT_SYNC_INTERVAL = 11;
    private static final int METHOD_GET_INTERFACE_BINDING = 7;
    private static final int METHOD_GET_MASTER_ANNOUNCE_INTERVAL = 9;
    private static final int METHOD_GET_MASTER_ELECTION_ENDPOINT = 3;
    private static final int METHOD_GET_MASTER_ELECTION_GROUP_ID = 5;
    private static final int METHOD_GET_MASTER_ELECTION_PRIORITY = 1;
    private static final int METHOD_GET_PANIC_THRESHOLD = 13;
    private static final int METHOD_SET_AUTO_DISABLE = 16;
    private static final int METHOD_SET_CLIENT_SYNC_INTERVAL = 12;
    private static final int METHOD_SET_INTERFACE_BINDING = 8;
    private static final int METHOD_SET_MASTER_ANNOUNCE_INTERVAL = 10;
    private static final int METHOD_SET_MASTER_ELECTION_ENDPOINT = 4;
    private static final int METHOD_SET_MASTER_ELECTION_GROUP_ID = 6;
    private static final int METHOD_SET_MASTER_ELECTION_PRIORITY = 2;
    private static final int METHOD_SET_PANIC_THRESHOLD = 14;
    public static final String SERVICE_NAME = "common_time.config";
    public static final int SUCCESS = 0;
    private DeathRecipient mDeathHandler;
    private String mInterfaceDesc;
    private final Object mListenerLock;
    private IBinder mRemote;
    private OnServerDiedListener mServerDiedListener;
    private CommonTimeUtils mUtils;

    public interface OnServerDiedListener {
        void onServerDied();
    }

    public CommonTimeConfig() throws RemoteException {
        this.mListenerLock = new Object();
        this.mServerDiedListener = null;
        this.mRemote = null;
        this.mInterfaceDesc = ProxyInfo.LOCAL_EXCL_LIST;
        this.mDeathHandler = new DeathRecipient() {
            public void binderDied() {
                synchronized (CommonTimeConfig.this.mListenerLock) {
                    if (CommonTimeConfig.this.mServerDiedListener != null) {
                        CommonTimeConfig.this.mServerDiedListener.onServerDied();
                    }
                }
            }
        };
        this.mRemote = ServiceManager.getService(SERVICE_NAME);
        if (this.mRemote == null) {
            throw new RemoteException();
        }
        this.mInterfaceDesc = this.mRemote.getInterfaceDescriptor();
        this.mUtils = new CommonTimeUtils(this.mRemote, this.mInterfaceDesc);
        this.mRemote.linkToDeath(this.mDeathHandler, 0);
    }

    public static CommonTimeConfig create() {
        try {
            return new CommonTimeConfig();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void release() {
        if (this.mRemote != null) {
            try {
                this.mRemote.unlinkToDeath(this.mDeathHandler, 0);
            } catch (NoSuchElementException e) {
            }
            this.mRemote = null;
        }
        this.mUtils = null;
    }

    public byte getMasterElectionPriority() throws RemoteException {
        throwOnDeadServer();
        return (byte) this.mUtils.transactGetInt(METHOD_GET_MASTER_ELECTION_PRIORITY, ERROR);
    }

    public int setMasterElectionPriority(byte priority) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetInt(METHOD_SET_MASTER_ELECTION_PRIORITY, priority);
    }

    public InetSocketAddress getMasterElectionEndpoint() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetSockaddr(METHOD_GET_MASTER_ELECTION_ENDPOINT);
    }

    public int setMasterElectionEndpoint(InetSocketAddress ep) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetSockaddr(METHOD_SET_MASTER_ELECTION_ENDPOINT, ep);
    }

    public long getMasterElectionGroupId() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetLong(METHOD_GET_MASTER_ELECTION_GROUP_ID, INVALID_GROUP_ID);
    }

    public int setMasterElectionGroupId(long id) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetLong(METHOD_SET_MASTER_ELECTION_GROUP_ID, id);
    }

    public String getInterfaceBinding() throws RemoteException {
        throwOnDeadServer();
        String ifaceName = this.mUtils.transactGetString(METHOD_GET_INTERFACE_BINDING, null);
        if (ifaceName == null || ifaceName.length() != 0) {
            return ifaceName;
        }
        return null;
    }

    public int setNetworkBinding(String ifaceName) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        CommonTimeUtils commonTimeUtils = this.mUtils;
        if (ifaceName == null) {
            ifaceName = ProxyInfo.LOCAL_EXCL_LIST;
        }
        return commonTimeUtils.transactSetString(METHOD_SET_INTERFACE_BINDING, ifaceName);
    }

    public int getMasterAnnounceInterval() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(METHOD_GET_MASTER_ANNOUNCE_INTERVAL, ERROR);
    }

    public int setMasterAnnounceInterval(int interval) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetInt(METHOD_SET_MASTER_ANNOUNCE_INTERVAL, interval);
    }

    public int getClientSyncInterval() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(METHOD_GET_CLIENT_SYNC_INTERVAL, ERROR);
    }

    public int setClientSyncInterval(int interval) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetInt(METHOD_SET_CLIENT_SYNC_INTERVAL, interval);
    }

    public int getPanicThreshold() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(METHOD_GET_PANIC_THRESHOLD, ERROR);
    }

    public int setPanicThreshold(int threshold) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetInt(METHOD_SET_PANIC_THRESHOLD, threshold);
    }

    public boolean getAutoDisable() throws RemoteException {
        throwOnDeadServer();
        if (METHOD_GET_MASTER_ELECTION_PRIORITY == this.mUtils.transactGetInt(METHOD_GET_AUTO_DISABLE, METHOD_GET_MASTER_ELECTION_PRIORITY)) {
            return true;
        }
        return false;
    }

    public int setAutoDisable(boolean autoDisable) {
        if (checkDeadServer()) {
            return ERROR_DEAD_OBJECT;
        }
        return this.mUtils.transactSetInt(METHOD_SET_AUTO_DISABLE, autoDisable ? METHOD_GET_MASTER_ELECTION_PRIORITY : 0);
    }

    public int forceNetworklessMasterMode() {
        int readInt;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(METHOD_FORCE_NETWORKLESS_MASTER_MODE, data, reply, 0);
            readInt = reply.readInt();
            return readInt;
        } catch (RemoteException e) {
            readInt = ERROR_DEAD_OBJECT;
            return readInt;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void setServerDiedListener(OnServerDiedListener listener) {
        synchronized (this.mListenerLock) {
            this.mServerDiedListener = listener;
        }
    }

    protected void finalize() throws Throwable {
        release();
    }

    private boolean checkDeadServer() {
        return this.mRemote == null || this.mUtils == null;
    }

    private void throwOnDeadServer() throws RemoteException {
        if (checkDeadServer()) {
            throw new RemoteException();
        }
    }
}
