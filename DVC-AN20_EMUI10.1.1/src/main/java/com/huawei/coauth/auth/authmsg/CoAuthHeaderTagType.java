package com.huawei.coauth.auth.authmsg;

public enum CoAuthHeaderTagType {
    VERSION(0),
    SRC_DID(1),
    SRC_MODULE(2),
    DST_DID(3),
    DST_MODULE(4);
    
    private final int value;

    private CoAuthHeaderTagType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
