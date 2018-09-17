package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spanned;

public class DrawableMarginSpan implements LeadingMarginSpan, LineHeightSpan {
    private Drawable mDrawable;
    private int mPad;

    public DrawableMarginSpan(Drawable b) {
        this.mDrawable = b;
    }

    public DrawableMarginSpan(Drawable b, int pad) {
        this.mDrawable = b;
        this.mPad = pad;
    }

    public int getLeadingMargin(boolean first) {
        return this.mDrawable.getIntrinsicWidth() + this.mPad;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        int ix = x;
        int itop = layout.getLineTop(layout.getLineForOffset(((Spanned) text).getSpanStart(this)));
        this.mDrawable.setBounds(ix, itop, ix + this.mDrawable.getIntrinsicWidth(), itop + this.mDrawable.getIntrinsicHeight());
        this.mDrawable.draw(c);
    }

    public void chooseHeight(CharSequence text, int start, int end, int istartv, int v, FontMetricsInt fm) {
        if (end == ((Spanned) text).getSpanEnd(this)) {
            int ht = this.mDrawable.getIntrinsicHeight();
            int need = ht - (((fm.descent + v) - fm.ascent) - istartv);
            if (need > 0) {
                fm.descent += need;
            }
            need = ht - (((fm.bottom + v) - fm.top) - istartv);
            if (need > 0) {
                fm.bottom += need;
            }
        }
    }
}
