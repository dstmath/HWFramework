package android.database;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import java.util.List;

public class CursorWrapper implements Cursor {
    @UnsupportedAppUsage
    protected final Cursor mCursor;

    public CursorWrapper(Cursor cursor) {
        this.mCursor = cursor;
    }

    public Cursor getWrappedCursor() {
        return this.mCursor;
    }

    @Override // android.database.Cursor, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        this.mCursor.close();
    }

    @Override // android.database.Cursor
    public boolean isClosed() {
        return this.mCursor.isClosed();
    }

    @Override // android.database.Cursor
    public int getCount() {
        return this.mCursor.getCount();
    }

    @Override // android.database.Cursor
    @Deprecated
    public void deactivate() {
        this.mCursor.deactivate();
    }

    @Override // android.database.Cursor
    public boolean moveToFirst() {
        return this.mCursor.moveToFirst();
    }

    @Override // android.database.Cursor
    public int getColumnCount() {
        return this.mCursor.getColumnCount();
    }

    @Override // android.database.Cursor
    public int getColumnIndex(String columnName) {
        return this.mCursor.getColumnIndex(columnName);
    }

    @Override // android.database.Cursor
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return this.mCursor.getColumnIndexOrThrow(columnName);
    }

    @Override // android.database.Cursor
    public String getColumnName(int columnIndex) {
        return this.mCursor.getColumnName(columnIndex);
    }

    @Override // android.database.Cursor
    public String[] getColumnNames() {
        return this.mCursor.getColumnNames();
    }

    @Override // android.database.Cursor
    public double getDouble(int columnIndex) {
        return this.mCursor.getDouble(columnIndex);
    }

    @Override // android.database.Cursor
    public void setExtras(Bundle extras) {
        this.mCursor.setExtras(extras);
    }

    @Override // android.database.Cursor
    public Bundle getExtras() {
        return this.mCursor.getExtras();
    }

    @Override // android.database.Cursor
    public float getFloat(int columnIndex) {
        return this.mCursor.getFloat(columnIndex);
    }

    @Override // android.database.Cursor
    public int getInt(int columnIndex) {
        return this.mCursor.getInt(columnIndex);
    }

    @Override // android.database.Cursor
    public long getLong(int columnIndex) {
        return this.mCursor.getLong(columnIndex);
    }

    @Override // android.database.Cursor
    public short getShort(int columnIndex) {
        return this.mCursor.getShort(columnIndex);
    }

    @Override // android.database.Cursor
    public String getString(int columnIndex) {
        return this.mCursor.getString(columnIndex);
    }

    @Override // android.database.Cursor
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        this.mCursor.copyStringToBuffer(columnIndex, buffer);
    }

    @Override // android.database.Cursor
    public byte[] getBlob(int columnIndex) {
        return this.mCursor.getBlob(columnIndex);
    }

    @Override // android.database.Cursor
    public boolean getWantsAllOnMoveCalls() {
        return this.mCursor.getWantsAllOnMoveCalls();
    }

    @Override // android.database.Cursor
    public boolean isAfterLast() {
        return this.mCursor.isAfterLast();
    }

    @Override // android.database.Cursor
    public boolean isBeforeFirst() {
        return this.mCursor.isBeforeFirst();
    }

    @Override // android.database.Cursor
    public boolean isFirst() {
        return this.mCursor.isFirst();
    }

    @Override // android.database.Cursor
    public boolean isLast() {
        return this.mCursor.isLast();
    }

    @Override // android.database.Cursor
    public int getType(int columnIndex) {
        return this.mCursor.getType(columnIndex);
    }

    @Override // android.database.Cursor
    public boolean isNull(int columnIndex) {
        return this.mCursor.isNull(columnIndex);
    }

    @Override // android.database.Cursor
    public boolean moveToLast() {
        return this.mCursor.moveToLast();
    }

    @Override // android.database.Cursor
    public boolean move(int offset) {
        return this.mCursor.move(offset);
    }

    @Override // android.database.Cursor
    public boolean moveToPosition(int position) {
        return this.mCursor.moveToPosition(position);
    }

    @Override // android.database.Cursor
    public boolean moveToNext() {
        return this.mCursor.moveToNext();
    }

    @Override // android.database.Cursor
    public int getPosition() {
        return this.mCursor.getPosition();
    }

    @Override // android.database.Cursor
    public boolean moveToPrevious() {
        return this.mCursor.moveToPrevious();
    }

    @Override // android.database.Cursor
    public void registerContentObserver(ContentObserver observer) {
        this.mCursor.registerContentObserver(observer);
    }

    @Override // android.database.Cursor
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mCursor.registerDataSetObserver(observer);
    }

    @Override // android.database.Cursor
    @Deprecated
    public boolean requery() {
        return this.mCursor.requery();
    }

    @Override // android.database.Cursor
    public Bundle respond(Bundle extras) {
        return this.mCursor.respond(extras);
    }

    @Override // android.database.Cursor
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        this.mCursor.setNotificationUri(cr, uri);
    }

    @Override // android.database.Cursor
    public void setNotificationUris(ContentResolver cr, List<Uri> uris) {
        this.mCursor.setNotificationUris(cr, uris);
    }

    @Override // android.database.Cursor
    public Uri getNotificationUri() {
        return this.mCursor.getNotificationUri();
    }

    @Override // android.database.Cursor
    public List<Uri> getNotificationUris() {
        return this.mCursor.getNotificationUris();
    }

    @Override // android.database.Cursor
    public void unregisterContentObserver(ContentObserver observer) {
        this.mCursor.unregisterContentObserver(observer);
    }

    @Override // android.database.Cursor
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mCursor.unregisterDataSetObserver(observer);
    }
}
