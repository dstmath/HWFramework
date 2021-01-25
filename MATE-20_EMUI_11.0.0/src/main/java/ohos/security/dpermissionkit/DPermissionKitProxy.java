package ohos.security.dpermissionkit;

import android.text.TextUtils;
import java.util.Objects;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.security.permission.BundleLabelInfo;
import ohos.security.permission.OnPermissionUsedRecord;
import ohos.security.permission.OnUsingPermissionReminder;
import ohos.security.permission.QueryPermissionUsedRequest;
import ohos.security.permission.QueryPermissionUsedResult;
import ohos.sysability.samgr.SysAbilityManager;

public class DPermissionKitProxy implements IDPermissionKit {
    private static final int ADD_PERMISSION_USED_RECORD = 65293;
    private static final int ALLOCATE_DUID = 65281;
    private static final int CHECK_DISTRIBUTED_PERMISSION = 65285;
    private static final int CHECK_D_PERMISSION_AND_USE = 65297;
    private static final int DEL_DUID_BY_DEVID = 65284;
    private static final int DEL_DUID_BY_DEVID_UID = 65283;
    private static final String DESCRIPTOR = "ohos.security.dpermission.DPermissionService";
    private static final int DPERMISSION_LOCAL_SERVICE_ID = 3501;
    private static final int GET_BUNDLE_LABEL_INFO = 65296;
    private static final int GET_PERMISSION_USED_RECORDS = 65294;
    private static final int GET_PERMISSION_USED_RECORDS_ASYNC = 65295;
    private static final int INIT_RESULT = -1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_DPERMISSION, "DPermissionKitProxy");
    private static final int MAINTAIN_COMMAND = 65299;
    private static final int NOTIFY_APP_STATUS_CHANGED = 65287;
    private static final int NOTIFY_PERMISSION_CHANGED = 65286;
    private static final int QUERY_DUID = 65282;
    private static final int REGISTER_ON_USING_PERMISSION_REMINDER = 65288;
    private static final int SUB_DOMAIN_SECURITY_DPERMISSION = 218115841;
    private static final int UNREGISTER_ON_USING_PERMISSION_REMINDER = 65289;
    private static volatile DPermissionKitProxy sInstance = null;
    private IRemoteObject mDPermissionRemoteService;
    private IRemoteObject.DeathRecipient mDeathRecipient;
    private final Object mLock;

    private DPermissionKitProxy() {
        this.mLock = new Object();
        this.mDPermissionRemoteService = null;
        this.mDeathRecipient = null;
        this.mDeathRecipient = new IRemoteObject.DeathRecipient() {
            /* class ohos.security.dpermissionkit.DPermissionKitProxy.AnonymousClass1 */

            @Override // ohos.rpc.IRemoteObject.DeathRecipient
            public void onRemoteDied() {
                HiLog.warn(DPermissionKitProxy.LABEL, "DPermissionKitProxy Received onRemoteDied Notify.", new Object[0]);
                synchronized (DPermissionKitProxy.this.mLock) {
                    DPermissionKitProxy.this.mDPermissionRemoteService = null;
                }
            }
        };
    }

    public static DPermissionKitProxy getInstance() {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new DPermissionKitProxy();
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.mLock) {
            if (this.mDPermissionRemoteService != null) {
                return this.mDPermissionRemoteService;
            }
            this.mDPermissionRemoteService = SysAbilityManager.getSysAbility(DPERMISSION_LOCAL_SERVICE_ID);
            if (this.mDPermissionRemoteService != null) {
                this.mDPermissionRemoteService.addDeathRecipient(this.mDeathRecipient, 0);
            } else {
                HiLog.error(LABEL, "DPermissionKitProxy getSysAbility %{public}d failed.", Integer.valueOf((int) DPERMISSION_LOCAL_SERVICE_ID));
            }
            return this.mDPermissionRemoteService;
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int allocateDuid(String str, int i) throws RemoteException {
        if (str == null) {
            HiLog.error(LABEL, "DPermissionKitProxy allocateDuid deviceId is null", new Object[0]);
            return -3;
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                asObject.sendRequest(ALLOCATE_DUID, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy allocateDuid fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to allocateDuid", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for allocateDuid", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int queryDuid(String str, int i) throws RemoteException {
        if (str == null) {
            HiLog.error(LABEL, "DPermissionKitProxy queryDuid deviceId is null", new Object[0]);
            return -3;
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                asObject.sendRequest(QUERY_DUID, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy queryDuid fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to queryDuid", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for queryDuid", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int delDuid(String str, int i) throws RemoteException {
        if (str == null) {
            HiLog.error(LABEL, "DPermissionKitProxy delDuid deviceId is null", new Object[0]);
            return -3;
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                asObject.sendRequest(DEL_DUID_BY_DEVID_UID, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy delDuid(by ruid) fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to delDuid by devId and rUid", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for delDuid by devId and rUid", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int delDuid(String str) throws RemoteException {
        if (str == null) {
            HiLog.error(LABEL, "DPermissionKitProxy delDuid by devid deviceId is null", new Object[0]);
            return -3;
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeString(str);
                asObject.sendRequest(DEL_DUID_BY_DEVID, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy delDuid(by devid) fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to delDuid by devId", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for delDuid by devId", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int checkDPermission(int i, String str) throws RemoteException {
        if (str == null) {
            HiLog.error(LABEL, "DPermissionKitProxy checkDPermission permissionName is null", new Object[0]);
            return -6;
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeInt(i);
                obtain.writeString(str);
                asObject.sendRequest(CHECK_DISTRIBUTED_PERMISSION, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy checkDPermission fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to checkDPermission", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for checkDPermission", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int notifyUidPermissionChanged(int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeInt(i);
                asObject.sendRequest(NOTIFY_PERMISSION_CHANGED, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy notifyUidPermissionChanged fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to notifyUidPermissionChanged", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for notifyUidPermissionChanged", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int notifyAppStatusChanged(int i) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeInt(i);
                asObject.sendRequest(NOTIFY_APP_STATUS_CHANGED, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt < 0) {
                    HiLog.error(LABEL, "DPermissionKitProxy notifyAppStatusChanged fail, replyData is %{public}d", Integer.valueOf(readInt));
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to notifyAppStatusChanged", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for notifyAppStatusChanged", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int registerUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            int i = -1;
            if (onUsingPermissionReminder == null) {
                HiLog.error(LABEL, "registerUsingPermissionReminder::fail to register, for callback is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                if (!obtain.writeRemoteObject(onUsingPermissionReminder.asObject())) {
                    HiLog.error(LABEL, "registerUsingPermissionReminder: fail to write parcel callback", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return -1;
                } else if (!asObject.sendRequest(REGISTER_ON_USING_PERMISSION_REMINDER, obtain, obtain2, messageOption)) {
                    HiLog.warn(LABEL, "registerUsingPermissionReminder send request failed", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return -1;
                } else {
                    i = obtain2.readInt();
                    if (i < 0) {
                        HiLog.error(LABEL, "registerUsingPermissionReminder failed, replyData is %{public}d", Integer.valueOf(i));
                        obtain.reclaim();
                        obtain2.reclaim();
                        return i;
                    }
                    HiLog.debug(LABEL, "registerUsingPermissionReminder success", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return i;
                }
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "Fail to registerUsingPermissionReminder, because of RemoteException", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "registerUsingPermissionReminder::fail to get remote object", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int unregisterUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            int i = -1;
            if (onUsingPermissionReminder == null) {
                HiLog.error(LABEL, "unregisterUsingPermissionReminder::fail to register, for callback is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                if (!obtain.writeRemoteObject(onUsingPermissionReminder.asObject())) {
                    HiLog.error(LABEL, "unregisterUsingPermissionReminder: fail to write parcel callback", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return -1;
                } else if (!asObject.sendRequest(UNREGISTER_ON_USING_PERMISSION_REMINDER, obtain, obtain2, messageOption)) {
                    HiLog.warn(LABEL, "unregisterUsingPermissionReminder send request failed", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return -1;
                } else {
                    i = obtain2.readInt();
                    if (i < 0) {
                        HiLog.error(LABEL, "unregisterUsingPermissionReminder failed, replyData is %{public}d", Integer.valueOf(i));
                        obtain.reclaim();
                        obtain2.reclaim();
                        return i;
                    }
                    HiLog.debug(LABEL, "unregisterUsingPermissionReminder success", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return i;
                }
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "Fail to unregisterUsingPermissionReminder, because of RemoteException", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "unregisterUsingPermissionReminder: fail to get remote object", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public void addPermissionUsedRecord(String str, String str2, int i, int i2) throws RemoteException {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            HiLog.error(LABEL, "addPermissionUsedRecord: failed, input param is null", new Object[0]);
            throw new RemoteException();
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            messageOption.setFlags(1);
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                boolean writeString = obtain.writeString(str);
                if (writeString) {
                    writeString = obtain.writeString(str2);
                }
                if (writeString) {
                    writeString = obtain.writeInt(i);
                }
                if (writeString) {
                    writeString = obtain.writeInt(i2);
                }
                if (!writeString) {
                    HiLog.error(LABEL, "addPermissionUsedRecord: failed, write data fail", new Object[0]);
                } else if (!asObject.sendRequest(ADD_PERMISSION_USED_RECORD, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL, "addPermissionUsedRecord: failed, sendRequest failed", new Object[0]);
                } else {
                    HiLog.info(LABEL, "addPermissionUsedRecord: succeed", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                obtain.reclaim();
                obtain2.reclaim();
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "addPermissionUsedRecord: got RemoteException", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "addPermissionUsedRecord: failed, remoteService is null", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int getPermissionUsedRecords(QueryPermissionUsedRequest queryPermissionUsedRequest, QueryPermissionUsedResult queryPermissionUsedResult) throws RemoteException {
        if (Objects.isNull(queryPermissionUsedRequest) || Objects.isNull(queryPermissionUsedResult)) {
            HiLog.error(LABEL, "getPermissionUsedRecords: failed, input param is null", new Object[0]);
            throw new RemoteException();
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeSequenceable(queryPermissionUsedRequest);
                HiLog.info(LABEL, "getPermissionUsedRecords: request data size: %{public}s", Integer.valueOf(obtain.getSize()));
                if (!asObject.sendRequest(GET_PERMISSION_USED_RECORDS, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL, "getPermissionUsedRecords: sendRequest failed", new Object[0]);
                    queryPermissionUsedResult.setCode(-1);
                } else if (!obtain2.readSequenceable(queryPermissionUsedResult)) {
                    HiLog.error(LABEL, "getPermissionUsedRecords: readSequenceable failed", new Object[0]);
                    queryPermissionUsedResult.setCode(-1);
                } else {
                    HiLog.info(LABEL, "getPermissionUsedRecords: response data size: %{public}s", Integer.valueOf(obtain2.getSize()));
                    obtain.reclaim();
                    obtain2.reclaim();
                    return 0;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return -1;
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "getPermissionUsedRecords: got RemoteException", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "getPermissionUsedRecords: failed, remoteService is null", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public int getPermissionUsedRecordsAsync(QueryPermissionUsedRequest queryPermissionUsedRequest, OnPermissionUsedRecord onPermissionUsedRecord) throws RemoteException {
        if (Objects.isNull(queryPermissionUsedRequest) || Objects.isNull(onPermissionUsedRecord)) {
            HiLog.error(LABEL, "getPermissionUsedRecordsAsync: failed, input param is null", new Object[0]);
            throw new RemoteException();
        }
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeSequenceable(queryPermissionUsedRequest);
                if (!obtain.writeRemoteObject(onPermissionUsedRecord)) {
                    HiLog.error(LABEL, "getPermissionUsedRecordsAsync: writeRemoteObject failed", new Object[0]);
                } else if (!asObject.sendRequest(GET_PERMISSION_USED_RECORDS_ASYNC, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL, "getPermissionUsedRecordsAsync: sendRequest failed", new Object[0]);
                } else {
                    HiLog.info(LABEL, "getPermissionUsedRecordsAsync: succeed", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return 0;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return -1;
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "getPermissionUsedRecordsAsync: got RemoteException", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "getPermissionUsedRecordsAsync: failed, remoteService is null", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public Optional<BundleLabelInfo> getBundleLabelInfo(int i) throws RemoteException {
        Optional<BundleLabelInfo> empty;
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            BundleLabelInfo bundleLabelInfo = new BundleLabelInfo();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeInt(i);
                if (!asObject.sendRequest(GET_BUNDLE_LABEL_INFO, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL, "getBundleLabelInfo: sendRequest failed: duid is %{public}d", Integer.valueOf(i));
                    empty = Optional.empty();
                } else if (!obtain2.readSequenceable(bundleLabelInfo)) {
                    HiLog.error(LABEL, "getBundleLabelInfo: readParcelable failed: duid is %{public}d", Integer.valueOf(i));
                    empty = Optional.empty();
                } else if (!bundleLabelInfo.valid()) {
                    empty = Optional.empty();
                } else {
                    HiLog.info(LABEL, "getBundleLabelInfo: success !", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return Optional.of(bundleLabelInfo);
                }
                obtain.reclaim();
                obtain2.reclaim();
                return empty;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to getBundleLabelInfo: duid is %{public}d", Integer.valueOf(i));
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        } else {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for getBundleLabelInfo", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.security.dpermissionkit.IDPermissionKit
    public void maintainCommand(String str, String str2) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject == null) {
            HiLog.error(LABEL, "DPermissionKitProxy Failed to get remote object for testCommand", new Object[0]);
            throw new RemoteException();
        } else if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            HiLog.error(LABEL, "maintainCommand: failed, input commandParam is null", new Object[0]);
            throw new RemoteException();
        } else {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                boolean writeString = obtain.writeString(str);
                if (writeString) {
                    writeString = obtain.writeString(str2);
                }
                if (!writeString) {
                    HiLog.error(LABEL, "maintainCommand: failed, write data fail", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                asObject.sendRequest(MAINTAIN_COMMAND, obtain, obtain2, messageOption);
                obtain.reclaim();
                obtain2.reclaim();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "DPermissionKitProxy Failed to maintainCommand", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }
}
