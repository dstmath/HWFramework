package android.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCursor implements CrossProcessCursor {
    private static final String TAG = "Cursor";
    @Deprecated
    protected boolean mClosed;
    private final ContentObservable mContentObservable = new ContentObservable();
    @Deprecated
    protected ContentResolver mContentResolver;
    protected Long mCurrentRowID;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private Bundle mExtras = Bundle.EMPTY;
    private Uri mNotifyUri;
    @Deprecated
    protected int mPos = -1;
    protected int mRowIdColumnIndex;
    private ContentObserver mSelfObserver;
    private final Object mSelfObserverLock = new Object();
    private boolean mSelfObserverRegistered;
    protected HashMap<Long, Map<String, Object>> mUpdatedRows;

    protected static class SelfContentObserver extends ContentObserver {
        WeakReference<AbstractCursor> mCursor;

        public SelfContentObserver(AbstractCursor cursor) {
            super(null);
            this.mCursor = new WeakReference(cursor);
        }

        public boolean deliverSelfNotifications() {
            return false;
        }

        public void onChange(boolean selfChange) {
            AbstractCursor cursor = (AbstractCursor) this.mCursor.get();
            if (cursor != null) {
                cursor.onChange(false);
            }
        }
    }

    public abstract String[] getColumnNames();

    public abstract int getCount();

    public abstract double getDouble(int i);

    public abstract float getFloat(int i);

    public abstract int getInt(int i);

    public abstract long getLong(int i);

    public abstract short getShort(int i);

    public abstract String getString(int i);

    public abstract boolean isNull(int i);

    public int getType(int column) {
        return 3;
    }

    public byte[] getBlob(int column) {
        throw new UnsupportedOperationException("getBlob is not supported");
    }

    public CursorWindow getWindow() {
        return null;
    }

    public int getColumnCount() {
        return getColumnNames().length;
    }

    public void deactivate() {
        onDeactivateOrClose();
    }

    protected void onDeactivateOrClose() {
        if (this.mSelfObserver != null) {
            this.mContentResolver.unregisterContentObserver(this.mSelfObserver);
            this.mSelfObserverRegistered = false;
        }
        this.mDataSetObservable.notifyInvalidated();
    }

    public boolean requery() {
        if (!(this.mSelfObserver == null || this.mSelfObserverRegistered)) {
            this.mContentResolver.registerContentObserver(this.mNotifyUri, true, this.mSelfObserver);
            this.mSelfObserverRegistered = true;
        }
        this.mDataSetObservable.notifyChanged();
        return true;
    }

    public boolean isClosed() {
        return this.mClosed;
    }

    public void close() {
        this.mClosed = true;
        this.mContentObservable.unregisterAll();
        onDeactivateOrClose();
    }

    public boolean onMove(int oldPosition, int newPosition) {
        return true;
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        String result = getString(columnIndex);
        if (result != null) {
            char[] data = buffer.data;
            if (data == null || data.length < result.length()) {
                buffer.data = result.toCharArray();
            } else {
                result.getChars(0, result.length(), data, 0);
            }
            buffer.sizeCopied = result.length();
            return;
        }
        buffer.sizeCopied = 0;
    }

    public final int getPosition() {
        return this.mPos;
    }

    public final boolean moveToPosition(int position) {
        int count = getCount();
        if (position >= count) {
            this.mPos = count;
            return false;
        } else if (position < 0) {
            this.mPos = -1;
            return false;
        } else if (position == this.mPos) {
            return true;
        } else {
            boolean result = onMove(this.mPos, position);
            if (result) {
                this.mPos = position;
            } else {
                this.mPos = -1;
            }
            return result;
        }
    }

    public void fillWindow(int position, CursorWindow window) {
        DatabaseUtils.cursorFillWindow(this, position, window);
    }

    public final boolean move(int offset) {
        return moveToPosition(this.mPos + offset);
    }

    public final boolean moveToFirst() {
        return moveToPosition(0);
    }

    public final boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    public final boolean moveToNext() {
        return moveToPosition(this.mPos + 1);
    }

    public final boolean moveToPrevious() {
        return moveToPosition(this.mPos - 1);
    }

    public final boolean isFirst() {
        return this.mPos == 0 && getCount() != 0;
    }

    public final boolean isLast() {
        int cnt = getCount();
        if (this.mPos != cnt - 1 || cnt == 0) {
            return false;
        }
        return true;
    }

    public final boolean isBeforeFirst() {
        boolean z = true;
        if (getCount() == 0) {
            return true;
        }
        if (this.mPos != -1) {
            z = false;
        }
        return z;
    }

    public final boolean isAfterLast() {
        boolean z = true;
        if (getCount() == 0) {
            return true;
        }
        if (this.mPos != getCount()) {
            z = false;
        }
        return z;
    }

    public int getColumnIndex(String columnName) {
        int periodIndex = columnName.lastIndexOf(46);
        if (periodIndex != -1) {
            Log.e(TAG, "requesting column name with table name -- " + columnName, new Exception());
            columnName = columnName.substring(periodIndex + 1);
        }
        String[] columnNames = getColumnNames();
        int length = columnNames.length;
        for (int i = 0; i < length; i++) {
            if (columnNames[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public int getColumnIndexOrThrow(String columnName) {
        int index = getColumnIndex(columnName);
        if (index >= 0) {
            return index;
        }
        throw new IllegalArgumentException("column '" + columnName + "' does not exist");
    }

    public String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }

    public void registerContentObserver(ContentObserver observer) {
        this.mContentObservable.registerObserver(observer);
    }

    public void unregisterContentObserver(ContentObserver observer) {
        if (!this.mClosed) {
            this.mContentObservable.unregisterObserver(observer);
        }
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    protected void onChange(boolean selfChange) {
        synchronized (this.mSelfObserverLock) {
            this.mContentObservable.dispatchChange(selfChange, null);
            if (this.mNotifyUri != null && selfChange) {
                this.mContentResolver.notifyChange(this.mNotifyUri, this.mSelfObserver);
            }
        }
    }

    public void setNotificationUri(ContentResolver cr, Uri notifyUri) {
        setNotificationUri(cr, notifyUri, UserHandle.myUserId());
    }

    public void setNotificationUri(ContentResolver cr, Uri notifyUri, int userHandle) {
        synchronized (this.mSelfObserverLock) {
            this.mNotifyUri = notifyUri;
            this.mContentResolver = cr;
            if (this.mSelfObserver != null) {
                this.mContentResolver.unregisterContentObserver(this.mSelfObserver);
            }
            this.mSelfObserver = new SelfContentObserver(this);
            this.mContentResolver.registerContentObserver(this.mNotifyUri, true, this.mSelfObserver, userHandle);
            this.mSelfObserverRegistered = true;
        }
    }

    public Uri getNotificationUri() {
        Uri uri;
        synchronized (this.mSelfObserverLock) {
            uri = this.mNotifyUri;
        }
        return uri;
    }

    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    public void setExtras(Bundle extras) {
        if (extras == null) {
            extras = Bundle.EMPTY;
        }
        this.mExtras = extras;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }

    @Deprecated
    protected boolean isFieldUpdated(int columnIndex) {
        return false;
    }

    @Deprecated
    protected Object getUpdatedField(int columnIndex) {
        return null;
    }

    protected void checkPosition() {
        if (-1 == this.mPos || getCount() == this.mPos) {
            throw new CursorIndexOutOfBoundsException(this.mPos, getCount());
        }
    }

    protected void finalize() {
        if (this.mSelfObserver != null && this.mSelfObserverRegistered) {
            this.mContentResolver.unregisterContentObserver(this.mSelfObserver);
        }
        try {
            if (!this.mClosed) {
                close();
            }
        } catch (Exception e) {
        }
    }
}
