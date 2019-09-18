package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;

public class PermissionProcessor extends BaseProcessor {
    private static final String SPECIAL_PERMISSION = "com.huawei.permission.sec.MDM_INSTALL_SYS_APP";

    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_PERMISSIONS)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_PERMISSIONS.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "PM_RC empty");
            return false;
        }
        rawCert.mPermissionsString = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        String[] permissions = rawCert.mCertificationData.mPermissionsString.split(",");
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        if (Arrays.asList(permissions).contains(SPECIAL_PERMISSION)) {
            HwAuthLogger.i("HwCertificationManager", "permissions contain special permision");
            rawCert.setContainSpecialPermissions(true);
        }
        for (String perm : permissions) {
            rawCert.getPermissionList().add(perm);
        }
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
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
