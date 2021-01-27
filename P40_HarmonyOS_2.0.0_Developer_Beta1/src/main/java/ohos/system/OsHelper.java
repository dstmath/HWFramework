package ohos.system;

import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

public final class OsHelper {
    private OsHelper() {
    }

    public static FileDescriptor[] createPipe2(int i) throws OsHelperErrnoException {
        try {
            return Os.pipe2(i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static String readSymbolicLink(String str) throws OsHelperErrnoException {
        try {
            return Os.readlink(str);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static FileStat getFileStatus(String str) throws OsHelperErrnoException {
        try {
            StructStat stat = Os.stat(str);
            return new FileStat(stat.st_dev, stat.st_ino, stat.st_mode, stat.st_nlink, stat.st_uid, stat.st_gid, stat.st_rdev, stat.st_size, new TimeSpecGroup(stat.st_atim.tv_sec, stat.st_atim.tv_nsec), new TimeSpecGroup(stat.st_mtim.tv_sec, stat.st_mtim.tv_nsec), new TimeSpecGroup(stat.st_ctim.tv_sec, stat.st_ctim.tv_nsec), stat.st_blksize, stat.st_blocks);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static FileStat getFileStatus(FileDescriptor fileDescriptor) throws OsHelperErrnoException {
        try {
            StructStat fstat = Os.fstat(fileDescriptor);
            return new FileStat(fstat.st_dev, fstat.st_ino, fstat.st_mode, fstat.st_nlink, fstat.st_uid, fstat.st_gid, fstat.st_rdev, fstat.st_size, new TimeSpecGroup(fstat.st_atim.tv_sec, fstat.st_atim.tv_nsec), new TimeSpecGroup(fstat.st_mtim.tv_sec, fstat.st_mtim.tv_nsec), new TimeSpecGroup(fstat.st_ctim.tv_sec, fstat.st_ctim.tv_nsec), fstat.st_blksize, fstat.st_blocks);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static FileStat getLinkFileStatus(String str) throws OsHelperErrnoException {
        try {
            StructStat lstat = Os.lstat(str);
            return new FileStat(lstat.st_dev, lstat.st_ino, lstat.st_mode, lstat.st_nlink, lstat.st_uid, lstat.st_gid, lstat.st_rdev, lstat.st_size, new TimeSpecGroup(lstat.st_atim.tv_sec, lstat.st_atim.tv_nsec), new TimeSpecGroup(lstat.st_mtim.tv_sec, lstat.st_mtim.tv_nsec), new TimeSpecGroup(lstat.st_ctim.tv_sec, lstat.st_ctim.tv_nsec), lstat.st_blksize, lstat.st_blocks);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void symbolLink(String str, String str2) throws OsHelperErrnoException {
        try {
            Os.symlink(str, str2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void createSocketPair(int i, int i2, int i3, FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2) throws OsHelperErrnoException {
        try {
            Os.socketpair(i, i2, i3, fileDescriptor, fileDescriptor2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static long getSystemConfig(int i) {
        return Os.sysconf(i);
    }

    public static void setFileExtendedAttribute(String str, String str2, byte[] bArr, int i) throws OsHelperErrnoException {
        try {
            Os.setxattr(str, str2, bArr, i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void setEnvironmentVar(String str, String str2, boolean z) throws OsHelperErrnoException {
        try {
            Os.setenv(str, str2, z);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void removeFileExtendedAttribute(String str, String str2) throws OsHelperErrnoException {
        try {
            Os.removexattr(str, str2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void changeFileName(String str, String str2) throws OsHelperErrnoException {
        try {
            Os.rename(str, str2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int readFile(FileDescriptor fileDescriptor, ByteBuffer byteBuffer) throws InterruptedIOException, OsHelperErrnoException {
        try {
            return Os.read(fileDescriptor, byteBuffer);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int readFile(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2) throws InterruptedIOException, OsHelperErrnoException {
        try {
            return Os.read(fileDescriptor, bArr, i, i2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int setProcessOption(int i, long j, long j2, long j3, long j4) throws OsHelperErrnoException {
        try {
            return Os.prctl(i, j, j2, j3, j4);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void allocSpaceForFile(FileDescriptor fileDescriptor, long j, long j2) throws OsHelperErrnoException {
        try {
            Os.posix_fallocate(fileDescriptor, j, j2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static long setFileOperationPos(FileDescriptor fileDescriptor, long j, int i) throws OsHelperErrnoException {
        try {
            return Os.lseek(fileDescriptor, j, i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int getUID() {
        return Os.getuid();
    }

    public static FileDescriptor copyFileDescriptor(FileDescriptor fileDescriptor, int i) throws OsHelperErrnoException {
        try {
            return Os.dup2(fileDescriptor, i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void closeFile(FileDescriptor fileDescriptor) throws OsHelperErrnoException {
        try {
            Os.close(fileDescriptor);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void changeFilePermission(String str, int i) throws OsHelperErrnoException {
        try {
            Os.chmod(str, i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static boolean checkFilePermission(String str, int i) throws OsHelperErrnoException {
        try {
            return Os.access(str, i);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static FileDescriptor openFile(String str, int i, int i2) throws OsHelperErrnoException {
        try {
            return Os.open(str, i, i2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int writeFile(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2) throws InterruptedIOException, OsHelperErrnoException {
        try {
            return Os.write(fileDescriptor, bArr, i, i2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static int manipulateFileDescriptor(FileDescriptor fileDescriptor, int i, int i2) throws OsHelperErrnoException {
        try {
            return Os.fcntlInt(fileDescriptor, i, i2);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static void trimFile(long j, FileDescriptor fileDescriptor) throws OsHelperErrnoException {
        try {
            Os.ftruncate(fileDescriptor, j);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }

    public static FileDescriptor copyFileDescriptor(FileDescriptor fileDescriptor) throws OsHelperErrnoException {
        try {
            return Os.dup(fileDescriptor);
        } catch (ErrnoException e) {
            throw new OsHelperErrnoException(e.errno, e.getMessage());
        }
    }
}
