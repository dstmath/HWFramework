package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collections;
import java.util.List;

public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR = new Parcelable.ClassLoaderCreator<ParceledListSlice>() {
        public ParceledListSlice createFromParcel(Parcel in) {
            return new ParceledListSlice(in, null);
        }

        public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            return new ParceledListSlice(in, loader);
        }

        public ParceledListSlice[] newArray(int size) {
            return new ParceledListSlice[size];
        }
    };

    public /* bridge */ /* synthetic */ List getList() {
        return super.getList();
    }

    public /* bridge */ /* synthetic */ void setInlineCountLimit(int i) {
        super.setInlineCountLimit(i);
    }

    public /* bridge */ /* synthetic */ void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }

    public ParceledListSlice(List<T> list) {
        super(list);
    }

    private ParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        return new ParceledListSlice<>(Collections.emptyList());
    }

    public int describeContents() {
        int contents = 0;
        List<T> list = getList();
        for (int i = 0; i < list.size(); i++) {
            contents |= ((Parcelable) list.get(i)).describeContents();
        }
        return contents;
    }

    /* access modifiers changed from: protected */
    public void writeElement(T parcelable, Parcel dest, int callFlags) {
        parcelable.writeToParcel(dest, callFlags);
    }

    /* access modifiers changed from: protected */
    public void writeParcelableCreator(T parcelable, Parcel dest) {
        dest.writeParcelableCreator(parcelable);
    }

    /* access modifiers changed from: protected */
    public Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        return from.readParcelableCreator(loader);
    }
}
