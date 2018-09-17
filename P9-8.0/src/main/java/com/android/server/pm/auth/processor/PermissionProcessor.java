package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import org.xmlpull.v1.XmlPullParser;

public class PermissionProcessor extends BaseProcessor {
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_PERMISSIONS)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_PERMISSIONS.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert:permission is empty");
            return false;
        }
        rawCert.mPermissionsString = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        int i = 0;
        String[] permissions = rawCert.mCertificationData.mPermissionsString.split(",");
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        int length = permissions.length;
        while (i < length) {
            rawCert.getPermissionList().add(permissions[i]);
            i++;
        }
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "--Verify Permission--");
        }
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "verifyCert:permission line ok");
        }
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_PERMISSIONS.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mPermissionsString = parser.getAttributeValue(null, "value");
        return true;
    }
}
