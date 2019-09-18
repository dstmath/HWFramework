package com.huawei.odmf.model.api;

public interface Attribute {
    public static final int BLOB = 6;
    public static final int BOOLEAN = 3;
    public static final int BYTE = 11;
    public static final int CALENDAR = 12;
    public static final int CHARACTER = 14;
    public static final int CLOB = 7;
    public static final int DATE = 9;
    public static final int DOUBLE = 5;
    public static final int FLOAT = 4;
    public static final int INTEGER = 0;
    public static final int LONG = 1;
    public static final int PRIM_BOOLEAN = 20;
    public static final int PRIM_BYTE = 21;
    public static final int PRIM_CHAR = 22;
    public static final int PRIM_DOUBLE = 19;
    public static final int PRIM_FLOAT = 18;
    public static final int PRIM_INT = 15;
    public static final int PRIM_LONG = 16;
    public static final int PRIM_SHORT = 17;
    public static final int SHORT = 8;
    public static final int STRING = 2;
    public static final int TIME = 10;
    public static final int TIMESTAMP = 13;

    String getColumnName();

    String getDefault_value();

    String getFieldName();

    int getType();

    boolean hasIndex();

    boolean isLazy();

    boolean isNotNull();

    boolean isUnique();
}
