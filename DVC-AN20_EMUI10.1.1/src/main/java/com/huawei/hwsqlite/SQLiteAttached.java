package com.huawei.hwsqlite;

public final class SQLiteAttached {
    String alias;
    byte[] encryptKey;
    String path;

    SQLiteAttached(String path2, String alias2, byte[] encryptKey2) {
        this.path = path2;
        this.alias = alias2;
        this.encryptKey = encryptKey2;
    }

    /* access modifiers changed from: package-private */
    public boolean isAliasEqual(SQLiteAttached other) {
        if (this == other) {
            return true;
        }
        String str = this.alias;
        if (str != null) {
            return str.equals(other.alias);
        }
        if (other.alias == null) {
            return true;
        }
        return false;
    }
}
