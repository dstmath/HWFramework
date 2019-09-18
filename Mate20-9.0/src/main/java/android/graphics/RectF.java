package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.FastMath;
import java.io.PrintWriter;

public class RectF implements Parcelable {
    public static final Parcelable.Creator<RectF> CREATOR = new Parcelable.Creator<RectF>() {
        public RectF createFromParcel(Parcel in) {
            RectF r = new RectF();
            r.readFromParcel(in);
            return r;
        }

        public RectF[] newArray(int size) {
            return new RectF[size];
        }
    };
    public float bottom;
    public float left;
    public float right;
    public float top;

    public RectF() {
    }

    public RectF(float left2, float top2, float right2, float bottom2) {
        this.left = left2;
        this.top = top2;
        this.right = right2;
        this.bottom = bottom2;
    }

    public RectF(RectF r) {
        if (r == null) {
            this.bottom = 0.0f;
            this.right = 0.0f;
            this.top = 0.0f;
            this.left = 0.0f;
            return;
        }
        this.left = r.left;
        this.top = r.top;
        this.right = r.right;
        this.bottom = r.bottom;
    }

    public RectF(Rect r) {
        if (r == null) {
            this.bottom = 0.0f;
            this.right = 0.0f;
            this.top = 0.0f;
            this.left = 0.0f;
            return;
        }
        this.left = (float) r.left;
        this.top = (float) r.top;
        this.right = (float) r.right;
        this.bottom = (float) r.bottom;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RectF r = (RectF) o;
        if (!(this.left == r.left && this.top == r.top && this.right == r.right && this.bottom == r.bottom)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int floatToIntBits = 31 * ((31 * ((31 * (this.left != 0.0f ? Float.floatToIntBits(this.left) : 0)) + (this.top != 0.0f ? Float.floatToIntBits(this.top) : 0))) + (this.right != 0.0f ? Float.floatToIntBits(this.right) : 0));
        if (this.bottom != 0.0f) {
            i = Float.floatToIntBits(this.bottom);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        return "RectF(" + this.left + ", " + this.top + ", " + this.right + ", " + this.bottom + ")";
    }

    public String toShortString() {
        return toShortString(new StringBuilder(32));
    }

    public String toShortString(StringBuilder sb) {
        sb.setLength(0);
        sb.append('[');
        sb.append(this.left);
        sb.append(',');
        sb.append(this.top);
        sb.append("][");
        sb.append(this.right);
        sb.append(',');
        sb.append(this.bottom);
        sb.append(']');
        return sb.toString();
    }

    public void printShortString(PrintWriter pw) {
        pw.print('[');
        pw.print(this.left);
        pw.print(',');
        pw.print(this.top);
        pw.print("][");
        pw.print(this.right);
        pw.print(',');
        pw.print(this.bottom);
        pw.print(']');
    }

    public final boolean isEmpty() {
        return this.left >= this.right || this.top >= this.bottom;
    }

    public final float width() {
        return this.right - this.left;
    }

    public final float height() {
        return this.bottom - this.top;
    }

    public final float centerX() {
        return (this.left + this.right) * 0.5f;
    }

    public final float centerY() {
        return (this.top + this.bottom) * 0.5f;
    }

    public void setEmpty() {
        this.bottom = 0.0f;
        this.top = 0.0f;
        this.right = 0.0f;
        this.left = 0.0f;
    }

    public void set(float left2, float top2, float right2, float bottom2) {
        this.left = left2;
        this.top = top2;
        this.right = right2;
        this.bottom = bottom2;
    }

    public void set(RectF src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }

    public void set(Rect src) {
        this.left = (float) src.left;
        this.top = (float) src.top;
        this.right = (float) src.right;
        this.bottom = (float) src.bottom;
    }

    public void offset(float dx, float dy) {
        this.left += dx;
        this.top += dy;
        this.right += dx;
        this.bottom += dy;
    }

    public void offsetTo(float newLeft, float newTop) {
        this.right += newLeft - this.left;
        this.bottom += newTop - this.top;
        this.left = newLeft;
        this.top = newTop;
    }

    public void inset(float dx, float dy) {
        this.left += dx;
        this.top += dy;
        this.right -= dx;
        this.bottom -= dy;
    }

    public boolean contains(float x, float y) {
        return this.left < this.right && this.top < this.bottom && x >= this.left && x < this.right && y >= this.top && y < this.bottom;
    }

    public boolean contains(float left2, float top2, float right2, float bottom2) {
        return this.left < this.right && this.top < this.bottom && this.left <= left2 && this.top <= top2 && this.right >= right2 && this.bottom >= bottom2;
    }

    public boolean contains(RectF r) {
        return this.left < this.right && this.top < this.bottom && this.left <= r.left && this.top <= r.top && this.right >= r.right && this.bottom >= r.bottom;
    }

    public boolean intersect(float left2, float top2, float right2, float bottom2) {
        if (this.left >= right2 || left2 >= this.right || this.top >= bottom2 || top2 >= this.bottom) {
            return false;
        }
        if (this.left < left2) {
            this.left = left2;
        }
        if (this.top < top2) {
            this.top = top2;
        }
        if (this.right > right2) {
            this.right = right2;
        }
        if (this.bottom > bottom2) {
            this.bottom = bottom2;
        }
        return true;
    }

    public boolean intersect(RectF r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    public boolean setIntersect(RectF a, RectF b) {
        if (a.left >= b.right || b.left >= a.right || a.top >= b.bottom || b.top >= a.bottom) {
            return false;
        }
        this.left = Math.max(a.left, b.left);
        this.top = Math.max(a.top, b.top);
        this.right = Math.min(a.right, b.right);
        this.bottom = Math.min(a.bottom, b.bottom);
        return true;
    }

    public boolean intersects(float left2, float top2, float right2, float bottom2) {
        return this.left < right2 && left2 < this.right && this.top < bottom2 && top2 < this.bottom;
    }

    public static boolean intersects(RectF a, RectF b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    }

    public void round(Rect dst) {
        dst.set(FastMath.round(this.left), FastMath.round(this.top), FastMath.round(this.right), FastMath.round(this.bottom));
    }

    public void roundOut(Rect dst) {
        dst.set((int) Math.floor((double) this.left), (int) Math.floor((double) this.top), (int) Math.ceil((double) this.right), (int) Math.ceil((double) this.bottom));
    }

    public void union(float left2, float top2, float right2, float bottom2) {
        if (left2 < right2 && top2 < bottom2) {
            if (this.left >= this.right || this.top >= this.bottom) {
                this.left = left2;
                this.top = top2;
                this.right = right2;
                this.bottom = bottom2;
                return;
            }
            if (this.left > left2) {
                this.left = left2;
            }
            if (this.top > top2) {
                this.top = top2;
            }
            if (this.right < right2) {
                this.right = right2;
            }
            if (this.bottom < bottom2) {
                this.bottom = bottom2;
            }
        }
    }

    public void union(RectF r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    public void union(float x, float y) {
        if (x < this.left) {
            this.left = x;
        } else if (x > this.right) {
            this.right = x;
        }
        if (y < this.top) {
            this.top = y;
        } else if (y > this.bottom) {
            this.bottom = y;
        }
    }

    public void sort() {
        if (this.left > this.right) {
            float temp = this.left;
            this.left = this.right;
            this.right = temp;
        }
        if (this.top > this.bottom) {
            float temp2 = this.top;
            this.top = this.bottom;
            this.bottom = temp2;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(this.left);
        out.writeFloat(this.top);
        out.writeFloat(this.right);
        out.writeFloat(this.bottom);
    }

    public void readFromParcel(Parcel in) {
        this.left = in.readFloat();
        this.top = in.readFloat();
        this.right = in.readFloat();
        this.bottom = in.readFloat();
    }

    public void scale(float scale) {
        if (scale != 1.0f) {
            this.left *= scale;
            this.top *= scale;
            this.right *= scale;
            this.bottom *= scale;
        }
    }
}
