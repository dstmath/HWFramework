package ohos.data.rdb.impl;

import ohos.data.rdb.RdbCorruptException;
import ohos.data.rdb.Statement;

public class GeneralStatement extends SqlStatement implements Statement {
    GeneralStatement(RdbStoreImpl rdbStoreImpl, String str, Object[] objArr) {
        super(rdbStoreImpl, str, objArr);
    }

    @Override // ohos.data.rdb.Statement
    public void execute() {
        acquireRef();
        try {
            getSession().execute(getSql(), getBindArgs(), isReadOnly());
            releaseRef();
        } catch (RdbCorruptException e) {
            getRdbStore().handleCorruption();
            throw e;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.Statement
    public int executeAndGetChanges() {
        acquireRef();
        try {
            int executeForChanges = getSession().executeForChanges(getSql(), getBindArgs(), isReadOnly());
            releaseRef();
            return executeForChanges;
        } catch (RdbCorruptException e) {
            getRdbStore().handleCorruption();
            throw e;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.Statement
    public long executeAndGetLastInsertRowId() {
        acquireRef();
        try {
            long executeForLastInsertRowId = getSession().executeForLastInsertRowId(getSql(), getBindArgs(), isReadOnly());
            releaseRef();
            return executeForLastInsertRowId;
        } catch (RdbCorruptException e) {
            getRdbStore().handleCorruption();
            throw e;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.Statement
    public long executeAndGetLong() {
        acquireRef();
        try {
            long executeGetLong = getSession().executeGetLong(getSql(), getBindArgs(), isReadOnly());
            releaseRef();
            return executeGetLong;
        } catch (RdbCorruptException e) {
            getRdbStore().handleCorruption();
            throw e;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.Statement
    public String executeAndGetString() {
        acquireRef();
        try {
            String executeGetString = getSession().executeGetString(getSql(), getBindArgs(), isReadOnly());
            releaseRef();
            return executeGetString;
        } catch (RdbCorruptException e) {
            getRdbStore().handleCorruption();
            throw e;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }
}
