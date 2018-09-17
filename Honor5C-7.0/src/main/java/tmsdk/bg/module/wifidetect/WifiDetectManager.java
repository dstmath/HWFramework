package tmsdk.bg.module.wifidetect;

import android.content.Context;
import android.net.wifi.ScanResult;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.utils.d;
import tmsdk.common.utils.m;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
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
    private b zF;

    public int detectARP(String str) {
        return !jg.cl() ? this.zF.detectARP(str) : -1;
    }

    public int detectDnsAndPhishing(IWifiDetectListener iWifiDetectListener) {
        if (jg.cl()) {
            return -1;
        }
        if (iWifiDetectListener == null) {
            return -2;
        }
        ma.bx(29966);
        return this.zF.detectDnsAndPhishing(iWifiDetectListener);
    }

    public int detectNetworkState() {
        return !jg.cl() ? this.zF.o("http://tools.3g.qq.com/wifi/cw.html", "Meri") : -1;
    }

    public int detectSecurity(ScanResult scanResult) {
        return !jg.cl() ? this.zF.detectSecurity(scanResult) : -1;
    }

    public int free() {
        d.g("WifiDetectManager", "free");
        return !jg.cl() ? 0 : -1;
    }

    public int init() {
        d.g("WifiDetectManager", "init");
        m.wakeup();
        return !jg.cl() ? 0 : -1;
    }

    public void onCreate(Context context) {
        this.zF = new b();
        this.zF.onCreate(context);
        a(this.zF);
    }
}
