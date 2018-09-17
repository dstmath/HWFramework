package tmsdk.bg.module.wifidetect;

import android.content.Context;
import android.net.wifi.ScanResult;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.utils.f;
import tmsdk.common.utils.s;
import tmsdkobf.ic;

public class WifiDetectManager extends BaseManagerB {
    public static final int ARP_ERROR = 263;
    public static final int ARP_FAKE = 262;
    public static final int ARP_OK = 261;
    public static final int CLOUND_CHECK_DNS_FAKE = 18;
    public static final int CLOUND_CHECK_NETWORK_ERROR = 16;
    public static final int CLOUND_CHECK_NO_FAKE = 17;
    public static final int CLOUND_CHECK_PHISHING_FAKE = 19;
    public static final int NETWORK_AVILABLE = 1;
    public static final int NETWORK_NOTAVILABLE = 2;
    public static final int NETWORK_NOTAVILABLE_APPROVE = 3;
    public static final int SECURITY_EAP = 259;
    public static final int SECURITY_NONE = 256;
    public static final int SECURITY_PSK = 258;
    public static final int SECURITY_WEP = 257;
    private b wQ;

    public int detectARP(String str) {
        return !ic.bE() ? this.wQ.detectARP(str) : -1;
    }

    public int detectDnsAndPhishing(IWifiDetectListener iWifiDetectListener, long j) {
        Object obj = null;
        if (ic.bE()) {
            return -1;
        }
        if (iWifiDetectListener == null) {
            return -2;
        }
        if (j >= 2000) {
            obj = 1;
        }
        if (obj == null) {
            j = 180000;
        }
        return this.wQ.detectDnsAndPhishing(iWifiDetectListener, j);
    }

    public int detectNetworkState() {
        return !ic.bE() ? this.wQ.l("http://tools.3g.qq.com/wifi/cw.html", "Meri") : -1;
    }

    public int detectSecurity(ScanResult scanResult) {
        return !ic.bE() ? this.wQ.detectSecurity(scanResult) : -1;
    }

    public int free() {
        f.h("WifiDetectManager", "free");
        return !ic.bE() ? 0 : -1;
    }

    public int init() {
        f.h("WifiDetectManager", "init");
        s.bW(256);
        return !ic.bE() ? 0 : -1;
    }

    public void onCreate(Context context) {
        this.wQ = new b();
        this.wQ.onCreate(context);
        a(this.wQ);
    }
}
