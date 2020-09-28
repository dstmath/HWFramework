package com.android.internal.telephony.cat;

public enum FontSize {
    NORMAL(0),
    LARGE(1),
    SMALL(2);
    
    private int mValue;

    private FontSize(int value) {
        this.mValue = value;
    }

    public static FontSize fromInt(int value) {
        FontSize[] values = values();
        for (FontSize e : values) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }
}
