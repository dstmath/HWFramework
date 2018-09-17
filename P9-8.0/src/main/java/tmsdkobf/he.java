package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;

public class he extends ContentProvider {
    private static final Map<Integer, Integer> pt = new HashMap();
    private final Object mLock = new Object();
    private SQLiteOpenHelper pq = null;
    private SQLiteDatabase pr = null;
    private Set<String> ps;

    private ContentValues a(ContentValues contentValues) {
        ContentValues contentValues2 = new ContentValues();
        if (contentValues.containsKey("xa")) {
            contentValues2.put("xa", contentValues.getAsInteger("xa"));
        }
        if (contentValues.containsKey("xf")) {
            contentValues2.put("xf", contentValues.getAsInteger("xf"));
        }
        if (contentValues.containsKey("xg")) {
            contentValues2.put("xg", contentValues.getAsInteger("xg"));
        }
        if (contentValues.containsKey("xh")) {
            contentValues2.put("xh", contentValues.getAsInteger("xh"));
        }
        if (contentValues.containsKey("xb")) {
            contentValues2.put("xb", contentValues.getAsInteger("xb"));
        }
        if (contentValues.containsKey("xc")) {
            contentValues2.put("xc", contentValues.getAsInteger("xc"));
        }
        if (contentValues.containsKey("xd")) {
            contentValues2.put("xd", contentValues.getAsInteger("xd"));
        }
        if (contentValues.containsKey("xe")) {
            contentValues2.put("xe", contentValues.getAsInteger("xe"));
        }
        if (contentValues.containsKey("xi")) {
            contentValues2.put("xi", contentValues.getAsString("xi"));
        }
        if (contentValues.containsKey("xm")) {
            contentValues2.put("xm", contentValues.getAsString("xm"));
        }
        if (contentValues.containsKey("xj")) {
            contentValues2.put("xj", contentValues.getAsString("xj"));
        }
        if (contentValues.containsKey("xk")) {
            contentValues2.put("xk", contentValues.getAsString("xk"));
        }
        if (contentValues.containsKey("xl")) {
            contentValues2.put("xl", contentValues.getAsString("xl"));
        }
        return contentValues2;
    }

    private void a(ContentValues contentValues, String str) {
        SQLiteDatabase database = getDatabase();
        int intValue = contentValues.getAsInteger("xa").intValue();
        contentValues.remove("xa");
        if (database.update("xml_pi_info_table", contentValues, str, null) <= 0) {
            contentValues.put("xa", Integer.valueOf(intValue));
            database.insert("xml_pi_info_table", null, contentValues);
        }
    }

    private void a(ContentValues contentValues, String str, String str2) {
        if (contentValues != null && str2 != null) {
            if ("both_pi_info_table".equals(str2) || "xml_pi_info_table".equals(str2)) {
                a(a(contentValues), str);
            }
            if ("both_pi_info_table".equals(str2) || "loc_pi_info_table".equals(str2)) {
                b(b(contentValues), str);
            }
        }
    }

    private int aa(int i) {
        bf();
        return !pt.containsKey(Integer.valueOf(i)) ? 0 : ((Integer) pt.get(Integer.valueOf(i))).intValue();
    }

    private void aq(String str) {
        if (this.ps == null) {
            this.ps = new HashSet();
        }
        this.ps.add(str);
    }

    private void ar(String str) {
        if (this.ps != null) {
            this.ps.remove(str);
        }
    }

    private ContentValues b(ContentValues contentValues) {
        ContentValues contentValues2 = new ContentValues();
        if (contentValues.containsKey("xa")) {
            contentValues2.put("xa", contentValues.getAsInteger("xa"));
        }
        if (contentValues.containsKey("lh")) {
            contentValues2.put("lh", contentValues.getAsInteger("lh"));
        }
        if (contentValues.containsKey("la")) {
            contentValues2.put("la", contentValues.getAsInteger("la"));
        }
        if (contentValues.containsKey("lb")) {
            contentValues2.put("lb", contentValues.getAsInteger("lb"));
        }
        if (contentValues.containsKey("lc")) {
            contentValues2.put("lc", contentValues.getAsInteger("lc"));
        }
        if (contentValues.containsKey("le")) {
            contentValues2.put("le", contentValues.getAsString("le"));
        }
        if (contentValues.containsKey("ld")) {
            contentValues2.put("ld", contentValues.getAsString("ld"));
        }
        if (contentValues.containsKey("lf")) {
            contentValues2.put("lf", contentValues.getAsString("lf"));
        }
        if (contentValues.containsKey("lg")) {
            contentValues2.put("lg", contentValues.getAsString("lg"));
        }
        return contentValues2;
    }

