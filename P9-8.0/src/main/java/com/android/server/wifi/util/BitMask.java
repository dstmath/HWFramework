package com.android.server.wifi.util;

public class BitMask {
    public int value;

    public BitMask(int value) {
        this.value = value;
    }

    public boolean testAndClear(int maskBit) {
        boolean ans = (this.value & maskBit) != 0;
        this.value &= ~maskBit;
        return ans;
    }
}
