package java.util.zip;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class ZipUtils {
    private static final long WINDOWS_EPOCH_IN_MICROSECONDS = -11644473600000000L;

    ZipUtils() {
    }

    public static final FileTime winTimeToFileTime(long wtime) {
        return FileTime.from((wtime / 10) + WINDOWS_EPOCH_IN_MICROSECONDS, TimeUnit.MICROSECONDS);
    }

    public static final long fileTimeToWinTime(FileTime ftime) {
        return (ftime.to(TimeUnit.MICROSECONDS) - WINDOWS_EPOCH_IN_MICROSECONDS) * 10;
    }

    public static final FileTime unixTimeToFileTime(long utime) {
        return FileTime.from(utime, TimeUnit.SECONDS);
    }

    public static final long fileTimeToUnixTime(FileTime ftime) {
        return ftime.to(TimeUnit.SECONDS);
    }

    private static long dosToJavaTime(long dtime) {
        return new Date((int) (((dtime >> 25) & 127) + 80), (int) (((dtime >> 21) & 15) - 1), (int) ((dtime >> 16) & 31), (int) ((dtime >> 11) & 31), (int) ((dtime >> 5) & 63), (int) ((dtime << 1) & 62)).getTime();
    }

    public static long extendedDosToJavaTime(long xdostime) {
        return (xdostime >> 32) + dosToJavaTime(xdostime);
    }

    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return 2162688;
        }
        return ((long) (((((((year - 1980) << 25) | ((d.getMonth() + 1) << 21)) | (d.getDate() << 16)) | (d.getHours() << 11)) | (d.getMinutes() << 5)) | (d.getSeconds() >> 1))) & 4294967295L;
    }

    public static long javaToExtendedDosTime(long time) {
        long j = 2162688;
        if (time < 0) {
            return 2162688;
        }
        long dostime = javaToDosTime(time);
        if (dostime != 2162688) {
            j = ((time % 2000) << 32) + dostime;
        }
        return j;
    }

    public static final int get16(byte[] b, int off) {
        return Byte.toUnsignedInt(b[off]) | (Byte.toUnsignedInt(b[off + 1]) << 8);
    }

    public static final long get32(byte[] b, int off) {
        return (((long) get16(b, off)) | (((long) get16(b, off + 2)) << 16)) & 4294967295L;
    }

    public static final long get64(byte[] b, int off) {
        return get32(b, off) | (get32(b, off + 4) << 32);
    }
}
