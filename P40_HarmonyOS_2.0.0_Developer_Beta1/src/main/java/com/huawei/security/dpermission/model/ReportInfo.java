package com.huawei.security.dpermission.model;

import ohos.global.icu.impl.PatternTokenizer;

public class ReportInfo {
    private String packageName;
    private String permission;
    private String version;

    public ReportInfo() {
    }

    public ReportInfo(String str, String str2, String str3) {
        this.packageName = str;
        this.version = str2;
        this.permission = str3;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String str) {
        this.version = str;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String str) {
        this.permission = str;
    }

    public boolean checkIsValid() {
        return !isEmpty(this.packageName) && !isEmpty(this.version) && !isEmpty(this.permission);
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public String toString() {
        return "ReportInfo{packageName='" + this.packageName + PatternTokenizer.SINGLE_QUOTE + ", version='" + this.version + PatternTokenizer.SINGLE_QUOTE + ", permission='" + this.permission + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
