package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.utils.LightweightMap;
import ohos.utils.Parcel;
import ohos.utils.PlainArray;
import ohos.utils.SequenceUuid;
import ohos.utils.Sequenceable;

public final class BleAdvertiseData implements Sequenceable {
    private PlainArray<byte[]> mManufacturerData;
    private Map<SequenceUuid, byte[]> mServiceData;
    private List<SequenceUuid> mServiceUuids;

    private BleAdvertiseData(List<SequenceUuid> list, PlainArray<byte[]> plainArray, Map<SequenceUuid, byte[]> map) {
        this.mServiceUuids = list;
        this.mManufacturerData = plainArray;
        this.mServiceData = map;
    }

    BleAdvertiseData() {
    }

    public List<SequenceUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public Map<SequenceUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public PlainArray<byte[]> getManufacturerData() {
        return this.mManufacturerData;
    }

    public static final class Builder {
        private PlainArray<byte[]> mManufacturerData = new PlainArray<>();
        private Map<SequenceUuid, byte[]> mServiceData = new LightweightMap();
        private List<SequenceUuid> mServiceUuids = new ArrayList();

        public Builder addServiceUuid(SequenceUuid sequenceUuid) {
            if (sequenceUuid != null) {
                this.mServiceUuids.add(sequenceUuid);
                return this;
            }
            throw new IllegalArgumentException("serviceUuid is null");
        }

        public Builder addServiceData(SequenceUuid sequenceUuid, byte[] bArr) {
            if (sequenceUuid == null) {
                throw new IllegalArgumentException("uuid is null");
            } else if (bArr != null) {
                this.mServiceData.put(sequenceUuid, bArr);
                return this;
            } else {
                throw new IllegalArgumentException("serviceData is null");
            }
        }

        public Builder addManufacturerData(int i, byte[] bArr) {
            if (i < 0) {
                throw new IllegalArgumentException("invalid manufacturerId - " + i);
            } else if (bArr != null) {
                this.mManufacturerData.put(i, bArr);
                return this;
            } else {
                throw new IllegalArgumentException("data is null");
            }
        }

        public BleAdvertiseData build() {
            return new BleAdvertiseData(this.mServiceUuids, this.mManufacturerData, this.mServiceData);
        }
    }

    public boolean marshalling(Parcel parcel) {
        List<SequenceUuid> list = this.mServiceUuids;
        if (list == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(list.size());
            for (SequenceUuid sequenceUuid : this.mServiceUuids) {
                if (sequenceUuid != null) {
                    parcel.writeInt(1);
                    sequenceUuid.marshalling(parcel);
                } else {
                    parcel.writeInt(0);
                }
            }
        }
        parcel.writeInt(this.mManufacturerData.size());
        for (int i = 0; i < this.mManufacturerData.size(); i++) {
            parcel.writeInt(this.mManufacturerData.keyAt(i));
            parcel.writeByteArray((byte[]) this.mManufacturerData.valueAt(i));
        }
        parcel.writeInt(this.mServiceData.size());
        for (SequenceUuid sequenceUuid2 : this.mServiceData.keySet()) {
            if (sequenceUuid2 != null) {
                parcel.writeInt(1);
                sequenceUuid2.marshalling(parcel);
            } else {
                parcel.writeInt(0);
            }
            parcel.writeByteArray(this.mServiceData.get(sequenceUuid2));
        }
        parcel.writeInt(0);
        parcel.writeInt(0);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        SequenceUuid sequenceUuid;
        int readInt = parcel.readInt();
        if (readInt >= 0) {
            this.mServiceUuids = new ArrayList(readInt);
        } else {
            this.mServiceUuids = new ArrayList(0);
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.readInt() != 0) {
                SequenceUuid sequenceUuid2 = new SequenceUuid();
                sequenceUuid2.unmarshalling(parcel);
                this.mServiceUuids.add(sequenceUuid2);
            } else {
                this.mServiceUuids.add(null);
            }
        }
        int readInt2 = parcel.readInt();
        if (readInt2 >= 0) {
            this.mManufacturerData = new PlainArray<>(readInt2);
        } else {
            this.mManufacturerData = new PlainArray<>(0);
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.mManufacturerData.put(parcel.readInt(), parcel.readByteArray());
        }
        int readInt3 = parcel.readInt();
        if (readInt3 >= 0) {
            this.mServiceData = new LightweightMap(readInt3);
        } else {
            this.mServiceData = new LightweightMap(0);
        }
        for (int i3 = 0; i3 < readInt3; i3++) {
            if (parcel.readInt() != 0) {
                sequenceUuid = new SequenceUuid();
                sequenceUuid.unmarshalling(parcel);
            } else {
                sequenceUuid = null;
            }
            this.mServiceData.put(sequenceUuid, parcel.readByteArray());
        }
        parcel.readInt();
        parcel.readInt();
        return true;
    }
}
