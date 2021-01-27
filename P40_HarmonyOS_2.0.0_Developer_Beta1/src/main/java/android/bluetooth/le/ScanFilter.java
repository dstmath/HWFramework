package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import com.android.internal.util.BitUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ScanFilter implements Parcelable {
    public static final Parcelable.Creator<ScanFilter> CREATOR = new Parcelable.Creator<ScanFilter>() {
        /* class android.bluetooth.le.ScanFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ScanFilter[] newArray(int size) {
            return new ScanFilter[size];
        }

        @Override // android.os.Parcelable.Creator
        public ScanFilter createFromParcel(Parcel in) {
            Builder builder = new Builder();
            if (in.readInt() == 1) {
                builder.setDeviceName(in.readString());
            }
            if (in.readInt() == 1) {
                builder.setDeviceAddress(in.readString());
            }
            if (in.readInt() == 1) {
                ParcelUuid uuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceUuid(uuid);
                if (in.readInt() == 1) {
                    builder.setServiceUuid(uuid, (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader()));
                }
            }
            if (in.readInt() == 1) {
                ParcelUuid solicitationUuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceSolicitationUuid(solicitationUuid);
                if (in.readInt() == 1) {
                    builder.setServiceSolicitationUuid(solicitationUuid, (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader()));
                }
            }
            if (in.readInt() == 1) {
                ParcelUuid servcieDataUuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    byte[] serviceData = new byte[in.readInt()];
                    in.readByteArray(serviceData);
                    if (in.readInt() == 0) {
                        builder.setServiceData(servcieDataUuid, serviceData);
                    } else {
                        byte[] serviceDataMask = new byte[in.readInt()];
                        in.readByteArray(serviceDataMask);
                        builder.setServiceData(servcieDataUuid, serviceData, serviceDataMask);
                    }
                }
            }
            int manufacturerId = in.readInt();
            if (in.readInt() == 1) {
                byte[] manufacturerData = new byte[in.readInt()];
                in.readByteArray(manufacturerData);
                if (in.readInt() == 0) {
                    builder.setManufacturerData(manufacturerId, manufacturerData);
                } else {
                    byte[] manufacturerDataMask = new byte[in.readInt()];
                    in.readByteArray(manufacturerDataMask);
                    builder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask);
                }
            }
            return builder.build();
        }
    };
    public static final ScanFilter EMPTY = new Builder().build();
    private final String mDeviceAddress;
    private final String mDeviceName;
    private final byte[] mManufacturerData;
    private final byte[] mManufacturerDataMask;
    private final int mManufacturerId;
    private final byte[] mServiceData;
    private final byte[] mServiceDataMask;
    private final ParcelUuid mServiceDataUuid;
    private final ParcelUuid mServiceSolicitationUuid;
    private final ParcelUuid mServiceSolicitationUuidMask;
    private final ParcelUuid mServiceUuid;
    private final ParcelUuid mServiceUuidMask;

    private ScanFilter(String name, String deviceAddress, ParcelUuid uuid, ParcelUuid uuidMask, ParcelUuid solicitationUuid, ParcelUuid solicitationUuidMask, ParcelUuid serviceDataUuid, byte[] serviceData, byte[] serviceDataMask, int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask) {
        this.mDeviceName = name;
        this.mServiceUuid = uuid;
        this.mServiceUuidMask = uuidMask;
        this.mServiceSolicitationUuid = solicitationUuid;
        this.mServiceSolicitationUuidMask = solicitationUuidMask;
        this.mDeviceAddress = deviceAddress;
        this.mServiceDataUuid = serviceDataUuid;
        this.mServiceData = serviceData;
        this.mServiceDataMask = serviceDataMask;
        this.mManufacturerId = manufacturerId;
        this.mManufacturerData = manufacturerData;
        this.mManufacturerDataMask = manufacturerDataMask;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        dest.writeInt(this.mDeviceName == null ? 0 : 1);
        String str = this.mDeviceName;
        if (str != null) {
            dest.writeString(str);
        }
        dest.writeInt(this.mDeviceAddress == null ? 0 : 1);
        String str2 = this.mDeviceAddress;
        if (str2 != null) {
            dest.writeString(str2);
        }
        dest.writeInt(this.mServiceUuid == null ? 0 : 1);
        ParcelUuid parcelUuid = this.mServiceUuid;
        if (parcelUuid != null) {
            dest.writeParcelable(parcelUuid, flags);
            dest.writeInt(this.mServiceUuidMask == null ? 0 : 1);
            ParcelUuid parcelUuid2 = this.mServiceUuidMask;
            if (parcelUuid2 != null) {
                dest.writeParcelable(parcelUuid2, flags);
            }
        }
        dest.writeInt(this.mServiceSolicitationUuid == null ? 0 : 1);
        ParcelUuid parcelUuid3 = this.mServiceSolicitationUuid;
        if (parcelUuid3 != null) {
            dest.writeParcelable(parcelUuid3, flags);
            dest.writeInt(this.mServiceSolicitationUuidMask == null ? 0 : 1);
            ParcelUuid parcelUuid4 = this.mServiceSolicitationUuidMask;
            if (parcelUuid4 != null) {
                dest.writeParcelable(parcelUuid4, flags);
            }
        }
        dest.writeInt(this.mServiceDataUuid == null ? 0 : 1);
        ParcelUuid parcelUuid5 = this.mServiceDataUuid;
        if (parcelUuid5 != null) {
            dest.writeParcelable(parcelUuid5, flags);
            dest.writeInt(this.mServiceData == null ? 0 : 1);
            byte[] bArr = this.mServiceData;
            if (bArr != null) {
                dest.writeInt(bArr.length);
                dest.writeByteArray(this.mServiceData);
                dest.writeInt(this.mServiceDataMask == null ? 0 : 1);
                byte[] bArr2 = this.mServiceDataMask;
                if (bArr2 != null) {
                    dest.writeInt(bArr2.length);
                    dest.writeByteArray(this.mServiceDataMask);
                }
            }
        }
        dest.writeInt(this.mManufacturerId);
        dest.writeInt(this.mManufacturerData == null ? 0 : 1);
        byte[] bArr3 = this.mManufacturerData;
        if (bArr3 != null) {
            dest.writeInt(bArr3.length);
            dest.writeByteArray(this.mManufacturerData);
            if (this.mManufacturerDataMask != null) {
                i = 1;
            }
            dest.writeInt(i);
            byte[] bArr4 = this.mManufacturerDataMask;
            if (bArr4 != null) {
                dest.writeInt(bArr4.length);
                dest.writeByteArray(this.mManufacturerDataMask);
            }
        }
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public ParcelUuid getServiceUuid() {
        return this.mServiceUuid;
    }

    public ParcelUuid getServiceUuidMask() {
        return this.mServiceUuidMask;
    }

    public ParcelUuid getServiceSolicitationUuid() {
        return this.mServiceSolicitationUuid;
    }

    public ParcelUuid getServiceSolicitationUuidMask() {
        return this.mServiceSolicitationUuidMask;
    }

    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    public byte[] getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceDataMask() {
        return this.mServiceDataMask;
    }

    public ParcelUuid getServiceDataUuid() {
        return this.mServiceDataUuid;
    }

    public int getManufacturerId() {
        return this.mManufacturerId;
    }

    public byte[] getManufacturerData() {
        return this.mManufacturerData;
    }

    public byte[] getManufacturerDataMask() {
        return this.mManufacturerDataMask;
    }

    public boolean matches(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        BluetoothDevice device = scanResult.getDevice();
        String str = this.mDeviceAddress;
        if (str != null && (device == null || !str.equals(device.getAddress()))) {
            return false;
        }
        ScanRecord scanRecord = scanResult.getScanRecord();
        if (scanRecord == null && (this.mDeviceName != null || this.mServiceUuid != null || this.mManufacturerData != null || this.mServiceData != null || this.mServiceSolicitationUuid != null)) {
            return false;
        }
        String str2 = this.mDeviceName;
        if (str2 != null && scanRecord != null && !str2.equals(scanRecord.getDeviceName())) {
            return false;
        }
        ParcelUuid parcelUuid = this.mServiceUuid;
        if (parcelUuid != null && scanRecord != null && !matchesServiceUuids(parcelUuid, this.mServiceUuidMask, scanRecord.getServiceUuids())) {
            return false;
        }
        ParcelUuid parcelUuid2 = this.mServiceSolicitationUuid;
        if (parcelUuid2 != null && scanRecord != null && !matchesServiceSolicitationUuids(parcelUuid2, this.mServiceSolicitationUuidMask, scanRecord.getServiceSolicitationUuids())) {
            return false;
        }
        ParcelUuid parcelUuid3 = this.mServiceDataUuid;
        if (parcelUuid3 != null && scanRecord != null && !matchesPartialData(this.mServiceData, this.mServiceDataMask, scanRecord.getServiceData(parcelUuid3))) {
            return false;
        }
        int i = this.mManufacturerId;
        if (i < 0 || scanRecord == null || matchesPartialData(this.mManufacturerData, this.mManufacturerDataMask, scanRecord.getManufacturerSpecificData(i))) {
            return true;
        }
        return false;
    }

    public static boolean matchesServiceUuids(ParcelUuid uuid, ParcelUuid parcelUuidMask, List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }
        for (ParcelUuid parcelUuid : uuids) {
            if (matchesServiceUuid(uuid.getUuid(), parcelUuidMask == null ? null : parcelUuidMask.getUuid(), parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesServiceUuid(UUID uuid, UUID mask, UUID data) {
        return BitUtils.maskedEquals(data, uuid, mask);
    }

    private static boolean matchesServiceSolicitationUuids(ParcelUuid solicitationUuid, ParcelUuid parcelSolicitationUuidMask, List<ParcelUuid> solicitationUuids) {
        if (solicitationUuid == null) {
            return true;
        }
        if (solicitationUuids == null) {
            return false;
        }
        for (ParcelUuid parcelSolicitationUuid : solicitationUuids) {
            if (matchesServiceUuid(solicitationUuid.getUuid(), parcelSolicitationUuidMask == null ? null : parcelSolicitationUuidMask.getUuid(), parcelSolicitationUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesServiceSolicitationUuid(UUID solicitationUuid, UUID solicitationUuidMask, UUID data) {
        return BitUtils.maskedEquals(data, solicitationUuid, solicitationUuidMask);
    }

    private boolean matchesPartialData(byte[] data, byte[] dataMask, byte[] parsedData) {
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        if (dataMask == null) {
            for (int i = 0; i < data.length; i++) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (int i2 = 0; i2 < data.length; i2++) {
            if ((dataMask[i2] & parsedData[i2]) != (dataMask[i2] & data[i2])) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "BluetoothLeScanFilter [mDeviceName=" + this.mDeviceName + ", mDeviceAddress=" + this.mDeviceAddress + ", mUuid=" + this.mServiceUuid + ", mUuidMask=" + this.mServiceUuidMask + ", mServiceSolicitationUuid=" + this.mServiceSolicitationUuid + ", mServiceSolicitationUuidMask=" + this.mServiceSolicitationUuidMask + ", mServiceDataUuid=" + Objects.toString(this.mServiceDataUuid) + ", mServiceData=" + Arrays.toString(this.mServiceData) + ", mServiceDataMask=" + Arrays.toString(this.mServiceDataMask) + ", mManufacturerId=" + this.mManufacturerId + ", mManufacturerData=" + Arrays.toString(this.mManufacturerData) + ", mManufacturerDataMask=" + Arrays.toString(this.mManufacturerDataMask) + "]";
    }

    public int hashCode() {
        return Objects.hash(this.mDeviceName, this.mDeviceAddress, Integer.valueOf(this.mManufacturerId), Integer.valueOf(Arrays.hashCode(this.mManufacturerData)), Integer.valueOf(Arrays.hashCode(this.mManufacturerDataMask)), this.mServiceDataUuid, Integer.valueOf(Arrays.hashCode(this.mServiceData)), Integer.valueOf(Arrays.hashCode(this.mServiceDataMask)), this.mServiceUuid, this.mServiceUuidMask, this.mServiceSolicitationUuid, this.mServiceSolicitationUuidMask);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanFilter other = (ScanFilter) obj;
        if (!Objects.equals(this.mDeviceName, other.mDeviceName) || !Objects.equals(this.mDeviceAddress, other.mDeviceAddress) || this.mManufacturerId != other.mManufacturerId || !Objects.deepEquals(this.mManufacturerData, other.mManufacturerData) || !Objects.deepEquals(this.mManufacturerDataMask, other.mManufacturerDataMask) || !Objects.equals(this.mServiceDataUuid, other.mServiceDataUuid) || !Objects.deepEquals(this.mServiceData, other.mServiceData) || !Objects.deepEquals(this.mServiceDataMask, other.mServiceDataMask) || !Objects.equals(this.mServiceUuid, other.mServiceUuid) || !Objects.equals(this.mServiceUuidMask, other.mServiceUuidMask) || !Objects.equals(this.mServiceSolicitationUuid, other.mServiceSolicitationUuid) || !Objects.equals(this.mServiceSolicitationUuidMask, other.mServiceSolicitationUuidMask)) {
            return false;
        }
        return true;
    }

    public boolean isAllFieldsEmpty() {
        return EMPTY.equals(this);
    }

    public static final class Builder {
        private String mDeviceAddress;
        private String mDeviceName;
        private byte[] mManufacturerData;
        private byte[] mManufacturerDataMask;
        private int mManufacturerId = -1;
        private byte[] mServiceData;
        private byte[] mServiceDataMask;
        private ParcelUuid mServiceDataUuid;
        private ParcelUuid mServiceSolicitationUuid;
        private ParcelUuid mServiceSolicitationUuidMask;
        private ParcelUuid mServiceUuid;
        private ParcelUuid mUuidMask;

        public Builder setDeviceName(String deviceName) {
            this.mDeviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(String deviceAddress) {
            if (deviceAddress == null || BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                this.mDeviceAddress = deviceAddress;
                return this;
            }
            throw new IllegalArgumentException("invalid device address " + deviceAddress);
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid) {
            this.mServiceUuid = serviceUuid;
            this.mUuidMask = null;
            return this;
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid, ParcelUuid uuidMask) {
            if (this.mUuidMask == null || this.mServiceUuid != null) {
                this.mServiceUuid = serviceUuid;
                this.mUuidMask = uuidMask;
                return this;
            }
            throw new IllegalArgumentException("uuid is null while uuidMask is not null!");
        }

        public Builder setServiceSolicitationUuid(ParcelUuid serviceSolicitationUuid) {
            this.mServiceSolicitationUuid = serviceSolicitationUuid;
            if (serviceSolicitationUuid == null) {
                this.mServiceSolicitationUuidMask = null;
            }
            return this;
        }

        public Builder setServiceSolicitationUuid(ParcelUuid serviceSolicitationUuid, ParcelUuid solicitationUuidMask) {
            if (solicitationUuidMask == null || serviceSolicitationUuid != null) {
                this.mServiceSolicitationUuid = serviceSolicitationUuid;
                this.mServiceSolicitationUuidMask = solicitationUuidMask;
                return this;
            }
            throw new IllegalArgumentException("SolicitationUuid is null while SolicitationUuidMask is not null!");
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid != null) {
                this.mServiceDataUuid = serviceDataUuid;
                this.mServiceData = serviceData;
                this.mServiceDataMask = null;
                return this;
            }
            throw new IllegalArgumentException("serviceDataUuid is null");
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData, byte[] serviceDataMask) {
            if (serviceDataUuid != null) {
                byte[] bArr = this.mServiceDataMask;
                if (bArr != null) {
                    byte[] bArr2 = this.mServiceData;
                    if (bArr2 == null) {
                        throw new IllegalArgumentException("serviceData is null while serviceDataMask is not null");
                    } else if (bArr2.length != bArr.length) {
                        throw new IllegalArgumentException("size mismatch for service data and service data mask");
                    }
                }
                this.mServiceDataUuid = serviceDataUuid;
                this.mServiceData = serviceData;
                this.mServiceDataMask = serviceDataMask;
                return this;
            }
            throw new IllegalArgumentException("serviceDataUuid is null");
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData) {
            if (manufacturerData == null || manufacturerId >= 0) {
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = null;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask) {
            if (manufacturerData == null || manufacturerId >= 0) {
                byte[] bArr = this.mManufacturerDataMask;
                if (bArr != null) {
                    byte[] bArr2 = this.mManufacturerData;
                    if (bArr2 == null) {
                        throw new IllegalArgumentException("manufacturerData is null while manufacturerDataMask is not null");
                    } else if (bArr2.length != bArr.length) {
                        throw new IllegalArgumentException("size mismatch for manufacturerData and manufacturerDataMask");
                    }
                }
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = manufacturerDataMask;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public ScanFilter build() {
            return new ScanFilter(this.mDeviceName, this.mDeviceAddress, this.mServiceUuid, this.mUuidMask, this.mServiceSolicitationUuid, this.mServiceSolicitationUuidMask, this.mServiceDataUuid, this.mServiceData, this.mServiceDataMask, this.mManufacturerId, this.mManufacturerData, this.mManufacturerDataMask);
        }
    }
}
