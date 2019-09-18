package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import java.lang.ref.WeakReference;

public abstract class DynamicDrawableSpan extends ReplacementSpan {
    public static final int ALIGN_BASELINE = 1;
    public static final int ALIGN_BOTTOM = 0;
    private WeakReference<Drawable> mDrawableRef;
    protected final int mVerticalAlignment;

    public abstract Drawable getDrawable();

    public DynamicDrawableSpan() {
        this.mVerticalAlignment = 0;
    }

    protected DynamicDrawableSpan(int verticalAlignment) {
        this.mVerticalAlignment = verticalAlignment;
    }

    public int getVerticalAlignment() {
        return this.mVerticalAlignment;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Rect rect = getCachedDrawable().getBounds();
        if (fm != null) {
            fm.ascent = -rect.bottom;
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        if (this.mVerticalAlignment == 1) {
            transY -= paint.getFontMetricsInt().descent;
        }
        canvas.translate(x, (float) transY);
        b.draw(canvas);
        canvas.restore();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.graphics.drawable.Drawable} */
    /* JADX WARNING: Multi-variable type inference failed */
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = this.mDrawableRef;
        Drawable d = null;
        if (wr != null) {
            d = wr.get();
        }
        if (d != null) {
            return d;
        }
        Drawable d2 = getDrawable();
        this.mDrawableRef = new WeakReference<>(d2);
        return d2;
    }
}
