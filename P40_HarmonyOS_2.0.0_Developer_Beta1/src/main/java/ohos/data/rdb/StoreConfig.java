package ohos.data.rdb;

import ohos.data.DatabaseFileType;

public class StoreConfig {
    public static final int MAX_ENCRYPT_KEY_SIZE = 1024;
    private DatabaseFileType databaseFileType;
    private byte[] encryptKey;
    private boolean isReadOnly;
    private JournalMode journalMode;
    private String name;
    private StorageMode storageMode;
    private SyncMode syncMode;

    public enum StorageMode {
        MODE_MEMORY(101),
        MODE_DISK(102);
        
        private int value;

        private StorageMode(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum JournalMode {
        MODE_DELETE("DELETE"),
        MODE_TRUNCATE("TRUNCATE"),
        MODE_PERSIST("PERSIST"),
        MODE_MEMORY("MEMORY"),
        MODE_WAL("WAL"),
        MODE_OFF("OFF");
        
        private String value;

        private JournalMode(String str) {
            this.value = str;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum SyncMode {
        MODE_OFF("OFF"),
        MODE_NORMAL("NORMAL"),
        MODE_FULL("FULL"),
        MODE_EXTRA("EXTRA");
        
        private String value;

        private SyncMode(String str) {
            this.value = str;
        }

        public String getValue() {
            return this.value;
        }
    }

    private StoreConfig(Builder builder) {
        this.name = builder.name;
        this.storageMode = builder.storageMode;
        this.journalMode = builder.journalMode;
        this.syncMode = builder.syncMode;
        this.encryptKey = builder.encryptKey;
        this.isReadOnly = builder.isReadOnly;
        this.databaseFileType = builder.databaseFileType;
    }

    public static StoreConfig newDefaultConfig(String str) {
        return new Builder().setName(str).setStorageMode(StorageMode.MODE_DISK).setJournalMode(null).setSyncMode(null).setEncryptKey(null).setReadOnly(false).setDatabaseFileType(DatabaseFileType.NORMAL).build();
    }

    public static StoreConfig newMemoryConfig() {
        return new Builder().setName(null).setStorageMode(StorageMode.MODE_MEMORY).setJournalMode(null).setSyncMode(null).setEncryptKey(null).setReadOnly(false).setDatabaseFileType(null).build();
    }

    public static StoreConfig newReadOnlyConfig(String str) {
        return new Builder().setName(str).setStorageMode(StorageMode.MODE_DISK).setJournalMode(null).setSyncMode(null).setEncryptKey(null).setReadOnly(true).setDatabaseFileType(DatabaseFileType.NORMAL).build();
    }

    public String getName() {
        return this.name;
    }

    public StorageMode getStorageMode() {
        return this.storageMode;
    }

    public JournalMode getJournalMode() {
        return this.journalMode;
    }

    public SyncMode getSyncMode() {
        return this.syncMode;
    }

    public byte[] getEncryptKey() {
        return this.encryptKey;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public DatabaseFileType getDatabaseFileType() {
        return this.databaseFileType;
    }

    public static final class Builder {
        private DatabaseFileType databaseFileType = DatabaseFileType.NORMAL;
        private byte[] encryptKey = null;
        private boolean isReadOnly = false;
        private JournalMode journalMode = null;
        private String name = null;
        private StorageMode storageMode = StorageMode.MODE_DISK;
        private SyncMode syncMode = null;

        public Builder() {
        }

        public Builder(StoreConfig storeConfig) {
            this.name = storeConfig.name;
            this.storageMode = storeConfig.storageMode;
            this.journalMode = storeConfig.journalMode;
            this.syncMode = storeConfig.syncMode;
            this.encryptKey = storeConfig.encryptKey;
            this.isReadOnly = storeConfig.isReadOnly;
            this.databaseFileType = storeConfig.databaseFileType;
        }

        public Builder setName(String str) {
            this.name = str;
            return this;
        }

        public Builder setStorageMode(StorageMode storageMode2) {
            this.storageMode = storageMode2;
            return this;
        }

        public Builder setJournalMode(JournalMode journalMode2) {
            this.journalMode = journalMode2;
            return this;
        }

        public Builder setSyncMode(SyncMode syncMode2) {
            this.syncMode = syncMode2;
            return this;
        }

        public Builder setEncryptKey(byte[] bArr) {
            if (bArr == null || bArr.length <= 1024) {
                this.encryptKey = bArr;
                return this;
            }
            throw new IllegalArgumentException("Encrypt Key size exceeds maximum limit.");
        }

        public Builder setReadOnly(boolean z) {
            this.isReadOnly = z;
            return this;
        }

        public Builder setDatabaseFileType(DatabaseFileType databaseFileType2) {
            this.databaseFileType = databaseFileType2;
            return this;
        }

        public StoreConfig build() {
            return new StoreConfig(this);
        }
    }
}
