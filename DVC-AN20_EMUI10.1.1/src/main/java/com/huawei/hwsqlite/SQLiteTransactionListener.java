package com.huawei.hwsqlite;

public interface SQLiteTransactionListener {
    void onBegin();

    void onCommit();

    void onRollback();
}
