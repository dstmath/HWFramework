package com.android.server.jankshield;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.JankBdData;
import android.os.JankBdItem;
import android.util.Log;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TableJankBd {
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = false;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "JankShield";
    private static final String[] field_Names = null;
    private static final String[] field_Types = null;
    public static final long recDELTACOUNT = 2000;
    private static long recordCount = 0;
    public static final long recordMAXCOUNT = 20000;
    public static final String tbName = "JankBd";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.jankshield.TableJankBd.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.jankshield.TableJankBd.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.jankshield.TableJankBd.<clinit>():void");
    }

    public static boolean insert(SQLiteDatabase db, JankBdData jankbd) {
        List<JankBdItem> itemslist = jankbd.getItems();
        for (int i = 0; i < itemslist.size(); i++) {
            JankBdItem item = (JankBdItem) itemslist.get(i);
            if (!item.isEmpty()) {
                insertItem(db, item);
            }
        }
        if (recordCount >= 22000) {
            deleteOutdate(db, (recordCount - recordMAXCOUNT) + 1);
        }
        return HWLOGW_E;
    }

    private static void deleteOutdate(SQLiteDatabase db, long num) {
        String lastTime = getOutdate(30);
        db.beginTransaction();
        try {
            db.execSQL("delete from JankBd where TimeStamp <= " + lastTime + " ");
            db.execSQL("delete from JankBd where id in(select id from JankBd order by TimeStamp asc limit " + num + " )");
            db.setTransactionSuccessful();
            getCount(db);
        } finally {
            db.endTransaction();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static JankBdItem extractItem(Cursor cur) {
        JankBdItem item = new JankBdItem();
        item.id = cur.getInt(cur.getColumnIndex(HwDevicePolicyManagerServiceUtil.EXCHANGE_ID));
        item.casename = cur.getString(cur.getColumnIndex("CaseName"));
        item.timestamp = cur.getString(cur.getColumnIndex("TimeStamp"));
        item.appname = cur.getString(cur.getColumnIndex("AppName"));
        item.marks = cur.getString(cur.getColumnIndex("Marks"));
        int i = 1;
        while (i <= 10) {
            int index = cur.getColumnIndex("Section" + i + "_cnt");
            if (index >= 0 && !cur.isNull(index)) {
                int value = cur.getInt(index);
                if (value < 0) {
                    break;
                }
                item.sectionCnts.add(Integer.valueOf(value));
                i++;
            }
        }
        return item;
    }

    public static JankBdItem queryItem(SQLiteDatabase db, String casename, String appname, String marks) {
        Cursor cursor = null;
        JankBdItem jankBdItem = null;
        try {
            cursor = db.rawQuery("select * from JankBd where CaseName = '" + casename + "' AND AppName = '" + appname + "' AND Marks = '" + marks + "' AND TimeStamp >= '" + getOutdate(40) + "'" + " order by TimeStamp desc limit 1 ", null);
            if (cursor != null && cursor.moveToFirst()) {
                jankBdItem = extractItem(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return jankBdItem;
    }

    public static boolean insertItem(SQLiteDatabase db, JankBdItem item) {
        boolean ret = HWFLOW;
        ContentValues values = item.getContentValues(field_Names);
        if (values == null) {
            return HWFLOW;
        }
        if (-1 != db.insert(tbName, null, values)) {
            ret = HWLOGW_E;
            recordCount++;
        } else if (HWFLOW) {
            Log.i(TAG, "insert(" + values.toString() + ") failed");
        }
        return ret;
    }

    public static boolean updateItem(SQLiteDatabase db, int id, JankBdItem item) {
        boolean ret = HWLOGW_E;
        ContentValues values = item.getContentValues(field_Names);
        if (values == null) {
            return HWFLOW;
        }
        try {
            db.update(tbName, values, "id = ?", new String[]{id + AppHibernateCst.INVALID_PKG});
        } catch (Exception e) {
            ret = HWFLOW;
            if (HWFLOW) {
                Log.i(TAG, "update() there catched Exception " + e.toString());
            }
        }
        return ret;
    }

    public static long getCount(SQLiteDatabase db) {
        recordCount = DatabaseUtils.longForQuery(db, "select count(*) from JankBd", null);
        return recordCount;
    }

    public static void dropTb(SQLiteDatabase db) {
        db.execSQL("drop table if exists JankBd" + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
    }

    public static String getOutdate(int days) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(new Date().getTime() - (((long) days) * Utils.DATE_TIME_24HOURS)));
    }

    public static String getCreateSql() {
        return getAddSql("create table if not exists JankBd (id integer primary key autoincrement ", field_Names, field_Types);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion >= 3 && (oldVersion == 1 || oldVersion == 2)) {
            db.execSQL("alter table JankBd rename to _temp_JankBd");
            db.execSQL(getCreateSql());
            db.execSQL("insert into JankBd select id, CaseName, TimeStamp, AppName, '0', Marks, Section1_cnt, Section2_cnt, Section3_cnt, Section4_cnt, Section5_cnt, Section6_cnt, Section7_cnt, Section8_cnt, Section9_cnt, Section10_cnt from _temp_JankBd");
            db.execSQL("drop table _temp_JankBd");
            oldVersion = 3;
        }
        if (oldVersion != newVersion) {
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
