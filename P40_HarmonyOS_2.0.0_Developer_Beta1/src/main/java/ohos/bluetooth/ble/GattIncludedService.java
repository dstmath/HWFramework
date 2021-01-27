package ohos.bluetooth.ble;

import java.util.UUID;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.SequenceUuid;
import ohos.utils.Sequenceable;

public class GattIncludedService implements Sequenceable {
    private static final int DEFAULT_GATT_INFO_SIZE = 3;
    private static final int NOT_PRIMARY = 1;
    private static final int PRIMARY = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "GattIncludedService");
    private int mHandle;
    private boolean mIsPrimary;
    private UUID mUuid;

    GattIncludedService(UUID uuid, int i, boolean z) {
        this.mUuid = uuid;
        this.mHandle = i;
        this.mIsPrimary = z;
    }

    /* access modifiers changed from: package-private */
    public int getHandle() {
        return this.mHandle;
    }

    public boolean isPrimary() {
        return this.mIsPrimary;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeBoolean(this.mIsPrimary);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        SequenceUuid sequenceUuid = new SequenceUuid();
        parcel.readSequenceable(sequenceUuid);
        this.mUuid = sequenceUuid.getUuid();
        this.mHandle = parcel.readInt();
        this.mIsPrimary = parcel.readBoolean();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean unmarshallingSpecially(Parcel parcel) {
        parcel.readString();
        this.mUuid = new UUID(parcel.readLong(), parcel.readLong());
        this.mHandle = parcel.readInt();
        this.mIsPrimary = parcel.readInt() == 0;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean marshallingSpecially(Parcel parcel) {
        parcel.writeString("android.os.ParcelUuid");
        long leastSignificantBits = this.mUuid.getLeastSignificantBits();
        parcel.writeLong(this.mUuid.getMostSignificantBits());
        parcel.writeLong(leastSignificantBits);
        parcel.writeInt(this.mHandle);
        if (this.mIsPrimary) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(1);
        }
        return true;
    }
}
