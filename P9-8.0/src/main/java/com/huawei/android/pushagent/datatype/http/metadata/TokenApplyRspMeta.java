package com.huawei.android.pushagent.datatype.http.metadata;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.g;

public class TokenApplyRspMeta {
    private String pkgName;
    private int ret = -1;
    private String token;
    private String userId;

    public boolean isValid() {
        if (this.ret != 0 || TextUtils.isEmpty(this.userId) || (g.vg(this.userId) ^ 1) != 0 || TextUtils.isEmpty(this.pkgName) || TextUtils.isEmpty(this.token)) {
            return false;
        }
        return true;
    }

    public boolean isRemoveAble() {
        if (this.ret == 0 || (TextUtils.isEmpty(this.pkgName) ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
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
