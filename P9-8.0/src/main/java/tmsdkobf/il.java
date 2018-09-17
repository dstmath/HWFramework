package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

public class il extends SparseArray<String> implements Parcelable {
    public static final Creator<il> CREATOR = new Creator<il>() {
        /* renamed from: a */
        public il createFromParcel(Parcel parcel) {
            int readInt = parcel.readInt();
            if (readInt >= 0) {
                return new il(parcel, readInt);
            }
            throw new IllegalArgumentException("negative size " + readInt);
        }

        /* renamed from: ad */
        public il[] newArray(int i) {
            return new il[i];
        }
    };

    public il(int i) {
        super(i);
    }

    protected il(Parcel parcel, int i) {
        this((i + 32) & -32);
        for (int i2 = 0; i2 < i; i2++) {
            put(parcel.readInt(), parcel.readString());
        }
    }

    public int describeContents() {
        return 0;
    }

    public synchronized void writeToParcel(Parcel parcel, int i) {
        int size = size();
        parcel.writeInt(size);
        for (int i2 = 0; i2 < size; i2++) {
            parcel.writeInt(keyAt(i2));
            parcel.writeString((String) valueAt(i2));
        }
    }
}
