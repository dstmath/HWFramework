package com.huawei.hsm.permission.monitor;

import android.os.Bundle;

public class PermCounter {
    private static final String DES = "desStr";
    private static final String ISALLOW = "isAllow";
    private static final String NUM = "num";
    private static final String PERM_NAME = "permission";
    private static final String PERM_TYPE = "permType";
    private static final String PID = "pid";
    private static final String PKG_NAME = "pkgName";
    private static final String UID = "uid";
    private Bundle mPermBundle = new Bundle();

    PermCounter(int permType, String desStr, int validUid, int validPid) {
        setData(permType, desStr, validUid, validPid);
    }

    PermCounter(String pkg, String permName, boolean isAllow, String desStr) {
        setData(pkg, permName, isAllow, desStr);
    }

    private void setData(int permType, String desStr, int validUid, int validPid) {
        this.mPermBundle.putInt("uid", validUid);
        this.mPermBundle.putInt("pid", validPid);
        this.mPermBundle.putInt(PERM_TYPE, permType);
        this.mPermBundle.putString(DES, desStr);
        this.mPermBundle.putInt(NUM, 1);
    }

    private void setData(String pkg, String permName, boolean isAllow, String desStr) {
        this.mPermBundle.putString("pkgName", pkg);
        this.mPermBundle.putString(PERM_NAME, permName);
        this.mPermBundle.putBoolean(ISALLOW, isAllow);
        this.mPermBundle.putString(DES, desStr);
        this.mPermBundle.putInt(NUM, 1);
    }

    private void setNumber(int num) {
        this.mPermBundle.putInt(NUM, num);
    }

    private int getNumber() {
        return this.mPermBundle.getInt(NUM, 0);
    }

    public void count() {
        setNumber(getNumber() + 1);
    }

    public Bundle getData() {
        return this.mPermBundle;
    }

    public boolean isSamePerm(String pkg, String permName, boolean isAllow, String desStr) {
        boolean z = false;
        if (this.mPermBundle == null || !this.mPermBundle.containsKey(PERM_NAME)) {
            return false;
        }
        if (desStr == null) {
            desStr = "";
        }
        String pkgName = this.mPermBundle.getString("pkgName", "");
        String permission = this.mPermBundle.getString(PERM_NAME, "");
        Boolean allow = Boolean.valueOf(this.mPermBundle.getBoolean(ISALLOW));
        String des = this.mPermBundle.getString(DES, "");
        if (pkgName.equals(pkg) && permission.equals(permName) && isAllow == allow.booleanValue() && des.equals(desStr)) {
            z = true;
        }
        return z;
    }

    public boolean isSamePerm(int uid, int pid, int permType) {
        boolean z = false;
        if (this.mPermBundle == null || this.mPermBundle.containsKey(PERM_NAME)) {
            return false;
        }
        int mUid = this.mPermBundle.getInt("uid", -1);
        int mPid = this.mPermBundle.getInt("pid", -1);
        int mPermType = this.mPermBundle.getInt(PERM_TYPE, -1);
        if (mUid == uid && mPid == pid && mPermType == permType) {
            z = true;
        }
        return z;
    }
}
