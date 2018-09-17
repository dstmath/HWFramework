package android.companion;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.provider.OneTimeUseBuilder;
import java.util.Objects;
import java.util.regex.Pattern;

public final class WifiDeviceFilter implements DeviceFilter<ScanResult> {
    public static final Creator<WifiDeviceFilter> CREATOR = new Creator<WifiDeviceFilter>() {
        public WifiDeviceFilter createFromParcel(Parcel in) {
            return new WifiDeviceFilter(in, null);
        }

        public WifiDeviceFilter[] newArray(int size) {
            return new WifiDeviceFilter[size];
        }
    };
    private final Pattern mNamePattern;

    public static final class Builder extends OneTimeUseBuilder<WifiDeviceFilter> {
        private Pattern mNamePattern;

        public Builder setNamePattern(Pattern regex) {
            checkNotUsed();
            this.mNamePattern = regex;
            return this;
        }

        public WifiDeviceFilter build() {
            markUsed();
            return new WifiDeviceFilter(this.mNamePattern, null);
        }
    }

    private WifiDeviceFilter(Pattern namePattern) {
        this.mNamePattern = namePattern;
    }

    @SuppressLint({"ParcelClassLoader"})
    private WifiDeviceFilter(Parcel in) {
        this(BluetoothDeviceFilterUtils.patternFromString(in.readString()));
    }

    public Pattern getNamePattern() {
        return this.mNamePattern;
    }

    public boolean matches(ScanResult device) {
        return BluetoothDeviceFilterUtils.matchesName(getNamePattern(), device);
    }

    public String getDeviceDisplayName(ScanResult device) {
        return BluetoothDeviceFilterUtils.getDeviceDisplayNameInternal(device);
    }

    public int getMediumType() {
        return 2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.mNamePattern, ((WifiDeviceFilter) o).mNamePattern);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mNamePattern});
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BluetoothDeviceFilterUtils.patternToString(getNamePattern()));
    }

    public int describeContents() {
        return 0;
    }
}
