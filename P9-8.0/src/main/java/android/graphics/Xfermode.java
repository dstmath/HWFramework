package android.graphics;

import android.graphics.PorterDuff.Mode;

public class Xfermode {
    static final int DEFAULT = Mode.SRC_OVER.nativeInt;
    int porterDuffMode = DEFAULT;
}
