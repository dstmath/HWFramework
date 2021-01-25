package ohos.data.rdb;

import java.util.List;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.ResultSetHook;
import ohos.utils.Pair;

public interface RdbStore {
    public static final int MAX_BATCH_INSERT_SIZE = 1000;

    boolean backup(String str);

    boolean backup(String str, byte[] bArr);

    List<Long> batchInsertOrThrowException(String str, List<ValuesBucket> list, ConflictResolution conflictResolution);

    void beginTransaction();

    void beginTransactionWithObserver(TransactionObserver transactionObserver);

    Statement buildStatement(String str);

    void changeEncryptKey(byte[] bArr);

    void close();

    long count(String str, String str2, String[] strArr);

    long count(RdbPredicates rdbPredicates);

    int delete(RdbPredicates rdbPredicates);

    void endTransaction();

    void executeSql(String str);

    void executeSql(String str, Object[] objArr);

    String getPath();

    int getVersion();

    void giveConnectionTemporarily(long j);

    long insert(String str, ValuesBucket valuesBucket);

    long insertOrThrowException(String str, ValuesBucket valuesBucket);

    long insertWithConflictResolution(String str, ValuesBucket valuesBucket, ConflictResolution conflictResolution);

    boolean isHoldingConnection();

    boolean isInTransaction();

    boolean isMemoryRdb();

    boolean isOpen();

    boolean isReadOnly();

    List<Pair<String, String>> listAttached();

    void markAsCommit();

    ResultSet query(RdbPredicates rdbPredicates, String[] strArr);

    ResultSet queryByStep(RdbPredicates rdbPredicates, String[] strArr);

    ResultSet querySql(String str, String[] strArr);

    ResultSet querySqlByStep(String str, String[] strArr);

    ResultSet querySqlWithHook(String str, String[] strArr, ResultSetHook resultSetHook);

    ResultSet queryWithHook(RdbPredicates rdbPredicates, String[] strArr, ResultSetHook resultSetHook);

    long replace(String str, ValuesBucket valuesBucket);

    long replaceOrThrowException(String str, ValuesBucket valuesBucket);

    boolean restore(String str);

    boolean restore(String str, byte[] bArr, byte[] bArr2);

    void setVersion(int i);

    @Override // ohos.data.rdb.RdbStore
    String toString();

    int update(ValuesBucket valuesBucket, RdbPredicates rdbPredicates);

    int updateWithConflictResolution(ValuesBucket valuesBucket, RdbPredicates rdbPredicates, ConflictResolution conflictResolution);

    public enum ConflictResolution {
        ON_CONFLICT_NONE(0),
        ON_CONFLICT_ROLLBACK(1),
        ON_CONFLICT_ABORT(2),
        ON_CONFLICT_FAIL(3),
        ON_CONFLICT_IGNORE(4),
        ON_CONFLICT_REPLACE(5);
        
        private int value;

        private ConflictResolution(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }
}
