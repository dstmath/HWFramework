package android.hardware.camera2.params;

import android.graphics.PointF;
import android.hardware.camera2.utils.HashCodeHelpers;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class TonemapCurve {
    public static final int CHANNEL_BLUE = 2;
    public static final int CHANNEL_GREEN = 1;
    public static final int CHANNEL_RED = 0;
    public static final float LEVEL_BLACK = 0.0f;
    public static final float LEVEL_WHITE = 1.0f;
    private static final int MIN_CURVE_LENGTH = 4;
    private static final int OFFSET_POINT_IN = 0;
    private static final int OFFSET_POINT_OUT = 1;
    public static final int POINT_SIZE = 2;
    private static final int TONEMAP_MIN_CURVE_POINTS = 2;
    private final float[] mBlue;
    private final float[] mGreen;
    private boolean mHashCalculated;
    private int mHashCode;
    private final float[] mRed;

    public TonemapCurve(float[] red, float[] green, float[] blue) {
        this.mHashCalculated = false;
        Preconditions.checkNotNull(red, "red must not be null");
        Preconditions.checkNotNull(green, "green must not be null");
        Preconditions.checkNotNull(blue, "blue must not be null");
        checkArgumentArrayLengthDivisibleBy(red, TONEMAP_MIN_CURVE_POINTS, "red");
        checkArgumentArrayLengthDivisibleBy(green, TONEMAP_MIN_CURVE_POINTS, "green");
        checkArgumentArrayLengthDivisibleBy(blue, TONEMAP_MIN_CURVE_POINTS, "blue");
        checkArgumentArrayLengthNoLessThan(red, MIN_CURVE_LENGTH, "red");
        checkArgumentArrayLengthNoLessThan(green, MIN_CURVE_LENGTH, "green");
        checkArgumentArrayLengthNoLessThan(blue, MIN_CURVE_LENGTH, "blue");
        Preconditions.checkArrayElementsInRange(red, LEVEL_BLACK, LEVEL_WHITE, "red");
        Preconditions.checkArrayElementsInRange(green, LEVEL_BLACK, LEVEL_WHITE, "green");
        Preconditions.checkArrayElementsInRange(blue, LEVEL_BLACK, LEVEL_WHITE, "blue");
        this.mRed = Arrays.copyOf(red, red.length);
        this.mGreen = Arrays.copyOf(green, green.length);
        this.mBlue = Arrays.copyOf(blue, blue.length);
    }

    private static void checkArgumentArrayLengthDivisibleBy(float[] array, int divisible, String arrayName) {
        if (array.length % divisible != 0) {
            throw new IllegalArgumentException(arrayName + " size must be divisible by " + divisible);
        }
    }

    private static int checkArgumentColorChannel(int colorChannel) {
        switch (colorChannel) {
            case OFFSET_POINT_IN /*0*/:
            case OFFSET_POINT_OUT /*1*/:
            case TONEMAP_MIN_CURVE_POINTS /*2*/:
                return colorChannel;
            default:
                throw new IllegalArgumentException("colorChannel out of range");
        }
    }

    private static void checkArgumentArrayLengthNoLessThan(float[] array, int minLength, String arrayName) {
        if (array.length < minLength) {
            throw new IllegalArgumentException(arrayName + " size must be at least " + minLength);
        }
    }

    public int getPointCount(int colorChannel) {
        checkArgumentColorChannel(colorChannel);
        return getCurve(colorChannel).length / TONEMAP_MIN_CURVE_POINTS;
    }

    public PointF getPoint(int colorChannel, int index) {
        checkArgumentColorChannel(colorChannel);
        if (index < 0 || index >= getPointCount(colorChannel)) {
            throw new IllegalArgumentException("index out of range");
        }
        float[] curve = getCurve(colorChannel);
        return new PointF(curve[(index * TONEMAP_MIN_CURVE_POINTS) + OFFSET_POINT_IN], curve[(index * TONEMAP_MIN_CURVE_POINTS) + OFFSET_POINT_OUT]);
    }

    public void copyColorCurve(int colorChannel, float[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length + offset < getPointCount(colorChannel) * TONEMAP_MIN_CURVE_POINTS) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        float[] curve = getCurve(colorChannel);
        System.arraycopy(curve, OFFSET_POINT_IN, destination, offset, curve.length);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TonemapCurve)) {
            return false;
        }
        TonemapCurve other = (TonemapCurve) obj;
        if (Arrays.equals(this.mRed, other.mRed) && Arrays.equals(this.mGreen, other.mGreen)) {
            z = Arrays.equals(this.mBlue, other.mBlue);
        }
        return z;
    }

    public int hashCode() {
        if (this.mHashCalculated) {
            return this.mHashCode;
        }
        this.mHashCode = HashCodeHelpers.hashCodeGeneric(this.mRed, this.mGreen, this.mBlue);
        this.mHashCalculated = true;
        return this.mHashCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("TonemapCurve{");
        sb.append("R:");
        sb.append(curveToString(OFFSET_POINT_IN));
        sb.append(", G:");
        sb.append(curveToString(OFFSET_POINT_OUT));
        sb.append(", B:");
        sb.append(curveToString(TONEMAP_MIN_CURVE_POINTS));
        sb.append("}");
        return sb.toString();
    }

    private String curveToString(int colorChannel) {
        checkArgumentColorChannel(colorChannel);
        StringBuilder sb = new StringBuilder("[");
        float[] curve = getCurve(colorChannel);
        int pointCount = curve.length / TONEMAP_MIN_CURVE_POINTS;
        int i = OFFSET_POINT_IN;
        int j = OFFSET_POINT_IN;
        while (i < pointCount) {
            sb.append("(");
            sb.append(curve[j]);
            sb.append(", ");
            sb.append(curve[j + OFFSET_POINT_OUT]);
            sb.append("), ");
            i += OFFSET_POINT_OUT;
            j += TONEMAP_MIN_CURVE_POINTS;
        }
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    private float[] getCurve(int colorChannel) {
        switch (colorChannel) {
            case OFFSET_POINT_IN /*0*/:
                return this.mRed;
            case OFFSET_POINT_OUT /*1*/:
                return this.mGreen;
            case TONEMAP_MIN_CURVE_POINTS /*2*/:
                return this.mBlue;
            default:
                throw new AssertionError("colorChannel out of range");
        }
    }
}
