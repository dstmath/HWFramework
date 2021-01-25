package ohos.data.orm;

import ohos.data.rdb.RdbStore;

public abstract class OrmMigration {
    private final int beginVersion;
    private final int endVersion;

    public abstract void onMigrate(RdbStore rdbStore);

    public OrmMigration(int i, int i2) {
        this.beginVersion = i;
        this.endVersion = i2;
    }

    public int getBeginVersion() {
        return this.beginVersion;
    }

    public int getEndVersion() {
        return this.endVersion;
    }
}
