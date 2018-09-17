package android.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class ContentQueryMap extends Observable {
    private String[] mColumnNames;
    private ContentObserver mContentObserver;
    private volatile Cursor mCursor;
    private boolean mDirty = false;
    private Handler mHandlerForUpdateNotifications = null;
    private boolean mKeepUpdated = false;
    private int mKeyColumn;
    private Map<String, ContentValues> mValues = null;

    public ContentQueryMap(Cursor cursor, String columnNameOfKey, boolean keepUpdated, Handler handlerForUpdateNotifications) {
        this.mCursor = cursor;
        this.mColumnNames = this.mCursor.getColumnNames();
        this.mKeyColumn = this.mCursor.getColumnIndexOrThrow(columnNameOfKey);
        this.mHandlerForUpdateNotifications = handlerForUpdateNotifications;
        setKeepUpdated(keepUpdated);
        if (!keepUpdated) {
            readCursorIntoCache(cursor);
        }
    }

    public void setKeepUpdated(boolean keepUpdated) {
        if (keepUpdated != this.mKeepUpdated) {
            this.mKeepUpdated = keepUpdated;
            if (this.mKeepUpdated) {
                if (this.mHandlerForUpdateNotifications == null) {
                    this.mHandlerForUpdateNotifications = new Handler();
                }
                if (this.mContentObserver == null) {
                    this.mContentObserver = new ContentObserver(this.mHandlerForUpdateNotifications) {
                        public void onChange(boolean selfChange) {
                            if (ContentQueryMap.this.countObservers() != 0) {
                                ContentQueryMap.this.requery();
                            } else {
                                ContentQueryMap.this.mDirty = true;
                            }
                        }
                    };
                }
                this.mCursor.registerContentObserver(this.mContentObserver);
                this.mDirty = true;
            } else {
                this.mCursor.unregisterContentObserver(this.mContentObserver);
                this.mContentObserver = null;
            }
        }
    }

    public synchronized ContentValues getValues(String rowName) {
        if (this.mDirty) {
            requery();
        }
        return (ContentValues) this.mValues.get(rowName);
    }

    public void requery() {
        Cursor cursor = this.mCursor;
        if (cursor != null) {
            this.mDirty = false;
            if (cursor.requery()) {
                readCursorIntoCache(cursor);
                setChanged();
                notifyObservers();
            }
        }
    }

    private synchronized void readCursorIntoCache(Cursor cursor) {
        this.mValues = new HashMap(this.mValues != null ? this.mValues.size() : 0);
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < this.mColumnNames.length; i++) {
                if (i != this.mKeyColumn) {
                    values.put(this.mColumnNames[i], cursor.getString(i));
                }
            }
            this.mValues.put(cursor.getString(this.mKeyColumn), values);
        }
    }

    public synchronized Map<String, ContentValues> getRows() {
        if (this.mDirty) {
            requery();
        }
        return this.mValues;
    }

    public synchronized void close() {
        if (this.mContentObserver != null) {
            this.mCursor.unregisterContentObserver(this.mContentObserver);
            this.mContentObserver = null;
        }
        this.mCursor.close();
        this.mCursor = null;
    }

    protected void finalize() throws Throwable {
        if (this.mCursor != null) {
            close();
        }
        super.finalize();
    }
}
