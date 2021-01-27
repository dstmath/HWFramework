package ohos.agp.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import ohos.agp.utils.RectFloat;

public class RectFloat {
    private static final int HASHCODE_MULTIPLIER = 31;
    public float bottom;
    public float left;
    private float pivotX;
    private float pivotY;
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

    public Point getCenter() {
        return new Point(getHorizontalCenter(), getVerticalCenter());
    }

    public void setPivot(float f, float f2) {
        this.pivotX = f;
        this.pivotY = f2;
    }

    public void setPivot(Point point) {
        if (point != null) {
            this.pivotX = (float) point.getPointXToInt();
            this.pivotY = (float) point.getPointYToInt();
        }
    }

    public void setPivotXCoordinate(float f) {
        this.pivotX = f;
    }

    public void setPivotYCoordinate(float f) {
        this.pivotY = f;
    }

    public Point getPivot() {
        return new Point(this.pivotX, this.pivotY);
    }

    public float getPivotXCoordinate() {
        return this.pivotX;
    }

    public float getPivotYCoordinate() {
        return this.pivotY;
    }

    public void scale(float f) {
        float f2 = this.pivotX;
        this.left = ((this.left - f2) * f) + f2;
        this.right = f2 + ((this.right - f2) * f);
        float f3 = this.pivotY;
        this.top = ((this.top - f3) * f) + f3;
        this.bottom = f3 + ((this.bottom - f3) * f);
    }

    public enum RotationEnum {
        ROTATE_90(0),
        ROTATE_180(1),
        ROTATE_270(3),
        ROTATE_360(4);
        
        private final int nativeValue;

        private RotationEnum(int i) {
            this.nativeValue = i;
        }

        /* access modifiers changed from: protected */
        public int getValue() {
            return this.nativeValue;
        }

        public static RotationEnum getByInt(int i) {
            return (RotationEnum) Arrays.stream(values()).filter(new Predicate(i) {
                /* class ohos.agp.utils.$$Lambda$RectFloat$RotationEnum$z8nMyXAlVCTJvhgEfTY0a1dQgkA */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return RectFloat.RotationEnum.lambda$getByInt$0(this.f$0, (RectFloat.RotationEnum) obj);
                }
            }).findAny().orElse(ROTATE_360);
        }

