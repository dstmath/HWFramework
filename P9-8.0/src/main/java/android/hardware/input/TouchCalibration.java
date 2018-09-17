package android.hardware.input;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TouchCalibration implements Parcelable {
    public static final Creator<TouchCalibration> CREATOR = new Creator<TouchCalibration>() {
        public TouchCalibration createFromParcel(Parcel in) {
            return new TouchCalibration(in);
        }

        public TouchCalibration[] newArray(int size) {
            return new TouchCalibration[size];
        }
    };
    public static final TouchCalibration IDENTITY = new TouchCalibration();
    private final float mXOffset;
    private final float mXScale;
    private final float mXYMix;
    private final float mYOffset;
    private final float mYScale;
    private final float mYXMix;

    public TouchCalibration() {
        this(1.0f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1.0f, TonemapCurve.LEVEL_BLACK);
    }

    public TouchCalibration(float xScale, float xyMix, float xOffset, float yxMix, float yScale, float yOffset) {
        this.mXScale = xScale;
        this.mXYMix = xyMix;
        this.mXOffset = xOffset;
        this.mYXMix = yxMix;
        this.mYScale = yScale;
        this.mYOffset = yOffset;
    }

    public TouchCalibration(Parcel in) {
        this.mXScale = in.readFloat();
        this.mXYMix = in.readFloat();
        this.mXOffset = in.readFloat();
        this.mYXMix = in.readFloat();
        this.mYScale = in.readFloat();
        this.mYOffset = in.readFloat();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mXScale);
        dest.writeFloat(this.mXYMix);
        dest.writeFloat(this.mXOffset);
        dest.writeFloat(this.mYXMix);
        dest.writeFloat(this.mYScale);
        dest.writeFloat(this.mYOffset);
    }

    public int describeContents() {
        return 0;
    }

    public float[] getAffineTransform() {
        return new float[]{this.mXScale, this.mXYMix, this.mXOffset, this.mYXMix, this.mYScale, this.mYOffset};
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TouchCalibration)) {
            return false;
        }
        TouchCalibration cal = (TouchCalibration) obj;
        if (cal.mXScale != this.mXScale || cal.mXYMix != this.mXYMix || cal.mXOffset != this.mXOffset || cal.mYXMix != this.mYXMix || cal.mYScale != this.mYScale) {
            z = false;
        } else if (cal.mYOffset != this.mYOffset) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((Float.floatToIntBits(this.mXScale) ^ Float.floatToIntBits(this.mXYMix)) ^ Float.floatToIntBits(this.mXOffset)) ^ Float.floatToIntBits(this.mYXMix)) ^ Float.floatToIntBits(this.mYScale)) ^ Float.floatToIntBits(this.mYOffset);
    }

    public String toString() {
        return String.format("[%f, %f, %f, %f, %f, %f]", new Object[]{Float.valueOf(this.mXScale), Float.valueOf(this.mXYMix), Float.valueOf(this.mXOffset), Float.valueOf(this.mYXMix), Float.valueOf(this.mYScale), Float.valueOf(this.mYOffset)});
    }
}
