package com.huawei.coauthservice.identitymgr;

public enum IdmLinkType {
    AP(0),
    P2P(1);
    
    private int type;

    private IdmLinkType(int value) {
        this.type = value;
    }

    public int toInt() {
        return this.type;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "LinkType{type=" + this.type + '}';
    }
}
