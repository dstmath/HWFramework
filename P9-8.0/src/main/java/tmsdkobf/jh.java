package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import tmsdk.common.TMSDKContext;

public class jh extends ContentProvider {
    private final int PHONE = 1;
    private final String TAG = "PiDBProvider";
    private String mName;
    private int mType = 1;
    protected Set<String> ps;
    public final Object sS = new Object();
    private final int sT = 2;
    private SQLiteOpenHelper sU;
    private hg sV;
    private int sW;
    private a sX;
    private String sY;

    public interface a {
        void onCreate(SQLiteDatabase sQLiteDatabase);

        void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

        void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
    }

    public jh(String str, int i, a aVar) {
        this.mName = str;
        this.sW = i;
        this.sX = aVar;
    }

    protected int a(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues, String str2, String[] strArr) {
        int i = -1;
        if (sQLiteDatabase == null) {
            return -1;
        }
        try {
            return sQLiteDatabase.update(str, contentValues, str2, strArr);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "update fail!");
            return i;
        }
    }

    protected int a(SQLiteDatabase sQLiteDatabase, String str, String str2, String[] strArr) {
        int i = -1;
        if (sQLiteDatabase == null) {
            return -1;
        }
        try {
            return sQLiteDatabase.delete(str, str2, strArr);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "delete fail!");
            return i;
        }
    }

    protected long a(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        long j = -1;
        if (sQLiteDatabase == null) {
            return -1;
        }
        try {
            return sQLiteDatabase.insert(str, null, contentValues);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "insert fail!");
            return j;
        }
    }

    protected Cursor a(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor cursor = null;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            return sQLiteDatabase.rawQuery(str, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "rawQuery fail!");
            return cursor;
        }
    }

    protected Cursor a(SQLiteDatabase sQLiteDatabase, String str, String[] strArr, String str2, String[] strArr2, String str3) {
        Cursor cursor = null;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            return sQLiteDatabase.query(str, strArr, str2, strArr2, null, null, str3);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "query fail!");
            return cursor;
        }
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        synchronized (this.sS) {
            ContentProviderResult[] contentProviderResultArr = null;
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
                return contentProviderResultArr;
            } catch (Throwable th) {
                if (database.inTransaction()) {
                    database.endTransaction();
                }
            }
        }
    }

    protected void aq(String str) {
        if (this.ps == null) {
            this.ps = new HashSet();
        }
        this.ps.add(str);
    }

    protected void ar(String str) {
        if (this.ps != null) {
            this.ps.remove(str);
        }
    }

    protected long b(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues) {
        long j = -1;
        if (sQLiteDatabase == null) {
            return -1;
        }
        try {
            return sQLiteDatabase.replace(str, null, contentValues);
        } catch (SQLiteException e) {
            e.printStackTrace();
            mb.o("PiDBProvider", "replace fail!");
            return j;
        }
    }

    protected void b(SQLiteDatabase sQLiteDatabase, String str) {
        if (sQLiteDatabase != null) {
            try {
                sQLiteDatabase.execSQL(str);
            } catch (SQLiteException e) {
                e.printStackTrace();
                mb.o("PiDBProvider", "execSQL fail!");
            }
        }
    }

    protected void bg() {
        if (this.ps == null || this.ps.size() <= 0) {
            if (this.sU != null && this.mType == 1) {
                this.sU.close();
            } else if (this.sV != null && this.mType == 2) {
                this.sV.close();
            }
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        synchronized (this.sS) {
            String path = uri.getPath();
            if ("/delete".equals(path)) {
                int a = a(getDatabase(), uri.getQuery(), str, strArr);
                return a;
            } else if ("/execSQL".equals(path)) {
                b(getDatabase(), uri.getQuery());
                return 0;
            } else if ("/closecursor".equals(path)) {
                ar(uri.getQuery());
                return 0;
            } else if ("/close".equals(path)) {
                bg();
                return 0;
            } else {
                mb.o("PiDBProvider", "error delete: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
    }

    protected SQLiteDatabase getDatabase() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (applicaionContext == null) {
            mb.n("PiDBProvider", "ProviderUtil.getForeContext()： " + applicaionContext);
            return null;
        } else if (this.mType != 1) {
            if (this.sV == null) {
                this.sV = new hg(applicaionContext, this.mName, null, this.sW, this.sY) {
                    public void onCreate(SQLiteDatabase sQLiteDatabase) {
                        mb.n("DBService", "SDCardSQLiteDatabase|onCreate|name=" + jh.this.mName + "|version=" + jh.this.sW);
                        jh.this.sX.onCreate(sQLiteDatabase);
                    }

                    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                        mb.s("DBService", "SDCardSQLiteDatabase|onUpgrade|name=" + jh.this.mName + "|oldversion=" + i + "|newVersion=" + i2);
                        jh.this.sX.onUpgrade(sQLiteDatabase, i, i2);
                    }
                };
            }
            return this.sV.getWritableDatabase();
        } else {
            if (this.sU == null) {
                this.sU = new SQLiteOpenHelper(applicaionContext, this.mName, null, this.sW) {
                    public void onCreate(SQLiteDatabase sQLiteDatabase) {
                        mb.n("DBService", "SQLiteDatabase|onCreate|name=" + jh.this.mName + "|version=" + jh.this.sW);
                        jh.this.sX.onCreate(sQLiteDatabase);
                    }

                    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                        mb.s("DBService", "SQLiteDatabase|onDowngrade|name=" + jh.this.mName + "|oldversion=" + i + "|newVersion=" + i2);
                        jh.this.sX.onDowngrade(sQLiteDatabase, i, i2);
                    }

                    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                        mb.s("DBService", "SQLiteDatabase|onUpgrade|name=" + jh.this.mName + "|oldversion=" + i + "|newVersion=" + i2);
                        jh.this.sX.onUpgrade(sQLiteDatabase, i, i2);
                    }
                };
            }
            return this.sU.getWritableDatabase();
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        synchronized (this.sS) {
            Uri parse;
            if ("/insert".equals(uri.getPath())) {
                parse = Uri.parse("content://" + uri.getAuthority() + "?" + a(getDatabase(), uri.getQuery(), contentValues));
                return parse;
            }
            if ("/replace".equals(uri.getPath())) {
                parse = Uri.parse("content://" + uri.getAuthority() + "?" + b(getDatabase(), uri.getQuery(), contentValues));
                return parse;
            }
            mb.o("PiDBProvider", "error insert: " + uri.toString());
            throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
        }
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        synchronized (this.sS) {
            Object path = uri.getPath();
            int indexOf = path.indexOf("_");
            if (indexOf != -1) {
                aq(path.substring(indexOf + 1));
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
                mb.o("PiDBProvider", "error query: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int a;
        synchronized (this.sS) {
            if ("/update".equals(uri.getPath())) {
                a = a(getDatabase(), uri.getQuery(), contentValues, str, strArr);
            } else {
                mb.o("PiDBProvider", "error update: " + uri.toString());
                throw new IllegalArgumentException("the uri " + uri.toString() + "is error");
            }
        }
        return a;
    }
}
