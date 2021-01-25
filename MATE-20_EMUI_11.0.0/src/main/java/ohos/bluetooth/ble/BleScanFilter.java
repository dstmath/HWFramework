package ohos.bluetooth.ble;

import java.util.UUID;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class BleScanFilter implements Sequenceable {
    private byte[] mManufacturerData;
    private int mManufacturerId;
    private byte[] mManufacturerMask;
    private String mPeripheralAddress;
    private String mPeripheralName;
    private byte[] mServiceData;
    private byte[] mServiceDataMask;
    private UUID mServiceDataUuid;
    private UUID mServiceUuid;
    private UUID mServiceUuidMask;

    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public void setPeripheralAddress(String str) {
        this.mPeripheralAddress = str;
    }

    public void setPeripheralName(String str) {
        this.mPeripheralName = str;
    }

    public void setPeripheralServiceData(UUID uuid, byte[] bArr, byte[] bArr2) {
        this.mServiceDataUuid = uuid;
        this.mServiceData = null;
        this.mServiceDataMask = null;
        if (bArr != null) {
            this.mServiceData = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.mServiceData, 0, bArr.length);
        }
        if (bArr2 != null) {
            this.mServiceDataMask = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.mServiceDataMask, 0, bArr2.length);
        }
    }

    public void setPeripheralServiceUuid(UUID uuid, UUID uuid2) {
        this.mServiceUuid = uuid;
        this.mServiceUuidMask = uuid2;
    }

    public void setPeripheralManufacturerData(int i, byte[] bArr, byte[] bArr2) {
        this.mManufacturerId = i;
        this.mManufacturerData = null;
        this.mManufacturerMask = null;
        if (bArr != null) {
            this.mManufacturerData = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.mManufacturerData, 0, bArr.length);
        }
        if (bArr2 != null) {
            this.mManufacturerMask = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.mManufacturerMask, 0, bArr2.length);
        }
    }

    public String getPeripheralAddress() {
        return this.mPeripheralAddress;
    }

    public String getPeripheralName() {
        return this.mPeripheralName;
    }

    public byte[] getPeripheralServiceData() {
        byte[] bArr = this.mServiceData;
        if (bArr == null) {
            return null;
        }
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public UUID getPeripheralServcieUuid() {
        return this.mServiceUuid;
    }

    public byte[] getPeripheralManufacturerData() {
        byte[] bArr = this.mManufacturerData;
        if (bArr == null) {
            return null;
        }
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public boolean marshalling(Parcel parcel) {
        int i = 0;
        parcel.writeInt(this.mPeripheralName == null ? 0 : 1);
        String str = this.mPeripheralName;
        if (str != null) {
            parcel.writeString(str);
        }
        parcel.writeInt(this.mPeripheralAddress == null ? 0 : 1);
        String str2 = this.mPeripheralAddress;
        if (str2 != null) {
            parcel.writeString(str2);
        }
        parcel.writeInt(this.mServiceUuid == null ? 0 : 1);
        UUID uuid = this.mServiceUuid;
        if (uuid != null) {
            parcel.writeLong(uuid.getMostSignificantBits());
            parcel.writeLong(this.mServiceUuid.getLeastSignificantBits());
            parcel.writeInt(this.mServiceUuidMask == null ? 0 : 1);
            UUID uuid2 = this.mServiceUuidMask;
            if (uuid2 != null) {
                parcel.writeLong(uuid2.getMostSignificantBits());
                parcel.writeLong(this.mServiceUuidMask.getLeastSignificantBits());
            }
        }
        parcel.writeInt(0);
        parcel.writeInt(this.mServiceDataUuid == null ? 0 : 1);
        UUID uuid3 = this.mServiceDataUuid;
        if (uuid3 != null) {
            parcel.writeLong(uuid3.getMostSignificantBits());
            parcel.writeLong(this.mServiceDataUuid.getLeastSignificantBits());
            parcel.writeInt(this.mServiceData == null ? 0 : 1);
            byte[] bArr = this.mServiceData;
            if (bArr != null) {
                parcel.writeInt(bArr.length);
                parcel.writeByteArray(this.mServiceData);
                parcel.writeInt(this.mServiceDataMask == null ? 0 : 1);
                byte[] bArr2 = this.mServiceDataMask;
                if (bArr2 != null) {
                    parcel.writeInt(bArr2.length);
                    parcel.writeByteArray(this.mServiceDataMask);
                }
            }
        }
        parcel.writeInt(this.mManufacturerId);
        parcel.writeInt(this.mManufacturerData == null ? 0 : 1);
        byte[] bArr3 = this.mManufacturerData;
        if (bArr3 != null) {
            parcel.writeInt(bArr3.length);
            parcel.writeByteArray(this.mManufacturerData);
            if (this.mManufacturerMask != null) {
                i = 1;
            }
            parcel.writeInt(i);
            byte[] bArr4 = this.mManufacturerMask;
            if (bArr4 != null) {
                parcel.writeInt(bArr4.length);
                parcel.writeByteArray(this.mManufacturerMask);
            }
        }
        return true;
    }
}
