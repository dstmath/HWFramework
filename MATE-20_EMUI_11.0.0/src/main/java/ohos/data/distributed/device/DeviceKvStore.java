package ohos.data.distributed.device;

import java.util.List;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreResultSet;
import ohos.data.distributed.common.Query;
import ohos.data.distributed.common.SyncCallback;
import ohos.data.distributed.common.SyncMode;

public interface DeviceKvStore extends KvStore {
    void closeResultSet(KvStoreResultSet kvStoreResultSet) throws KvStoreException;

    boolean getBoolean(String str, String str2) throws KvStoreException;

    byte[] getByteArray(String str, String str2) throws KvStoreException;

    double getDouble(String str, String str2) throws KvStoreException;

    List<Entry> getEntries(String str, String str2) throws KvStoreException;

    List<Entry> getEntries(Query query) throws KvStoreException;

    float getFloat(String str, String str2) throws KvStoreException;

    int getInt(String str, String str2) throws KvStoreException;

    KvStoreResultSet getResultSet(String str, String str2) throws KvStoreException;

    KvStoreResultSet getResultSet(Query query) throws KvStoreException;

    int getResultSize(Query query) throws KvStoreException;

    String getString(String str, String str2) throws KvStoreException;

    void registerSyncCallback(SyncCallback syncCallback) throws KvStoreException;

    void removeDeviceData(String str) throws KvStoreException;

    void sync(List<String> list, SyncMode syncMode) throws KvStoreException;

    void unRegisterSyncCallback() throws KvStoreException;
}
