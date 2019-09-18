package jcifs.dcerpc;

import jcifs.dcerpc.rpc;

public class UnicodeString extends rpc.unicode_string {
    boolean zterm;

    public UnicodeString(boolean zterm2) {
        this.zterm = zterm2;
    }

    public UnicodeString(rpc.unicode_string rus, boolean zterm2) {
        this.length = rus.length;
        this.maximum_length = rus.maximum_length;
        this.buffer = rus.buffer;
        this.zterm = zterm2;
    }

    public UnicodeString(String str, boolean zterm2) {
        int zt;
        this.zterm = zterm2;
        int len = str.length();
        if (zterm2) {
            zt = 1;
        } else {
            zt = 0;
        }
        short s = (short) ((len + zt) * 2);
        this.maximum_length = s;
        this.length = s;
        this.buffer = new short[(len + zt)];
        int i = 0;
        while (i < len) {
            this.buffer[i] = (short) str.charAt(i);
            i++;
        }
        if (zterm2) {
            this.buffer[i] = 0;
        }
    }

    public String toString() {
        int i;
        int i2 = this.length / 2;
        if (this.zterm) {
            i = 1;
        } else {
            i = 0;
        }
        int len = i2 - i;
        char[] ca = new char[len];
        for (int i3 = 0; i3 < len; i3++) {
            ca[i3] = (char) this.buffer[i3];
        }
        return new String(ca, 0, len);
    }
}
