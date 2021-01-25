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
    public Statement getInsertStatement(DataBase dataBase, String str, List<? extends Attribute> list) {
        if (dataBase == null || str == null || list == null) {
            throw new ODMFIllegalArgumentException("Execute getInsertStatement Failed : The input parameter has null.");
        }
        if (this.insertSQLiteStatement == null) {
            this.insertSQLiteStatement = dataBase.compileStatement(SqlUtil.createSqlInsert(str, list));
        }
        return this.insertSQLiteStatement;
    }

    public void clearInsertStatement() {
        Statement statement = this.insertSQLiteStatement;
        if (statement != null) {
            statement.close();
            this.insertSQLiteStatement = null;
        }
    }

    /* access modifiers changed from: package-private */
    public Statement getUpdateStatement(DataBase dataBase, String str, List<? extends Attribute> list) {
        if (dataBase == null || str == null || list == null) {
            throw new ODMFIllegalArgumentException("Execute getUpdateStatement Failed : The input parameter has null.");
        }
        if (this.updateSQLiteStatement == null) {
            this.updateSQLiteStatement = dataBase.compileStatement(SqlUtil.createSqlUpdate(str, list, new String[]{DatabaseQueryService.getRowidColumnName()}));
        }
        return this.updateSQLiteStatement;
    }

    public void clearUpdateStatement() {
        Statement statement = this.updateSQLiteStatement;
        if (statement != null) {
            statement.close();
            this.updateSQLiteStatement = null;
        }
    }

    /* access modifiers changed from: package-private */
    public Statement getDeleteStatement(DataBase dataBase, String str, List<? extends Attribute> list) {
        if (dataBase == null || str == null || list == null) {
            throw new ODMFIllegalArgumentException("Execute getDeleteStatement Failed : The input parameter has null.");
        }
        if (this.deleteSQLiteStatement == null) {
            this.deleteSQLiteStatement = dataBase.compileStatement(SqlUtil.createSqlDelete(str, new String[]{DatabaseQueryService.getRowidColumnName()}));
        }
        return this.deleteSQLiteStatement;
    }

    public void clearDeleteStatement() {
        Statement statement = this.deleteSQLiteStatement;
        if (statement != null) {
            statement.close();
            this.deleteSQLiteStatement = null;
        }
    }
}
