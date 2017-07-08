package android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextPaint;

public abstract class ReplacementSpan extends MetricAffectingSpan {
    public abstract void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint);

    public abstract int getSize(Paint paint, CharSequence charSequence, int i, int i2, FontMetricsInt fontMetricsInt);

    public void updateMeasureState(TextPaint p) {
    }

    public void updateDrawState(TextPaint ds) {
    }
}
