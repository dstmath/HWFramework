package com.huawei.odmf.store;

import android.database.SQLException;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.utils.LOG;
import com.huawei.odmf.utils.StringUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class ODMFSQLiteTableBuilder {
    static final String ABORT = "ABORT";
    static final String BLOB = "BLOB";
    static final String BOOLEAN = "BOOLEAN";
    static final String IGNORE = "IGNORE";
    static final String INTEGER = "INTEGER";
    static final String REAL = "REAL";
    static final String REPLACE = "REPLACE";
    static final String TEXT = "TEXT";
    private ArrayList<Column> sqliteColumns = new ArrayList<>();
    private ArrayList<SQLiteTableConstraint> sqliteTableConstraints = new ArrayList<>();
    private String sqliteTableName;

    @Retention(RetentionPolicy.SOURCE)
    @interface ColumnType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface ConflictAction {
    }

    /* access modifiers changed from: private */
    public interface SQLiteColumnConstraint extends SQLiteConstraint {
    }

    private interface SQLiteConstraint {
        String toSql();
    }

    private interface SQLiteTableConstraint extends SQLiteConstraint {
    }

    ODMFSQLiteTableBuilder() {
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder setSqliteTableName(String str) {
        this.sqliteTableName = str;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder setPrimaryKey(String str, String str2, boolean z) {
        SQLitePrimaryKey sQLitePrimaryKey = new SQLitePrimaryKey(z);
        sQLitePrimaryKey.isAutoIncrement = z;
        Column column = new Column(str, str2);
        column.constraints.add(sQLitePrimaryKey);
        this.sqliteColumns.add(column);
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder addColumn(String str, String str2) {
        this.sqliteColumns.add(new Column(str, str2));
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder setNullable(boolean z) {
        getLastColumn().constraints.add(new SQLiteNullable(z));
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder setDefaultValue(String str) {
        getLastColumn().constraints.add(new SQLiteDefaultValue(str));
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder setUnique(String str) {
        getLastColumn().constraints.add(new SQLiteUnique(new ConflictClause(str)));
        return this;
    }

    /* access modifiers changed from: package-private */
    public ODMFSQLiteTableBuilder primaryKey(List<String> list) {
        this.sqliteTableConstraints.add(new PrimaryKeySQLiteTableConstraint(list));
        return this;
    }

    /* access modifiers changed from: package-private */
    public void createTable(DataBase dataBase) {
        if (this.sqliteTableName != null) {
            String str = "CREATE TABLE " + this.sqliteTableName;
            if (!this.sqliteColumns.isEmpty()) {
                String str2 = str + " (";
                ArrayList arrayList = new ArrayList();
                int size = this.sqliteColumns.size();
                for (int i = 0; i < size; i++) {
                    arrayList.add(this.sqliteColumns.get(i).toSql());
                }
                int size2 = this.sqliteTableConstraints.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    arrayList.add(this.sqliteTableConstraints.get(i2).toSql());
                }
                try {
                    dataBase.execSQL(str2 + StringUtil.join(arrayList, ", ") + ")");
                } catch (SQLException e) {
                    LOG.logE("Execute createTable Failed : A SQLException occurred when createTable");
                    throw new ODMFRuntimeException("Execute createTable Failed : " + e.toString());
                }
            } else {
                throw new IllegalStateException("Execute createTable Failed : No columns specified");
            }
        } else {
            throw new IllegalStateException("Execute createTable Failed : Table name not specified");
        }
    }

    public void alterTableAddColumn(DataBase dataBase) {
        if (this.sqliteTableName != null) {
            try {
                int size = this.sqliteColumns.size();
                for (int i = 0; i < size; i++) {
                    dataBase.execSQL("ALTER TABLE " + this.sqliteTableName + " ADD COLUMN " + this.sqliteColumns.get(i).toSql());
                }
            } catch (SQLException e) {
                LOG.logE("Execute alterTableAddColumn Failed : A SQLException occurred when alterTableAddColumn");
                throw new ODMFRuntimeException("Execute alterTableAddColumn Failed : " + e.toString());
            }
        } else {
            throw new IllegalStateException("Execute alterTableAddColumn Failed : Table name not specified");
        }
    }

    public static void alterTableName(DataBase dataBase, String str, String str2) {
        if (str == null || str2 == null) {
            throw new IllegalStateException("Execute alterTableName Failed : Table name not specified");
        }
        try {
            dataBase.execSQL(String.format(Locale.ENGLISH, "ALTER TABLE %s RENAME TO %s;", str, str2));
        } catch (SQLException e) {
            LOG.logE("Execute alterTableName Failed : A SQLException occurred when alterTableName");
            throw new ODMFRuntimeException("Execute alterTableName Failed : " + e.toString());
        }
    }

    private Column getLastColumn() {
        if (!this.sqliteColumns.isEmpty()) {
            ArrayList<Column> arrayList = this.sqliteColumns;
            return arrayList.get(arrayList.size() - 1);
        }
        throw new IllegalStateException("Execute getLastColumn Failed : No column previously specified");
    }

    /* access modifiers changed from: private */
    public static class Column {
        ArrayList<SQLiteColumnConstraint> constraints = new ArrayList<>();
        private String name;
        private String type;

        Column(String str, String str2) {
            this.name = str;
            this.type = str2;
        }

        /* access modifiers changed from: package-private */
        public String toSql() {
            String str = "'" + this.name + "' " + this.type + " ";
            ArrayList arrayList = new ArrayList();
            int size = this.constraints.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(this.constraints.get(i).toSql());
            }
            return str + StringUtil.join(arrayList, " ");
        }
    }

    private static class ConflictClause {
        String conflictAction;

        ConflictClause(String str) {
            this.conflictAction = str;
        }

        /* access modifiers changed from: package-private */
        public String toSql() {
            return "ON CONFLICT " + this.conflictAction;
        }
    }

    private abstract class ColumnBasedSQLiteTableConstraint implements SQLiteTableConstraint {
        private ColumnBasedSQLiteTableConstraint() {
        }
    }

    private static class SQLitePrimaryKey implements SQLiteColumnConstraint {
        boolean isAutoIncrement;

        SQLitePrimaryKey(boolean z) {
            this.isAutoIncrement = z;
        }

        @Override // com.huawei.odmf.store.ODMFSQLiteTableBuilder.SQLiteConstraint
        public String toSql() {
            if (!this.isAutoIncrement) {
                return "PRIMARY KEY";
            }
            return "PRIMARY KEY AUTOINCREMENT";
        }
    }

    private class PrimaryKeySQLiteTableConstraint extends ColumnBasedSQLiteTableConstraint {
        private final List<String> columns;
        private final String constraintType = "PRIMARY KEY";

        PrimaryKeySQLiteTableConstraint(List<String> list) {
            super();
            this.columns = list;
        }

        @Override // com.huawei.odmf.store.ODMFSQLiteTableBuilder.SQLiteConstraint
        public String toSql() {
            String str = this.constraintType;
            return str + "(" + StringUtil.join(this.columns, ",") + ")";
        }
    }

    private static class SQLiteNullable implements SQLiteColumnConstraint {
        boolean isNullable;

        SQLiteNullable(boolean z) {
            this.isNullable = z;
        }

        @Override // com.huawei.odmf.store.ODMFSQLiteTableBuilder.SQLiteConstraint
        public String toSql() {
            if (this.isNullable) {
                return "";
            }
            return " NOT NULL";
        }
    }

    private static class SQLiteDefaultValue implements SQLiteColumnConstraint {
        String defaultValue;

        SQLiteDefaultValue(String str) {
            this.defaultValue = str;
        }

        @Override // com.huawei.odmf.store.ODMFSQLiteTableBuilder.SQLiteConstraint
        public String toSql() {
            return "DEFAULT '" + this.defaultValue + "'";
        }
    }

    private static class SQLiteUnique implements SQLiteColumnConstraint {
        ConflictClause conflictClause;

        SQLiteUnique(ConflictClause conflictClause2) {
            this.conflictClause = conflictClause2;
        }

        @Override // com.huawei.odmf.store.ODMFSQLiteTableBuilder.SQLiteConstraint
        public String toSql() {
            return "UNIQUE " + this.conflictClause.toSql();
        }
    }
}
