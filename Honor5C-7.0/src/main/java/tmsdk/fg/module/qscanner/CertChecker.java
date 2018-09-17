package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import tmsdkobf.ms;

@Deprecated
/* compiled from: Unknown */
public class CertChecker {
    private List<ApkKey> LM;
    private Context mContext;

    public CertChecker(Context context) {
        this.mContext = context;
        ms.a(this.mContext, "trustcerts.dat", null);
        this.LM = ms.a(this.mContext, "label_tc", "trustcerts.dat", new ApkKey());
        if (this.LM == null) {
            this.LM = new ArrayList();
        }
    }

    public QScanResult checkCert(QScanResult qScanResult) {
        for (ApkKey apkKey : this.LM) {
            ApkKey apkKey2 = qScanResult.apkkey;
            if (!(!apkKey2.pkgName.equals(apkKey.pkgName) || apkKey2.certMd5.equals(apkKey.certMd5) || qScanResult.type == 3)) {
                qScanResult.type = 8;
                qScanResult.advice = 1;
            }
        }
        return qScanResult;
    }

    public int getApkClass(String str) {
        for (ApkKey apkKey : this.LM) {
            if (apkKey.pkgName.equals(str)) {
                return 1;
            }
        }
        return 0;
    }
}
