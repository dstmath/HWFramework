package ohos.data.dataability;

import android.database.AbstractWindowedCursor;
import android.os.Bundle;
import java.io.IOException;
import java.lang.ref.WeakReference;
import ohos.data.rdb.DataObserver;
import ohos.data.resultset.SharedBlock;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.adapter.PacMapUtils;

public class SharedResultSetToWindowedCursorAdapter extends AbstractWindowedCursor {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "SharedResultSetToWindowedCursorAdapter");
    private ResultSetObserver resultSetObserver;
    private SharedResultSet sharedResultSet;

    public SharedResultSetToWindowedCursorAdapter(SharedResultSet sharedResultSet2) {
        if (sharedResultSet2 != null) {
            this.sharedResultSet = sharedResultSet2;
            this.resultSetObserver = new ResultSetObserver(this);
            this.sharedResultSet.registerObserver(this.resultSetObserver);
            return;
        }
        HiLog.info(LABEL, "SharedResultSetToWindowedCursorAdapter: inputResultSet can't be null.", new Object[0]);
        throw new IllegalArgumentException("inputResultSet can't be null");
    }

    @Override // android.database.CrossProcessCursor, android.database.AbstractCursor
    public boolean onMove(int i, int i2) {
        boolean onGo = this.sharedResultSet.onGo(i, i2);
        if (onGo) {
            SharedBlock block = this.sharedResultSet.getBlock();
            if (block != null) {
                try {
                    super.setWindow(ContentProviderConverter.sharedBlockToCursorWindow(block));
                    super.getWindow().setStartPosition(this.sharedResultSet.getBlock().getStartRowIndex());
                } catch (IOException unused) {
                    HiLog.info(LABEL, "SharedResultSetToWindowedCursorAdapter: SharedBlock can't be null.", new Object[0]);
                }
            } else {
                HiLog.info(LABEL, "SharedResultSetToWindowedCursorAdapter: SharedBlock can't be null.", new Object[0]);
                throw new IllegalArgumentException("SharedBlock can't be null");
            }
        }
        return onGo;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getCount() {
        return this.sharedResultSet.getRowCount();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        return this.sharedResultSet.getAllColumnNames();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean isClosed() {
        return this.sharedResultSet.isClosed();
    }

    @Override // java.io.Closeable, android.database.AbstractCursor, java.lang.AutoCloseable, android.database.Cursor
    public void close() {
        super.close();
        SharedResultSet sharedResultSet2 = this.sharedResultSet;
        if (sharedResultSet2 != null) {
            sharedResultSet2.close();
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public Bundle getExtras() {
        return PacMapUtils.convertIntoBundle(this.sharedResultSet.getExtensions());
    }

    /* access modifiers changed from: protected */
    @Override // android.database.AbstractCursor
    public void onChange(boolean z) {
        super.onChange(z);
    }

    private class ResultSetObserver implements DataObserver {
        private WeakReference<SharedResultSetToWindowedCursorAdapter> cursorRef;

        public ResultSetObserver(SharedResultSetToWindowedCursorAdapter sharedResultSetToWindowedCursorAdapter) {
            this.cursorRef = new WeakReference<>(sharedResultSetToWindowedCursorAdapter);
        }

        @Override // ohos.data.rdb.DataObserver
        public void onChange() {
            SharedResultSetToWindowedCursorAdapter sharedResultSetToWindowedCursorAdapter = this.cursorRef.get();
            if (sharedResultSetToWindowedCursorAdapter != null) {
                sharedResultSetToWindowedCursorAdapter.onChange(false);
            }
        }
    }
}
