package com.huawei.iconnect.wearable.config;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import com.huawei.iconnect.hwutil.Utils;
import java.util.Arrays;
import java.util.HashMap;

public class BluetoothDeviceData {
    private final String address;
    private final String deviceName;
    private final HashMap<Integer, byte[]> manufacturerSpecificData;
    private final int standard;
    private final ParcelUuid[] uuids;

    public HashMap<Integer, byte[]> getManufacturerSpecificData() {
        return this.manufacturerSpecificData;
    }

    public BluetoothDeviceData(HashMap<Integer, byte[]> manufacturerSpecificData, BluetoothDevice device, ParcelUuid[] huaweiScanUuid) {
        this.address = device.getAddress();
        this.deviceName = device.getName();
        this.standard = device.getType();
        if (huaweiScanUuid == null) {
            this.uuids = new ParcelUuid[0];
        } else {
            this.uuids = (ParcelUuid[]) huaweiScanUuid.clone();
        }
        this.manufacturerSpecificData = manufacturerSpecificData;
    }

    public BluetoothDeviceData(HashMap<Integer, byte[]> manufacturerSpecificData, String address, String deviceName, int standard, ParcelUuid[] uuids) {
        this.address = address;
        this.deviceName = deviceName;
        this.standard = standard;
        if (uuids == null) {
            this.uuids = new ParcelUuid[0];
        } else {
            this.uuids = (ParcelUuid[]) uuids.clone();
        }
        this.manufacturerSpecificData = manufacturerSpecificData;
    }

    public String toString() {
        return "BluetoothDeviceData{address='" + Utils.toMacSecureString(this.address) + '\'' + ", deviceName='" + this.deviceName + '\'' + ", standard=" + this.standard + ", uuids=" + Arrays.toString(this.uuids) + '}';
    }

    public String getAddress() {
        return this.address;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public int getStandard() {
        return this.standard;
    }

    public ParcelUuid[] getUuids() {
        if (this.uuids == null) {
            return new ParcelUuid[0];
        }
        return (ParcelUuid[]) this.uuids.clone();
    }
}
