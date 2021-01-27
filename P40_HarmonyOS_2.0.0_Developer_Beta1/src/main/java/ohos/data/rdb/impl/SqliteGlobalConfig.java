package ohos.data.rdb.impl;

public final class SqliteGlobalConfig {
    private static final String DEFAULT_JOURNAL_MODE = "WAL";
    private static final String DEFAULT_SYNC_MODE = "FULL";
    private static final int JOURNAL_FILE_SIZE = 524288;
    private static final int MAX_CONNECTION_POOL_SIZE = 4;
    private static final int MEMORY_CONNECTION_POOL_SIZE = 1;
    private static final int PAGE_SIZE = 4096;
    private static final int WAL_AUTO_CHECKPOINT = 100;

    public static String getDefaultJournalMode() {
        return DEFAULT_JOURNAL_MODE;
    }

    public static String getDefaultSyncMode() {
        return DEFAULT_SYNC_MODE;
    }

    public static int getJournalFileSize() {
        return 524288;
    }

    public static int getMaxConnectionPoolSize() {
        return 4;
    }

    public static int getMemoryConnectionPoolSize() {
        return 1;
    }

    public static int getPageSize() {
        return 4096;
    }

    public static int getWalAutoCheckpoint() {
        return 100;
    }

    private static native int nativeReleaseMemory();

    private SqliteGlobalConfig() {
    }

    public static int releaseRdbMemory() {
        return nativeReleaseMemory();
    }
}
