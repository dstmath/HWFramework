package com.huawei.odmf.core;

import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.utils.SqlUtil;
import java.util.List;

public class StatementsLoader {
    private Statement deleteSQLiteStatement = null;
    private Statement insertSQLiteStatement = null;
    private Statement updateSQLiteStatement = null;

    /* access modifiers changed from: package-private */
    public Statement getInsertStatement(DataBase db, String tableName, List<? extends Attribute> attributes) {
        if (db == null || tableName == null || attributes == null) {
            throw new ODMFIllegalArgumentException("Execute getInsertStatement Failed : The input parameter has null.");
        }
        if (this.insertSQLiteStatement == null) {
            this.insertSQLiteStatement = db.compileStatement(SqlUtil.createSqlInsert(tableName, attributes));
        }
        return this.insertSQLiteStatement;
    }

    /* access modifiers changed from: package-private */
    public Statement getUpdateStatement(DataBase db, String tableName, List<? extends Attribute> attributes) {
        if (db == null || tableName == null || attributes == null) {
            throw new ODMFIllegalArgumentException("Execute getUpdateStatement Failed : The input parameter has null.");
        }
        if (this.updateSQLiteStatement == null) {
            this.updateSQLiteStatement = db.compileStatement(SqlUtil.createSqlUpdate(tableName, attributes, new String[]{DatabaseQueryService.getRowidColumnName()}));
        }
        return this.updateSQLiteStatement;
    }

    /* access modifiers changed from: package-private */
    public Statement getDeleteStatement(DataBase db, String tableName, List<? extends Attribute> attributes) {
        if (db == null || tableName == null || attributes == null) {
            throw new ODMFIllegalArgumentException("Execute getDeleteStatement Failed : The input parameter has null.");
        }
        if (this.deleteSQLiteStatement == null) {
            this.deleteSQLiteStatement = db.compileStatement(SqlUtil.createSqlDelete(tableName, new String[]{DatabaseQueryService.getRowidColumnName()}));
        }
        return this.deleteSQLiteStatement;
    }
}
