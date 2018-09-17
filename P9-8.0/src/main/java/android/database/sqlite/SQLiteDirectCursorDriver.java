package android.database.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.CancellationSignal;

public final class SQLiteDirectCursorDriver implements SQLiteCursorDriver {
    private final CancellationSignal mCancellationSignal;
    private final SQLiteDatabase mDatabase;
    private final String mEditTable;
    private SQLiteQuery mQuery;
    private final String mSql;

    public SQLiteDirectCursorDriver(SQLiteDatabase db, String sql, String editTable, CancellationSignal cancellationSignal) {
        this.mDatabase = db;
        this.mEditTable = editTable;
        this.mSql = sql;
        this.mCancellationSignal = cancellationSignal;
    }

    public Cursor query(CursorFactory factory, String[] selectionArgs) {
        SQLiteQuery query = new SQLiteQuery(this.mDatabase, this.mSql, this.mCancellationSignal);
        try {
            Cursor cursor;
            query.bindAllArgsAsStrings(selectionArgs);
            if (factory == null) {
                cursor = new SQLiteCursor(this, this.mEditTable, query);
            } else {
                cursor = factory.newCursor(this.mDatabase, this, this.mEditTable, query);
            }
            this.mQuery = query;
            return cursor;
        } catch (RuntimeException ex) {
            query.close();
            throw ex;
        }
    }

    public void cursorClosed() {
    }

    public void setBindArguments(String[] bindArgs) {
        this.mQuery.bindAllArgsAsStrings(bindArgs);
    }

    public void cursorDeactivated() {
    }

    public void cursorRequeried(Cursor cursor) {
    }

    public String toString() {
        return "SQLiteDirectCursorDriver: " + this.mSql;
    }
}
