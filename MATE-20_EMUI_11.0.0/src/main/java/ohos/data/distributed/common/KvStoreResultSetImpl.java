package ohos.data.distributed.common;

public class KvStoreResultSetImpl implements KvStoreResultSet {
    private long nativeResultSet = 0;

    private native int nativeGetCount(long j);

    private native Entry nativeGetEntry(long j);

    private native int nativeGetPosition(long j);

    private native boolean nativeIsAfterLast(long j);

    private native boolean nativeIsBeforeFirst(long j);

    private native boolean nativeIsFirst(long j);

    private native boolean nativeIsLast(long j);

    private native boolean nativeMove(long j, int i);

    private native boolean nativeMoveToFirst(long j);

    private native boolean nativeMoveToLast(long j);

    private native boolean nativeMoveToNext(long j);

    private native boolean nativeMoveToPosition(long j, int i);

    private native boolean nativeMoveToPrevious(long j);

    public KvStoreResultSetImpl(long j) {
        this.nativeResultSet = j;
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public int getRowCount() {
        return nativeGetCount(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public int getRowIndex() {
        return nativeGetPosition(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean goToFirstRow() {
        return nativeMoveToFirst(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean goToLastRow() {
        return nativeMoveToLast(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean goToNextRow() {
        return nativeMoveToNext(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean goToPreviousRow() {
        return nativeMoveToPrevious(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean skipRow(int i) {
        return nativeMove(this.nativeResultSet, i);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean goToRow(int i) {
        return nativeMoveToPosition(this.nativeResultSet, i);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean isAtFirstRow() {
        return nativeIsFirst(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean isAtLastRow() {
        return nativeIsLast(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean isStarted() {
        return nativeIsBeforeFirst(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public boolean isEnded() {
        return nativeIsAfterLast(this.nativeResultSet);
    }

    @Override // ohos.data.distributed.common.KvStoreResultSet
    public Entry getEntry() {
        return nativeGetEntry(this.nativeResultSet);
    }
}
