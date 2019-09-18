package org.bouncycastle.crypto.tls;

public class AlertLevel {
    public static final short fatal = 2;
    public static final short warning = 1;

    public static String getName(short s) {
        switch (s) {
            case 1:
                return "warning";
            case 2:
                return "fatal";
            default:
                return "UNKNOWN";
        }
    }

    public static String getText(short s) {
        return getName(s) + "(" + s + ")";
    }
}
