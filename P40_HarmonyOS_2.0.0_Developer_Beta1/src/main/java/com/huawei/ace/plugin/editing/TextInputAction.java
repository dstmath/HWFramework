package com.huawei.ace.plugin.editing;

public enum TextInputAction {
    UNSPECIFIED(0),
    NONE(1),
    GO(2),
    SEARCH(3),
    SEND(4),
    NEXT(5),
    DONE(6),
    PREVIOUS(7);
    
    private int value = 0;

    public static TextInputAction of(Integer num) {
        if (num == null || num.intValue() < 0 || num.intValue() >= values().length) {
            return UNSPECIFIED;
        }
        return values()[num.intValue()];
    }

    public int getValue() {
        return this.value;
    }

    private TextInputAction(int i) {
        this.value = i;
    }
}
