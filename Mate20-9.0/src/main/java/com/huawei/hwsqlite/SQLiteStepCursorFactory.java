package com.huawei.hwsqlite;

import android.database.Cursor;
import com.huawei.hwsqlite.SQLiteDatabase;

class SQLiteStepCursorFactory implements SQLiteDatabase.CursorFactory {
    SQLiteStepCursorFactory() {
    }

    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver cursorDriver, String editTable, SQLiteQuery query) {
        return new SQLiteStepCursor(cursorDriver, query);
    }
}
