package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class CertificateProcessor extends BaseProcessor {
    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "CF_RC line is empty");
            return false;
        } else if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "CF_RC cert is empty");
            return false;
        } else if (line.startsWith("Certificate:")) {
            rawCert.mCertificate = line.substring(HwCertification.KEY_CERTIFICATE.length() + 1);
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "CF_RC ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "CF_RC error");
            return false;
        }
    }

    public boolean parserCert(HwCertification rawCert) {
        if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "CF_PC cert is null");
            return false;
        }
        HwCertification.CertificationData certData = rawCert.mCertificationData;
        String certificate = certData.mCertificate;
        if (certificate == null || certificate.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "CF_PC is empty");
            return false;
        } else if (!HwCertification.isContainsCertificateType(certificate)) {
            HwAuthLogger.e("HwCertificationManager", "CF_PC not in reasonable range");
            return false;
        } else {
            rawCert.setCertificate(certData.mCertificate);
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "CF_PC ok");
            }
            return true;
        }
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        if (pkg == null || cert == null) {
            HwAuthLogger.e("HwCertificationManager", "CF_VC error or package is null");
            return false;
        }
        String certificate = cert.getCertificate();
        if (certificate == null || certificate.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "CF_VC is empty");
            return false;
        } else if (cert.mCertificationData.mPermissionsString == null) {
            HwAuthLogger.e("HwCertificationManager", "CF_VC permission is null");
            return false;
        } else if (!certificate.equals("null") || !cert.mCertificationData.mPermissionsString.equals("null")) {
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "CF_VC ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "CF_VC permission is not allowed");
            return false;
        }
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (HwCertification.KEY_CERTIFICATE.equals(tag)) {
            cert.mCertificationData.mCertificate = parser.getAttributeValue(null, "value");
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "CF_PX ok");
            }
            return true;
        }
        HwAuthLogger.e("HwCertificationManager", "CF_PX error");
        return false;
    }
}
