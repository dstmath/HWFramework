package android.support.v4.graphics;

import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

class PaintCompatApi14 {
    private static final String TOFU_STRING = "󟿽";
    private static final ThreadLocal<Pair<Rect, Rect>> sRectThreadLocal = new ThreadLocal();

    PaintCompatApi14() {
    }

    static boolean hasGlyph(@NonNull Paint paint, @NonNull String string) {
        int length = string.length();
        if (length == 1 && Character.isWhitespace(string.charAt(0))) {
            return true;
        }
        float missingGlyphWidth = paint.measureText(TOFU_STRING);
        float width = paint.measureText(string);
        if (width == 0.0f) {
            return false;
        }
        if (string.codePointCount(0, string.length()) > 1) {
            if (width > 2.0f * missingGlyphWidth) {
                return false;
            }
            float sumWidth = 0.0f;
            int i = 0;
            while (i < length) {
                int charCount = Character.charCount(string.codePointAt(i));
                sumWidth += paint.measureText(string, i, i + charCount);
                i += charCount;
            }
            if (width >= sumWidth) {
                return false;
            }
        }
        if (width != missingGlyphWidth) {
            return true;
        }
        Pair<Rect, Rect> rects = obtainEmptyRects();
        paint.getTextBounds(TOFU_STRING, 0, TOFU_STRING.length(), (Rect) rects.first);
        paint.getTextBounds(string, 0, length, (Rect) rects.second);
        return ((Rect) rects.first).equals(rects.second) ^ 1;
    }

    private static Pair<Rect, Rect> obtainEmptyRects() {
        Pair<Rect, Rect> rects = (Pair) sRectThreadLocal.get();
        if (rects == null) {
            rects = new Pair(new Rect(), new Rect());
            sRectThreadLocal.set(rects);
            return rects;
        }
        ((Rect) rects.first).setEmpty();
        ((Rect) rects.second).setEmpty();
        return rects;
    }
}
