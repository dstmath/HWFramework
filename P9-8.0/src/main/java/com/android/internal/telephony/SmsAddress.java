package com.android.internal.telephony;

public abstract class SmsAddress {
    public static final int TON_ABBREVIATED = 6;
    public static final int TON_ALPHANUMERIC = 5;
    public static final int TON_INTERNATIONAL = 1;
    public static final int TON_NATIONAL = 2;
    public static final int TON_NETWORK = 3;
    public static final int TON_SUBSCRIBER = 4;
    public static final int TON_UNKNOWN = 0;
    public String address;
    public byte[] origBytes;
    public int ton;

    public String getAddressString() {
        return this.address;
    }

    public boolean isAlphanumeric() {
        return this.ton == 5;
    }

    public boolean isNetworkSpecific() {
        return this.ton == 3;
    }

    public boolean couldBeEmailGateway() {
        return this.address.length() <= 4;
    }
}
