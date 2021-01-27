package android.os;

import android.os.Parcelable;

public final class ParcelableException extends RuntimeException implements Parcelable {
    public static final Parcelable.Creator<ParcelableException> CREATOR = new Parcelable.Creator<ParcelableException>() {
        /* class android.os.ParcelableException.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableException createFromParcel(Parcel source) {
            return new ParcelableException(ParcelableException.readFromParcel(source));
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableException[] newArray(int size) {
            return new ParcelableException[size];
        }
    };

    public ParcelableException(Throwable t) {
        super(t);
    }

    public <T extends Throwable> void maybeRethrow(Class<T> clazz) throws Throwable {
        if (clazz.isAssignableFrom(getCause().getClass())) {
            throw getCause();
        }
    }

    public static Throwable readFromParcel(Parcel in) {
        String name = in.readString();
        String msg = in.readString();
        try {
            Class<?> clazz = Class.forName(name, true, Parcelable.class.getClassLoader());
            if (Throwable.class.isAssignableFrom(clazz)) {
                return (Throwable) clazz.getConstructor(String.class).newInstance(msg);
            }
        } catch (ReflectiveOperationException e) {
        }
        return new RuntimeException(name + ": " + msg);
    }

    public static void writeToParcel(Parcel out, Throwable t) {
        out.writeString(t.getClass().getName());
        out.writeString(t.getMessage());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest, getCause());
    }
}
