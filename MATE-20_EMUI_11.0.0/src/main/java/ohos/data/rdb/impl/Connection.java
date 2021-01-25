package ohos.data.rdb.impl;

import ohos.data.resultset.SharedBlock;

public interface Connection {
    PrecompiledStatement beginStepQuery(String str);

    void changeEncryptKey(SqliteEncryptKeyLoader sqliteEncryptKeyLoader);

    void close();

    void endStepQuery(PrecompiledStatement precompiledStatement);

    void execute(String str, Object[] objArr);

    int executeForChanges(String str, Object[] objArr);

    long executeForLastInsertRowId(String str, Object[] objArr);

    int executeForSharedBlock(String str, Object[] objArr, SharedBlock sharedBlock, int i, int i2, boolean z);

    int executeForStepQuery(String str, Object[] objArr);

    long executeGetLong(String str, Object[] objArr);

    String executeGetString(String str, Object[] objArr);

    boolean isPrecompiledStatementInCache(String str);

    boolean isWriteConnection();

    SqliteStatementInfo prepare(String str);

    void resetStatement(PrecompiledStatement precompiledStatement);
}
