package com.huawei.security.dpermission.model;

import java.util.ArrayList;
import java.util.List;
import ohos.global.icu.impl.PatternTokenizer;

public class PackageBo {
    private String name;
    private List<PermissionBo> permissions = new ArrayList();
    private List<SignBo> sign = new ArrayList();

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public List<SignBo> getSign() {
        return this.sign;
    }

    public void setSign(List<SignBo> list) {
        this.sign = list;
    }

    public List<PermissionBo> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<PermissionBo> list) {
        this.permissions = list;
    }

    public String toString() {
        return "PackageBo{name='" + this.name + PatternTokenizer.SINGLE_QUOTE + ", sign=" + this.sign + ", permissions=" + this.permissions + '}';
    }
}
