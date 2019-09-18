package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class PackageNameProcessor extends BaseProcessor {
    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith("PackageName")) {
            return false;
        }
        String key = line.substring("PackageName".length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "PN_RC is empty");
            return false;
        }
        rawCert.mPackageName = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        HwCertification.CertificationData certData = rawCert.mCertificationData;
        if (certData.mPackageName == null || certData.mPackageName.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "PN_PC error");
            return false;
        }
        rawCert.setPackageName(certData.mPackageName);
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHwFlow()) {
            HwAuthLogger.i("HwCertificationManager", "PN_PC start");
        }
        String pkgName = cert.getPackageName();
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        String realPkgName = pkg.packageName;
        if (pkgName.equals(realPkgName)) {
            if (HwAuthLogger.getHwDebug()) {
                HwAuthLogger.d("HwCertificationManager", "PN_PC ok");
            }
            return true;
        }
        HwAuthLogger.w("HwCertificationManager", "PN_PC error rn is :" + realPkgName + "pkgName in HUAWEI.CER is" + pkgName);
        return false;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!"PackageName".equals(tag)) {
            return false;
        }
        cert.mCertificationData.mPackageName = parser.getAttributeValue(null, "value");
        return true;
    }
}
