package ohos.storageinfomgr;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class StorageInfo implements Sequenceable {
    public static final Sequenceable.Producer<StorageInfo> PRODUCER = new Sequenceable.Producer<StorageInfo>() {
        /* class ohos.storageinfomgr.StorageInfo.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public StorageInfo createFromParcel(Parcel parcel) {
            if (parcel == null) {
                return null;
            }
            return new StorageInfo(parcel);
        }
    };
    private long appSize;
    private long cacheSize;
    private long dataSize;

    private StorageInfo(Parcel parcel) {
        this.appSize = parcel.readLong();
        this.dataSize = parcel.readLong();
        this.cacheSize = parcel.readLong();
    }

    public long getAppSize() {
        return this.appSize;
    }

    public long getDataSize() {
        return this.dataSize;
    }

    public long getCacheSize() {
        return this.cacheSize;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        return parcel.writeLong(this.cacheSize) & parcel.writeLong(this.appSize) & true & parcel.writeLong(this.dataSize);
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.appSize = parcel.readLong();
        this.dataSize = parcel.readLong();
        this.cacheSize = parcel.readLong();
        return true;
    }
}
