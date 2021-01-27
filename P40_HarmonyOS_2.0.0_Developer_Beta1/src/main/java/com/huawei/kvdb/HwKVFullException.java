package com.huawei.kvdb;

public class HwKVFullException extends HwKVException {
    public HwKVFullException() {
        super("No space is left.");
    }
}
