package android.net.wifi;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;

public class PPPOEConfig implements Parcelable {
    public static final Creator<PPPOEConfig> CREATOR = null;
    public int MSS;
    public String interf;
    public int lcp_echo_failure;
    public int lcp_echo_interval;
    public int mru;
    public int mtu;
    public String password;
    public int timeout;
    public String username;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.PPPOEConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.PPPOEConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.PPPOEConfig.<clinit>():void");
    }

    public PPPOEConfig() {
        this.interf = SystemProperties.get("wifi.interface", "eth0");
        this.lcp_echo_interval = 30;
        this.lcp_echo_failure = 3;
        this.mtu = 1480;
        this.mru = 1480;
        this.timeout = 70;
        this.MSS = 1412;
    }

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
