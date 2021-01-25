package ohos.data.distributed.user;

import java.util.List;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreResultSet;
import ohos.data.distributed.common.Query;
import ohos.data.distributed.common.SyncCallback;
import ohos.data.distributed.common.SyncMode;

public interface SingleKvStore extends KvStore {
    void closeResultSet(KvStoreResultSet kvStoreResultSet) throws KvStoreException;

    boolean getBoolean(String str) throws KvStoreException;

    byte[] getByteArray(String str) throws KvStoreException;

    double getDouble(String str) throws KvStoreException;

    List<Entry> getEntries(String str) throws KvStoreException;

    List<Entry> getEntries(Query query) throws KvStoreException;

    float getFloat(String str) throws KvStoreException;

    int getInt(String str) throws KvStoreException;

    KvStoreResultSet getResultSet(String str) throws KvStoreException;

    KvStoreResultSet getResultSet(Query query) throws KvStoreException;

    int getResultSize(Query query) throws KvStoreException;

    String getString(String str) throws KvStoreException;

    void registerSyncCallback(SyncCallback syncCallback) throws KvStoreException;

    void removeDeviceData(String str) throws KvStoreException;

    void setSyncParam(int i) throws KvStoreException;

    void sync(List<String> list, SyncMode syncMode) throws KvStoreException;

    void sync(List<String> list, SyncMode syncMode, int i) throws KvStoreException;

    void unRegisterSyncCallback() throws KvStoreException;
}
