package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.f;
import tmsdkobf.ji.a;

public class jm {
    private static Object lock = new Object();
    private static jm tj;
    private jv oA;

    private jm() {
        this.oA = null;
        this.oA = ((kf) fj.D(9)).ap("QQSecureProvider");
    }

    private ContentValues a(as asVar) {
        int i = 0;
        ContentValues contentValues = new ContentValues();
        if (asVar == null) {
            return contentValues;
        }
        contentValues.put("b", Integer.valueOf(asVar.bR));
        contentValues.put("c", Integer.valueOf(asVar.valueType));
        switch (asVar.valueType) {
            case 1:
                contentValues.put("d", Integer.valueOf(asVar.i));
                break;
            case 2:
                contentValues.put("e", Long.valueOf(asVar.bS));
                break;
            case 3:
                contentValues.put("f", asVar.bT);
                break;
            case 4:
                contentValues.put("g", asVar.bU);
                break;
            case 5:
                String str = "h";
                if (asVar.bV) {
                    i = 1;
                }
                contentValues.put(str, Integer.valueOf(i));
                break;
            case 6:
                contentValues.put("i", Integer.valueOf(asVar.bW));
                break;
        }
        return contentValues;
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pf_key_value_profile_db_table_name (a INTEGER PRIMARY KEY,b INTEGER,c INTEGER,d INTEGER,e LONG,f TEXT,h INTEGER,i INTEGER,g BLOB)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 15) {
            a(sQLiteDatabase);
        }
    }

    private ArrayList<jp> aP(String str) {
        Cursor cursor = null;
        ArrayList<jp> arrayList = new ArrayList();
        try {
            cursor = this.oA.a("pf_key_value_profile_db_table_name", null, str, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    as asVar = new as();
                    asVar.valueType = cursor.getInt(cursor.getColumnIndex("c"));
                    asVar.bR = cursor.getInt(cursor.getColumnIndex("b"));
                    int i = cursor.getInt(cursor.getColumnIndex("a"));
                    switch (asVar.valueType) {
                        case 1:
                            asVar.i = cursor.getInt(cursor.getColumnIndex("d"));
                            break;
                        case 2:
                            asVar.bS = cursor.getLong(cursor.getColumnIndex("e"));
                            break;
                        case 3:
                            asVar.bT = cursor.getString(cursor.getColumnIndex("f"));
                            break;
                        case 4:
                            asVar.bU = cursor.getBlob(cursor.getColumnIndex("g"));
                            break;
                        case 5:
                            asVar.bV = cursor.getInt(cursor.getColumnIndex("h")) == 1;
                            break;
                        case 6:
                            asVar.bW = (short) ((short) cursor.getInt(cursor.getColumnIndex("i")));
                            break;
                    }
                    arrayList.add(new jp(asVar, i));
                    cursor.moveToNext();
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        f.e("KeyValueProfileDB", "cursor.close() crash : " + e.toString());
                    }
                }
                return arrayList;
            }
            ArrayList<jp> arrayList2 = arrayList;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    f.e("KeyValueProfileDB", "cursor.close() crash : " + e2.toString());
                }
            }
            return arrayList;
        } catch (Exception e3) {
            f.e("KeyValueProfileDB", e3.toString());
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e32) {
                    f.e("KeyValueProfileDB", "cursor.close() crash : " + e32.toString());
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                    f.e("KeyValueProfileDB", "cursor.close() crash : " + e4.toString());
                }
            }
        }
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pf_key_value_profile_db_table_name");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    public static jm cw() {
        if (tj == null) {
            synchronized (lock) {
                if (tj == null) {
                    tj = new jm();
                }
            }
        }
        return tj;
    }

    public boolean a(ArrayList<a> arrayList, ArrayList<Boolean> arrayList2) {
        if (arrayList == null || arrayList.size() <= 0) {
            return true;
        }
        a aVar;
        ArrayList arrayList3 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            aVar = (a) it.next();
            if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof as))) {
                as asVar = (as) aVar.ta;
                arrayList3.add(ContentProviderOperation.newDelete(this.oA.an("pf_key_value_profile_db_table_name")).withSelection(String.format("%s = %s", new Object[]{"b", Integer.valueOf(asVar.bR)}), null).build());
            }
        }
        it = arrayList.iterator();
        while (it.hasNext()) {
            aVar = (a) it.next();
            if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof as))) {
                arrayList3.add(ContentProviderOperation.newInsert(this.oA.am("pf_key_value_profile_db_table_name")).withValues(a((as) aVar.ta)).build());
            }
        }
        if (arrayList3 != null && arrayList3.size() > 0) {
            ContentProviderResult[] applyBatch = this.oA.applyBatch(arrayList3);
            if (applyBatch == null || applyBatch.length <= 0 || applyBatch[0] == null) {
                gr.f("KeyValueProfileDB", "applyBatchOperation fail!!!");
                return false;
            } else if (arrayList2 != null) {
                int length = applyBatch.length / 2;
                for (int i = 0; i < length; i++) {
                    if (applyBatch[i] != null) {
                        arrayList2.add(Boolean.valueOf(applyBatch[i].count.intValue() > 0));
                    }
                }
            }
        }
        return true;
    }

    public int ai(int i) {
        return this.oA.delete("pf_key_value_profile_db_table_name", "b = " + i, null);
    }

    public ArrayList<JceStruct> getAll() {
        ArrayList<JceStruct> arrayList = new ArrayList();
        ArrayList aP = aP(null);
        if (aP == null || aP.size() <= 0) {
            return arrayList;
        }
        Iterator it = aP.iterator();
        while (it.hasNext()) {
            arrayList.add(((jp) it.next()).tr);
        }
        return arrayList;
    }
}
