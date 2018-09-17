package tmsdkobf;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class jg implements kg {
    private ContentResolver mContentResolver;
    private long mr;

    public jg(Context context, long j) {
        this.mContentResolver = context.getContentResolver();
        this.mr = j;
    }

    private void a(Exception exception) {
        mb.o("SysDBService", exception.getMessage());
    }

    public int delete(Uri uri, String str, String[] strArr) {
        mb.d("SysDBService", "delete|caller=" + this.mr + "|uri=" + uri.toString());
        int i = 0;
        try {
            return this.mContentResolver.delete(uri, str, strArr);
        } catch (Exception e) {
            a(e);
            return i;
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        mb.d("SysDBService", "insert|caller=" + this.mr + "|uri=" + uri.toString());
        Uri uri2 = null;
        try {
            return this.mContentResolver.insert(uri, contentValues);
        } catch (Exception e) {
            a(e);
            return uri2;
        }
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        mb.d("SysDBService", "query|caller=" + this.mr + "|uri=" + uri.toString());
        Cursor cursor = null;
        try {
            cursor = this.mContentResolver.query(uri, strArr, str, strArr2, str2);
        } catch (Exception e) {
            a(e);
        }
        return cursor == null ? null : new je(cursor);
    }
}
