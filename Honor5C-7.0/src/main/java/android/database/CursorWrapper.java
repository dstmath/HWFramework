package android.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

public class CursorWrapper implements Cursor {
    protected final Cursor mCursor;

    public CursorWrapper(Cursor cursor) {
        this.mCursor = cursor;
    }

    public Cursor getWrappedCursor() {
        return this.mCursor;
    }

    public void close() {
        this.mCursor.close();
    }

    public boolean isClosed() {
        return this.mCursor.isClosed();
    }

    public int getCount() {
        return this.mCursor.getCount();
    }

    @Deprecated
    public void deactivate() {
        this.mCursor.deactivate();
    }

    public boolean moveToFirst() {
        return this.mCursor.moveToFirst();
    }

    public int getColumnCount() {
        return this.mCursor.getColumnCount();
    }

    public int getColumnIndex(String columnName) {
        return this.mCursor.getColumnIndex(columnName);
    }

    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return this.mCursor.getColumnIndexOrThrow(columnName);
    }

    public String getColumnName(int columnIndex) {
        return this.mCursor.getColumnName(columnIndex);
    }

    public String[] getColumnNames() {
        return this.mCursor.getColumnNames();
    }

    public double getDouble(int columnIndex) {
        return this.mCursor.getDouble(columnIndex);
    }

    public void setExtras(Bundle extras) {
        this.mCursor.setExtras(extras);
    }

    public Bundle getExtras() {
        return this.mCursor.getExtras();
    }

    public float getFloat(int columnIndex) {
        return this.mCursor.getFloat(columnIndex);
    }

    public int getInt(int columnIndex) {
        return this.mCursor.getInt(columnIndex);
    }

    public long getLong(int columnIndex) {
        return this.mCursor.getLong(columnIndex);
    }

    public short getShort(int columnIndex) {
        return this.mCursor.getShort(columnIndex);
    }

    public String getString(int columnIndex) {
        return this.mCursor.getString(columnIndex);
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        this.mCursor.copyStringToBuffer(columnIndex, buffer);
    }

    public byte[] getBlob(int columnIndex) {
        return this.mCursor.getBlob(columnIndex);
    }

    public boolean getWantsAllOnMoveCalls() {
        return this.mCursor.getWantsAllOnMoveCalls();
    }

    public boolean isAfterLast() {
        return this.mCursor.isAfterLast();
    }

    public boolean isBeforeFirst() {
        return this.mCursor.isBeforeFirst();
    }

    public boolean isFirst() {
        return this.mCursor.isFirst();
    }

    public boolean isLast() {
        return this.mCursor.isLast();
    }

    public int getType(int columnIndex) {
        return this.mCursor.getType(columnIndex);
    }

    public boolean isNull(int columnIndex) {
        return this.mCursor.isNull(columnIndex);
    }

    public boolean moveToLast() {
        return this.mCursor.moveToLast();
    }

    public boolean move(int offset) {
        return this.mCursor.move(offset);
    }

    public boolean moveToPosition(int position) {
        return this.mCursor.moveToPosition(position);
    }

    public boolean moveToNext() {
        return this.mCursor.moveToNext();
    }

    public int getPosition() {
        return this.mCursor.getPosition();
    }

    public boolean moveToPrevious() {
        return this.mCursor.moveToPrevious();
    }

    public void registerContentObserver(ContentObserver observer) {
        this.mCursor.registerContentObserver(observer);
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mCursor.registerDataSetObserver(observer);
    }

    @Deprecated
    public boolean requery() {
        return this.mCursor.requery();
    }

    public Bundle respond(Bundle extras) {
        return this.mCursor.respond(extras);
    }

    public void setNotificationUri(ContentResolver cr, Uri uri) {
        this.mCursor.setNotificationUri(cr, uri);
    }

    public Uri getNotificationUri() {
        return this.mCursor.getNotificationUri();
    }

    public void unregisterContentObserver(ContentObserver observer) {
        this.mCursor.unregisterContentObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mCursor.unregisterDataSetObserver(observer);
    }
}