    private void b(ContentValues contentValues, String str) {
        SQLiteDatabase database = getDatabase();
        int intValue = contentValues.getAsInteger("xa").intValue();
        contentValues.remove("xa");
        if (database.update("loc_pi_info_table", contentValues, str, null) <= 0) {
            contentValues.put("xa", Integer.valueOf(intValue));
            database.insert("loc_pi_info_table", null, contentValues);
        }
    }

    private void b(ContentValues contentValues, String str, String str2) {
        SQLiteDatabase database = getDatabase();
        int intValue = contentValues.getAsInteger("xa").intValue();
        contentValues.remove("xa");
        if (database.update("pi_compat_table", contentValues, str, null) <= 0) {
            contentValues.put("xa", Integer.valueOf(intValue));
            database.insert("pi_compat_table", null, contentValues);
        }
    }

    private void bf() {
        if (pt.isEmpty()) {
            Cursor cursor = null;
            try {
                cursor = getDatabase().query("loc_pi_info_table", new String[]{"xa", "lc"}, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    pt.put(Integer.valueOf(cursor.getInt(cursor.getColumnIndex("xa"))), Integer.valueOf(cursor.getInt(cursor.getColumnIndex("lc"))));
                    cursor.moveToNext();
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                f.e("ConfigProvider", "ensureStateMap err: " + e.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private void bg() {
        if ((this.ps == null || this.ps.size() <= 0) && this.pr != null) {
            this.pr.close();
            this.pr = null;
        }
    }

    private void g(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS xml_pi_info_table (xa INTEGER PRIMARY KEY,xb INTEGER,xc INTEGER,xd INTEGER,xe INTEGER,xf INTEGER,xg INTEGER,xh INTEGER,xi TEXT,xm TEXT,xj TEXT,xk TEXT,xl TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS loc_pi_info_table (xa INTEGER PRIMARY KEY,lh INTEGER,la INTEGER,lb INTEGER,lc INTEGER,ld TEXT,le TEXT,lf INTEGER,lg INTEGER)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pi_compat_table (xa INTEGER PRIMARY KEY,pa INTEGER,xb INTEGER)");
    }

    private SQLiteDatabase getDatabase() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (applicaionContext != null) {
            if (this.pr == null) {
                if (this.pq == null) {
                    f.f("ConfigProvider", "context: " + applicaionContext);
                    this.pq = new SQLiteOpenHelper(applicaionContext, "pi_config.db", null, 6) {
                        public void onCreate(SQLiteDatabase sQLiteDatabase) {
                            f.f("ConfigProvider", "onCreate");
                            he.this.g(sQLiteDatabase);
                        }

                        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                            f.f("ConfigProvider", "onDowngrade");
                            he.this.j(sQLiteDatabase, i, i2);
                        }

                        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                            f.f("ConfigProvider", "onUpgrade");
                            he.this.i(sQLiteDatabase, i, i2);
                        }
                    };
                }
                this.pr = this.pq.getWritableDatabase();
            }
            return this.pr;
        }
        throw new IllegalStateException("context is null,maybe process has crashed. please check former log!");
    }

    private void h(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS xml_pi_info_table");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS loc_pi_info_table");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pi_compat_table");
    }

    /* JADX WARNING: Missing block: B:2:0x000a, code:
            r6.execSQL("ALTER TABLE xml_pi_info_table ADD COLUMN xm TEXT");
     */
    /* JADX WARNING: Missing block: B:3:0x0010, code:
            r1 = new java.lang.StringBuilder();
            r1.append("update ").append("loc_pi_info_table").append(" set ").append("la").append("=%d").append(" where ").append("loc_pi_info_table").append(".").append("la").append(" != 0 and (").append("SELECT ").append("xj").append(" FROM ").append("xml_pi_info_table").append(" WHERE ").append("xml_pi_info_table").append(".").append("xa").append("=").append("loc_pi_info_table").append(".").append("xa").append(") is %s null and (").append("SELECT ").append("xk").append(" FROM ").append("xml_pi_info_table").append(" WHERE ").append("xml_pi_info_table").append(".").append("xa").append("=").append("loc_pi_info_table").append(".").append("xa").append(") is %s null");
            r6.execSQL(r1.toString());
     */
    /* JADX WARNING: Missing block: B:4:0x0117, code:
            r6.execSQL("ALTER TABLE loc_pi_info_table ADD COLUMN lf INTEGER");
            r6.execSQL("ALTER TABLE loc_pi_info_table ADD COLUMN lg INTEGER");
     */
    /* JADX WARNING: Missing block: B:5:0x0123, code:
            r6.execSQL("ALTER TABLE loc_pi_info_table ADD COLUMN lh INTEGER");
     */
    /* JADX WARNING: Missing block: B:7:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void i(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        switch (i) {
            case 1:
                sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pi_compat_table (xa INTEGER PRIMARY KEY,pa INTEGER,xb INTEGER)");
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            default:
                return;
        }
    }

    private void j(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        h(sQLiteDatabase);
        g(sQLiteDatabase);
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        ContentProviderResult[] contentProviderResultArr;
        synchronized (this.mLock) {
            contentProviderResultArr = null;
            SQLiteDatabase database = getDatabase();
            database.beginTransaction();
            try {
                contentProviderResultArr = super.applyBatch(arrayList);
                database.setTransactionSuccessful();
                database.endTransaction();
            } catch (Throwable e) {
                f.b("ConfigProvider", "applyBatch, err: " + e.getMessage(), e);
                database.endTransaction();
            } catch (Throwable th) {
                database.endTransaction();
            }
        }
        return contentProviderResultArr;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int i;
        synchronized (this.mLock) {
            i = -1;
            String path = uri.getPath();
            if ("/delete".equals(path)) {
                String query = uri.getQuery();
                if (strArr != null && strArr.length == 1) {
                    if ("vo_init".equals(strArr[0])) {
                        SQLiteDatabase database;
                        if ("xml_pi_info_table".equals(query)) {
                            database = getDatabase();
                            database.execSQL("DROP TABLE IF EXISTS " + query);
                            database.execSQL("CREATE TABLE IF NOT EXISTS xml_pi_info_table (xa INTEGER PRIMARY KEY,xb INTEGER,xc INTEGER,xd INTEGER,xe INTEGER,xf INTEGER,xg INTEGER,xh INTEGER,xi TEXT,xm TEXT,xj TEXT,xk TEXT,xl TEXT)");
                        } else if ("loc_pi_info_table".equals(query)) {
                            database = getDatabase();
                            database.execSQL("DROP TABLE IF EXISTS " + query);
                            database.execSQL("CREATE TABLE IF NOT EXISTS loc_pi_info_table (xa INTEGER PRIMARY KEY,lh INTEGER,la INTEGER,lb INTEGER,lc INTEGER,ld TEXT,le TEXT,lf INTEGER,lg INTEGER)");
                        } else if ("pi_compat_table".equals(query)) {
                            database = getDatabase();
                            database.execSQL("DROP TABLE IF EXISTS " + query);
                            database.execSQL("CREATE TABLE IF NOT EXISTS pi_compat_table (xa INTEGER PRIMARY KEY,pa INTEGER,xb INTEGER)");
                        }
                        i = 0;
                    }
                }
                i = !"vt_ps".equals(query) ? getDatabase().delete(query, str, strArr) : aa(Integer.parseInt(str));
            } else {
                if ("/closecursor".equals(path)) {
                    ar(uri.getQuery());
                } else if ("/close".equals(path)) {
                    bg();
                }
                i = 0;
            }
        }
        return i;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        bf();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        synchronized (this.mLock) {
            Cursor query;
            String path = uri.getPath();
            int indexOf = path.indexOf("_");
            if (indexOf != -1) {
                aq(path.substring(indexOf + 1));
            }
            String query2 = uri.getQuery();
            if (!"xml_pi_info_table".equals(query2)) {
                if (!"loc_pi_info_table".equals(query2)) {
                    if ("pi_compat_table".equals(query2)) {
                        query = getDatabase().query(query2, strArr, str, strArr2, null, null, str2);
                        return query;
                    }
                    return null;
                }
            }
            query = getDatabase().query(query2, strArr, str, strArr2, null, null, str2);
            return query;
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0051, code:
            if ("loc_pi_info_table".equals(r2) == false) goto L_0x0025;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i;
        synchronized (this.mLock) {
            i = -1;
            String query = uri.getQuery();
            if ("both_pi_info_table".equals(query) || "xml_pi_info_table".equals(query) || "loc_pi_info_table".equals(query)) {
                a(contentValues, str, query);
                if (!"both_pi_info_table".equals(query)) {
                }
                pt.clear();
                bf();
            } else {
                if ("pi_compat_table".equals(query)) {
                    b(contentValues, str, query);
                }
            }
            i = 0;
        }
        return i;
    }
}
