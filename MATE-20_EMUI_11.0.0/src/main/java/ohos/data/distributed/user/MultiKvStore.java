package ohos.data.distributed.user;

import ohos.data.distributed.common.KvStore;
import ohos.data.distributed.common.KvStoreException;
import ohos.data.distributed.common.KvStoreObserver;

public interface MultiKvStore extends KvStore {
    void clear() throws KvStoreException;

    KvStoreSnapshot getKvStoreSnapshot(KvStoreObserver kvStoreObserver) throws KvStoreException;

    void releaseKvStoreSnapshot(KvStoreSnapshot kvStoreSnapshot) throws KvStoreException;
}
