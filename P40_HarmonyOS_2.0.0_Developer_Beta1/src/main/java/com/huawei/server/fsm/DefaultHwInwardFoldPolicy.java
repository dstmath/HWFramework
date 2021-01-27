package com.huawei.server.fsm;

import android.content.Context;
import android.graphics.Rect;
import com.android.server.display.FoldPolicy;
import com.huawei.android.hardware.display.DisplayViewportEx;

public class DefaultHwInwardFoldPolicy extends FoldPolicy {
    protected DefaultHwInwardFoldPolicy(Context context) {
        super(context);
    }

    public static DefaultHwInwardFoldPolicy getInstance(Context context) {
        return new DefaultHwInwardFoldPolicy(context);
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
