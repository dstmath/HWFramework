package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.List;

abstract class FileLockTable {
    public abstract void add(FileLock fileLock) throws OverlappingFileLockException;

    public abstract void remove(FileLock fileLock);

    public abstract List<FileLock> removeAll();

    public abstract void replace(FileLock fileLock, FileLock fileLock2);

    protected FileLockTable() {
    }

    public static FileLockTable newSharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        return new SharedFileLockTable(channel, fd);
    }
}
