package ohos.bluetooth.ble;

import java.util.UUID;
import ohos.utils.Parcel;
import ohos.utils.SequenceUuid;
import ohos.utils.Sequenceable;

public class GattDescriptor implements Sequenceable {
    private GattCharacteristic mCharacteristic;
    private int mHandle;
    private int mPermissions;
    private UUID mUuid;
    private byte[] mValues;

    public GattDescriptor(UUID uuid, int i) {
        this.mUuid = uuid;
        this.mHandle = 0;
        this.mPermissions = i;
    }

    public GattDescriptor() {
    }

    /* access modifiers changed from: package-private */
    public void setCharacteristic(GattCharacteristic gattCharacteristic) {
        this.mCharacteristic = gattCharacteristic;
    }

    /* access modifiers changed from: package-private */
    public int getHandle() {
        return this.mHandle;
    }

    /* access modifiers changed from: package-private */
    public void setHandle(int i) {
        this.mHandle = i;
    }

    public GattCharacteristic getCharacteristic() {
        return this.mCharacteristic;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public int getPermissions() {
        return this.mPermissions;
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

    public boolean setValue(byte[] bArr) {
        if (bArr != null) {
            this.mValues = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.mValues, 0, bArr.length);
            return true;
        }
        this.mValues = null;
        return false;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(new SequenceUuid(this.mUuid));
        parcel.writeInt(this.mHandle);
        parcel.writeInt(this.mPermissions);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        SequenceUuid sequenceUuid = new SequenceUuid();
        parcel.readSequenceable(sequenceUuid);
        this.mUuid = sequenceUuid.getUuid();
        this.mHandle = parcel.readInt();
        this.mPermissions = parcel.readInt();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean unmarshallingSpecially(Parcel parcel) {
        parcel.readString();
        this.mUuid = new UUID(parcel.readLong(), parcel.readLong());
        this.mHandle = parcel.readInt();
        this.mPermissions = parcel.readInt();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean marshallingSpecially(Parcel parcel) {
        parcel.writeString("android.os.ParcelUuid");
        long leastSignificantBits = this.mUuid.getLeastSignificantBits();
        parcel.writeLong(this.mUuid.getMostSignificantBits());
        parcel.writeLong(leastSignificantBits);
        parcel.writeInt(this.mHandle);
        parcel.writeInt(this.mPermissions);
        return true;
    }
}
