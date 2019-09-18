package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.HwLog;
import java.util.ArrayList;
import java.util.List;

public class CloseRangeEventFilter implements Parcelable {
    public static final Parcelable.Creator<CloseRangeEventFilter> CREATOR = new Parcelable.Creator<CloseRangeEventFilter>() {
        public CloseRangeEventFilter createFromParcel(Parcel source) {
            CloseRangeBusinessType businessType = CloseRangeBusinessType.valueOf(source.readString());
            List<CloseRangeConfigInstance> configInstances = new ArrayList<>();
            source.readTypedList(configInstances, CloseRangeConfigInstance.CREATOR);
            return new CloseRangeEventFilter(businessType, configInstances);
        }

        public CloseRangeEventFilter[] newArray(int size) {
            return new CloseRangeEventFilter[size];
        }
    };
    private static final String TAG = "CloseRangeEventFilter";
    private CloseRangeBusinessType businessType;
    private List<CloseRangeConfigInstance> configInstances;

    private CloseRangeEventFilter(CloseRangeBusinessType businessType2, List<CloseRangeConfigInstance> configInstances2) {
        this.businessType = businessType2;
        this.configInstances = new ArrayList();
        this.configInstances.addAll(configInstances2);
    }

    public static CloseRangeEventFilter buildFilter(CloseRangeBusinessType businessType2, List<CloseRangeConfigInstance> configInstances2) {
        if (businessType2 != null && configInstances2 != null) {
            return new CloseRangeEventFilter(businessType2, configInstances2);
        }
        HwLog.e(TAG, "error when build filter");
        return null;
    }

    public CloseRangeBusinessType getBusinessType() {
        return this.businessType;
    }

    public List<CloseRangeConfigInstance> getConfigInstances() {
        return this.configInstances;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.businessType.name());
        dest.writeTypedList(this.configInstances);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloseRangeEventFilter)) {
            return false;
        }
        return getBusinessType().equals(((CloseRangeEventFilter) o).getBusinessType());
    }

    public int hashCode() {
        return getBusinessType().getTag();
    }

    public String toString() {
        return "CloseRangeEventFilter{businessType=" + this.businessType + ", configInstances=" + this.configInstances + '}';
    }
}
