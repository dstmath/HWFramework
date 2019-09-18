package com.huawei.hwsqlite;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SQLiteDatabaseConfiguration {
    private static final Pattern EMAIL_IN_DB_PATTERN = Pattern.compile("[\\w\\.\\-]+@[\\w\\.\\-]+");
    public static final String MEMORY_DB_PATH = ":memory:";
    public final ArrayList<SQLiteAttached> attachedAlias = new ArrayList<>();
    public final ArrayList<SQLiteCustomFunction> customFunctions = new ArrayList<>();
    public SQLiteEncryptKeyLoader encryptKeyLoader = null;
    public boolean foreignKeyConstraintsEnabled;
    public final String label;
    public Locale locale;
    public int maxConnectionCount;
    public int maxSqlCacheSize;
    public int openFlags;
    public final String path;

    public SQLiteDatabaseConfiguration(String path2, int openFlags2) {
        if (path2 != null) {
            this.path = path2;
            this.label = stripPathForLogs(path2);
            this.openFlags = openFlags2;
            this.maxSqlCacheSize = 25;
            this.maxConnectionCount = 0;
            this.locale = Locale.getDefault();
            return;
        }
        throw new IllegalArgumentException("path must not be null.");
    }

    public SQLiteDatabaseConfiguration(SQLiteDatabaseConfiguration other) {
        if (other != null) {
            this.path = other.path;
            this.label = other.label;
            updateParametersFrom(other);
            return;
        }
        throw new IllegalArgumentException("other must not be null.");
    }

    public void updateParametersFrom(SQLiteDatabaseConfiguration other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null.");
        } else if (this.path.equals(other.path)) {
            this.openFlags = other.openFlags;
            this.maxSqlCacheSize = other.maxSqlCacheSize;
            this.maxConnectionCount = other.maxConnectionCount;
            this.locale = other.locale;
            this.foreignKeyConstraintsEnabled = other.foreignKeyConstraintsEnabled;
            this.customFunctions.clear();
            this.customFunctions.addAll(other.customFunctions);
            this.encryptKeyLoader = other.encryptKeyLoader;
            this.attachedAlias.clear();
            this.attachedAlias.addAll(other.attachedAlias);
        } else {
            throw new IllegalArgumentException("other configuration must refer to the same database.");
        }
    }

    public boolean isInMemoryDb() {
        return this.path.equalsIgnoreCase(MEMORY_DB_PATH);
    }

    public byte[] getEncryptKey() {
        if (this.encryptKeyLoader == null) {
            return new byte[0];
        }
        return this.encryptKeyLoader.getEncryptKey();
    }

    public void updateEncryptKeyLoader(SQLiteEncryptKeyLoader newLoader) {
        this.encryptKeyLoader = newLoader;
    }

    public boolean addAttachAlias(SQLiteAttached attached) {
        if (isAttachAliasExists(attached.alias)) {
            return false;
        }
        this.attachedAlias.add(attached);
        return true;
    }

    public boolean removeAttachAlias(String alias) {
        SQLiteAttached attached = findAttachedAlias(alias);
        if (attached == null) {
            return false;
        }
        this.attachedAlias.remove(attached);
        return true;
    }

    public boolean isAttachAliasExists(String alias) {
        return findAttachedAlias(alias) != null;
    }

    private static String stripPathForLogs(String path2) {
        if (path2.indexOf(64) == -1) {
            return path2;
        }
        return EMAIL_IN_DB_PATTERN.matcher(path2).replaceAll("XX@YY");
    }

    private SQLiteAttached findAttachedAlias(String alias) {
        if (alias != null) {
            int length = this.attachedAlias.size();
            for (int i = 0; i < length; i++) {
                SQLiteAttached orig = this.attachedAlias.get(i);
                if (alias.equals(orig.alias)) {
                    return orig;
                }
            }
        }
        return null;
    }
}
