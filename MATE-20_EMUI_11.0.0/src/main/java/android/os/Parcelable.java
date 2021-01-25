package android.os;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Parcelable {
    public static final int CONTENTS_FILE_DESCRIPTOR = 1;
    public static final int PARCELABLE_ELIDE_DUPLICATES = 2;
    public static final int PARCELABLE_WRITE_RETURN_VALUE = 1;

    public interface ClassLoaderCreator<T> extends Creator<T> {
        T createFromParcel(Parcel parcel, ClassLoader classLoader);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentsFlags {
    }

    public interface Creator<T> {
        T createFromParcel(Parcel parcel);

        T[] newArray(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WriteFlags {
    }

    int describeContents();

    void writeToParcel(Parcel parcel, int i);
}
