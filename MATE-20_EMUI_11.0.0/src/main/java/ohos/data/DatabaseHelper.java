package ohos.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import ohos.app.Context;
import ohos.data.orm.OrmConfig;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmDatabase;
import ohos.data.orm.OrmMigration;
import ohos.data.orm.impl.OrmContextImpl;
import ohos.data.orm.impl.StoreCoordinator;
import ohos.data.preferences.Preferences;
import ohos.data.preferences.impl.PreferencesController;
import ohos.data.preferences.impl.PreferencesXmlUtils;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.impl.RdbStoreImpl;
import ohos.data.rdb.impl.SqliteDatabaseUtils;
import ohos.data.resultset.ResultSetHook;

public class DatabaseHelper {
    private Context context;

    public DatabaseHelper(Context context2) {
        this.context = context2;
    }

    public RdbStore getRdbStore(StoreConfig storeConfig, int i, RdbOpenCallback rdbOpenCallback) {
        return RdbStoreImpl.open(this.context, storeConfig, i, rdbOpenCallback, null);
    }

    public RdbStore getRdbStore(StoreConfig storeConfig, int i, RdbOpenCallback rdbOpenCallback, ResultSetHook resultSetHook) {
        return RdbStoreImpl.open(this.context, storeConfig, i, rdbOpenCallback, resultSetHook);
    }

    public <T extends OrmDatabase> OrmContext getOrmContext(String str, String str2, Class<T> cls, OrmMigration... ormMigrationArr) {
        return getOrmContext(new OrmConfig.Builder().setAlias(str).setName(str2).build(), cls, ormMigrationArr);
    }

    public <T extends OrmDatabase> OrmContext getOrmContext(OrmConfig ormConfig, Class<T> cls, OrmMigration... ormMigrationArr) {
        StoreCoordinator.getInstance().createOrmStore(this.context, ormConfig, cls, ormMigrationArr);
        return OrmContextImpl.openOrmContext(ormConfig.getAlias());
    }

    public OrmContext getOrmContext(String str) {
        return OrmContextImpl.openOrmContext(str);
    }

    public boolean deleteRdbStore(String str) {
        String path = SqliteDatabaseUtils.getDatabasePath(this.context, str).getPath();
        boolean delete = new File(path).delete();
        boolean delete2 = delete | new File(path + "-journal").delete() | new File(path + "-shm").delete();
        return new File(path + "-wal").delete() | delete2;
    }

    public static int releaseRdbMemory() {
        return RdbStoreImpl.releaseRdbMemory();
    }

    public Preferences getPreferences(String str) {
        return PreferencesController.getInstance().getPreferences(this.context, str);
    }

    public boolean deletePreferences(String str) {
        return PreferencesController.getInstance().deletePreferences(this.context, str);
    }

    public void removePreferencesFromCache(String str) {
        PreferencesController.getInstance().removePreferencesFromCache(this.context, str);
    }

    public boolean movePreferences(Context context2, String str, String str2) {
        return PreferencesController.getInstance().movePreferences(context2, str, this.context, str2);
    }

    public boolean moveDatabase(Context context2, String str, String str2) {
        if (context2 != null) {
            String path = SqliteDatabaseUtils.getDatabasePath(context2, str).getPath();
            String path2 = SqliteDatabaseUtils.getDatabasePath(this.context, str2).getPath();
            try {
                File canonicalFile = new File(path).getCanonicalFile();
                File canonicalFile2 = new File(path2).getCanonicalFile();
                if (!canonicalFile.exists()) {
                    return true;
                }
                deleteRdbStore(str2);
                moveFile(canonicalFile, canonicalFile2);
                File file = new File(canonicalFile.getPath() + "-journal");
                if (file.exists()) {
                    moveFile(file, new File(path2 + "-journal"));
                }
                File file2 = new File(canonicalFile.getPath() + "-wal");
                if (file2.exists()) {
                    moveFile(file2, new File(path2 + "-wal"));
                }
                return canonicalFile.delete() | file.delete() | file2.delete() | new File(canonicalFile + "-shm").delete();
            } catch (IOException unused) {
                throw new IllegalArgumentException("sourceName is invalid");
            }
        } else {
            throw new IllegalArgumentException("sourceContext should not be null");
        }
    }

    private void moveFile(File file, File file2) {
        try {
            Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PreferencesXmlUtils.limitFilePermission(file2);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy sourceFile to targetFile,eMsg:" + e.getMessage());
        }
    }
}
