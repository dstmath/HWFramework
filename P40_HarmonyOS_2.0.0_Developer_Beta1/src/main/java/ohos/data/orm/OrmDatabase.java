package ohos.data.orm;

import java.io.File;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.impl.SqliteDatabaseUtils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class OrmDatabase {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "OrmDatabase");

    public abstract RdbOpenCallback getHelper();

    public abstract int getVersion();

    public void corruptionHandler(File file) {
        HiLog.error(LABEL, "Called default method on corruption.", new Object[0]);
        String path = file.getPath();
        String path2 = SqliteDatabaseUtils.getCorruptPath(path).getPath();
        SqliteDatabaseUtils.renameFile(path, path2 + "-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-wal", path2 + "-wal-corrupted");
        SqliteDatabaseUtils.renameFile(path + "-journal", path2 + "-journal-corrupted");
    }
}
