package com.huawei.nb.authority;

public class GroupMember {
    private int authority;
    private String memberPkgName;

    public GroupMember(String str, int i) {
        this.memberPkgName = str;
        this.authority = i;
    }

    public String getMemberPkgName() {
        return this.memberPkgName;
    }

    public int getAuthority() {
        return this.authority;
    }
}
