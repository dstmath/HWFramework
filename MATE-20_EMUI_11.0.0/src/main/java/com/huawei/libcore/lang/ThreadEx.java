package com.huawei.libcore.lang;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ThreadEx {
    public static int getLockOwnerThreadId(Object lock) {
        return Thread.getLockOwnerThreadId(lock);
    }
}
