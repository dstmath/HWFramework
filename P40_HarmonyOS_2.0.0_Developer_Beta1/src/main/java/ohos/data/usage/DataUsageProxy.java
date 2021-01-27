package ohos.data.usage;

import com.huawei.ohos.interwork.AndroidDataHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.net.Uri;

public final class DataUsageProxy implements IDataUsage {
    private static final HashMap<IDataUsageCallback, DataUsageListenerStub> CALLBACK_MAP = new HashMap<>();
    private static final int DATA_USAGE_DAEMON_SERVICE_ID = 1301;
    private static final String DESCRIPTOR = "OHOS.DistributedKv.IKvStoreDataService";
    private static final int DFS_CMD_FORMAT_VOLUME = 32;
    private static final int DFS_CMD_GET_DISK_MOUNTED_STATUS = 20;
    private static final int DFS_CMD_GET_DISK_MOUNTED_STATUS_BY_FILE = 21;
    private static final int DFS_CMD_GET_PRIMARY_VOLUME = 25;
    private static final int DFS_CMD_GET_STORAGE_VOLUME_WITH_FILE = 26;
    private static final int DFS_CMD_GET_STORAGE_VOLUME_WITH_URI = 27;
    private static final int DFS_CMD_GET_VOLUMES = 24;
    private static final int DFS_CMD_GET_VOLUME_VIEWS = 36;
    private static final int DFS_CMD_INSTALL_VOLUME = 30;
    private static final int DFS_CMD_IS_DISK_PLUGGABLE = 22;
    private static final int DFS_CMD_IS_DISK_PLUGGABLE_BY_FILE = 23;
    private static final int DFS_CMD_PARTITION_MIXED_DISK = 35;
    private static final int DFS_CMD_PARTITION_PRIVATE_DISK = 34;
    private static final int DFS_CMD_PARTITION_PUBLIC_DISK = 33;
    private static final int DFS_CMD_REGISTER_CALLBACK = 28;
    private static final int DFS_CMD_UNINSTALL_VOLUME = 31;
    private static final int DFS_CMD_UNREGISTER_CALLBACK = 29;
    private static final int HANDLE_CALLBACK_SUCCESS = 1;
    private static final String H_SCHEME = "zcontent";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109441, "DataUsageProxy");
    private static final Lock LOCK = new ReentrantLock();
    private static final int MAX_RATIO = 90;
    private static final int MAX_VOLUME_NUM = 20;
    private static final String MEDIA_AUTHORITY = "media";
    private static final int MIN_RATIO = 10;
    private static volatile IDataUsage proxy;
    private final IRemoteObject remote;

    /* access modifiers changed from: package-private */
    public interface IUriHelper {
        Optional<ResultSet> query(Uri uri, String[] strArr) throws DataAbilityRemoteException;

        void release();
    }

    static {
        System.loadLibrary("ipc_core.z");
    }

    private DataUsageProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public static IDataUsage getInstance() throws RemoteException {
        if (proxy == null) {
            try {
                LOCK.lock();
                if (proxy == null) {
                    IRemoteObject sysAbility = SysAbilityManager.getSysAbility((int) DATA_USAGE_DAEMON_SERVICE_ID);
                    if (sysAbility != null) {
                        HiLog.info(LABEL, "DataUsageProxy-getInstance: get the binder", new Object[0]);
                        sysAbility.addDeathRecipient(new ServerDeathRecipient(), 0);
                        proxy = new DataUsageProxy(sysAbility);
                    } else {
                        HiLog.error(LABEL, "DataUsageProxy-getInstance: get data usage service fail", new Object[0]);
                        throw new RemoteException();
                    }
                }
            } finally {
                LOCK.unlock();
            }
        }
        return proxy;
    }

    @Override // ohos.data.usage.IDataUsage
    public MountState getDiskMountedStatus() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        MountState mountState = MountState.DISK_UNMOUNTED;
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                return mountState;
            }
            this.remote.sendRequest(20, create, create2, messageOption);
            MountState status = MountState.getStatus(create2.readString());
            HiLog.debug(LABEL, "DataUsageProxy-getDiskMountedStatus: status = %{public}s", new Object[]{status.getDescription()});
            create.reclaim();
            create2.reclaim();
            return status;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public MountState getDiskMountedStatus(File file) throws RemoteException {
        MountState mountState = MountState.DISK_UNMOUNTED;
        if (file == null) {
            return mountState;
        }
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                create.reclaim();
                create2.reclaim();
                return mountState;
            }
            create.writeString(canonicalPath);
            this.remote.sendRequest(21, create, create2, messageOption);
            mountState = MountState.getStatus(create2.readString());
            HiLog.debug(LABEL, "DataUsageProxy-getDiskMountedStatus: status = %{public}s", new Object[]{mountState.getDescription()});
            create.reclaim();
            create2.reclaim();
            return mountState;
        } catch (IOException e) {
            HiLog.error(LABEL, "DataUsageProxy-getDiskMountedStatus: get file %{public} path exception %{public}s", new Object[]{file.getName(), e.getMessage()});
        } catch (Throwable th) {
            create.reclaim();
            create2.reclaim();
            throw th;
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean isDiskPluggable() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                return false;
            }
            this.remote.sendRequest(22, create, create2, messageOption);
            boolean z = create2.readInt() == 1;
            HiLog.debug(LABEL, "DataUsageProxy-isDiskPluggable: result = %{public}s", new Object[]{Boolean.valueOf(z)});
            create.reclaim();
            create2.reclaim();
            return z;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean isDiskPluggable(File file) throws RemoteException {
        IOException e;
        boolean z;
        if (file == null) {
            return false;
        }
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                create.reclaim();
                create2.reclaim();
                return false;
            }
            create.writeString(canonicalPath);
            this.remote.sendRequest(23, create, create2, messageOption);
            boolean z2 = create2.readInt() == 1;
            try {
                HiLog.debug(LABEL, "DataUsageProxy-isDiskPluggable: status = %{public}s", new Object[]{Boolean.valueOf(z2)});
                create.reclaim();
                create2.reclaim();
                return z2;
            } catch (IOException e2) {
                z = z2;
                e = e2;
                try {
                    HiLog.error(LABEL, "DataUsageProxy-isDiskPluggable: get file %{public} path exception %{public}s", new Object[]{file.getName(), e.getMessage()});
                    return z;
                } finally {
                    create.reclaim();
                    create2.reclaim();
                }
            }
        } catch (IOException e3) {
            e = e3;
            z = false;
            HiLog.error(LABEL, "DataUsageProxy-isDiskPluggable: get file %{public} path exception %{public}s", new Object[]{file.getName(), e.getMessage()});
            return z;
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public Optional<List<Volume>> getVolumes() throws RemoteException {
        Optional<List<Volume>> of;
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        ArrayList arrayList = new ArrayList();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                of = Optional.of(arrayList);
            } else {
                this.remote.sendRequest(24, create, create2, messageOption);
                int readInt = create2.readInt();
                HiLog.info(LABEL, "DataUsageProxy-getVolumes: get volume size is %{public}d", new Object[]{Integer.valueOf(readInt)});
                if (readInt > 20) {
                    of = Optional.of(arrayList);
                } else {
                    for (int i = 0; i < readInt; i++) {
                        arrayList.add(new Volume(create2));
                    }
                    create.reclaim();
                    create2.reclaim();
                    return Optional.of(arrayList);
                }
            }
            return of;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public Optional<Volume> getPrimaryVolume() throws RemoteException {
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        Optional<Volume> empty = Optional.empty();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                return empty;
            }
            this.remote.sendRequest(25, create, create2, messageOption);
            if (create2.readInt() == 1) {
                empty = Optional.of(new Volume(create2));
            }
            create.reclaim();
            create2.reclaim();
            return empty;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public Optional<Volume> getVolume(File file) throws RemoteException {
        Optional<Volume> empty = Optional.empty();
        if (file == null) {
            return empty;
        }
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                create.reclaim();
                create2.reclaim();
                return empty;
            }
            create.writeString(file.getCanonicalPath());
            this.remote.sendRequest(26, create, create2, messageOption);
            if (create2.readInt() == 1) {
                empty = Optional.of(new Volume(create2));
            }
            create.reclaim();
            create2.reclaim();
            return empty;
        } catch (IOException e) {
            HiLog.error(LABEL, "DataUsageProxy-getStorageVolume: get file %{public} path exception %{public}s", new Object[]{file.getName(), e.getMessage()});
        } catch (Throwable th) {
            create.reclaim();
            create2.reclaim();
            throw th;
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public Optional<Volume> getVolume(Context context, Uri uri) throws RemoteException {
        Optional<Volume> empty = Optional.empty();
        if (uri == null || context == null) {
            return empty;
        }
        Optional<String> uriFilePath = getUriFilePath(context, uri);
        if (!uriFilePath.isPresent()) {
            HiLog.info(LABEL, "DataUsageProxy-getVolume: get null path", new Object[0]);
            return empty;
        }
        HiLog.info(LABEL, "DataUsageProxy-getVolume: the uri path is %s", new Object[]{uriFilePath.get()});
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                return empty;
            }
            create.writeString(uriFilePath.get());
            this.remote.sendRequest(27, create, create2, messageOption);
            if (create2.readInt() == 1) {
                empty = Optional.of(new Volume(create2));
            }
            create.reclaim();
            create2.reclaim();
            return empty;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean isDiskEmulated() throws RemoteException {
        Optional<Volume> primaryVolume = getPrimaryVolume();
        if (primaryVolume.isPresent()) {
            return primaryVolume.get().isEmulated();
        }
        return false;
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean isDiskEmulated(File file) throws RemoteException {
        if (file == null) {
            return false;
        }
        Optional<Volume> volume = getVolume(file);
        if (volume.isPresent()) {
            return volume.get().isEmulated();
        }
        return false;
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean registerCallback(IDataUsageCallback iDataUsageCallback) throws RemoteException {
        boolean z = false;
        if (iDataUsageCallback == null) {
            HiLog.info(LABEL, "DataUsageProxy-registerCallback: null callback", new Object[0]);
            return false;
        }
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            LOCK.lock();
            if (CALLBACK_MAP.containsKey(iDataUsageCallback)) {
                HiLog.info(LABEL, "DataUsageProxy-registerCallback: the callback has been registed", new Object[0]);
                return false;
            }
            DataUsageListenerStub dataUsageListenerStub = new DataUsageListenerStub(iDataUsageCallback);
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return false;
            }
            boolean writeRemoteObject = create.writeRemoteObject(dataUsageListenerStub.asObject());
            if (!writeRemoteObject) {
                HiLog.info(LABEL, "DataUsageProxy-registerCallback: fail to write stub object", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return writeRemoteObject;
            }
            boolean sendRequest = this.remote.sendRequest(28, create, create2, messageOption);
            if (!sendRequest) {
                HiLog.info(LABEL, "DataUsageProxy-registerCallback: fail to send register request", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return sendRequest;
            }
            if (create2.readInt() == 1) {
                z = true;
            }
            if (z) {
                CALLBACK_MAP.put(iDataUsageCallback, dataUsageListenerStub);
            }
            LOCK.unlock();
            create.reclaim();
            create2.reclaim();
            return z;
        } finally {
            LOCK.unlock();
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public boolean unRegisterCallback(IDataUsageCallback iDataUsageCallback) throws RemoteException {
        boolean z = false;
        if (iDataUsageCallback == null) {
            HiLog.info(LABEL, "DataUsageProxy-unRegisterCallback: null callback", new Object[0]);
            return false;
        }
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        try {
            LOCK.lock();
            if (!CALLBACK_MAP.containsKey(iDataUsageCallback)) {
                HiLog.info(LABEL, "DataUsageProxy-unRegisterCallback: the callback has not registed", new Object[0]);
                return false;
            }
            DataUsageListenerStub dataUsageListenerStub = CALLBACK_MAP.get(iDataUsageCallback);
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return false;
            }
            boolean writeRemoteObject = create.writeRemoteObject(dataUsageListenerStub.asObject());
            if (!writeRemoteObject) {
                HiLog.info(LABEL, "DataUsageProxy-unRegisterCallback: fail to write stub object", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return writeRemoteObject;
            }
            boolean sendRequest = this.remote.sendRequest(29, create, create2, messageOption);
            if (!sendRequest) {
                HiLog.info(LABEL, "DataUsageProxy-unRegisterCallback: fail to send register request", new Object[0]);
                LOCK.unlock();
                create.reclaim();
                create2.reclaim();
                return sendRequest;
            }
            if (create2.readInt() == 1) {
                z = true;
            }
            if (z) {
                CALLBACK_MAP.remove(iDataUsageCallback);
            }
            LOCK.unlock();
            create.reclaim();
            create2.reclaim();
            return z;
        } finally {
            LOCK.unlock();
            create.reclaim();
            create2.reclaim();
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void installVolume(String str) throws RemoteException {
        if (str != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                this.remote.sendRequest(30, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void uninstallVolume(String str) throws RemoteException {
        if (str != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                this.remote.sendRequest(31, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void formatVolume(String str) throws RemoteException {
        if (str != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                this.remote.sendRequest(32, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void partitionPublicDisk(String str) throws RemoteException {
        if (str != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                this.remote.sendRequest(33, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void partitionPrivateDisk(String str) throws RemoteException {
        if (str != null) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                this.remote.sendRequest(34, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public void partitionMixedDisk(String str, int i) throws RemoteException {
        if (str != null && i >= 10 && i <= 90) {
            MessageParcel create = MessageParcel.create();
            MessageParcel create2 = MessageParcel.create();
            MessageOption messageOption = new MessageOption();
            try {
                if (!create.writeInterfaceToken(DESCRIPTOR)) {
                    HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                    return;
                }
                create.writeString(str);
                create.writeInt(i);
                this.remote.sendRequest(35, create, create2, messageOption);
                create.reclaim();
                create2.reclaim();
            } finally {
                create.reclaim();
                create2.reclaim();
            }
        }
    }

    @Override // ohos.data.usage.IDataUsage
    public Optional<List<VolumeView>> getVolumeViews() throws RemoteException {
        Optional<List<VolumeView>> of;
        MessageParcel create = MessageParcel.create();
        MessageParcel create2 = MessageParcel.create();
        MessageOption messageOption = new MessageOption();
        ArrayList arrayList = new ArrayList();
        try {
            if (!create.writeInterfaceToken(DESCRIPTOR)) {
                HiLog.error(LABEL, "writeInterfaceToken failed", new Object[0]);
                of = Optional.of(arrayList);
            } else {
                this.remote.sendRequest(36, create, create2, messageOption);
                int readInt = create2.readInt();
                if (readInt > 20) {
                    of = Optional.of(arrayList);
                } else {
                    for (int i = 0; i < readInt; i++) {
                        arrayList.add(new VolumeView(create2));
                    }
                    create.reclaim();
                    create2.reclaim();
                    return Optional.of(arrayList);
                }
            }
            return of;
        } finally {
            create.reclaim();
            create2.reclaim();
        }
    }

    private Optional<String> getUriFilePath(Context context, Uri uri) {
        IUriHelper iUriHelper;
        if (!Objects.equals(uri.getEncodedAuthority(), MEDIA_AUTHORITY)) {
            return Optional.empty();
        }
        if (H_SCHEME.equals(uri.getScheme())) {
            iUriHelper = new HUriHelper(context, uri);
        } else {
            iUriHelper = new AndroidUriHelper(context, uri);
        }
        return getUriFilePath(iUriHelper, uri);
    }

    private Optional<String> getUriFilePath(IUriHelper iUriHelper, Uri uri) {
        Optional<String> empty = Optional.empty();
        try {
            Optional<ResultSet> query = iUriHelper.query(uri, new String[]{"_data"});
            if (!query.isPresent() || query.get().getRowCount() <= 0) {
                HiLog.error(LABEL, "DataUsageProxy-getUriFilePath, query uri %{private}s fail", new Object[]{uri.toString()});
                iUriHelper.release();
                return empty;
            }
            while (true) {
                if (!query.get().goToNextRow()) {
                    break;
                }
                String string = query.get().getString(query.get().getColumnIndexForName("_data"));
                if (!string.isEmpty()) {
                    HiLog.info(LABEL, "DataUsageProxy-getUriFilePath, uri path is %s", new Object[]{string});
                    empty = Optional.of(string);
                    break;
                }
            }
            iUriHelper.release();
            return empty;
        } catch (DataAbilityRemoteException e) {
            HiLog.error(LABEL, "DataUsageProxy-getUriFilePath, query uri %s exception %s{public}s", new Object[]{uri.toString(), e.getMessage()});
        } catch (Throwable th) {
            iUriHelper.release();
            throw th;
        }
    }

    private static class ServerDeathRecipient implements IRemoteObject.DeathRecipient {
        private ServerDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.info(DataUsageProxy.LABEL, "ServerDeathRecipient-onRemoteDied: server die", new Object[0]);
            DataUsageProxy.LOCK.lock();
            IDataUsage unused = DataUsageProxy.proxy = null;
            DataUsageProxy.CALLBACK_MAP.clear();
            DataUsageProxy.LOCK.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public class AndroidUriHelper implements IUriHelper {
        private AndroidDataHelper androidDataHelper;

        public AndroidUriHelper(Context context, Uri uri) {
            this.androidDataHelper = AndroidDataHelper.creator(context, uri);
        }

        @Override // ohos.data.usage.DataUsageProxy.IUriHelper
        public Optional<ResultSet> query(Uri uri, String[] strArr) throws DataAbilityRemoteException {
            ResultSet query = this.androidDataHelper.query(uri, strArr, null);
            if (query == null) {
                return Optional.empty();
            }
            return Optional.of(query);
        }

        @Override // ohos.data.usage.DataUsageProxy.IUriHelper
        public void release() {
            this.androidDataHelper.release();
        }
    }

    /* access modifiers changed from: package-private */
    public class HUriHelper implements IUriHelper {
        private DataAbilityHelper dataAbilityHelper;

        public HUriHelper(Context context, Uri uri) {
            this.dataAbilityHelper = DataAbilityHelper.creator(context, uri);
        }

        @Override // ohos.data.usage.DataUsageProxy.IUriHelper
        public Optional<ResultSet> query(Uri uri, String[] strArr) throws DataAbilityRemoteException {
            ResultSet query = this.dataAbilityHelper.query(uri, strArr, null);
            if (query == null) {
                return Optional.empty();
            }
            return Optional.of(query);
        }

        @Override // ohos.data.usage.DataUsageProxy.IUriHelper
        public void release() {
            this.dataAbilityHelper.release();
        }
    }
}
