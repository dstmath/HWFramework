package ohos.bluetooth.ble;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class BleScanResult implements Sequenceable {
    private int mAdvertiseFlag;
    private BlePeripheralDevice mBlePeripheralDevice;
    private boolean mIsConnectable;
    private Map<Integer, byte[]> mManufacturerDatas;
    private int mRssi;
    private Map<UUID, byte[]> mServiceDatas;
    private List<UUID> mServiceUuids;

    public boolean marshalling(Parcel parcel) {
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public BleScanResult(BlePeripheralDevice blePeripheralDevice, Map<Integer, byte[]> map, Map<UUID, byte[]> map2, List<UUID> list, int i) {
        this.mBlePeripheralDevice = blePeripheralDevice;
        this.mManufacturerDatas = map;
        this.mServiceDatas = map2;
        this.mServiceUuids = list;
        this.mAdvertiseFlag = i;
    }

    /* access modifiers changed from: package-private */
    public void setRssi(int i) {
        this.mRssi = i;
    }

    /* access modifiers changed from: package-private */
    public void setIsConnectable(boolean z) {
        this.mIsConnectable = z;
    }

    public BlePeripheralDevice getPeripheralDevice() {
        return this.mBlePeripheralDevice;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public boolean isConnectable() {
        return this.mIsConnectable;
    }

    public Map<Integer, byte[]> getManufacturerData() {
        return this.mManufacturerDatas;
    }

    public Map<UUID, byte[]> getServiceData() {
        return this.mServiceDatas;
    }

    public List<UUID> getServiceUuids() {
        return this.mServiceUuids;
    }

    public int getAdvertiseFlag() {
        return this.mAdvertiseFlag;
    }
}
