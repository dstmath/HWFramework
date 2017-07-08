package android.bluetooth.le;

import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.security.keymaster.KeymasterDefs;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScanRecord {
    private static final int DATA_TYPE_FLAGS = 1;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 9;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 8;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 255;
    private static final int DATA_TYPE_SERVICE_DATA = 22;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 7;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 6;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 3;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 2;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 5;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 4;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 10;
    private static final String TAG = "ScanRecord";
    private final int mAdvertiseFlags;
    private final byte[] mBytes;
    private final String mDeviceName;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    private final List<ParcelUuid> mServiceUuids;
    private final int mTxPowerLevel;

    public int getAdvertiseFlags() {
        return this.mAdvertiseFlags;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public SparseArray<byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public byte[] getManufacturerSpecificData(int manufacturerId) {
        return (byte[]) this.mManufacturerSpecificData.get(manufacturerId);
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceData(ParcelUuid serviceDataUuid) {
        if (serviceDataUuid == null) {
            return null;
        }
        return (byte[]) this.mServiceData.get(serviceDataUuid);
    }

    public int getTxPowerLevel() {
        return this.mTxPowerLevel;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public byte[] getBytes() {
        return this.mBytes;
    }

    private ScanRecord(List<ParcelUuid> serviceUuids, SparseArray<byte[]> manufacturerData, Map<ParcelUuid, byte[]> serviceData, int advertiseFlags, int txPowerLevel, String localName, byte[] bytes) {
        this.mServiceUuids = serviceUuids;
        this.mManufacturerSpecificData = manufacturerData;
        this.mServiceData = serviceData;
        this.mDeviceName = localName;
        this.mAdvertiseFlags = advertiseFlags;
        this.mTxPowerLevel = txPowerLevel;
        this.mBytes = bytes;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ScanRecord parseFromBytes(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }
        int advertiseFlag = -1;
        List serviceUuids = new ArrayList();
        String localName = null;
        int txPowerLevel = KeymasterDefs.KM_BIGNUM;
        SparseArray<byte[]> manufacturerData = new SparseArray();
        Map<ParcelUuid, byte[]> serviceData = new ArrayMap();
        int currentPos = 0;
        while (currentPos < scanRecord.length) {
            int currentPos2;
            try {
                currentPos2 = currentPos + DATA_TYPE_FLAGS;
                try {
                    int length = scanRecord[currentPos] & DATA_TYPE_MANUFACTURER_SPECIFIC_DATA;
                    if (length == 0) {
                        if (serviceUuids.isEmpty()) {
                            serviceUuids = null;
                        }
                        return new ScanRecord(serviceUuids, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName, scanRecord);
                    }
                    int dataLength = length - 1;
                    currentPos = currentPos2 + DATA_TYPE_FLAGS;
                    switch (scanRecord[currentPos2] & DATA_TYPE_MANUFACTURER_SPECIFIC_DATA) {
                        case DATA_TYPE_FLAGS /*1*/:
                            advertiseFlag = scanRecord[currentPos] & DATA_TYPE_MANUFACTURER_SPECIFIC_DATA;
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL /*2*/:
                        case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE /*3*/:
                            parseServiceUuid(scanRecord, currentPos, dataLength, DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, serviceUuids);
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL /*4*/:
                        case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE /*5*/:
                            parseServiceUuid(scanRecord, currentPos, dataLength, DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, serviceUuids);
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL /*6*/:
                        case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE /*7*/:
                            parseServiceUuid(scanRecord, currentPos, dataLength, 16, serviceUuids);
                            break;
                        case DATA_TYPE_LOCAL_NAME_SHORT /*8*/:
                        case DATA_TYPE_LOCAL_NAME_COMPLETE /*9*/:
                            localName = new String(extractBytes(scanRecord, currentPos, dataLength));
                            break;
                        case DATA_TYPE_TX_POWER_LEVEL /*10*/:
                            txPowerLevel = scanRecord[currentPos];
                            break;
                        case DATA_TYPE_SERVICE_DATA /*22*/:
                            serviceData.put(BluetoothUuid.parseUuidFrom(extractBytes(scanRecord, currentPos, DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL)), extractBytes(scanRecord, currentPos + DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, dataLength - 2));
                            break;
                        case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA /*255*/:
                            manufacturerData.put(((scanRecord[currentPos + DATA_TYPE_FLAGS] & DATA_TYPE_MANUFACTURER_SPECIFIC_DATA) << DATA_TYPE_LOCAL_NAME_SHORT) + (scanRecord[currentPos] & DATA_TYPE_MANUFACTURER_SPECIFIC_DATA), extractBytes(scanRecord, currentPos + DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, dataLength - 2));
                            break;
                        default:
                            break;
                    }
                    currentPos += dataLength;
                } catch (Exception e) {
                }
            } catch (Exception e2) {
                currentPos2 = currentPos;
            }
        }
        if (serviceUuids.isEmpty()) {
            serviceUuids = null;
        }
        return new ScanRecord(serviceUuids, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName, scanRecord);
    }

    public String toString() {
        return "ScanRecord [mAdvertiseFlags=" + this.mAdvertiseFlags + ", mServiceUuids=" + this.mServiceUuids + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(this.mManufacturerSpecificData) + ", mServiceData=" + BluetoothLeUtils.toString(this.mServiceData) + ", mTxPowerLevel=" + this.mTxPowerLevel + ", mDeviceName=" + this.mDeviceName + "]";
    }

    private static int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength, int uuidLength, List<ParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            serviceUuids.add(BluetoothUuid.parseUuidFrom(extractBytes(scanRecord, currentPos, uuidLength)));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }

    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }
}
