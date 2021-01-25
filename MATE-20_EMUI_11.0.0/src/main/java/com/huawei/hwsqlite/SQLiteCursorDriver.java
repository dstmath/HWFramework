package com.huawei.hwsqlite;

import android.database.Cursor;
import com.huawei.hwsqlite.SQLiteDatabase;

public interface SQLiteCursorDriver {
    void cursorClosed();

    void cursorDeactivated();

    void cursorRequeried(Cursor cursor);

    Cursor query(SQLiteDatabase.CursorFactory cursorFactory, String[] strArr);

    void setBindArguments(String[] strArr);
}
