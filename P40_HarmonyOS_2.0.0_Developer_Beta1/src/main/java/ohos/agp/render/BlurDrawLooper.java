package ohos.agp.render;

import ohos.agp.utils.Color;

public class BlurDrawLooper {
    public final int intColor;
    public final float offsetX;
    public final float offsetY;
    public final float shadowRadius;

    public BlurDrawLooper(float f, float f2, float f3, Color color) {
        this.shadowRadius = f;
        this.offsetX = f2;
        this.offsetY = f3;
        this.intColor = color.getValue();
    }
}
