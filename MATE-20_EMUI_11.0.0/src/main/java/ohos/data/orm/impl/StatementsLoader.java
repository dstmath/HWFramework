package ohos.data.orm.impl;

import ohos.data.orm.EntityHelper;
import ohos.data.orm.OrmObject;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.Statement;

public class StatementsLoader {
    private Statement deleteSqliteStatement = null;
    private Statement insertSqliteStatement = null;
    private final Object statementLock = new Object();
    private Statement updateSqliteStatement = null;

    /* access modifiers changed from: package-private */
    public <T extends OrmObject> Statement getInsertStatement(RdbStore rdbStore, EntityHelper<T> entityHelper) {
        Statement statement;
        if (rdbStore == null || entityHelper == null) {
            throw new IllegalArgumentException("Execute getInsertStatement Failed : The input parameter has null.");
        } else if (entityHelper.getInsertStatement() == null || "".equals(entityHelper.getInsertStatement())) {
            throw new IllegalStateException("Execute getInsertStatement Failed : The insert statement in helper is null.");
        } else {
            synchronized (this.statementLock) {
                if (this.insertSqliteStatement == null) {
                    this.insertSqliteStatement = rdbStore.buildStatement(entityHelper.getInsertStatement());
                }
                statement = this.insertSqliteStatement;
            }
            return statement;
        }
    }

    /* access modifiers changed from: package-private */
    public <T extends OrmObject> Statement getUpdateStatement(RdbStore rdbStore, EntityHelper<T> entityHelper) {
        Statement statement;
        if (rdbStore == null || entityHelper == null) {
            throw new IllegalArgumentException("Execute getUpdateStatement Failed : The input parameter has null.");
        } else if (entityHelper.getUpdateStatement() == null || "".equals(entityHelper.getUpdateStatement())) {
            throw new IllegalStateException("Execute getUpdateStatement Failed : The update statement in helper is null.");
        } else {
            synchronized (this.statementLock) {
                if (this.updateSqliteStatement == null) {
                    this.updateSqliteStatement = rdbStore.buildStatement(entityHelper.getUpdateStatement());
                }
                statement = this.updateSqliteStatement;
            }
            return statement;
        }
    }

    /* access modifiers changed from: package-private */
    public <T extends OrmObject> Statement getDeleteStatement(RdbStore rdbStore, EntityHelper<T> entityHelper) {
        Statement statement;
        if (rdbStore == null || entityHelper == null) {
            throw new IllegalArgumentException("Execute getDeleteStatement Failed : The input parameter has null.");
        } else if (entityHelper.getDeleteStatement() == null || "".equals(entityHelper.getDeleteStatement())) {
            throw new IllegalStateException("Execute getDeleteStatement Failed : The delete statement in helper is null.");
        } else {
            synchronized (this.statementLock) {
                if (this.deleteSqliteStatement == null) {
                    this.deleteSqliteStatement = rdbStore.buildStatement(entityHelper.getDeleteStatement());
                }
                statement = this.deleteSqliteStatement;
            }
            return statement;
        }
    }
}
