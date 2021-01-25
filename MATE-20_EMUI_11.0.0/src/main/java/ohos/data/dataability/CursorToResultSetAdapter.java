package ohos.data.dataability;

import android.database.ContentObserver;
import android.database.Cursor;
import java.lang.ref.WeakReference;
import ohos.data.resultset.AbsResultSet;
import ohos.data.resultset.ResultSet;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class CursorToResultSetAdapter extends AbsResultSet {
    private Cursor cursor;
    private CursorObserver cursorObserver;

    public CursorToResultSetAdapter(Cursor cursor2) {
        if (cursor2 != null) {
            this.cursor = cursor2;
            this.cursorObserver = new CursorObserver(this);
            this.cursor.registerContentObserver(this.cursorObserver);
            return;
        }
        throw new IllegalArgumentException("cursor cannot be null");
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        return this.cursor.getColumnNames();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getColumnCount() {
        return this.cursor.getColumnCount();
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        return ResultSet.ColumnType.getByValue(this.cursor.getType(i));
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getColumnIndexForName(String str) {
        return this.cursor.getColumnIndex(str);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getColumnNameForIndex(int i) {
        return this.cursor.getColumnName(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        return this.cursor.getCount();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowIndex() {
        return this.cursor.getPosition();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goTo(int i) {
        return this.cursor.move(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToRow(int i) {
        return this.cursor.moveToPosition(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToFirstRow() {
        return this.cursor.moveToFirst();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToLastRow() {
        return this.cursor.moveToLast();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToNextRow() {
        return this.cursor.moveToNext();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToPreviousRow() {
        return this.cursor.moveToPrevious();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isEnded() {
        return this.cursor.isAfterLast();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isStarted() {
        return this.cursor.isBeforeFirst();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isAtFirstRow() {
        return this.cursor.isFirst();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isAtLastRow() {
        return this.cursor.isLast();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        return this.cursor.getBlob(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getString(int i) {
        return this.cursor.getString(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public short getShort(int i) {
        return this.cursor.getShort(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getInt(int i) {
        return this.cursor.getInt(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public long getLong(int i) {
        return this.cursor.getLong(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        return this.cursor.getFloat(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        return this.cursor.getDouble(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        return this.cursor.isNull(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isClosed() {
        return this.cursor.isClosed();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        super.close();
        this.cursor.close();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public PacMap getExtensions() {
        return PacMapUtils.convertFromBundle(this.cursor.getExtras());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.resultset.AbsResultSet
    public void notifyChange() {
        super.notifyChange();
    }

    private class CursorObserver extends ContentObserver {
        private WeakReference<CursorToResultSetAdapter> resultSetRef;

        public CursorObserver(CursorToResultSetAdapter cursorToResultSetAdapter) {
            super(null);
            this.resultSetRef = new WeakReference<>(cursorToResultSetAdapter);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            CursorToResultSetAdapter cursorToResultSetAdapter = this.resultSetRef.get();
            if (cursorToResultSetAdapter != null) {
                cursorToResultSetAdapter.notifyChange();
            }
        }
    }
}
