package com.huawei.nearbysdk.util;

import com.huawei.nearbysdk.closeRange.CloseRangeProtocol;
import com.huawei.nearbysdk.publishinfo.PublishDeviceInfo;
import java.nio.charset.Charset;
import java.util.HashMap;

public class UsbDiscoveryProtocol {
    private static final int BT_MAC_ADDRESS_LENGTH = 6;
    private static final int BT_MAC_ADDRESS_TAG = 4;
    private static final int BUSINESS_ID_MAX = 127;
    private static final int BUSINESS_LENGTH = 1;
    private static final int BUSINESS_TAG = 8;
    private static final int DEVICE_ID_LENGTH = 32;
    private static final int DEVICE_ID_TAG = 3;
    private static final int DEVICE_NAME_TAG = 5;
    private static final int HEAD_LENGTH = 16;
    private static final int HEAD_LENGTH_POS = 8;
    private static final int HEAD_SUB_TYPE_POS = 14;
    private static final int HEAD_TYPE_POS = 10;
    private static final int HEAD_VERSION_POS = 13;
    private static final int INVALID_VALUE = -1;
    private static final int IP_ADDRESS_LENGTH = 14;
    private static final int IP_ADDRESS_TAG = 6;
    private static final int IP_PORT_LENGTH = 4;
    private static final int IP_PORT_TAG = 7;
    private static final String MAGIC_STRING = "hwnearby";
    private static final int MODEL_ID_LENGTH = 3;
    private static final int MODEL_ID_TAG = 1;
    private static final int PORT_MAX = 65535;
    private static final int SUB_MODEL_ID_LENGTH = 1;
    private static final int SUB_MODEL_ID_TAG = 2;
    private static final int TAG_CAPACITY = 9;
    private static final int TLV_BODY_LENGTH = 240;
    private static final int USB_MODEL_ID_MAX = 8388607;
    private static final int USB_MODE_LENGTH = 1;
    private static final int USB_MODE_MAX = 127;
    private static final int USB_MODE_TAG = 0;
    private static final int USB_SUB_MODEL_ID_MAX = 127;
    private byte[] mHead = new byte[HEAD_LENGTH];
    private byte[] mTLVBody = new byte[TLV_BODY_LENGTH];
    private HashMap<Integer, TLVMetaData> mTLVByteStream = new HashMap<>(9);

    public UsbDiscoveryProtocol(int modelID) {
        System.arraycopy(MAGIC_STRING.getBytes(Charset.defaultCharset()), 0, this.mHead, 0, 8);
        this.mHead[8] = PublishDeviceInfo.TAG_SEQUENCENUM;
        this.mHead[HEAD_VERSION_POS] = 1;
        byte[] temp = new byte[3];
        temp[2] = (byte) (modelID & CloseRangeProtocol.INVALID_RSSI);
        temp[1] = (byte) ((modelID >> 8) & CloseRangeProtocol.INVALID_RSSI);
        temp[0] = (byte) ((modelID >> HEAD_LENGTH) & CloseRangeProtocol.INVALID_RSSI);
        this.mTLVByteStream.put(1, new TLVMetaData(1, 3, temp));
    }

    private void setHeadLength(int length) {
        this.mHead[8] = (byte) (length & CloseRangeProtocol.INVALID_RSSI);
        this.mHead[9] = (byte) ((length >> 8) & CloseRangeProtocol.INVALID_RSSI);
    }

    public void setHeadType(int type) {
        this.mHead[HEAD_TYPE_POS] = (byte) (type & CloseRangeProtocol.INVALID_RSSI);
        this.mHead[11] = (byte) ((type >> 8) & CloseRangeProtocol.INVALID_RSSI);
    }

    public void setHeadSubType(int subType) {
        this.mHead[14] = (byte) (subType & CloseRangeProtocol.INVALID_RSSI);
        this.mHead[15] = (byte) ((subType >> 8) & CloseRangeProtocol.INVALID_RSSI);
    }

