package com.android.internal.database;

import android.annotation.UnsupportedAppUsage;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import java.lang.reflect.Array;

public class SortCursor extends AbstractCursor {
    private static final String TAG = "SortCursor";
    private final int ROWCACHESIZE = 64;
    private int[][] mCurRowNumCache;
    @UnsupportedAppUsage
    private Cursor mCursor;
    private int[] mCursorCache = new int[64];
    @UnsupportedAppUsage
    private Cursor[] mCursors;
    private int mLastCacheHit = -1;
    private DataSetObserver mObserver = new DataSetObserver() {
        /* class com.android.internal.database.SortCursor.AnonymousClass1 */

        @Override // android.database.DataSetObserver
        public void onChanged() {
            SortCursor.this.mPos = -1;
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            SortCursor.this.mPos = -1;
        }
    };
    private int[] mRowNumCache = new int[64];
    private int[] mSortColumns;

    @UnsupportedAppUsage
    public SortCursor(Cursor[] cursors, String sortcolumn) {
        this.mCursors = cursors;
        int length = this.mCursors.length;
        this.mSortColumns = new int[length];
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].registerDataSetObserver(this.mObserver);
                this.mCursors[i].moveToFirst();
                this.mSortColumns[i] = this.mCursors[i].getColumnIndexOrThrow(sortcolumn);
            }
        }
        this.mCursor = null;
        String smallest = "";
        for (int j = 0; j < length; j++) {
            Cursor[] cursorArr2 = this.mCursors;
            if (cursorArr2[j] != null && !cursorArr2[j].isAfterLast()) {
                String current = this.mCursors[j].getString(this.mSortColumns[j]);
                if (this.mCursor == null || (current != null && current.compareToIgnoreCase(smallest) < 0)) {
                    smallest = current != null ? current : smallest;
                    this.mCursor = this.mCursors[j];
                }
            }
        }
        for (int i2 = this.mRowNumCache.length - 1; i2 >= 0; i2--) {
            this.mRowNumCache[i2] = -2;
        }
        this.mCurRowNumCache = (int[][]) Array.newInstance(int.class, 64, length);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getCount() {
        int count = 0;
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                count += cursorArr[i].getCount();
            }
        }
        return count;
    }

    @Override // android.database.CrossProcessCursor, android.database.AbstractCursor
    public boolean onMove(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }
        int cache_entry = newPosition % 64;
        if (this.mRowNumCache[cache_entry] == newPosition) {
            int which = this.mCursorCache[cache_entry];
            this.mCursor = this.mCursors[which];
            Cursor cursor = this.mCursor;
            if (cursor == null) {
                Log.w(TAG, "onMove: cache results in a null cursor.");
                return false;
            }
            cursor.moveToPosition(this.mCurRowNumCache[cache_entry][which]);
            this.mLastCacheHit = cache_entry;
            return true;
        }
        this.mCursor = null;
        int length = this.mCursors.length;
        if (this.mLastCacheHit >= 0) {
            for (int i = 0; i < length; i++) {
                Cursor[] cursorArr = this.mCursors;
                if (cursorArr[i] != null) {
                    cursorArr[i].moveToPosition(this.mCurRowNumCache[this.mLastCacheHit][i]);
                }
            }
        }
        if (newPosition < oldPosition || oldPosition == -1) {
            for (int i2 = 0; i2 < length; i2++) {
                Cursor[] cursorArr2 = this.mCursors;
                if (cursorArr2[i2] != null) {
                    cursorArr2[i2].moveToFirst();
                }
            }
            oldPosition = 0;
        }
        if (oldPosition < 0) {
            oldPosition = 0;
        }
        int smallestIdx = -1;
        for (int i3 = oldPosition; i3 <= newPosition; i3++) {
            String smallest = "";
            smallestIdx = -1;
            for (int j = 0; j < length; j++) {
                Cursor[] cursorArr3 = this.mCursors;
                if (cursorArr3[j] != null && !cursorArr3[j].isAfterLast()) {
                    String current = this.mCursors[j].getString(this.mSortColumns[j]);
                    if (smallestIdx < 0 || current.compareToIgnoreCase(smallest) < 0) {
                        smallest = current;
                        smallestIdx = j;
                    }
                }
            }
            if (i3 == newPosition) {
                break;
            }
            Cursor[] cursorArr4 = this.mCursors;
            if (cursorArr4[smallestIdx] != null) {
                cursorArr4[smallestIdx].moveToNext();
            }
        }
        this.mCursor = this.mCursors[smallestIdx];
        this.mRowNumCache[cache_entry] = newPosition;
        this.mCursorCache[cache_entry] = smallestIdx;
        for (int i4 = 0; i4 < length; i4++) {
            Cursor[] cursorArr5 = this.mCursors;
            if (cursorArr5[i4] != null) {
                this.mCurRowNumCache[cache_entry][i4] = cursorArr5[i4].getPosition();
            }
        }
        this.mLastCacheHit = -1;
        return true;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String getString(int column) {
        return this.mCursor.getString(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public short getShort(int column) {
        return this.mCursor.getShort(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getInt(int column) {
        return this.mCursor.getInt(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public long getLong(int column) {
        return this.mCursor.getLong(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public float getFloat(int column) {
        return this.mCursor.getFloat(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public double getDouble(int column) {
        return this.mCursor.getDouble(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getType(int column) {
        return this.mCursor.getType(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean isNull(int column) {
        return this.mCursor.isNull(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public byte[] getBlob(int column) {
        return this.mCursor.getBlob(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        Cursor cursor = this.mCursor;
        if (cursor != null) {
            return cursor.getColumnNames();
        }
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                return cursorArr[i].getColumnNames();
            }
        }
        throw new IllegalStateException("No cursor that can return names");
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void deactivate() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].deactivate();
            }
        }
    }

    @Override // java.io.Closeable, android.database.AbstractCursor, java.lang.AutoCloseable, android.database.Cursor
    public void close() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].close();
            }
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void registerDataSetObserver(DataSetObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].registerDataSetObserver(observer);
            }
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void unregisterDataSetObserver(DataSetObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].unregisterDataSetObserver(observer);
            }
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean requery() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null && !cursorArr[i].requery()) {
                return false;
            }
        }
        return true;
    }
}
