package android.graphics;

import android.graphics.PorterDuff;

public class PorterDuffXfermode extends Xfermode {
    public PorterDuffXfermode(PorterDuff.Mode mode) {
        this.porterDuffMode = mode.nativeInt;
    }
}
