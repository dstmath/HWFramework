package com.huawei.internal.telephony.vsim.util;

public final class SafeUnBox {
    private SafeUnBox() {
    }

    public static boolean unBox(Boolean box, boolean def) {
        return box == null ? def : box.booleanValue();
    }

    public static int unBox(Integer box, int def) {
        return box == null ? def : box.intValue();
    }

    public static long unBox(Long box, long def) {
        return box == null ? def : box.longValue();
    }

    public static double unBox(Double box, double def) {
        return box == null ? def : box.doubleValue();
    }

    public static float unBox(Float box, float def) {
        return box == null ? def : box.floatValue();
    }

    public static byte unBox(Byte box, byte def) {
        return box == null ? def : box.byteValue();
    }
}
