package com.huawei.coauthservice.identitymgr;

public enum IdmUserType {
    SAME_USER_ID(0),
    NO_USER_ID(1);
    
    private int type;

    private IdmUserType(int value) {
        this.type = value;
    }

    public int toInt() {
        return this.type;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "IdmUserType{type=" + this.type + '}';
    }
}
