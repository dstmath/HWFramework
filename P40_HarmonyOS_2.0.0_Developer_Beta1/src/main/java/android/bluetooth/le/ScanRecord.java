package android.bluetooth.le;

import android.annotation.UnsupportedAppUsage;
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
    private static final int DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT = 21;
    private static final int DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT = 20;
    private static final int DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT = 31;
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
    private final List<ParcelUuid> mServiceSolicitationUuids;
    private final List<ParcelUuid> mServiceUuids;
    private final int mTxPowerLevel;

    public int getAdvertiseFlags() {
        return this.mAdvertiseFlags;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public List<ParcelUuid> getServiceSolicitationUuids() {
        return this.mServiceSolicitationUuids;
    }

    public SparseArray<byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public byte[] getManufacturerSpecificData(int manufacturerId) {
        SparseArray<byte[]> sparseArray = this.mManufacturerSpecificData;
        if (sparseArray == null) {
            return null;
        }
        return sparseArray.get(manufacturerId);
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceData(ParcelUuid serviceDataUuid) {
        Map<ParcelUuid, byte[]> map;
        if (serviceDataUuid == null || (map = this.mServiceData) == null) {
            return null;
        }
        return map.get(serviceDataUuid);
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

    private ScanRecord(List<ParcelUuid> serviceUuids, List<ParcelUuid> serviceSolicitationUuids, SparseArray<byte[]> manufacturerData, Map<ParcelUuid, byte[]> serviceData, int advertiseFlags, int txPowerLevel, String localName, byte[] bytes) {
        this.mServiceSolicitationUuids = serviceSolicitationUuids;
        this.mServiceUuids = serviceUuids;
        this.mManufacturerSpecificData = manufacturerData;
        this.mServiceData = serviceData;
        this.mDeviceName = localName;
        this.mAdvertiseFlags = advertiseFlags;
        this.mTxPowerLevel = txPowerLevel;
        this.mBytes = bytes;
    }

    @UnsupportedAppUsage
    public static ScanRecord parseFromBytes(byte[] scanRecord) {
        List<ParcelUuid> serviceUuids;
        if (scanRecord == null) {
            return null;
        }
        List<ParcelUuid> serviceUuids2 = new ArrayList<>();
        List<ParcelUuid> serviceSolicitationUuids = new ArrayList<>();
        SparseArray<byte[]> manufacturerData = new SparseArray<>();
        Map<ParcelUuid, byte[]> serviceData = new ArrayMap<>();
        int advertiseFlag = -1;
        String localName = null;
        int txPowerLevel = Integer.MIN_VALUE;
        int currentPos = 0;
        while (true) {
            try {
                if (currentPos < scanRecord.length) {
                    int currentPos2 = currentPos + 1;
                    try {
                        int length = scanRecord[currentPos] & 255;
                        if (length != 0) {
                            int dataLength = length - 1;
                            int currentPos3 = currentPos2 + 1;
                            try {
                                int fieldType = scanRecord[currentPos2] & 255;
                                if (fieldType != 255) {
                                    switch (fieldType) {
                                        case 1:
                                            advertiseFlag = scanRecord[currentPos3] & 255;
                                            continue;
                                        case 2:
                                        case 3:
                                            parseServiceUuid(scanRecord, currentPos3, dataLength, 2, serviceUuids2);
                                            continue;
                                        case 4:
                                        case 5:
                                            parseServiceUuid(scanRecord, currentPos3, dataLength, 4, serviceUuids2);
                                            continue;
                                        case 6:
                                        case 7:
                                            parseServiceUuid(scanRecord, currentPos3, dataLength, 16, serviceUuids2);
                                            continue;
                                        case 8:
                                        case 9:
                                            localName = new String(extractBytes(scanRecord, currentPos3, dataLength));
                                            continue;
                                        case 10:
                                            txPowerLevel = scanRecord[currentPos3];
                                            continue;
                                        default:
                                            switch (fieldType) {
                                                case 20:
                                                    parseServiceSolicitationUuid(scanRecord, currentPos3, dataLength, 2, serviceSolicitationUuids);
                                                    break;
                                                case 21:
                                                    parseServiceSolicitationUuid(scanRecord, currentPos3, dataLength, 16, serviceSolicitationUuids);
                                                    break;
                                                default:
                                                    switch (fieldType) {
                                                        case 31:
                                                            parseServiceSolicitationUuid(scanRecord, currentPos3, dataLength, 4, serviceSolicitationUuids);
                                                            break;
                                                        case 32:
                                                        case 33:
                                                            break;
                                                        default:
                                                            continue;
                                                    }
                                                case 22:
                                                    int serviceUuidLength = 2;
                                                    if (fieldType == 32) {
                                                        serviceUuidLength = 4;
                                                    } else if (fieldType == 33) {
                                                        serviceUuidLength = 16;
                                                    }
                                                    serviceData.put(BluetoothUuid.parseUuidFrom(extractBytes(scanRecord, currentPos3, serviceUuidLength)), extractBytes(scanRecord, currentPos3 + serviceUuidLength, dataLength - serviceUuidLength));
                                                    break;
                                            }
                                    }
                                } else {
                                    manufacturerData.put(((scanRecord[currentPos3 + 1] & 255) << 8) + (255 & scanRecord[currentPos3]), extractBytes(scanRecord, currentPos3 + 2, dataLength - 2));
                                }
                                currentPos = currentPos3 + dataLength;
                            } catch (Exception e) {
                                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                                return new ScanRecord(null, null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
                            }
                        }
                    } catch (Exception e2) {
                        Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                        return new ScanRecord(null, null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
                    }
                }
            } catch (Exception e3) {
                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
                return new ScanRecord(null, null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
            }
        }
        try {
            if (serviceUuids2.isEmpty()) {
                serviceUuids = null;
            } else {
                serviceUuids = serviceUuids2;
            }
        } catch (Exception e4) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            return new ScanRecord(null, null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
        }
        try {
            return new ScanRecord(serviceUuids, serviceSolicitationUuids, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName, scanRecord);
        } catch (Exception e5) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            return new ScanRecord(null, null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
        }
    }

    public String toString() {
        return "ScanRecord [mAdvertiseFlags=" + this.mAdvertiseFlags + ", mServiceUuids=" + this.mServiceUuids + ", mServiceSolicitationUuids=" + this.mServiceSolicitationUuids + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(this.mManufacturerSpecificData) + ", mServiceData=" + BluetoothLeUtils.toString(this.mServiceData) + ", mTxPowerLevel=" + this.mTxPowerLevel + ", mDeviceName=" + this.mDeviceName + "]";
    }

    private static int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength, int uuidLength, List<ParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            serviceUuids.add(BluetoothUuid.parseUuidFrom(extractBytes(scanRecord, currentPos, uuidLength)));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }

    private static int parseServiceSolicitationUuid(byte[] scanRecord, int currentPos, int dataLength, int uuidLength, List<ParcelUuid> serviceSolicitationUuids) {
        while (dataLength > 0) {
            serviceSolicitationUuids.add(BluetoothUuid.parseUuidFrom(extractBytes(scanRecord, currentPos, uuidLength)));
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
