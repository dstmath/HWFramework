package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class IpConfiguration implements Parcelable {
    public static final Parcelable.Creator<IpConfiguration> CREATOR = new Parcelable.Creator<IpConfiguration>() {
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

    private void init(IpAssignment ipAssignment2, ProxySettings proxySettings2, StaticIpConfiguration staticIpConfiguration2, ProxyInfo httpProxy2) {
        this.ipAssignment = ipAssignment2;
        this.proxySettings = proxySettings2;
        ProxyInfo proxyInfo = null;
        this.staticIpConfiguration = staticIpConfiguration2 == null ? null : new StaticIpConfiguration(staticIpConfiguration2);
        if (httpProxy2 != null) {
            proxyInfo = new ProxyInfo(httpProxy2);
        }
        this.httpProxy = proxyInfo;
    }

    public IpConfiguration() {
        init(IpAssignment.UNASSIGNED, ProxySettings.UNASSIGNED, null, null);
    }

    public IpConfiguration(IpAssignment ipAssignment2, ProxySettings proxySettings2, StaticIpConfiguration staticIpConfiguration2, ProxyInfo httpProxy2) {
        init(ipAssignment2, proxySettings2, staticIpConfiguration2, httpProxy2);
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

    public void setIpAssignment(IpAssignment ipAssignment2) {
        this.ipAssignment = ipAssignment2;
    }

    public StaticIpConfiguration getStaticIpConfiguration() {
        return this.staticIpConfiguration;
    }

    public void setStaticIpConfiguration(StaticIpConfiguration staticIpConfiguration2) {
        this.staticIpConfiguration = staticIpConfiguration2;
    }

    public ProxySettings getProxySettings() {
        return this.proxySettings;
    }

    public void setProxySettings(ProxySettings proxySettings2) {
        this.proxySettings = proxySettings2;
    }

    public ProxyInfo getHttpProxy() {
        return this.httpProxy;
    }

    public void setHttpProxy(ProxyInfo httpProxy2) {
        this.httpProxy = httpProxy2;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("IP assignment: " + this.ipAssignment.toString());
        sbuf.append("\n");
        if (this.staticIpConfiguration != null) {
            sbuf.append("Static configuration: " + this.staticIpConfiguration.toString());
            sbuf.append("\n");
        }
        sbuf.append("Proxy settings: " + this.proxySettings.toString());
        sbuf.append("\n");
        if (this.httpProxy != null) {
            sbuf.append("HTTP proxy: " + this.httpProxy.toString());
            sbuf.append("\n");
        }
        return sbuf.toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof IpConfiguration)) {
            return false;
        }
        IpConfiguration other = (IpConfiguration) o;
        if (this.ipAssignment != other.ipAssignment || this.proxySettings != other.proxySettings || !Objects.equals(this.staticIpConfiguration, other.staticIpConfiguration) || !Objects.equals(this.httpProxy, other.httpProxy)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return 13 + (this.staticIpConfiguration != null ? this.staticIpConfiguration.hashCode() : 0) + (17 * this.ipAssignment.ordinal()) + (47 * this.proxySettings.ordinal()) + (83 * this.httpProxy.hashCode());
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
