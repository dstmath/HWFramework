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

    public StructStatVfs(long f_bsize2, long f_frsize2, long f_blocks2, long f_bfree2, long f_bavail2, long f_files2, long f_ffree2, long f_favail2, long f_fsid2, long f_flag2, long f_namemax2) {
        this.f_bsize = f_bsize2;
        this.f_frsize = f_frsize2;
        this.f_blocks = f_blocks2;
        this.f_bfree = f_bfree2;
        this.f_bavail = f_bavail2;
        this.f_files = f_files2;
        this.f_ffree = f_ffree2;
        this.f_favail = f_favail2;
        this.f_fsid = f_fsid2;
        this.f_flag = f_flag2;
        this.f_namemax = f_namemax2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
