package com.huawei.server.fsm;

import android.content.Context;
import android.graphics.Rect;
import com.android.server.display.FoldPolicy;
import com.huawei.android.hardware.display.DisplayViewportEx;

public class DefaultHwOutwardFoldPolicy extends FoldPolicy {
    protected DefaultHwOutwardFoldPolicy(Context context) {
        super(context);
    }

    public static DefaultHwOutwardFoldPolicy getInstance(Context context) {
        return new DefaultHwOutwardFoldPolicy(context);
    }

    public void adjustViewportFrame(DisplayViewportEx viewportEx, Rect layerRect, Rect displayRect) {
    }

    public Rect getDispRect(int mode) {
        return new Rect();
    }

    public Rect getScreenDispRect(int orientation) {
        return new Rect();
    }
}
