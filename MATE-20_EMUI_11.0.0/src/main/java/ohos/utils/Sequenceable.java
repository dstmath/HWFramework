package ohos.utils;

public interface Sequenceable {

    public interface Producer<T> {
        T createFromParcel(Parcel parcel);
    }

    default boolean hasFileDescriptor() {
        return false;
    }

    boolean marshalling(Parcel parcel);

    boolean unmarshalling(Parcel parcel);
}
