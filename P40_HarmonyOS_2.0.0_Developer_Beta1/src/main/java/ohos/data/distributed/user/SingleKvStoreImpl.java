package ohos.data.distributed.user;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreErrorCode;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreObserver;
import ohos.data.distributed.common.KvStoreResultSet;
import ohos.data.distributed.common.KvStoreResultSetImpl;
import ohos.data.distributed.common.Options;
import ohos.data.distributed.common.Query;
import ohos.data.distributed.common.SubscribeType;
import ohos.data.distributed.common.SyncCallback;
import ohos.data.distributed.common.SyncMode;
import ohos.data.distributed.common.TextUtils;
import ohos.data.distributed.common.Value;

public class SingleKvStoreImpl implements SingleKvStore {
    private KvManagerConfig config;
    private long nativeKvStore;
    private Options options;
    private String storeId;

    private native void nativeCloseResultSet(KvStoreResultSet kvStoreResultSet, long j);

    private native void nativeCommit(long j);

    private native void nativeDelete(byte[] bArr, long j);

    private native void nativeDeleteBatch(String[] strArr, long j);

    private native Value nativeGet(byte[] bArr, long j);

    private native int nativeGetCountWithQuery(String str, long j);

    private native List<Entry> nativeGetEntries(byte[] bArr, long j);

    private native List<Entry> nativeGetEntriesWithQuery(String str, long j);

    private native long nativeGetResultSet(byte[] bArr, long j);

    private native long nativeGetResultSetWithQuery(String str, long j);

    private native void nativePut(byte[] bArr, Value value, long j);

    private native void nativePutBatch(Entry[] entryArr, long j);

    private native void nativeRegisterObserver(SubscribeType subscribeType, KvStoreObserver kvStoreObserver, KvManagerConfig kvManagerConfig, long j);

    private native void nativeRegisterSyncCallback(SyncCallback syncCallback, long j);

    private native void nativeRemoveDeviceData(String str, long j);

    private native void nativeRollback(long j);

    private static native void nativeSetCapabilityEnabled(boolean z, long j);

    private static native void nativeSetCapabilityRange(String[] strArr, String[] strArr2, long j);

    private native void nativeSetSyncParam(int i, long j);

    private native void nativeStartTransaction(long j);

    private native void nativeSync(String[] strArr, int i, int i2, long j);

    private native void nativeUnRegisterSyncCallback(long j);

    private native void nativeUnregisterObserver(SubscribeType subscribeType, KvStoreObserver kvStoreObserver, KvManagerConfig kvManagerConfig, long j);

    public SingleKvStoreImpl(KvManagerConfig kvManagerConfig, String str, long j, Options options2) {
        this.config = kvManagerConfig;
        this.storeId = str;
        this.nativeKvStore = j;
        this.options = options2;
    }

    public Options getOptions() {
        return this.options;
    }

    @Override // ohos.data.distributed.common.KvStore
    public String getStoreId() throws KvStoreException {
        return this.storeId;
    }

