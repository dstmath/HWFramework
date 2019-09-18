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
        String str = this.LOG_TAG;
        ChrLog.chrLogE(str, "getOrdinal failed name is not in the enum map, name = " + this.name);
        return -1;
    }

    public void setValue(String name2) {
        this.name = name2;
    }

    /* JADX WARNING: type inference failed for: r5v0, types: [byte[]] */
    /* JADX WARNING: type inference failed for: r0v4, types: [byte] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r0v4, types: [byte] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setByByteArray(byte[] r5, int len, boolean bIsLittleEndian) {
        int data;
        if (this.length != len) {
            String str = this.LOG_TAG;
            ChrLog.chrLogE(str, "setByByteArray failed ,not support len = " + len);
        }
        if (this.length == 1) {
            data = r5[0];
        } else if (this.length == 2) {
            data = ByteConvert.littleEndianBytesToShort(r5);
        } else if (this.length == 4) {
            data = ByteConvert.littleEndianBytesToInt(r5);
        } else {
            String str2 = this.LOG_TAG;
            ChrLog.chrLogE(str2, "setByByteArray failed ,not support length = " + this.length);
            return;
        }
        for (Map.Entry<String, Integer> m : this.map.entrySet()) {
            if (data == m.getValue().intValue()) {
                this.name = m.getKey();
            }
        }
        String str3 = this.LOG_TAG;
        ChrLog.chrLogI(str3, "setByByteArray data = " + data + ", name = " + this.name);
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
