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

    public void setTable(String table) {
        this.table = table;
    }

    public long getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(long primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().isInstance(o)) {
            return false;
        }
        SQLInfo compare = (SQLInfo) o;
        return this.table.equals(compare.table) && this.primaryKey == compare.primaryKey;
    }

    public String toString() {
        return this.table + "," + this.primaryKey;
    }

    public int hashCode() {
        return (this.table == null ? 0 : this.table.hashCode()) + 961;
    }
}
