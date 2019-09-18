package com.android.server.wifi.util;

public class BitMask {
    public int value;

    public BitMask(int value2) {
        this.value = value2;
    }

    public boolean testAndClear(int maskBit) {
        boolean ans = (this.value & maskBit) != 0;
        this.value &= ~maskBit;
        return ans;
    }
}
