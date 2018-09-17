package android.support.v4.os;

import android.os.Parcel;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;

@Deprecated
public final class ParcelableCompat {

    static class ParcelableCompatCreatorHoneycombMR2<T> implements ClassLoaderCreator<T> {
        private final ParcelableCompatCreatorCallbacks<T> mCallbacks;

        ParcelableCompatCreatorHoneycombMR2(ParcelableCompatCreatorCallbacks<T> callbacks) {
            this.mCallbacks = callbacks;
        }

        public T createFromParcel(Parcel in) {
            return this.mCallbacks.createFromParcel(in, null);
        }

        public T createFromParcel(Parcel in, ClassLoader loader) {
            return this.mCallbacks.createFromParcel(in, loader);
        }

        public T[] newArray(int size) {
            return this.mCallbacks.newArray(size);
        }
    }

    @Deprecated
    public static <T> Creator<T> newCreator(ParcelableCompatCreatorCallbacks<T> callbacks) {
        return new ParcelableCompatCreatorHoneycombMR2(callbacks);
    }

    private ParcelableCompat() {
    }
}
