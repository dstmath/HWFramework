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
        String retString = null;
        switch (index) {
            case 0:
                retString = context.getString(17039707);
                break;
            case 1:
                retString = context.getString(17040313);
                break;
            case 2:
                retString = context.getString(17040505);
                break;
            case 3:
                retString = context.getString(17040120);
                break;
            case 4:
                retString = context.getString(17041238);
                break;
            case 5:
                retString = context.getString(17040896);
                break;
            case 6:
                retString = context.getString(17040059);
                break;
        }
        return retString;
    }
}
