package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class ExtenstionProcessor extends BaseProcessor {
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert Extenstion error, line is null");
            return false;
        } else if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "readCert Extenstion error, CertificationData is null");
            return false;
        } else if (line.startsWith("Extension:")) {
            rawCert.mExtenstion = line.substring(HwCertification.KEY_EXTENSION.length() + 1);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readCert:Extenstion ok");
            }
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "readCert Extenstion start error");
            return false;
        }
    }

    public boolean parserCert(HwCertification rawCert) {
        if (rawCert == null) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Extenstion error, HwCertification is null");
            return false;
        }
        CertificationData certData = rawCert.mCertificationData;
        String extenstion = certData.mExtenstion;
        if (extenstion == null || extenstion.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:Extenstion error, is empty");
            return false;
        }
        rawCert.setExtenstion(certData.mExtenstion);
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "parserCert:Extenstion ok");
        }
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (pkg == null || cert == null) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Extenstion error, Package or HwCertification is null");
            return false;
        }
        String extenstion = cert.getExtenstion();
        if (extenstion == null || extenstion.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "verifyCert:Extenstion error, is empty");
            return false;
        }
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "verifyCert:Extenstion ok");
        }
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (HwCertification.KEY_EXTENSION.equals(tag)) {
            cert.mCertificationData.mExtenstion = parser.getAttributeValue(null, "value");
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "parseXmlTag:Extenstion ok");
            }
            return true;
        }
        HwAuthLogger.e("HwCertificationManager", "parseXmlTag Extenstion error");
        return false;
    }
}
