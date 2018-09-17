package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface LineBackgroundSpan extends ParagraphStyle {
    void drawBackground(Canvas canvas, Paint paint, int i, int i2, int i3, int i4, int i5, CharSequence charSequence, int i6, int i7, int i8);
}
