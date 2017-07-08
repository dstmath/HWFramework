package android.system;

import libcore.util.Objects;

public final class StructStatVfs {
    public final long f_bavail;
    public final long f_bfree;
    public final long f_blocks;
    public final long f_bsize;
    public final long f_favail;
    public final long f_ffree;
    public final long f_files;
    public final long f_flag;
    public final long f_frsize;
    public final long f_fsid;
    public final long f_namemax;

    public StructStatVfs(long f_bsize, long f_frsize, long f_blocks, long f_bfree, long f_bavail, long f_files, long f_ffree, long f_favail, long f_fsid, long f_flag, long f_namemax) {
        this.f_bsize = f_bsize;
        this.f_frsize = f_frsize;
        this.f_blocks = f_blocks;
        this.f_bfree = f_bfree;
        this.f_bavail = f_bavail;
        this.f_files = f_files;
        this.f_ffree = f_ffree;
        this.f_favail = f_favail;
        this.f_fsid = f_fsid;
        this.f_flag = f_flag;
        this.f_namemax = f_namemax;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
