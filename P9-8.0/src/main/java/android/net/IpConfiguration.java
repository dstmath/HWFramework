package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class IpConfiguration implements Parcelable {
    public static final Creator<IpConfiguration> CREATOR = new Creator<IpConfiguration>() {
        public IpConfiguration createFromParcel(Parcel in) {
            IpConfiguration config = new IpConfiguration();
            config.ipAssignment = IpAssignment.valueOf(in.readString());
            config.proxySettings = ProxySettings.valueOf(in.readString());
            config.staticIpConfiguration = (StaticIpConfiguration) in.readParcelable(null);
            config.httpProxy = (ProxyInfo) in.readParcelable(null);
            return config;
        }

        public IpConfiguration[] newArray(int size) {
            return new IpConfiguration[size];
        }
    };
    private static final String TAG = "IpConfiguration";
    public ProxyInfo httpProxy;
    public IpAssignment ipAssignment;
    public ProxySettings proxySettings;
    public StaticIpConfiguration staticIpConfiguration;

    public enum IpAssignment {
        STATIC,
        DHCP,
        UNASSIGNED
    }

    public enum ProxySettings {
        NONE,
        STATIC,
        UNASSIGNED,
        PAC
    }

    private void init(IpAssignment ipAssignment, ProxySettings proxySettings, StaticIpConfiguration staticIpConfiguration, ProxyInfo httpProxy) {
        StaticIpConfiguration staticIpConfiguration2;
        ProxyInfo proxyInfo = null;
        this.ipAssignment = ipAssignment;
        this.proxySettings = proxySettings;
        if (staticIpConfiguration == null) {
            staticIpConfiguration2 = null;
        } else {
            staticIpConfiguration2 = new StaticIpConfiguration(staticIpConfiguration);
        }
        this.staticIpConfiguration = staticIpConfiguration2;
        if (httpProxy != null) {
            proxyInfo = new ProxyInfo(httpProxy);
        }
        this.httpProxy = proxyInfo;
    }

    public IpConfiguration() {
        init(IpAssignment.UNASSIGNED, ProxySettings.UNASSIGNED, null, null);
    }

    public IpConfiguration(IpAssignment ipAssignment, ProxySettings proxySettings, StaticIpConfiguration staticIpConfiguration, ProxyInfo httpProxy) {
        init(ipAssignment, proxySettings, staticIpConfiguration, httpProxy);
    }

    public IpConfiguration(IpConfiguration source) {
        this();
        if (source != null) {
            init(source.ipAssignment, source.proxySettings, source.staticIpConfiguration, source.httpProxy);
        }
    }

    public IpAssignment getIpAssignment() {
        return this.ipAssignment;
    }

    public void setIpAssignment(IpAssignment ipAssignment) {
        this.ipAssignment = ipAssignment;
    }

    public StaticIpConfiguration getStaticIpConfiguration() {
        return this.staticIpConfiguration;
    }

    public void setStaticIpConfiguration(StaticIpConfiguration staticIpConfiguration) {
        this.staticIpConfiguration = staticIpConfiguration;
    }

    public ProxySettings getProxySettings() {
        return this.proxySettings;
    }

    public void setProxySettings(ProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    public ProxyInfo getHttpProxy() {
        return this.httpProxy;
    }

    public void setHttpProxy(ProxyInfo httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("IP assignment: ").append(this.ipAssignment.toString());
        sbuf.append("\n");
        if (this.staticIpConfiguration != null) {
            sbuf.append("Static configuration: ").append(this.staticIpConfiguration.toString());
            sbuf.append("\n");
        }
        sbuf.append("Proxy settings: ").append(this.proxySettings.toString());
        sbuf.append("\n");
        if (this.httpProxy != null) {
            sbuf.append("HTTP proxy: ").append(this.httpProxy.toString());
            sbuf.append("\n");
        }
        return sbuf.toString();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof IpConfiguration)) {
            return false;
        }
        IpConfiguration other = (IpConfiguration) o;
        if (this.ipAssignment == other.ipAssignment && this.proxySettings == other.proxySettings && Objects.equals(this.staticIpConfiguration, other.staticIpConfiguration)) {
            z = Objects.equals(this.httpProxy, other.httpProxy);
        }
        return z;
    }

    public int hashCode() {
        return ((((this.staticIpConfiguration != null ? this.staticIpConfiguration.hashCode() : 0) + 13) + (this.ipAssignment.ordinal() * 17)) + (this.proxySettings.ordinal() * 47)) + (this.httpProxy.hashCode() * 83);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ipAssignment.name());
        dest.writeString(this.proxySettings.name());
        dest.writeParcelable(this.staticIpConfiguration, flags);
        dest.writeParcelable(this.httpProxy, flags);
    }
}
