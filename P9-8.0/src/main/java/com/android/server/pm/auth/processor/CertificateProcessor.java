package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class CertificateProcessor extends BaseProcessor {
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert Certificate error, line is null");
            return false;
        } else if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "readCert Certificate error, CertificationData is null");
            return false;
        } else if (line.startsWith("Certificate:")) {
            rawCert.mCertificate = line.substring(HwCertification.KEY_CERTIFICATE.length() + 1);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readCert Certificate ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "readCert Certificate start error");
            return false;
        }
    }

    public boolean parserCert(HwCertification rawCert) {
        if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Certificate error, HwCertification is null");
            return false;
        }
        CertificationData certData = rawCert.mCertificationData;
        String certificate = certData.mCertificate;
        if (certificate == null || certificate.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Certificate error, is empty");
            return false;
        } else if (HwCertification.isContainsCertificateType(certificate)) {
            rawCert.setCertificate(certData.mCertificate);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "parserCert:Certificate ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Certificate error, value is not within the range of values");
            return false;
        }
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (pkg == null || cert == null) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Certificate error, Package or HwCertification is null");
            return false;
        }
        String certificate = cert.getCertificate();
        if (certificate == null || certificate.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Certificate error, is empty");
            return false;
        } else if (cert.mCertificationData.mPermissionsString == null) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Certificate error, CertificationData.mPermissionsString is null");
            return false;
        } else if (certificate.equals("null") && cert.mCertificationData.mPermissionsString.equals("null")) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Certificate error, Permissions and Certificate are default value, it is not allowed");
            return false;
        } else {
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "verifyCert:Certificate ok");
            }
            return true;
        }
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (HwCertification.KEY_CERTIFICATE.equals(tag)) {
            cert.mCertificationData.mCertificate = parser.getAttributeValue(null, "value");
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "parseXmlTag:Certificate ok");
            }
            return true;
        }
        HwAuthLogger.e("HwCertificationManager", "parseXmlTag Certificate error");
        return false;
    }
}
