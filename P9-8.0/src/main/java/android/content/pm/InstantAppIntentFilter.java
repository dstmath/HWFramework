package android.content.pm;

import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class InstantAppIntentFilter implements Parcelable {
    public static final Creator<InstantAppIntentFilter> CREATOR = new Creator<InstantAppIntentFilter>() {
        public InstantAppIntentFilter createFromParcel(Parcel in) {
            return new InstantAppIntentFilter(in);
        }

        public InstantAppIntentFilter[] newArray(int size) {
            return new InstantAppIntentFilter[size];
        }
    };
    private final List<IntentFilter> mFilters = new ArrayList();
    private final String mSplitName;

    public InstantAppIntentFilter(String splitName, List<IntentFilter> filters) {
        if (filters == null || filters.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.mSplitName = splitName;
        this.mFilters.addAll(filters);
    }

    InstantAppIntentFilter(Parcel in) {
        this.mSplitName = in.readString();
        in.readList(this.mFilters, null);
    }

    public String getSplitName() {
        return this.mSplitName;
    }

    public List<IntentFilter> getFilters() {
        return this.mFilters;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mSplitName);
        out.writeList(this.mFilters);
    }
}
