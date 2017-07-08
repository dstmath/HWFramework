package com.android.server.jankshield;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.JankEventData;
import android.util.Log;
import com.android.server.rms.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TableJankEvent {
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = false;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "JankShield";
    private static final String[] field_Names = null;
    private static final String[] field_Types = null;
    public static final long recDELTACOUNT = 200;
    public static final long recMAXCOUNT = 2000;
    private static long recordCount = 0;
    public static final String tbName = "JankEvent";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.jankshield.TableJankEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.jankshield.TableJankEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.jankshield.TableJankEvent.<clinit>():void");
    }

    public static boolean insert(SQLiteDatabase db, JankEventData jankevent) {
        boolean ret = HWFLOW;
        ContentValues values = jankevent.getContentValues(field_Names);
        if (values == null) {
            return HWFLOW;
        }
        if (recordCount >= 2200) {
            delete(db, (recordCount - recMAXCOUNT) + 1);
        }
        if (-1 != db.insert(tbName, null, values)) {
            ret = HWLOGW_E;
            recordCount++;
        } else if (HWFLOW) {
            Log.i(TAG, "insert(" + values.toString() + ") failed");
        }
        return ret;
    }

    private static void delete(SQLiteDatabase db, long num) {
        String lastTime = getOutdate(60);
        db.beginTransaction();
        try {
            db.execSQL("delete from JankEvent where id in(select id from JankEvent order by TimeStamp asc limit " + num + " )");
            db.execSQL("delete from JankEvent where TimeStamp <= " + lastTime + " ");
            db.setTransactionSuccessful();
            getCount(db);
        } finally {
            db.endTransaction();
        }
    }

    public static long getCount(SQLiteDatabase db) {
        recordCount = DatabaseUtils.longForQuery(db, "select count(*) from JankEvent", null);
        return recordCount;
    }

    public static void dropTb(SQLiteDatabase db) {
        db.execSQL("drop table if exists JankEvent" + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
    }

    public static String getOutdate(int days) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(new Date().getTime() - (((long) days) * Utils.DATE_TIME_24HOURS)));
    }

    public static String getCreateSql() {
        return getAddSql("create table if not exists JankEvent (id integer primary key autoincrement ", field_Names, field_Types);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > 3 && oldVersion <= 3) {
            db.beginTransaction();
            try {
                db.execSQL("alter table JankEvent rename to _temp_JankEvent");
                db.execSQL(getCreateSql());
                db.execSQL("insert into JankEvent select id, CaseName, TimeStamp, Arg1, Arg2, CpuLoad, FreeMem, FreeStorage, Limit_Freq, CpuLoadTop_proc1, CpuLoadTop_proc2, CpuLoadTop_proc3, '0', '0', '0', '0', Reserve1, Reserve2 from _temp_JankEvent");
                db.execSQL("drop table _temp_JankEvent");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public static void downgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static String getAddSql(String str, String[] names, String[] types) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            buf.append(", ");
            buf.append(names[i]);
            buf.append(" ");
            buf.append(types[i]);
        }
        buf.append(" )");
        return str + buf.toString();
    }
}
