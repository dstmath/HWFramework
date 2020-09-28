package com.huawei.coauth.tlv;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class TlvWrapper {
    private static final int BYTE_LEN = 1;
    private static final String DEFAULT_ENCODE_TYPE = "UTF-8";
    private static final ByteOrder DEFAULT_ORDER = ByteOrder.BIG_ENDIAN;
    private static final int DOUBLE_LEN = 8;
    private static final int FLOAT_LEN = 4;
    private static final int INT_LEN = 4;
    private static final int LONG_LEN = 8;
    private static final int SHORT_LEN = 2;
    private static final int TLV_LENGTH_LEN = 4;
    private static final int TLV_TYPE_LEN = 4;
    private static final String TRANS_NOT_MATCH_EXCEPTIN_MSG = "Data length in TLV does not match the length of param";
    private static final String TRANS_OVER_FLOW_EXCEPTION_MSG = "Overflow of data to be converted";
    private List<TlvBase> tlvList = new ArrayList();
    private int totalByteLen = 0;

    public List<TlvBase> getTlvList() {
        return this.tlvList;
    }

    public void setTlvList(List<TlvBase> tlvList2) {
        this.tlvList = tlvList2;
    }

    public synchronized void appendByte(int type, byte value) {
        appendBytes(type, new byte[]{value});
    }

    public synchronized void appendBytes(int type, byte[] value) {
        int length;
        TlvBase tlv;
        if (value != null) {
            if (value.length != 0) {
                tlv = new TlvBase(type, value.length, value);
                length = value.length;
                this.tlvList.add(tlv);
                this.totalByteLen += length + 8;
            }
        }
        tlv = new TlvBase(type, 0, new byte[0]);
        length = 0;
        this.tlvList.add(tlv);
        this.totalByteLen += length + 8;
    }

    public synchronized void appendShort(int type, short value) {
        appendBytes(type, ByteBuffer.allocate(2).order(DEFAULT_ORDER).putShort(value).array());
    }

    public synchronized void appendInt(int type, int value) {
        appendBytes(type, ByteBuffer.allocate(4).order(DEFAULT_ORDER).putInt(value).array());
    }

    public synchronized void appendLong(int type, long value) {
        appendBytes(type, ByteBuffer.allocate(8).order(DEFAULT_ORDER).putLong(value).array());
    }

    public synchronized void appendString(int type, String value) throws UnsupportedEncodingException {
        appendString(type, value, DEFAULT_ENCODE_TYPE);
    }

    public synchronized void appendString(int type, String value, String encodingType) throws UnsupportedEncodingException {
        byte[] byteArr;
        if (value == null) {
            byteArr = new byte[0];
        } else {
            byteArr = value.getBytes(encodingType);
        }
        appendBytes(type, byteArr);
    }

    public synchronized void appendTlvWrapper(int type, TlvWrapper value) {
        byte[] byteArr;
        if (value == null) {
            byteArr = new byte[0];
        } else {
            byteArr = value.serialize();
        }
        appendBytes(type, byteArr);
    }

    public synchronized byte[] serialize() {
        byte[] data;
        data = new byte[this.totalByteLen];
        int offset = 0;
        for (TlvBase tlv : this.tlvList) {
            byte[] type = ByteBuffer.allocate(4).order(DEFAULT_ORDER).putInt(tlv.getType()).array();
            System.arraycopy(type, 0, data, offset, type.length);
            int offset2 = offset + type.length;
            byte[] length = ByteBuffer.allocate(4).order(DEFAULT_ORDER).putInt(tlv.getLen()).array();
            System.arraycopy(length, 0, data, offset2, length.length);
            int offset3 = offset2 + length.length;
            byte[] value = tlv.getValue();
            System.arraycopy(value, 0, data, offset3, value.length);
            offset = offset3 + value.length;
        }
        return data;
    }

    public static TlvWrapper deserialize(byte[] data) throws TlvTransformException {
        if (data != null && data.length != 0) {
            return deserialize(data, 0, data.length);
        }
        throw new TlvTransformException("no data to deserialize");
    }

    public static TlvWrapper deserialize(byte[] data, int begin, int length) throws TlvTransformException {
        if (data == null || data.length == 0) {
            throw new TlvTransformException("no data to deserialize");
        } else if (begin + length <= data.length) {
            TlvWrapper tlvWrapper = new TlvWrapper();
            int offset = begin;
            while (begin + length > offset + 4 + 4) {
                int type = ByteBuffer.wrap(data, offset, 4).order(DEFAULT_ORDER).getInt();
                int offset2 = offset + 4;
                int valueLen = ByteBuffer.wrap(data, offset2, 4).order(DEFAULT_ORDER).getInt();
                int offset3 = offset2 + 4;
                byte[] value = new byte[valueLen];
                if (offset3 + valueLen <= begin + length) {
                    System.arraycopy(data, offset3, value, 0, valueLen);
                    tlvWrapper.appendBytes(type, value);
                    offset = offset3 + valueLen;
                } else {
                    throw new TlvTransformException(TRANS_NOT_MATCH_EXCEPTIN_MSG);
                }
            }
            if (offset == begin + length) {
                return tlvWrapper;
            }
            throw new TlvTransformException(TRANS_NOT_MATCH_EXCEPTIN_MSG);
        } else {
            throw new TlvTransformException(TRANS_OVER_FLOW_EXCEPTION_MSG);
        }
    }

    public static OptionalInt parseInt(TlvBase tlv) {
        if (isNullOrEmptyTlv(tlv)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(ByteBuffer.wrap(tlv.getValue()).order(DEFAULT_ORDER).getInt());
    }

    public static Optional<Byte> parseByte(TlvBase tlv) {
        if (isNullOrEmptyTlv(tlv)) {
            return Optional.empty();
        }
        return Optional.ofNullable(Byte.valueOf(tlv.getValue()[0]));
    }

    public static Optional<Short> parseShort(TlvBase tlv) {
        if (isNullOrEmptyTlv(tlv)) {
            return Optional.empty();
        }
        return Optional.ofNullable(Short.valueOf(ByteBuffer.wrap(tlv.getValue()).order(DEFAULT_ORDER).getShort()));
    }

    public static OptionalLong parseLong(TlvBase tlv) {
        if (isNullOrEmptyTlv(tlv)) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(ByteBuffer.wrap(tlv.getValue()).order(DEFAULT_ORDER).getLong());
    }

    public static Optional<String> parseString(TlvBase tlv, String decodingType) throws UnsupportedEncodingException {
        if (isNullOrEmptyTlv(tlv)) {
            return Optional.empty();
        }
        return Optional.ofNullable(new String(tlv.getValue(), Charset.forName(decodingType)));
    }

    public static Optional<String> parseString(TlvBase tlv) throws UnsupportedEncodingException {
        return parseString(tlv, DEFAULT_ENCODE_TYPE);
    }

    public static Optional<byte[]> parseBytes(TlvBase tlv) {
        if (isNullOrEmptyTlv(tlv)) {
            return Optional.ofNullable(new byte[0]);
        }
        return Optional.ofNullable(tlv.getValue());
    }

    public synchronized int getWrapperBytesLen() {
        return this.totalByteLen;
    }

    public static boolean isNullOrEmptyTlv(TlvBase tlv) {
        return tlv == null || tlv.isEmpty();
    }
}
