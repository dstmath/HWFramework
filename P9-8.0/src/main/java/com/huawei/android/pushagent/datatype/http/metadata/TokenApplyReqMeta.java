package com.huawei.android.pushagent.datatype.http.metadata;

public class TokenApplyReqMeta {
    private String pkgName;
    private String userId;

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String str) {
        this.pkgName = str;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }
}
