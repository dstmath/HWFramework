package android.hardware.camera2.params;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.util.Size;
import com.android.internal.util.Preconditions;

public final class MeteringRectangle {
    public static final int METERING_WEIGHT_DONT_CARE = 0;
    public static final int METERING_WEIGHT_MAX = 1000;
    public static final int METERING_WEIGHT_MIN = 0;
    private final int mHeight;
    private final int mWeight;
    private final int mWidth;
    private final int mX;
    private final int mY;

    public MeteringRectangle(int x, int y, int width, int height, int meteringWeight) {
        this.mX = Preconditions.checkArgumentNonnegative(x, "x must be nonnegative");
        this.mY = Preconditions.checkArgumentNonnegative(y, "y must be nonnegative");
        this.mWidth = Preconditions.checkArgumentNonnegative(width, "width must be nonnegative");
        this.mHeight = Preconditions.checkArgumentNonnegative(height, "height must be nonnegative");
        this.mWeight = Preconditions.checkArgumentInRange(meteringWeight, 0, 1000, "meteringWeight");
    }

    public MeteringRectangle(Point xy, Size dimensions, int meteringWeight) {
        Preconditions.checkNotNull(xy, "xy must not be null");
        Preconditions.checkNotNull(dimensions, "dimensions must not be null");
        this.mX = Preconditions.checkArgumentNonnegative(xy.x, "x must be nonnegative");
        this.mY = Preconditions.checkArgumentNonnegative(xy.y, "y must be nonnegative");
        this.mWidth = Preconditions.checkArgumentNonnegative(dimensions.getWidth(), "width must be nonnegative");
        this.mHeight = Preconditions.checkArgumentNonnegative(dimensions.getHeight(), "height must be nonnegative");
        this.mWeight = Preconditions.checkArgumentNonnegative(meteringWeight, "meteringWeight must be nonnegative");
    }

    public MeteringRectangle(Rect rect, int meteringWeight) {
        Preconditions.checkNotNull(rect, "rect must not be null");
        this.mX = Preconditions.checkArgumentNonnegative(rect.left, "rect.left must be nonnegative");
        this.mY = Preconditions.checkArgumentNonnegative(rect.top, "rect.top must be nonnegative");
        this.mWidth = Preconditions.checkArgumentNonnegative(rect.width(), "rect.width must be nonnegative");
        this.mHeight = Preconditions.checkArgumentNonnegative(rect.height(), "rect.height must be nonnegative");
        this.mWeight = Preconditions.checkArgumentNonnegative(meteringWeight, "meteringWeight must be nonnegative");
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getMeteringWeight() {
        return this.mWeight;
    }

    public Point getUpperLeftPoint() {
        return new Point(this.mX, this.mY);
    }

    public Size getSize() {
        return new Size(this.mWidth, this.mHeight);
    }

    public Rect getRect() {
        return new Rect(this.mX, this.mY, this.mX + this.mWidth, this.mY + this.mHeight);
    }

    public boolean equals(Object other) {
        return other instanceof MeteringRectangle ? equals((MeteringRectangle) other) : false;
    }

    public boolean equals(MeteringRectangle other) {
        boolean z = false;
        if (other == null) {
            return false;
        }
        if (this.mX == other.mX && this.mY == other.mY && this.mWidth == other.mWidth && this.mHeight == other.mHeight && this.mWeight == other.mWeight) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mX, this.mY, this.mWidth, this.mHeight, this.mWeight);
    }

    public String toString() {
        return String.format("(x:%d, y:%d, w:%d, h:%d, wt:%d)", new Object[]{Integer.valueOf(this.mX), Integer.valueOf(this.mY), Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mWeight)});
    }
}
