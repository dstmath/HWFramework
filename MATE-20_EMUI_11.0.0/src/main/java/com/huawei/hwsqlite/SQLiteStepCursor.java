package com.huawei.hwsqlite;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.huawei.hwsqlite.SQLiteConnection;
import java.util.HashMap;
import java.util.Map;

public class SQLiteStepCursor implements Cursor {
    static final int INIT_POS = -1;
    static final int SQLITE_DONE = 1;
    static final String TAG = "SQLiteStepCursor";
    private final SQLiteCloseGuard mCloseGuard = SQLiteCloseGuard.get();
    private boolean mClosed = false;
    private Map<String, Integer> mColumnNameMap;
    private final String[] mColumns;
    private final SQLiteCursorDriver mDriver;
    private boolean mIsAfterLast = false;
    private int mPos = -1;
    private final SQLiteQuery mQuery;
    private SQLiteSession mSession = null;
    private SQLiteConnection.PreparedStatement mStatement = null;

    private static native void nativeCopyStringToBuffer(long j, int i, CharArrayBuffer charArrayBuffer);

    private static native byte[] nativeGetBlob(long j, int i);

    private static native double nativeGetDouble(long j, int i);

    private static native long nativeGetLong(long j, int i);

    private static native String nativeGetString(long j, int i);

    private static native int nativeGetType(long j, int i);

    public SQLiteStepCursor(SQLiteCursorDriver cursorDriver, SQLiteQuery query) {
        this.mQuery = query;
        this.mDriver = cursorDriver;
        this.mColumns = query.getColumnNames();
        this.mColumnNameMap = null;
        this.mCloseGuard.open(TAG, false);
    }

    private void checkSession() {
        SQLiteSession sQLiteSession = this.mSession;
        if (sQLiteSession != null && sQLiteSession != this.mQuery.getSession()) {
            throw new IllegalStateException("StepCursor passed cross threads!");
        }
    }

    private void checkState() {
        if (this.mStatement == null) {
            throw new IllegalStateException("StepCursor query haven't been executed!");
        }
    }

    private void prepareForStep() {
        if (this.mSession == null) {
            this.mSession = this.mQuery.getSession();
            this.mStatement = this.mQuery.beginStepQuery();
            return;
        }
        checkSession();
    }

    private void finishStep() {
        checkSession();
        if (this.mSession != null) {
            this.mQuery.endStepQuery(this.mStatement);
            this.mSession = null;
            this.mStatement = null;
        }
    }

