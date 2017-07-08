package tmsdkobf;

import android.database.sqlite.SQLiteDatabase;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class gf {
    private static Object lock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.gf.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.gf.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.gf.<clinit>():void");
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("create table if not exists dcp_info(info1 text,info2 blob,info4 blob,info3 blob)");
        sQLiteDatabase.execSQL("create table if not exists dcr_info(info1 text,info2 blob)");
        sQLiteDatabase.execSQL("create index if not exists dcp_index on dcp_info(info1)");
        sQLiteDatabase.execSQL("create index if not exists dcr_index on dcr_info(info1)");
        sQLiteDatabase.execSQL("create table if not exists up(info1 text primary key,info2 integer)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("DeepCleanCloudDB", "upgradeDB  " + i + " >> " + i2 + ">> " + 18);
        if (i < 18) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE dcp_info ADD COLUMN info4 blob");
                d.d("DeepCleanCloudDB", "add  column sucess");
            } catch (Exception e) {
                d.d("DeepCleanCloudDB", "add  column::" + e.toString());
            }
            kz.dx().dy();
        }
        a(sQLiteDatabase);
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("drop table if exists dcd_info");
        sQLiteDatabase.execSQL("create table if not exists dcd_info(info1 text,info2 blob)");
        sQLiteDatabase.execSQL("drop table if exists dcp_info");
        sQLiteDatabase.execSQL("create table if not exists dcp_info(info1 text,info2 blob,info4 blob,info3 blob)");
        sQLiteDatabase.execSQL("create index if not exists dcd_index on dcd_info(info1)");
        sQLiteDatabase.execSQL("create index if not exists dcp_index on dcp_info(info1)");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("DeepCleanCloudDB", "downgradeDB");
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }
}
