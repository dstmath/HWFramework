package android.net;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.util.Objects;

public class NetworkKey implements Parcelable {
    public static final Creator<NetworkKey> CREATOR = new Creator<NetworkKey>() {
        public NetworkKey createFromParcel(Parcel in) {
            return new NetworkKey(in, null);
        }

        public NetworkKey[] newArray(int size) {
            return new NetworkKey[size];
        }
    };
    private static final String TAG = "NetworkKey";
    public static final int TYPE_WIFI = 1;
    public final int type;
    public final WifiKey wifiKey;

    /* synthetic */ NetworkKey(Parcel in, NetworkKey -this1) {
        this(in);
    }

    public static NetworkKey createFromScanResult(ScanResult result) {
        if (!(result == null || result.wifiSsid == null)) {
            String ssid = result.wifiSsid.toString();
            String bssid = result.BSSID;
            if (!(TextUtils.isEmpty(ssid) || (ssid.equals(WifiSsid.NONE) ^ 1) == 0 || (TextUtils.isEmpty(bssid) ^ 1) == 0)) {
                try {
                    return new NetworkKey(new WifiKey(String.format("\"%s\"", new Object[]{ssid}), bssid));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Unable to create WifiKey.", e);
                    return null;
                }
            }
        }
        return null;
    }

    public static NetworkKey createFromWifiInfo(WifiInfo wifiInfo) {
        if (wifiInfo != null) {
            String ssid = wifiInfo.getSSID();
            String bssid = wifiInfo.getBSSID();
            if (!(TextUtils.isEmpty(ssid) || (ssid.equals(WifiSsid.NONE) ^ 1) == 0 || (TextUtils.isEmpty(bssid) ^ 1) == 0)) {
                try {
                    return new NetworkKey(new WifiKey(ssid, bssid));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Unable to create WifiKey.", e);
                    return null;
                }
            }
        }
        return null;
    }

    public NetworkKey(WifiKey wifiKey) {
        this.type = 1;
        this.wifiKey = wifiKey;
    }

    private NetworkKey(Parcel in) {
        this.type = in.readInt();
        switch (this.type) {
            case 1:
                this.wifiKey = (WifiKey) WifiKey.CREATOR.createFromParcel(in);
                return;
            default:
                throw new IllegalArgumentException("Parcel has unknown type: " + this.type);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.type);
        switch (this.type) {
            case 1:
                this.wifiKey.writeToParcel(out, flags);
                return;
            default:
                throw new IllegalStateException("NetworkKey has unknown type " + this.type);
        }
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkKey that = (NetworkKey) o;
        if (this.type == that.type) {
            z = Objects.equals(this.wifiKey, that.wifiKey);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.type), this.wifiKey});
    }

    public String toString() {
        switch (this.type) {
            case 1:
                return this.wifiKey.toString();
            default:
                return "InvalidKey";
        }
    }
}
