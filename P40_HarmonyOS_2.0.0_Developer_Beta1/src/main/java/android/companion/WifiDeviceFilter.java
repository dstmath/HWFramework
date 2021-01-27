package android.companion;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OneTimeUseBuilder;
import java.util.Objects;
import java.util.regex.Pattern;

public final class WifiDeviceFilter implements DeviceFilter<ScanResult> {
    public static final Parcelable.Creator<WifiDeviceFilter> CREATOR = new Parcelable.Creator<WifiDeviceFilter>() {
        /* class android.companion.WifiDeviceFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiDeviceFilter createFromParcel(Parcel in) {
            return new WifiDeviceFilter(in);
        }

        @Override // android.os.Parcelable.Creator
        public WifiDeviceFilter[] newArray(int size) {
            return new WifiDeviceFilter[size];
        }
    };
    private final Pattern mNamePattern;

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

    @Override // android.companion.DeviceFilter
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
        return Objects.hash(this.mNamePattern);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BluetoothDeviceFilterUtils.patternToString(getNamePattern()));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static final class Builder extends OneTimeUseBuilder<WifiDeviceFilter> {
        private Pattern mNamePattern;

        public Builder setNamePattern(Pattern regex) {
            checkNotUsed();
            this.mNamePattern = regex;
            return this;
        }

        @Override // android.provider.OneTimeUseBuilder
        public WifiDeviceFilter build() {
            markUsed();
            return new WifiDeviceFilter(this.mNamePattern);
        }
    }
}
