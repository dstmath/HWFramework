package android.database;

import android.database.AbstractCursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public final class BulkCursorToCursorAdaptor extends AbstractWindowedCursor {
    private static final String TAG = "BulkCursor";
    private IBulkCursor mBulkCursor;
    private String[] mColumns;
    private int mCount;
    private AbstractCursor.SelfContentObserver mObserverBridge = new AbstractCursor.SelfContentObserver(this);
    private boolean mWantsAllOnMoveCalls;

    public void initialize(BulkCursorDescriptor d) {
        this.mBulkCursor = d.cursor;
        this.mColumns = d.columnNames;
        this.mWantsAllOnMoveCalls = d.wantsAllOnMoveCalls;
        this.mCount = d.count;
        if (d.window != null) {
            setWindow(d.window);
        }
    }

    public IContentObserver getObserver() {
        return this.mObserverBridge.getContentObserver();
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

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0038 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0039 A[RETURN] */
    @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
    public boolean onMove(int oldPosition, int newPosition) {
        throwIfCursorIsClosed();
        try {
            if (this.mWindow != null && newPosition >= this.mWindow.getStartPosition()) {
                if (newPosition < this.mWindow.getStartPosition() + this.mWindow.getNumRows()) {
                    if (this.mWantsAllOnMoveCalls) {
                        this.mBulkCursor.onMove(newPosition);
                    }
                    if (this.mWindow != null) {
                        return false;
                    }
                    return true;
                }
            }
            setWindow(this.mBulkCursor.getWindow(newPosition));
            if (this.mWindow != null) {
            }
        } catch (RemoteException e) {
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
            } catch (RemoteException e) {
                Log.w(TAG, "Remote process exception when deactivating");
            }
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        super.close();
        IBulkCursor iBulkCursor = this.mBulkCursor;
        if (iBulkCursor != null) {
            try {
                iBulkCursor.close();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote process exception when closing");
            } catch (Throwable th) {
                this.mBulkCursor = null;
                throw th;
            }
            this.mBulkCursor = null;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean requery() {
        throwIfCursorIsClosed();
        try {
            this.mCount = this.mBulkCursor.requery(getObserver());
            if (this.mCount != -1) {
                this.mPos = -1;
                closeWindow();
                super.requery();
                return true;
            }
            deactivate();
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "Unable to requery because the remote process exception " + ex.getMessage());
            deactivate();
            return false;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        throwIfCursorIsClosed();
        return this.mColumns;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public Bundle getExtras() {
        throwIfCursorIsClosed();
        try {
            return this.mBulkCursor.getExtras();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
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
