package android.graphics;

import android.graphics.PorterDuff.Mode;

public class PorterDuffXfermode extends Xfermode {
    public final Mode mode;

    private static native long nativeCreateXfermode(int i);

    public PorterDuffXfermode(Mode mode) {
        this.mode = mode;
        this.native_instance = nativeCreateXfermode(mode.nativeInt);
    }
}
