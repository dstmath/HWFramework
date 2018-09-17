package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import java.util.Collections;
import java.util.List;

public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    public static final ClassLoaderCreator<ParceledListSlice> CREATOR = new ClassLoaderCreator<ParceledListSlice>() {
        public ParceledListSlice createFromParcel(Parcel in) {
            return new ParceledListSlice(in, null, null);
        }

        public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            return new ParceledListSlice(in, loader, null);
        }

        public ParceledListSlice[] newArray(int size) {
            return new ParceledListSlice[size];
        }
    };

    /* synthetic */ ParceledListSlice(Parcel in, ClassLoader loader, ParceledListSlice -this2) {
        this(in, loader);
    }

    public ParceledListSlice(List<T> list) {
        super(list);
    }

    private ParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        return new ParceledListSlice(Collections.emptyList());
    }

    public int describeContents() {
        int contents = 0;
        List<T> list = getList();
        for (int i = 0; i < list.size(); i++) {
            contents |= ((Parcelable) list.get(i)).describeContents();
        }
        return contents;
    }

    protected void writeElement(T parcelable, Parcel dest, int callFlags) {
        parcelable.writeToParcel(dest, callFlags);
    }

    protected void writeParcelableCreator(T parcelable, Parcel dest) {
        dest.writeParcelableCreator(parcelable);
    }

    protected Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        return from.readParcelableCreator(loader);
    }
}
