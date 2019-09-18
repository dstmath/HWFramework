package android.companion;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OneTimeUseBuilder;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AssociationRequest implements Parcelable {
    public static final Parcelable.Creator<AssociationRequest> CREATOR = new Parcelable.Creator<AssociationRequest>() {
        public AssociationRequest createFromParcel(Parcel in) {
            return new AssociationRequest(in);
        }

        public AssociationRequest[] newArray(int size) {
            return new AssociationRequest[size];
        }
    };
    private final List<DeviceFilter<?>> mDeviceFilters;
    private final boolean mSingleDevice;

    public static final class Builder extends OneTimeUseBuilder<AssociationRequest> {
        private ArrayList<DeviceFilter<?>> mDeviceFilters = null;
        private boolean mSingleDevice = false;

        public Builder setSingleDevice(boolean singleDevice) {
            checkNotUsed();
            this.mSingleDevice = singleDevice;
            return this;
        }

        public Builder addDeviceFilter(DeviceFilter<?> deviceFilter) {
            checkNotUsed();
            if (deviceFilter != null) {
                this.mDeviceFilters = ArrayUtils.add(this.mDeviceFilters, deviceFilter);
            }
            return this;
        }

        public AssociationRequest build() {
            markUsed();
            return new AssociationRequest(this.mSingleDevice, this.mDeviceFilters);
        }
    }

    private AssociationRequest(boolean singleDevice, List<DeviceFilter<?>> deviceFilters) {
        this.mSingleDevice = singleDevice;
        this.mDeviceFilters = CollectionUtils.emptyIfNull(deviceFilters);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    private AssociationRequest(Parcel in) {
        this(in.readByte() != 0, (List<DeviceFilter<?>>) in.readParcelableList(new ArrayList(), AssociationRequest.class.getClassLoader()));
    }

    public boolean isSingleDevice() {
        return this.mSingleDevice;
    }

    public List<DeviceFilter<?>> getDeviceFilters() {
        return this.mDeviceFilters;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssociationRequest that = (AssociationRequest) o;
        if (this.mSingleDevice != that.mSingleDevice || !Objects.equals(this.mDeviceFilters, that.mDeviceFilters)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.mSingleDevice), this.mDeviceFilters});
    }

    public String toString() {
        return "AssociationRequest{mSingleDevice=" + this.mSingleDevice + ", mDeviceFilters=" + this.mDeviceFilters + '}';
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mSingleDevice ? (byte) 1 : 0);
        dest.writeParcelableList(this.mDeviceFilters, flags);
    }

    public int describeContents() {
        return 0;
    }
}
