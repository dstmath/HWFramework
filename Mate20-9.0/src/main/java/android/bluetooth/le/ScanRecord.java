package android.bluetooth.le;

import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ScanRecord {
    private static final int DATA_TYPE_FLAGS = 1;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 9;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 8;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 255;
    private static final int DATA_TYPE_SERVICE_DATA_128_BIT = 33;
    private static final int DATA_TYPE_SERVICE_DATA_16_BIT = 22;
    private static final int DATA_TYPE_SERVICE_DATA_32_BIT = 32;
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
        if (this.mManufacturerSpecificData == null) {
            return null;
        }
        return this.mManufacturerSpecificData.get(manufacturerId);
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceData(ParcelUuid serviceDataUuid) {
        if (serviceDataUuid == null || this.mServiceData == null) {
            return null;
        }
        return this.mServiceData.get(serviceDataUuid);
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

    public static ScanRecord parseFromBytes(byte[] scanRecord) {
        Map<ParcelUuid, byte[]> serviceData;
        byte[] bArr = scanRecord;
        if (bArr == null) {
            return null;
        }
        List<ParcelUuid> arrayList = new ArrayList<>();
        SparseArray<byte[]> manufacturerData = new SparseArray<>();
        Map<ParcelUuid, byte[]> serviceData2 = new ArrayMap<>();
        int advertiseFlag = -1;
        String localName = null;
        int txPowerLevel = Integer.MIN_VALUE;
        int serviceUuidLength = 0;
        while (true) {
            serviceData = serviceData2;
            try {
                if (serviceUuidLength < bArr.length) {
                    int currentPos = serviceUuidLength + 1;
                    try {
                        int length = bArr[serviceUuidLength] & 255;
                        if (length == 0) {
                            int i = currentPos;
                        } else {
                            int dataLength = length - 1;
                            int currentPos2 = currentPos + 1;
                            try {
                                int currentPos3 = bArr[currentPos] & 255;
                                if (currentPos3 != 22) {
                                    if (currentPos3 != 255) {
                                        switch (currentPos3) {
                                            case 1:
                                                advertiseFlag = bArr[currentPos2] & 255;
                                                continue;
                                            case 2:
                                            case 3:
                                                parseServiceUuid(bArr, currentPos2, dataLength, 2, arrayList);
                                                continue;
                                            case 4:
                                            case 5:
                                                parseServiceUuid(bArr, currentPos2, dataLength, 4, arrayList);
                                                continue;
                                            case 6:
                                            case 7:
                                                parseServiceUuid(bArr, currentPos2, dataLength, 16, arrayList);
                                                continue;
                                            case 8:
                                            case 9:
                                                localName = new String(extractBytes(bArr, currentPos2, dataLength));
                                                continue;
                                            case 10:
                                                txPowerLevel = bArr[currentPos2];
                                                continue;
                                            default:
                                                switch (currentPos3) {
                                                    case 32:
                                                    case 33:
                                                        break;
                                                    default:
                                                        continue;
                                                        continue;
                                                }
                                        }
                                    } else {
                                        manufacturerData.put(((bArr[currentPos2 + 1] & 255) << 8) + (255 & bArr[currentPos2]), extractBytes(bArr, currentPos2 + 2, dataLength - 2));
                                    }
                                    serviceUuidLength = currentPos2 + dataLength;
                                    serviceData2 = serviceData;
                                }
                                int serviceUuidLength2 = 2;
                                if (currentPos3 == 32) {
                                    serviceUuidLength2 = 4;
                                } else if (currentPos3 == 33) {
                                    serviceUuidLength2 = 16;
                                }
                                serviceData.put(BluetoothUuid.parseUuidFrom(extractBytes(bArr, currentPos2, serviceUuidLength2)), extractBytes(bArr, currentPos2 + serviceUuidLength2, dataLength - serviceUuidLength2));
                                serviceUuidLength = currentPos2 + dataLength;
                                serviceData2 = serviceData;
                            } catch (Exception e) {
                                ArrayList arrayList2 = arrayList;
                                int i2 = currentPos2;
                                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                                ScanRecord scanRecord2 = new ScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, bArr);
                                return scanRecord2;
                            }
                        }
                    } catch (Exception e2) {
                        ArrayList arrayList3 = arrayList;
                        int i3 = currentPos;
                        Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                        ScanRecord scanRecord22 = new ScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, bArr);
                        return scanRecord22;
                    }
                } else {
                    int i4 = serviceUuidLength;
                }
            } catch (Exception e3) {
                int i5 = serviceUuidLength;
                List<ParcelUuid> serviceUuids = arrayList;
                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                ScanRecord scanRecord222 = new ScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, bArr);
                return scanRecord222;
            }
        }
        try {
            try {
                ScanRecord scanRecord3 = new ScanRecord(arrayList.isEmpty() ? null : arrayList, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName, bArr);
                return scanRecord3;
            } catch (Exception e4) {
                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                ScanRecord scanRecord2222 = new ScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, bArr);
                return scanRecord2222;
            }
        } catch (Exception e5) {
            List<ParcelUuid> serviceUuids2 = arrayList;
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            ScanRecord scanRecord22222 = new ScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, bArr);
            return scanRecord22222;
        }
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
