package android.database.sqlite;

import android.database.sqlite.SQLiteDatabaseEx;

public class SQLiteOpenHelperEx {
    public static void setExclusiveConnectionEnabled(SQLiteOpenHelper helper, boolean enabled, SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        helper.setExclusiveConnectionEnabled(enabled, connectionExclusiveHandler);
    }
}
