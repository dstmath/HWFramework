package ohos.data.rdb.impl;

import ohos.data.rdb.RdbCorruptException;
import ohos.data.rdb.RdbException;
import ohos.data.resultset.SharedBlock;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class QueryStatement extends SqlStatement implements Query {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "QueryStatement");

    QueryStatement(RdbStoreImpl rdbStoreImpl, String str) {
        super(rdbStoreImpl, str, null);
    }

    @Override // ohos.data.rdb.impl.Query
    public PrecompiledStatement beginStepQuery() {
        try {
            return getSession().beginStepQuery(getSql(), isReadOnly());
        } catch (RdbCorruptException e) {
            HiLog.error(LABEL, "database corrupt: %{public}s; beginStepQuery: %{private}s", new Object[]{e.getMessage(), this.getSql()});
            this.getRdbStore().handleCorruption();
            throw e;
        }
    }

    @Override // ohos.data.rdb.impl.Query
    public int step() {
        acquireRef();
        try {
            int executeForStepQuery = getSession().executeForStepQuery(getSql(), getBindArgs());
            releaseRef();
            return executeForStepQuery;
        } catch (RdbCorruptException e) {
            HiLog.error(LABEL, "database corrupt: %{public}s; query: %{private}s", new Object[]{e.getMessage(), getSql()});
            getRdbStore().handleCorruption();
            throw e;
        } catch (RdbException e2) {
            HiLog.error(LABEL, "exception: %{public}s; query: %{private}s", new Object[]{e2.getMessage(), getSql()});
            throw e2;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.impl.Query
    public void endStepQuery(PrecompiledStatement precompiledStatement) {
        try {
            getSession().endStepQuery(precompiledStatement);
        } catch (RdbCorruptException e) {
            HiLog.error(LABEL, "database corrupt: %{public}s; endStepQuery: %{private}s", new Object[]{e.getMessage(), getSql()});
            getRdbStore().handleCorruption();
            throw e;
        }
    }

    @Override // ohos.data.rdb.impl.Query
    public void resetStatement(PrecompiledStatement precompiledStatement) {
        try {
            getSession().resetStatement(precompiledStatement);
        } catch (RdbCorruptException e) {
            HiLog.error(LABEL, "database corrupt: %{public}s; reset: %{private}s", new Object[]{e.getMessage(), getSql()});
            getRdbStore().handleCorruption();
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public int fillBlock(SharedBlock sharedBlock, int i, int i2, boolean z) {
        acquireRef();
        try {
            sharedBlock.acquireRef();
            try {
                int executeForSharedBlock = getSession().executeForSharedBlock(getSql(), getBindArgs(), sharedBlock, i, i2, z, isReadOnly());
                sharedBlock.releaseRef();
                return executeForSharedBlock;
            } catch (RdbCorruptException e) {
                HiLog.error(LABEL, "database corrupt: %{public}s; fillBlock: %{private}s", new Object[]{e.getMessage(), getSql()});
                getRdbStore().handleCorruption();
                throw e;
            } catch (RdbException e2) {
                HiLog.error(LABEL, "exception: %{public}s; query: %{private}s", new Object[]{e2.getMessage(), getSql()});
                throw e2;
            } catch (Throwable th) {
                sharedBlock.releaseRef();
                throw th;
            }
        } finally {
            releaseRef();
        }
    }
}
