package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwPermissionListManager;
import com.android.server.pm.auth.util.HwAuthLogger;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;

public class PermissionProcessor implements IProcessor {
    public static final String SPECIAL_PERMISSION = "com.huawei.permission.sec.MDM_INSTALL_SYS_APP";

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_PERMISSIONS) || certLine.length() <= HwCertification.KEY_PERMISSIONS.length() + 1) {
            return false;
        }
        String permissions = certLine.substring(HwCertification.KEY_PERMISSIONS.length() + 1);
        if (TextUtils.isEmpty(permissions)) {
            HwAuthLogger.error(IProcessor.TAG, "PM_RC empty!");
            return false;
        }
        certData.setPermissionsString(permissions);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        String[] permissions;
        int i = 0;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String permissionsStr = certData.getPermissionsString();
        if (TextUtils.isEmpty(permissionsStr) || (permissions = permissionsStr.split(",")) == null || permissions.length == 0) {
            return false;
        }
        if (Arrays.asList(permissions).contains(SPECIAL_PERMISSION)) {
            HwAuthLogger.info(IProcessor.TAG, "permissions contain special permission.");
            hwCert.setContainSpecialPermissions(true);
        }
        if ("2".equals(hwCert.getVersion())) {
            int length = permissions.length;
            while (i < length) {
                hwCert.getPermissions().add(permissions[i]);
                i++;
            }
        } else {
            int length2 = permissions.length;
            while (i < length2) {
                parsePermissionForVersion1Cert(hwCert, permissions[i]);
                i++;
            }
        }
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_PERMISSIONS.equals(tag)) {
            String permissionsString = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setPermissionsString(permissionsString);
                return true;
            }
        }
        return false;
    }

    private void parsePermissionForVersion1Cert(HwCertification hwCert, String permission) {
        if (!HwPermissionListManager.isPermissionControlledForMdmApk(permission)) {
            hwCert.getPermissions().add(permission);
        } else if (HwPermissionListManager.getInstance().couldGrantExtendSystemPermissionToMdmApk(permission)) {
            hwCert.getPermissions().add(permission);
        }
    }
}
