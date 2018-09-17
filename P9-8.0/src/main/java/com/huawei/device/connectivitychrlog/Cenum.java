package com.huawei.device.connectivitychrlog;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Cenum {
    private final String LOG_TAG = ("Cenum" + getClass().getSimpleName());
    int length;
    Map<String, Integer> map = new LinkedHashMap();
    String name;

    public int getOrdinal() {
        if (this.map.get(this.name) != null) {
            return ((Integer) this.map.get(this.name)).intValue();
        }
        ChrLog.chrLogE(this.LOG_TAG, "getOrdinal failed name is not in the enum map, name = " + this.name);
        return -1;
    }

    public void setValue(String name) {
        this.name = name;
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        int data;
        if (this.length != len) {
            ChrLog.chrLogE(this.LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        if (this.length == 1) {
            data = src[0];
        } else if (this.length == 2) {
            data = ByteConvert.littleEndianBytesToShort(src);
        } else if (this.length == 4) {
            data = ByteConvert.littleEndianBytesToInt(src);
        } else {
            ChrLog.chrLogE(this.LOG_TAG, "setByByteArray failed ,not support length = " + this.length);
            return;
        }
        for (Entry<String, Integer> m : this.map.entrySet()) {
            if (data == ((Integer) m.getValue()).intValue()) {
                this.name = (String) m.getKey();
            }
        }
        ChrLog.chrLogI(this.LOG_TAG, "setByByteArray data = " + data + ", name = " + this.name);
    }

    public String getValue() {
        return this.name;
    }

    void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
        if (this.length == 1) {
            bytebuf.put((byte) getOrdinal());
        } else if (this.length == 2) {
            bytebuf.put(ByteConvert.shortToBytes((short) getOrdinal()));
        } else {
            bytebuf.put(ByteConvert.intToBytes(getOrdinal()));
        }
        return bytebuf.array();
    }
}
