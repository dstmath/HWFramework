package ohos.data.rdb;

public interface TransactionObserver {
    void onBegin();

    void onCommit();

    void onRollback();
}
