package com.huawei.ace.plugin.editing;

public enum TextInputType {
    TEXT(0),
    MULTILINE(1),
    NUMBER(2),
    PHONE(3),
    DATETIME(4),
    EMAIL_ADDRESS(5),
    URL(6),
    VISIBLE_PASSWORD(7);
    
    private int value = 0;

    public static TextInputType of(Integer num) {
        if (num == null || num.intValue() < 0 || num.intValue() >= values().length) {
            return TEXT;
        }
        return values()[num.intValue()];
    }

    public int getValue() {
        return this.value;
    }

    private TextInputType(int i) {
        this.value = i;
    }
}
