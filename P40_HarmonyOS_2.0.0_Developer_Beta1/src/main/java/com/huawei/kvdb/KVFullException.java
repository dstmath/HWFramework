package com.huawei.kvdb;

public class KVFullException extends KVException {
    public KVFullException() {
        super("No space is left.");
    }
}
