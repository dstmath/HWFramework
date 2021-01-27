package ohos.data.distributed.user;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreErrorCode;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.TextUtils;
import ohos.data.distributed.common.Value;

public class KvStoreSnapshotImpl implements KvStoreSnapshot {
    private long nativeSnapshot;

    private native Value nativeGet(byte[] bArr, long j);

    private native List<Entry> nativeGetEntries(byte[] bArr, long j);

    private native List<String> nativeGetKeys(byte[] bArr, long j);

    public KvStoreSnapshotImpl(long j) {
        this.nativeSnapshot = j;
    }

    public long getSnapshot() {
        return this.nativeSnapshot;
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public boolean getBoolean(String str) throws KvStoreException {
        return getInt(str) == 1;
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public int getInt(String str) throws KvStoreException {
        return innerGetValue(str).getInt();
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public float getFloat(String str) throws KvStoreException {
        return innerGetValue(str).getFloat();
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public double getDouble(String str) throws KvStoreException {
        return innerGetValue(str).getDouble();
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public String getString(String str) throws KvStoreException {
        return innerGetValue(str).getString();
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public byte[] getByteArray(String str) throws KvStoreException {
        return innerGetValue(str).getByteArray();
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public List<Entry> getEntries(String str) throws KvStoreException {
        List<Entry> nativeGetEntries = nativeGetEntries(TextUtils.getKeyBytesMayEmpty(str), this.nativeSnapshot);
        return nativeGetEntries == null ? Collections.emptyList() : nativeGetEntries;
    }

    @Override // ohos.data.distributed.user.KvStoreSnapshot
    public List<String> getKeys(String str) throws KvStoreException {
        List<String> nativeGetKeys = nativeGetKeys(TextUtils.getKeyBytesMayEmpty(str), this.nativeSnapshot);
        return Objects.isNull(nativeGetKeys) ? Collections.emptyList() : nativeGetKeys;
    }

    private void assertKey(String str) {
        assertKey(str, "key is empty or over maximum size.");
    }

    private void assertKey(String str, String str2) {
        if (TextUtils.isEmpty(str) || !TextUtils.lenLessEqualThan(str, 1024)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, str2);
        }
    }

    private void assertValue(String str) {
        if (!TextUtils.lenLessEqualThan(str, KvStore.MAX_VALUE_LENGTH)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "value over maximum size.");
        }
    }

    private Value innerGetValue(String str) {
        assertKey(str);
        Value nativeGet = nativeGet(TextUtils.getKeyBytes(str), this.nativeSnapshot);
        if (!Objects.isNull(nativeGet)) {
            return nativeGet;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "not exist key value");
    }
}
