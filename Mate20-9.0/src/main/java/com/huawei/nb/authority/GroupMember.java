package com.huawei.nb.authority;

public class GroupMember {
    private int authority;
    private String memberPkgName;

    public GroupMember(String memberPkgName2, int authority2) {
        this.memberPkgName = memberPkgName2;
        this.authority = authority2;
    }

    public String getMemberPkgName() {
        return this.memberPkgName;
    }

    public int getAuthority() {
        return this.authority;
    }
}
