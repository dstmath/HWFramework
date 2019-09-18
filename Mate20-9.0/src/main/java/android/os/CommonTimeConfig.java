package android.os;

import android.os.IBinder;
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
    private IBinder.DeathRecipient mDeathHandler;
    private String mInterfaceDesc;
    /* access modifiers changed from: private */
    public final Object mListenerLock;
    private IBinder mRemote;
    /* access modifiers changed from: private */
    public OnServerDiedListener mServerDiedListener;
    private CommonTimeUtils mUtils;

    public interface OnServerDiedListener {
        void onServerDied();
    }

    public CommonTimeConfig() throws RemoteException {
        this.mListenerLock = new Object();
        this.mServerDiedListener = null;
        this.mRemote = null;
        this.mInterfaceDesc = "";
        this.mDeathHandler = new IBinder.DeathRecipient() {
            public void binderDied() {
                synchronized (CommonTimeConfig.this.mListenerLock) {
                    if (CommonTimeConfig.this.mServerDiedListener != null) {
                        CommonTimeConfig.this.mServerDiedListener.onServerDied();
                    }
                }
            }
        };
        this.mRemote = ServiceManager.getService(SERVICE_NAME);
        if (this.mRemote != null) {
            this.mInterfaceDesc = this.mRemote.getInterfaceDescriptor();
            this.mUtils = new CommonTimeUtils(this.mRemote, this.mInterfaceDesc);
            this.mRemote.linkToDeath(this.mDeathHandler, 0);
            return;
        }
        throw new RemoteException();
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
        return (byte) this.mUtils.transactGetInt(1, -1);
    }

    public int setMasterElectionPriority(byte priority) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetInt(2, priority);
    }

    public InetSocketAddress getMasterElectionEndpoint() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetSockaddr(3);
    }

    public int setMasterElectionEndpoint(InetSocketAddress ep) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetSockaddr(4, ep);
    }

    public long getMasterElectionGroupId() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetLong(5, -1);
    }

    public int setMasterElectionGroupId(long id) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetLong(6, id);
    }

    public String getInterfaceBinding() throws RemoteException {
        throwOnDeadServer();
        String ifaceName = this.mUtils.transactGetString(7, null);
        if (ifaceName == null || ifaceName.length() != 0) {
            return ifaceName;
        }
        return null;
    }

    public int setNetworkBinding(String ifaceName) {
        String str;
        if (checkDeadServer()) {
            return -7;
        }
        CommonTimeUtils commonTimeUtils = this.mUtils;
        if (ifaceName == null) {
            str = "";
        } else {
            str = ifaceName;
        }
        return commonTimeUtils.transactSetString(8, str);
    }

    public int getMasterAnnounceInterval() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(9, -1);
    }

    public int setMasterAnnounceInterval(int interval) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetInt(10, interval);
    }

    public int getClientSyncInterval() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(11, -1);
    }

    public int setClientSyncInterval(int interval) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetInt(12, interval);
    }

    public int getPanicThreshold() throws RemoteException {
        throwOnDeadServer();
        return this.mUtils.transactGetInt(13, -1);
    }

    public int setPanicThreshold(int threshold) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetInt(14, threshold);
    }

    public boolean getAutoDisable() throws RemoteException {
        throwOnDeadServer();
        return 1 == this.mUtils.transactGetInt(15, 1);
    }

    public int setAutoDisable(boolean autoDisable) {
        if (checkDeadServer()) {
            return -7;
        }
        return this.mUtils.transactSetInt(16, autoDisable);
    }

    public int forceNetworklessMasterMode() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(17, data, reply, 0);
            return reply.readInt();
        } catch (RemoteException e) {
            return -7;
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
