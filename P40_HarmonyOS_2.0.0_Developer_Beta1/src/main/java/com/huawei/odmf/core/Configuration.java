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

    private boolean checkConfig(int i, int i2, int i3) {
        if (i != 200 && i != 201) {
            return false;
        }
        if (i2 == 302 || i2 == 301) {
            return i3 == 401 || i3 == 402;
        }
        return false;
    }

    public Configuration(String str, int i, int i2, int i3) {
        configurationImpl(str, i, i2, i3);
    }

    public Configuration() {
        configurationImpl(null, 200, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String str) {
        configurationImpl(str, 200, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String str, int i) {
        configurationImpl(str, i, CONFIGURATION_DATABASE_ANDROID, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(String str, int i, int i2) {
        configurationImpl(str, i, i2, CONFIGURATION_STORAGE_MODE_DISK);
    }

    public Configuration(int i) {
        configurationImpl(null, 200, CONFIGURATION_DATABASE_ANDROID, i);
    }

    private void configurationImpl(String str, int i, int i2, int i3) {
        if (checkConfig(i, i2, i3)) {
            this.path = str;
            this.type = i;
            this.databaseType = i2;
            this.storageMode = i3;
            return;
        }
        throw new ODMFIllegalArgumentException("The configuration is incorrect.");
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
    public void setType(int i) {
        this.type = i;
    }

    /* access modifiers changed from: package-private */
    public void setDatabaseType(int i) {
        this.databaseType = i;
    }

    /* access modifiers changed from: package-private */
    public void setStorageMode(int i) {
        this.storageMode = i;
    }

    private static String modeToString(int i) {
        if (i != 401) {
            return i != 402 ? Integer.toString(i) : "CONFIGURATION_STORAGE_MODE_DISK";
        }
        return "CONFIGURATION_STORAGE_MODE_MEMORY";
    }

    private static String typeToString(int i) {
        if (i != 200) {
            return i != 201 ? Integer.toString(i) : "CONFIGURATION_TYPE_PROVIDER";
        }
        return "CONFIGURATION_TYPE_LOCAL";
    }

    private static String databaseTypeToString(int i) {
        if (i != 301) {
            return i != 302 ? Integer.toString(i) : "CONFIGURATION_DATABASE_ANDROID";
        }
        return "CONFIGURATION_DATABASE_ODMF";
    }

    public boolean isThrowException() {
        return this.throwException;
    }

    public void setThrowException(boolean z) {
        this.throwException = z;
    }

    public boolean isDetectDelete() {
        return this.detectDelete;
    }

    public void setDetectDelete(boolean z) {
        this.detectDelete = z;
    }

    public String toString() {
        return "Configuration {Path :" + this.path + ", Mode:" + modeToString(this.storageMode) + ", Type:" + typeToString(this.type) + ", DatabaseType:" + databaseTypeToString(this.databaseType) + "}";
    }
}
