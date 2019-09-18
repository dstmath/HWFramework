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

    /* access modifiers changed from: package-private */
    public long blockSize() {
        return this.f_frsize;
    }

    /* access modifiers changed from: package-private */
    public long totalBlocks() {
        return this.f_blocks;
    }

    /* access modifiers changed from: package-private */
    public long freeBlocks() {
        return this.f_bfree;
    }

    /* access modifiers changed from: package-private */
    public long availableBlocks() {
        return this.f_bavail;
    }
}
