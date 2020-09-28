package android.view.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
public class ParcelableMap extends HashMap<AutofillId, AutofillValue> implements Parcelable {
    public static final Parcelable.Creator<ParcelableMap> CREATOR = new Parcelable.Creator<ParcelableMap>() {
        /* class android.view.autofill.ParcelableMap.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableMap createFromParcel(Parcel source) {
            int size = source.readInt();
            ParcelableMap map = new ParcelableMap(size);
            for (int i = 0; i < size; i++) {
                map.put((AutofillId) source.readParcelable(null), (AutofillValue) source.readParcelable(null));
            }
            return map;
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableMap[] newArray(int size) {
            return new ParcelableMap[size];
        }
    };

    ParcelableMap(int size) {
        super(size);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size());
        for (Map.Entry<AutofillId, AutofillValue> entry : entrySet()) {
            dest.writeParcelable(entry.getKey(), 0);
            dest.writeParcelable(entry.getValue(), 0);
        }
    }
}
