package tmsdkobf;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class kn implements lo {
    private long lU;
    private ContentResolver mContentResolver;

    public kn(Context context, long j) {
        this.mContentResolver = context.getContentResolver();
        this.lU = j;
    }

    private void a(Exception exception) {
        d.c("SysDBService", exception.getMessage());
    }

    public int delete(Uri uri, String str, String[] strArr) {
        d.e("SysDBService", "delete|caller=" + this.lU + "|uri=" + uri.toString());
        try {
            return this.mContentResolver.delete(uri, str, strArr);
        } catch (Exception e) {
            a(e);
            return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        d.e("SysDBService", "insert|caller=" + this.lU + "|uri=" + uri.toString());
        try {
            return this.mContentResolver.insert(uri, contentValues);
        } catch (Exception e) {
            a(e);
            return null;
        }
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        Cursor query;
        d.e("SysDBService", "query|caller=" + this.lU + "|uri=" + uri.toString());
        try {
            query = this.mContentResolver.query(uri, strArr, str, strArr2, str2);
        } catch (Exception e) {
            a(e);
            query = null;
        }
        return query == null ? null : new kl(query);
    }
}
