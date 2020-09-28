package android.database;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private Bundle mExtras = Bundle.EMPTY;
    @UnsupportedAppUsage
    private Uri mNotifyUri;
    private List<Uri> mNotifyUris;
    @Deprecated
    protected int mPos = -1;
    protected int mRowIdColumnIndex;
    private ContentObserver mSelfObserver;
    private final Object mSelfObserverLock = new Object();
    private boolean mSelfObserverRegistered;
    protected HashMap<Long, Map<String, Object>> mUpdatedRows;

    @Override // android.database.Cursor
    public abstract String[] getColumnNames();

    @Override // android.database.Cursor
    public abstract int getCount();

    @Override // android.database.Cursor
    public abstract double getDouble(int i);

    @Override // android.database.Cursor
    public abstract float getFloat(int i);

    @Override // android.database.Cursor
    public abstract int getInt(int i);

    @Override // android.database.Cursor
    public abstract long getLong(int i);

    @Override // android.database.Cursor
    public abstract short getShort(int i);

    @Override // android.database.Cursor
    public abstract String getString(int i);

    @Override // android.database.Cursor
    public abstract boolean isNull(int i);

    @Override // android.database.Cursor
    public int getType(int column) {
        return 3;
    }

    @Override // android.database.Cursor
    public byte[] getBlob(int column) {
        throw new UnsupportedOperationException("getBlob is not supported");
    }

    @Override // android.database.CrossProcessCursor
    public CursorWindow getWindow() {
        return null;
    }

    @Override // android.database.Cursor
    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override // android.database.Cursor
    public void deactivate() {
        onDeactivateOrClose();
    }

    /* access modifiers changed from: protected */
    public void onDeactivateOrClose() {
        ContentObserver contentObserver = this.mSelfObserver;
        if (contentObserver != null) {
            this.mContentResolver.unregisterContentObserver(contentObserver);
            this.mSelfObserverRegistered = false;
        }
        this.mDataSetObservable.notifyInvalidated();
    }

    @Override // android.database.Cursor
    public boolean requery() {
        if (this.mSelfObserver != null && !this.mSelfObserverRegistered) {
            int size = this.mNotifyUris.size();
            for (int i = 0; i < size; i++) {
                this.mContentResolver.registerContentObserver(this.mNotifyUris.get(i), true, this.mSelfObserver);
            }
            this.mSelfObserverRegistered = true;
        }
        this.mDataSetObservable.notifyChanged();
        return true;
    }

    @Override // android.database.Cursor
    public boolean isClosed() {
        return this.mClosed;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable, android.database.Cursor
    public void close() {
        this.mClosed = true;
        this.mContentObservable.unregisterAll();
        onDeactivateOrClose();
    }

    @Override // android.database.CrossProcessCursor
    public boolean onMove(int oldPosition, int newPosition) {
        return true;
    }

    @Override // android.database.Cursor
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

    @Override // android.database.Cursor
    public final int getPosition() {
        return this.mPos;
    }

    @Override // android.database.Cursor
    public final boolean moveToPosition(int position) {
        int count = getCount();
        if (position >= count) {
            this.mPos = count;
            return false;
        } else if (position < 0) {
            this.mPos = -1;
            return false;
        } else {
            int i = this.mPos;
            if (position == i) {
                return true;
            }
            boolean result = onMove(i, position);
            if (!result) {
                this.mPos = -1;
            } else {
                this.mPos = position;
            }
            return result;
        }
    }

    @Override // android.database.CrossProcessCursor
    public void fillWindow(int position, CursorWindow window) {
        DatabaseUtils.cursorFillWindow(this, position, window);
    }

    @Override // android.database.Cursor
    public final boolean move(int offset) {
        return moveToPosition(this.mPos + offset);
    }

    @Override // android.database.Cursor
    public final boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override // android.database.Cursor
    public final boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override // android.database.Cursor
    public final boolean moveToNext() {
        return moveToPosition(this.mPos + 1);
    }

    @Override // android.database.Cursor
    public final boolean moveToPrevious() {
        return moveToPosition(this.mPos - 1);
    }

    @Override // android.database.Cursor
    public final boolean isFirst() {
        return this.mPos == 0 && getCount() != 0;
    }

    @Override // android.database.Cursor
    public final boolean isLast() {
        int cnt = getCount();
        return this.mPos == cnt + -1 && cnt != 0;
    }

    @Override // android.database.Cursor
    public final boolean isBeforeFirst() {
        if (getCount() == 0 || this.mPos == -1) {
            return true;
        }
        return false;
    }

    @Override // android.database.Cursor
    public final boolean isAfterLast() {
        if (getCount() == 0 || this.mPos == getCount()) {
            return true;
        }
        return false;
    }

    @Override // android.database.Cursor
    public int getColumnIndex(String columnName) {
        int periodIndex = columnName.lastIndexOf(46);
        if (periodIndex != -1) {
            Exception e = new Exception();
            Log.e(TAG, "requesting column name with table name -- " + columnName, e);
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

    @Override // android.database.Cursor
    public int getColumnIndexOrThrow(String columnName) {
        int index = getColumnIndex(columnName);
        if (index >= 0) {
            return index;
        }
        String availableColumns = "";
        try {
            availableColumns = Arrays.toString(getColumnNames());
        } catch (Exception e) {
            Log.d(TAG, "Cannot collect column names for debug purposes", e);
        }
        throw new IllegalArgumentException("column '" + columnName + "' does not exist. Available columns: " + availableColumns);
    }

    @Override // android.database.Cursor
    public String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }

    @Override // android.database.Cursor
    public void registerContentObserver(ContentObserver observer) {
        this.mContentObservable.registerObserver(observer);
    }

    @Override // android.database.Cursor
    public void unregisterContentObserver(ContentObserver observer) {
        if (!this.mClosed) {
            this.mContentObservable.unregisterObserver(observer);
        }
    }

    @Override // android.database.Cursor
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    @Override // android.database.Cursor
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    /* access modifiers changed from: protected */
    public void onChange(boolean selfChange) {
        synchronized (this.mSelfObserverLock) {
            this.mContentObservable.dispatchChange(selfChange, null);
            if (this.mNotifyUris != null && selfChange) {
                int size = this.mNotifyUris.size();
                for (int i = 0; i < size; i++) {
                    this.mContentResolver.notifyChange(this.mNotifyUris.get(i), this.mSelfObserver);
                }
            }
        }
    }

    @Override // android.database.Cursor
    public void setNotificationUri(ContentResolver cr, Uri notifyUri) {
        setNotificationUris(cr, Arrays.asList(notifyUri));
    }

    @Override // android.database.Cursor
    public void setNotificationUris(ContentResolver cr, List<Uri> notifyUris) {
        Preconditions.checkNotNull(cr);
        Preconditions.checkNotNull(notifyUris);
        setNotificationUris(cr, notifyUris, cr.getUserId(), true);
    }

    public void setNotificationUris(ContentResolver cr, List<Uri> notifyUris, int userHandle, boolean registerSelfObserver) {
        synchronized (this.mSelfObserverLock) {
            this.mNotifyUris = notifyUris;
            this.mNotifyUri = this.mNotifyUris.get(0);
            this.mContentResolver = cr;
            if (this.mSelfObserver != null) {
                this.mContentResolver.unregisterContentObserver(this.mSelfObserver);
                this.mSelfObserverRegistered = false;
            }
            if (registerSelfObserver) {
                this.mSelfObserver = new SelfContentObserver(this);
                int size = this.mNotifyUris.size();
                for (int i = 0; i < size; i++) {
                    this.mContentResolver.registerContentObserver(this.mNotifyUris.get(i), true, this.mSelfObserver, userHandle);
                }
                this.mSelfObserverRegistered = true;
            }
        }
    }

    @Override // android.database.Cursor
    public Uri getNotificationUri() {
        Uri uri;
        synchronized (this.mSelfObserverLock) {
            uri = this.mNotifyUri;
        }
        return uri;
    }

    @Override // android.database.Cursor
    public List<Uri> getNotificationUris() {
        List<Uri> list;
        synchronized (this.mSelfObserverLock) {
            list = this.mNotifyUris;
        }
        return list;
    }

    @Override // android.database.Cursor
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override // android.database.Cursor
    public void setExtras(Bundle extras) {
        this.mExtras = extras == null ? Bundle.EMPTY : extras;
    }

    @Override // android.database.Cursor
    public Bundle getExtras() {
        return this.mExtras;
    }

    @Override // android.database.Cursor
    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean isFieldUpdated(int columnIndex) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Object getUpdatedField(int columnIndex) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void checkPosition() {
        if (-1 == this.mPos || getCount() == this.mPos) {
            throw new CursorIndexOutOfBoundsException(this.mPos, getCount());
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        ContentObserver contentObserver = this.mSelfObserver;
        if (contentObserver != null && this.mSelfObserverRegistered) {
            this.mContentResolver.unregisterContentObserver(contentObserver);
        }
        try {
            if (!this.mClosed) {
                close();
            }
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public static class SelfContentObserver extends ContentObserver {
        WeakReference<AbstractCursor> mCursor;

        public SelfContentObserver(AbstractCursor cursor) {
            super(null);
            this.mCursor = new WeakReference<>(cursor);
        }

        @Override // android.database.ContentObserver
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            AbstractCursor cursor = this.mCursor.get();
            if (cursor != null) {
                cursor.onChange(false);
            }
        }
    }
}
