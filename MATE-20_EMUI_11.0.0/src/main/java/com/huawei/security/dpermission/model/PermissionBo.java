package com.huawei.security.dpermission.model;

import ohos.global.icu.impl.PatternTokenizer;

public class PermissionBo {
    private int level;
    private String name;
    private int status;
    private String type;

    public String getName() {
        return this.name;
    }

    public int getLevel() {
        return this.level;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setType(String str) {
        this.type = str;
    }

    public int getStatus() {
        return this.status;
    }

    public void setLevel(int i) {
        this.level = i;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return "PermissionBo{name='" + this.name + PatternTokenizer.SINGLE_QUOTE + ", type='" + this.type + PatternTokenizer.SINGLE_QUOTE + ", level=" + this.level + ", status=" + this.status + '}';
    }
}
