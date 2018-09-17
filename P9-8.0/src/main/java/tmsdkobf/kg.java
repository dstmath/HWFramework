package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public interface kg {
    int delete(Uri uri, String str, String[] strArr);

    Uri insert(Uri uri, ContentValues contentValues);

    Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2);
}
