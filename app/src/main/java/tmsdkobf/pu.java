package tmsdkobf;

import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class pu {
    public static on IE;
    private static pu II;
    public int AR;
    public long IF;
    public String IG;
    public boolean IH;
    public String ja;
    public int port;
    public int v;

    public pu() {
        this.ja = "";
        this.port = 0;
        this.v = 0;
        this.IF = 0;
        this.AR = 0;
        this.IG = "";
        this.IH = false;
    }

    public static void a(pu puVar) {
        II = puVar;
    }

    public static pu hm() {
        if (II == null) {
            II = new pu();
        }
        return II;
    }

    public static void release() {
        a(null);
    }

    public void hn() {
        if (IE != null) {
            d.d("TcpConnectInfo", this.ja + "|" + String.valueOf(this.port) + "|" + String.valueOf(this.v) + "|" + String.valueOf(this.IF) + "|" + String.valueOf(this.AR) + "|" + this.IG + "|" + String.valueOf(this.IH));
            IE.b(this.ja, String.valueOf(this.port), String.valueOf(this.v), String.valueOf(this.IF), String.valueOf(this.AR), this.IG, String.valueOf(this.IH));
        }
    }
}
