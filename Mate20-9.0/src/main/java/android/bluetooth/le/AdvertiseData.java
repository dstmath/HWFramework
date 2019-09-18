package android.bluetooth.le;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdvertiseData implements Parcelable {
    public static final Parcelable.Creator<AdvertiseData> CREATOR = new Parcelable.Creator<AdvertiseData>() {
        public AdvertiseData[] newArray(int size) {
            return new AdvertiseData[size];
        }

        public AdvertiseData createFromParcel(Parcel in) {
            Builder builder = new Builder();
            Iterator<ParcelUuid> it = in.createTypedArrayList(ParcelUuid.CREATOR).iterator();
            while (it.hasNext()) {
                builder.addServiceUuid(it.next());
            }
            int manufacturerSize = in.readInt();
            boolean z = false;
            for (int i = 0; i < manufacturerSize; i++) {
                builder.addManufacturerData(in.readInt(), in.createByteArray());
            }
            int serviceDataSize = in.readInt();
            for (int i2 = 0; i2 < serviceDataSize; i2++) {
                builder.addServiceData((ParcelUuid) in.readTypedObject(ParcelUuid.CREATOR), in.createByteArray());
            }
            builder.setIncludeTxPowerLevel(in.readByte() == 1);
            if (in.readByte() == 1) {
                z = true;
            }
            builder.setIncludeDeviceName(z);
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
        private SparseArray<byte[]> mManufacturerSpecificData = new SparseArray<>();
        private Map<ParcelUuid, byte[]> mServiceData = new ArrayMap();
        private List<ParcelUuid> mServiceUuids = new ArrayList();

        public Builder addServiceUuid(ParcelUuid serviceUuid) {
            if (serviceUuid != null) {
                this.mServiceUuids.add(serviceUuid);
                return this;
            }
            throw new IllegalArgumentException("serivceUuids are null");
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
            } else if (manufacturerSpecificData != null) {
                this.mManufacturerSpecificData.put(manufacturerId, manufacturerSpecificData);
                return this;
            } else {
                throw new IllegalArgumentException("manufacturerSpecificData is null");
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
            AdvertiseData advertiseData = new AdvertiseData(this.mServiceUuids, this.mManufacturerSpecificData, this.mServiceData, this.mIncludeTxPowerLevel, this.mIncludeDeviceName);
            return advertiseData;
        }
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
        if (!Objects.equals(this.mServiceUuids, other.mServiceUuids) || !BluetoothLeUtils.equals(this.mManufacturerSpecificData, other.mManufacturerSpecificData) || !BluetoothLeUtils.equals(this.mServiceData, other.mServiceData) || this.mIncludeDeviceName != other.mIncludeDeviceName || this.mIncludeTxPowerLevel != other.mIncludeTxPowerLevel) {
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
        dest.writeTypedArray((ParcelUuid[]) this.mServiceUuids.toArray(new ParcelUuid[this.mServiceUuids.size()]), flags);
        dest.writeInt(this.mManufacturerSpecificData.size());
        for (int i = 0; i < this.mManufacturerSpecificData.size(); i++) {
            dest.writeInt(this.mManufacturerSpecificData.keyAt(i));
            dest.writeByteArray(this.mManufacturerSpecificData.valueAt(i));
        }
        dest.writeInt(this.mServiceData.size());
        for (ParcelUuid uuid : this.mServiceData.keySet()) {
            dest.writeTypedObject(uuid, flags);
            dest.writeByteArray(this.mServiceData.get(uuid));
        }
        dest.writeByte(getIncludeTxPowerLevel() ? (byte) 1 : 0);
        dest.writeByte(getIncludeDeviceName() ? (byte) 1 : 0);
    }
}
