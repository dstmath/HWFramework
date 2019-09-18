package com.huawei.odmf.core;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;

public class Configuration {
    public static final int CONFIGURATION_DATABASE_ANDROID = 302;
    public static final int CONFIGURATION_DATABASE_ODMF = 301;
    public static final int CONFIGURATION_STORAGE_MODE_DISK = 402;
    public static final int CONFIGURATION_STORAGE_MODE_MEMORY = 401;
    public static final int CONFIGURATION_TYPE_LOCAL = 200;
    public static final int CONFIGURATION_TYPE_PROVIDER = 201;
    private int databaseType;
    private boolean detectDelete;
    private String path;
    private int storageMode;
    private boolean throwException;
    private int type;

    public Configuration(String path2, int type2, int databaseType2, int storageMode2) {
        configurationImpl(path2, type2, databaseType2, storageMode2);
    }

    public Configuration() {
        configurationImpl(null, 200, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String path2) {
        configurationImpl(path2, 200, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String path2, int type2) {
        configurationImpl(path2, type2, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String path2, int type2, int databaseType2) {
        configurationImpl(path2, type2, databaseType2, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(int storageMode2) {
        configurationImpl(null, 200, CONFIGURATION_DATABASE_ANDROID, storageMode2);
    }

    private void configurationImpl(String path2, int type2, int databaseType2, int storageMode2) {
        if (!checkConfig(type2, databaseType2, storageMode2)) {
            throw new ODMFIllegalArgumentException("The configuration is incorrect.");
        }
        this.path = path2;
        this.type = type2;
        this.databaseType = databaseType2;
        this.storageMode = storageMode2;
    }

    private boolean checkConfig(int type2, int databaseType2, int storageMode2) {
        if ((type2 == 200 || type2 == 201) && ((databaseType2 == 302 || databaseType2 == 301) && (storageMode2 == 401 || storageMode2 == 402))) {
            return true;
        }
        return false;
    }

    public String getPath() {
        return this.path;
    }

    public int getType() {
        return this.type;
    }

    public int getDatabaseType() {
        return this.databaseType;
    }

    public int getStorageMode() {
        return this.storageMode;
    }

    /* access modifiers changed from: package-private */
    public void setType(int type2) {
        this.type = type2;
    }

    /* access modifiers changed from: package-private */
    public void setDatabaseType(int databaseType2) {
        this.databaseType = databaseType2;
    }

    /* access modifiers changed from: package-private */
    public void setStorageMode(int storageMode2) {
        this.storageMode = storageMode2;
    }

    private static String modeToString(int mode) {
        switch (mode) {
            case CONFIGURATION_STORAGE_MODE_MEMORY /*401*/:
                return "CONFIGURATION_STORAGE_MODE_MEMORY";
            case CONFIGURATION_STORAGE_MODE_DISK /*402*/:
                return "CONFIGURATION_STORAGE_MODE_DISK";
            default:
                return Integer.toString(mode);
        }
    }

    private static String typeToString(int type2) {
        switch (type2) {
            case 200:
                return "CONFIGURATION_TYPE_LOCAL";
            case CONFIGURATION_TYPE_PROVIDER /*201*/:
                return "CONFIGURATION_TYPE_PROVIDER";
            default:
                return Integer.toString(type2);
        }
    }

    private static String databaseTypeToString(int databaseType2) {
        switch (databaseType2) {
            case CONFIGURATION_DATABASE_ODMF /*301*/:
                return "CONFIGURATION_DATABASE_ODMF";
            case CONFIGURATION_DATABASE_ANDROID /*302*/:
                return "CONFIGURATION_DATABASE_ANDROID";
            default:
                return Integer.toString(databaseType2);
        }
    }

    public boolean isThrowException() {
        return this.throwException;
    }

    public void setThrowException(boolean throwException2) {
        this.throwException = throwException2;
    }

    public boolean isDetectDelete() {
        return this.detectDelete;
    }

    public void setDetectDelete(boolean detectDelete2) {
        this.detectDelete = detectDelete2;
    }

    public String toString() {
        return "Configuration {Path :" + this.path + ", Mode:" + modeToString(this.storageMode) + ", Type:" + typeToString(this.type) + ", DatabaseType:" + databaseTypeToString(this.databaseType) + "}";
    }
}