        static /* synthetic */ boolean lambda$getByInt$0(int i, RotationEnum rotationEnum) {
            return rotationEnum.getValue() == i;
        }
    }

    public void rotateBy(RotationEnum rotationEnum) {
        float f;
        float f2;
        float f3;
        float f4;
        if (rotationEnum != null) {
            float f5 = this.left;
            float f6 = this.top;
            float f7 = this.right;
            float f8 = this.bottom;
            int i = AnonymousClass1.$SwitchMap$ohos$agp$utils$RectFloat$RotationEnum[rotationEnum.ordinal()];
            if (i != 1) {
                if (i == 2) {
                    float f9 = this.pivotX;
                    f5 = (f9 + f9) - this.right;
                    float f10 = this.pivotY;
                    f3 = (f10 + f10) - this.bottom;
                    float f11 = (f9 + f9) - this.left;
                    f4 = (f10 + f10) - this.top;
                    f2 = f11;
                } else if (i != 3) {
                    f = f8;
                    f2 = f7;
                    f3 = f6;
                } else {
                    float f12 = this.top;
                    float f13 = this.pivotY;
                    float f14 = this.pivotX;
                    f3 = (f14 - this.right) + f13;
                    f2 = (this.bottom - f13) + f14;
                    f4 = f13 + (f14 - this.left);
                    f5 = (f12 - f13) + f14;
                }
                f = f4;
            } else {
                float f15 = this.pivotY;
                float f16 = this.pivotX;
                f5 = (f15 - this.bottom) + f16;
                f3 = (this.left - f16) + f15;
                f2 = (f15 - this.top) + f16;
                f = f15 + (this.right - f16);
            }
            this.left = f5;
            this.top = f3;
            this.right = f2;
            this.bottom = f;
        }
    }

    /* renamed from: ohos.agp.utils.RectFloat$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$utils$RectFloat$RotationEnum = new int[RotationEnum.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$utils$RectFloat$RotationEnum[RotationEnum.ROTATE_90.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$utils$RectFloat$RotationEnum[RotationEnum.ROTATE_180.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$utils$RectFloat$RotationEnum[RotationEnum.ROTATE_270.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
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

    public boolean isInclude(Point point) {
        if (point != null) {
            return isInclude(point.getPointX(), point.getPointY());
        }
        return false;
    }

    public boolean isInclude(RectFloat rectFloat) {
        if (rectFloat == null) {
            return false;
        }
        return isInclude(rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom);
    }

    public boolean isInclude(float f, float f2, float f3, float f4) {
        if (f > f3 || f2 > f4 || !verifyRectFloat() || f < this.left || f3 > this.right || f2 < this.top || f4 > this.bottom) {
            return false;
        }
        return true;
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

    public DimensFloat getRectSize() {
        return new DimensFloat(getWidth(), getHeight());
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
        if (rectFloat == null || rectFloat2 == null) {
            return false;
        }
        return rectFloat.isInclude(rectFloat2);
    }

    public boolean isIntersect(float f, float f2, float f3, float f4) {
        return obtainIntersectRect(f, f2, f3, f4) != null;
    }

    public boolean isIntersect(RectFloat rectFloat) {
        return obtainIntersectRect(rectFloat) != null;
    }

    public final boolean isEmpty() {
        return this.left >= this.right || this.top >= this.bottom;
    }

    public boolean verifyRectFloat() {
        return this.left < this.right && this.top < this.bottom;
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

    public void translateCenterTo(float f, float f2) {
        translate(f - getHorizontalCenter(), f2 - getVerticalCenter());
    }

    public void translateCenterTo(Point point) {
        if (point != null) {
            translateCenterTo(point.getPointX(), point.getPointY());
        }
    }

    public void rounding(Rect rect) {
        if (rect != null) {
            rect.set((int) Math.floor((double) this.left), (int) Math.floor((double) this.top), (int) Math.ceil((double) this.right), (int) Math.ceil((double) this.bottom));
        }
    }

    public RectFloat obtainIntersectRect(float f, float f2, float f3, float f4) {
        if (f >= f3 || f2 >= f4 || !verifyRectFloat()) {
            return null;
        }
        return obtainRectWithInsidePoints(f, f2, f3, f4, getPointsInThisRect(f, f2, f3, f4));
    }

    private RectFloat obtainInnerRect(float f, float f2, float f3, float f4) {
        float f5 = this.left;
        if (f5 < f || f5 > f3) {
            return null;
        }
        float f6 = this.right;
        if (f6 < f || f6 > f3) {
            return null;
        }
        float f7 = this.top;
        if (f7 < f2 || f7 > f4) {
            return null;
        }
        float f8 = this.bottom;
        if (f8 < f2 || f8 > f4) {
            return null;
        }
        return new RectFloat(this);
    }

    private RectFloat obtainRectWithInsidePoints(float f, float f2, float f3, float f4, List<float[]> list) {
        int size = list.size();
        if (size == 0) {
            return obtainInnerRect(f, f2, f3, f4);
        }
        if (size == 1) {
            float[] fArr = list.get(0);
            if (Float.compare(fArr[0], f) == 0) {
                if (Float.compare(fArr[1], f2) == 0) {
                    return new RectFloat(f, f2, this.right, this.bottom);
                }
                return new RectFloat(f, this.top, this.right, f4);
            } else if (Float.compare(fArr[1], f2) == 0) {
                return new RectFloat(this.left, f2, f3, this.bottom);
            } else {
                return new RectFloat(this.left, this.top, f3, f4);
            }
        } else if (size == 2) {
            float[] fArr2 = list.get(0);
            if (Float.compare(fArr2[0], list.get(1)[0]) == 0) {
                if (Float.compare(fArr2[0], f) == 0) {
                    return new RectFloat(f, f2, this.right, f4);
                }
                return new RectFloat(this.left, f2, f3, f4);
            } else if (Float.compare(fArr2[1], f2) == 0) {
                return new RectFloat(f, f2, f3, this.bottom);
            } else {
                return new RectFloat(f, this.top, f3, f4);
            }
        } else if (size != 4) {
            return null;
        } else {
            return new RectFloat(f, f2, f3, f4);
        }
    }

    private List<float[]> getPointsInThisRect(float f, float f2, float f3, float f4) {
        $$Lambda$RectFloat$6FNr1t1tGE82nw4w8OCLOR0kqKg r0 = new Predicate() {
            /* class ohos.agp.utils.$$Lambda$RectFloat$6FNr1t1tGE82nw4w8OCLOR0kqKg */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return RectFloat.this.lambda$getPointsInThisRect$0$RectFloat((float[]) obj);
            }
        };
        ArrayList arrayList = new ArrayList();
        float[] fArr = {f, f2};
        if (r0.test(fArr)) {
            arrayList.add(fArr);
        }
        float[] fArr2 = {f3, f2};
        if (r0.test(fArr2)) {
            arrayList.add(fArr2);
        }
        float[] fArr3 = {f, f4};
        if (r0.test(fArr3)) {
            arrayList.add(fArr3);
        }
        float[] fArr4 = {f3, f4};
        if (r0.test(fArr4)) {
            arrayList.add(fArr4);
        }
        return arrayList;
    }

    public /* synthetic */ boolean lambda$getPointsInThisRect$0$RectFloat(float[] fArr) {
        return fArr[0] >= this.left && fArr[0] <= this.right && fArr[1] >= this.top && fArr[1] <= this.bottom;
    }

    public RectFloat obtainIntersectRect(RectFloat rectFloat) {
        if (rectFloat != null) {
            return obtainIntersectRect(rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom);
        }
        return null;
    }
}
