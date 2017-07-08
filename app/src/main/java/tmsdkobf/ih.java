package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class ih extends ContentProvider {
    private static final Map<Integer, Integer> rQ = null;
    private final Object mLock;
    private SQLiteOpenHelper rN;
    private SQLiteDatabase rO;
    private Set<String> rP;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ih.1 */
    class AnonymousClass1 extends SQLiteOpenHelper {
        final /* synthetic */ ih rR;

        AnonymousClass1(ih ihVar, Context context, String str, CursorFactory cursorFactory, int i) {
            this.rR = ihVar;
            super(context, str, cursorFactory, i);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            d.d("ConfigProvider", "onCreate");
            this.rR.g(sQLiteDatabase);
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            d.d("ConfigProvider", "onDowngrade");
            this.rR.j(sQLiteDatabase, i, i2);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            d.d("ConfigProvider", "onUpgrade");
            this.rR.i(sQLiteDatabase, i, i2);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ih.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ih.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ih.<clinit>():void");
    }

    public ih() {
        this.mLock = new Object();
        this.rN = null;
        this.rO = null;
    }

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

    private int aT(int i) {
        bK();
        return !rQ.containsKey(Integer.valueOf(i)) ? 0 : ((Integer) rQ.get(Integer.valueOf(i))).intValue();
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
        if (contentValues.containsKey(TMSDKContext.CON_LC)) {
            contentValues2.put(TMSDKContext.CON_LC, contentValues.getAsInteger(TMSDKContext.CON_LC));
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

    private void bK() {
        Exception e;
        Throwable th;
        if (rQ.isEmpty()) {
            Cursor query;
            try {
                query = getDatabase().query("loc_pi_info_table", new String[]{"xa", TMSDKContext.CON_LC}, null, null, null, null, null);
                try {
                    query.moveToFirst();
                    while (!query.isAfterLast()) {
                        rQ.put(Integer.valueOf(query.getInt(query.getColumnIndex("xa"))), Integer.valueOf(query.getInt(query.getColumnIndex(TMSDKContext.CON_LC))));
                        query.moveToNext();
                    }
                    if (query != null) {
                        query.close();
                    }
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                query = null;
                try {
                    d.c("ConfigProvider", "ensureStateMap err: " + e.getMessage());
                    if (query != null) {
                        query.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (query != null) {
                        query.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th;
            }
        }
    }

    private void bL() {
        if (this.rP == null || this.rP.size() <= 0) {
            if (this.rO != null) {
                this.rO.close();
                this.rO = null;
            }
        }
    }

    private void bq(String str) {
        if (this.rP == null) {
            this.rP = new HashSet();
        }
        this.rP.add(str);
    }

    private void br(String str) {
        if (this.rP != null) {
            this.rP.remove(str);
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
            if (this.rO == null) {
                if (this.rN == null) {
                    d.d("ConfigProvider", "context: " + applicaionContext);
                    this.rN = new AnonymousClass1(this, applicaionContext, "pi_config.db", null, 6);
                }
                this.rO = this.rN.getWritableDatabase();
            }
            return this.rO;
        }
        throw new IllegalStateException("context is null,maybe process has crashed. please check former log!");
    }

    private void h(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS xml_pi_info_table");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS loc_pi_info_table");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pi_compat_table");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void i(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pi_compat_table (xa INTEGER PRIMARY KEY,pa INTEGER,xb INTEGER)");
                break;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                break;
            case FileInfo.TYPE_BIGFILE /*3*/:
                break;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                break;
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                break;
            default:
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
                d.a("ConfigProvider", "applyBatch, err: " + e.getMessage(), e);
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
                path = uri.getQuery();
                if (strArr != null && strArr.length == 1 && "vo_init".equals(strArr[0])) {
                    SQLiteDatabase database;
                    if ("xml_pi_info_table".equals(path)) {
                        database = getDatabase();
                        database.execSQL("DROP TABLE IF EXISTS " + path);
                        database.execSQL("CREATE TABLE IF NOT EXISTS xml_pi_info_table (xa INTEGER PRIMARY KEY,xb INTEGER,xc INTEGER,xd INTEGER,xe INTEGER,xf INTEGER,xg INTEGER,xh INTEGER,xi TEXT,xm TEXT,xj TEXT,xk TEXT,xl TEXT)");
                    } else if ("loc_pi_info_table".equals(path)) {
                        database = getDatabase();
                        database.execSQL("DROP TABLE IF EXISTS " + path);
                        database.execSQL("CREATE TABLE IF NOT EXISTS loc_pi_info_table (xa INTEGER PRIMARY KEY,lh INTEGER,la INTEGER,lb INTEGER,lc INTEGER,ld TEXT,le TEXT,lf INTEGER,lg INTEGER)");
                    } else if ("pi_compat_table".equals(path)) {
                        database = getDatabase();
                        database.execSQL("DROP TABLE IF EXISTS " + path);
                        database.execSQL("CREATE TABLE IF NOT EXISTS pi_compat_table (xa INTEGER PRIMARY KEY,pa INTEGER,xb INTEGER)");
                    }
                    i = 0;
                } else {
                    i = !"vt_ps".equals(path) ? getDatabase().delete(path, str, strArr) : aT(Integer.parseInt(str));
                }
            } else {
                if ("/closecursor".equals(path)) {
                    br(uri.getQuery());
                } else if ("/close".equals(path)) {
                    bL();
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
        bK();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        synchronized (this.mLock) {
            Cursor query;
            String path = uri.getPath();
            int indexOf = path.indexOf("_");
            if (indexOf != -1) {
                bq(path.substring(indexOf + 1));
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

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i;
        synchronized (this.mLock) {
            i = -1;
            String query = uri.getQuery();
            if ("both_pi_info_table".equals(query) || "xml_pi_info_table".equals(query) || "loc_pi_info_table".equals(query)) {
                a(contentValues, str, query);
                if (!"both_pi_info_table".equals(query)) {
                    if (!"loc_pi_info_table".equals(query)) {
                    }
                }
                rQ.clear();
                bK();
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
