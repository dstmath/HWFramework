package ohos.data.orm;

import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.impl.SqliteDatabaseUtils;

public abstract class OrmDatabase {
    public abstract RdbOpenCallback getHelper();

    public abstract int getVersion();

    public void corruptionHandler(RdbStore rdbStore) {
        String path = rdbStore.getPath();
        SqliteDatabaseUtils.renameFile(path, path + "-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-wal", path + "-wal-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-journal", path + "-journal-corrupted");
    }
}
