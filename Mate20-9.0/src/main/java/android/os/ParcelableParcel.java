package android.os;

import android.os.Parcelable;
import android.util.MathUtils;

public class ParcelableParcel implements Parcelable {
    public static final Parcelable.ClassLoaderCreator<ParcelableParcel> CREATOR = new Parcelable.ClassLoaderCreator<ParcelableParcel>() {
        public ParcelableParcel createFromParcel(Parcel in) {
            return new ParcelableParcel(in, null);
        }

        public ParcelableParcel createFromParcel(Parcel in, ClassLoader loader) {
            return new ParcelableParcel(in, loader);
        }

        public ParcelableParcel[] newArray(int size) {
            return new ParcelableParcel[size];
        }
    };
    final ClassLoader mClassLoader;
    final Parcel mParcel = Parcel.obtain();

    public ParcelableParcel(ClassLoader loader) {
        this.mClassLoader = loader;
    }

    public ParcelableParcel(Parcel src, ClassLoader loader) {
        this.mClassLoader = loader;
        int size = src.readInt();
        if (size >= 0) {
            int pos = src.dataPosition();
            src.setDataPosition(MathUtils.addOrThrow(pos, size));
            this.mParcel.appendFrom(src, pos, size);
            return;
        }
        throw new IllegalArgumentException("Negative size read from parcel");
    }

    public Parcel getParcel() {
        this.mParcel.setDataPosition(0);
        return this.mParcel;
    }

    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mParcel.dataSize());
        dest.appendFrom(this.mParcel, 0, this.mParcel.dataSize());
    }
}
