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

    public void initialize(BulkCursorDescriptor d) {
        this.mBulkCursor = d.getCursor();
        this.mColumns = d.getColumnNames();
        this.mWantsAllOnMoveCalls = d.isWantsAllOnMoveCalls();
        this.mCount = d.getCount();
        if (d.getWindow() != null) {
            setWindow(d.getWindow());
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

    public int getCount() {
        throwIfCursorIsClosed();
        return this.mCount;
    }

    public boolean onMove(int oldPosition, int newPosition) {
        throwIfCursorIsClosed();
        try {
            if (this.mWindow == null || newPosition < this.mWindow.getStartPosition() || newPosition >= this.mWindow.getStartPosition() + this.mWindow.getNumRows()) {
                setWindow(this.mBulkCursor.getWindow(newPosition));
            } else if (this.mWantsAllOnMoveCalls) {
                this.mBulkCursor.onMove(newPosition);
            }
            if (this.mWindow == null) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get window because the remote process is dead");
            return false;
        }
    }

    public void deactivate() {
        super.deactivate();
        if (this.mBulkCursor != null) {
            try {
                this.mBulkCursor.deactivate();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote process exception when deactivating");
            }
        }
    }

    public void close() {
        super.close();
        if (this.mBulkCursor != null) {
            try {
                this.mBulkCursor.close();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote process exception when closing");
            } finally {
                this.mBulkCursor = null;
            }
        }
    }

    public String[] getColumnNames() {
        throwIfCursorIsClosed();
        return (String[]) this.mColumns.clone();
    }

    public Bundle getExtras() {
        throwIfCursorIsClosed();
        try {
            return this.mBulkCursor.getExtras();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public Bundle respond(Bundle extras) {
        throwIfCursorIsClosed();
        try {
            return this.mBulkCursor.respond(extras);
        } catch (RemoteException e) {
            Log.w(TAG, "respond() threw RemoteException, returning an empty bundle.", e);
            return Bundle.EMPTY;
        }
    }
}
