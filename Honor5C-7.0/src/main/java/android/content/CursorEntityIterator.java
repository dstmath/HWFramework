package android.content;

import android.database.Cursor;
import android.os.RemoteException;

public abstract class CursorEntityIterator implements EntityIterator {
    private final Cursor mCursor;
    private boolean mIsClosed;

    public abstract Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException;

    public CursorEntityIterator(Cursor cursor) {
        this.mIsClosed = false;
        this.mCursor = cursor;
        this.mCursor.moveToFirst();
    }

    public final boolean hasNext() {
        if (!this.mIsClosed) {
            return !this.mCursor.isAfterLast();
        } else {
            throw new IllegalStateException("calling hasNext() when the iterator is closed");
        }
    }

    public Entity next() {
        if (this.mIsClosed) {
            throw new IllegalStateException("calling next() when the iterator is closed");
        } else if (hasNext()) {
            try {
                return getEntityAndIncrementCursor(this.mCursor);
            } catch (RemoteException e) {
                throw new RuntimeException("caught a remote exception, this process will die soon", e);
            }
        } else {
            throw new IllegalStateException("you may only call next() if hasNext() is true");
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove not supported by EntityIterators");
    }

    public final void reset() {
        if (this.mIsClosed) {
            throw new IllegalStateException("calling reset() when the iterator is closed");
        }
        this.mCursor.moveToFirst();
    }

    public final void close() {
        if (this.mIsClosed) {
            throw new IllegalStateException("closing when already closed");
        }
        this.mIsClosed = true;
        this.mCursor.close();
    }
}
