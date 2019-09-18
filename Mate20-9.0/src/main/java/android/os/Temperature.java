package android.os;

import android.os.Parcelable;

public class Temperature implements Parcelable {
    public static final Parcelable.Creator<Temperature> CREATOR = new Parcelable.Creator<Temperature>() {
        public Temperature createFromParcel(Parcel p) {
            return new Temperature(p);
        }

        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };
    private int mType;
    private float mValue;

    public Temperature() {
        this(-3.4028235E38f, Integer.MIN_VALUE);
    }

    public Temperature(float value, int type) {
        this.mValue = value;
        this.mType = type;
    }

    public float getValue() {
        return this.mValue;
    }

    public int getType() {
        return this.mType;
    }

    private Temperature(Parcel p) {
        readFromParcel(p);
    }

    public void readFromParcel(Parcel p) {
        this.mValue = p.readFloat();
        this.mType = p.readInt();
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeFloat(this.mValue);
        p.writeInt(this.mType);
    }

    public int describeContents() {
        return 0;
    }
}
