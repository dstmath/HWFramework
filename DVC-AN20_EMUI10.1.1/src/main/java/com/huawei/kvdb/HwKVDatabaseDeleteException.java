package com.huawei.kvdb;

public class HwKVDatabaseDeleteException extends HwKVException {
    public HwKVDatabaseDeleteException() {
        super("Database file is deleted");
    }
}
