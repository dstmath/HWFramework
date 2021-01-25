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

public class GattCharacteristic implements Sequenceable {
    private static final int DEFAULT_GATT_INFO_NUM = 3;
    public static final int PROPERTY_READ = 2;
    public static final int PROPERTY_WRITE = 8;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 4;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "GattCharacteristic");
    private static final int WRITE_TYPE_DEFAULT = 2;
    private static final int WRITE_TYPE_NO_RESPONSE = 1;
    private List<GattDescriptor> mDescriptors;
    private int mHandle;
    private int mPermissions;
    private int mProperties;
    private GattService mService;
    private UUID mUuid;
    private byte[] mValues;
    private int mWriteType;

    public GattCharacteristic(UUID uuid, int i, int i2) {
        this.mUuid = uuid;
        this.mHandle = 0;
        this.mPermissions = i;
        this.mProperties = i2;
        this.mWriteType = (this.mProperties & 4) != 0 ? 1 : 2;
        this.mDescriptors = new ArrayList();
    }

    GattCharacteristic() {
        this.mDescriptors = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public void setService(GattService gattService) {
        this.mService = gattService;
    }

    /* access modifiers changed from: package-private */
    public int getHandle() {
        return this.mHandle;
    }

    /* access modifiers changed from: package-private */
    public void setHandle(int i) {
        this.mHandle = i;
    }

    /* access modifiers changed from: package-private */
    public int getWriteType() {
        return this.mWriteType;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public int getPermissions() {
        return this.mPermissions;
    }

    public int getProperties() {
        return this.mProperties;
    }

    public boolean setValue(byte[] bArr) {
        if (bArr == null) {
            this.mValues = null;
            return false;
        }
        this.mValues = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.mValues, 0, bArr.length);
        return true;
    }

    public byte[] getValue() {
        byte[] bArr = this.mValues;
        if (bArr == null) {
            return new byte[0];
        }
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public GattService getService() {
        return this.mService;
    }

    public boolean addDescriptor(GattDescriptor gattDescriptor) {
        this.mDescriptors.add(gattDescriptor);
        gattDescriptor.setCharacteristic(this);
        return true;
    }

    public List<GattDescriptor> getDescriptors() {
        return this.mDescriptors;
    }

    public Optional<GattDescriptor> getDescriptor(UUID uuid) {
        for (GattDescriptor gattDescriptor : this.mDescriptors) {
            if (Objects.equals(uuid, gattDescriptor.getUuid())) {
                return Optional.ofNullable(gattDescriptor);
            }
        }
        return Optional.empty();
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeInt(this.mPermissions);
        parcel.writeInt(this.mProperties);
        parcel.writeInt(this.mWriteType);
        parcel.writeInt(this.mDescriptors.size());
        for (GattDescriptor gattDescriptor : this.mDescriptors) {
            parcel.writeSequenceable(gattDescriptor);
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        SequenceUuid sequenceUuid = new SequenceUuid();
        parcel.readSequenceable(sequenceUuid);
        this.mUuid = sequenceUuid.getUuid();
        this.mHandle = parcel.readInt();
        this.mPermissions = parcel.readInt();
        this.mProperties = parcel.readInt();
        this.mWriteType = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt < 0) {
            this.mDescriptors = new ArrayList();
        } else {
            this.mDescriptors = new ArrayList(3);
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.getReadableBytes() <= 0) {
                HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
                return true;
            }
            GattDescriptor gattDescriptor = new GattDescriptor();
            parcel.readSequenceable(gattDescriptor);
            gattDescriptor.setCharacteristic(this);
            this.mDescriptors.add(gattDescriptor);
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
        this.mProperties = parcel.readInt();
        this.mPermissions = parcel.readInt();
        parcel.readInt();
        this.mWriteType = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt < 0) {
            this.mDescriptors = new ArrayList();
        } else {
            this.mDescriptors = new ArrayList(3);
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.getReadableBytes() <= 0) {
                HiLog.warn(TAG, "unmarshallingSpecially failed due to data size mismatch", new Object[0]);
                return true;
            }
            if (parcel.readInt() == 1) {
                GattDescriptor gattDescriptor = new GattDescriptor();
                gattDescriptor.unmarshallingSpecially(parcel);
                gattDescriptor.setCharacteristic(this);
                this.mDescriptors.add(gattDescriptor);
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean marshallingSpecially(Parcel parcel) {
        parcel.writeString("android.os.ParcelUuid");
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeInt(this.mProperties);
        parcel.writeInt(this.mPermissions);
        parcel.writeInt(16);
        parcel.writeInt(this.mWriteType);
        parcel.writeInt(this.mDescriptors.size());
        for (GattDescriptor gattDescriptor : this.mDescriptors) {
            if (gattDescriptor != null) {
                parcel.writeInt(1);
                gattDescriptor.marshallingSpecially(parcel);
            } else {
                parcel.writeInt(0);
            }
        }
        return true;
    }
}
