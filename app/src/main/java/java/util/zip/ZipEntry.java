package java.util.zip;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ZipEntry implements ZipConstants, Cloneable {
    public static final int DEFLATED = 8;
    public static final int STORED = 0;
    String comment;
    long crc;
    long csize;
    long dataOffset;
    byte[] extra;
    int flag;
    int method;
    String name;
    long size;
    long time;

    public ZipEntry(String name, String comment, long crc, long compressedSize, long size, int compressionMethod, int time, byte[] extra, long dataOffset) {
        this.time = -1;
        this.crc = -1;
        this.size = -1;
        this.csize = -1;
        this.method = -1;
        this.flag = 0;
        this.name = name;
        this.comment = comment;
        this.crc = crc;
        this.csize = compressedSize;
        this.size = size;
        this.method = compressionMethod;
        this.time = (long) time;
        this.extra = extra;
        this.dataOffset = dataOffset;
    }

    public ZipEntry(String name) {
        this.time = -1;
        this.crc = -1;
        this.size = -1;
        this.csize = -1;
        this.method = -1;
        this.flag = 0;
        if (name == null) {
            throw new NullPointerException();
        } else if (name.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new IllegalArgumentException(name + " too long: " + name.getBytes(StandardCharsets.UTF_8).length);
        } else {
            this.name = name;
        }
    }

    public ZipEntry(ZipEntry e) {
        this.time = -1;
        this.crc = -1;
        this.size = -1;
        this.csize = -1;
        this.method = -1;
        this.flag = 0;
        this.name = e.name;
        this.time = e.time;
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
        this.time = -1;
        this.crc = -1;
        this.size = -1;
        this.csize = -1;
        this.method = -1;
        this.flag = 0;
    }

    public long getDataOffset() {
        return this.dataOffset;
    }

    public String getName() {
        return this.name;
    }

    public void setTime(long time) {
        this.time = javaToDosTime(time);
    }

    public long getTime() {
        return this.time != -1 ? dosToJavaTime(this.time) : -1;
    }

    public void setSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("invalid entry size");
        }
        this.size = size;
    }

    public long getSize() {
        return this.size;
    }

    public long getCompressedSize() {
        return this.csize;
    }

    public void setCompressedSize(long csize) {
        this.csize = csize;
    }

    public void setCrc(long crc) {
        if (crc < 0 || crc > 4294967295L) {
            throw new IllegalArgumentException("invalid entry crc-32");
        }
        this.crc = crc;
    }

    public long getCrc() {
        return this.crc;
    }

    public void setMethod(int method) {
        if (method == 0 || method == DEFLATED) {
            this.method = method;
            return;
        }
        throw new IllegalArgumentException("invalid compression method");
    }

    public int getMethod() {
        return this.method;
    }

    public void setExtra(byte[] extra) {
        if (extra == null || extra.length <= 65535) {
            this.extra = extra;
            return;
        }
        throw new IllegalArgumentException("invalid extra field length");
    }

    public byte[] getExtra() {
        return this.extra;
    }

    public void setComment(String comment) {
        if (comment == null) {
            this.comment = null;
        } else if (comment.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new IllegalArgumentException(comment + " too long: " + comment.getBytes(StandardCharsets.UTF_8).length);
        } else {
            this.comment = comment;
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

    private static long dosToJavaTime(long dtime) {
        return new Date((int) (((dtime >> 25) & 127) + 80), (int) (((dtime >> 21) & 15) - 1), (int) ((dtime >> 16) & 31), (int) ((dtime >> 11) & 31), (int) ((dtime >> 5) & 63), (int) ((dtime << 1) & 62)).getTime();
    }

    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return 2162688;
        }
        return (long) (((((((year - 1980) << 25) | ((d.getMonth() + 1) << 21)) | (d.getDate() << 16)) | (d.getHours() << 11)) | (d.getMinutes() << 5)) | (d.getSeconds() >> 1));
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public Object clone() {
        byte[] bArr = null;
        try {
            ZipEntry e = (ZipEntry) super.clone();
            if (this.extra != null) {
                bArr = (byte[]) this.extra.clone();
            }
            e.extra = bArr;
            return e;
        } catch (CloneNotSupportedException e2) {
            throw new InternalError();
        }
    }
}
