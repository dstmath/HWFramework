package android.database.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public interface SQLiteCursorDriver {
    void cursorClosed();

    void cursorDeactivated();

    void cursorRequeried(Cursor cursor);

    Cursor query(CursorFactory cursorFactory, String[] strArr);

    void setBindArguments(String[] strArr);
}
