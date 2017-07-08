package com.vzw.nfc.dos;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class VzwTlv {
    private byte[] mRawData;
    private int mTag;
    private int mValueIndex;
    private int mValueLength;

    public VzwTlv(byte[] rawData, int tag, int valueIndex, int valueLength) {
        this.mRawData = null;
        this.mTag = 0;
        this.mValueIndex = 0;
        this.mValueLength = 0;
        this.mRawData = rawData;
        this.mTag = tag;
        this.mValueIndex = valueIndex;
        this.mValueLength = valueLength;
    }

    public static VzwTlv parse(byte[] data, int startIndex) throws DoParserException {
        if (data == null || data.length == 0) {
            throw new DoParserException("No data given!");
        }
        int curIndex = startIndex;
        if (startIndex < data.length) {
            curIndex = startIndex + 1;
            int tag = data[startIndex] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            if (curIndex < data.length) {
                return new VzwTlv(data, tag, curIndex + 1, data[curIndex] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
            }
            throw new DoParserException("Index " + curIndex + " out of range! [0..[" + data.length);
        }
        throw new DoParserException("Index out of bound");
    }

    public void translate() throws DoParserException {
    }

    public int getTag() {
        return this.mTag;
    }

    public int getValueIndex() {
        return this.mValueIndex;
    }

    public byte[] getValue() {
        if (this.mRawData == null || this.mValueLength == 0 || this.mValueIndex < 0 || this.mValueIndex > this.mRawData.length || this.mValueIndex + this.mValueLength > this.mRawData.length) {
            return null;
        }
        byte[] data = new byte[this.mValueLength];
        System.arraycopy(this.mRawData, this.mValueIndex, data, 0, this.mValueLength);
        return data;
    }

    protected byte[] getRawData() {
        return this.mRawData;
    }

    public int getValueLength() {
        return this.mValueLength;
    }

    public static void encodeLength(int length, ByteArrayOutputStream stream) {
        stream.write(length & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof VzwTlv)) {
            return false;
        }
        VzwTlv berTlv = (VzwTlv) obj;
        boolean equals = this.mTag == berTlv.mTag;
        if (!equals) {
            return equals;
        }
        byte[] test1 = getValue();
        byte[] test2 = berTlv.getValue();
        if (test1 != null) {
            return equals & Arrays.equals(test1, test2);
        }
        return (test1 == null && test2 == null) ? equals : equals;
    }

    public int hashCode() {
        return (((((Arrays.hashCode(this.mRawData) * 31) + this.mTag) * 31) + this.mValueIndex) * 31) + this.mValueLength;
    }
}
