package java.util.zip;

import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ZipEntry implements ZipConstants, Cloneable {
    public static final int DEFLATED = 8;
    static final long DOSTIME_BEFORE_1980 = 2162688;
    public static final int STORED = 0;
    public static final long UPPER_DOSTIME_BOUND = 4036608000000L;
    FileTime atime;
    String comment;
    long crc = -1;
    long csize = -1;
    FileTime ctime;
    long dataOffset;
    byte[] extra;
    int flag = 0;
    int method = -1;
    FileTime mtime;
    String name;
    long size = -1;
    long xdostime = -1;

    public ZipEntry(String name2, String comment2, long crc2, long compressedSize, long size2, int compressionMethod, int xdostime2, byte[] extra2, long dataOffset2) {
        this.name = name2;
        this.comment = comment2;
        this.crc = crc2;
        this.csize = compressedSize;
        this.size = size2;
        this.method = compressionMethod;
        this.xdostime = (long) xdostime2;
        this.dataOffset = dataOffset2;
        setExtra0(extra2, false);
    }

    public ZipEntry(String name2) {
        Objects.requireNonNull(name2, "name");
        if (name2.getBytes(StandardCharsets.UTF_8).length <= 65535) {
            this.name = name2;
            return;
        }
        throw new IllegalArgumentException(name2 + " too long: " + name2.getBytes(StandardCharsets.UTF_8).length);
    }

    public ZipEntry(ZipEntry e) {
        Objects.requireNonNull(e, "entry");
        this.name = e.name;
        this.xdostime = e.xdostime;
        this.mtime = e.mtime;
        this.atime = e.atime;
        this.ctime = e.ctime;
        this.crc = e.crc;
        this.size = e.size;
        this.csize = e.csize;
        this.method = e.method;
        this.flag = e.flag;
        this.extra = e.extra;
        this.comment = e.comment;
        this.dataOffset = e.dataOffset;
    }

    ZipEntry() {
    }

    public long getDataOffset() {
        return this.dataOffset;
    }

    public String getName() {
        return this.name;
    }

    public void setTime(long time) {
        this.xdostime = ZipUtils.javaToExtendedDosTime(time);
        if (this.xdostime == DOSTIME_BEFORE_1980 || time > UPPER_DOSTIME_BOUND) {
            this.mtime = FileTime.from(time, TimeUnit.MILLISECONDS);
        } else {
            this.mtime = null;
        }
    }

    public long getTime() {
        if (this.mtime != null) {
            return this.mtime.toMillis();
        }
        long j = -1;
        if (this.xdostime != -1) {
            j = ZipUtils.extendedDosToJavaTime(this.xdostime);
        }
        return j;
    }

    public ZipEntry setLastModifiedTime(FileTime time) {
        this.mtime = (FileTime) Objects.requireNonNull(time, "lastModifiedTime");
        this.xdostime = ZipUtils.javaToExtendedDosTime(time.to(TimeUnit.MILLISECONDS));
        return this;
    }

    public FileTime getLastModifiedTime() {
        if (this.mtime != null) {
            return this.mtime;
        }
        if (this.xdostime == -1) {
            return null;
        }
        return FileTime.from(getTime(), TimeUnit.MILLISECONDS);
    }

    public ZipEntry setLastAccessTime(FileTime time) {
        this.atime = (FileTime) Objects.requireNonNull(time, "lastAccessTime");
        return this;
    }

    public FileTime getLastAccessTime() {
        return this.atime;
    }

    public ZipEntry setCreationTime(FileTime time) {
        this.ctime = (FileTime) Objects.requireNonNull(time, "creationTime");
        return this;
    }

    public FileTime getCreationTime() {
        return this.ctime;
    }

    public void setSize(long size2) {
        if (size2 >= 0) {
            this.size = size2;
            return;
        }
        throw new IllegalArgumentException("invalid entry size");
    }

    public long getSize() {
        return this.size;
    }

    public long getCompressedSize() {
        return this.csize;
    }

    public void setCompressedSize(long csize2) {
        this.csize = csize2;
    }

    public void setCrc(long crc2) {
        if (crc2 < 0 || crc2 > 4294967295L) {
            throw new IllegalArgumentException("invalid entry crc-32");
        }
        this.crc = crc2;
    }

    public long getCrc() {
        return this.crc;
    }

    public void setMethod(int method2) {
        if (method2 == 0 || method2 == 8) {
            this.method = method2;
            return;
        }
        throw new IllegalArgumentException("invalid compression method");
    }

    public int getMethod() {
        return this.method;
    }

    public void setExtra(byte[] extra2) {
        setExtra0(extra2, false);
    }

    /* access modifiers changed from: package-private */
    public void setExtra0(byte[] extra2, boolean doZIP64) {
        if (extra2 != null) {
            if (extra2.length <= 65535) {
                int off = 0;
                int len = extra2.length;
                while (off + 4 < len) {
                    int tag = ZipUtils.get16(extra2, off);
                    int sz = ZipUtils.get16(extra2, off + 2);
                    int off2 = off + 4;
                    if (off2 + sz > len) {
                        break;
                    }
                    if (tag != 1) {
                        if (tag != 10) {
                            if (tag == 21589) {
                                int flag2 = Byte.toUnsignedInt(extra2[off2]);
                                int sz0 = 1;
                                if ((flag2 & 1) != 0 && 1 + 4 <= sz) {
                                    this.mtime = ZipUtils.unixTimeToFileTime(ZipUtils.get32(extra2, off2 + 1));
                                    sz0 = 1 + 4;
                                }
                                if ((flag2 & 2) != 0 && sz0 + 4 <= sz) {
                                    this.atime = ZipUtils.unixTimeToFileTime(ZipUtils.get32(extra2, off2 + sz0));
                                    sz0 += 4;
                                }
                                if ((flag2 & 4) != 0 && sz0 + 4 <= sz) {
                                    this.ctime = ZipUtils.unixTimeToFileTime(ZipUtils.get32(extra2, off2 + sz0));
                                    int sz02 = sz0 + 4;
                                }
                            }
                        } else if (sz >= 32) {
                            int pos = off2 + 4;
                            if (ZipUtils.get16(extra2, pos) == 1 && ZipUtils.get16(extra2, pos + 2) == 24) {
                                this.mtime = ZipUtils.winTimeToFileTime(ZipUtils.get64(extra2, pos + 4));
                                this.atime = ZipUtils.winTimeToFileTime(ZipUtils.get64(extra2, pos + 12));
                                this.ctime = ZipUtils.winTimeToFileTime(ZipUtils.get64(extra2, pos + 20));
                            }
                        }
                    } else if (doZIP64 && sz >= 16) {
                        this.size = ZipUtils.get64(extra2, off2);
                        this.csize = ZipUtils.get64(extra2, off2 + 8);
                    }
                    off = off2 + sz;
                }
            } else {
                throw new IllegalArgumentException("invalid extra field length");
            }
        }
        this.extra = extra2;
    }

    public byte[] getExtra() {
        return this.extra;
    }

    public void setComment(String comment2) {
        if (comment2 == null) {
            this.comment = null;
        } else if (comment2.getBytes(StandardCharsets.UTF_8).length <= 65535) {
            this.comment = comment2;
        } else {
            throw new IllegalArgumentException(comment2 + " too long: " + comment2.getBytes(StandardCharsets.UTF_8).length);
        }
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isDirectory() {
        return this.name.endsWith("/");
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public Object clone() {
        try {
            ZipEntry e = (ZipEntry) super.clone();
            e.extra = this.extra == null ? null : (byte[]) this.extra.clone();
            return e;
        } catch (CloneNotSupportedException e2) {
            throw new InternalError((Throwable) e2);
        }
    }
}
