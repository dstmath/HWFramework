package sun.misc;

import java.io.FileDescriptor;

public interface JavaIOFileDescriptorAccess {
    int get(FileDescriptor fileDescriptor);

    long getHandle(FileDescriptor fileDescriptor);

    void set(FileDescriptor fileDescriptor, int i);

    void setHandle(FileDescriptor fileDescriptor, long j);
}
