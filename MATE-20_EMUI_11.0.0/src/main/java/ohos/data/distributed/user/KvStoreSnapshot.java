package ohos.data.distributed.user;

import java.util.List;
import ohos.data.distributed.common.Entry;
import ohos.data.distributed.common.KvStoreException;

public interface KvStoreSnapshot {
    boolean getBoolean(String str) throws KvStoreException;

    byte[] getByteArray(String str) throws KvStoreException;

    double getDouble(String str) throws KvStoreException;

    List<Entry> getEntries(String str) throws KvStoreException;

    float getFloat(String str) throws KvStoreException;

    int getInt(String str) throws KvStoreException;

    List<String> getKeys(String str) throws KvStoreException;

    String getString(String str) throws KvStoreException;
}
