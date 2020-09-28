package android.database;

import android.util.SparseArray;
import java.util.Map;

public class RedactingCursor extends CrossProcessCursorWrapper {
    private final SparseArray<Object> mRedactions;

    private RedactingCursor(Cursor cursor, SparseArray<Object> redactions) {
        super(cursor);
        this.mRedactions = redactions;
    }

    public static Cursor create(Cursor cursor, Map<String, Object> redactions) {
        SparseArray<Object> internalRedactions = new SparseArray<>();
        String[] columns = cursor.getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            if (redactions.containsKey(columns[i])) {
                internalRedactions.put(i, redactions.get(columns[i]));
            }
        }
        if (internalRedactions.size() == 0) {
            return cursor;
        }
        return new RedactingCursor(cursor, internalRedactions);
    }

    @Override // android.database.CrossProcessCursor, android.database.CrossProcessCursorWrapper
    public void fillWindow(int position, CursorWindow window) {
        DatabaseUtils.cursorFillWindow(this, position, window);
    }

    @Override // android.database.CrossProcessCursor, android.database.CrossProcessCursorWrapper
    public CursorWindow getWindow() {
        return null;
    }

    @Override // android.database.CursorWrapper
    public Cursor getWrappedCursor() {
        throw new UnsupportedOperationException("Returning underlying cursor risks leaking redacted data");
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public double getDouble(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return ((Double) this.mRedactions.valueAt(i)).doubleValue();
        }
        return super.getDouble(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public float getFloat(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return ((Float) this.mRedactions.valueAt(i)).floatValue();
        }
        return super.getFloat(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public int getInt(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return ((Integer) this.mRedactions.valueAt(i)).intValue();
        }
        return super.getInt(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public long getLong(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return ((Long) this.mRedactions.valueAt(i)).longValue();
        }
        return super.getLong(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public short getShort(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return ((Short) this.mRedactions.valueAt(i)).shortValue();
        }
        return super.getShort(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public String getString(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return (String) this.mRedactions.valueAt(i);
        }
        return super.getString(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            buffer.data = ((String) this.mRedactions.valueAt(i)).toCharArray();
            buffer.sizeCopied = buffer.data.length;
            return;
        }
        super.copyStringToBuffer(columnIndex, buffer);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public byte[] getBlob(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return (byte[]) this.mRedactions.valueAt(i);
        }
        return super.getBlob(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public int getType(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return DatabaseUtils.getTypeOfObject(this.mRedactions.valueAt(i));
        }
        return super.getType(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public boolean isNull(int columnIndex) {
        int i = this.mRedactions.indexOfKey(columnIndex);
        if (i >= 0) {
            return this.mRedactions.valueAt(i) == null;
        }
        return super.isNull(columnIndex);
    }
}
