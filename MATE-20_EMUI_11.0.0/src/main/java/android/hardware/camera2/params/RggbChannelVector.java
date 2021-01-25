package android.hardware.camera2.params;

import com.android.internal.util.Preconditions;

public final class RggbChannelVector {
    public static final int BLUE = 3;
    public static final int COUNT = 4;
    public static final int GREEN_EVEN = 1;
    public static final int GREEN_ODD = 2;
    public static final int RED = 0;
    private final float mBlue;
    private final float mGreenEven;
    private final float mGreenOdd;
    private final float mRed;

    public RggbChannelVector(float red, float greenEven, float greenOdd, float blue) {
        this.mRed = Preconditions.checkArgumentFinite(red, "red");
        this.mGreenEven = Preconditions.checkArgumentFinite(greenEven, "greenEven");
        this.mGreenOdd = Preconditions.checkArgumentFinite(greenOdd, "greenOdd");
        this.mBlue = Preconditions.checkArgumentFinite(blue, "blue");
    }

    public final float getRed() {
        return this.mRed;
    }

    public float getGreenEven() {
        return this.mGreenEven;
    }

    public float getGreenOdd() {
        return this.mGreenOdd;
    }

    public float getBlue() {
        return this.mBlue;
    }

    public float getComponent(int colorChannel) {
        if (colorChannel < 0 || colorChannel >= 4) {
            throw new IllegalArgumentException("Color channel out of range");
        } else if (colorChannel == 0) {
            return this.mRed;
        } else {
            if (colorChannel == 1) {
                return this.mGreenEven;
            }
            if (colorChannel == 2) {
                return this.mGreenOdd;
            }
            if (colorChannel == 3) {
                return this.mBlue;
            }
            throw new AssertionError("Unhandled case " + colorChannel);
        }
    }

    public void copyTo(float[] destination, int offset) {
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length - offset >= 4) {
            destination[offset + 0] = this.mRed;
            destination[offset + 1] = this.mGreenEven;
            destination[offset + 2] = this.mGreenOdd;
            destination[offset + 3] = this.mBlue;
            return;
        }
        throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RggbChannelVector)) {
            return false;
        }
        RggbChannelVector other = (RggbChannelVector) obj;
        if (this.mRed == other.mRed && this.mGreenEven == other.mGreenEven && this.mGreenOdd == other.mGreenOdd && this.mBlue == other.mBlue) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((Float.floatToIntBits(this.mRed) ^ Float.floatToIntBits(this.mGreenEven)) ^ Float.floatToIntBits(this.mGreenOdd)) ^ Float.floatToIntBits(this.mBlue);
    }

    public String toString() {
        return String.format("RggbChannelVector%s", toShortString());
    }

    private String toShortString() {
        return String.format("{R:%f, G_even:%f, G_odd:%f, B:%f}", Float.valueOf(this.mRed), Float.valueOf(this.mGreenEven), Float.valueOf(this.mGreenOdd), Float.valueOf(this.mBlue));
    }
}
