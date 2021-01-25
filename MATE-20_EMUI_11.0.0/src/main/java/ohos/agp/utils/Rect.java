package ohos.agp.utils;

import java.math.BigDecimal;
import ohos.aafwk.utils.log.LogDomain;
import ohos.devtools.JLogConstants;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class Rect implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "Rect");
    public int bottom;
    public int left;
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
        if (!parcel.writeInt(this.left) || !parcel.writeInt(this.top) || !parcel.writeInt(this.right) || !parcel.writeInt(this.bottom)) {
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

    public boolean isInclude(Rect rect) {
        int i;
        int i2;
        int i3;
        int i4;
        if (rect != null && (i = this.left) < (i2 = this.right) && (i3 = this.top) < (i4 = this.bottom) && rect.left >= i && rect.right <= i2 && rect.top >= i3 && rect.bottom <= i4) {
            return true;
        }
        return false;
    }

    public final float getPreciseHorizontalCenter() {
        return new BigDecimal(this.left).add(new BigDecimal(this.right)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public final float getPreciseVerticalCenter() {
        return new BigDecimal(this.top).add(new BigDecimal(this.bottom)).multiply(new BigDecimal(0.5d)).floatValue();
    }

    public void shrink(int i, int i2) {
        this.left += i;
        this.right -= i;
        this.top += i2;
        this.bottom -= i2;
    }

    public static boolean isIntersect(Rect rect, Rect rect2) {
        return rect != null && rect2 != null && rect.left < rect2.right && rect2.left < rect.right && rect.top < rect2.bottom && rect2.top < rect.bottom;
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
        return this.left < i3 && this.right > i && this.top < i4 && this.bottom > i2;
    }
}
