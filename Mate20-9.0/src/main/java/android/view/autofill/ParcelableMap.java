package android.view.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

class ParcelableMap extends HashMap<AutofillId, AutofillValue> implements Parcelable {
    public static final Parcelable.Creator<ParcelableMap> CREATOR = new Parcelable.Creator<ParcelableMap>() {
        public ParcelableMap createFromParcel(Parcel source) {
            int size = source.readInt();
            ParcelableMap map = new ParcelableMap(size);
            for (int i = 0; i < size; i++) {
                map.put((AutofillId) source.readParcelable(null), (AutofillValue) source.readParcelable(null));
            }
            return map;
        }

        public ParcelableMap[] newArray(int size) {
            return new ParcelableMap[size];
        }
    };

    ParcelableMap(int size) {
        super(size);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size());
        for (Map.Entry<AutofillId, AutofillValue> entry : entrySet()) {
            dest.writeParcelable(entry.getKey(), 0);
            dest.writeParcelable(entry.getValue(), 0);
        }
    }
}
