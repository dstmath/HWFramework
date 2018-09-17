package android.database;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public final class BulkCursorToCursorAdaptor extends AbstractWindowedCursor {
    private static final String TAG = "BulkCursor";
    private IBulkCursor mBulkCursor;
    private String[] mColumns;
    private int mCount;
    private SelfContentObserver mObserverBridge = new SelfContentObserver(this);
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

    public int getCount() {
        throwIfCursorIsClosed();
        return this.mCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x001d A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            } catch (Throwable th) {
                this.mBulkCursor = null;
            }
            this.mBulkCursor = null;
        }
    }

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

    public String[] getColumnNames() {
        throwIfCursorIsClosed();
        return this.mColumns;
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