    /* access modifiers changed from: private */
    public static class TLVMetaData {
        private int length;
        private int tag;
        private byte[] value;

        public TLVMetaData(int tag2, int length2, byte[] value2) {
            this.tag = tag2;
            this.length = length2;
            this.value = new byte[length2];
            System.arraycopy(value2, 0, this.value, 0, length2);
        }
    }

    public int setUsbMode(int mode) {
        if (mode > 127) {
            return -1;
        }
        this.mTLVByteStream.put(0, new TLVMetaData(0, 1, new byte[]{(byte) (mode & CloseRangeProtocol.INVALID_RSSI)}));
        return 0;
    }

    public int setSubModelID(int subModelID) {
        if (subModelID > 127) {
            return -1;
        }
        this.mTLVByteStream.put(2, new TLVMetaData(2, 1, new byte[]{(byte) (subModelID & CloseRangeProtocol.INVALID_RSSI)}));
        return 0;
    }

    public int setDeviceID(byte[] deviceID) {
        if (deviceID.length > DEVICE_ID_LENGTH) {
            return -1;
        }
        this.mTLVByteStream.put(3, new TLVMetaData(4, 6, deviceID));
        return 0;
    }

    public int setBTAddress(byte[] btMac) {
        if (btMac.length > 6) {
            return -1;
        }
        this.mTLVByteStream.put(4, new TLVMetaData(4, 6, btMac));
        return 0;
    }

    public int setDeviceName(String deviceName) {
        if (deviceName == null) {
            return -1;
        }
        this.mTLVByteStream.put(5, new TLVMetaData(5, deviceName.length(), deviceName.getBytes(Charset.defaultCharset())));
        return 0;
    }

    public int setIp(String iP) {
        if (iP.length() > 14) {
            return -1;
        }
        this.mTLVByteStream.put(6, new TLVMetaData(6, 14, iP.getBytes(Charset.defaultCharset())));
        return 0;
    }

    public int setPort(int port) {
        if (port < 0 || port > PORT_MAX) {
            return -1;
        }
        byte[] temp = new byte[4];
        temp[3] = (byte) ((port >> 0) & CloseRangeProtocol.INVALID_RSSI);
        temp[2] = (byte) ((port >> 8) & CloseRangeProtocol.INVALID_RSSI);
        temp[1] = (byte) ((port >> HEAD_LENGTH) & CloseRangeProtocol.INVALID_RSSI);
        temp[0] = (byte) ((port >> 24) & CloseRangeProtocol.INVALID_RSSI);
        this.mTLVByteStream.put(7, new TLVMetaData(7, 4, temp));
        return 0;
    }

    public int setBusinessData(int business) {
        if (business < 0 || business > 127) {
            return -1;
        }
        this.mTLVByteStream.put(8, new TLVMetaData(8, 1, new byte[]{(byte) (business & CloseRangeProtocol.INVALID_RSSI)}));
        return 0;
    }

    private int buildTLVData() {
        int curPos = 0;
        for (int tag = 0; tag < 9; tag++) {
            TLVMetaData item = this.mTLVByteStream.get(Integer.valueOf(tag));
            if (item != null) {
                int curPos2 = curPos + 1;
                this.mTLVBody[curPos] = (byte) item.tag;
                int curPos3 = curPos2 + 1;
                this.mTLVBody[curPos2] = (byte) item.length;
                System.arraycopy(item.value, 0, this.mTLVBody, curPos3, item.length);
                curPos = curPos3 + item.length;
            } else {
                curPos = curPos;
            }
        }
        return curPos;
    }

    public byte[] getUsbDiscoveryProtocol() {
        int tlvBodyLength = buildTLVData();
        setHeadLength(tlvBodyLength + HEAD_LENGTH);
        byte[] result = new byte[(tlvBodyLength + HEAD_LENGTH)];
        System.arraycopy(this.mHead, 0, result, 0, HEAD_LENGTH);
        System.arraycopy(this.mTLVBody, 0, result, HEAD_LENGTH, tlvBodyLength);
        return result;
    }
}
