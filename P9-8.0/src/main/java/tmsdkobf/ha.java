package tmsdkobf;

import android.database.sqlite.SQLiteDatabase;
import tmsdkobf.jh.a;

public class ha extends jh {
    public static final a ph = new a() {
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS team_tables(team_name TEXT primary key, team_version int not null)");
            mb.n("QQSecureProvider", "onCreate");
            ha.c(sQLiteDatabase);
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS team_tables(team_name TEXT primary key, team_version int not null)");
            ha.f(sQLiteDatabase, i, i2);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS team_tables(team_name TEXT primary key, team_version int not null)");
            if (i2 >= i) {
                ha.c(sQLiteDatabase, i, i2);
            } else {
                ha.f(sQLiteDatabase, i, i2);
            }
        }
    };

    public ha() {
        super("qqsecure.db", 18, ph);
    }

    private static void c(SQLiteDatabase sQLiteDatabase) {
        mb.d("QQSecureProvider", "invoke createPhoneSqliteData");
        d(sQLiteDatabase);
        qm.a(sQLiteDatabase);
        gp.a(sQLiteDatabase);
        jm.a(sQLiteDatabase);
        jr.a(sQLiteDatabase);
        gv.a(sQLiteDatabase);
    }

    private static void c(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        mb.d("QQSecureProvider", "invoke upgradePhoneSqliteData");
        d(sQLiteDatabase, i, i2);
        d(sQLiteDatabase);
        e(sQLiteDatabase, i, i2);
        qm.a(sQLiteDatabase, i, i2);
        gp.a(sQLiteDatabase, i, i2);
        jm.a(sQLiteDatabase, i, i2);
        jr.a(sQLiteDatabase, i, i2);
        gv.a(sQLiteDatabase, i, i2);
    }

    private static void d(SQLiteDatabase sQLiteDatabase) {
        mb.n("QQSecureProvider", "createNetwork CREATE TABLE IF NOT EXISTS operator_data_sync_result (id INTEGER PRIMARY KEY,type INTEGER,error_code INTEGER,timestamp INTEGER,area_code TEXT,sim_type TEXT,query_code TEXT,sms TEXT,trigger_type INTEGER,total_setting INTEGER,used_setting INTEGER,fix_template_type INTEGER,value_old INTEGER,value_new INTEGER,software_version TEXT,addtion TEXT)");
        mb.n("QQSecureProvider", "createNetwork CREATE TABLE IF NOT EXISTS network_shark_save (id INTEGER PRIMARY KEY,com INTEGER,str TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS network_filter (uid INTEGER,filter_ip TEXT,pkg_name TEXT,app_name TEXT,is_allow_network BOOLEAN,is_allow_network_wifi BOOLEAN,is_sys_app BOOLEAN)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS networK (id INTEGER PRIMARY KEY,date LONG,data LONG,type INTEGER,imsi TEXT,flag TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS operator_data_sync_result (id INTEGER PRIMARY KEY,type INTEGER,error_code INTEGER,timestamp INTEGER,area_code TEXT,sim_type TEXT,query_code TEXT,sms TEXT,trigger_type INTEGER,total_setting INTEGER,used_setting INTEGER,fix_template_type INTEGER,value_old INTEGER,value_new INTEGER,software_version TEXT,addtion TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS network_shark_save (id INTEGER PRIMARY KEY,com INTEGER,str TEXT)");
    }

    private static void d(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        mb.n("QQSecureProvider", "^^ upgradeNetworkFilter oldVersion " + i);
        if (i < 8) {
            mb.n("QQSecureProvider", "^^ upgradeNetworkFilter newVersion " + i2);
            String str = "ALTER TABLE network_filter ADD COLUMN filter_ip TEXT";
            String str2 = "ALTER TABLE network_filter ADD COLUMN is_allow_network_wifi BOOLEAN";
            String str3 = "UPDATE network_filter SET is_allow_network_wifi = 1";
            mb.d("QQSecureProvider", "when TB_NETWORK_FILTER, alter: " + str);
            mb.d("QQSecureProvider", "when TB_NETWORK_FILTER, alter: " + str2);
            mb.d("QQSecureProvider", "when TB_NETWORK_FILTER, update: " + str3);
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
        mb.n("QQSecureProvider", "^^ upgradeTrafficReport oldVersion " + i);
        if (i < 8) {
            mb.n("QQSecureProvider", "^^ upgradeTrafficReport newVersion " + i2);
            String str = "ALTER TABLE operator_data_sync_result ADD COLUMN addtion TEXT";
            mb.d("QQSecureProvider", "when upgradeTrafficReport, alter: " + str);
            sQLiteDatabase.execSQL(str);
        }
    }

    private static void f(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        mb.d("QQSecureProvider", "invoke downgradePhoneSqliteData");
        e(sQLiteDatabase);
        d(sQLiteDatabase);
        qm.b(sQLiteDatabase, i, i2);
        gp.b(sQLiteDatabase, i, i2);
        jm.b(sQLiteDatabase, i, i2);
        jr.b(sQLiteDatabase, i, i2);
        gv.b(sQLiteDatabase, i, i2);
    }
}
