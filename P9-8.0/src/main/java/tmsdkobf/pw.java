package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.qq.taf.jce.a;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.TccCryptor;

public class pw {
    static pw KQ = null;
    private static Object KR = new Object();
    private SQLiteOpenHelper KS;
    private final String KT = "CREATE TABLE IF NOT EXISTS r_tb (a INTEGER PRIMARY KEY,f INTEGER,b INTEGER,c INTEGER,d INTEGER,e LONG,i TEXT,j TEXT,k INTEGER,l INTEGER)";

    private pw() {
        ps.g("DataManager-DataManager");
        this.KS = new SQLiteOpenHelper(TMSDKContext.getApplicaionContext(), "r.db", null, 10) {
            public void onCreate(SQLiteDatabase sQLiteDatabase) {
                ps.g("onCreate-db:[" + sQLiteDatabase + "]");
                pw.this.onCreate(sQLiteDatabase);
            }

            public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                ps.g("onDowngrade-db:[" + sQLiteDatabase + "]oldVersion:[" + i + "]newVersion:[" + i2 + "]");
            }

            public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
                ps.g("onUpgrade-db:[" + sQLiteDatabase + "]oldVersion:[" + i + "]newVersion:[" + i2 + "]");
                pw.this.onUpgrade(sQLiteDatabase, i, i2);
            }
        };
        this.KS.getWritableDatabase().setLockingEnabled(false);
    }

    private long a(String str, ContentValues contentValues) {
        long insert;
        synchronized (KR) {
            insert = this.KS.getWritableDatabase().insert(str, null, contentValues);
        }
        return insert;
    }

    private Cursor al(String str) {
        Cursor rawQuery;
        synchronized (KR) {
            rawQuery = this.KS.getReadableDatabase().rawQuery(str, null);
        }
        return rawQuery;
    }

    private void close() {
        synchronized (KR) {
            this.KS.close();
        }
    }

    private int delete(String str, String str2, String[] strArr) {
        int delete;
        synchronized (KR) {
            delete = this.KS.getWritableDatabase().delete(str, str2, strArr);
        }
        return delete;
    }

    private ContentValues f(pv pvVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("a", Integer.valueOf(pvVar.KN.KV));
        contentValues.put("b", Integer.valueOf(pvVar.KN.KW));
        contentValues.put("c", Integer.valueOf(pvVar.KN.KX));
        contentValues.put("d", Integer.valueOf(pvVar.KN.KY));
        contentValues.put("e", Long.valueOf(pvVar.KN.KZ));
        contentValues.put("f", Integer.valueOf(pvVar.KN.La));
        contentValues.put("i", pvVar.KN.Lb);
        contentValues.put("j", a.c(TccCryptor.encrypt(pvVar.KN.Lc.getBytes(), null)));
        contentValues.put("k", Integer.valueOf(pvVar.KO));
        contentValues.put("l", Integer.valueOf(pvVar.KP));
        return contentValues;
    }

    private List<pv> f(Cursor cursor) {
        List<pv> arrayList = new ArrayList();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                pv pvVar = new pv();
                pvVar.KN = new px();
                pvVar.KN.KV = cursor.getInt(cursor.getColumnIndex("a"));
                pvVar.KN.KW = cursor.getInt(cursor.getColumnIndex("b"));
                pvVar.KN.KX = cursor.getInt(cursor.getColumnIndex("c"));
                pvVar.KN.KY = cursor.getInt(cursor.getColumnIndex("d"));
                pvVar.KN.KZ = cursor.getLong(cursor.getColumnIndex("e"));
                pvVar.KN.La = cursor.getInt(cursor.getColumnIndex("f"));
                pvVar.KN.Lb = cursor.getString(cursor.getColumnIndex("i"));
                pvVar.KN.Lc = new String(TccCryptor.decrypt(a.E(cursor.getString(cursor.getColumnIndex("j"))), null));
                pvVar.KO = cursor.getInt(cursor.getColumnIndex("k"));
                pvVar.KP = cursor.getInt(cursor.getColumnIndex("l"));
                arrayList.add(pvVar);
                cursor.moveToNext();
            }
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
        }
        return arrayList.size() != 0 ? arrayList : null;
    }

    public static pw ih() {
        Class cls = pw.class;
        synchronized (pw.class) {
            if (KQ == null) {
                Class cls2 = pw.class;
                synchronized (pw.class) {
                    if (KQ == null) {
                        KQ = new pw();
                    }
                }
            }
            return KQ;
        }
    }

    private void onCreate(SQLiteDatabase sQLiteDatabase) {
        ps.g("execSQL:[CREATE TABLE IF NOT EXISTS r_tb (a INTEGER PRIMARY KEY,f INTEGER,b INTEGER,c INTEGER,d INTEGER,e LONG,i TEXT,j TEXT,k INTEGER,l INTEGER)]");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS r_tb (a INTEGER PRIMARY KEY,f INTEGER,b INTEGER,c INTEGER,d INTEGER,e LONG,i TEXT,j TEXT,k INTEGER,l INTEGER)");
    }

    private void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
    }

    private int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        int update;
        synchronized (KR) {
            update = this.KS.getWritableDatabase().update(str, contentValues, str2, strArr);
        }
        return update;
    }

    public pv bQ(int i) {
        ps.g("getDataItem-id:[" + i + "]");
        pv pvVar = null;
        Cursor cursor = null;
        try {
            StringBuilder stringBuilder = new StringBuilder(120);
            stringBuilder.append("SELECT * FROM ");
            stringBuilder.append("r_tb");
            stringBuilder.append(" WHERE ");
            stringBuilder.append("a");
            stringBuilder.append("=");
            stringBuilder.append(i);
            cursor = al(stringBuilder.toString());
            if (cursor != null) {
                List f = f(cursor);
                if (f != null && f.size() > 0) {
                    pvVar = (pv) f.get(0);
                }
            }
            ps.g("getDataItem-item:[" + pvVar + "]");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th) {
                    ps.g("e:[" + th + "]");
                }
            }
        } catch (Throwable th2) {
            ps.g("e:[" + th2 + "]");
        }
        return pvVar;
    }

    public void bR(int i) {
        ps.g("deleteDataItem-id:[" + i + "]");
        try {
            delete("r_tb", "a=" + i, null);
        } catch (Throwable th) {
            ps.g("e:[" + th + "]");
        }
    }

    public void d(pv pvVar) {
        ps.g("updateDataItem:[" + pvVar + "]");
        try {
            update("r_tb", f(pvVar), "a=" + pvVar.KN.KV, null);
        } catch (Throwable th) {
            ps.g("e:[" + th + "]");
        }
    }

    public void e(pv pvVar) {
        ps.g("insertDataItem:[" + pvVar + "]");
        try {
            a("r_tb", f(pvVar));
        } catch (Throwable th) {
            ps.g("e:[" + th + "]");
        }
    }

    public int getCount() {
        int i = 0;
        Cursor cursor = null;
        try {
            StringBuilder stringBuilder = new StringBuilder(120);
            stringBuilder.append("SELECT COUNT(*) FROM ");
            stringBuilder.append("r_tb");
            cursor = al(stringBuilder.toString());
            if (cursor != null && cursor.moveToNext()) {
                i = cursor.getInt(0);
            }
            ps.g("getCount-size:[" + i + "]");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th) {
                    ps.g("e:[" + th + "]");
                }
            }
        } catch (Throwable th2) {
            ps.g("e:[" + th2 + "]");
        }
        return i;
    }

    public void ii() {
        ps.g("DataManager-freeInstance");
        close();
    }

    public List<pv> ij() {
        List<pv> list = null;
        Cursor cursor = null;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            StringBuilder stringBuilder = new StringBuilder(120);
            stringBuilder.append("SELECT * FROM ");
            stringBuilder.append("r_tb");
            stringBuilder.append(" WHERE ");
            stringBuilder.append("k");
            stringBuilder.append("=");
            stringBuilder.append(2);
            stringBuilder.append(" OR ");
            stringBuilder.append("e");
            stringBuilder.append("<");
            stringBuilder.append(currentTimeMillis);
            ps.g("getCleanItems-sql:[" + stringBuilder.toString() + "]");
            cursor = al(stringBuilder.toString());
            if (cursor != null) {
                list = f(cursor);
            }
            ps.g("getCleanItems-size:[" + (list == null ? 0 : list.size()) + "]");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th) {
                    ps.g("e:[" + th + "]");
                }
            }
        } catch (Throwable th2) {
            ps.g("e:[" + th2 + "]");
        }
        return list;
    }

    public List<pv> ik() {
        List<pv> list = null;
        Cursor cursor = null;
        try {
            StringBuilder stringBuilder = new StringBuilder(120);
            stringBuilder.append("SELECT * FROM ");
            stringBuilder.append("r_tb");
            stringBuilder.append(" WHERE ");
            stringBuilder.append("l");
            stringBuilder.append("=");
            stringBuilder.append(1);
            stringBuilder.append(" OR ");
            stringBuilder.append("l");
            stringBuilder.append("=");
            stringBuilder.append(2);
            stringBuilder.append(" AND ");
            stringBuilder.append("k");
            stringBuilder.append("=");
            stringBuilder.append(1);
            ps.g("getNeedDownloadItems-sql:[" + stringBuilder.toString() + "]");
            cursor = al(stringBuilder.toString());
            if (cursor != null) {
                list = f(cursor);
            }
            ps.g("getNeedDownloadItems-size:[" + (list == null ? 0 : list.size()) + "]");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th) {
                    ps.g("e:[" + th + "]");
                }
            }
        } catch (Throwable th2) {
            ps.g("e:[" + th2 + "]");
        }
        return list;
    }

    public List<pv> il() {
        List<pv> list = null;
        Cursor cursor = null;
        try {
            StringBuilder stringBuilder = new StringBuilder(120);
            stringBuilder.append("SELECT * FROM ");
            stringBuilder.append("r_tb");
            stringBuilder.append(" WHERE ");
            stringBuilder.append("l");
            stringBuilder.append("=");
            stringBuilder.append(3);
            stringBuilder.append(" AND ");
            stringBuilder.append("d");
            stringBuilder.append("=");
            stringBuilder.append(1);
            ps.g("getAutoRunItems-sql:[" + stringBuilder.toString() + "]");
            cursor = al(stringBuilder.toString());
            if (cursor != null) {
                list = f(cursor);
            }
            ps.g("getAutoRunItems-size:[" + (list == null ? 0 : list.size()) + "]");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th) {
                    ps.g("e:[" + th + "]");
                }
            }
        } catch (Throwable th2) {
            ps.g("e:[" + th2 + "]");
        }
        return list;
    }
}
