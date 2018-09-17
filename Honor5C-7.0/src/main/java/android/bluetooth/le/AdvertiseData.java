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
    public static final Creator<AdvertiseData> CREATOR = null;
    private final boolean mIncludeDeviceName;
    private final boolean mIncludeTxPowerLevel;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    private final List<ParcelUuid> mServiceUuids;

    public static final class Builder {
        private boolean mIncludeDeviceName;
        private boolean mIncludeTxPowerLevel;
        private SparseArray<byte[]> mManufacturerSpecificData;
        private Map<ParcelUuid, byte[]> mServiceData;
        private List<ParcelUuid> mServiceUuids;

        public Builder() {
            this.mServiceUuids = new ArrayList();
            this.mManufacturerSpecificData = new SparseArray();
            this.mServiceData = new ArrayMap();
        }

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
            return new AdvertiseData(this.mManufacturerSpecificData, this.mServiceData, this.mIncludeTxPowerLevel, this.mIncludeDeviceName, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.le.AdvertiseData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.le.AdvertiseData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.AdvertiseData.<clinit>():void");
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
        int i = 1;
        dest.writeList(this.mServiceUuids);
        dest.writeInt(this.mManufacturerSpecificData.size());
        for (int i2 = 0; i2 < this.mManufacturerSpecificData.size(); i2++) {
            dest.writeInt(this.mManufacturerSpecificData.keyAt(i2));
            byte[] data = (byte[]) this.mManufacturerSpecificData.valueAt(i2);
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
        dest.writeByte((byte) (getIncludeTxPowerLevel() ? 1 : 0));
        if (!getIncludeDeviceName()) {
            i = 0;
        }
        dest.writeByte((byte) i);
    }
}
