package android.text.style;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public abstract class DynamicDrawableSpan extends ReplacementSpan {
    public static final int ALIGN_BASELINE = 1;
    public static final int ALIGN_BOTTOM = 0;
    public static final int ALIGN_CENTER = 2;
    @UnsupportedAppUsage
    private WeakReference<Drawable> mDrawableRef;
    protected final int mVerticalAlignment;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignmentType {
    }

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

    @Override // android.text.style.ReplacementSpan
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

    @Override // android.text.style.ReplacementSpan
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        int i = this.mVerticalAlignment;
        if (i == 1) {
            transY -= paint.getFontMetricsInt().descent;
        } else if (i == 2) {
            transY = ((bottom - top) / 2) - (b.getBounds().height() / 2);
        }
        canvas.translate(x, (float) transY);
        b.draw(canvas);
        canvas.restore();
    }

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
