package android.net;

import android.annotation.SystemApi;
import android.net.wifi.ParcelUtil;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import java.util.regex.Pattern;

@SystemApi
public class WifiKey implements Parcelable {
    private static final Pattern BSSID_PATTERN = Pattern.compile("([\\p{XDigit}]{2}:){5}[\\p{XDigit}]{2}");
    public static final Parcelable.Creator<WifiKey> CREATOR = new Parcelable.Creator<WifiKey>() {
        public WifiKey createFromParcel(Parcel in) {
            return new WifiKey(in);
        }

        public WifiKey[] newArray(int size) {
            return new WifiKey[size];
        }
    };
    private static final Pattern SSID_PATTERN = Pattern.compile("(\".*\")|(0x[\\p{XDigit}]+)", 32);
    public final String bssid;
    public final String ssid;

    public WifiKey(String ssid2, String bssid2) {
        if (ssid2 == null || !SSID_PATTERN.matcher(ssid2).matches()) {
            throw new IllegalArgumentException("Invalid ssid: " + ssid2);
        } else if (bssid2 == null || !BSSID_PATTERN.matcher(bssid2).matches()) {
            throw new IllegalArgumentException("Invalid bssid: " + bssid2);
        } else {
            this.ssid = ssid2;
            this.bssid = bssid2;
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
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WifiKey wifiKey = (WifiKey) o;
        if (!Objects.equals(this.ssid, wifiKey.ssid) || !Objects.equals(this.bssid, wifiKey.bssid)) {
            z = false;
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
