package ohos.ai.cv.common;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CvPoint implements Sequenceable {
    public int x;
    public int y;

    public CvPoint() {
        this(0, 0);
    }

    public CvPoint(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public CvPoint(CvPoint cvPoint) {
        this(cvPoint.x, cvPoint.y);
    }

    public void set(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public final void offset(int i, int i2) {
        this.x += i;
        this.y += i2;
    }

    public final boolean equals(int i, int i2) {
        return this.x == i && this.y == i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CvPoint)) {
            return false;
        }
        CvPoint cvPoint = (CvPoint) obj;
        return this.x == cvPoint.x && this.y == cvPoint.y;
    }

    public int hashCode() {
        long doubleToLongBits = Double.doubleToLongBits((double) this.x);
        long doubleToLongBits2 = Double.doubleToLongBits((double) this.y);
        return ((((int) (doubleToLongBits ^ (doubleToLongBits >>> 32))) + 31) * 31) + ((int) ((doubleToLongBits2 >>> 32) ^ doubleToLongBits2));
    }

    public String toString() {
        return "CvPoint(" + this.x + ", " + this.y + ")";
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.x);
        parcel.writeInt(this.y);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.x = parcel.readInt();
        this.y = parcel.readInt();
        return true;
    }
}
