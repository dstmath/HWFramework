package com.huawei.device.connectivitychrlog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class LogString {
    static String CHARSET = "UTF-8";
    static String EMPTY_STRING = "";
    private static final String LOG_TAG = "LogString";
    private int length;
    private String value;

    private String getEmptyString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(EMPTY_STRING);
        }
        return sb.toString();
    }

    public int getLength() {
        return this.length;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value == null) {
            this.value = getEmptyString(this.length);
        } else {
            this.value = value;
        }
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        try {
            this.value = new String(src, CHARSET);
            ChrLog.chrLogI(LOG_TAG, "setByByteArray value = " + this.value);
        } catch (UnsupportedEncodingException e) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray UnsupportedEncodingException");
        }
    }

    public LogString(int length) {
        this.length = length;
        this.value = getEmptyString(length);
    }

    public String toString() {
        return this.value.toString();
    }

    public byte[] toByteArray() {
        try {
            ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
            byte[] subValueBytes = this.value.getBytes(CHARSET);
            if (subValueBytes.length > this.length) {
                bytebuf.put(subValueBytes, 0, this.length);
                ChrLog.chrLogE(LOG_TAG, "toByteArray length error, subValueBytes.length = " + subValueBytes.length + ", length = " + this.length);
            } else {
                bytebuf.put(subValueBytes);
            }
            return bytebuf.array();
        } catch (UnsupportedEncodingException e) {
            return new byte[this.length];
        }
    }
}
