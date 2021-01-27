package com.huawei.hwsqlite;

public interface SQLiteErrorHandler {
    void onCorruption(SQLiteDatabase sQLiteDatabase);
}
