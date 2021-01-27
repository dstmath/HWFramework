package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.graphics.PorterDuff;

public class Xfermode {
    static final int DEFAULT = PorterDuff.Mode.SRC_OVER.nativeInt;
    @UnsupportedAppUsage
    int porterDuffMode = DEFAULT;
}
