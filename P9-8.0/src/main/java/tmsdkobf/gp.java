package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.f;

public class gp {
    private static Object lock = new Object();
    private static gp oB;
    private jv oA;
    byte oC;

    public static class a {
        public int bK = 0;
        byte[] data;
        public int oD = -1;
        private ar oE = null;
        public int ox = -1;

        public a(byte[] bArr, int i, int i2, int i3) {
            this.ox = i2;
            this.data = bArr;
            this.oD = i;
            this.bK = i3;
        }

        public ar aY() {
            if (this.oE == null && this.oD == 0 && this.data != null && this.data.length > 0) {
                try {
                    this.oE = gr.f(this.data);
                } catch (Throwable th) {
                    f.e("ProfileQueue", th);
                }
            }
            return this.oE;
        }
    }

    private gp() {
        this.oA = null;
        this.oC = (byte) 0;
        this.oA = ((kf) fj.D(9)).ap("QQSecureProvider");
    }

    private String Q(int i) {
        return String.format("%s = %s", new Object[]{"c", Integer.valueOf(i)});
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS profile_fifo_upload_queue (a INTEGER PRIMARY KEY,c INTEGER,d INTEGER,e INTEGER,b BLOB)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 15) {
            a(sQLiteDatabase);
        }
    }

    public static gp aV() {
        if (oB == null) {
            synchronized (lock) {
                if (oB == null) {
                    oB = new gp();
                }
            }
        }
        return oB;
    }

    private int b(byte[] bArr, int i, int i2) {
        Object obj = 1;
        ContentValues contentValues = new ContentValues();
        if (bArr != null && bArr.length > 0) {
            contentValues.put("b", bArr);
        }
        contentValues.put("e", Integer.valueOf(i));
        if (i2 > 0 && i2 < 5) {
            contentValues.put("c", Integer.valueOf(i2));
        }
        int ba = gq.aZ().ba();
        contentValues.put("d", Integer.valueOf(ba));
        if ((this.oA.a("profile_fifo_upload_queue", contentValues) < 0 ? 1 : null) != null) {
            obj = null;
        }
        if (obj == null) {
            return -1;
        }
        gq.aZ().bb();
        return ba;
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS profile_fifo_upload_queue");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    private a c(String str, String str2) {
        ArrayList e = e(str, str2);
        return (e != null && e.size() > 0) ? (a) e.get(0) : null;
    }

    private ArrayList<a> e(String str, String str2) {
        Cursor cursor = null;
        ArrayList<a> arrayList = new ArrayList();
        try {
            cursor = this.oA.a("profile_fifo_upload_queue", null, str, null, str2);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    arrayList.add(new a(cursor.getBlob(cursor.getColumnIndex("b")), cursor.getInt(cursor.getColumnIndex("e")), cursor.getInt(cursor.getColumnIndex("d")), cursor.getInt(cursor.getColumnIndex("c"))));
                    cursor.moveToNext();
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        f.e("ProfileQueue", "cursor.close() crash : " + e.toString());
                    }
                }
                return arrayList;
            }
            ArrayList<a> arrayList2 = arrayList;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    f.e("ProfileQueue", "cursor.close() crash : " + e2.toString());
                }
            }
            return arrayList;
        } catch (Exception e3) {
            f.e("ProfileQueue", e3.toString());
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e32) {
                    f.e("ProfileQueue", "cursor.close() crash : " + e32.toString());
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                    f.e("ProfileQueue", "cursor.close() crash : " + e4.toString());
                }
            }
        }
    }

    public boolean O(int i) {
        return this.oA.delete("profile_fifo_upload_queue", Q(i), null) > 0;
    }

    public byte[] P(int i) {
        String str = "d = " + i;
        a c = c(str, null);
        if (c == null) {
            return null;
        }
        int delete = this.oA.delete("profile_fifo_upload_queue", str, null);
        if (delete > 1) {
            gr.f("ProfileUpload", "delete error! 多于一行被delete了！！");
        } else if (delete == 0) {
            return null;
        }
        return c.data;
    }

    public int a(byte[] bArr, int i) {
        return b(bArr, 0, i);
    }

    public int aW() {
        return b(null, 1, 0);
    }

    public List<a> aX() {
        return e(null, "d");
    }
}