    @Override // android.database.Cursor
    public int getCount() {
        throw new SQLiteNotSupportException("getCount() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public int getPosition() {
        return this.mPos;
    }

    @Override // android.database.Cursor
    public boolean move(int offset) {
        throw new SQLiteNotSupportException("move() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean moveToPosition(int position) {
        throw new SQLiteNotSupportException("moveToPosition() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean moveToFirst() {
        if (this.mPos == -1) {
            return moveToNext();
        }
        return false;
    }

    @Override // android.database.Cursor
    public boolean moveToLast() {
        throw new SQLiteNotSupportException("moveToLast() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean moveToNext() {
        if (this.mIsAfterLast) {
            return false;
        }
        prepareForStep();
        try {
            if (this.mQuery.fillStep() == 1) {
                this.mIsAfterLast = true;
                finishStep();
                return false;
            }
            this.mPos++;
            return true;
        } catch (SQLiteException ex) {
            finishStep();
            throw ex;
        }
    }

    @Override // android.database.Cursor
    public boolean moveToPrevious() {
        throw new SQLiteNotSupportException("moveToPrevious() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean isFirst() {
        return this.mPos == 0;
    }

    @Override // android.database.Cursor
    public boolean isLast() {
        throw new SQLiteNotSupportException("isLast() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean isBeforeFirst() {
        return this.mPos == -1;
    }

    @Override // android.database.Cursor
    public boolean isAfterLast() {
        return this.mIsAfterLast;
    }

    @Override // android.database.Cursor
    public int getColumnIndex(String columnName) {
        if (this.mColumnNameMap == null) {
            String[] columns = this.mColumns;
            int columnCount = columns.length;
            HashMap<String, Integer> map = new HashMap<>(columnCount, 1.0f);
            for (int i = 0; i < columnCount; i++) {
                map.put(columns[i], Integer.valueOf(i));
            }
            this.mColumnNameMap = map;
        }
        String newColumnName = columnName;
        int periodIndex = columnName.lastIndexOf(46);
        if (periodIndex != -1) {
            Exception e = new Exception();
            Log.e(TAG, "requesting column name with table name -- " + columnName, e);
            newColumnName = columnName.substring(periodIndex + 1);
        }
        Integer i2 = this.mColumnNameMap.get(newColumnName);
        if (i2 != null) {
            return i2.intValue();
        }
        return -1;
    }

    @Override // android.database.Cursor
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        int index = getColumnIndex(columnName);
        if (index >= 0) {
            return index;
        }
        throw new IllegalArgumentException("column '" + columnName + "' does not exist");
    }

    @Override // android.database.Cursor
    public String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }

    @Override // android.database.Cursor
    public String[] getColumnNames() {
        return this.mColumns;
    }

    @Override // android.database.Cursor
    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override // android.database.Cursor
    public byte[] getBlob(int columnIndex) {
        checkState();
        return nativeGetBlob(this.mStatement.mStatementPtr, columnIndex);
    }

    @Override // android.database.Cursor
    public String getString(int columnIndex) {
        checkState();
        return nativeGetString(this.mStatement.mStatementPtr, columnIndex);
    }

    @Override // android.database.Cursor
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        checkState();
        if (buffer != null) {
            nativeCopyStringToBuffer(this.mStatement.mStatementPtr, columnIndex, buffer);
            return;
        }
        throw new IllegalArgumentException("CharArrayBuffer should not be null");
    }

    @Override // android.database.Cursor
    public short getShort(int columnIndex) {
        return (short) ((int) getLong(columnIndex));
    }

    @Override // android.database.Cursor
    public int getInt(int columnIndex) {
        return (int) getLong(columnIndex);
    }

    @Override // android.database.Cursor
    public long getLong(int columnIndex) {
        checkState();
        return nativeGetLong(this.mStatement.mStatementPtr, columnIndex);
    }

    @Override // android.database.Cursor
    public float getFloat(int columnIndex) {
        return (float) getDouble(columnIndex);
    }

    @Override // android.database.Cursor
    public double getDouble(int columnIndex) {
        checkState();
        return nativeGetDouble(this.mStatement.mStatementPtr, columnIndex);
    }

    @Override // android.database.Cursor
    public int getType(int column) {
        checkState();
        return nativeGetType(this.mStatement.mStatementPtr, column);
    }

    @Override // android.database.Cursor
    public boolean isNull(int columnIndex) {
        return getType(columnIndex) == 0;
    }

    @Override // android.database.Cursor
    public void deactivate() {
        this.mDriver.cursorDeactivated();
    }

    @Override // android.database.Cursor
    public boolean requery() {
        if (isClosed()) {
            return false;
        }
        synchronized (this) {
            if (!this.mQuery.getDatabase().isOpen()) {
                return false;
            }
            this.mPos = -1;
            this.mIsAfterLast = false;
            this.mDriver.cursorRequeried(this);
            finishStep();
            return true;
        }
    }

    @Override // android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (!this.mClosed) {
            this.mClosed = true;
            finishStep();
            dispose(false);
            synchronized (this) {
                this.mQuery.close();
                this.mDriver.cursorClosed();
            }
        }
    }

    @Override // android.database.Cursor
    public boolean isClosed() {
        return this.mClosed;
    }

    @Override // android.database.Cursor
    public void registerContentObserver(ContentObserver observer) {
        throw new SQLiteNotSupportException("registerContentObserver() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public void unregisterContentObserver(ContentObserver observer) {
        throw new SQLiteNotSupportException("unregisterContentObserver() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public void registerDataSetObserver(DataSetObserver observer) {
        throw new SQLiteNotSupportException("registerDataSetObserver() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public void unregisterDataSetObserver(DataSetObserver observer) {
        throw new SQLiteNotSupportException("unregisterDataSetObserver() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new SQLiteNotSupportException("setNotificationUri() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public Uri getNotificationUri() {
        throw new SQLiteNotSupportException("getNotificationUri() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override // android.database.Cursor
    public void setExtras(Bundle extras) {
        throw new SQLiteNotSupportException("setExtras() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public Bundle getExtras() {
        throw new SQLiteNotSupportException("getExtras() method is not supported by SQLiteStepCursor.");
    }

    @Override // android.database.Cursor
    public Bundle respond(Bundle extras) {
        throw new SQLiteNotSupportException("respond() method is not supported by SQLiteStepCursor.");
    }

    public void setSelectionArguments(String[] selectionArgs) {
        this.mDriver.setBindArguments(selectionArgs);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        dispose(true);
        super.finalize();
    }

    private void dispose(boolean finalized) {
        SQLiteCloseGuard sQLiteCloseGuard = this.mCloseGuard;
        if (sQLiteCloseGuard != null) {
            if (finalized) {
                sQLiteCloseGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
    }
}
