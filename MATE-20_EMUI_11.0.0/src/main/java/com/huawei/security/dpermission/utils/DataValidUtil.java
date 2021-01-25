package com.huawei.security.dpermission.utils;

import com.huawei.security.dpermission.model.PackageBo;
import com.huawei.security.dpermission.model.PermissionBo;
import com.huawei.security.dpermission.model.SignBo;
import com.huawei.security.dpermission.model.SubjectUidPackageBo;

public final class DataValidUtil {
    public static boolean isUidValid(int i) {
        return i >= 0;
    }

    private static boolean isValidPermissionBo(PermissionBo permissionBo) {
        return permissionBo != null;
    }

    private static boolean isValidSignBo(SignBo signBo) {
        return signBo != null;
    }

    private DataValidUtil() {
    }

    public static boolean isSubjectUidPackageBoValid(SubjectUidPackageBo subjectUidPackageBo) {
        if (subjectUidPackageBo == null || subjectUidPackageBo.getPackages() == null) {
            return false;
        }
        for (PackageBo packageBo : subjectUidPackageBo.getPackages()) {
            if (!isPackageBoValid(packageBo)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPackageBoValid(PackageBo packageBo) {
        if (packageBo == null || packageBo.getSign() == null || packageBo.getPermissions() == null) {
            return false;
        }
        for (SignBo signBo : packageBo.getSign()) {
            if (!isValidSignBo(signBo)) {
                return false;
            }
        }
        for (PermissionBo permissionBo : packageBo.getPermissions()) {
            if (!isValidPermissionBo(permissionBo)) {
                return false;
            }
        }
        return true;
    }
}
