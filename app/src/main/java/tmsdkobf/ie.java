package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
final class ie implements lc {
    private long lU;
    private ContentProvider rF;
    private String rG;
    private String rH;

    public ie(long j, ContentProvider contentProvider, String str) {
        this.lU = j;
        this.rG = str;
        this.rH = "content://" + str;
        this.rF = contentProvider;
    }

    private void a(Exception exception, int i) {
        d.c("RawDBService", exception.getMessage());
    }

    public long a(String str, ContentValues contentValues) {
        d.e("RawDBService", "insert|caller=" + this.lU + "|authority=" + this.rG + "|table=" + str);
        Uri parse = Uri.parse(this.rH + "/insert" + "?" + str);
        long j = -1;
        try {
            parse = this.rF.insert(parse, contentValues);
            if (parse != null) {
                j = Long.parseLong(parse.getQuery());
            }
        } catch (Exception e) {
            a(e, 2);
        }
        return j;
    }

    public Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3) {
        Cursor query;
        d.e("RawDBService", "query|caller=" + this.lU + "|authority=" + this.rG + "|table=" + str);
        try {
            query = this.rF.query(Uri.parse(this.rH + "/query" + "_" + "1-" + "?" + str), strArr, str2, strArr2, str3);
        } catch (Exception e) {
            a(e, 1);
            query = null;
        }
        return query == null ? null : new kl(query);
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) {
        d.d("RawDBService", "applyBatch|caller=" + this.lU + "|authority=" + this.rG);
        try {
            return this.rF.applyBatch(arrayList);
        } catch (Exception e) {
            a(e, 7);
            return null;
        }
    }

    public Cursor bl(String str) {
        Cursor query;
        d.e("RawDBService", "query|caller=" + this.lU + "|authority=" + this.rG + "|sql=" + str);
        try {
            query = this.rF.query(Uri.parse(this.rH + "/rawquery" + "_" + "1-" + "?" + Uri.encode(str)), null, null, null, null);
        } catch (Exception e) {
            a(e, 1);
            query = null;
        }
        return query == null ? null : new kl(query);
    }

    public Uri bm(String str) {
        return Uri.parse("content://" + this.rG + "/insert" + "?" + str);
    }

    public Uri bn(String str) {
        return Uri.parse("content://" + this.rG + "/delete" + "?" + str);
    }

    public Uri bo(String str) {
        return Uri.parse("content://" + this.rG + "/update" + "?" + str);
    }

    public void close() {
    }

    public int delete(String str, String str2, String[] strArr) {
        d.e("RawDBService", "delete|caller=" + this.lU + "|authority=" + this.rG + "|table=" + str);
        try {
            return this.rF.delete(Uri.parse(this.rH + "/delete" + "?" + str), str2, strArr);
        } catch (Exception e) {
            a(e, 3);
            return 0;
        }
    }

    public int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        d.e("RawDBService", "update|caller=" + this.lU + "|authority=" + this.rG + "|table=" + str);
        try {
            return this.rF.update(Uri.parse(this.rH + "/update" + "?" + str), contentValues, str2, strArr);
        } catch (Exception e) {
            a(e, 4);
            return 0;
        }
    }
}
