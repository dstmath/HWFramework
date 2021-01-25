package ohos.agp.render.opengl.adapter;

import android.opengl.GLUtils;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;

public final class GLUtilsAdapter {
    public static void texImage2DUtil(int i, int i2, PixelMap pixelMap, int i3) {
        GLUtils.texImage2D(i, i2, ImageDoubleFwConverter.createShadowBitmap(pixelMap), i3);
    }
}
