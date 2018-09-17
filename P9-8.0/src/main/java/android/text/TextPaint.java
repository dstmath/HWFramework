package android.text;

import android.graphics.Paint;

public class TextPaint extends Paint {
    public int baselineShift;
    public int bgColor;
    public float density = 1.0f;
    public int[] drawableState;
    public int linkColor;
    public int underlineColor = 0;
    public float underlineThickness;

    public TextPaint(int flags) {
        super(flags);
    }

    public TextPaint(Paint p) {
        super(p);
    }

    public void set(TextPaint tp) {
        super.set(tp);
        this.bgColor = tp.bgColor;
        this.baselineShift = tp.baselineShift;
        this.linkColor = tp.linkColor;
        this.drawableState = tp.drawableState;
        this.density = tp.density;
        this.underlineColor = tp.underlineColor;
        this.underlineThickness = tp.underlineThickness;
    }

    public void setUnderlineText(int color, float thickness) {
        this.underlineColor = color;
        this.underlineThickness = thickness;
    }
}
