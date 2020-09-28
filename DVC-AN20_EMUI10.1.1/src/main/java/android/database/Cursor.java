package android.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import java.io.Closeable;
import java.util.Arrays;
import java.util.List;

public interface Cursor extends Closeable {
    public static final int FIELD_TYPE_BLOB = 4;
    public static final int FIELD_TYPE_FLOAT = 2;
    public static final int FIELD_TYPE_INTEGER = 1;
    public static final int FIELD_TYPE_NULL = 0;
    public static final int FIELD_TYPE_STRING = 3;

    @Override // java.io.Closeable, java.lang.AutoCloseable
    void close();

    void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer);

    @Deprecated
    void deactivate();

    byte[] getBlob(int i);

    int getColumnCount();

    int getColumnIndex(String str);

    int getColumnIndexOrThrow(String str) throws IllegalArgumentException;

    String getColumnName(int i);

    String[] getColumnNames();

    int getCount();

    double getDouble(int i);

    Bundle getExtras();

    float getFloat(int i);

    int getInt(int i);

    long getLong(int i);

    Uri getNotificationUri();

    int getPosition();

    short getShort(int i);

    String getString(int i);

    int getType(int i);

    boolean getWantsAllOnMoveCalls();

    boolean isAfterLast();

    boolean isBeforeFirst();

    boolean isClosed();

    boolean isFirst();

    boolean isLast();

    boolean isNull(int i);

    boolean move(int i);

    boolean moveToFirst();

    boolean moveToLast();

    boolean moveToNext();

    boolean moveToPosition(int i);

    boolean moveToPrevious();

    void registerContentObserver(ContentObserver contentObserver);

    void registerDataSetObserver(DataSetObserver dataSetObserver);

    @Deprecated
    boolean requery();

    Bundle respond(Bundle bundle);

    void setExtras(Bundle bundle);

    void setNotificationUri(ContentResolver contentResolver, Uri uri);

    void unregisterContentObserver(ContentObserver contentObserver);

    void unregisterDataSetObserver(DataSetObserver dataSetObserver);

    default void setNotificationUris(ContentResolver cr, List<Uri> uris) {
        setNotificationUri(cr, uris.get(0));
    }

    default List<Uri> getNotificationUris() {
        Uri notifyUri = getNotificationUri();
        if (notifyUri == null) {
            return null;
        }
        return Arrays.asList(notifyUri);
    }
}
