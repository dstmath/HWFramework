package android.text.style;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;

public class IconMarginSpan implements LeadingMarginSpan, LineHeightSpan {
    private final Bitmap mBitmap;
    private final int mPad;

    public IconMarginSpan(Bitmap bitmap) {
        this(bitmap, 0);
    }

    public IconMarginSpan(Bitmap bitmap, int pad) {
        this.mBitmap = bitmap;
        this.mPad = pad;
    }

    public int getLeadingMargin(boolean first) {
        return this.mBitmap.getWidth() + this.mPad;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        int x2;
        Layout layout2 = layout;
        int itop = layout2.getLineTop(layout2.getLineForOffset(((Spanned) text).getSpanStart(this)));
        if (dir < 0) {
            x2 = x - this.mBitmap.getWidth();
        } else {
            x2 = x;
        }
        c.drawBitmap(this.mBitmap, (float) x2, (float) itop, p);
    }

    public void chooseHeight(CharSequence text, int start, int end, int istartv, int v, Paint.FontMetricsInt fm) {
        if (end == ((Spanned) text).getSpanEnd(this)) {
            int ht = this.mBitmap.getHeight();
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
