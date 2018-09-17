package com.huawei.android.pushagent.datatype.http.metadata;

public class TokenDelReqMeta {
    private String pkgName;
    private String token;

    public TokenDelReqMeta(String str, String str2) {
        this.pkgName = str;
        this.token = str2;
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
}
