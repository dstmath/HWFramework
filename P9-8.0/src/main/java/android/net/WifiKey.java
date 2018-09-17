package android.net;

import android.net.wifi.ParcelUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;
import java.util.regex.Pattern;

public class WifiKey implements Parcelable {
    private static final Pattern BSSID_PATTERN = Pattern.compile("([\\p{XDigit}]{2}:){5}[\\p{XDigit}]{2}");
    public static final Creator<WifiKey> CREATOR = new Creator<WifiKey>() {
        public WifiKey createFromParcel(Parcel in) {
            return new WifiKey(in, null);
        }

        public WifiKey[] newArray(int size) {
            return new WifiKey[size];
        }
    };
    private static final Pattern SSID_PATTERN = Pattern.compile("(\".*\")|(0x[\\p{XDigit}]+)", 32);
    public final String bssid;
    public final String ssid;

    public WifiKey(String ssid, String bssid) {
        if (ssid == null || (SSID_PATTERN.matcher(ssid).matches() ^ 1) != 0) {
            throw new IllegalArgumentException("Invalid ssid: " + ssid);
        } else if (bssid == null || (BSSID_PATTERN.matcher(bssid).matches() ^ 1) != 0) {
            throw new IllegalArgumentException("Invalid bssid: " + bssid);
        } else {
            this.ssid = ssid;
            this.bssid = bssid;
        }
    }

    private WifiKey(Parcel in) {
        this.ssid = in.readString();
        this.bssid = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ssid);
        out.writeString(this.bssid);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WifiKey wifiKey = (WifiKey) o;
        if (Objects.equals(this.ssid, wifiKey.ssid)) {
            z = Objects.equals(this.bssid, wifiKey.bssid);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.ssid, this.bssid});
    }

    public String toString() {
        return "WifiKey[SSID=" + this.ssid + ",BSSID=" + ParcelUtil.safeDisplayMac(this.bssid) + "]";
    }
}
