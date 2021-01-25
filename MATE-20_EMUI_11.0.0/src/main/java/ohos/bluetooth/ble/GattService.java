package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.SequenceUuid;
import ohos.utils.Sequenceable;

public class GattService implements Sequenceable {
    private static final int DEFAULT_GATT_INFO_SIZE = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "GattService");
    private List<GattCharacteristic> mCharacteristics;
    private int mHandle;
    private List<GattService> mIncludedServices;
    private boolean mIsPrimary;
    private UUID mUuid;

    public GattService(UUID uuid, boolean z) {
        this.mUuid = uuid;
        this.mHandle = 0;
        this.mIsPrimary = z;
        this.mCharacteristics = new ArrayList();
        this.mIncludedServices = new ArrayList();
    }

    GattService(UUID uuid, int i, boolean z) {
        this.mUuid = uuid;
        this.mHandle = i;
        this.mIsPrimary = z;
        this.mCharacteristics = new ArrayList();
        this.mIncludedServices = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public int getHandle() {
        return this.mHandle;
    }

    /* access modifiers changed from: package-private */
    public void setHandle(int i) {
        this.mHandle = i;
    }

    public boolean isPrimary() {
        return this.mIsPrimary;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public boolean addService(GattService gattService) {
        this.mIncludedServices.add(gattService);
        return true;
    }

    public List<GattService> getIncludedServices() {
        return this.mIncludedServices;
    }

    public boolean addCharacteristic(GattCharacteristic gattCharacteristic) {
        this.mCharacteristics.add(gattCharacteristic);
        gattCharacteristic.setService(this);
        return true;
    }

    public List<GattCharacteristic> getCharacteristics() {
        return this.mCharacteristics;
    }

    public Optional<GattCharacteristic> getCharacteristic(UUID uuid) {
        for (GattCharacteristic gattCharacteristic : this.mCharacteristics) {
            if (Objects.equals(uuid, gattCharacteristic.getUuid())) {
                return Optional.ofNullable(gattCharacteristic);
            }
        }
        return Optional.empty();
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeBoolean(this.mIsPrimary);
        parcel.writeInt(this.mCharacteristics.size());
        for (GattCharacteristic gattCharacteristic : this.mCharacteristics) {
            parcel.writeSequenceable(gattCharacteristic);
        }
        parcel.writeInt(this.mIncludedServices.size());
        for (GattService gattService : this.mIncludedServices) {
            parcel.writeSequenceable(new SequenceUuid(gattService.mUuid));
            parcel.writeInt(gattService.mHandle);
            parcel.writeBoolean(gattService.mIsPrimary);
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        SequenceUuid sequenceUuid = new SequenceUuid();
        parcel.readSequenceable(sequenceUuid);
        this.mUuid = sequenceUuid.getUuid();
        this.mHandle = parcel.readInt();
        this.mIsPrimary = parcel.readBoolean();
        int readInt = parcel.readInt();
        if (readInt < 0) {
            HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
            return false;
        }
        this.mCharacteristics = new ArrayList(3);
        for (int i = 0; i < readInt; i++) {
            if (parcel.getReadableBytes() <= 0) {
                HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
                return false;
            }
            GattCharacteristic gattCharacteristic = new GattCharacteristic();
            parcel.readSequenceable(gattCharacteristic);
            gattCharacteristic.setService(this);
            this.mCharacteristics.add(gattCharacteristic);
        }
        int readInt2 = parcel.readInt();
        if (readInt2 < 0) {
            HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
            return false;
        }
        this.mIncludedServices = new ArrayList(3);
        for (int i2 = 0; i2 < readInt2; i2++) {
            if (parcel.getReadableBytes() <= 0) {
                HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
                return false;
            }
            SequenceUuid sequenceUuid2 = new SequenceUuid();
            parcel.readSequenceable(sequenceUuid2);
            this.mIncludedServices.add(new GattService(sequenceUuid2.getUuid(), parcel.readInt(), parcel.readBoolean()));
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean unmarshallingSpecially(Parcel parcel) {
        parcel.readString();
        SequenceUuid sequenceUuid = new SequenceUuid();
        parcel.readSequenceable(sequenceUuid);
        this.mUuid = sequenceUuid.getUuid();
        this.mHandle = parcel.readInt();
        this.mIsPrimary = parcel.readInt() == 0;
        int readInt = parcel.readInt();
        if (readInt == -1) {
            this.mCharacteristics = new ArrayList();
        } else {
            this.mCharacteristics = new ArrayList(3);
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.readInt() == 1) {
                GattCharacteristic gattCharacteristic = new GattCharacteristic();
                gattCharacteristic.unmarshallingSpecially(parcel);
                gattCharacteristic.setService(this);
                this.mCharacteristics.add(gattCharacteristic);
            }
        }
        int readInt2 = parcel.readInt();
        if (readInt2 == -1) {
            this.mIncludedServices = new ArrayList();
        } else {
            this.mIncludedServices = new ArrayList(3);
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            parcel.readString();
            SequenceUuid sequenceUuid2 = new SequenceUuid();
            parcel.readSequenceable(sequenceUuid2);
            this.mIncludedServices.add(new GattService(sequenceUuid2.getUuid(), parcel.readInt(), parcel.readInt() == 0));
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean marshallingSpecially(Parcel parcel) {
        parcel.writeString("android.os.ParcelUuid");
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeBoolean(this.mIsPrimary);
        parcel.writeInt(this.mCharacteristics.size());
        for (GattCharacteristic gattCharacteristic : this.mCharacteristics) {
            if (gattCharacteristic != null) {
                parcel.writeInt(1);
                gattCharacteristic.marshallingSpecially(parcel);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(this.mIncludedServices.size());
        for (GattCharacteristic gattCharacteristic2 : this.mCharacteristics) {
            if (gattCharacteristic2 != null) {
                parcel.writeInt(1);
                gattCharacteristic2.marshallingSpecially(parcel);
            } else {
                parcel.writeInt(0);
            }
        }
        parcel.writeInt(this.mIncludedServices.size());
        for (GattService gattService : this.mIncludedServices) {
            if (gattService != null) {
                parcel.writeInt(1);
                gattService.marshallingSpecially(parcel);
            } else {
                parcel.writeInt(0);
            }
        }
        return true;
    }
}
