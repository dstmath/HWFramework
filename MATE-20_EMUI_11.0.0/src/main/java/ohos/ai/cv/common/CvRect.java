package ohos.ai.cv.common;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CvRect implements Sequenceable {
    public int bottom;
    public int left;
    public int right;
    public int top;

    public CvRect() {
        this(0, 0, 0, 0);
    }

    public CvRect(int i, int i2, int i3, int i4) {
        this.top = i;
        this.left = i2;
        this.bottom = i3;
        this.right = i4;
    }

    public CvRect(CvRect cvRect) {
        this(cvRect.top, cvRect.left, cvRect.bottom, cvRect.right);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CvRect)) {
            return false;
        }
        CvRect cvRect = (CvRect) obj;
        return this.top == cvRect.top && this.left == cvRect.left && this.bottom == cvRect.bottom && this.right == cvRect.right;
    }

    public int hashCode() {
        long doubleToLongBits = Double.doubleToLongBits((double) this.top);
        long doubleToLongBits2 = Double.doubleToLongBits((double) this.left);
        int i = ((((int) (doubleToLongBits ^ (doubleToLongBits >>> 32))) + 31) * 31) + ((int) (doubleToLongBits2 ^ (doubleToLongBits2 >>> 32)));
        long doubleToLongBits3 = Double.doubleToLongBits((double) this.bottom);
        int i2 = (i * 31) + ((int) (doubleToLongBits3 ^ (doubleToLongBits3 >>> 32)));
        long doubleToLongBits4 = Double.doubleToLongBits((double) this.right);
        return (i2 * 31) + ((int) ((doubleToLongBits4 >>> 32) ^ doubleToLongBits4));
    }

    public String toString() {
        return "CvRect(" + this.top + ", " + this.left + ", " + this.bottom + ", " + this.right + ")";
    }

    public boolean isEmpty() {
        return this.top >= this.bottom || this.left >= this.right;
    }

    public int width() {
        int i = this.right;
        int i2 = this.left;
        if (i > i2) {
            return i - i2;
        }
        return 0;
    }

    public int height() {
        int i = this.bottom;
        int i2 = this.top;
        if (i > i2) {
            return i - i2;
        }
        return 0;
    }

    public void set(int i, int i2, int i3, int i4) {
        this.top = i;
        this.left = i2;
        this.bottom = i3;
        this.right = i4;
    }

    public void set(CvRect cvRect) {
        this.top = cvRect.top;
        this.left = cvRect.left;
        this.bottom = cvRect.bottom;
        this.right = cvRect.right;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.top);
        parcel.writeInt(this.left);
        parcel.writeInt(this.bottom);
        parcel.writeInt(this.right);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.top = parcel.readInt();
        this.left = parcel.readInt();
        this.bottom = parcel.readInt();
        this.right = parcel.readInt();
        return true;
    }
}
