package tmsdkobf;

import android.database.sqlite.SQLiteDatabase;
import tmsdk.common.utils.d;
import tmsdkobf.ko.a;

/* compiled from: Unknown */
public class id extends ko {
    public static final a rE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.id.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.id.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.id.<clinit>():void");
    }

    public id() {
        super("qqsecure.db", 18, rE);
    }

    private static void c(SQLiteDatabase sQLiteDatabase) {
        d.e("QQSecureProvider", "invoke createPhoneSqliteData");
        d(sQLiteDatabase);
        gf.a(sQLiteDatabase);
        hs.a(sQLiteDatabase);
        kt.a(sQLiteDatabase);
        ky.a(sQLiteDatabase);
        hy.a(sQLiteDatabase);
    }

    private static void c(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.e("QQSecureProvider", "invoke upgradePhoneSqliteData");
        d(sQLiteDatabase, i, i2);
        d(sQLiteDatabase);
        e(sQLiteDatabase, i, i2);
        gf.a(sQLiteDatabase, i, i2);
        hs.a(sQLiteDatabase, i, i2);
        kt.a(sQLiteDatabase, i, i2);
        ky.a(sQLiteDatabase, i, i2);
        hy.a(sQLiteDatabase, i, i2);
    }

    private static void d(SQLiteDatabase sQLiteDatabase) {
        d.d("QQSecureProvider", "createNetwork CREATE TABLE IF NOT EXISTS operator_data_sync_result (id INTEGER PRIMARY KEY,type INTEGER,error_code INTEGER,timestamp INTEGER,area_code TEXT,sim_type TEXT,query_code TEXT,sms TEXT,trigger_type INTEGER,total_setting INTEGER,used_setting INTEGER,fix_template_type INTEGER,value_old INTEGER,value_new INTEGER,software_version TEXT,addtion TEXT)");
        d.d("QQSecureProvider", "createNetwork CREATE TABLE IF NOT EXISTS network_shark_save (id INTEGER PRIMARY KEY,com INTEGER,str TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS network_filter (uid INTEGER,filter_ip TEXT,pkg_name TEXT,app_name TEXT,is_allow_network BOOLEAN,is_allow_network_wifi BOOLEAN,is_sys_app BOOLEAN)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS networK (id INTEGER PRIMARY KEY,date LONG,data LONG,type INTEGER,imsi TEXT,flag TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS operator_data_sync_result (id INTEGER PRIMARY KEY,type INTEGER,error_code INTEGER,timestamp INTEGER,area_code TEXT,sim_type TEXT,query_code TEXT,sms TEXT,trigger_type INTEGER,total_setting INTEGER,used_setting INTEGER,fix_template_type INTEGER,value_old INTEGER,value_new INTEGER,software_version TEXT,addtion TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS network_shark_save (id INTEGER PRIMARY KEY,com INTEGER,str TEXT)");
    }

    private static void d(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("QQSecureProvider", "^^ upgradeNetworkFilter oldVersion " + i);
        if (i < 8) {
            d.d("QQSecureProvider", "^^ upgradeNetworkFilter newVersion " + i2);
            String str = "ALTER TABLE network_filter ADD COLUMN filter_ip TEXT";
            String str2 = "ALTER TABLE network_filter ADD COLUMN is_allow_network_wifi BOOLEAN";
            String str3 = "UPDATE network_filter SET is_allow_network_wifi = 1";
            d.e("QQSecureProvider", "when TB_NETWORK_FILTER, alter: " + str);
            d.e("QQSecureProvider", "when TB_NETWORK_FILTER, alter: " + str2);
            d.e("QQSecureProvider", "when TB_NETWORK_FILTER, update: " + str3);
            sQLiteDatabase.execSQL(str);
            sQLiteDatabase.execSQL(str2);
            sQLiteDatabase.execSQL(str3);
        }
    }

    private static void e(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS network_filter");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS networK");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS operator_data_sync_result");
    }

    private static void e(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("QQSecureProvider", "^^ upgradeTrafficReport oldVersion " + i);
        if (i < 8) {
            d.d("QQSecureProvider", "^^ upgradeTrafficReport newVersion " + i2);
            String str = "ALTER TABLE operator_data_sync_result ADD COLUMN addtion TEXT";
            d.e("QQSecureProvider", "when upgradeTrafficReport, alter: " + str);
            sQLiteDatabase.execSQL(str);
        }
    }

    private static void f(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.e("QQSecureProvider", "invoke downgradePhoneSqliteData");
        e(sQLiteDatabase);
        d(sQLiteDatabase);
        gf.b(sQLiteDatabase, i, i2);
        hs.b(sQLiteDatabase, i, i2);
        kt.b(sQLiteDatabase, i, i2);
        ky.b(sQLiteDatabase, i, i2);
        hy.b(sQLiteDatabase, i, i2);
    }
}
