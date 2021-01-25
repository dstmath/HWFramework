package ohos.data.rdb.impl;

import java.io.File;
import ohos.data.rdb.StoreConfig;

public class SqliteDatabaseConfig {
    public static final int CREATE_IF_NECESSARY = 4;
    private static final String MEMORY_DB_PATH = ":memory:";
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 2;
    private SqliteEncryptKeyLoader encryptKeyLoader;
    private boolean isMemoryDb;
    private String journalMode;
    private int openFlags;
    private final String path;
    private String syncMode;

    SqliteDatabaseConfig(String str, SqliteEncryptKeyLoader sqliteEncryptKeyLoader, StoreConfig storeConfig) {
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
        this.journalMode = storeConfig.getJournalMode() == null ? SqliteGlobalConfig.getDefaultJournalMode() : storeConfig.getJournalMode().getValue();
        this.syncMode = storeConfig.getSyncMode() == null ? SqliteGlobalConfig.getDefaultSyncMode() : storeConfig.getSyncMode().getValue();
    }

    SqliteDatabaseConfig(SqliteDatabaseConfig sqliteDatabaseConfig) {
        if (sqliteDatabaseConfig != null) {
            this.path = sqliteDatabaseConfig.path;
            this.journalMode = sqliteDatabaseConfig.journalMode;
            this.syncMode = sqliteDatabaseConfig.syncMode;
            this.openFlags = sqliteDatabaseConfig.openFlags;
            this.encryptKeyLoader = new SqliteEncryptKeyLoader(sqliteDatabaseConfig.getEncryptKeyLoader());
            this.isMemoryDb = sqliteDatabaseConfig.isMemoryDb;
            return;
        }
        throw new IllegalArgumentException("other must not be null.");
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
    public int getOpenFlags() {
        return this.openFlags;
    }

    /* access modifiers changed from: package-private */
    public boolean isMemoryDb() {
        return this.isMemoryDb;
    }

    /* access modifiers changed from: package-private */
    public void destroyEncryptKey() {
        SqliteEncryptKeyLoader sqliteEncryptKeyLoader = this.encryptKeyLoader;
        if (sqliteEncryptKeyLoader != null) {
            sqliteEncryptKeyLoader.destroy();
        }
    }
}
