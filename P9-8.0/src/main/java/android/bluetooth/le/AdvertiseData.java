package android.bluetooth.le;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdvertiseData implements Parcelable {
    public static final Creator<AdvertiseData> CREATOR = new Creator<AdvertiseData>() {
        public AdvertiseData[] newArray(int size) {
            return new AdvertiseData[size];
        }

        public AdvertiseData createFromParcel(Parcel in) {
            int i;
            Builder builder = new Builder();
            List<ParcelUuid> uuids = in.readArrayList(ParcelUuid.class.getClassLoader());
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    builder.addServiceUuid(uuid);
                }
            }
            int manufacturerSize = in.readInt();
            for (i = 0; i < manufacturerSize; i++) {
                int manufacturerId = in.readInt();
                if (in.readInt() == 1) {
                    byte[] manufacturerData = new byte[in.readInt()];
                    in.readByteArray(manufacturerData);
                    builder.addManufacturerData(manufacturerId, manufacturerData);
                }
            }
            int serviceDataSize = in.readInt();
            for (i = 0; i < serviceDataSize; i++) {
                ParcelUuid serviceDataUuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    byte[] serviceData = new byte[in.readInt()];
                    in.readByteArray(serviceData);
                    builder.addServiceData(serviceDataUuid, serviceData);
                }
            }
            builder.setIncludeTxPowerLevel(in.readByte() == (byte) 1);
            builder.setIncludeDeviceName(in.readByte() == (byte) 1);
            return builder.build();
        }
    };
    private final boolean mIncludeDeviceName;
    private final boolean mIncludeTxPowerLevel;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    private final List<ParcelUuid> mServiceUuids;

    public static final class Builder {
        private boolean mIncludeDeviceName;
        private boolean mIncludeTxPowerLevel;
        private SparseArray<byte[]> mManufacturerSpecificData = new SparseArray();
        private Map<ParcelUuid, byte[]> mServiceData = new ArrayMap();
        private List<ParcelUuid> mServiceUuids = new ArrayList();

        public Builder addServiceUuid(ParcelUuid serviceUuid) {
            if (serviceUuid == null) {
                throw new IllegalArgumentException("serivceUuids are null");
            }
            this.mServiceUuids.add(serviceUuid);
            return this;
        }

        public Builder addServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid == null || serviceData == null) {
                throw new IllegalArgumentException("serviceDataUuid or serviceDataUuid is null");
            }
            this.mServiceData.put(serviceDataUuid, serviceData);
            return this;
        }

        public Builder addManufacturerData(int manufacturerId, byte[] manufacturerSpecificData) {
            if (manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacturerId - " + manufacturerId);
            } else if (manufacturerSpecificData == null) {
                throw new IllegalArgumentException("manufacturerSpecificData is null");
            } else {
                this.mManufacturerSpecificData.put(manufacturerId, manufacturerSpecificData);
                return this;
            }
        }

        public Builder setIncludeTxPowerLevel(boolean includeTxPowerLevel) {
            this.mIncludeTxPowerLevel = includeTxPowerLevel;
            return this;
        }

        public Builder setIncludeDeviceName(boolean includeDeviceName) {
            this.mIncludeDeviceName = includeDeviceName;
            return this;
        }

        public AdvertiseData build() {
            return new AdvertiseData(this.mServiceUuids, this.mManufacturerSpecificData, this.mServiceData, this.mIncludeTxPowerLevel, this.mIncludeDeviceName, null);
        }
    }

    /* synthetic */ AdvertiseData(List serviceUuids, SparseArray manufacturerData, Map serviceData, boolean includeTxPowerLevel, boolean includeDeviceName, AdvertiseData -this5) {
        this(serviceUuids, manufacturerData, serviceData, includeTxPowerLevel, includeDeviceName);
    }

    private AdvertiseData(List<ParcelUuid> serviceUuids, SparseArray<byte[]> manufacturerData, Map<ParcelUuid, byte[]> serviceData, boolean includeTxPowerLevel, boolean includeDeviceName) {
        this.mServiceUuids = serviceUuids;
        this.mManufacturerSpecificData = manufacturerData;
        this.mServiceData = serviceData;
        this.mIncludeTxPowerLevel = includeTxPowerLevel;
        this.mIncludeDeviceName = includeDeviceName;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public SparseArray<byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public boolean getIncludeTxPowerLevel() {
        return this.mIncludeTxPowerLevel;
    }

    public boolean getIncludeDeviceName() {
        return this.mIncludeDeviceName;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mServiceUuids, this.mManufacturerSpecificData, this.mServiceData, Boolean.valueOf(this.mIncludeDeviceName), Boolean.valueOf(this.mIncludeTxPowerLevel)});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AdvertiseData other = (AdvertiseData) obj;
        if (!Objects.equals(this.mServiceUuids, other.mServiceUuids) || !BluetoothLeUtils.equals(this.mManufacturerSpecificData, other.mManufacturerSpecificData) || !BluetoothLeUtils.equals(this.mServiceData, other.mServiceData) || this.mIncludeDeviceName != other.mIncludeDeviceName) {
            z = false;
        } else if (this.mIncludeTxPowerLevel != other.mIncludeTxPowerLevel) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "AdvertiseData [mServiceUuids=" + this.mServiceUuids + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(this.mManufacturerSpecificData) + ", mServiceData=" + BluetoothLeUtils.toString(this.mServiceData) + ", mIncludeTxPowerLevel=" + this.mIncludeTxPowerLevel + ", mIncludeDeviceName=" + this.mIncludeDeviceName + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        byte[] data;
        int i;
        int i2 = 1;
        dest.writeList(this.mServiceUuids);
        dest.writeInt(this.mManufacturerSpecificData.size());
        for (int i3 = 0; i3 < this.mManufacturerSpecificData.size(); i3++) {
            dest.writeInt(this.mManufacturerSpecificData.keyAt(i3));
            data = (byte[]) this.mManufacturerSpecificData.valueAt(i3);
            if (data == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeInt(data.length);
                dest.writeByteArray(data);
            }
        }
        dest.writeInt(this.mServiceData.size());
        for (ParcelUuid uuid : this.mServiceData.keySet()) {
            dest.writeParcelable(uuid, flags);
            data = (byte[]) this.mServiceData.get(uuid);
            if (data == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeInt(data.length);
                dest.writeByteArray(data);
            }
        }
        if (getIncludeTxPowerLevel()) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (!getIncludeDeviceName()) {
            i2 = 0;
        }
        dest.writeByte((byte) i2);
    }
}
