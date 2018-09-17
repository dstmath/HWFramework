package com.android.server.mtm.iaware.appmng;

import android.util.ArrayMap;
import java.util.Map;

public class AwareProcessWindowInfo {
    public static final int MIN_HEIGHT = 1;
    public static final int MIN_WIDTH = 1;
    public int mHeight;
    public int mMode;
    public int mWidth;
    public Map<Integer, Boolean> mWindows = new ArrayMap();

    public AwareProcessWindowInfo(int mode, int width, int height) {
        this.mMode = mode;
        this.mWidth = width;
        this.mHeight = height;
    }

    public void addWindow(Integer code, boolean checkSize) {
        this.mWindows.put(code, Boolean.valueOf(checkSize));
    }

    public void removeWindow(Integer code) {
        this.mWindows.remove(code);
    }

    public boolean isEvil() {
        return this.mWidth == 1 && this.mHeight == 1;
    }
}
