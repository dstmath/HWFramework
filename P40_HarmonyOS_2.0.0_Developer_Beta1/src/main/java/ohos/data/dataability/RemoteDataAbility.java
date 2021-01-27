package ohos.data.dataability;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.OperationExecuteException;
import ohos.data.dataability.impl.IRemoteDataAbility;
import ohos.data.dataability.impl.InterfaceVersionMisMatchException;
import ohos.data.dataability.impl.MainVersionMisMatchException;
import ohos.data.dataability.impl.NewProxyCantProcessException;
import ohos.data.dataability.impl.NewProxyMainVersionException;
import ohos.data.dataability.impl.OldProxyMainVersionException;
import ohos.data.dataability.impl.RemoteDataAbilityObserverProxy;
import ohos.data.dataability.impl.RemoteServiceObserver;
import ohos.data.dataability.impl.ResultSetRemoteTransport;
import ohos.data.dataability.impl.ResultSetRemoteTransportDescriptor;
import ohos.data.dataability.impl.SqliteExceptionUtils;
import ohos.data.rdb.ValuesBucket;
import ohos.data.rdb.impl.SharedResultSetWrapper;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class RemoteDataAbility extends RemoteObject implements IRemoteDataAbility {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "RemoteDataAbility");
    private static final long LOCK_TIMEOUT_SECOND = 10;
    private static final int OBSERVER_MAX_NUM = 10000;
    private static final int PERMISSION_DENIED = -1;
    private Ability ability;
    private Context context;
    private Map<IRemoteObject, RemoteServiceObserver> observerMap = new HashMap();
    private final ReentrantLock observerMapLock = new ReentrantLock();
    private String permission;
    private String readPermission;
    private String writePermission;
    private ohos.app.Context zcontext;

    public IRemoteObject asObject() {
        return this;
    }

    static {
        try {
            System.loadLibrary("ipc_core.z");
        } catch (NullPointerException | UnsatisfiedLinkError e) {
            HiLog.info(LABEL, "loadLibrary ipc_core.z error,eMsg:%{public}s", new Object[]{e.getMessage()});
        }
    }

    public RemoteDataAbility(Builder builder) {
        super("ohos.data.dataability.RemoteDataAbility");
        this.ability = builder.ability;
        this.context = builder.context;
        this.zcontext = builder.abilityContext;
        this.permission = builder.permission;
        this.readPermission = builder.readPermission;
        this.writePermission = builder.writePermission;
    }

    public static class Builder {
        private Ability ability;
        private ohos.app.Context abilityContext;
        private Context context;
        private String permission;
        private String readPermission;
        private String writePermission;

        public Builder(Ability ability2) {
            this.ability = ability2;
        }

        public Builder context(Context context2) {
            this.context = context2;
            return this;
        }

        public Builder permission(ohos.app.Context context2, String str, String str2, String str3) {
            this.abilityContext = context2;
            this.permission = str;
            this.readPermission = str2;
            this.writePermission = str3;
            return this;
        }

        public RemoteDataAbility build() {
            return new RemoteDataAbility(this);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        Exception e;
        int i2 = -1;
        try {
            checkVisible();
            int readInt = messageParcel.readInt();
            int readInt2 = messageParcel.readInt();
            switch (i) {
                case 2:
                    checkInterfaceVersion(readInt2, 1);
                    checkWritePermission();
                    batchInsert(messageParcel, messageParcel2);
                    return true;
                case 3:
                    checkInterfaceVersion(readInt2, 1);
                    call(messageParcel, messageParcel2);
                    return true;
                case 4:
                    checkInterfaceVersion(readInt2, 1);
                    checkWritePermission();
                    delete(messageParcel, messageParcel2);
                    return true;
                case 5:
                    checkInterfaceVersion(readInt2, 1);
                    executeBatch(messageParcel, messageParcel2);
                    return true;
                case 6:
                    checkInterfaceVersion(readInt2, 1);
                    getFileTypes(messageParcel, messageParcel2);
                    return true;
                case 7:
                    checkInterfaceVersion(readInt2, 1);
                    getType(messageParcel, messageParcel2);
                    return true;
                case 8:
                    checkInterfaceVersion(readInt2, 1);
                    checkWritePermission();
                    insert(messageParcel, messageParcel2);
                    return true;
                case 9:
                    checkInterfaceVersion(readInt2, 1);
                    checkReadPermission();
                    query(messageParcel, messageParcel2);
                    return true;
                case 10:
                    checkInterfaceVersion(readInt2, 1);
                    checkReadPermission();
                    registerObserver(messageParcel, messageParcel2);
                    return true;
                case 11:
                    checkInterfaceVersion(readInt2, 1);
                    checkReadPermission();
                    unregisterObserver(messageParcel, messageParcel2);
                    return true;
                case 12:
                    checkInterfaceVersion(readInt2, 1);
                    checkWritePermission();
                    update(messageParcel, messageParcel2);
                    return true;
                case 13:
                    checkInterfaceVersion(readInt2, 1);
                    checkReadPermission();
                    normalizeUri(messageParcel, messageParcel2);
                    return true;
                case 14:
                    checkInterfaceVersion(readInt2, 1);
                    checkReadPermission();
                    denormalizeUri(messageParcel, messageParcel2);
                    return true;
                case 15:
                    try {
                        checkInterfaceVersion(readInt2, 1);
                        checkReadPermission();
                        reload(messageParcel, messageParcel2);
                        return true;
                    } catch (Exception e2) {
                        e = e2;
                        i2 = 1;
                        break;
                    }
                default:
                    if (i > 1) {
                        checkMainVersion(readInt);
                    }
                    return RemoteDataAbility.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
        } catch (Exception e3) {
            e = e3;
        }
        SqliteExceptionUtils.writeExceptionToParcel(messageParcel2, e);
        if (e instanceof MainVersionMisMatchException) {
            messageParcel2.writeInt(1);
        }
        if (e instanceof InterfaceVersionMisMatchException) {
            messageParcel2.writeInt(i2);
        }
        return true;
    }

    private void update(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "update start", new Object[0]);
        Uri readFromParcel = Uri.readFromParcel(messageParcel);
        DataAbilityPredicates dataAbilityPredicates = null;
        ValuesBucket valuesBucket = messageParcel.readInt() != 0 ? new ValuesBucket((Parcel) messageParcel) : null;
        if (messageParcel.readInt() != 0) {
            dataAbilityPredicates = new DataAbilityPredicates((Parcel) messageParcel);
        }
        int update = this.ability.update(readFromParcel, valuesBucket, dataAbilityPredicates);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeInt(update);
    }

    private void insert(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "insert start", new Object[0]);
        int insert = this.ability.insert(Uri.readFromParcel(messageParcel), messageParcel.readInt() != 0 ? new ValuesBucket((Parcel) messageParcel) : null);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeInt(insert);
    }

    private void getType(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "getType start", new Object[0]);
        String type = this.ability.getType(Uri.readFromParcel(messageParcel));
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeString(type);
    }

    private void getFileTypes(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "getFileTypes start", new Object[0]);
        String[] fileTypes = this.ability.getFileTypes(Uri.readFromParcel(messageParcel), messageParcel.readString());
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeStringArray(fileTypes);
    }

    private void executeBatch(MessageParcel messageParcel, MessageParcel messageParcel2) throws OperationExecuteException {
        ArrayList<DataAbilityOperation> arrayList;
        HiLog.info(LABEL, "executeBatch start", new Object[0]);
        int readInt = messageParcel.readInt();
        if (readInt > 0) {
            arrayList = new ArrayList<>();
            for (int i = 0; i < readInt && messageParcel.getReadableBytes() > 0; i++) {
                if (messageParcel.readInt() != 0) {
                    DataAbilityOperation createFromParcel = DataAbilityOperation.createFromParcel(messageParcel);
                    int type = createFromParcel.getType();
                    if (type == 1 || type == 2 || type == 3) {
                        checkWritePermission();
                        arrayList.add(createFromParcel);
                    } else {
                        throw new IllegalStateException("unknow DataAbilityOperation type.");
                    }
                }
            }
        } else {
            arrayList = null;
        }
        Sequenceable[] executeBatch = this.ability.executeBatch(arrayList);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        int length = executeBatch != null ? executeBatch.length : 0;
        messageParcel2.writeInt(length);
        for (int i2 = 0; i2 < length; i2++) {
            messageParcel2.writeSequenceable(executeBatch[i2]);
        }
    }

    private void delete(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "delete start", new Object[0]);
        int delete = this.ability.delete(Uri.readFromParcel(messageParcel), messageParcel.readInt() != 0 ? new DataAbilityPredicates((Parcel) messageParcel) : null);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeInt(delete);
    }

    private void call(MessageParcel messageParcel, MessageParcel messageParcel2) {
        PacMap pacMap;
        HiLog.info(LABEL, "call start", new Object[0]);
        String readString = messageParcel.readString();
        String readString2 = messageParcel.readString();
        if (messageParcel.readInt() == 1) {
            pacMap = new PacMap();
            pacMap.unmarshalling(messageParcel);
        } else {
            pacMap = null;
        }
        PacMap call = this.ability.call(readString, readString2, pacMap);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeSequenceable(call);
    }

    private void batchInsert(MessageParcel messageParcel, MessageParcel messageParcel2) {
        ArrayList arrayList;
        HiLog.info(LABEL, "batchInsert start", new Object[0]);
        Uri readFromParcel = Uri.readFromParcel(messageParcel);
        int readInt = messageParcel.readInt();
        ValuesBucket[] valuesBucketArr = null;
        if (readInt > 0) {
            arrayList = new ArrayList();
            for (int i = 0; i < readInt && messageParcel.getReadableBytes() > 0; i++) {
                arrayList.add(messageParcel.readInt() != 0 ? new ValuesBucket((Parcel) messageParcel) : null);
            }
        } else {
            arrayList = null;
        }
        if (arrayList != null) {
            valuesBucketArr = (ValuesBucket[]) arrayList.toArray(new ValuesBucket[arrayList.size()]);
        }
        int batchInsert = this.ability.batchInsert(readFromParcel, valuesBucketArr);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeInt(batchInsert);
    }

    private void query(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "query start ", new Object[0]);
        ResultSetRemoteTransportDescriptor resultSetRemoteTransportDescriptor = null;
        ResultSet query = this.ability.query(Uri.readFromParcel(messageParcel), messageParcel.readInt() != 0 ? messageParcel.readStringArray() : null, messageParcel.readInt() != 0 ? new DataAbilityPredicates((Parcel) messageParcel) : null);
        if (query != null) {
            resultSetRemoteTransportDescriptor = new ResultSetRemoteTransport(query instanceof SharedResultSet ? (SharedResultSet) query : new SharedResultSetWrapper(query)).getResultSetRemoteTransportDescriptor();
        }
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeSequenceable(resultSetRemoteTransportDescriptor);
    }

    private void registerObserver(MessageParcel messageParcel, MessageParcel messageParcel2) {
        RemoteServiceObserver remoteServiceObserver;
        HiLog.info(LABEL, "registerObserver start", new Object[0]);
        android.net.Uri convertToAndroidContentUri = UriConverter.convertToAndroidContentUri(Uri.readFromParcel(messageParcel));
        IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
        try {
            if (!this.observerMapLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
                SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
                this.observerMapLock.unlock();
                return;
            }
            if (this.observerMap.containsKey(readRemoteObject)) {
                remoteServiceObserver = this.observerMap.get(readRemoteObject);
            } else if (this.observerMap.size() > 10000) {
                HiLog.error(LABEL, "number of observers out of limit on the server side,failed to register observer.", new Object[0]);
                SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
                this.observerMapLock.unlock();
                return;
            } else {
                remoteServiceObserver = new RemoteServiceObserver(new Handler(this.context.getMainLooper()), new RemoteDataAbilityObserverProxy(readRemoteObject));
            }
            this.context.getContentResolver().registerContentObserver(convertToAndroidContentUri, true, remoteServiceObserver);
            if (!this.observerMap.containsKey(readRemoteObject)) {
                this.observerMap.put(readRemoteObject, remoteServiceObserver);
            }
            HiLog.debug(LABEL, "put stub:%{private}s, serviceObserver:%{private}s", new Object[]{readRemoteObject, remoteServiceObserver});
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            this.observerMapLock.unlock();
            HiLog.info(LABEL, "registerObserver finish", new Object[0]);
        } catch (InterruptedException e) {
            SqliteExceptionUtils.writeExceptionToParcel(messageParcel2, e);
        } catch (Throwable th) {
            this.observerMapLock.unlock();
            throw th;
        }
    }

    private void unregisterObserver(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(LABEL, "unregisterObserver start", new Object[0]);
        IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
        try {
            if (!this.observerMapLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
                SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
                this.observerMapLock.unlock();
                return;
            }
            if (this.observerMap.containsKey(readRemoteObject)) {
                this.context.getContentResolver().unregisterContentObserver(this.observerMap.get(readRemoteObject));
                this.observerMap.remove(readRemoteObject);
                HiLog.debug(LABEL, "remove stub:%{private}s ", new Object[]{readRemoteObject});
            } else {
                HiLog.error(LABEL, "Observer was not registered.", new Object[0]);
            }
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            this.observerMapLock.unlock();
            HiLog.info(LABEL, "unregisterObserver finish", new Object[0]);
        } catch (InterruptedException e) {
            SqliteExceptionUtils.writeExceptionToParcel(messageParcel2, e);
        } catch (Throwable th) {
            this.observerMapLock.unlock();
            throw th;
        }
    }

    public void normalizeUri(MessageParcel messageParcel, MessageParcel messageParcel2) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "normalizeUri start", new Object[0]);
        Uri normalizeUri = this.ability.normalizeUri(Uri.readFromParcel(messageParcel));
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeSequenceable(normalizeUri);
    }

    public void denormalizeUri(MessageParcel messageParcel, MessageParcel messageParcel2) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "denormalizeUri start", new Object[0]);
        Uri denormalizeUri = this.ability.denormalizeUri(Uri.readFromParcel(messageParcel));
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeSequenceable(denormalizeUri);
    }

    private void reload(MessageParcel messageParcel, MessageParcel messageParcel2) throws DataAbilityRemoteException {
        PacMap pacMap;
        HiLog.info(LABEL, "reload start", new Object[0]);
        Uri readFromParcel = Uri.readFromParcel(messageParcel);
        if (messageParcel.readInt() == 1) {
            pacMap = new PacMap();
            pacMap.unmarshalling(messageParcel);
        } else {
            pacMap = null;
        }
        boolean reload = this.ability.reload(readFromParcel, pacMap);
        SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
        messageParcel2.writeBoolean(reload);
    }

    private void checkWritePermission() {
        if (!checkPermission(this.writePermission)) {
            checkPermission(this.permission);
        }
    }

    private void checkReadPermission() {
        if (!checkPermission(this.readPermission)) {
            checkPermission(this.permission);
        }
    }

    private boolean checkPermission(String str) {
        if (TextUtils.isEmpty(str)) {
            HiLog.info(LABEL, "permission name is empty", new Object[0]);
            return false;
        }
        ohos.app.Context context2 = this.zcontext;
        if (context2 == null) {
            HiLog.error(LABEL, "abilityContext is null.", new Object[0]);
            throw new IllegalArgumentException("abilityContext is null.");
        } else if (context2.verifyCallingPermission(str) != -1) {
            return true;
        } else {
            HiLog.error(LABEL, "Permission Denial: accessing ability requires %{public}s", new Object[]{str});
            throw new SecurityException("Permission Denial: accessing ability requires " + str);
        }
    }

    private void checkVisible() {
        Ability ability2 = this.ability;
        if (ability2 == null || ability2.getAbilityInfo() == null) {
            throw new IllegalArgumentException("ability or ability info is null.");
        } else if (!this.ability.getAbilityInfo().isVisible()) {
            throw new SecurityException("Permission Denial: ability is not visible!");
        }
    }

    private void checkMainVersion(int i) {
        if (i > 1) {
            throw new NewProxyMainVersionException("The proxy has a new main version with added interface.");
        } else if (i < 1) {
            throw new OldProxyMainVersionException("The server has a new main version with deleted interface.");
        }
    }

    private void checkInterfaceVersion(int i, int i2) {
        if (i > i2) {
            throw new NewProxyCantProcessException("The proxy has a new interface version with params changed while server can't process.");
        }
    }
}
