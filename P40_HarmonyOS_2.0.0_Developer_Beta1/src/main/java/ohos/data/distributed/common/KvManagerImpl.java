package ohos.data.distributed.common;

import java.util.List;
import java.util.Objects;
import ohos.data.distributed.device.DeviceChangeCallback;
import ohos.data.distributed.device.DeviceFilterStrategy;
import ohos.data.distributed.device.DeviceInfo;
import ohos.data.distributed.device.DeviceKvStoreImpl;
import ohos.data.distributed.user.MultiKvStoreImpl;
import ohos.data.distributed.user.SingleKvStoreImpl;

public class KvManagerImpl implements KvManager {
    private static final String LABEL = "KvManagerImpl";
    private static final Object LOCK = new Object();
    private KvManagerConfig config;
    private long nativeManager = nativeSetup();

    private static native void nativeCloseKvStore(Options options, String str, KvStore kvStore, long j);

    private static native void nativeDeleteKvStore(String str, String str2, long j);

    private static native List<String> nativeGetAllKvStoreId(String str, long j);

    private static native List<DeviceInfo> nativeGetConnectedDevicesInfo(String str, long j);

    private static native long nativeGetKvStore(Options options, String str, String str2, long j);

    private static native DeviceInfo nativeGetLocalDeviceInfo(long j);

    private static native void nativeRegisterDeviceChangeListener(DeviceChangeCallback deviceChangeCallback, String str, long j);

    private static native void nativeRegisterServiceDeath(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient, long j);

    private static native long nativeSetup();

    private static native void nativeUnRegisterDeviceChangeListener(DeviceChangeCallback deviceChangeCallback, long j);

    private static native void nativeUnRegisterServiceDeath(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient, long j);

    static {
        System.loadLibrary("distributeddata_jni.z");
    }

    public KvManagerImpl(KvManagerConfig kvManagerConfig) throws KvStoreException {
        this.config = kvManagerConfig;
    }

