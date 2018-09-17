package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;

final class hb implements jv {
    private long mr;
    private ContentProvider pi;
    private String pj;
    private String pk;

    public hb(long j, ContentProvider contentProvider, String str) {
        this.mr = j;
        this.pj = str;
        this.pk = "content://" + str;
        this.pi = contentProvider;
    }

    private void a(Exception exception, int i) {
        mb.o("RawDBService", exception.getMessage());
    }

    public long a(String str, ContentValues contentValues) {
        mb.d("RawDBService", "insert|caller=" + this.mr + "|authority=" + this.pj + "|table=" + str);
        try {
            Uri insert = this.pi.insert(Uri.parse(this.pk + "/insert" + "?" + str), contentValues);
            return insert == null ? -1 : Long.parseLong(insert.getQuery());
        } catch (Exception e) {
            a(e, 2);
            return -1;
        }
    }

    public Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3) {
        mb.d("RawDBService", "query|caller=" + this.mr + "|authority=" + this.pj + "|table=" + str);
        Cursor cursor = null;
        try {
            cursor = this.pi.query(Uri.parse(this.pk + "/query" + "_" + "1-" + "?" + str), strArr, str2, strArr2, str3);
        } catch (Exception e) {
            a(e, 1);
        }
        return cursor == null ? null : new je(cursor);
    }

    public Cursor al(String str) {
        mb.d("RawDBService", "query|caller=" + this.mr + "|authority=" + this.pj + "|sql=" + str);
        Cursor cursor = null;
        try {
            cursor = this.pi.query(Uri.parse(this.pk + "/rawquery" + "_" + "1-" + "?" + Uri.encode(str)), null, null, null, null);
        } catch (Exception e) {
            a(e, 1);
        }
        return cursor == null ? null : new je(cursor);
    }

    public Uri am(String str) {
        return Uri.parse("content://" + this.pj + "/insert" + "?" + str);
    }

    public Uri an(String str) {
        return Uri.parse("content://" + this.pj + "/delete" + "?" + str);
    }

    public Uri ao(String str) {
        return Uri.parse("content://" + this.pj + "/update" + "?" + str);
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) {
        mb.n("RawDBService", "applyBatch|caller=" + this.mr + "|authority=" + this.pj);
        ContentProviderResult[] contentProviderResultArr = null;
        try {
            return this.pi.applyBatch(arrayList);
        } catch (Exception e) {
            a(e, 7);
            return contentProviderResultArr;
        }
    }

    public void close() {
    }

    public int delete(String str, String str2, String[] strArr) {
        mb.d("RawDBService", "delete|caller=" + this.mr + "|authority=" + this.pj + "|table=" + str);
        int i = 0;
        try {
            return this.pi.delete(Uri.parse(this.pk + "/delete" + "?" + str), str2, strArr);
        } catch (Exception e) {
            a(e, 3);
            return i;
        }
    }

    public int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        mb.d("RawDBService", "update|caller=" + this.mr + "|authority=" + this.pj + "|table=" + str);
        int i = 0;
        try {
            return this.pi.update(Uri.parse(this.pk + "/update" + "?" + str), contentValues, str2, strArr);
        } catch (Exception e) {
            a(e, 4);
            return i;
        }
    }
}
