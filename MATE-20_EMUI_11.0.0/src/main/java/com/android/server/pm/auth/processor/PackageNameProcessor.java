package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class PackageNameProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_PACKAGE_NAME) || certLine.length() <= HwCertification.KEY_PACKAGE_NAME.length() + 1) {
            return false;
        }
        String packageName = certLine.substring(HwCertification.KEY_PACKAGE_NAME.length() + 1);
        if (TextUtils.isEmpty(packageName)) {
            HwAuthLogger.error(IProcessor.TAG, "PN_RC is empty!");
            return false;
        }
        certData.setPackageName(packageName);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String packageName = certData.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            hwCert.setPackageName(packageName);
            return true;
        }
        HwAuthLogger.error(IProcessor.TAG, "PN_PC error!");
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        String pkgName = hwCert.getPackageName();
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        String realPkgName = pkg.packageName;
        if (pkgName.equals(realPkgName)) {
            return true;
        }
        HwAuthLogger.warn(IProcessor.TAG, "PN_PC error rn is " + realPkgName + ", pkgName in HUAWEI.CER is " + pkgName);
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_PACKAGE_NAME.equals(tag)) {
            String packageName = parser.getAttributeValue(null, "value");
            if (!TextUtils.isEmpty(packageName)) {
                packageName = packageName.intern();
            }
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setPackageName(packageName);
                return true;
            }
        }
        return false;
    }
}
