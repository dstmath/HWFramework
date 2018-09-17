package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;

public interface jv {
    long a(String str, ContentValues contentValues);

    Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3);

    Cursor al(String str);

    Uri am(String str);

    Uri an(String str);

    Uri ao(String str);

    ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList);

    void close();

    int delete(String str, String str2, String[] strArr);

    int update(String str, ContentValues contentValues, String str2, String[] strArr);
}
