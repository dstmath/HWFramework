package ohos.media.camera.device.adapter.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.agp.graphics.Surface;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class SurfaceUtils {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(SurfaceUtils.class);
    private static final String SURFACEUTILS_CLASS_NAME = "android.hardware.camera2.utils.SurfaceUtils";
    private static final String SURFACEUTILS_METHOD_GETSRURFACESIZE = "getSurfaceSize";

    public static Size getSurfaceSize(Surface surface) {
        android.util.Size size;
        if (surface == null) {
            return null;
        }
        android.util.Size size2 = new android.util.Size(0, 0);
        try {
            Class<?> cls = Class.forName(SURFACEUTILS_CLASS_NAME);
            Method method = cls.getMethod(SURFACEUTILS_METHOD_GETSRURFACESIZE, android.view.Surface.class);
            android.view.Surface surfaceImpl = ohos.agp.components.surfaceview.adapter.SurfaceUtils.getSurfaceImpl(surface);
            if (surfaceImpl == null) {
                return new Size(size2.getWidth(), size2.getHeight());
            }
            size = (android.util.Size) method.invoke(cls, surfaceImpl);
            return new Size(size.getWidth(), size.getHeight());
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("getSurfaceSize exception", e, new Object[0]);
            size = size2;
        }
    }
}
