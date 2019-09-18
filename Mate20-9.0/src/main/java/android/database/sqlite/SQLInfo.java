package android.database.sqlite;

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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().isInstance(o)) {
            return false;
        }
        SQLInfo compare = (SQLInfo) o;
        if (!this.table.equals(compare.table) || this.primaryKey != compare.primaryKey) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.table + "," + this.primaryKey;
    }

    public int hashCode() {
        return (31 * 1 * 31) + (this.table == null ? 0 : this.table.hashCode());
    }
}
