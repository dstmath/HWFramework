package android.media;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

public class ExternalRingtonesCursorWrapper extends CursorWrapper {
    private Uri mUri;

    public ExternalRingtonesCursorWrapper(Cursor cursor, Uri uri) {
        super(cursor);
        this.mUri = uri;
    }

    @Override // android.database.CursorWrapper, android.database.Cursor
    public String getString(int index) {
        if (index == 2) {
            return this.mUri.toString();
        }
        return super.getString(index);
    }
}
