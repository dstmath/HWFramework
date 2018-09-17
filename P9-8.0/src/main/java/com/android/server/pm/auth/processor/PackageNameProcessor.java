package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class PackageNameProcessor extends BaseProcessor {
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith("PackageName")) {
            return false;
        }
        String key = line.substring("PackageName".length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert:PackageName is empty");
            return false;
        }
        rawCert.mPackageName = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        CertificationData certData = rawCert.mCertificationData;
        if (certData.mPackageName == null || (certData.mPackageName.isEmpty() ^ 1) == 0) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:PackageName error");
            return false;
        }
        rawCert.setPackageName(certData.mPackageName);
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "--Verify PackageName--");
        }
        String pkgName = cert.getPackageName();
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        String realPkgName = pkg.packageName;
        if (pkgName.equals(realPkgName)) {
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.d("HwCertificationManager", "verifyCert:pkgName line ok");
            }
            return true;
        }
        HwAuthLogger.w("HwCertificationManager", "verifyCert:pkgName error, real pkgName is:" + realPkgName + "pkgName in HUAWEI.CER is" + pkgName);
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
