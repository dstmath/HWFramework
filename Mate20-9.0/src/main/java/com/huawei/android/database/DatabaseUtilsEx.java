package com.huawei.android.database;

import android.database.Cursor;
import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseUtilsEx {
    public static void cursorFillWindow(Cursor cursor, int position, CursorWindow window) {
        DatabaseUtils.cursorFillWindow(cursor, position, window);
    }

    public static boolean queryIsEmpty(SQLiteDatabase db, String table) {
        return DatabaseUtils.queryIsEmpty(db, table);
    }
}
