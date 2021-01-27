package com.huawei.android.database.sqlite;

import android.database.sqlite.SQLiteDatabaseEx;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteOpenHelperEx;

public class SQLiteOpenHelperExEx {

    public interface DatabaseConnectionExclusiveHandlerEx {
        boolean onConnectionExclusive();
    }

    private static final class DatabaseConnectionExclusiveHandlerInner implements SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler {
        DatabaseConnectionExclusiveHandlerEx mDatabaseConnectionExclusiveHandlerEx;

        private DatabaseConnectionExclusiveHandlerInner() {
            this.mDatabaseConnectionExclusiveHandlerEx = null;
        }

        public boolean onConnectionExclusive() {
            DatabaseConnectionExclusiveHandlerEx databaseConnectionExclusiveHandlerEx = this.mDatabaseConnectionExclusiveHandlerEx;
            if (databaseConnectionExclusiveHandlerEx != null) {
                return databaseConnectionExclusiveHandlerEx.onConnectionExclusive();
            }
            return false;
        }
    }

    public static void setExclusiveConnectionEnabled(SQLiteOpenHelper helper, boolean enabled, DatabaseConnectionExclusiveHandlerEx connectionExclusiveHandler) {
        DatabaseConnectionExclusiveHandlerInner handler = new DatabaseConnectionExclusiveHandlerInner();
        handler.mDatabaseConnectionExclusiveHandlerEx = connectionExclusiveHandler;
        SQLiteOpenHelperEx.setExclusiveConnectionEnabled(helper, enabled, handler);
    }
}
