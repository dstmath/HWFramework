package com.huawei.hwsqlite;

import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class SQLiteCursor extends AbstractWindowedCursor {
    static final int NO_COUNT = -1;
    static final String TAG = "SQLiteCursor";
    private Map<String, Integer> mColumnNameMap;
    private final String[] mColumns;
    private int mCount;
    private int mCursorWindowCapacity;
    private final SQLiteCursorDriver mDriver;
    private final String mEditTable;
    private final SQLiteQuery mQuery;

    @Deprecated
    public SQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        this(driver, editTable, query);
    }

    public SQLiteCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        this.mCount = -1;
        if (query != null) {
            this.mDriver = driver;
            this.mEditTable = editTable;
            this.mColumnNameMap = null;
            this.mQuery = query;
            this.mColumns = query.getColumnNames();
            return;
        }
        throw new IllegalArgumentException("query object cannot be null");
    }

    public SQLiteDatabase getDatabase() {
        return this.mQuery.getDatabase();
    }

    @Override // android.database.CrossProcessCursor, android.database.AbstractCursor
    public boolean onMove(int oldPosition, int newPosition) {
        if (this.mWindow != null && newPosition >= this.mWindow.getStartPosition() && newPosition < this.mWindow.getStartPosition() + this.mWindow.getNumRows()) {
            return true;
        }
        fillWindow(newPosition);
        return true;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getCount() {
        if (this.mCount == -1) {
            fillWindow(0);
        }
        return this.mCount;
    }

    private void awcClearOrCreateWindow(String name) {
        CursorWindow win = getWindow();
        if (win == null) {
            setWindow(new CursorWindow(name));
        } else {
            win.clear();
        }
    }

    private void awcCloseWindow() {
        setWindow(null);
    }

    private void fillWindow(int requiredPos) {
        awcClearOrCreateWindow(getDatabase().getPath());
        try {
            if (this.mCount == -1) {
                this.mCount = this.mQuery.fillWindow(this.mWindow, SQLiteDatabaseUtils.cursorPickFillWindowStartPosition(requiredPos, 0), requiredPos, true);
                this.mCursorWindowCapacity = this.mWindow.getNumRows();
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "received count(*) from native_fill_window: " + this.mCount);
                }
                return;
            }
            this.mQuery.fillWindow(this.mWindow, SQLiteDatabaseUtils.cursorPickFillWindowStartPosition(requiredPos, this.mCursorWindowCapacity), requiredPos, false);
        } catch (RuntimeException ex) {
            awcCloseWindow();
            throw ex;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
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
        int periodIndex = columnName.lastIndexOf(46);
        if (periodIndex != -1) {
            Exception e = new Exception();
            Log.e(TAG, "requesting column name with table name -- " + columnName, e);
            columnName = columnName.substring(periodIndex + 1);
        }
        Integer i2 = this.mColumnNameMap.get(columnName);
        if (i2 != null) {
            return i2.intValue();
        }
        return -1;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        return this.mColumns;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void deactivate() {
        super.deactivate();
        this.mDriver.cursorDeactivated();
    }

    @Override // java.io.Closeable, android.database.AbstractCursor, java.lang.AutoCloseable, android.database.Cursor
    public void close() {
        super.close();
        synchronized (this) {
            this.mQuery.close();
            this.mDriver.cursorClosed();
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean requery() {
        if (isClosed()) {
            return false;
        }
        synchronized (this) {
            if (!this.mQuery.getDatabase().isOpen()) {
                return false;
            }
            if (this.mWindow != null) {
                this.mWindow.clear();
            }
            this.mPos = -1;
            this.mCount = -1;
            this.mDriver.cursorRequeried(this);
            try {
                return super.requery();
            } catch (IllegalStateException e) {
                Log.w(TAG, "requery() failed " + e.getMessage(), e);
                return false;
            }
        }
    }

    @Override // android.database.AbstractWindowedCursor
    public void setWindow(CursorWindow window) {
        super.setWindow(window);
        this.mCount = -1;
    }

    public void setSelectionArguments(String[] selectionArgs) {
        this.mDriver.setBindArguments(selectionArgs);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object, android.database.AbstractCursor
    public void finalize() {
        try {
            if (this.mWindow != null) {
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
