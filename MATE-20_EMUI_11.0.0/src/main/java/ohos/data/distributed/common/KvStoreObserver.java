package ohos.data.distributed.common;

import ohos.data.distributed.user.KvStoreSnapshot;

public interface KvStoreObserver {
    default void onChange(ChangeNotification changeNotification) {
    }

    default void onChange(ChangeNotification changeNotification, KvStoreSnapshot kvStoreSnapshot) {
    }
}
