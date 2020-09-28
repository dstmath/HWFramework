package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.util.Size;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

public class Point implements Parcelable {
    public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
        /* class android.graphics.Point.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Point createFromParcel(Parcel in) {
            Point r = new Point();
            r.readFromParcel(in);
            return r;
        }

        @Override // android.os.Parcelable.Creator
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };
    public int x;
    public int y;

    public Point() {
    }

    public Point(int x2, int y2) {
        this.x = x2;
        this.y = y2;
    }

    public Point(Point src) {
        this.x = src.x;
        this.y = src.y;
    }

    public void set(int x2, int y2) {
        this.x = x2;
        this.y = y2;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public final void offset(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public final boolean equals(int x2, int y2) {
        return this.x == x2 && this.y == y2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        if (this.x == point.x && this.y == point.y) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.x * 31) + this.y;
    }

    public String toString() {
        return "Point(" + this.x + ", " + this.y + ")";
    }

    public void printShortString(PrintWriter pw) {
        pw.print("[");
        pw.print(this.x);
        pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
        pw.print(this.y);
        pw.print("]");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.x);
        out.writeInt(this.y);
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        protoOutputStream.write(1120986464257L, this.x);
        protoOutputStream.write(1120986464258L, this.y);
        protoOutputStream.end(token);
    }

    public void readFromParcel(Parcel in) {
        this.x = in.readInt();
        this.y = in.readInt();
    }

    public static Point convert(Size size) {
        return new Point(size.getWidth(), size.getHeight());
    }

    public static Size convert(Point point) {
        return new Size(point.x, point.y);
    }
}
