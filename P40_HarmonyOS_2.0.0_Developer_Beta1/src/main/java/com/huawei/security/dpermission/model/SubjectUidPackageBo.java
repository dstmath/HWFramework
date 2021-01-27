package com.huawei.security.dpermission.model;

import java.util.ArrayList;
import java.util.List;

public class SubjectUidPackageBo {
    private static final int DEFAULT_SIZE = 10;
    private int appAttribute = -2;
    private List<PackageBo> packages = new ArrayList(10);
    private int uid;

    public int getUid() {
        return this.uid;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public int getAppAttribute() {
        return this.appAttribute;
    }

    public void setAppAttribute(int i) {
        this.appAttribute = i;
    }

    public List<PackageBo> getPackages() {
        return this.packages;
    }

    public void setPackages(List<PackageBo> list) {
        this.packages = list;
    }

    public String toString() {
        return "SubjectUidPackageBo{uid=" + this.uid + ", appAttribute=" + this.appAttribute + ", packages=" + this.packages + '}';
    }
}
