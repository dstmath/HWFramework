package android.graphics;

import android.graphics.PorterDuff;

public class Xfermode {
    static final int DEFAULT = PorterDuff.Mode.SRC_OVER.nativeInt;
    int porterDuffMode = DEFAULT;
}
