package com.huawei.android.pushagent.datatype.http.metadata;

import android.text.TextUtils;

public class TokenDelRspMeta {
    private String pkgName;
    private int ret;
    private String token;

    public boolean isValid() {
        if (this.ret != 0 || TextUtils.isEmpty(this.token)) {
            return false;
        }
        return true;
    }

    public boolean isRemoveAble() {
        if (this.ret == 0 || (TextUtils.isEmpty(this.token) ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String str) {
        this.pkgName = str;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String str) {
        this.token = str;
    }

    public int getRet() {
        return this.ret;
    }

    public void setRet(int i) {
        this.ret = i;
    }
}
