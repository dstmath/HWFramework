package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.f;
import tmsdkobf.ji.a;

public class jr {
    private static Object lock = new Object();
    private static jr tt;
    private jv oA;

    private jr() {
        this.oA = null;
        this.oA = ((kf) fj.D(9)).ap("QQSecureProvider");
    }

    private ContentValues a(av avVar) {
        int i = 0;
        if (avVar == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("b", avVar.packageName);
        contentValues.put("c", avVar.softName);
        contentValues.put("d", avVar.bZ);
        contentValues.put("e", Long.valueOf(avVar.ca));
        String str = "f";
        if (avVar.cb) {
            i = 1;
        }
        contentValues.put(str, Integer.valueOf(i));
        contentValues.put("h", avVar.aS);
        contentValues.put("i", Long.valueOf(avVar.cd));
        contentValues.put("j", avVar.version);
        contentValues.put("g", avVar.cc);
        contentValues.put("k", Long.valueOf(avVar.ce));
        return contentValues;
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pf_soft_list_profile_db_table_name (a INTEGER PRIMARY KEY,b TEXT,c TEXT,d TEXT,e LONG,f INTEGER,h TEXT,i LONG,g TEXT,j TEXT,k LONG)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        f.f("SoftListProfileDB", "upgradeDB");
        if (i < 15) {
            a(sQLiteDatabase);
        }
        if (i >= 15 && i < 18) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE pf_soft_list_profile_db_table_name ADD COLUMN k LONG");
            } catch (Exception e) {
            }
        }
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pf_soft_list_profile_db_table_name");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        f.f("SoftListProfileDB", "downgradeDB");
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    public static jr cB() {
        if (tt == null) {
            synchronized (lock) {
                if (tt == null) {
                    tt = new jr();
                }
            }
        }
        return tt;
    }

    private ArrayList<jq> cD() {
        ArrayList<jq> arrayList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = this.oA.a("pf_soft_list_profile_db_table_name", null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    av avVar = new av();
                    avVar.ca = cursor.getLong(cursor.getColumnIndex("e"));
                    avVar.bZ = cursor.getString(cursor.getColumnIndex("d"));
                    avVar.aS = cursor.getString(cursor.getColumnIndex("h"));
                    avVar.cb = cursor.getInt(cursor.getColumnIndex("f")) == 1;
                    avVar.packageName = cursor.getString(cursor.getColumnIndex("b"));
                    avVar.softName = cursor.getString(cursor.getColumnIndex("c"));
                    avVar.version = cursor.getString(cursor.getColumnIndex("j"));
                    avVar.cd = cursor.getLong(cursor.getColumnIndex("i"));
                    avVar.cc = cursor.getString(cursor.getColumnIndex("g"));
                    avVar.ce = cursor.getLong(cursor.getColumnIndex("k"));
                    arrayList.add(new jq(avVar, cursor.getInt(cursor.getColumnIndex("a"))));
                    cursor.moveToNext();
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        f.e("SoftListProfileDB", "cursor.close() crash : " + e.toString());
                    }
                }
                return arrayList;
            }
            ArrayList<jq> arrayList2 = arrayList;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    f.e("SoftListProfileDB", "cursor.close() crash : " + e2.toString());
                }
            }
            return arrayList;
        } catch (Exception e3) {
            f.e("SoftListProfileDB", e3.toString());
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e32) {
                    f.e("SoftListProfileDB", "cursor.close() crash : " + e32.toString());
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                    f.e("SoftListProfileDB", "cursor.close() crash : " + e4.toString());
                }
            }
        }
    }

    private void clear() {
        this.oA.delete("pf_soft_list_profile_db_table_name", null, null);
    }

    public ArrayList<av> cC() {
        f.f("SoftListProfileDB", "getAllSoftImage");
        ArrayList<av> arrayList = new ArrayList();
        ArrayList cD = cD();
        if (cD != null && cD.size() > 0) {
            Iterator it = cD.iterator();
            while (it.hasNext()) {
                jq jqVar = (jq) it.next();
                if (!(jqVar == null || jqVar.ts == null)) {
                    arrayList.add(jqVar.ts);
                }
            }
        }
        return arrayList;
    }

    public boolean l(ArrayList<a> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            return true;
        }
        ArrayList arrayList2 = new ArrayList();
        Object obj = null;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof av))) {
                av avVar = (av) aVar.ta;
                switch (aVar.action) {
                    case 0:
                        if (obj == null) {
                            obj = 1;
                            clear();
                            break;
                        }
                        break;
                    case 1:
                        break;
                    case 2:
                        arrayList2.add(ContentProviderOperation.newDelete(this.oA.an("pf_soft_list_profile_db_table_name")).withSelection(String.format("%s = '%s'", new Object[]{"b", avVar.packageName}), null).build());
                        continue;
                    case 3:
                        arrayList2.add(ContentProviderOperation.newUpdate(this.oA.ao("pf_soft_list_profile_db_table_name")).withValues(a(avVar)).withSelection(String.format("%s = '%s'", new Object[]{"b", avVar.packageName}), null).build());
                        continue;
                    default:
                        continue;
                }
                arrayList2.add(ContentProviderOperation.newInsert(this.oA.am("pf_soft_list_profile_db_table_name")).withValues(a(avVar)).build());
            }
        }
        if (arrayList2 != null && arrayList2.size() > 0) {
            ContentProviderResult[] applyBatch = this.oA.applyBatch(arrayList2);
            if (applyBatch == null || applyBatch.length <= 0 || applyBatch[0] == null) {
                gr.f("SoftListProfileDB", "applyBatchOperation fail!!!");
                return false;
            }
        }
        return true;
    }
}
