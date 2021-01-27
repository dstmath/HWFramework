package com.huawei.systemmanager.common;

import android.content.Context;

public class HwResourceEx {
    public static final int BYTE_SHORT = 0;
    public static final int FILE_SIZE_SUFFIX = 6;
    public static final int GIGA_BYTE_SHORT = 3;
    public static final int KILO_BYTE_SHORT = 1;
    public static final int MEGA_BYTE_SHORT = 2;
    public static final int PETA_BYTE_SHORT = 5;
    public static final int TERA_BYTE_SHORT = 4;

    public static String getString(Context context, int index) {
        if (context == null) {
            return null;
        }
        switch (index) {
            case 0:
                return context.getString(17039725);
            case 1:
                return context.getString(17040390);
            case 2:
                return context.getString(17040016);
            case 3:
                return context.getString(17040015);
            case 4:
                return context.getString(17041361);
            case 5:
                return context.getString(17041022);
            case 6:
                return context.getString(17040145);
            default:
                return null;
        }
    }
}
