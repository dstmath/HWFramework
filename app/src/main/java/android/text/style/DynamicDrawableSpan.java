package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import java.lang.ref.WeakReference;

public abstract class DynamicDrawableSpan extends ReplacementSpan {
    public static final int ALIGN_BASELINE = 1;
    public static final int ALIGN_BOTTOM = 0;
    private static final String TAG = "DynamicDrawableSpan";
    private WeakReference<Drawable> mDrawableRef;
    protected final int mVerticalAlignment;

    public abstract Drawable getDrawable();

    public DynamicDrawableSpan() {
        this.mVerticalAlignment = ALIGN_BOTTOM;
    }

    protected DynamicDrawableSpan(int verticalAlignment) {
        this.mVerticalAlignment = verticalAlignment;
    }

    public int getVerticalAlignment() {
        return this.mVerticalAlignment;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        Rect rect = getCachedDrawable().getBounds();
        if (fm != null) {
            fm.ascent = -rect.bottom;
            fm.descent = ALIGN_BOTTOM;
            fm.top = fm.ascent;
            fm.bottom = ALIGN_BOTTOM;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        if (this.mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }
        canvas.translate(x, (float) transY);
        b.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = this.mDrawableRef;
        Drawable d = null;
        if (wr != null) {
            d = (Drawable) wr.get();
        }
        if (d != null) {
            return d;
        }
        d = getDrawable();
        this.mDrawableRef = new WeakReference(d);
        return d;
    }
}
