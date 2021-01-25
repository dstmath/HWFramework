package ohos.agp.components.surfaceview.adapter;

import android.view.Surface;
import java.lang.reflect.Field;

public class SurfaceUtils {
    private static native Surface nativeGetASurface(long j);

    private static native long nativeSetASurface(long j, long j2);

    public static void setSurfaceImpl(ohos.agp.graphics.Surface surface, Surface surface2) {
        if (surface2 != null) {
            nativeSetASurface(getPrivateSurfacePtr(surface), getPrivateASurfacePtr(surface2));
        }
    }

    public static Surface getSurfaceImpl(ohos.agp.graphics.Surface surface) {
        return nativeGetASurface(getPrivateSurfacePtr(surface));
    }

    public static ohos.agp.graphics.Surface getSurface(Surface surface) {
        ohos.agp.graphics.Surface surface2 = new ohos.agp.graphics.Surface();
        setSurfaceImpl(surface2, surface);
        return surface2;
    }

    private static long getPrivateSurfacePtr(ohos.agp.graphics.Surface surface) {
        try {
            Field declaredField = surface.getClass().getDeclaredField("mNativePtr");
            declaredField.setAccessible(true);
            return Long.parseLong(String.valueOf(declaredField.get(surface)));
        } catch (ReflectiveOperationException unused) {
            return 0;
        }
    }

    private static long getPrivateASurfacePtr(Surface surface) {
        try {
            Field declaredField = surface.getClass().getDeclaredField("mNativeObject");
            declaredField.setAccessible(true);
            return Long.parseLong(String.valueOf(declaredField.get(surface)));
        } catch (ReflectiveOperationException unused) {
            return 0;
        }
    }
}
