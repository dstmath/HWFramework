package com.huawei.nb.searchmanager.query.bulkcursor;

import android.database.CrossProcessCursor;
import android.database.CrossProcessCursorWrapper;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.StaleDataException;
import android.os.Bundle;
import android.os.IBinder;

public final class CursorToBulkCursorAdaptor extends BulkCursorNative implements IBinder.DeathRecipient {
    private CrossProcessCursor mCursor;
    private CursorWindow mFilledWindow;
    private final Object mLock = new Object();
    private final String mProviderName;

    public CursorToBulkCursorAdaptor(Cursor cursor, String str) {
        if (cursor instanceof CrossProcessCursor) {
            this.mCursor = (CrossProcessCursor) cursor;
        } else {
            this.mCursor = new CrossProcessCursorWrapper(cursor);
        }
        this.mProviderName = str;
    }

    private void closeFilledWindowLocked() {
        CursorWindow cursorWindow = this.mFilledWindow;
        if (cursorWindow != null) {
            cursorWindow.close();
            this.mFilledWindow = null;
        }
    }

    private void disposeLocked() {
        CrossProcessCursor crossProcessCursor = this.mCursor;
        if (crossProcessCursor != null) {
            crossProcessCursor.close();
            this.mCursor = null;
        }
        closeFilledWindowLocked();
    }

    private void throwIfCursorIsClosed() {
        if (this.mCursor == null) {
            throw new StaleDataException("Attempted to access a cursor after it has been closed.");
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (this.mLock) {
            disposeLocked();
        }
    }

    public BulkCursorDescriptor getBulkCursorDescriptor() {
        BulkCursorDescriptor bulkCursorDescriptor;
        synchronized (this.mLock) {
            throwIfCursorIsClosed();
            bulkCursorDescriptor = new BulkCursorDescriptor(this, this.mCursor.getColumnNames(), this.mCursor.getWantsAllOnMoveCalls(), this.mCursor.getCount(), this.mCursor.getWindow());
            if (bulkCursorDescriptor.getWindow() != null) {
                bulkCursorDescriptor.getWindow().acquireReference();
            }
        }
        return bulkCursorDescriptor;
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public CursorWindow getWindow(int i) {
        synchronized (this.mLock) {
            throwIfCursorIsClosed();
            if (!this.mCursor.moveToPosition(i)) {
                closeFilledWindowLocked();
                return null;
            }
            CursorWindow window = this.mCursor.getWindow();
            if (window != null) {
                closeFilledWindowLocked();
                window.acquireReference();
            } else {
                window = this.mFilledWindow;
                if (window == null) {
                    this.mFilledWindow = new CursorWindow(this.mProviderName);
                    window = this.mFilledWindow;
                } else if (i < window.getStartPosition() || i >= window.getStartPosition() + window.getNumRows()) {
                    window.clear();
                }
                this.mCursor.fillWindow(i, window);
            }
            return window;
        }
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public void onMove(int i) {
        synchronized (this.mLock) {
            throwIfCursorIsClosed();
            this.mCursor.onMove(this.mCursor.getPosition(), i);
        }
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public void deactivate() {
        synchronized (this.mLock) {
            if (this.mCursor != null) {
                this.mCursor.deactivate();
            }
            closeFilledWindowLocked();
        }
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public void close() {
        synchronized (this.mLock) {
            disposeLocked();
        }
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public Bundle getExtras() {
        Bundle extras;
        synchronized (this.mLock) {
            throwIfCursorIsClosed();
            extras = this.mCursor.getExtras();
        }
        return extras;
    }

    @Override // com.huawei.nb.searchmanager.query.bulkcursor.IBulkCursor
    public Bundle respond(Bundle bundle) {
        Bundle respond;
        synchronized (this.mLock) {
            throwIfCursorIsClosed();
            respond = this.mCursor.respond(bundle);
        }
        return respond;
    }
}
