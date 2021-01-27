package com.huawei.android.view;

import android.graphics.RenderNode;

public interface IHwShadowManager {
    public static final int FLOATING_SHADOW_SIZE_L = 6;
    public static final int FLOATING_SHADOW_SIZE_M = 5;
    public static final int FLOATING_SHADOW_SIZE_S = 4;
    public static final int SHADOW_DEVICE_CAR = 3;
    public static final int SHADOW_DEVICE_DEFAULT = -1;
    public static final int SHADOW_DEVICE_PHONE = 1;
    public static final int SHADOW_DEVICE_TV = 4;
    public static final int SHADOW_MODE_DARK = 1;
    public static final int SHADOW_MODE_DEFAULT = -1;
    public static final int SHADOW_MODE_LIGHT = 0;
    public static final int SHADOW_MODE_TRANSLUCENT = 2;
    public static final int SHADOW_SIZE_L = 3;
    public static final int SHADOW_SIZE_M = 2;
    public static final int SHADOW_SIZE_OFF = -1;
    public static final int SHADOW_SIZE_S = 1;
    public static final int SHADOW_SIZE_XS = 0;

    boolean setShadowStyle(RenderNode renderNode, int i, int i2, int i3);
}
