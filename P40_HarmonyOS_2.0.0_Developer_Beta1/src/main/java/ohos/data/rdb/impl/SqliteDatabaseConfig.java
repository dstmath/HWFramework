package ohos.data.rdb.impl;

import java.io.File;
import java.util.Locale;
import ohos.app.Context;
import ohos.data.DatabaseFileConfig;
import ohos.data.DatabaseFileType;
import ohos.data.rdb.StoreConfig;

public class SqliteDatabaseConfig {
    public static final int CREATE_IF_NECESSARY = 4;
    public static final String MEMORY_DB_PATH = ":memory:";
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 2;
    private DatabaseFileType databaseFileType;
    private SqliteEncryptKeyLoader encryptKeyLoader;
    private boolean isMemoryDb;
    private String journalMode;
    private Locale locale;
    private int openFlags;
    private String path;
    private String syncMode;

    private SqliteDatabaseConfig(String str, SqliteEncryptKeyLoader sqliteEncryptKeyLoader, StoreConfig storeConfig) {
        if (str == null) {
            this.path = MEMORY_DB_PATH;
            this.isMemoryDb = true;
        } else {
            this.path = str;
            this.isMemoryDb = false;
        }
        this.encryptKeyLoader = sqliteEncryptKeyLoader;
        if (storeConfig.isReadOnly()) {
            this.openFlags = 1;
        } else {
            this.openFlags = 6;
        }
        this.databaseFileType = storeConfig.getDatabaseFileType();
        this.journalMode = storeConfig.getJournalMode() == null ? SqliteGlobalConfig.getDefaultJournalMode() : storeConfig.getJournalMode().getValue();
        this.syncMode = storeConfig.getSyncMode() == null ? SqliteGlobalConfig.getDefaultSyncMode() : storeConfig.getSyncMode().getValue();
        this.locale = Locale.getDefault();
    }

    SqliteDatabaseConfig(SqliteDatabaseConfig sqliteDatabaseConfig) {
        if (sqliteDatabaseConfig != null) {
            this.path = sqliteDatabaseConfig.path;
            this.journalMode = sqliteDatabaseConfig.journalMode;
            this.syncMode = sqliteDatabaseConfig.syncMode;
            this.openFlags = sqliteDatabaseConfig.openFlags;
            this.encryptKeyLoader = new SqliteEncryptKeyLoader(sqliteDatabaseConfig.getEncryptKeyLoader());
            this.databaseFileType = sqliteDatabaseConfig.databaseFileType;
            this.isMemoryDb = sqliteDatabaseConfig.isMemoryDb;
            this.locale = sqliteDatabaseConfig.locale;
            return;
        }
        throw new IllegalArgumentException("other must not be null.");
    }

    public static SqliteDatabaseConfig create(Context context, StoreConfig storeConfig) {
        SqliteEncryptKeyLoader generate = SqliteEncryptKeyLoader.generate(context, storeConfig.getEncryptKey());
        return new SqliteDatabaseConfig(storeConfig.getStorageMode() != StoreConfig.StorageMode.MODE_MEMORY ? SqliteDatabaseUtils.getDatabasePath(context, new DatabaseFileConfig.Builder().setName(storeConfig.getName()).setEncrypted(!generate.isEmpty()).setDatabaseFileType(storeConfig.getDatabaseFileType()).build()).getPath() : null, generate, storeConfig);
    }

    public String getName() {
        return this.isMemoryDb ? MEMORY_DB_PATH : new File(this.path).getName();
    }

    public String getPath() {
        return this.path;
    }

    /* access modifiers changed from: package-private */
    public String getJournalMode() {
        return this.journalMode;
    }

    /* access modifiers changed from: package-private */
    public String getSyncMode() {
        return this.syncMode;
    }

    /* access modifiers changed from: package-private */
    public SqliteEncryptKeyLoader getEncryptKeyLoader() {
        return this.encryptKeyLoader;
    }

    /* access modifiers changed from: package-private */
    public void setEncryptKeyLoader(SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        destroyEncryptKey();
        this.encryptKeyLoader = sqliteEncryptKeyLoader;
    }

    /* access modifiers changed from: package-private */
    public void setPath(String str) {
        this.path = str;
    }

    /* access modifiers changed from: package-private */
    public int getOpenFlags() {
        return this.openFlags;
    }

    /* access modifiers changed from: package-private */
    public boolean isMemoryDb() {
        return this.isMemoryDb;
    }

    /* access modifiers changed from: package-private */
    public DatabaseFileType getDatabaseFileType() {
        return this.databaseFileType;
    }

    /* access modifiers changed from: package-private */
    public void destroyEncryptKey() {
        SqliteEncryptKeyLoader sqliteEncryptKeyLoader = this.encryptKeyLoader;
        if (sqliteEncryptKeyLoader != null) {
            sqliteEncryptKeyLoader.destroy();
        }
    }

    /* access modifiers changed from: package-private */
    public Locale getLocale() {
        return this.locale;
    }

    /* access modifiers changed from: package-private */
    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }
}
