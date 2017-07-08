package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class IpConfiguration implements Parcelable {
    public static final Creator<IpConfiguration> CREATOR = null;
    private static final String TAG = "IpConfiguration";
    public ProxyInfo httpProxy;
    public IpAssignment ipAssignment;
    public ProxySettings proxySettings;
    public StaticIpConfiguration staticIpConfiguration;

    public enum IpAssignment {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.IpConfiguration.IpAssignment.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.IpConfiguration.IpAssignment.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.IpConfiguration.IpAssignment.<clinit>():void");
        }
    }

    public enum ProxySettings {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.IpConfiguration.ProxySettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.IpConfiguration.ProxySettings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.IpConfiguration.ProxySettings.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.IpConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.IpConfiguration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.IpConfiguration.<clinit>():void");
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
