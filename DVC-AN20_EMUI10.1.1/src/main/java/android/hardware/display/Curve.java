package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;

public final class Curve implements Parcelable {
    public static final Parcelable.Creator<Curve> CREATOR = new Parcelable.Creator<Curve>() {
        /* class android.hardware.display.Curve.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Curve createFromParcel(Parcel in) {
            return new Curve(in.createFloatArray(), in.createFloatArray());
        }

        @Override // android.os.Parcelable.Creator
        public Curve[] newArray(int size) {
            return new Curve[size];
        }
    };
    private final float[] mX;
    private final float[] mY;

    public Curve(float[] x, float[] y) {
        this.mX = x;
        this.mY = y;
    }

    public float[] getX() {
        return this.mX;
    }

    public float[] getY() {
        return this.mY;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloatArray(this.mX);
        out.writeFloatArray(this.mY);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        int size = this.mX.length;
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append("(");
            sb.append(this.mX[i]);
            sb.append(", ");
            sb.append(this.mY[i]);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
