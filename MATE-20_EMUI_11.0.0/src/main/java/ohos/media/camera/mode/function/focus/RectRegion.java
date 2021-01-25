package ohos.media.camera.mode.function.focus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;
import ohos.agp.utils.Rect;

public class RectRegion {
    public static final int MAX_WEIGHT = 1000;
    private final int height;
    private final int weight;
    private final int width;
    private final int x;
    private final int y;

    @Retention(RetentionPolicy.SOURCE)
    private @interface RegionIndex {
        public static final int MAX = 5;
        public static final int WEIGHT = 4;
        public static final int X_BOUND = 2;
        public static final int X_START = 0;
        public static final int Y_BOUND = 3;
        public static final int Y_START = 1;
    }

    public RectRegion(int i, int i2, int i3, int i4, int i5) {
        this.x = i;
        this.y = i2;
        this.width = i3;
        this.height = i4;
        this.weight = i5;
    }

    public static RectRegion fromRect(Rect rect) {
        if (rect == null) {
            return new RectRegion(0, 0, 0, 0, 0);
        }
        return new RectRegion(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, 1000);
    }

    public static RectRegion fromIntArray(int[] iArr) {
        if (iArr == null || iArr.length < 5) {
            return null;
        }
        return new RectRegion(iArr[0], iArr[1], iArr[2] - iArr[0], iArr[3] - iArr[1], iArr[4]);
    }

    public int[] toIntArray() {
        int i = this.x;
        int i2 = this.y;
        return new int[]{i, i2, i + this.width, i2 + this.height, this.weight};
    }

    public boolean hasNegativeValue() {
        return this.x < 0 || this.y < 0 || this.width < 0 || this.height < 0;
    }

    public boolean equals(Object obj) {
        return (obj instanceof RectRegion) && equals((RectRegion) obj);
    }

    private boolean equals(RectRegion rectRegion) {
        return rectRegion != null && this.x == rectRegion.x && this.y == rectRegion.y && this.width == rectRegion.width && this.height == rectRegion.height && this.weight == rectRegion.weight;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.width), Integer.valueOf(this.height), Integer.valueOf(this.weight));
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "(x:%d, y:%d, w:%d, h:%d, wt:%d)", Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.width), Integer.valueOf(this.height), Integer.valueOf(this.weight));
    }
}
