package android.os;

import android.os.Parcelable.Creator;

public final class ParcelableException extends RuntimeException implements Parcelable {
    public static final Creator<ParcelableException> CREATOR = new Creator<ParcelableException>() {
        public ParcelableException createFromParcel(Parcel source) {
            return new ParcelableException(ParcelableException.readFromParcel(source));
        }

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
                return (Throwable) clazz.getConstructor(new Class[]{String.class}).newInstance(new Object[]{msg});
            }
        } catch (ReflectiveOperationException e) {
        }
        return new RuntimeException(name + ": " + msg);
    }

    public static void writeToParcel(Parcel out, Throwable t) {
        out.writeString(t.getClass().getName());
        out.writeString(t.getMessage());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest, getCause());
    }
}
