package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;

public class PointF implements Parcelable {
    public static final Parcelable.Creator<PointF> CREATOR = new Parcelable.Creator<PointF>() {
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

    public PointF() {
    }

    public PointF(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public PointF(Point p) {
        this.x = (float) p.x;
        this.y = (float) p.y;
    }

    public final void set(float x2, float y2) {
        this.x = x2;
        this.y = y2;
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

    public final boolean equals(float x2, float y2) {
        return this.x == x2 && this.y == y2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointF pointF = (PointF) o;
        if (Float.compare(pointF.x, this.x) == 0 && Float.compare(pointF.y, this.y) == 0) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int floatToIntBits = 31 * (this.x != 0.0f ? Float.floatToIntBits(this.x) : 0);
        if (this.y != 0.0f) {
            i = Float.floatToIntBits(this.y);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        return "PointF(" + this.x + ", " + this.y + ")";
    }

    public final float length() {
        return length(this.x, this.y);
    }

    public static float length(float x2, float y2) {
        return (float) Math.hypot((double) x2, (double) y2);
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
