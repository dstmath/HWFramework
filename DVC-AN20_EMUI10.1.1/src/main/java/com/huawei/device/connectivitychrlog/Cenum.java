package com.huawei.device.connectivitychrlog;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cenum {
    private final String LOG_TAG = ("Cenum" + getClass().getSimpleName());
    int length;
    Map<String, Integer> map = new LinkedHashMap();
    String name;

    public int getOrdinal() {
        if (this.map.get(this.name) != null) {
            return this.map.get(this.name).intValue();
        }
        ChrLog.chrLogE(this.LOG_TAG, false, "getOrdinal failed name is not in the enum map, name = %{public}s", this.name);
        return -1;
    }

    public void setValue(String name2) {
        this.name = name2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v5, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v6, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v7, resolved type: byte */
    /* JADX WARN: Multi-variable type inference failed */
    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        int data;
        if (this.length != len) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setByByteArray failed ,not support len = %{public}d", Integer.valueOf(len));
        }
        int i = this.length;
        if (i == 1) {
            data = src[0];
        } else if (i == 2) {
            data = ByteConvert.littleEndianBytesToShort(src);
        } else if (i == 4) {
            data = ByteConvert.littleEndianBytesToInt(src);
        } else {
            ChrLog.chrLogE(this.LOG_TAG, false, "setByByteArray failed ,not support length = %{public}d", Integer.valueOf(i));
            return;
        }
        for (Map.Entry<String, Integer> m : this.map.entrySet()) {
            if (data == m.getValue().intValue()) {
                this.name = m.getKey();
            }
        }
        ChrLog.chrLogI(this.LOG_TAG, false, "setByByteArray data = %{public}d, name = %{public}s", Integer.valueOf(data), this.name);
    }

    public String getValue() {
        return this.name;
    }

    /* access modifiers changed from: package-private */
    public void setLength(int length2) {
        this.length = length2;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
        int i = this.length;
        if (i == 1) {
            bytebuf.put((byte) getOrdinal());
        } else if (i == 2) {
            bytebuf.put(ByteConvert.shortToBytes((short) getOrdinal()));
        } else {
            bytebuf.put(ByteConvert.intToBytes(getOrdinal()));
        }
        return bytebuf.array();
    }
}
