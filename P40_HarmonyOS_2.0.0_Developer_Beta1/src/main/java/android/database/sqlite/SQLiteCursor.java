package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.StrictMode;
import android.util.Log;
import com.android.internal.util.Preconditions;
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
    @UnsupportedAppUsage
    private final String mEditTable;
    private boolean mFillWindowForwardOnly;
    @UnsupportedAppUsage
    private final SQLiteQuery mQuery;
    private final Throwable mStackTrace;

    @Deprecated
    public SQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        this(driver, editTable, query);
    }

    public SQLiteCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        this.mCount = -1;
        if (query != null) {
            if (StrictMode.vmSqliteObjectLeaksEnabled()) {
                this.mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();
            } else {
                this.mStackTrace = null;
            }
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

    @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
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

    @UnsupportedAppUsage
    private void fillWindow(int requiredPos) {
        int startPos;
        clearOrCreateWindow(getDatabase().getPath());
        try {
            Preconditions.checkArgumentNonnegative(requiredPos, "requiredPos cannot be negative, but was " + requiredPos);
            if (this.mCount == -1) {
                this.mCount = this.mQuery.fillWindow(this.mWindow, requiredPos, requiredPos, true);
                this.mCursorWindowCapacity = this.mWindow.getNumRows();
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "received count(*) from native_fill_window: " + this.mCount);
                    return;
                }
                return;
            }
            if (this.mFillWindowForwardOnly) {
                startPos = requiredPos;
            } else {
                startPos = DatabaseUtils.cursorPickFillWindowStartPosition(requiredPos, this.mCursorWindowCapacity);
            }
            this.mQuery.fillWindow(this.mWindow, startPos, requiredPos, false);
        } catch (RuntimeException ex) {
            closeWindow();
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

    @Override // android.database.AbstractCursor, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
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

    public void setFillWindowForwardOnly(boolean fillWindowForwardOnly) {
        this.mFillWindowForwardOnly = fillWindowForwardOnly;
    }

    /* access modifiers changed from: protected */
    @Override // android.database.AbstractCursor, java.lang.Object
    public void finalize() {
        try {
            if (this.mWindow != null) {
                if (this.mStackTrace != null) {
                    String sql = this.mQuery.getSql();
                    int len = sql.length();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Finalizing a Cursor that has not been deactivated or closed. database = ");
                    sb.append(this.mQuery.getDatabase().getLabel());
                    sb.append(", table = ");
                    sb.append(this.mEditTable);
                    sb.append(", query = ");
                    int i = 1000;
                    if (len <= 1000) {
                        i = len;
                    }
                    sb.append(sql.substring(0, i));
                    StrictMode.onSqliteObjectLeaked(sb.toString(), this.mStackTrace);
                }
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
