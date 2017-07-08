package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import org.xmlpull.v1.XmlPullParser;

public class PackageNameProcessor extends BaseProcessor {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwSecDiagnoseConstant.ANTIMAL_APK_PACKAGE_NAME)) {
            return false;
        }
        String key = line.substring(HwSecDiagnoseConstant.ANTIMAL_APK_PACKAGE_NAME.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "readCert:PackageName is empty");
            return false;
        }
        rawCert.mPackageName = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        CertificationData certData = rawCert.mCertificationData;
        if (certData.mPackageName == null || certData.mPackageName.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "parserCert:PackageName error");
            return false;
        }
        rawCert.setPackageName(certData.mPackageName);
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "--Verify PackageName--");
        }
        String pkgName = cert.getPackageName();
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        String realPkgName = pkg.packageName;
        if (pkgName.equals(realPkgName)) {
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.d(Utils.TAG, "verifyCert:pkgName line ok");
            }
            return true;
        }
        HwAuthLogger.w(Utils.TAG, "verifyCert:pkgName error, real pkgName is:" + realPkgName + "pkgName in HUAWEI.CER is" + pkgName);
        return false;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwSecDiagnoseConstant.ANTIMAL_APK_PACKAGE_NAME.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mPackageName = parser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE);
        return true;
    }
}
