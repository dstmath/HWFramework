package ohos.data.distributed.user;

import java.util.List;
import java.util.Objects;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreErrorCode;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreObserver;
import ohos.data.distributed.common.Options;
import ohos.data.distributed.common.SubscribeType;
import ohos.data.distributed.common.TextUtils;
import ohos.data.distributed.common.Value;

public class MultiKvStoreImpl implements MultiKvStore {
    private KvManagerConfig config;
    private long nativeKvStore;
    private Options options;
    private String storeId;

    private native void nativeClear(long j);

    private native void nativeCommit(long j);

    private native boolean nativeDelete(byte[] bArr, long j);

    private native void nativeDeleteBatch(String[] strArr, long j);

    private native long nativeGetKvStoreSnapshot(KvStoreObserver kvStoreObserver, long j);

    private native boolean nativePut(byte[] bArr, Value value, long j);

    private native void nativePutBatch(Entry[] entryArr, long j);

    private native void nativeReleaseKvStoreSnapshot(long j, KvStoreSnapshot kvStoreSnapshot);

    private native void nativeRollback(long j);

    private native void nativeStartTransaction(long j);

    private native boolean nativeSubscribe(SubscribeType subscribeType, KvStoreObserver kvStoreObserver, KvManagerConfig kvManagerConfig, long j);

    private native boolean nativeUnSubscribe(SubscribeType subscribeType, KvStoreObserver kvStoreObserver, KvManagerConfig kvManagerConfig, long j);

    @Override // ohos.data.distributed.common.KvStore
    public void enableSync(boolean z) throws KvStoreException {
    }

    @Override // ohos.data.distributed.common.KvStore
    public void setSyncRange(List<String> list, List<String> list2) throws KvStoreException {
    }

    public MultiKvStoreImpl(KvManagerConfig kvManagerConfig, String str, long j, Options options2) {
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
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "value over maximum size.");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void putByteArray(String str, byte[] bArr) throws KvStoreException {
        TextUtils.assertKey(str);
        if (Objects.isNull(bArr) || bArr.length > 4194303) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "over maximum value length.");
        }
        nativePut(TextUtils.getKeyBytes(str), Value.get(bArr), this.nativeKvStore);
    }

    @Override // ohos.data.distributed.common.KvStore
    public void delete(String str) throws KvStoreException {
        TextUtils.assertKey(str);
        nativeDelete(TextUtils.getKeyBytes(str), this.nativeKvStore);
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010  */
    @Override // ohos.data.distributed.common.KvStore
    public void putBatch(List<Entry> list) throws KvStoreException {
        if (!TextUtils.isListEmpty(list)) {
            for (Entry entry : list) {
                if (Objects.isNull(entry) || TextUtils.isEmpty(entry.getKey()) || Objects.isNull(entry.getValue())) {
                    throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
                }
                while (r0.hasNext()) {
                }
            }
            nativePutBatch((Entry[]) list.toArray(new Entry[list.size()]), this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "entries is empty.");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void deleteBatch(List<String> list) throws KvStoreException {
        if (!TextUtils.isListEmpty(list)) {
            for (String str : list) {
                if (!TextUtils.lenLessEqualThan(str, 1024)) {
                    KvStoreErrorCode kvStoreErrorCode = KvStoreErrorCode.INVALID_ARGUMENT;
                    throw new KvStoreException(kvStoreErrorCode, "key:" + str + ", over maximum length.");
                }
            }
            nativeDeleteBatch((String[]) list.toArray(new String[list.size()]), this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "keys is empty.");
    }

    @Override // ohos.data.distributed.user.MultiKvStore
    public void clear() throws KvStoreException {
        nativeClear(this.nativeKvStore);
    }

    @Override // ohos.data.distributed.user.MultiKvStore
    public KvStoreSnapshot getKvStoreSnapshot(KvStoreObserver kvStoreObserver) throws KvStoreException {
        return new KvStoreSnapshotImpl(nativeGetKvStoreSnapshot(kvStoreObserver, this.nativeKvStore));
    }

    @Override // ohos.data.distributed.user.MultiKvStore
    public void releaseKvStoreSnapshot(KvStoreSnapshot kvStoreSnapshot) throws KvStoreException {
        if (Objects.isNull(kvStoreSnapshot)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "input param is null.");
        } else if (!kvStoreSnapshot.getClass().isAssignableFrom(KvStoreSnapshotImpl.class)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "invalid instance.");
        } else if (((KvStoreSnapshotImpl) KvStoreSnapshotImpl.class.cast(kvStoreSnapshot)).getSnapshot() > 0) {
            nativeReleaseKvStoreSnapshot(this.nativeKvStore, kvStoreSnapshot);
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "invalid instance, snapshot closed.");
        }
    }

    @Override // ohos.data.distributed.common.KvStore
    public void subscribe(SubscribeType subscribeType, KvStoreObserver kvStoreObserver) throws KvStoreException {
        if (!Objects.isNull(kvStoreObserver)) {
            nativeSubscribe(subscribeType, kvStoreObserver, this.config, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "observer is null.");
    }

    @Override // ohos.data.distributed.common.KvStore
    public void unSubscribe(KvStoreObserver kvStoreObserver) throws KvStoreException {
        if (!Objects.isNull(kvStoreObserver)) {
            nativeUnSubscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserver, this.config, this.nativeKvStore);
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "observer is null.");
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
}
