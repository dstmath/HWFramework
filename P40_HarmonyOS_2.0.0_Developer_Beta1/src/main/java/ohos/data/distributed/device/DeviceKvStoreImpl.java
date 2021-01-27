package ohos.data.distributed.device;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStoreErrorCode;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreObserver;
import ohos.data.distributed.common.KvStoreResultSet;
import ohos.data.distributed.common.Options;
import ohos.data.distributed.common.Query;
import ohos.data.distributed.common.SubscribeType;
import ohos.data.distributed.common.SyncCallback;
import ohos.data.distributed.common.SyncMode;
import ohos.data.distributed.common.TextUtils;
import ohos.data.distributed.user.SingleKvStoreImpl;

public class DeviceKvStoreImpl implements DeviceKvStore {
    private static final String FMT = "%s%s%s";
    private static final Object LOCK = new Object();
    private Map<KvStoreObserver, KvStoreObserver> observers = new ConcurrentHashMap();
    private SingleKvStoreImpl singleKvStoreImpl;

    public DeviceKvStoreImpl(SingleKvStoreImpl singleKvStoreImpl2) {
        this.singleKvStoreImpl = singleKvStoreImpl2;
    }

    public Options getOptions() {
        return this.singleKvStoreImpl.getOptions();
    }

    @Override // ohos.data.distributed.common.KvStore
    public String getStoreId() throws KvStoreException {
        return this.singleKvStoreImpl.getStoreId();
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putBoolean(String str, boolean z) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putInt(str, z ? 1 : 0);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putInt(String str, int i) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putInt(str, i);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putFloat(String str, float f) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putFloat(str, f);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putDouble(String str, double d) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putDouble(str, d);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putString(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putString(str, str2);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putByteArray(String str, byte[] bArr) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.putByteArray(str, bArr);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void delete(String str) throws KvStoreException {
        TextUtils.assertDeviceKey(str);
        this.singleKvStoreImpl.delete(str);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public boolean getBoolean(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getBoolean(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public int getInt(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getInt(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public float getFloat(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getFloat(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public double getDouble(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getDouble(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public String getString(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getString(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public byte[] getByteArray(String str, String str2) throws KvStoreException {
        TextUtils.assertDeviceKey(str2);
        return this.singleKvStoreImpl.getByteArray(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public List<Entry> getEntries(String str, String str2) throws KvStoreException {
        return this.singleKvStoreImpl.getEntries(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public List<Entry> getEntries(Query query) throws KvStoreException {
        return getEntries("", query);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public List<Entry> getEntries(String str, Query query) throws KvStoreException {
        if (query != null) {
            return this.singleKvStoreImpl.getEntries(query.deviceId(str));
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null");
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public KvStoreResultSet getResultSet(String str, String str2) throws KvStoreException {
        return this.singleKvStoreImpl.getResultSet(getDeviceKey(str, str2));
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public KvStoreResultSet getResultSet(Query query) throws KvStoreException {
        return getResultSet("", query);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public KvStoreResultSet getResultSet(String str, Query query) throws KvStoreException {
        if (query != null) {
            return this.singleKvStoreImpl.getResultSet(query.deviceId(str));
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null");
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public void closeResultSet(KvStoreResultSet kvStoreResultSet) throws KvStoreException {
        this.singleKvStoreImpl.closeResultSet(kvStoreResultSet);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public int getResultSize(Query query) throws KvStoreException {
        return getResultSize("", query);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public int getResultSize(String str, Query query) throws KvStoreException {
        if (query != null) {
            return this.singleKvStoreImpl.getResultSize(query.deviceId(str));
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null");
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public void sync(List<String> list, SyncMode syncMode) throws KvStoreException {
        this.singleKvStoreImpl.sync(list, syncMode);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void subscribe(SubscribeType subscribeType, KvStoreObserver kvStoreObserver) throws KvStoreException {
        synchronized (LOCK) {
            if (Objects.isNull(kvStoreObserver) || Objects.isNull(subscribeType)) {
                throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
            } else if (!this.observers.containsKey(kvStoreObserver)) {
                this.singleKvStoreImpl.subscribe(subscribeType, kvStoreObserver);
                this.observers.put(kvStoreObserver, kvStoreObserver);
            } else {
                throw new KvStoreException(KvStoreErrorCode.STORE_ALREADY_SUBSCRIBE, "already registered");
            }
        }
    }

    @Override // ohos.data.distributed.common.KvStore
    public void unSubscribe(KvStoreObserver kvStoreObserver) throws KvStoreException {
        synchronized (LOCK) {
            if (Objects.isNull(kvStoreObserver)) {
                throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
            } else if (this.observers.containsKey(kvStoreObserver)) {
                this.singleKvStoreImpl.unSubscribe(kvStoreObserver);
                this.observers.remove(kvStoreObserver);
            } else {
                throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "not registered");
            }
        }
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public void removeDeviceData(String str) throws KvStoreException {
        if (!Objects.isNull(str)) {
            this.singleKvStoreImpl.removeDeviceData(str);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "deviceId is null.");
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public void registerSyncCallback(SyncCallback syncCallback) throws KvStoreException {
        this.singleKvStoreImpl.registerSyncCallback(syncCallback);
    }

    @Override // ohos.data.distributed.device.DeviceKvStore
    public void unRegisterSyncCallback() throws KvStoreException {
        this.singleKvStoreImpl.unRegisterSyncCallback();
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putBatch(List<Entry> list) throws KvStoreException {
        if (!TextUtils.isListEmpty(list)) {
            this.singleKvStoreImpl.putBatch(list);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "entries is empty.");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void deleteBatch(List<String> list) throws KvStoreException {
        if (!TextUtils.isListEmpty(list)) {
            this.singleKvStoreImpl.deleteBatch(list);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "keys is empty.");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void startTransaction() throws KvStoreException {
        this.singleKvStoreImpl.startTransaction();
    }

    @Override // ohos.data.distributed.common.KvStore
    public void commit() throws KvStoreException {
        this.singleKvStoreImpl.commit();
    }

    @Override // ohos.data.distributed.common.KvStore
    public void rollback() throws KvStoreException {
        this.singleKvStoreImpl.rollback();
    }

    @Override // ohos.data.distributed.common.KvStore
    public void enableSync(boolean z) throws KvStoreException {
        this.singleKvStoreImpl.enableSync(z);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void setSyncRange(List<String> list, List<String> list2) throws KvStoreException {
        if (TextUtils.isListEmpty(list) || TextUtils.isListEmpty(list2)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "local and remote labels should not null.");
        }
        this.singleKvStoreImpl.setSyncRange(list, list2);
    }

    public SingleKvStoreImpl getSingleKvStore() {
        return this.singleKvStoreImpl;
    }

    private String getDeviceKey(String str, String str2) {
        if (TextUtils.isEmpty(str) || Objects.isNull(str2)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "input param is invalid.");
        }
        return String.format(Locale.ENGLISH, FMT, String.format(Locale.ENGLISH, "%4d", Integer.valueOf(str.length())).replace(" ", "0"), str, str2);
    }
}
