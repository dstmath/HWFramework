package com.huawei.hwsqlite;

import android.database.Cursor;
import com.huawei.hwsqlite.SQLiteDatabase;

/* access modifiers changed from: package-private */
public class SQLiteStepCursorFactory implements SQLiteDatabase.CursorFactory {
    SQLiteStepCursorFactory() {
    }

    @Override // com.huawei.hwsqlite.SQLiteDatabase.CursorFactory
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver cursorDriver, String editTable, SQLiteQuery query) {
        return new SQLiteStepCursor(cursorDriver, query);
    }
}
