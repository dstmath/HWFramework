package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.GLUtilsAdapter;
import ohos.media.image.PixelMap;

public final class GLUtils {
    private GLUtils() {
    }

    public static void texImage2DUtil(int i, int i2, PixelMap pixelMap, int i3) {
        GLUtilsAdapter.texImage2DUtil(i, i2, pixelMap, i3);
    }
}
