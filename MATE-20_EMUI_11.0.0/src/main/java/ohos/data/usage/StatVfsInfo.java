package ohos.data.usage;

public class StatVfsInfo {
    private long availableBlocks;
    private long blockSize;
    private long freeBlocks;
    private long totalBlocks;

    public StatVfsInfo(long j, long j2, long j3, long j4) {
        this.blockSize = j;
        this.totalBlocks = j2;
        this.freeBlocks = j3;
        this.availableBlocks = j4;
    }

    public long getTotalBytes() {
        return this.totalBlocks * this.blockSize;
    }

    public long getFreeBytes() {
        return this.freeBlocks * this.blockSize;
    }

    public long getAvailableBytes() {
        return this.availableBlocks * this.blockSize;
    }
}
