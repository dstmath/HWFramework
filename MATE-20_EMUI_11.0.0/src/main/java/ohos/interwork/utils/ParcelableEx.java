package ohos.interwork.utils;

import ohos.utils.Parcel;
import ohos.utils.ParcelException;

public interface ParcelableEx {
    void marshallingEx(Parcel parcel) throws ParcelException;

    default void unmarshallingEx(Parcel parcel) throws ParcelException {
        throw new ParcelException("unmarshallingEx no implementation.");
    }
}
