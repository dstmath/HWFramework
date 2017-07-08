package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class hs {
    public static String TAG;
    private static Object lock;
    private static hs qV;
    private lc qU;
    byte qW;

    /* compiled from: Unknown */
    public static class a {
        public int bf;
        byte[] data;
        public int qR;
        public int qX;
        private ak qY;

        public a(byte[] bArr, int i, int i2, int i3) {
            this.qX = -1;
            this.qR = -1;
            this.bf = 0;
            this.qY = null;
            this.qR = i2;
            this.data = bArr;
            this.qX = i;
            this.bf = i3;
        }

        public ak bC() {
            if (this.qY == null && this.qX == 0 && this.data != null && this.data.length > 0) {
                try {
                    this.qY = hu.f(this.data);
                } catch (Throwable th) {
                    d.c(hs.TAG, th);
                }
            }
            return this.qY;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.hs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.hs.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.hs.<clinit>():void");
    }

    private hs() {
        this.qU = null;
        this.qW = (byte) 0;
        this.qU = ((ln) fe.ad(9)).bp("QQSecureProvider");
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS profile_fifo_upload_queue (a INTEGER PRIMARY KEY,c INTEGER,d INTEGER,e INTEGER,b BLOB)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 15) {
            a(sQLiteDatabase);
        }
    }

    private String aI(int i) {
        return String.format("%s = %s", new Object[]{"c", Integer.valueOf(i)});
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
        int bE = ht.bD().bE();
        contentValues.put("d", Integer.valueOf(bE));
        if ((this.qU.a("profile_fifo_upload_queue", contentValues) < 0 ? 1 : null) != null) {
            obj = null;
        }
        if (obj == null) {
            return -1;
        }
        ht.bD().bF();
        return bE;
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS profile_fifo_upload_queue");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    public static hs bz() {
        if (qV == null) {
            synchronized (lock) {
                if (qV == null) {
                    qV = new hs();
                }
            }
        }
        return qV;
    }

    private a f(String str, String str2) {
        ArrayList g = g(str, str2);
        return (g != null && g.size() > 0) ? (a) g.get(0) : null;
    }

    private ArrayList<a> g(String str, String str2) {
        Exception e;
        Throwable th;
        ArrayList<a> arrayList = new ArrayList();
        Cursor a;
        try {
            a = this.qU.a("profile_fifo_upload_queue", null, str, null, str2);
            if (a != null) {
                try {
                    a.moveToFirst();
                    while (!a.isAfterLast()) {
                        arrayList.add(new a(a.getBlob(a.getColumnIndex("b")), a.getInt(a.getColumnIndex("e")), a.getInt(a.getColumnIndex("d")), a.getInt(a.getColumnIndex("c"))));
                        a.moveToNext();
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Exception e2) {
                            d.c(TAG, "cursor.close() crash : " + e2.toString());
                        }
                    }
                } catch (Exception e3) {
                    e2 = e3;
                }
                return arrayList;
            }
            if (a != null) {
                try {
                    a.close();
                } catch (Exception e22) {
                    d.c(TAG, "cursor.close() crash : " + e22.toString());
                }
            }
            return arrayList;
        } catch (Exception e4) {
            e22 = e4;
            a = null;
            try {
                d.c(TAG, e22.toString());
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e222) {
                        d.c(TAG, "cursor.close() crash : " + e222.toString());
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e5) {
                        d.c(TAG, "cursor.close() crash : " + e5.toString());
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            a = null;
            if (a != null) {
                a.close();
            }
            throw th;
        }
    }

    public boolean aG(int i) {
        return this.qU.delete("profile_fifo_upload_queue", aI(i), null) > 0;
    }

    public byte[] aH(int i) {
        String str = "d = " + i;
        a f = f(str, null);
        if (f == null) {
            return null;
        }
        int delete = this.qU.delete("profile_fifo_upload_queue", str, null);
        if (delete > 1) {
            hu.h(hv.TAG, "delete error! \u591a\u4e8e\u4e00\u884c\u88abdelete\u4e86\uff01\uff01");
        } else if (delete == 0) {
            return null;
        }
        return f.data;
    }

    public int b(byte[] bArr, int i) {
        return b(bArr, 0, i);
    }

    public int bA() {
        return b(null, 1, 0);
    }

    public List<a> bB() {
        return g(null, "d");
    }
}
