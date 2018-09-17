package android.content.pm;

import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

@Deprecated
public final class EphemeralIntentFilter implements Parcelable {
    public static final Creator<EphemeralIntentFilter> CREATOR = new Creator<EphemeralIntentFilter>() {
        public EphemeralIntentFilter createFromParcel(Parcel in) {
            return new EphemeralIntentFilter(in);
        }

        public EphemeralIntentFilter[] newArray(int size) {
            return new EphemeralIntentFilter[size];
        }
    };
    private final InstantAppIntentFilter mInstantAppIntentFilter;

    public EphemeralIntentFilter(String splitName, List<IntentFilter> filters) {
        this.mInstantAppIntentFilter = new InstantAppIntentFilter(splitName, filters);
    }

    EphemeralIntentFilter(InstantAppIntentFilter intentFilter) {
        this.mInstantAppIntentFilter = intentFilter;
    }

    EphemeralIntentFilter(Parcel in) {
        this.mInstantAppIntentFilter = (InstantAppIntentFilter) in.readParcelable(null);
    }

    public String getSplitName() {
        return this.mInstantAppIntentFilter.getSplitName();
    }

    public List<IntentFilter> getFilters() {
        return this.mInstantAppIntentFilter.getFilters();
    }

    InstantAppIntentFilter getInstantAppIntentFilter() {
        return this.mInstantAppIntentFilter;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.mInstantAppIntentFilter, flags);
    }
}
