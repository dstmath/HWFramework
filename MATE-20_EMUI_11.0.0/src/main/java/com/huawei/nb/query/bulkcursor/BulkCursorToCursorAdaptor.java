package com.huawei.nb.query.bulkcursor;

import android.database.AbstractWindowedCursor;
import android.database.StaleDataException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public final class BulkCursorToCursorAdaptor extends AbstractWindowedCursor {
    private static final String TAG = "BulkCursor";
    private IBulkCursor mBulkCursor;
    private String[] mColumns;
    private int mCount;
    private boolean mWantsAllOnMoveCalls;

    public void initialize(BulkCursorDescriptor bulkCursorDescriptor) {
        this.mBulkCursor = bulkCursorDescriptor.getCursor();
        this.mColumns = bulkCursorDescriptor.getColumnNames();
        this.mWantsAllOnMoveCalls = bulkCursorDescriptor.isWantsAllOnMoveCalls();
        this.mCount = bulkCursorDescriptor.getCount();
        if (bulkCursorDescriptor.getWindow() != null) {
            setWindow(bulkCursorDescriptor.getWindow());
        }
        if (this.mBulkCursor == null) {
            Log.w(TAG, "mBulkCursor is null");
        }
    }

    private void throwIfCursorIsClosed() {
        if (this.mBulkCursor == null) {
            throw new StaleDataException("Attempted to access a cursor after it has been closed.");
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getCount() {
        throwIfCursorIsClosed();
        return this.mCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0037 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0038 A[RETURN] */
    @Override // android.database.CrossProcessCursor, android.database.AbstractCursor
    public boolean onMove(int i, int i2) {
        throwIfCursorIsClosed();
        try {
            if (this.mWindow != null && i2 >= this.mWindow.getStartPosition()) {
                if (i2 < this.mWindow.getStartPosition() + this.mWindow.getNumRows()) {
                    if (this.mWantsAllOnMoveCalls) {
                        this.mBulkCursor.onMove(i2);
                    }
                    if (this.mWindow != null) {
                        return false;
                    }
                    return true;
                }
            }
            setWindow(this.mBulkCursor.getWindow(i2));
            if (this.mWindow != null) {
            }
        } catch (RemoteException unused) {
            Log.e(TAG, "Unable to get window because the remote process is dead");
            return false;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void deactivate() {
        super.deactivate();
        IBulkCursor iBulkCursor = this.mBulkCursor;
        if (iBulkCursor != null) {
            try {
                iBulkCursor.deactivate();
            } catch (RemoteException unused) {
                Log.w(TAG, "Remote process exception when deactivating");
            }
        }
    }

    @Override // java.io.Closeable, android.database.AbstractCursor, java.lang.AutoCloseable, android.database.Cursor
    public void close() {
        super.close();
        IBulkCursor iBulkCursor = this.mBulkCursor;
        if (iBulkCursor != null) {
            try {
                iBulkCursor.close();
            } catch (RemoteException unused) {
                Log.w(TAG, "Remote process exception when closing");
            } catch (Throwable th) {
                this.mBulkCursor = null;
                throw th;
            }
            this.mBulkCursor = null;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        throwIfCursorIsClosed();
        return (String[]) this.mColumns.clone();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public Bundle getExtras() {
        throwIfCursorIsClosed();
        try {
            return this.mBulkCursor.getExtras();
        } catch (RemoteException e) {
            Log.w(TAG, "getExtras() threw RemoteException, returning an empty bundle.", e);
            return Bundle.EMPTY;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public Bundle respond(Bundle bundle) {
        throwIfCursorIsClosed();
        try {
            return this.mBulkCursor.respond(bundle);
        } catch (RemoteException e) {
            Log.w(TAG, "respond() threw RemoteException, returning an empty bundle.", e);
            return Bundle.EMPTY;
        }
    }
}
