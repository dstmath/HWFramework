package android.graphics;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.util.VirtualRefBasePtr;

public final class CanvasProperty<T> {
    private VirtualRefBasePtr mProperty;

    private static native long nCreateFloat(float f);

    private static native long nCreatePaint(long j);

    @UnsupportedAppUsage
    public static CanvasProperty<Float> createFloat(float initialValue) {
        return new CanvasProperty<>(nCreateFloat(initialValue));
    }

    @UnsupportedAppUsage
    public static CanvasProperty<Paint> createPaint(Paint initialValue) {
        return new CanvasProperty<>(nCreatePaint(initialValue.getNativeInstance()));
    }

    private CanvasProperty(long nativeContainer) {
        this.mProperty = new VirtualRefBasePtr(nativeContainer);
    }

    public long getNativeContainer() {
        return this.mProperty.get();
    }
}
