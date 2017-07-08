package android.database.sqlite;

import android.database.sqlite.SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler;

public class SQLiteOpenHelperEx {
    public static void setExclusiveConnectionEnabled(SQLiteOpenHelper helper, boolean enabled, DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        helper.setExclusiveConnectionEnabled(enabled, connectionExclusiveHandler);
    }
}
