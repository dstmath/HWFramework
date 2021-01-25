package ohos.agp.utils;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class MimeData implements Sequenceable {
    public boolean marshalling(Parcel parcel) {
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        return false;
    }
}
