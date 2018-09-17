package android.graphics;

import android.graphics.PorterDuff.Mode;

public class PorterDuffXfermode extends Xfermode {
    public PorterDuffXfermode(Mode mode) {
        this.porterDuffMode = mode.nativeInt;
    }
}
