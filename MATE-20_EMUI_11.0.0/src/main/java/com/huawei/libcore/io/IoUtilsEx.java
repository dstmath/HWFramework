package com.huawei.libcore.io;

import com.huawei.annotation.HwSystemApi;
import java.io.FileDescriptor;
import java.io.IOException;
import libcore.io.IoUtils;

public final class IoUtilsEx {
    public static void closeQuietly(FileDescriptor fd) {
        IoUtils.closeQuietly(fd);
    }

    @HwSystemApi
    public static void closeQuietly(AutoCloseable closeable) {
        IoUtils.closeQuietly(closeable);
    }

    @HwSystemApi
    public static String readFileAsString(String absolutePath) throws IOException {
        return IoUtils.readFileAsString(absolutePath);
    }
}
