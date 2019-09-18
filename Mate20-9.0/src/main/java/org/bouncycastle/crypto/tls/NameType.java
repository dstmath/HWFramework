package org.bouncycastle.crypto.tls;

public class NameType {
    public static final short host_name = 0;

    public static boolean isValid(short s) {
        return s == 0;
    }
}
