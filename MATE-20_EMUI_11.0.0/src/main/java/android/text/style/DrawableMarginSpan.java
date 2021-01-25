package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spanned;

public class DrawableMarginSpan implements LeadingMarginSpan, LineHeightSpan {
    private static final int STANDARD_PAD_WIDTH = 0;
    private final Drawable mDrawable;
    private final int mPad;

    public DrawableMarginSpan(Drawable drawable) {
        this(drawable, 0);
    }

    public DrawableMarginSpan(Drawable drawable, int pad) {
        this.mDrawable = drawable;
        this.mPad = pad;
    }

    @Override // android.text.style.LeadingMarginSpan
    public int getLeadingMargin(boolean first) {
        return this.mDrawable.getIntrinsicWidth() + this.mPad;
    }

    @Override // android.text.style.LeadingMarginSpan
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        int itop = layout.getLineTop(layout.getLineForOffset(((Spanned) text).getSpanStart(this)));
        this.mDrawable.setBounds(x, itop, x + this.mDrawable.getIntrinsicWidth(), itop + this.mDrawable.getIntrinsicHeight());
        this.mDrawable.draw(c);
    }

    @Override // android.text.style.LineHeightSpan
    public void chooseHeight(CharSequence text, int start, int end, int istartv, int v, Paint.FontMetricsInt fm) {
        if (end == ((Spanned) text).getSpanEnd(this)) {
            int ht = this.mDrawable.getIntrinsicHeight();
            int need = ht - (((fm.descent + v) - fm.ascent) - istartv);
            if (need > 0) {
                fm.descent += need;
            }
            int need2 = ht - (((fm.bottom + v) - fm.top) - istartv);
            if (need2 > 0) {
                fm.bottom += need2;
            }
        }
    }
}
