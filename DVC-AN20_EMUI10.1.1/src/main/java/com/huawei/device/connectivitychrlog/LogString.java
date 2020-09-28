package com.huawei.device.connectivitychrlog;

import com.huawei.uikit.effect.BuildConfig;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class LogString {
    static String CHARSET = "UTF-8";
    static String EMPTY_STRING = BuildConfig.FLAVOR;
    private static final String LOG_TAG = "LogString";
    private int length;
    private String value;

    private String getEmptyString(int length2) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length2; i++) {
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

    public void setValue(String value2) {
        if (value2 == null) {
            this.value = getEmptyString(this.length);
        } else {
            this.value = value2;
        }
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, false, "setByByteArray failed ,not support len = %{public}d", Integer.valueOf(len));
        }
        try {
            this.value = new String(src, CHARSET);
            ChrLog.chrLogI(LOG_TAG, false, "setByByteArray value = %{public}s", this.value);
        } catch (UnsupportedEncodingException e) {
            ChrLog.chrLogE(LOG_TAG, false, "setByByteArray UnsupportedEncodingException", new Object[0]);
        }
    }

    public LogString(int length2) {
        this.length = length2;
        this.value = getEmptyString(length2);
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
                ChrLog.chrLogE(LOG_TAG, false, "toByteArray length error, subValueBytes.length = %{public}d, length = %{public}d", Integer.valueOf(subValueBytes.length), Integer.valueOf(this.length));
            } else {
                bytebuf.put(subValueBytes);
            }
            return bytebuf.array();
        } catch (UnsupportedEncodingException e) {
            return new byte[this.length];
        }
    }
}