    @Override // ohos.data.distributed.common.KvManager
    public <KVSTORE extends KvStore> KVSTORE getKvStore(Options options, String str) throws KvStoreException {
        KVSTORE kvstore;
        LogPrint.debug(LABEL, "getKvStore start.", new Object[0]);
        if (Objects.isNull(options) || TextUtils.isEmpty(str) || Objects.isNull(options.getKvStoreType()) || !TextUtils.lenLessEqualThan(str, 128)) {
            LogPrint.warn(LABEL, "getKvStore invalid argument ", new Object[0]);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "invalid argument");
        }
        KvStoreType kvStoreType = options.getKvStoreType();
        if (kvStoreType == KvStoreType.DEVICE_COLLABORATION || kvStoreType == KvStoreType.SINGLE_VERSION || kvStoreType == KvStoreType.MULTI_VERSION) {
            synchronized (LOCK) {
                long nativeGetKvStore = nativeGetKvStore(options, str, this.config.getBundleName(), this.nativeManager);
                if (nativeGetKvStore == 0) {
                    LogPrint.warn(LABEL, "native getKvStore failed.", new Object[0]);
                    throw new KvStoreException(KvStoreErrorCode.UNKNOWN_ERROR, "native getKvStore failed.");
                } else if (options.getKvStoreType() == KvStoreType.DEVICE_COLLABORATION) {
                    kvstore = new DeviceKvStoreImpl(new SingleKvStoreImpl(this.config, str, nativeGetKvStore, options));
                } else if (options.getKvStoreType() == KvStoreType.MULTI_VERSION) {
                    kvstore = new MultiKvStoreImpl(this.config, str, nativeGetKvStore, options);
                } else {
                    kvstore = new SingleKvStoreImpl(this.config, str, nativeGetKvStore, options);
                }
            }
            return kvstore;
        }
        LogPrint.warn(LABEL, "getKvStore type is invalid", new Object[0]);
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "invalid argument");
    }

    @Override // ohos.data.distributed.common.KvManager
    public void closeKvStore(KvStore kvStore) throws KvStoreException {
        long j;
        Options options;
        if (!Objects.isNull(kvStore)) {
            synchronized (LOCK) {
                if (kvStore.getClass().isAssignableFrom(MultiKvStoreImpl.class)) {
                    MultiKvStoreImpl multiKvStoreImpl = (MultiKvStoreImpl) MultiKvStoreImpl.class.cast(kvStore);
                    j = multiKvStoreImpl.getNativeKvStore();
                    options = multiKvStoreImpl.getOptions();
                } else if (kvStore.getClass().isAssignableFrom(DeviceKvStoreImpl.class)) {
                    DeviceKvStoreImpl deviceKvStoreImpl = (DeviceKvStoreImpl) DeviceKvStoreImpl.class.cast(kvStore);
                    options = deviceKvStoreImpl.getOptions();
                    j = deviceKvStoreImpl.getSingleKvStore().getNativeKvStore();
                    kvStore = deviceKvStoreImpl.getSingleKvStore();
                } else if (kvStore.getClass().isAssignableFrom(SingleKvStoreImpl.class)) {
                    SingleKvStoreImpl singleKvStoreImpl = (SingleKvStoreImpl) SingleKvStoreImpl.class.cast(kvStore);
                    Options options2 = singleKvStoreImpl.getOptions();
                    long nativeKvStore = singleKvStoreImpl.getNativeKvStore();
                    options = options2;
                    j = nativeKvStore;
                } else {
                    throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "invalid argument.");
                }
                if (j != 0) {
                    LogPrint.info(LABEL, "closeKvStore.", new Object[0]);
                    nativeCloseKvStore(options, this.config.getBundleName(), kvStore, this.nativeManager);
                } else {
                    LogPrint.error(LABEL, "KvStore closed already.", new Object[0]);
                    throw new KvStoreException(KvStoreErrorCode.STORE_NOT_OPEN, "kvStore closed already.");
                }
            }
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "input param is null.");
    }

    @Override // ohos.data.distributed.common.KvManager
    public void deleteKvStore(String str) throws KvStoreException {
        if (TextUtils.isEmpty(str)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "storeId is null or empty.");
        } else if (TextUtils.lenLessEqualThan(str, 128)) {
            LogPrint.info(LABEL, "deleteKvStore.", new Object[0]);
            synchronized (LOCK) {
                nativeDeleteKvStore(str, this.config.getBundleName(), this.nativeManager);
            }
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "storeId is too long.");
        }
    }

    @Override // ohos.data.distributed.common.KvManager
    public List<String> getAllKvStoreId() throws KvStoreException {
        List<String> nativeGetAllKvStoreId;
        synchronized (LOCK) {
            nativeGetAllKvStoreId = nativeGetAllKvStoreId(this.config.getBundleName(), this.nativeManager);
        }
        return nativeGetAllKvStoreId;
    }

    public void registerKvStoreServiceDeathRecipient(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient) throws KvStoreException {
        if (!Objects.isNull(kvStoreServiceDeathRecipient)) {
            synchronized (LOCK) {
                nativeRegisterServiceDeath(kvStoreServiceDeathRecipient, this.nativeManager);
            }
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
    }

    public void unRegisterKvStoreServiceDeathRecipient(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient) throws KvStoreException {
        if (!Objects.isNull(kvStoreServiceDeathRecipient)) {
            synchronized (LOCK) {
                nativeUnRegisterServiceDeath(kvStoreServiceDeathRecipient, this.nativeManager);
            }
            return;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
    }

    @Override // ohos.data.distributed.common.KvManager
    public List<DeviceInfo> getConnectedDevicesInfo(DeviceFilterStrategy deviceFilterStrategy) throws KvStoreException {
        return nativeGetConnectedDevicesInfo(deviceFilterStrategy.name(), this.nativeManager);
    }

    @Override // ohos.data.distributed.common.KvManager
    public DeviceInfo getLocalDeviceInfo() throws KvStoreException {
        return nativeGetLocalDeviceInfo(this.nativeManager);
    }

    @Override // ohos.data.distributed.common.KvManager
    public void registerDeviceChangeCallback(DeviceChangeCallback deviceChangeCallback, DeviceFilterStrategy deviceFilterStrategy) throws KvStoreException {
        nativeRegisterDeviceChangeListener(deviceChangeCallback, deviceFilterStrategy.name(), this.nativeManager);
    }

    @Override // ohos.data.distributed.common.KvManager
    public void unRegisterDeviceChangeCallback(DeviceChangeCallback deviceChangeCallback) throws KvStoreException {
        nativeUnRegisterDeviceChangeListener(deviceChangeCallback, this.nativeManager);
    }
}
