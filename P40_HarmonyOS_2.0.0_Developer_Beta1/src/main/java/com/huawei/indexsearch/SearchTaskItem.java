package com.huawei.indexsearch;

import java.util.List;

public class SearchTaskItem {
    private List<String> ids;
    private int operator;
    private String pkgName;

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public void setIds(List<String> ids2) {
        this.ids = ids2;
    }

    public int getOp() {
        return this.operator;
    }

    public void setOp(int op) {
        this.operator = op;
    }
}
