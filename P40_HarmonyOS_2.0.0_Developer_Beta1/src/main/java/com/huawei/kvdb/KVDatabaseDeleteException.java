package com.huawei.kvdb;

public class KVDatabaseDeleteException extends KVException {
    public KVDatabaseDeleteException() {
        super("Database file is deleted");
    }
}
