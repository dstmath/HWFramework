package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import org.xmlpull.v1.XmlPullParser;

public class VersionProcessor extends BaseProcessor {
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "readCert Version error, line is empty");
            return false;
        } else if (rawCert == null) {
            HwAuthLogger.e(Utils.TAG, "readCert Version error, CertificationData is null");
            return false;
        } else if (line.startsWith("Version:")) {
            rawCert.mVersion = line.substring(HwCertification.KEY_VERSION.length() + 1);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(Utils.TAG, "readCert Version ok");
            }
            return true;
        } else {
            HwAuthLogger.e(Utils.TAG, "readCert Version start error");
            return false;
        }
    }

    public boolean parserCert(HwCertification rawCert) {
        if (rawCert == null) {
            HwAuthLogger.e(Utils.TAG, "parserCert:Version error, HwCertification is null");
            return false;
        }
        CertificationData certData = rawCert.mCertificationData;
        String version = certData.mVersion;
        if (version == null || version.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "parserCert:Version error, is empty");
            return false;
        }
        rawCert.setVersion(certData.mVersion);
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "parserCert: Version ok");
        }
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (pkg == null || cert == null) {
            HwAuthLogger.e(Utils.TAG, "verifyCert:Version error, Package or HwCertification is null");
            return false;
        }
        String version = cert.getVersion();
        if (version == null || version.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "verifyCert:Version error, is empty");
            return false;
        }
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "verifyCert: Version ok");
        }
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (HwCertification.KEY_VERSION.equals(tag)) {
            cert.mCertificationData.mVersion = parser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE);
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(Utils.TAG, "parseXmlTag:Version ok");
            }
            return true;
        }
        HwAuthLogger.e(Utils.TAG, "parseXmlTag Version error");
        return false;
    }
}
