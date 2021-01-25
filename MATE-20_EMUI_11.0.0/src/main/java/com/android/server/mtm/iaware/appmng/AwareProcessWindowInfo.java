package com.android.server.mtm.iaware.appmng;

import android.util.ArrayMap;
import java.util.Map;

public class AwareProcessWindowInfo {
    private static int sMinWindowHeight = 1;
    private static int sMinWindowWidth = 1;
    public int height;
    public boolean inRestriction;
    public int mode;
    public String pkg;
    public int uid;
    public int width;
    public Map<Integer, Boolean> windows;

    public AwareProcessWindowInfo(int mode2, String pkg2, int uid2) {
        this(mode2, pkg2, uid2, -1, -1);
    }

    public AwareProcessWindowInfo(int mode2, String pkg2, int uid2, int width2, int height2) {
        this.width = -1;
        this.height = -1;
        this.inRestriction = false;
        this.windows = new ArrayMap();
        this.mode = mode2;
        this.pkg = pkg2;
        this.uid = uid2;
        this.width = width2;
        this.height = height2;
    }

    public boolean containsWindow(int code) {
        return this.windows.containsKey(Integer.valueOf(code));
    }

    public void addWindow(Integer code, boolean evil) {
        this.windows.put(code, Boolean.valueOf(evil));
    }

    public void removeWindow(Integer code) {
        this.windows.remove(code);
    }

    public boolean isEvil() {
        for (Map.Entry<Integer, Boolean> window : this.windows.entrySet()) {
            if (!window.getValue().booleanValue()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEvil(int code) {
        return this.windows.get(Integer.valueOf(code)).booleanValue();
    }

    public static int getMinWindowWidth() {
        return sMinWindowWidth;
    }

    public static int getMinWindowHeight() {
        return sMinWindowHeight;
    }

    public static void setMinWindowWidth(int width2) {
        sMinWindowWidth = width2;
    }

    public static void setMinWindowHeight(int height2) {
        sMinWindowHeight = height2;
    }
}
