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
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ko extends ContentProvider {
    private final int PHONE;
    private final String TAG;
    private String mName;
    private int mType;
    protected Set<String> rP;
    public final Object vN;
    private final int vO;
    private SQLiteOpenHelper vP;
    private ij vQ;
    private int vR;
    private a vS;
    private String vT;

    /* compiled from: Unknown */
    public interface a {
        void onCreate(SQLiteDatabase sQLiteDatabase);

        void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

        void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ko.1 */
    class AnonymousClass1 extends SQLiteOpenHelper {
        final /* synthetic */ ko vU;

        AnonymousClass1(ko koVar, Context context, String str, CursorFactory cursorFactory, int i) {
            this.vU = koVar;
            super(context, str, cursorFactory, i);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            d.d("DBService", "SQLiteDatabase|onCreate|name=" + this.vU.mName + "|version=" + this.vU.vR);
            this.vU.vS.onCreate(sQLiteDatabase);
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            d.f("DBService", "SQLiteDatabase|onDowngrade|name=" + this.vU.mName + "|oldversion=" + i + "|newVersion=" + i2);
            this.vU.vS.onDowngrade(sQLiteDatabase, i, i2);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            d.f("DBService", "SQLiteDatabase|onUpgrade|name=" + this.vU.mName + "|oldversion=" + i + "|newVersion=" + i2);
            this.vU.vS.onUpgrade(sQLiteDatabase, i, i2);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ko.2 */
    class AnonymousClass2 extends ij {
        final /* synthetic */ ko vU;

        AnonymousClass2(ko koVar, Context context, String str, CursorFactory cursorFactory, int i, String str2) {
            this.vU = koVar;
            super(context, str, cursorFactory, i, str2);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            d.d("DBService", "SDCardSQLiteDatabase|onCreate|name=" + this.vU.mName + "|version=" + this.vU.vR);
            this.vU.vS.onCreate(sQLiteDatabase);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            d.f("DBService", "SDCardSQLiteDatabase|onUpgrade|name=" + this.vU.mName + "|oldversion=" + i + "|newVersion=" + i2);
            this.vU.vS.onUpgrade(sQLiteDatabase, i, i2);
        }
    }

    public ko(String str, int i, a aVar) {
        this.vN = new Object();
        this.TAG = "PiDBProvider";
        this.PHONE = 1;
        this.vO = 2;
        this.mType = 1;
        this.mName = str;
        this.vR = i;
        this.vS = aVar;
    }

    protected int a(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues, String str2, String[] strArr) {
        int i = -1;
        if (sQLiteDatabase != null) {
            try {
                i = sQLiteDatabase.update(str, contentValues, str2, strArr);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "update fail!");
            }
        }
        return i;
    }

    protected int a(SQLiteDatabase sQLiteDatabase, String str, String str2, String[] strArr) {
        int i = -1;
        if (sQLiteDatabase != null) {
            try {
                i = sQLiteDatabase.delete(str, str2, strArr);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "delete fail!");
            }
        }
        return i;
    }

    protected long a(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        long j = -1;
        if (sQLiteDatabase != null) {
            try {
                j = sQLiteDatabase.insert(str, null, contentValues);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "insert fail!");
            }
        }
        return j;
    }

    protected Cursor a(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor cursor = null;
        if (sQLiteDatabase != null) {
            try {
                cursor = sQLiteDatabase.rawQuery(str, null);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "rawQuery fail!");
            }
        }
        return cursor;
    }

    protected Cursor a(SQLiteDatabase sQLiteDatabase, String str, String[] strArr, String str2, String[] strArr2, String str3) {
        Cursor cursor;
        if (sQLiteDatabase == null) {
            cursor = null;
        } else {
            try {
                cursor = sQLiteDatabase.query(str, strArr, str2, strArr2, null, null, str3);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "query fail!");
                return null;
            }
        }
        return cursor;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        ContentProviderResult[] contentProviderResultArr = null;
        synchronized (this.vN) {
            SQLiteDatabase database = getDatabase();
            if (database == null) {
                return null;
            }
            database.beginTransaction();
            try {
                contentProviderResultArr = super.applyBatch(arrayList);
                database.setTransactionSuccessful();
                if (database.inTransaction()) {
                    database.endTransaction();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (database.inTransaction()) {
                    database.endTransaction();
                }
            } catch (Throwable th) {
                if (database.inTransaction()) {
                    database.endTransaction();
                }
            }
            return contentProviderResultArr;
        }
    }

    protected long b(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        long j = -1;
        if (sQLiteDatabase != null) {
            try {
                j = sQLiteDatabase.replace(str, null, contentValues);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "replace fail!");
            }
        }
        return j;
    }

    protected void b(SQLiteDatabase sQLiteDatabase, String str) {
        if (sQLiteDatabase != null) {
            try {
                sQLiteDatabase.execSQL(str);
            } catch (SQLiteException e) {
                e.printStackTrace();
                d.c("PiDBProvider", "execSQL fail!");
            }
        }
    }

    protected void bL() {
        if (this.rP == null || this.rP.size() <= 0) {
            if (this.vP != null && this.mType == 1) {
                this.vP.close();
            } else if (this.vQ != null && this.mType == 2) {
                this.vQ.close();
            }
        }
    }

    protected void bq(String str) {
        if (this.rP == null) {
            this.rP = new HashSet();
        }
        this.rP.add(str);
    }

    protected void br(String str) {
        if (this.rP != null) {
            this.rP.remove(str);
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        synchronized (this.vN) {
            String path = uri.getPath();
            if ("/delete".equals(path)) {
                int a = a(getDatabase(), uri.getQuery(), str, strArr);
                return a;
            } else if ("/execSQL".equals(path)) {
                b(getDatabase(), uri.getQuery());
                return 0;
            } else if ("/closecursor".equals(path)) {
                path = uri.getQuery();
                br(path);
                d.d("PiDBProvider", "removeCursorId: " + path);
                return 0;
            } else if ("/close".equals(path)) {
                bL();
                return 0;
            } else {
                d.c("PiDBProvider", "error delete: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
    }

    protected SQLiteDatabase getDatabase() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (applicaionContext == null) {
            d.d("PiDBProvider", "ProviderUtil.getForeContext()\uff1a " + applicaionContext);
            return null;
        } else if (this.mType != 1) {
            if (this.vQ == null) {
                this.vQ = new AnonymousClass2(this, applicaionContext, this.mName, null, this.vR, this.vT);
            }
            return this.vQ.getWritableDatabase();
        } else {
            if (this.vP == null) {
                this.vP = new AnonymousClass1(this, applicaionContext, this.mName, null, this.vR);
            }
            return this.vP.getWritableDatabase();
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        synchronized (this.vN) {
            Uri parse;
            if ("/insert".equals(uri.getPath())) {
                parse = Uri.parse("content://" + uri.getAuthority() + "?" + a(getDatabase(), uri.getQuery(), contentValues));
                return parse;
            } else if ("/replace".equals(uri.getPath())) {
                parse = Uri.parse("content://" + uri.getAuthority() + "?" + b(getDatabase(), uri.getQuery(), contentValues));
                return parse;
            } else {
                d.c("PiDBProvider", "error insert: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        synchronized (this.vN) {
            Object path = uri.getPath();
            int indexOf = path.indexOf("_");
            if (indexOf != -1) {
                String substring = path.substring(indexOf + 1);
                bq(substring);
                d.d("PiDBProvider", "addCursorId: " + substring);
                path = path.substring(0, indexOf);
            }
            Cursor a;
            if ("/query".equals(path)) {
                a = a(getDatabase(), uri.getQuery(), strArr, str, strArr2, str2);
                return a;
            } else if ("/rawquery".equals(path)) {
                a = a(getDatabase(), uri.getQuery());
                return a;
            } else {
                d.c("PiDBProvider", "error query: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int a;
        synchronized (this.vN) {
            if ("/update".equals(uri.getPath())) {
                a = a(getDatabase(), uri.getQuery(), contentValues, str, strArr);
            } else {
                d.c("PiDBProvider", "error update: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
        return a;
    }
}
