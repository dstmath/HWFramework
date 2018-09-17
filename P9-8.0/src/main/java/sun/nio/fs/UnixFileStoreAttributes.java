package sun.nio.fs;

class UnixFileStoreAttributes {
    private long f_bavail;
    private long f_bfree;
    private long f_blocks;
    private long f_frsize;

    private UnixFileStoreAttributes() {
    }

    static UnixFileStoreAttributes get(UnixPath path) throws UnixException {
        UnixFileStoreAttributes attrs = new UnixFileStoreAttributes();
        UnixNativeDispatcher.statvfs(path, attrs);
        return attrs;
    }

    long blockSize() {
        return this.f_frsize;
    }

    long totalBlocks() {
        return this.f_blocks;
    }

    long freeBlocks() {
        return this.f_bfree;
    }

    long availableBlocks() {
        return this.f_bavail;
    }
}
