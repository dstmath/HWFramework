package ohos.data.rdb;

import ohos.data.rdb.impl.SqliteDatabaseUtils;

public abstract class RdbOpenCallback {
    public abstract void onCreate(RdbStore rdbStore);

    public void onDowngrade(RdbStore rdbStore, int i, int i2) {
    }

    public void onOpen(RdbStore rdbStore) {
    }

    public abstract void onUpgrade(RdbStore rdbStore, int i, int i2);

    public void onCorruption(RdbStore rdbStore) {
        String path = rdbStore.getPath();
        SqliteDatabaseUtils.renameFile(path, path + "-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-wal", path + "-wal-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-journal", path + "-journal-corrupted");
    }
}
