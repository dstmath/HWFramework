package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;

public class PPPOEConfig implements Parcelable {
    public static final Parcelable.Creator<PPPOEConfig> CREATOR = new Parcelable.Creator<PPPOEConfig>() {
        /* class android.net.wifi.PPPOEConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PPPOEConfig createFromParcel(Parcel source) {
            PPPOEConfig config = new PPPOEConfig();
            config.username = source.readString();
            config.password = source.readString();
            config.interf = source.readString();
            config.lcp_echo_interval = source.readInt();
            config.lcp_echo_failure = source.readInt();
            config.mtu = source.readInt();
            config.mru = source.readInt();
            config.timeout = source.readInt();
            config.MSS = source.readInt();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public PPPOEConfig[] newArray(int size) {
            return new PPPOEConfig[size];
        }
    };
    public int MSS = 1412;
    public String interf = SystemProperties.get("wifi.interface", "eth0");
    public int lcp_echo_failure = 3;
    public int lcp_echo_interval = 30;
    public int mru = 1480;
    public int mtu = 1480;
    public String password;
    public int timeout = 70;
    public String username;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.interf);
        dest.writeInt(this.lcp_echo_interval);
        dest.writeInt(this.lcp_echo_failure);
        dest.writeInt(this.mtu);
        dest.writeInt(this.mru);
        dest.writeInt(this.timeout);
        dest.writeInt(this.MSS);
    }

    public String[] getArgs() {
        return new String[]{this.username, this.password, this.interf, "" + this.lcp_echo_interval, "" + this.lcp_echo_failure, "" + this.mtu, "" + this.mru, "" + this.timeout, "" + this.MSS};
    }

    public String toString() {
        return "username=" + this.username + ",interf=" + this.interf + ",lcp_echo_interval=" + this.lcp_echo_interval + ",lcp_echo_failure=" + this.lcp_echo_failure + ",mtu=" + this.mtu + ",mru=" + this.mru + ",timeout=" + this.timeout + ",MSS=" + this.MSS;
    }
}