    public long getNativeKvStore() {
        return this.nativeKvStore;
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putBoolean(String str, boolean z) throws KvStoreException {
        TextUtils.assertKey(str);
        putInt(str, z ? 1 : 0);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putInt(String str, int i) throws KvStoreException {
        TextUtils.assertKey(str);
        nativePut(TextUtils.getKeyBytes(str), Value.get(i), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putFloat(String str, float f) throws KvStoreException {
        TextUtils.assertKey(str);
        nativePut(TextUtils.getKeyBytes(str), Value.get(f), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putDouble(String str, double d) throws KvStoreException {
        TextUtils.assertKey(str);
        nativePut(TextUtils.getKeyBytes(str), Value.get(d), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putString(String str, String str2) throws KvStoreException {
        TextUtils.assertKey(str);
        if (TextUtils.lenLessEqualThan(str2, KvStore.MAX_VALUE_LENGTH)) {
            nativePut(TextUtils.getKeyBytes(str), Value.get(str2), this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "value null or too large");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putByteArray(String str, byte[] bArr) throws KvStoreException {
        TextUtils.assertKey(str);
        if (Objects.isNull(bArr) || bArr.length > 4194303) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "value null or too large");
        }
        nativePut(TextUtils.getKeyBytes(str), Value.get(bArr), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void delete(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        nativeDelete(TextUtils.getKeyBytes(str), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public boolean getBoolean(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return getInt(str) == 1;
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public int getInt(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return innerGetValue(str).getInt();
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public float getFloat(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return innerGetValue(str).getFloat();
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public double getDouble(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return innerGetValue(str).getDouble();
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public String getString(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return innerGetValue(str).getString();
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public byte[] getByteArray(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        return innerGetValue(str).getByteArray();
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public List<Entry> getEntries(String str) throws KvStoreException {
        List<Entry> nativeGetEntries = nativeGetEntries(TextUtils.getKeyBytesMayEmpty(str), this.nativeKvStore);
        return nativeGetEntries == null ? Collections.emptyList() : nativeGetEntries;
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public List<Entry> getEntries(Query query) throws KvStoreException {
        if (!Objects.isNull(query)) {
            List<Entry> nativeGetEntriesWithQuery = nativeGetEntriesWithQuery(query.getSqlLike(), this.nativeKvStore);
            return nativeGetEntriesWithQuery == null ? Collections.emptyList() : nativeGetEntriesWithQuery;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public KvStoreResultSet getResultSet(String str) throws KvStoreException {
        return new KvStoreResultSetImpl(nativeGetResultSet(TextUtils.getKeyBytesMayEmpty(str), this.nativeKvStore));
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public KvStoreResultSet getResultSet(Query query) throws KvStoreException {
        if (!Objects.isNull(query)) {
            return new KvStoreResultSetImpl(nativeGetResultSetWithQuery(query.getSqlLike(), this.nativeKvStore));
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void closeResultSet(KvStoreResultSet kvStoreResultSet) throws KvStoreException {
        if (!Objects.isNull(kvStoreResultSet)) {
            nativeCloseResultSet(kvStoreResultSet, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "resultSet is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public int getResultSize(Query query) throws KvStoreException {
        if (!Objects.isNull(query)) {
            return nativeGetCountWithQuery(query.getSqlLike(), this.nativeKvStore);
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "query is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void sync(List<String> list, SyncMode syncMode) throws KvStoreException {
        sync(list, syncMode, 0);
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void sync(List<String> list, SyncMode syncMode, int i) throws KvStoreException {
        if (TextUtils.isListEmpty(list) || syncMode == null) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "deviceIds is empty or mode is null.");
        }
        nativeSync((String[]) list.toArray(new String[list.size()]), syncMode.getValue(), i, this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void subscribe(SubscribeType subscribeType, KvStoreObserver kvStoreObserver) throws KvStoreException {
        if (Objects.isNull(subscribeType)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "subscribeType is null.");
        } else if (!Objects.isNull(kvStoreObserver)) {
            nativeRegisterObserver(subscribeType, kvStoreObserver, this.config, this.nativeKvStore);
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "observer is null.");
        }
    }

    @Override // ohos.data.distributed.common.KvStore
    public void unSubscribe(KvStoreObserver kvStoreObserver) throws KvStoreException {
        if (!Objects.isNull(kvStoreObserver)) {
            nativeUnregisterObserver(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserver, this.config, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "observer is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void registerSyncCallback(SyncCallback syncCallback) throws KvStoreException {
        if (!Objects.isNull(syncCallback)) {
            nativeRegisterSyncCallback(syncCallback, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "syncCallback is null.");
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void unRegisterSyncCallback() throws KvStoreException {
        nativeUnRegisterSyncCallback(this.nativeKvStore);
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void removeDeviceData(String str) throws KvStoreException {
        if (!Objects.isNull(str)) {
            nativeRemoveDeviceData(str, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "deviceId is null.");
    }

    private Value innerGetValue(String str) throws KvStoreException {
        Value nativeGet = nativeGet(TextUtils.getKeyBytes(str), this.nativeKvStore);
        if (!Objects.isNull(nativeGet)) {
            return nativeGet;
        }
        KvStoreErrorCode kvStoreErrorCode = KvStoreErrorCode.KEY_NOT_FOUND;
        throw new KvStoreException(kvStoreErrorCode, "value not exist by key=" + str);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putBatch(List<Entry> list) throws KvStoreException {
        if (TextUtils.isListEmpty(list)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "entries is empty.");
        } else if (list.size() <= 128) {
            for (Entry entry : list) {
                if (Objects.isNull(entry) || Objects.isNull(entry.getValue())) {
                    throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
                }
                TextUtils.assertKey(entry.getKey());
            }
            nativePutBatch((Entry[]) list.toArray(new Entry[list.size()]), this.nativeKvStore);
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "entries is larger than 128");
        }
    }

    @Override // ohos.data.distributed.common.KvStore
    public void deleteBatch(List<String> list) throws KvStoreException {
        if (TextUtils.isListEmpty(list)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "keys is empty.");
        } else if (list.size() <= 128) {
            for (String str : list) {
                TextUtils.assertKey(str);
            }
            nativeDeleteBatch((String[]) list.toArray(new String[list.size()]), this.nativeKvStore);
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "entries is larger than 128");
        }
    }

    @Override // ohos.data.distributed.common.KvStore
    public void startTransaction() throws KvStoreException {
        nativeStartTransaction(this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void commit() throws KvStoreException {
        nativeCommit(this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void rollback() throws KvStoreException {
        nativeRollback(this.nativeKvStore);
    }

    @Override // ohos.data.distributed.user.SingleKvStore
    public void setSyncParam(int i) throws KvStoreException {
        nativeSetSyncParam(i, this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void enableSync(boolean z) throws KvStoreException {
        nativeSetCapabilityEnabled(z, this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void setSyncRange(List<String> list, List<String> list2) throws KvStoreException {
        if (TextUtils.isListEmpty(list) || TextUtils.isListEmpty(list2)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "local and remote labels should not null.");
        }
        nativeSetCapabilityRange((String[]) list.toArray(new String[list.size()]), (String[]) list2.toArray(new String[list2.size()]), this.nativeKvStore);
    }
}
