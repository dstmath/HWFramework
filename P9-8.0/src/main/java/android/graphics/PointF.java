package android.graphics;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PointF implements Parcelable {
    public static final Creator<PointF> CREATOR = new Creator<PointF>() {
        public PointF createFromParcel(Parcel in) {
            PointF r = new PointF();
            r.readFromParcel(in);
            return r;
        }

        public PointF[] newArray(int size) {
            return new PointF[size];
        }
    };
    public float x;
    public float y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF(Point p) {
        this.x = (float) p.x;
        this.y = (float) p.y;
    }

    public final void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final void set(PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public final void offset(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public final boolean equals(float x, float y) {
        return this.x == x && this.y == y;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointF pointF = (PointF) o;
        return Float.compare(pointF.x, this.x) == 0 && Float.compare(pointF.y, this.y) == 0;
    }

    public int hashCode() {
        return ((this.x != TonemapCurve.LEVEL_BLACK ? Float.floatToIntBits(this.x) : 0) * 31) + (this.y != TonemapCurve.LEVEL_BLACK ? Float.floatToIntBits(this.y) : 0);
    }

    public String toString() {
        return "PointF(" + this.x + ", " + this.y + ")";
    }

    public final float length() {
        return length(this.x, this.y);
    }

    public static float length(float x, float y) {
        return (float) Math.hypot((double) x, (double) y);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
    }

    public void readFromParcel(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }
}
