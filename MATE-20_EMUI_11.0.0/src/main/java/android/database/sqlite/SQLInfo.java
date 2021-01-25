package android.database.sqlite;

import android.telephony.SmsManager;

public class SQLInfo {
    private long primaryKey;
    private String table;

    public SQLInfo(String mTable, long mPrimaryKey) {
        this.table = mTable;
        this.primaryKey = mPrimaryKey;
    }

    public String getTable() {
        return this.table;
    }

    public void setTable(String table2) {
        this.table = table2;
    }

    public long getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(long primaryKey2) {
        this.primaryKey = primaryKey2;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || !getClass().isInstance(object)) {
            return false;
        }
        SQLInfo compare = (SQLInfo) object;
        if (!this.table.equals(compare.table) || this.primaryKey != compare.primaryKey) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.table + SmsManager.REGEX_PREFIX_DELIMITER + this.primaryKey;
    }

    public int hashCode() {
        int i = 1 * 31 * 31;
        String str = this.table;
        return i + (str == null ? 0 : str.hashCode());
    }
}
