package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;

/* compiled from: Unknown */
public interface lc {
    long a(String str, ContentValues contentValues);

    Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3);

    ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList);

    Cursor bl(String str);

    Uri bm(String str);

    Uri bn(String str);

    Uri bo(String str);

    void close();

    int delete(String str, String str2, String[] strArr);

    int update(String str, ContentValues contentValues, String str2, String[] strArr);
}
