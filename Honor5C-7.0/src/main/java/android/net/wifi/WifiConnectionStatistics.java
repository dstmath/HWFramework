package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.HashMap;

public class WifiConnectionStatistics implements Parcelable {
    public static final Creator<WifiConnectionStatistics> CREATOR = null;
    private static final String TAG = "WifiConnnectionStatistics";
    public int num24GhzConnected;
    public int num5GhzConnected;
    public int numAutoJoinAttempt;
    public int numAutoRoamAttempt;
    public int numWifiManagerJoinAttempt;
    public HashMap<String, WifiNetworkConnectionStatistics> untrustedNetworkHistory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConnectionStatistics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConnectionStatistics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConnectionStatistics.<clinit>():void");
    }

    public WifiConnectionStatistics() {
        this.untrustedNetworkHistory = new HashMap();
    }

    public void incrementOrAddUntrusted(String SSID, int connection, int usage) {
        if (!TextUtils.isEmpty(SSID)) {
            WifiNetworkConnectionStatistics stats;
            if (this.untrustedNetworkHistory.containsKey(SSID)) {
                stats = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(SSID);
                if (stats != null) {
                    stats.numConnection += connection;
                    stats.numUsage += usage;
                }
            } else {
                stats = new WifiNetworkConnectionStatistics(connection, usage);
            }
            if (stats != null) {
                this.untrustedNetworkHistory.put(SSID, stats);
            }
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Connected on: 2.4Ghz=").append(this.num24GhzConnected);
        sbuf.append(" 5Ghz=").append(this.num5GhzConnected).append("\n");
        sbuf.append(" join=").append(this.numWifiManagerJoinAttempt);
        sbuf.append("\\").append(this.numAutoJoinAttempt).append("\n");
        sbuf.append(" roam=").append(this.numAutoRoamAttempt).append("\n");
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics stats = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(Key);
            if (stats != null) {
                sbuf.append(Key).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(stats.toString()).append("\n");
            }
        }
        return sbuf.toString();
    }

    public WifiConnectionStatistics(WifiConnectionStatistics source) {
        this.untrustedNetworkHistory = new HashMap();
        if (source != null) {
            this.untrustedNetworkHistory.putAll(source.untrustedNetworkHistory);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.num24GhzConnected);
        dest.writeInt(this.num5GhzConnected);
        dest.writeInt(this.numAutoJoinAttempt);
        dest.writeInt(this.numAutoRoamAttempt);
        dest.writeInt(this.numWifiManagerJoinAttempt);
        dest.writeInt(this.untrustedNetworkHistory.size());
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics num = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(Key);
            dest.writeString(Key);
            dest.writeInt(num.numConnection);
            dest.writeInt(num.numUsage);
        }
    }
}
