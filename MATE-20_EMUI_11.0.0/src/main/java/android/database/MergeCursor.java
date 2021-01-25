package android.database;

public class MergeCursor extends AbstractCursor {
    private Cursor mCursor;
    private Cursor[] mCursors;
    private DataSetObserver mObserver = new DataSetObserver() {
        /* class android.database.MergeCursor.AnonymousClass1 */

        @Override // android.database.DataSetObserver
        public void onChanged() {
            MergeCursor.this.mPos = -1;
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            MergeCursor.this.mPos = -1;
        }
    };

    public MergeCursor(Cursor[] cursors) {
        this.mCursors = cursors;
        this.mCursor = cursors[0];
        int i = 0;
        while (true) {
            Cursor[] cursorArr = this.mCursors;
            if (i < cursorArr.length) {
                if (cursorArr[i] != null) {
                    cursorArr[i].registerDataSetObserver(this.mObserver);
                }
                i++;
            } else {
                return;
            }
        }
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

    @Override // android.database.AbstractCursor, android.database.CrossProcessCursor
    public boolean onMove(int oldPosition, int newPosition) {
        this.mCursor = null;
        int cursorStartPos = 0;
        int length = this.mCursors.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                if (newPosition < cursorArr[i].getCount() + cursorStartPos) {
                    this.mCursor = this.mCursors[i];
                    break;
                }
                cursorStartPos += this.mCursors[i].getCount();
            }
            i++;
        }
        Cursor cursor = this.mCursor;
        if (cursor != null) {
            return cursor.moveToPosition(newPosition - cursorStartPos);
        }
        return false;
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
        return new String[0];
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
        super.deactivate();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].close();
            }
        }
        super.close();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void registerContentObserver(ContentObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].registerContentObserver(observer);
            }
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public void unregisterContentObserver(ContentObserver observer) {
        int length = this.mCursors.length;
        for (int i = 0; i < length; i++) {
            Cursor[] cursorArr = this.mCursors;
            if (cursorArr[i] != null) {
                cursorArr[i].unregisterContentObserver(observer);
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
