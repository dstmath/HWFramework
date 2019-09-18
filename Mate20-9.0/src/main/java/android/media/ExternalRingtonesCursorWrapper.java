package android.media;

import android.content.ContentProvider;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

public class ExternalRingtonesCursorWrapper extends CursorWrapper {
    private int mUserId;

    public ExternalRingtonesCursorWrapper(Cursor cursor, int userId) {
        super(cursor);
        this.mUserId = userId;
    }

    public String getString(int index) {
        String result = super.getString(index);
        if (index == 2) {
            return ContentProvider.maybeAddUserId(Uri.parse(result), this.mUserId).toString();
        }
        return result;
    }
}
