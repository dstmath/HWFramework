package ohos.io;

import java.io.FileDescriptor;
import java.io.IOException;
import libcore.io.IoUtils;

public class IoManager {
    public static void setFileBlocking(FileDescriptor fileDescriptor, boolean z) throws IOException {
        IoUtils.setBlocking(fileDescriptor, z);
    }

    public static int obtainRawFileDescriptor(FileDescriptor fileDescriptor) {
        return IoUtils.acquireRawFd(fileDescriptor);
    }

    public static void closeFileQuietly(FileDescriptor fileDescriptor) {
        IoUtils.closeQuietly(fileDescriptor);
    }

    public static void setFileDescriptorOwner(FileDescriptor fileDescriptor, Object obj) {
        IoUtils.setFdOwner(fileDescriptor, obj);
    }
}
