package android.os;

import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStatVfs;

public class StatFs {
    private StructStatVfs mStat;

    public StatFs(String path) {
        this.mStat = doStat(path);
    }

    private static StructStatVfs doStat(String path) {
        try {
            return Os.statvfs(path);
        } catch (ErrnoException e) {
            throw new IllegalArgumentException("Invalid path: " + path, e);
        }
    }

    public void restat(String path) {
        this.mStat = doStat(path);
    }

    @Deprecated
    public int getBlockSize() {
        return (int) this.mStat.f_bsize;
    }

    public long getBlockSizeLong() {
        return this.mStat.f_bsize;
    }

    @Deprecated
    public int getBlockCount() {
        return (int) this.mStat.f_blocks;
    }

    public long getBlockCountLong() {
        return this.mStat.f_blocks;
    }

    @Deprecated
    public int getFreeBlocks() {
        return (int) this.mStat.f_bfree;
    }

    public long getFreeBlocksLong() {
        return this.mStat.f_bfree;
    }

    public long getFreeBytes() {
        return this.mStat.f_bfree * this.mStat.f_bsize;
    }

    @Deprecated
    public int getAvailableBlocks() {
        return (int) this.mStat.f_bavail;
    }

    public long getAvailableBlocksLong() {
        return this.mStat.f_bavail;
    }

    public long getAvailableBytes() {
        return this.mStat.f_bavail * this.mStat.f_bsize;
    }

    public long getTotalBytes() {
        return this.mStat.f_blocks * this.mStat.f_bsize;
    }
}
