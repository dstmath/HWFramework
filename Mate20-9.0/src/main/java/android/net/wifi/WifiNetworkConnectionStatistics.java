package android.net.wifi;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class WifiNetworkConnectionStatistics implements Parcelable {
    public static final Parcelable.Creator<WifiNetworkConnectionStatistics> CREATOR = new Parcelable.Creator<WifiNetworkConnectionStatistics>() {
        public WifiNetworkConnectionStatistics createFromParcel(Parcel in) {
            return new WifiNetworkConnectionStatistics(in.readInt(), in.readInt());
        }

        public WifiNetworkConnectionStatistics[] newArray(int size) {
            return new WifiNetworkConnectionStatistics[size];
        }
    };
    private static final String TAG = "WifiNetworkConnnectionStatistics";
    public int numConnection;
    public int numUsage;

    public WifiNetworkConnectionStatistics(int connection, int usage) {
        this.numConnection = connection;
        this.numUsage = usage;
    }

    public WifiNetworkConnectionStatistics() {
    }

    public String toString() {
        return "c=" + this.numConnection + " u=" + this.numUsage;
    }

    public WifiNetworkConnectionStatistics(WifiNetworkConnectionStatistics source) {
        this.numConnection = source.numConnection;
        this.numUsage = source.numUsage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
    }
}
