package defpackage;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* renamed from: by */
final class by implements HostnameVerifier {
    by() {
    }

    public boolean verify(String str, SSLSession sSLSession) {
        aw.d("PushLog2828", "hostname=" + str);
        return str != null && str.startsWith("push") && str.endsWith("hicloud.com");
    }
}
