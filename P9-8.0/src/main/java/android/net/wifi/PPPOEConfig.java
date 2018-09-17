package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;

public class PPPOEConfig implements Parcelable {
    public static final Creator<PPPOEConfig> CREATOR = new Creator<PPPOEConfig>() {
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

    public int describeContents() {
        return 0;
    }

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
        return new String[]{this.username, this.password, this.interf, ProxyInfo.LOCAL_EXCL_LIST + this.lcp_echo_interval, ProxyInfo.LOCAL_EXCL_LIST + this.lcp_echo_failure, ProxyInfo.LOCAL_EXCL_LIST + this.mtu, ProxyInfo.LOCAL_EXCL_LIST + this.mru, ProxyInfo.LOCAL_EXCL_LIST + this.timeout, ProxyInfo.LOCAL_EXCL_LIST + this.MSS};
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("username=").append(this.username).append(",interf=").append(this.interf).append(",lcp_echo_interval=").append(this.lcp_echo_interval).append(",lcp_echo_failure=").append(this.lcp_echo_failure).append(",mtu=").append(this.mtu).append(",mru=").append(this.mru).append(",timeout=").append(this.timeout).append(",MSS=").append(this.MSS);
        return strBuilder.toString();
    }
}
