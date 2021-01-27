package ohos.data.rdb;

import java.io.File;
import ohos.data.rdb.impl.SqliteDatabaseUtils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class RdbOpenCallback {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "RdbOpenCallback");

    public abstract void onCreate(RdbStore rdbStore);

    public void onDowngrade(RdbStore rdbStore, int i, int i2) {
    }

    public void onOpen(RdbStore rdbStore) {
    }

    public abstract void onUpgrade(RdbStore rdbStore, int i, int i2);

    public void onCorruption(File file) {
        HiLog.error(LABEL, "Called default method on corruption.", new Object[0]);
        String path = file.getPath();
        String path2 = SqliteDatabaseUtils.getCorruptPath(path).getPath();
        SqliteDatabaseUtils.renameFile(path, path2 + "-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-wal", path2 + "-wal-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-journal", path2 + "-journal-corrupted");
    }
}
