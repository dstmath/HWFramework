package tmsdkobf;

import android.content.Context;
import android.content.pm.PackageInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import tmsdk.common.utils.f;

final class iq {
    private static final HashMap<String, String> rN = new HashMap();
    private static final long rO = (new GregorianCalendar(2040, 0, 1).getTimeInMillis() / 1000);
    private Context mContext;
    private Properties rP;
    boolean rQ = false;

    static {
        rN.put("AresEngineManager", "aresengine");
        rN.put("QScannerManager", "qscanner");
        rN.put("LocationManager", "phoneservice");
        rN.put("IpDialManager", "phoneservice");
        rN.put("UsefulNumberManager", "phoneservice");
        rN.put("NetworkManager", "network");
        rN.put("TrafficCorrectionManager", "network");
        rN.put("FirewallManager", "network");
        rN.put("NetSettingManager", "netsetting");
        rN.put("OptimizeManager", "optimize");
        rN.put("UpdateManager", "update");
        rN.put("UrlCheckManager", "urlcheck");
        rN.put("PermissionManager", "permission");
        rN.put("SoftwareManager", "software");
        rN.put("AntitheftManager", "antitheft");
        rN.put("PowerSavingManager", "powersaving");
    }

    iq(Properties properties, Context context) {
        this.rP = properties;
        this.mContext = context;
    }

    private String aH(String str) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str2 = null;
        if (packageInfo == null) {
            return null;
        }
        InputStream byteArrayInputStream = new ByteArrayInputStream(packageInfo.signatures[0].toByteArray());
        try {
            str2 = mc.n(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(byteArrayInputStream)).getEncoded());
            byteArrayInputStream.close();
            return str2;
        } catch (CertificateException e2) {
            e2.printStackTrace();
            return str2;
        } catch (IOException e3) {
            e3.printStackTrace();
            return str2;
        }
    }

    public String bQ() {
        return this.rP.getProperty("lc_sdk_channel");
    }

    public boolean bS() {
        if (this.rQ) {
            return true;
        }
        String aH = aH(this.mContext.getPackageName());
        if (aH == null) {
            return true;
        }
        String trim = this.rP.getProperty("signature").toUpperCase().trim();
        this.rQ = aH.equals(trim);
        if (!this.rQ) {
            f.f("DEBUG", "your    signature is " + aH + " len:" + aH.length());
            f.f("DEBUG", "licence signature is " + trim + " len:" + trim.length());
        }
        return this.rQ;
    }

    public long bT() {
        return Long.parseLong(this.rP.getProperty("expiry.seconds", Long.toString(rO)));
    }
}
