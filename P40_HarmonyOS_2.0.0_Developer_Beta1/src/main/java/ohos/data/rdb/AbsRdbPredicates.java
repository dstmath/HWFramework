package ohos.data.rdb;

import ohos.data.AbsPredicates;

public abstract class AbsRdbPredicates extends AbsPredicates {
    private String tableName;

    public AbsRdbPredicates(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("no tableName specified.");
        }
        this.tableName = str;
    }

    public String getTableName() {
        return this.tableName;
    }

    @Override // ohos.data.AbsPredicates
    public void clear() {
        super.clear();
    }
}
