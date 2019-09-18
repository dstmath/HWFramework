package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collections;
import java.util.List;

public class StringParceledListSlice extends BaseParceledListSlice<String> {
    public static final Parcelable.ClassLoaderCreator<StringParceledListSlice> CREATOR = new Parcelable.ClassLoaderCreator<StringParceledListSlice>() {
        public StringParceledListSlice createFromParcel(Parcel in) {
            return new StringParceledListSlice(in, null);
        }

        public StringParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            return new StringParceledListSlice(in, loader);
        }

        public StringParceledListSlice[] newArray(int size) {
            return new StringParceledListSlice[size];
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

    public StringParceledListSlice(List<String> list) {
        super(list);
    }

    private StringParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static StringParceledListSlice emptyList() {
        return new StringParceledListSlice(Collections.emptyList());
    }

    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void writeElement(String parcelable, Parcel reply, int callFlags) {
        reply.writeString(parcelable);
    }

    /* access modifiers changed from: protected */
    public void writeParcelableCreator(String parcelable, Parcel dest) {
    }

    /* access modifiers changed from: protected */
    public Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        return Parcel.STRING_CREATOR;
    }
}
