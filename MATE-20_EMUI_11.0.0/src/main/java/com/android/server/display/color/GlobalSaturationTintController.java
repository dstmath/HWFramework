package com.android.server.display.color;

import android.content.Context;
import android.hardware.display.ColorDisplayManager;
import android.opengl.Matrix;
import android.util.Slog;
import java.util.Arrays;

final class GlobalSaturationTintController extends TintController {
    private final float[] mMatrixGlobalSaturation = new float[16];

    GlobalSaturationTintController() {
    }

    @Override // com.android.server.display.color.TintController
    public void setUp(Context context, boolean needsLinear) {
    }

    @Override // com.android.server.display.color.TintController
    public float[] getMatrix() {
        float[] fArr = this.mMatrixGlobalSaturation;
        return Arrays.copyOf(fArr, fArr.length);
    }

    @Override // com.android.server.display.color.TintController
    public void setMatrix(int saturationLevel) {
        if (saturationLevel < 0) {
            saturationLevel = 0;
        } else if (saturationLevel > 100) {
            saturationLevel = 100;
        }
        Slog.d("ColorDisplayService", "Setting saturation level: " + saturationLevel);
        if (saturationLevel == 100) {
            setActivated(false);
            Matrix.setIdentityM(this.mMatrixGlobalSaturation, 0);
            return;
        }
        setActivated(true);
        float saturation = ((float) saturationLevel) * 0.01f;
        float desaturation = 1.0f - saturation;
        float[] luminance = {0.231f * desaturation, 0.715f * desaturation, 0.072f * desaturation};
        float[] fArr = this.mMatrixGlobalSaturation;
        fArr[0] = luminance[0] + saturation;
        fArr[1] = luminance[0];
        fArr[2] = luminance[0];
        fArr[4] = luminance[1];
        fArr[5] = luminance[1] + saturation;
        fArr[6] = luminance[1];
        fArr[8] = luminance[2];
        fArr[9] = luminance[2];
        fArr[10] = luminance[2] + saturation;
    }

    @Override // com.android.server.display.color.TintController
    public int getLevel() {
        return 150;
    }

    @Override // com.android.server.display.color.TintController
    public boolean isAvailable(Context context) {
        return ColorDisplayManager.isColorTransformAccelerated(context);
    }
}
