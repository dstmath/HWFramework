package android.support.v4.os;

import android.os.Build.VERSION;

public class BuildCompat {
    private BuildCompat() {
    }

    @Deprecated
    public static boolean isAtLeastN() {
        return VERSION.SDK_INT >= 24;
    }

    @Deprecated
    public static boolean isAtLeastNMR1() {
        return VERSION.SDK_INT >= 25;
    }

    public static boolean isAtLeastO() {
        return VERSION.SDK_INT >= 26;
    }

    public static boolean isAtLeastOMR1() {
        if (VERSION.CODENAME.startsWith("OMR")) {
            return true;
        }
        return isAtLeastP();
    }

    public static boolean isAtLeastP() {
        return VERSION.CODENAME.equals("P");
    }
}
