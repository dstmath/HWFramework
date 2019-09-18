package android.companion;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.provider.OneTimeUseBuilder;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class BluetoothDeviceFilter implements DeviceFilter<BluetoothDevice> {
    public static final Parcelable.Creator<BluetoothDeviceFilter> CREATOR = new Parcelable.Creator<BluetoothDeviceFilter>() {
        public BluetoothDeviceFilter createFromParcel(Parcel in) {
            return new BluetoothDeviceFilter(in);
        }

        public BluetoothDeviceFilter[] newArray(int size) {
            return new BluetoothDeviceFilter[size];
        }
    };
    private final String mAddress;
    private final Pattern mNamePattern;
    private final List<ParcelUuid> mServiceUuidMasks;
    private final List<ParcelUuid> mServiceUuids;

    public static final class Builder extends OneTimeUseBuilder<BluetoothDeviceFilter> {
        private String mAddress;
        private Pattern mNamePattern;
        private ArrayList<ParcelUuid> mServiceUuid;
        private ArrayList<ParcelUuid> mServiceUuidMask;

        public Builder setNamePattern(Pattern regex) {
            checkNotUsed();
            this.mNamePattern = regex;
            return this;
        }

        public Builder setAddress(String address) {
            checkNotUsed();
            this.mAddress = address;
            return this;
        }

        public Builder addServiceUuid(ParcelUuid serviceUuid, ParcelUuid serviceUuidMask) {
            checkNotUsed();
            this.mServiceUuid = ArrayUtils.add(this.mServiceUuid, serviceUuid);
            this.mServiceUuidMask = ArrayUtils.add(this.mServiceUuidMask, serviceUuidMask);
            return this;
        }

        public BluetoothDeviceFilter build() {
            markUsed();
            BluetoothDeviceFilter bluetoothDeviceFilter = new BluetoothDeviceFilter(this.mNamePattern, this.mAddress, this.mServiceUuid, this.mServiceUuidMask);
            return bluetoothDeviceFilter;
        }
    }

    private BluetoothDeviceFilter(Pattern namePattern, String address, List<ParcelUuid> serviceUuids, List<ParcelUuid> serviceUuidMasks) {
        this.mNamePattern = namePattern;
        this.mAddress = address;
        this.mServiceUuids = CollectionUtils.emptyIfNull(serviceUuids);
        this.mServiceUuidMasks = CollectionUtils.emptyIfNull(serviceUuidMasks);
    }

    private BluetoothDeviceFilter(Parcel in) {
        this(BluetoothDeviceFilterUtils.patternFromString(in.readString()), in.readString(), readUuids(in), readUuids(in));
    }

    private static List<ParcelUuid> readUuids(Parcel in) {
        return in.readParcelableList(new ArrayList(), ParcelUuid.class.getClassLoader());
    }

    public boolean matches(BluetoothDevice device) {
        return BluetoothDeviceFilterUtils.matchesAddress(this.mAddress, device) && BluetoothDeviceFilterUtils.matchesServiceUuids(this.mServiceUuids, this.mServiceUuidMasks, device) && BluetoothDeviceFilterUtils.matchesName(getNamePattern(), device);
    }

    public String getDeviceDisplayName(BluetoothDevice device) {
        return BluetoothDeviceFilterUtils.getDeviceDisplayNameInternal(device);
    }

    public int getMediumType() {
        return 0;
    }

    public Pattern getNamePattern() {
        return this.mNamePattern;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public List<ParcelUuid> getServiceUuidMasks() {
        return this.mServiceUuidMasks;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BluetoothDeviceFilterUtils.patternToString(getNamePattern()));
        dest.writeString(this.mAddress);
        dest.writeParcelableList(this.mServiceUuids, flags);
        dest.writeParcelableList(this.mServiceUuidMasks, flags);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BluetoothDeviceFilter that = (BluetoothDeviceFilter) o;
        if (!Objects.equals(this.mNamePattern, that.mNamePattern) || !Objects.equals(this.mAddress, that.mAddress) || !Objects.equals(this.mServiceUuids, that.mServiceUuids) || !Objects.equals(this.mServiceUuidMasks, that.mServiceUuidMasks)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mNamePattern, this.mAddress, this.mServiceUuids, this.mServiceUuidMasks});
    }

    public int describeContents() {
        return 0;
    }
}
