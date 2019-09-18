package android.system;

import libcore.util.Objects;

public final class StructStat {
    public final StructTimespec st_atim;
    public final long st_atime;
    public final long st_blksize;
    public final long st_blocks;
    public final StructTimespec st_ctim;
    public final long st_ctime;
    public final long st_dev;
    public final int st_gid;
    public final long st_ino;
    public final int st_mode;
    public final StructTimespec st_mtim;
    public final long st_mtime;
    public final long st_nlink;
    public final long st_rdev;
    public final long st_size;
    public final int st_uid;

    public StructStat(long st_dev2, long st_ino2, int st_mode2, long st_nlink2, int st_uid2, int st_gid2, long st_rdev2, long st_size2, long st_atime2, long st_mtime2, long st_ctime2, long st_blksize2, long st_blocks2) {
        this(st_dev2, st_ino2, st_mode2, st_nlink2, st_uid2, st_gid2, st_rdev2, st_size2, new StructTimespec(st_atime2, 0), new StructTimespec(st_mtime2, 0), new StructTimespec(st_ctime2, 0), st_blksize2, st_blocks2);
    }

    public StructStat(long st_dev2, long st_ino2, int st_mode2, long st_nlink2, int st_uid2, int st_gid2, long st_rdev2, long st_size2, StructTimespec st_atim2, StructTimespec st_mtim2, StructTimespec st_ctim2, long st_blksize2, long st_blocks2) {
        StructTimespec structTimespec = st_atim2;
        StructTimespec structTimespec2 = st_mtim2;
        StructTimespec structTimespec3 = st_ctim2;
        this.st_dev = st_dev2;
        this.st_ino = st_ino2;
        this.st_mode = st_mode2;
        this.st_nlink = st_nlink2;
        this.st_uid = st_uid2;
        this.st_gid = st_gid2;
        this.st_rdev = st_rdev2;
        this.st_size = st_size2;
        this.st_atime = structTimespec.tv_sec;
        this.st_mtime = structTimespec2.tv_sec;
        this.st_ctime = structTimespec3.tv_sec;
        this.st_atim = structTimespec;
        this.st_mtim = structTimespec2;
        this.st_ctim = structTimespec3;
        this.st_blksize = st_blksize2;
        this.st_blocks = st_blocks2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
