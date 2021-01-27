package com.huawei.haptic;

public final class HapticAttributes {
    public static final int USAGE_ALARM = 2;
    public static final int USAGE_GAME = 5;
    public static final int USAGE_MEDIA = 1;
    public static final int USAGE_NOTIFICATION = 3;
    public static final int USAGE_RINGTONE = 4;
    public static final int USAGE_UNKNOWN = 0;
    private int mFlags = 0;
    private int mUsage = 0;

    static HwHapticAttributes createHwHapticAttributes(HapticAttributes attr) {
        if (attr == null) {
            return null;
        }
        HwHapticAttributes hwHapticAttr = new HwHapticAttributes();
        hwHapticAttr.mUsage = attr.mUsage;
        hwHapticAttr.mFlags = attr.mFlags;
        return hwHapticAttr;
    }

    public void setFlags(int flags) {
        this.mFlags |= flags;
    }

    public void setUsage(int usage) {
        this.mUsage = usage;
    }
}
