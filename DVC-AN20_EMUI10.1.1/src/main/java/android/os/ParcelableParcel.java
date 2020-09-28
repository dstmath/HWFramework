package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcelable;
import android.util.MathUtils;

public class ParcelableParcel implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.ClassLoaderCreator<ParcelableParcel> CREATOR = new Parcelable.ClassLoaderCreator<ParcelableParcel>() {
        /* class android.os.ParcelableParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableParcel createFromParcel(Parcel in) {
            return new ParcelableParcel(in, null);
        }

        @Override // android.os.Parcelable.ClassLoaderCreator
        public ParcelableParcel createFromParcel(Parcel in, ClassLoader loader) {
            return new ParcelableParcel(in, loader);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableParcel[] newArray(int size) {
            return new ParcelableParcel[size];
        }
    };
    final ClassLoader mClassLoader;
    final Parcel mParcel = Parcel.obtain();

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public Parcel getParcel() {
        this.mParcel.setDataPosition(0);
        return this.mParcel;
    }

    @UnsupportedAppUsage
    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mParcel.dataSize());
        Parcel parcel = this.mParcel;
        dest.appendFrom(parcel, 0, parcel.dataSize());
    }
}
