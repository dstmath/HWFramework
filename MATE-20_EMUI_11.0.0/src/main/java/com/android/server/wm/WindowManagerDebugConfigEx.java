package com.android.server.wm;

public class WindowManagerDebugConfigEx {
    private WindowManagerDebugConfigEx() {
    }

    public static String getWmTag() {
        return "WindowManager";
    }

    public static boolean getDebugAnim() {
        return WindowManagerDebugConfig.DEBUG_ANIM;
    }

    public static boolean getDebug() {
        return false;
    }
}
