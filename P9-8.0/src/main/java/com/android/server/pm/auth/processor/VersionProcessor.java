package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class VersionProcessor extends BaseProcessor {
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert Version error, line is empty");
            return false;
        } else if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "readCert Version error, CertificationData is null");
            return false;
        } else if (line.startsWith("Version:")) {
            rawCert.mVersion = line.substring(HwCertification.KEY_VERSION.length() + 1);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readCert Version ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "readCert Version start error");
            return false;
        }
    }

    public boolean parserCert(HwCertification rawCert) {
        if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Version error, HwCertification is null");
            return false;
        }
        CertificationData certData = rawCert.mCertificationData;
        String version = certData.mVersion;
        if (version == null || version.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Version error, is empty");
            return false;
        }
        rawCert.setVersion(certData.mVersion);
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "parserCert: Version ok");
        }
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (pkg == null || cert == null) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Version error, Package or HwCertification is null");
            return false;
        }
        String version = cert.getVersion();
        if (version == null || version.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Version error, is empty");
            return false;
        }
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "verifyCert: Version ok");
        }
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (HwCertification.KEY_VERSION.equals(tag)) {
            cert.mCertificationData.mVersion = parser.getAttributeValue(null, "value");
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "parseXmlTag:Version ok");
            }
            return true;
        }
        HwAuthLogger.e("HwCertificationManager", "parseXmlTag Version error");
        return false;
    }
}
