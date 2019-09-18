package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class DeveloperKeyProcessor extends BaseProcessor {
    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_DEVELIOPER)) {
            return false;
        }
        String devKey = line.substring(HwCertification.KEY_DEVELIOPER.length() + 1);
        if (devKey == null || devKey.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "DK_RC is empty");
            return false;
        }
        rawCert.mDelveoperKey = devKey;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        HwCertification.CertificationData certData = rawCert.mCertificationData;
        if (certData.mDelveoperKey == null || certData.mDelveoperKey.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "DK_PC error");
            return false;
        }
        rawCert.setDelveoperKey(certData.mDelveoperKey);
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHwFlow()) {
            HwAuthLogger.i("HwCertificationManager", "DK_VC start");
        }
        String keyFromCert = cert.getDelveoperKey();
        if (keyFromCert == null || keyFromCert.isEmpty() || pkg.mSigningDetails.signatures[0] == null) {
            return false;
        }
        if (keyFromCert.equals(pkg.mSigningDetails.signatures[0].toCharsString())) {
            if (HwAuthLogger.getHwDebug()) {
                HwAuthLogger.d("HwCertificationManager", "DK_VC ok");
            }
            return true;
        }
        HwAuthLogger.e("HwCertificationManager", "DK_VC not same");
        return false;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_DEVELIOPER.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mDelveoperKey = parser.getAttributeValue(null, "value");
        return true;
    }
}
