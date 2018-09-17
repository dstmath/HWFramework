package com.android.internal.database;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.LogException;
import java.lang.reflect.Array;

public class SortCursor extends AbstractCursor {
    private static final String TAG = "SortCursor";
    private final int ROWCACHESIZE = 64;
    private int[][] mCurRowNumCache;
    private Cursor mCursor;
    private int[] mCursorCache = new int[64];
    private Cursor[] mCursors;
    private int mLastCacheHit = -1;
    private DataSetObserver mObserver = new DataSetObserver() {
        public void onChanged() {
            SortCursor.this.mPos = -1;
        }

        public void onInvalidated() {
            SortCursor.this.mPos = -1;
        }
    };
    private int[] mRowNumCache = new int[64];
    private int[] mSortColumns;

    public SortCursor(Cursor[] cursors, String sortcolumn) {
        int i;
        this.mCursors = cursors;
        int length = this.mCursors.length;
        this.mSortColumns = new int[length];
        for (i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCursors[i].registerDataSetObserver(this.mObserver);
                this.mCursors[i].moveToFirst();
                this.mSortColumns[i] = this.mCursors[i].getColumnIndexOrThrow(sortcolumn);
            }
        }
        this.mCursor = null;
        String smallest = LogException.NO_VALUE;
        int j = 0;
        while (j < length) {
            if (!(this.mCursors[j] == null || this.mCursors[j].isAfterLast())) {
                String current = this.mCursors[j].getString(this.mSortColumns[j]);
                if (this.mCursor == null || (current != null && current.compareToIgnoreCase(smallest) < 0)) {
                    if (current != null) {
                        smallest = current;
                    }
                    this.mCursor = this.mCursors[j];
                }
            }
            j++;
        }
        for (i = this.mRowNumCache.length - 1; i >= 0; i--) {
            this.mRowNumCache[i] = -2;
        }
        this.mCurRowNumCache = (int[][]) Array.newInstance(Integer.TYPE, new int[]{64, length});
    }

    public int getCount() {
        int count = 0;
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                count += this.mCursors[i].getCount();
            }
        }
        return count;
    }

    public boolean onMove(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }
        int cache_entry = newPosition % 64;
        if (this.mRowNumCache[cache_entry] == newPosition) {
            int which = this.mCursorCache[cache_entry];
            this.mCursor = this.mCursors[which];
            if (this.mCursor == null) {
                Log.w(TAG, "onMove: cache results in a null cursor.");
                return false;
            }
            this.mCursor.moveToPosition(this.mCurRowNumCache[cache_entry][which]);
            this.mLastCacheHit = cache_entry;
            return true;
        }
        int i;
        this.mCursor = null;
        int length = this.mCursors.length;
        if (this.mLastCacheHit >= 0) {
            for (i = 0; i < length; i++) {
                if (this.mCursors[i] != null) {
                    this.mCursors[i].moveToPosition(this.mCurRowNumCache[this.mLastCacheHit][i]);
                }
            }
        }
        if (newPosition < oldPosition || oldPosition == -1) {
            for (i = 0; i < length; i++) {
                if (this.mCursors[i] != null) {
                    this.mCursors[i].moveToFirst();
                }
            }
            oldPosition = 0;
        }
        if (oldPosition < 0) {
            oldPosition = 0;
        }
        int smallestIdx = -1;
        for (i = oldPosition; i <= newPosition; i++) {
            String smallest = LogException.NO_VALUE;
            smallestIdx = -1;
            int j = 0;
            while (j < length) {
                if (!(this.mCursors[j] == null || this.mCursors[j].isAfterLast())) {
                    String current = this.mCursors[j].getString(this.mSortColumns[j]);
                    if (smallestIdx < 0 || current.compareToIgnoreCase(smallest) < 0) {
                        smallest = current;
                        smallestIdx = j;
                    }
                }
                j++;
            }
            if (i == newPosition) {
                break;
            }
            if (this.mCursors[smallestIdx] != null) {
                this.mCursors[smallestIdx].moveToNext();
            }
        }
        this.mCursor = this.mCursors[smallestIdx];
        this.mRowNumCache[cache_entry] = newPosition;
        this.mCursorCache[cache_entry] = smallestIdx;
        for (i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCurRowNumCache[cache_entry][i] = this.mCursors[i].getPosition();
            }
        }
        this.mLastCacheHit = -1;
        return true;
    }

    public String getString(int column) {
        return this.mCursor.getString(column);
    }

    public short getShort(int column) {
        return this.mCursor.getShort(column);
    }

    public int getInt(int column) {
        return this.mCursor.getInt(column);
    }

    public long getLong(int column) {
        return this.mCursor.getLong(column);
    }

    public float getFloat(int column) {
        return this.mCursor.getFloat(column);
    }

    public double getDouble(int column) {
        return this.mCursor.getDouble(column);
    }

    public int getType(int column) {
        return this.mCursor.getType(column);
    }

    public boolean isNull(int column) {
        return this.mCursor.isNull(column);
    }

    public byte[] getBlob(int column) {
        return this.mCursor.getBlob(column);
    }

    public String[] getColumnNames() {
        if (this.mCursor != null) {
            return this.mCursor.getColumnNames();
        }
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                return this.mCursors[i].getColumnNames();
            }
        }
        throw new IllegalStateException("No cursor that can return names");
    }

    public void deactivate() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCursors[i].deactivate();
            }
        }
    }

    public void close() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCursors[i].close();
            }
        }
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCursors[i].registerDataSetObserver(observer);
            }
        }
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            if (this.mCursors[i] != null) {
                this.mCursors[i].unregisterDataSetObserver(observer);
            }
        }
    }

    public boolean requery() {
        int length = this.mCursors.length;
        int i = 0;
        while (i < length) {
            if (this.mCursors[i] != null && !this.mCursors[i].requery()) {
                return false;
            }
            i++;
        }
        return true;
    }
}
