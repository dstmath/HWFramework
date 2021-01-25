package com.google.android.textclassifier;

public final class NamedVariant {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int TYPE_BOOL = 5;
    public static final int TYPE_DOUBLE = 4;
    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_INT = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_STRING = 6;
    private boolean boolValue;
    private double doubleValue;
    private float floatValue;
    private int intValue;
    private long longValue;
    private final String name;
    private String stringValue;
    private final int type = 4;

    public NamedVariant(String name2, int value) {
        this.name = name2;
        this.intValue = value;
    }

    public NamedVariant(String name2, long value) {
        this.name = name2;
        this.longValue = value;
    }

    public NamedVariant(String name2, float value) {
        this.name = name2;
        this.floatValue = value;
    }

    public NamedVariant(String name2, double value) {
        this.name = name2;
        this.doubleValue = value;
    }

    public NamedVariant(String name2, boolean value) {
        this.name = name2;
        this.boolValue = value;
    }

    public NamedVariant(String name2, String value) {
        this.name = name2;
        this.stringValue = value;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public int getInt() {
        return this.intValue;
    }

    public long getLong() {
        return this.longValue;
    }

    public float getFloat() {
        return this.floatValue;
    }

    public double getDouble() {
        return this.doubleValue;
    }

    public boolean getBool() {
        return this.boolValue;
    }

    public String getString() {
        return this.stringValue;
    }
}
