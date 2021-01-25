package ohos.data.distributed.common;

import java.util.List;
import ohos.data.distributed.device.DeviceChangeCallback;
import ohos.data.distributed.device.DeviceFilterStrategy;
import ohos.data.distributed.device.DeviceInfo;

public interface KvManager {
    void closeKvStore(KvStore kvStore) throws KvStoreException;

    void deleteKvStore(String str) throws KvStoreException;

    List<String> getAllKvStoreId() throws KvStoreException;

    List<DeviceInfo> getConnectedDevicesInfo(DeviceFilterStrategy deviceFilterStrategy) throws KvStoreException;

    <KVSTORE extends KvStore> KVSTORE getKvStore(Options options, String str) throws KvStoreException;

    DeviceInfo getLocalDeviceInfo() throws KvStoreException;

    void registerDeviceChangeCallback(DeviceChangeCallback deviceChangeCallback, DeviceFilterStrategy deviceFilterStrategy) throws KvStoreException;

    void unRegisterDeviceChangeCallback(DeviceChangeCallback deviceChangeCallback) throws KvStoreException;
}
