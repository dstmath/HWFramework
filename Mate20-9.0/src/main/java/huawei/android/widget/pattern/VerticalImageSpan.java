package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.style.ImageSpan;
import android.util.Log;

public class VerticalImageSpan extends ImageSpan {
    private static final int MAX_HEIGHT = 24;
    private Context mContext;

    public VerticalImageSpan(Context context, Drawable drawable) {
        super(drawable);
        this.mContext = context;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getDrawable();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.bottom - fmPaint.top;
        if (drawable instanceof VectorDrawable) {
            int maxValue = (int) ((24.0f * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
            drawable.setBounds(new Rect(0, 0, maxValue, maxValue));
        } else {
            Log.w("VerticalImageSpan", "the drawable " + drawable + " is not svg drawable");
        }
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            int drHeight = rect.bottom - rect.top;
            int top = (drHeight / 2) - (fontHeight / 4);
            int bottom = (drHeight / 2) + (fontHeight / 4);
            fontMetricsInt.ascent = -bottom;
            fontMetricsInt.top = -bottom;
            fontMetricsInt.bottom = top;
            fontMetricsInt.descent = top;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        canvas.save();
        canvas.translate(x, (float) ((((bottom - top) - drawable.getBounds().bottom) / 2) + top));
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        drawable.draw(canvas);
        canvas.restore();
    }
}
