package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class WifiDisplay implements Parcelable {
    public static final Parcelable.Creator<WifiDisplay> CREATOR = new Parcelable.Creator<WifiDisplay>() {
        public WifiDisplay createFromParcel(Parcel in) {
            WifiDisplay wifiDisplay = new WifiDisplay(in.readString(), in.readString(), in.readString(), in.readInt() != 0, in.readInt() != 0, in.readInt() != 0);
            return wifiDisplay;
        }

        public WifiDisplay[] newArray(int size) {
            return size == 0 ? WifiDisplay.EMPTY_ARRAY : new WifiDisplay[size];
        }
    };
    public static final WifiDisplay[] EMPTY_ARRAY = new WifiDisplay[0];
    private final boolean mCanConnect;
    private final String mDeviceAddress;
    private final String mDeviceAlias;
    private final String mDeviceName;
    private final boolean mIsAvailable;
    private final boolean mIsRemembered;

    public WifiDisplay(String deviceAddress, String deviceName, String deviceAlias, boolean available, boolean canConnect, boolean remembered) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress must not be null");
        } else if (deviceName != null) {
            this.mDeviceAddress = deviceAddress;
            this.mDeviceName = deviceName;
            this.mDeviceAlias = deviceAlias;
            this.mIsAvailable = available;
            this.mCanConnect = canConnect;
            this.mIsRemembered = remembered;
        } else {
            throw new IllegalArgumentException("deviceName must not be null");
        }
    }

    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public String getDeviceAlias() {
        return this.mDeviceAlias;
    }

    public boolean isAvailable() {
        return this.mIsAvailable;
    }

    public boolean canConnect() {
        return this.mCanConnect;
    }

    public boolean isRemembered() {
        return this.mIsRemembered;
    }

    public String getFriendlyDisplayName() {
        return this.mDeviceAlias != null ? this.mDeviceAlias : this.mDeviceName;
    }

    public boolean equals(Object o) {
        return (o instanceof WifiDisplay) && equals((WifiDisplay) o);
    }

    public boolean equals(WifiDisplay other) {
        return other != null && this.mDeviceAddress.equals(other.mDeviceAddress) && this.mDeviceName.equals(other.mDeviceName) && Objects.equals(this.mDeviceAlias, other.mDeviceAlias);
    }

    public boolean hasSameAddress(WifiDisplay other) {
        return other != null && this.mDeviceAddress.equals(other.mDeviceAddress);
    }

    public int hashCode() {
        return this.mDeviceAddress.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceAddress);
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mDeviceAlias);
        dest.writeInt(this.mIsAvailable ? 1 : 0);
        dest.writeInt(this.mCanConnect ? 1 : 0);
        dest.writeInt(this.mIsRemembered ? 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String result = this.mDeviceName + " (" + this.mDeviceAddress + ")";
        if (this.mDeviceAlias != null) {
            result = result + ", alias " + this.mDeviceAlias;
        }
        return result + ", isAvailable " + this.mIsAvailable + ", canConnect " + this.mCanConnect + ", isRemembered " + this.mIsRemembered;
    }
}
