package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class FileChannel extends AbstractInterruptibleChannel implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

    public static class MapMode {
        public static final MapMode PRIVATE = new MapMode("PRIVATE");
        public static final MapMode READ_ONLY = new MapMode("READ_ONLY");
        public static final MapMode READ_WRITE = new MapMode("READ_WRITE");
        private final String name;

        private MapMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public abstract void force(boolean z) throws IOException;

    public abstract FileLock lock(long j, long j2, boolean z) throws IOException;

    public abstract MappedByteBuffer map(MapMode mapMode, long j, long j2) throws IOException;

    public abstract long position() throws IOException;

    public abstract FileChannel position(long j) throws IOException;

    public abstract int read(ByteBuffer byteBuffer) throws IOException;

    public abstract int read(ByteBuffer byteBuffer, long j) throws IOException;

    public abstract long read(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    public abstract long size() throws IOException;

    public abstract long transferFrom(ReadableByteChannel readableByteChannel, long j, long j2) throws IOException;

    public abstract long transferTo(long j, long j2, WritableByteChannel writableByteChannel) throws IOException;

    public abstract FileChannel truncate(long j) throws IOException;

    public abstract FileLock tryLock(long j, long j2, boolean z) throws IOException;

    public abstract int write(ByteBuffer byteBuffer) throws IOException;

    public abstract int write(ByteBuffer byteBuffer, long j) throws IOException;

    public abstract long write(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    protected FileChannel() {
    }

    public static FileChannel open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return path.getFileSystem().provider().newFileChannel(path, options, attrs);
    }

    public static FileChannel open(Path path, OpenOption... options) throws IOException {
        Set<OpenOption> set = new HashSet(options.length);
        Collections.addAll(set, options);
        return open(path, set, NO_ATTRIBUTES);
    }

    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    public final FileLock lock() throws IOException {
        return lock(0, Long.MAX_VALUE, false);
    }

    public final FileLock tryLock() throws IOException {
        return tryLock(0, Long.MAX_VALUE, false);
    }
}
