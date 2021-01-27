package android.test.mock;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import java.util.List;

@Deprecated
public class MockCursor implements Cursor {
    @Override // android.database.Cursor
    public int getColumnCount() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getColumnIndex(String columnName) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getColumnIndexOrThrow(String columnName) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public String getColumnName(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public String[] getColumnNames() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getCount() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isNull(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getInt(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public long getLong(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public short getShort(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public float getFloat(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public double getDouble(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public byte[] getBlob(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public String getString(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void setExtras(Bundle extras) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public Bundle getExtras() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getPosition() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isAfterLast() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isBeforeFirst() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isFirst() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isLast() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean move(int offset) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean moveToFirst() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean moveToLast() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean moveToNext() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean moveToPrevious() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean moveToPosition(int position) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    @Deprecated
    public void deactivate() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean isClosed() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    @Deprecated
    public boolean requery() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void registerContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void registerDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public Bundle respond(Bundle extras) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public boolean getWantsAllOnMoveCalls() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void setNotificationUris(ContentResolver cr, List<Uri> list) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public Uri getNotificationUri() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public List<Uri> getNotificationUris() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void unregisterContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public void unregisterDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }

    @Override // android.database.Cursor
    public int getType(int columnIndex) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
}
