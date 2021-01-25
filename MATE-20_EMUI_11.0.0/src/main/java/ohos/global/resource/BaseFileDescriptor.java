package ohos.global.resource;

import java.io.Closeable;
import java.io.FileDescriptor;

public abstract class BaseFileDescriptor implements Closeable {
    public abstract FileDescriptor getFileDescriptor();

    public abstract long getFileSize();

    public abstract long getStartPosition();
}
