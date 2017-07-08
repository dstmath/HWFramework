package tmsdkobf;

import android.database.Cursor;
import android.database.CursorWrapper;

/* compiled from: Unknown */
public class kl extends CursorWrapper {
    public kl(Cursor cursor) {
        super(cursor);
    }

    public void close() {
        try {
            super.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
