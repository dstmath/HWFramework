package tmsdkobf;

import android.database.sqlite.SQLiteDatabase;

public class gv {
    private static Object lock = new Object();

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shark_data_base (a INTEGER PRIMARY KEY,b INTEGER,d INTEGER,c BLOB)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 15) {
            a(sQLiteDatabase);
        }
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS shark_data_base");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }
}
