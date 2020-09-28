package android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;
import java.util.Arrays;
import java.util.Objects;

public class TranslatingCursor extends CrossProcessCursorWrapper {
    private final int mAuxiliaryColumnIndex;
    private final Config mConfig;
    private final boolean mDropLast;
    private final ArraySet<Integer> mTranslateColumnIndices = new ArraySet<>();
    private final Translator mTranslator;

    public interface Translator {
        String translate(String str, int i, String str2, Cursor cursor);
    }

    public static class Config {
        public final String auxiliaryColumn;
        public final Uri baseUri;
        public final String[] translateColumns;

        public Config(Uri baseUri2, String auxiliaryColumn2, String... translateColumns2) {
            this.baseUri = baseUri2;
            this.auxiliaryColumn = auxiliaryColumn2;
            this.translateColumns = translateColumns2;
        }
    }

    public TranslatingCursor(Cursor cursor, Config config, Translator translator, boolean dropLast) {
        super(cursor);
        this.mConfig = (Config) Objects.requireNonNull(config);
        this.mTranslator = (Translator) Objects.requireNonNull(translator);
        this.mDropLast = dropLast;
        this.mAuxiliaryColumnIndex = cursor.getColumnIndexOrThrow(config.auxiliaryColumn);
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (ArrayUtils.contains(config.translateColumns, cursor.getColumnName(i))) {
                this.mTranslateColumnIndices.add(Integer.valueOf(i));
            }
        }
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public int getColumnCount() {
        if (this.mDropLast) {
            return super.getColumnCount() - 1;
        }
        return super.getColumnCount();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public String[] getColumnNames() {
        if (this.mDropLast) {
            return (String[]) Arrays.copyOfRange(super.getColumnNames(), 0, super.getColumnCount() - 1);
        }
        return super.getColumnNames();
    }

    public static Cursor query(Config config, Translator translator, SQLiteQueryBuilder qb, SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit, CancellationSignal signal) {
        String[] projectionIn2 = projectionIn;
        boolean z = true;
        boolean requestedAuxiliaryColumn = ArrayUtils.isEmpty(projectionIn) || ArrayUtils.contains(projectionIn2, config.auxiliaryColumn);
        if (!(ArrayUtils.isEmpty(projectionIn) || ArrayUtils.containsAny(projectionIn2, config.translateColumns))) {
            return qb.query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, limit, signal);
        }
        if (!requestedAuxiliaryColumn) {
            projectionIn2 = (String[]) ArrayUtils.appendElement(String.class, projectionIn2, config.auxiliaryColumn);
        }
        Cursor c = qb.query(db, projectionIn2, selection, selectionArgs, groupBy, having, sortOrder);
        if (requestedAuxiliaryColumn) {
            z = false;
        }
        return new TranslatingCursor(c, config, translator, z);
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
        throw new UnsupportedOperationException("Returning underlying cursor risks leaking data");
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public double getDouble(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getDouble(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public float getFloat(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getFloat(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public int getInt(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getInt(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public long getLong(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getLong(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public short getShort(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getShort(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public String getString(int columnIndex) {
        if (ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return this.mTranslator.translate(super.getString(columnIndex), this.mAuxiliaryColumnIndex, getColumnName(columnIndex), this);
        }
        return super.getString(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            super.copyStringToBuffer(columnIndex, buffer);
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public byte[] getBlob(int columnIndex) {
        if (!ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return super.getBlob(columnIndex);
        }
        throw new IllegalArgumentException();
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public int getType(int columnIndex) {
        if (ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return 3;
        }
        return super.getType(columnIndex);
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public boolean isNull(int columnIndex) {
        if (ArrayUtils.contains(this.mTranslateColumnIndices, Integer.valueOf(columnIndex))) {
            return getString(columnIndex) == null;
        }
        return super.isNull(columnIndex);
    }
}
