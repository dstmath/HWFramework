package com.huawei.nearbysdk.closeRange;

import android.os.ParcelUuid;
import android.util.SparseArray;
import com.huawei.nearbysdk.BuildConfig;
import com.huawei.nearbysdk.HwLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public enum CloseRangeProtocol {
    CloseRangeBusinessId(1, 2, true),
    ReferenceRssi(2, 1, false),
    ModelId(3, 3, false),
    SubModelId(4, 1, false),
    DeviceId(5, 2, false),
    ConnectedDevice(6, 1, false),
    PairedDevice(7, 1, false),
    MaxConnectNumber(8, 1, false),
    MaxPairNumber(9, 1, false),
    DualModeDeviceKey(10, 2, false),
    TotalBattery(11, 1, false),
    LeftEarBattery(12, 1, false),
    RightEarBattery(13, 1, false),
    ChargerBattery(14, 1, false),
    BtFeature(15, 1, false),
    SequenceNumber(16, 1, false),
    AdvPower(17, 1, false),
    NewModelId(18, 4, false);
    
    private static final int BUSINESS_CLOSERANGE_BIT = 0;
    private static final int BUSINESS_RECONNECT_BIT = 1;
    private static final int BYTE_MASK = 255;
    private static final int CUSTOM_FIELD = 255;
    public static final int INVALID_RSSI = 255;
    private static final int INVALID_VALUE = -255;
    public static final ParcelUuid PARCEL_UUID_CLOSERANGE = new ParcelUuid(UUID_NEARBY_CLOSERANGE);
    public static final int RSSI_LENGTH = 1;
    private static final String STRING_UUID_NEARBY_CLOSERANGE = "0000FDEE-0000-1000-8000-00805F9B34FB";
    private static final String TAG = "CloseRangeProtocol";
    private static final UUID UUID_NEARBY_CLOSERANGE = UUID.fromString(STRING_UUID_NEARBY_CLOSERANGE);
    private static final SparseArray<CloseRangeProtocol> lookupMap = new SparseArray<>();
    private static final LinkedList<CloseRangeProtocol> mustList = new LinkedList<>();
    private boolean isMust;
    private int type;
    private int valueLength;

    static {
        CloseRangeProtocol[] values = values();
        for (CloseRangeProtocol value : values) {
            lookupMap.put(value.type, value);
            if (value.isMust) {
                mustList.add(value);
            }
        }
    }

    private CloseRangeProtocol(int type2, int valueLength2, boolean isMust2) {
        this.type = type2;
        this.valueLength = valueLength2;
        this.isMust = isMust2;
    }

    public static SparseArray<byte[]> parseCloseRangeServiceData(byte[] serviceData) {
        if (serviceData == null) {
            HwLog.e(TAG, "Service data is null");
            return new SparseArray<>();
        }
        SparseArray<byte[]> resultMap = new SparseArray<>();
        int length = serviceData.length;
        int currentIndex = 0;
        while (currentIndex < length) {
            CloseRangeProtocol protocol = lookupMap.get(serviceData[currentIndex] & 255);
            if (protocol == null) {
                break;
            }
            int startIndex = currentIndex + 1;
            int endIndex = startIndex + protocol.getValueLength();
            if (endIndex > length) {
                HwLog.e(TAG, "Error service data, no such length");
                return new SparseArray<>();
            }
            int type2 = protocol.getType();
            byte[] curData = Arrays.copyOfRange(serviceData, startIndex, endIndex);
            if (resultMap.get(type2) == null) {
                resultMap.put(type2, curData);
            } else if (protocol != DeviceId) {
                HwLog.e(TAG, "Duplicate");
                return new SparseArray<>();
            } else {
                byte[] orgData = resultMap.get(protocol.getType());
                byte[] fullData = new byte[(orgData.length + curData.length)];
                System.arraycopy(orgData, 0, fullData, 0, orgData.length);
                System.arraycopy(curData, 0, fullData, orgData.length, curData.length);
                resultMap.put(type2, fullData);
            }
            currentIndex = endIndex;
        }
        if (currentIndex < length) {
            parseCustomField(currentIndex, length, serviceData, resultMap);
        }
        Iterator<CloseRangeProtocol> it = mustList.iterator();
        while (it.hasNext()) {
            if (resultMap.get(it.next().getType()) == null) {
                HwLog.e(TAG, "Type is null");
                return new SparseArray<>();
            }
        }
        return resultMap;
    }

    public int getType() {
        return this.type;
    }

    public int getValueLength() {
        return this.valueLength;
    }

    public static boolean isCloseRangeEnabled(SparseArray<byte[]> nearbyArray) {
        boolean z = true;
        byte[] data = getBusinessData(nearbyArray);
        if (data == null) {
            return false;
        }
        if (CloseRangeBusinessType.fromTag(data[0]) != CloseRangeBusinessType.iConnect) {
            HwLog.i(TAG, "not iConnect, isCloseRangeEnabled return false");
            return false;
        }
        if (getBit(data[1], 0) <= 0) {
            z = false;
        }
        return z;
    }

    private static boolean isInvalidData(int type2, byte[] data) {
        return data == null || data.length != lookupMap.get(type2).getValueLength();
    }

    public static byte[] getBusinessData(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        int type2 = CloseRangeBusinessId.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return data;
        }
        HwLog.i(TAG, "invalid business data");
        return null;
    }

    private static int getBit(byte byteData, int position) {
        return (byteData >> position) & 1;
    }

    public static boolean isReconnectEnabled(SparseArray<byte[]> nearbyArray) {
        boolean z = true;
        byte[] data = getBusinessData(nearbyArray);
        if (data == null) {
            return false;
        }
        if (CloseRangeBusinessType.fromTag(data[0]) != CloseRangeBusinessType.iConnect) {
            HwLog.i(TAG, "reconnect=false");
            return false;
        }
        if (getBit(data[1], 1) <= 0) {
            z = false;
        }
        return z;
    }

    public static String getModelId(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        int type2 = ModelId.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return byteArrayToHexStr(data);
        }
        HwLog.i(TAG, "invalid model id");
        return null;
    }

    public static String getNewModelId(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        int type2 = NewModelId.getType();
        byte[] data = nearbyArray.get(type2);
        if (isInvalidData(type2, data)) {
            HwLog.i(TAG, "invalid new model id");
            return null;
        }
        String newModelId = BuildConfig.FLAVOR;
        for (int i = 0; i < data.length; i++) {
            newModelId = newModelId + ((char) data[i]);
        }
        return "00" + newModelId;
    }

    public static String getSubModelId(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        int type2 = SubModelId.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return byteArrayToHexStr(data);
        }
        HwLog.i(TAG, "invalid sub model id");
        return null;
    }

    private static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int length = byteArray.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", Byte.valueOf(byteArray[i])));
        }
        return sb.toString();
    }

    public static int getReferenceRssi(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        int type2 = ReferenceRssi.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return data[0];
        }
        return INVALID_VALUE;
    }

    public static String getDualModeDeviceKey(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        int type2 = DualModeDeviceKey.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return byteArrayToHexStr(data);
        }
        HwLog.i(TAG, "invalid dual model key");
        return null;
    }

    public static List<String> getDeviceId(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        byte[] data = nearbyArray.get(DeviceId.getType());
        int length = DeviceId.getValueLength();
        if (data == null || data.length == 0 || data.length % length != 0) {
            HwLog.d(TAG, "invalid paired device id");
            return null;
        }
        String s = byteArrayToHexStr(data);
        if (s == null) {
            HwLog.d(TAG, "invalid paired device id");
            return null;
        }
        int strLength = length * 2;
        return getStrList(s, strLength, s.length() / strLength);
    }

    private static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(substring(inputString, index * length, (index + 1) * length));
        }
        return list;
    }

    private static String substring(String str, int f, int t) {
        if (f > str.length()) {
            return null;
        }
        if (t > str.length()) {
            return str.substring(f, str.length());
        }
        return str.substring(f, t);
    }

    public static int getConnectedDevice(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        byte[] data = nearbyArray.get(ConnectedDevice.getType());
        if (data != null) {
            return toInt(data);
        }
        return INVALID_VALUE;
    }

    public static int getPairedDevice(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        byte[] data = nearbyArray.get(PairedDevice.getType());
        if (data != null) {
            return toInt(data);
        }
        return INVALID_VALUE;
    }

    public static int getMaxConnectNumber(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        byte[] data = nearbyArray.get(MaxConnectNumber.getType());
        if (data != null) {
            return toInt(data);
        }
        return INVALID_VALUE;
    }

    public static int getMaxPairNumber(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        byte[] data = nearbyArray.get(MaxPairNumber.getType());
        if (data != null) {
            return toInt(data);
        }
        return INVALID_VALUE;
    }

    public static class BatteryStatus {
        private int batteryLevel;
        private boolean isCharge;

        public static BatteryStatus build(byte[] data) {
            boolean z;
            if (data.length == 0) {
                return null;
            }
            BatteryStatus result = new BatteryStatus();
            if ((data[0] & 128) != 0) {
                z = true;
            } else {
                z = false;
            }
            result.isCharge = z;
            result.batteryLevel = data[0] & Byte.MAX_VALUE & CloseRangeProtocol.INVALID_RSSI;
            return result;
        }

        public int getBatteryLevel() {
            return this.batteryLevel;
        }

        public boolean isCharge() {
            return this.isCharge;
        }
    }

    public static BatteryStatus getTotalBattery(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        byte[] data = nearbyArray.get(TotalBattery.getType());
        if (data != null) {
            return BatteryStatus.build(data);
        }
        return null;
    }

    public static BatteryStatus getLeftBattery(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        byte[] data = nearbyArray.get(LeftEarBattery.getType());
        if (data != null) {
            return BatteryStatus.build(data);
        }
        return null;
    }

    public static BatteryStatus getRightBattery(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        byte[] data = nearbyArray.get(RightEarBattery.getType());
        if (data != null) {
            return BatteryStatus.build(data);
        }
        return null;
    }

    public static BatteryStatus getChargerBattery(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return null;
        }
        byte[] data = nearbyArray.get(ChargerBattery.getType());
        if (data != null) {
            return BatteryStatus.build(data);
        }
        return null;
    }

    public static int getSequenceNumber(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.i(TAG, "empty array");
            return INVALID_VALUE;
        }
        int type2 = SequenceNumber.getType();
        byte[] data = nearbyArray.get(SequenceNumber.getType());
        if (!isInvalidData(type2, data)) {
            return toInt(data);
        }
        HwLog.i(TAG, "invalid SequenceNumber data");
        return INVALID_VALUE;
    }

    public static int getAdvPower(SparseArray<byte[]> nearbyArray) {
        if (nearbyArray == null) {
            HwLog.d(TAG, "empty array");
            return INVALID_RSSI;
        }
        int type2 = AdvPower.getType();
        byte[] data = nearbyArray.get(type2);
        if (!isInvalidData(type2, data)) {
            return data[0];
        }
        HwLog.d(TAG, "invalid data ");
        return INVALID_RSSI;
    }

    private static int toInt(byte[] bRefArr) {
        int res = 0;
        for (int i = 0; i < bRefArr.length; i++) {
            res += (bRefArr[i] & 255) << (i * 8);
        }
        return res;
    }

    private static void parseCustomField(int currentIndex, int length, byte[] serviceData, SparseArray<byte[]> resultMap) {
        int protocolType = serviceData[currentIndex] & 255;
        if (protocolType == 255) {
            resultMap.put(protocolType, Arrays.copyOfRange(serviceData, currentIndex + 1, length));
        }
    }
}
