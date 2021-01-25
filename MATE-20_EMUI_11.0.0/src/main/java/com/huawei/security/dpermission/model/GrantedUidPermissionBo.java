package com.huawei.security.dpermission.model;

import java.util.ArrayList;
import java.util.List;

public class GrantedUidPermissionBo {
    private List<PermissionBo> permissions = new ArrayList();
    private int uid;

    public int getUid() {
        return this.uid;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public List<PermissionBo> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<PermissionBo> list) {
        this.permissions = list;
    }

    public String toString() {
        return "GrantedUidPermissionBo{uid=" + this.uid + ", permissions=" + this.permissions + '}';
    }
}
