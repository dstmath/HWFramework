package com.huawei.odmf.core;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;

final class FastCursor implements Cursor {
    private final int count;
    private int position;
    private final CursorWindow window;

    public FastCursor(CursorWindow window2) {
        this.window = window2;
        this.count = window2.getNumRows();
    }

    public int getCount() {
        return this.window.getNumRows();
    }

    public int getPosition() {
        return this.position;
    }

    public boolean move(int offset) {
        return moveToPosition(this.position + offset);
    }

    public boolean moveToPosition(int position2) {
        if (position2 < 0 || position2 >= this.count) {
            return false;
        }
        this.position = position2;
        return true;
    }

    public boolean moveToFirst() {
        this.position = 0;
        if (this.count > 0) {
            return true;
        }
        return false;
    }

    public boolean moveToLast() {
        if (this.count <= 0) {
            return false;
        }
        this.position = this.count - 1;
        return true;
    }

    public boolean moveToNext() {
        if (this.position >= this.count - 1) {
            return false;
        }
        this.position++;
        return true;
    }

    public boolean moveToPrevious() {
        if (this.position <= 0) {
            return false;
        }
        this.position--;
        return true;
    }

    public boolean isFirst() {
        return this.position == 0;
    }

    public boolean isLast() {
        return this.position == this.count + -1;
    }

    public boolean isBeforeFirst() {
        throw new ODMFUnsupportedOperationException();
    }

    public boolean isAfterLast() {
        throw new ODMFUnsupportedOperationException();
    }

    public int getColumnIndex(String columnName) {
        throw new ODMFUnsupportedOperationException();
    }

    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        throw new ODMFUnsupportedOperationException();
    }

    public String getColumnName(int columnIndex) {
        throw new ODMFUnsupportedOperationException();
    }

    public String[] getColumnNames() {
        throw new ODMFUnsupportedOperationException();
    }

    public int getColumnCount() {
        throw new ODMFUnsupportedOperationException();
    }

    public byte[] getBlob(int columnIndex) {
        return this.window.getBlob(this.position, columnIndex);
    }

    public String getString(int columnIndex) {
        return this.window.getString(this.position, columnIndex);
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        throw new ODMFUnsupportedOperationException();
    }

    public short getShort(int columnIndex) {
        return this.window.getShort(this.position, columnIndex);
    }

    public int getInt(int columnIndex) {
        return this.window.getInt(this.position, columnIndex);
    }

    public long getLong(int columnIndex) {
        return this.window.getLong(this.position, columnIndex);
    }

    public float getFloat(int columnIndex) {
        return this.window.getFloat(this.position, columnIndex);
    }

    public double getDouble(int columnIndex) {
        return this.window.getDouble(this.position, columnIndex);
    }

    public boolean isNull(int columnIndex) {
        return this.window.isNull(this.position, columnIndex);
    }

    public void deactivate() {
        throw new ODMFUnsupportedOperationException();
    }

    public boolean requery() {
        throw new ODMFUnsupportedOperationException();
    }

    public void close() {
        throw new ODMFUnsupportedOperationException();
    }

    public boolean isClosed() {
        throw new ODMFUnsupportedOperationException();
    }

    public void registerContentObserver(ContentObserver observer) {
        throw new ODMFUnsupportedOperationException();
    }

    public void unregisterContentObserver(ContentObserver observer) {
        throw new ODMFUnsupportedOperationException();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        throw new ODMFUnsupportedOperationException();
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        throw new ODMFUnsupportedOperationException();
    }

    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new ODMFUnsupportedOperationException();
    }

    public boolean getWantsAllOnMoveCalls() {
        throw new ODMFUnsupportedOperationException();
    }

    public void setExtras(Bundle extras) {
        throw new ODMFUnsupportedOperationException();
    }

    public Bundle getExtras() {
        throw new ODMFUnsupportedOperationException();
    }

    public Bundle respond(Bundle extras) {
        throw new ODMFUnsupportedOperationException();
    }

    public int getType(int columnIndex) {
        throw new ODMFUnsupportedOperationException();
    }

    public Uri getNotificationUri() {
        return null;
    }
}
