package android.support.v4.graphics;

import android.graphics.Paint;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;

public final class PaintCompat {
    public static boolean hasGlyph(@NonNull Paint paint, @NonNull String string) {
        if (VERSION.SDK_INT >= 23) {
            return paint.hasGlyph(string);
        }
        return PaintCompatApi14.hasGlyph(paint, string);
    }

    private PaintCompat() {
    }
}
