package com.huawei.libcore.io;

import java.io.FileDescriptor;
import libcore.io.IoUtils;

public final class IoUtilsEx {
    public static void closeQuietly(FileDescriptor fd) {
        IoUtils.closeQuietly(fd);
    }
}
