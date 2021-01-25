package com.huawei.android.graphics;

import android.graphics.Canvas;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CanvasEx {
    private CanvasEx() {
    }

    public static void freeCaches() {
        Canvas.freeCaches();
    }

    public static void freeTextLayoutCaches() {
        Canvas.freeTextLayoutCaches();
    }
}
