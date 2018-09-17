package android.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.PrintWriter;

public class Vr2dDisplayProperties implements Parcelable {
    public static final Creator<Vr2dDisplayProperties> CREATOR = new Creator<Vr2dDisplayProperties>() {
        public Vr2dDisplayProperties createFromParcel(Parcel source) {
            return new Vr2dDisplayProperties(source, null);
        }

        public Vr2dDisplayProperties[] newArray(int size) {
            return new Vr2dDisplayProperties[size];
        }
    };
    private final int mDpi;
    private final int mHeight;
    private final int mWidth;

    /* synthetic */ Vr2dDisplayProperties(Parcel source, Vr2dDisplayProperties -this1) {
        this(source);
    }

    public Vr2dDisplayProperties(int width, int height, int dpi) {
        this.mWidth = width;
        this.mHeight = height;
        this.mDpi = dpi;
    }

    public int hashCode() {
        return (((getWidth() * 31) + getHeight()) * 31) + getDpi();
    }

    public String toString() {
        return "Vr2dDisplayProperties{mWidth=" + this.mWidth + ", mHeight=" + this.mHeight + ", mDpi=" + this.mDpi + "}";
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vr2dDisplayProperties that = (Vr2dDisplayProperties) o;
        if (getWidth() != that.getWidth() || getHeight() != that.getHeight()) {
            return false;
        }
        if (getDpi() != that.getDpi()) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        dest.writeInt(this.mDpi);
    }

    private Vr2dDisplayProperties(Parcel source) {
        this.mWidth = source.readInt();
        this.mHeight = source.readInt();
        this.mDpi = source.readInt();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Vr2dDisplayProperties:");
        pw.println(prefix + "  width=" + this.mWidth);
        pw.println(prefix + "  height=" + this.mHeight);
        pw.println(prefix + "  dpi=" + this.mDpi);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getDpi() {
        return this.mDpi;
    }
}
