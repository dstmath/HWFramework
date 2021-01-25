package com.huawei.odmf.database;

public interface Statement {
    void bindBlob(int i, byte[] bArr);

    void bindDouble(int i, double d);

    void bindLong(int i, long j);

    void bindNull(int i);

    void bindString(int i, String str);

    void clearBindings();

    void close();

    void execute();

    long executeInsert();

    void executeUpdateDelete();
}
