package ohos.agp.utils;

import java.math.BigDecimal;

public class RectFloat {
    private static final int HASHCODE_MULTIPLIER = 31;
    public float bottom;
    public float left;
    public float right;
    public float top;

    public RectFloat() {
    }

    public RectFloat(float f, float f2, float f3, float f4) {
        this.left = f;
        this.top = f2;
        this.right = f3;
        this.bottom = f4;
    }

    public RectFloat(Rect rect) {
        if (rect != null) {
            this.left = (float) rect.left;
            this.top = (float) rect.top;
            this.right = (float) rect.right;
            this.bottom = (float) rect.bottom;
            return;
        }
        this.left = 0.0f;
        this.top = 0.0f;
        this.right = 0.0f;
        this.bottom = 0.0f;
    }

    public RectFloat(RectFloat rectFloat) {
        if (rectFloat != null) {
            this.left = rectFloat.left;
            this.top = rectFloat.top;
            this.right = rectFloat.right;
            this.bottom = rectFloat.bottom;
            return;
        }
        this.left = 0.0f;
        this.top = 0.0f;
        this.right = 0.0f;
        this.bottom = 0.0f;
    }

    public final float getHorizontalCenter() {
        return new BigDecimal((double) this.left).add(new BigDecimal((double) this.right)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public final float getVerticalCenter() {
        return new BigDecimal((double) this.top).add(new BigDecimal((double) this.bottom)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public boolean isInclude(float f, float f2) {
        float f3 = this.left;
        float f4 = this.right;
        if (f3 >= f4) {
            return false;
        }
        float f5 = this.top;
        float f6 = this.bottom;
        return f5 < f6 && f >= f3 && f < f4 && f2 >= f5 && f2 < f6;
    }

    public boolean isInclude(RectFloat rectFloat) {
        if (rectFloat == null) {
            return false;
        }
        float f = this.left;
        float f2 = this.right;
        if (f >= f2) {
            return false;
        }
        float f3 = this.top;
        float f4 = this.bottom;
        if (f3 < f4 && f <= rectFloat.left && rectFloat.right <= f2 && f3 <= rectFloat.top && rectFloat.bottom <= f4) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            RectFloat rectFloat = (RectFloat) obj;
            if (new BigDecimal((double) rectFloat.left).compareTo(new BigDecimal((double) this.left)) == 0 && new BigDecimal((double) rectFloat.top).compareTo(new BigDecimal((double) this.top)) == 0 && new BigDecimal((double) rectFloat.right).compareTo(new BigDecimal((double) this.right)) == 0 && new BigDecimal((double) rectFloat.bottom).compareTo(new BigDecimal((double) this.bottom)) == 0) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        float f = this.left;
        int i = 0;
        int floatToIntBits = (f == 0.0f ? 0 : Float.floatToIntBits(f)) * 31;
        float f2 = this.top;
        int floatToIntBits2 = (floatToIntBits + (f2 == 0.0f ? 0 : Float.floatToIntBits(f2))) * 31;
        float f3 = this.right;
        int floatToIntBits3 = (floatToIntBits2 + (f3 == 0.0f ? 0 : Float.floatToIntBits(f3))) * 31;
        float f4 = this.bottom;
        if (f4 != 0.0f) {
            i = Float.floatToIntBits(f4);
        }
        return floatToIntBits3 + i;
    }

    public final float getHeight() {
        return this.bottom - this.top;
    }

    public final float getWidth() {
        return this.right - this.left;
    }

    public void shrink(float f, float f2) {
        this.left += f;
        this.right -= f;
        this.top += f2;
        this.bottom -= f2;
    }

    public boolean getIntersectRect(RectFloat rectFloat) {
        if (rectFloat == null) {
            return false;
        }
        float f = this.left;
        if (f < rectFloat.right) {
            float f2 = this.right;
            float f3 = rectFloat.left;
            if (f2 > f3 && this.top < rectFloat.bottom && this.bottom > rectFloat.top) {
                this.left = Math.max(f, f3);
                this.top = Math.max(this.top, rectFloat.top);
                this.right = Math.min(this.right, rectFloat.right);
                this.bottom = Math.min(this.bottom, rectFloat.bottom);
                return true;
            }
        }
        return false;
    }

    public static boolean isIntersect(RectFloat rectFloat, RectFloat rectFloat2) {
        return rectFloat != null && rectFloat2 != null && rectFloat.left < rectFloat2.right && rectFloat2.left < rectFloat.right && rectFloat.top < rectFloat2.bottom && rectFloat2.top < rectFloat.bottom;
    }

    public final boolean isEmpty() {
        return this.left >= this.right || this.top >= this.bottom;
    }

    public void translate(float f, float f2) {
        this.right += f;
        this.bottom += f2;
        this.left += f;
        this.top += f2;
    }

    public void modify(float f, float f2, float f3, float f4) {
        this.left = f;
        this.right = f3;
        this.top = f2;
        this.bottom = f4;
    }

    public void modify(Rect rect) {
        if (rect != null) {
            this.left = (float) rect.left;
            this.top = (float) rect.top;
            this.right = (float) rect.right;
            this.bottom = (float) rect.bottom;
        }
    }

    public void modify(RectFloat rectFloat) {
        if (rectFloat != null) {
            this.left = rectFloat.left;
            this.top = rectFloat.top;
            this.right = rectFloat.right;
            this.bottom = rectFloat.bottom;
        }
    }

    public void clear() {
        this.left = 0.0f;
        this.top = 0.0f;
        this.right = 0.0f;
        this.bottom = 0.0f;
    }

    public boolean setIntersect(RectFloat rectFloat, RectFloat rectFloat2) {
        if (!(rectFloat == null || rectFloat2 == null)) {
            float f = rectFloat.left;
            if (f < rectFloat2.right) {
                float f2 = rectFloat2.left;
                if (f2 < rectFloat.right && rectFloat.top < rectFloat2.bottom && rectFloat2.top < rectFloat.bottom) {
                    this.left = Math.max(f, f2);
                    this.top = Math.max(rectFloat.top, rectFloat2.top);
                    this.right = Math.min(rectFloat.right, rectFloat2.right);
                    this.bottom = Math.min(rectFloat.bottom, rectFloat2.bottom);
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return "RectFloat(" + this.left + ", " + this.top + ", " + this.right + ", " + this.bottom + ")";
    }

    public void fuse(float f, float f2, float f3, float f4) {
        if (f < f3 && f2 < f4) {
            float f5 = this.left;
            if (f5 >= this.right || this.top >= this.bottom) {
                this.left = f;
                this.right = f3;
                this.top = f2;
                this.bottom = f4;
                return;
            }
            this.left = Math.min(f5, f);
            this.top = Math.min(this.top, f2);
            this.right = Math.max(this.right, f3);
            this.bottom = Math.max(this.bottom, f4);
        }
    }

    public void fuse(RectFloat rectFloat) {
        if (rectFloat != null) {
            fuse(rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom);
        }
    }

    public void translateTo(float f, float f2) {
        this.right += f - this.left;
        this.bottom += f2 - this.top;
        this.left = f;
        this.top = f2;
    }

    public void rounding(Rect rect) {
        if (rect != null) {
            rect.set((int) Math.floor((double) this.left), (int) Math.floor((double) this.top), (int) Math.ceil((double) this.right), (int) Math.ceil((double) this.bottom));
        }
    }
}
