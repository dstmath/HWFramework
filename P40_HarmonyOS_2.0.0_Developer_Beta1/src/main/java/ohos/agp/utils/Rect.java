package ohos.agp.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.Rect;
import ohos.devtools.JLogConstants;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class Rect implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "Rect");
    public int bottom;
    public int left;
    private int pivotX;
    private int pivotY;
    public int right;
    public int top;

    public Rect() {
        this(0, 0, 0, 0);
    }

    public Rect(int i, int i2, int i3, int i4) {
        this.left = i;
        this.bottom = i4;
        this.right = i3;
        this.top = i2;
    }

    public Rect(Rect rect) {
        if (rect != null) {
            this.left = rect.left;
            this.top = rect.top;
            this.right = rect.right;
            this.bottom = rect.bottom;
            return;
        }
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
    }

    public void set(int i, int i2, int i3, int i4) {
        this.left = i;
        this.bottom = i4;
        this.right = i3;
        this.top = i2;
    }

    public final boolean isEmpty() {
        return this.top >= this.bottom || this.left >= this.right;
    }

    public final boolean verifyRect() {
        return this.left < this.right && this.top < this.bottom;
    }

    public final int getWidth() {
        return this.right - this.left;
    }

    public final int getHeight() {
        return this.bottom - this.top;
    }

    public final int getCenterX() {
        return (this.left + this.right) >> 1;
    }

    public final int getCenterY() {
        return (this.top + this.bottom) >> 1;
    }

    public int[] getCenter() {
        return new int[]{getCenterX(), getCenterY()};
    }

    public int[] getRectSize() {
        return new int[]{getWidth(), getHeight()};
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Rect)) {
            return false;
        }
        return ((Rect) obj).hashCode() == hashCode();
    }

    public int hashCode() {
        return ((((((JLogConstants.JLID_FREQ_LIMIT_INFO + this.left) * 19) + this.top) * 19) + this.right) * 19) + this.bottom;
    }

    public String toString() {
        return "(" + this.left + "," + this.top + "," + this.right + "," + this.bottom + ")";
    }

    public boolean marshalling(Parcel parcel) {
        HiLog.debug(LABEL, "enter marshalling", new Object[0]);
        if (!parcel.writeInt(this.left) || !parcel.writeInt(this.top) || !parcel.writeInt(this.right) || !parcel.writeInt(this.bottom) || !parcel.writeInt(this.pivotX) || !parcel.writeInt(this.pivotY)) {
            return false;
        }
        HiLog.debug(LABEL, "marshalling succeed", new Object[0]);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        HiLog.debug(LABEL, "enter unmarshalling", new Object[0]);
        this.left = parcel.readInt();
        this.top = parcel.readInt();
        this.right = parcel.readInt();
        this.bottom = parcel.readInt();
        this.pivotX = parcel.readInt();
        this.pivotY = parcel.readInt();
        HiLog.debug(LABEL, "unmarshalling succeed", new Object[0]);
        return true;
    }

    public boolean isInclude(int i, int i2) {
        int i3;
        int i4;
        int i5 = this.left;
        int i6 = this.right;
        return i5 < i6 && (i3 = this.top) < (i4 = this.bottom) && i >= i5 && i < i6 && i2 >= i3 && i2 < i4;
    }

    public boolean isInclude(Point point) {
        return new RectFloat(this).isInclude(point);
    }

    public boolean isInclude(Rect rect) {
        if (rect == null) {
            return false;
        }
        return isInclude(rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean isInclude(int i, int i2, int i3, int i4) {
        if (i > i3 || i2 > i4 || !verifyRect() || i < this.left || i3 > this.right || i2 < this.top || i4 > this.bottom) {
            return false;
        }
        return true;
    }

    public final float getPreciseHorizontalCenter() {
        return new BigDecimal(this.left).add(new BigDecimal(this.right)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public final float getPreciseVerticalCenter() {
        return new BigDecimal(this.top).add(new BigDecimal(this.bottom)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public Point getPreciseCenter() {
        return new Point(getPreciseHorizontalCenter(), getPreciseVerticalCenter());
    }

    public void setPivot(int i, int i2) {
        this.pivotX = i;
        this.pivotY = i2;
    }

    public void setPivot(Point point) {
        if (point != null) {
            this.pivotX = point.getPointXToInt();
            this.pivotY = point.getPointYToInt();
        }
    }

    public void setPivotXCoordinate(int i) {
        this.pivotX = i;
    }

    public void setPivotYCoordinate(int i) {
        this.pivotY = i;
    }

    public Point getPivot() {
        return new Point((float) this.pivotX, (float) this.pivotY);
    }

    public int getPivotXCoordinate() {
        return this.pivotX;
    }

    public int getPivotYCoordinate() {
        return this.pivotY;
    }

    public void scale(float f) {
        int i = this.pivotX;
        this.left = ((int) (((float) (this.left - i)) * f)) + i;
        this.right = i + ((int) (((float) (this.right - i)) * f));
        int i2 = this.pivotY;
        this.top = ((int) (((float) (this.top - i2)) * f)) + i2;
        this.bottom = i2 + ((int) (((float) (this.bottom - i2)) * f));
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
                /* class ohos.agp.utils.$$Lambda$Rect$RotationEnum$_RY90A7obf356yh5lXWWqvPpQ */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return Rect.RotationEnum.lambda$getByInt$0(this.f$0, (Rect.RotationEnum) obj);
                }
            }).findAny().orElse(ROTATE_360);
        }

        static /* synthetic */ boolean lambda$getByInt$0(int i, RotationEnum rotationEnum) {
            return rotationEnum.getValue() == i;
        }
    }

    public void rotateBy(RotationEnum rotationEnum) {
        int i;
        int i2;
        int i3;
        int i4;
        if (rotationEnum != null) {
            int i5 = this.left;
            int i6 = this.top;
            int i7 = this.right;
            int i8 = this.bottom;
            int i9 = AnonymousClass1.$SwitchMap$ohos$agp$utils$Rect$RotationEnum[rotationEnum.ordinal()];
            if (i9 != 1) {
                if (i9 == 2) {
                    int i10 = this.pivotX;
                    i5 = (i10 + i10) - this.right;
                    int i11 = this.pivotY;
                    i3 = (i11 + i11) - this.bottom;
                    int i12 = (i10 + i10) - this.left;
                    i4 = (i11 + i11) - this.top;
                    i2 = i12;
                } else if (i9 != 3) {
                    i = i8;
                    i2 = i7;
                    i3 = i6;
                } else {
                    int i13 = this.top;
                    int i14 = this.pivotY;
                    int i15 = this.pivotX;
                    i3 = (i15 - this.right) + i14;
                    i2 = (this.bottom - i14) + i15;
                    i4 = i14 + (i15 - this.left);
                    i5 = (i13 - i14) + i15;
                }
                i = i4;
            } else {
                int i16 = this.pivotY;
                int i17 = this.pivotX;
                i5 = (i16 - this.bottom) + i17;
                i3 = (this.left - i17) + i16;
                i2 = (i16 - this.top) + i17;
                i = i16 + (this.right - i17);
            }
            this.left = i5;
            this.top = i3;
            this.right = i2;
            this.bottom = i;
        }
    }

    /* renamed from: ohos.agp.utils.Rect$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$utils$Rect$RotationEnum = new int[RotationEnum.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$utils$Rect$RotationEnum[RotationEnum.ROTATE_90.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$utils$Rect$RotationEnum[RotationEnum.ROTATE_180.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$utils$Rect$RotationEnum[RotationEnum.ROTATE_270.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public void shrink(int i, int i2) {
        this.left += i;
        this.right -= i;
        this.top += i2;
        this.bottom -= i2;
    }

    public static boolean isIntersect(Rect rect, Rect rect2) {
        if (rect == null || rect2 == null) {
            return false;
        }
        return rect.isIntersect(rect2);
    }

    public void translate(int i, int i2) {
        this.left += i;
        this.top += i2;
        this.bottom += i2;
        this.right += i;
    }

    public void translateTo(int i, int i2) {
        this.right += i - this.left;
        this.bottom += i2 - this.top;
        this.left = i;
        this.top = i2;
    }

    public void translateCenterTo(int i, int i2) {
        translate(i - getCenterX(), i2 - getCenterY());
    }

    public void translateCenterTo(Point point) {
        if (point != null) {
            translateCenterTo(point.getPointXToInt(), point.getPointYToInt());
        }
    }

    public void modify(Rect rect) {
        if (rect != null) {
            this.left = rect.left;
            this.right = rect.right;
            this.top = rect.top;
            this.bottom = rect.bottom;
        }
    }

    public void clear() {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
    }

    public void fuse(int i, int i2, int i3, int i4) {
        if (i < i3 && i2 < i4) {
            int i5 = this.left;
            if (i5 >= this.right || this.top >= this.bottom) {
                this.left = i;
                this.right = i3;
                this.top = i2;
                this.bottom = i4;
                return;
            }
            this.left = Math.min(i5, i);
            this.right = Math.max(this.right, i3);
            this.top = Math.min(this.top, i2);
            this.bottom = Math.max(this.bottom, i4);
        }
    }

    public void fuse(Rect rect) {
        if (rect != null) {
            fuse(rect.left, rect.top, rect.right, this.bottom);
        }
    }

    public boolean getIntersectRect(int i, int i2, int i3, int i4) {
        int i5 = this.left;
        if (i5 >= i3 || this.right <= i || this.top >= i4 || this.bottom <= i2) {
            return false;
        }
        this.left = Math.max(i5, i);
        this.top = Math.max(this.top, i2);
        this.right = Math.min(this.right, i3);
        this.bottom = Math.min(this.bottom, i4);
        return true;
    }

    public boolean getIntersectRect(Rect rect) {
        if (rect == null) {
            return false;
        }
        return getIntersectRect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean isIntersect(int i, int i2, int i3, int i4) {
        return obtainIntersectRect(i, i2, i3, i4) != null;
    }

    public boolean isIntersect(Rect rect) {
        return obtainIntersectRect(rect) != null;
    }

    public Rect obtainIntersectRect(int i, int i2, int i3, int i4) {
        if (i >= i3 || i2 >= i4 || !verifyRect()) {
            return null;
        }
        return obtainRectWithInsidePoints(i, i2, i3, i4, getPointsInThisRect(i, i2, i3, i4));
    }

    private Rect obtainRectWithInsidePoints(int i, int i2, int i3, int i4, List<int[]> list) {
        int size = list.size();
        if (size == 0) {
            return obtainInnerRect(i, i2, i3, i4);
        }
        if (size == 1) {
            int[] iArr = list.get(0);
            if (iArr[0] == i) {
                if (iArr[1] == i2) {
                    return new Rect(i, i2, this.right, this.bottom);
                }
                return new Rect(i, this.top, this.right, i4);
            } else if (iArr[1] == i2) {
                return new Rect(this.left, i2, i3, this.bottom);
            } else {
                return new Rect(this.left, this.top, i3, i4);
            }
        } else if (size == 2) {
            int[] iArr2 = list.get(0);
            if (iArr2[0] == list.get(1)[0]) {
                if (iArr2[0] == i) {
                    return new Rect(i, i2, this.right, i4);
                }
                return new Rect(this.left, i2, i3, i4);
            } else if (iArr2[1] == i2) {
                return new Rect(i, i2, i3, this.bottom);
            } else {
                return new Rect(i, this.top, i3, i4);
            }
        } else if (size != 4) {
            return null;
        } else {
            return new Rect(i, i2, i3, i4);
        }
    }

    private Rect obtainInnerRect(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8 = this.left;
        if (i8 < i || i8 > i3 || (i5 = this.right) < i || i5 > i3 || (i6 = this.top) < i2 || i6 > i4 || (i7 = this.bottom) < i2 || i7 > i4) {
            return null;
        }
        return new Rect(this);
    }

    private List<int[]> getPointsInThisRect(int i, int i2, int i3, int i4) {
        $$Lambda$Rect$nVKQ8FlHod4CiO9VvnYJFk9hhu4 r0 = new Predicate() {
            /* class ohos.agp.utils.$$Lambda$Rect$nVKQ8FlHod4CiO9VvnYJFk9hhu4 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return Rect.this.lambda$getPointsInThisRect$0$Rect((int[]) obj);
            }
        };
        ArrayList arrayList = new ArrayList();
        int[] iArr = {i, i2};
        if (r0.test(iArr)) {
            arrayList.add(iArr);
        }
        int[] iArr2 = {i3, i2};
        if (r0.test(iArr2)) {
            arrayList.add(iArr2);
        }
        int[] iArr3 = {i, i4};
        if (r0.test(iArr3)) {
            arrayList.add(iArr3);
        }
        int[] iArr4 = {i3, i4};
        if (r0.test(iArr4)) {
            arrayList.add(iArr4);
        }
        return arrayList;
    }

    public /* synthetic */ boolean lambda$getPointsInThisRect$0$Rect(int[] iArr) {
        return iArr[0] >= this.left && iArr[0] <= this.right && iArr[1] >= this.top && iArr[1] <= this.bottom;
    }

    public Rect obtainIntersectRect(Rect rect) {
        if (rect != null) {
            return obtainIntersectRect(rect.left, rect.top, rect.right, rect.bottom);
        }
        return null;
    }
}
