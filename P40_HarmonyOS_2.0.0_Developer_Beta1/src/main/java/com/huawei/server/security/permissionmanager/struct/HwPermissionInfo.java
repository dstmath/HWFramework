package com.huawei.server.security.permissionmanager.struct;

import java.util.Arrays;
import java.util.Objects;

public class HwPermissionInfo {
    private static final int HASH_CODE = 31;
    private boolean mIsUnit;
    private long mPermissionCode;
    private String[] mPermissionStrSets;

    public HwPermissionInfo(long permissionCode, String[] permissionStr, boolean isUnit) {
        this.mPermissionCode = permissionCode;
        this.mPermissionStrSets = (String[]) permissionStr.clone();
        this.mIsUnit = isUnit;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HwPermissionInfo)) {
            return false;
        }
        HwPermissionInfo info = (HwPermissionInfo) obj;
        boolean isPermissionCodeEquals = this.mPermissionCode == info.mPermissionCode;
        boolean isUnitEquals = isUnit() == info.isUnit();
        boolean isStrEquals = Arrays.equals(this.mPermissionStrSets, info.mPermissionStrSets);
        if (!isPermissionCodeEquals || !isUnitEquals || !isStrEquals) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (Objects.hash(Long.valueOf(this.mPermissionCode), Boolean.valueOf(isUnit())) * HASH_CODE) + Arrays.hashCode(this.mPermissionStrSets);
    }

    public long getPermissionCode() {
        return this.mPermissionCode;
    }

    public void setPermissionCode(long permissionCode) {
        this.mPermissionCode = permissionCode;
    }

    public String[] getPermissionStr() {
        String[] strArr = this.mPermissionStrSets;
        if (strArr != null) {
            return (String[]) strArr.clone();
        }
        return new String[0];
    }

    public void setPermissionStr(String[] permissionStr) {
        if (permissionStr != null) {
            this.mPermissionStrSets = (String[]) permissionStr.clone();
        } else {
            this.mPermissionStrSets = new String[0];
        }
    }

    public boolean isUnit() {
        return this.mIsUnit;
    }

    public void setIsUnit(boolean isUnit) {
        this.mIsUnit = isUnit;
    }
}
