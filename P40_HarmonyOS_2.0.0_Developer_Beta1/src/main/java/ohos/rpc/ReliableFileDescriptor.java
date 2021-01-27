package ohos.rpc;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.io.IoManager;
import ohos.net.NetManager;
import ohos.system.FileStat;
import ohos.system.OsHelper;
import ohos.system.OsHelperConstants;
import ohos.system.OsHelperErrnoException;

public class ReliableFileDescriptor {
    private static final int AF_UNIX = 1;
    private static final long INITIAL_OWNER_TYPE = 255;
    private static final int OFFSET = 56;
    private static final int O_CLOEXEC = 524288;
    private static final int SOCK_CLOEXEC = 524288;
    private static final int SOCK_STREAM = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "ReliableFileDescriptor");
    private FileDescriptor mCommonFd;
    private final FileDescriptor mFd;

    public ReliableFileDescriptor(FileDescriptor fileDescriptor) {
        this(fileDescriptor, null);
    }

    public ReliableFileDescriptor(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2) {
        if (fileDescriptor == null) {
            HiLog.error(TAG, "input file descriptor cannot be null!", new Object[0]);
        }
        this.mFd = fileDescriptor;
        setFdOwner(this.mFd, this);
        this.mCommonFd = fileDescriptor2;
        if (fileDescriptor2 != null) {
            setFdOwner(fileDescriptor2, this);
        }
    }

    public FileDescriptor getFileDescriptor() {
        return this.mFd;
    }

    public int getNativeFd() {
        FileDescriptor fileDescriptor = this.mFd;
        if (fileDescriptor == null) {
            return -1;
        }
        return fileDescriptor.getInt$();
    }

    public static ReliableFileDescriptor[] createPipe2() throws OsHelperErrnoException {
        try {
            FileDescriptor[] createPipe2 = OsHelper.createPipe2(524288);
            return new ReliableFileDescriptor[]{new ReliableFileDescriptor(createPipe2[0]), new ReliableFileDescriptor(createPipe2[1])};
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to createPipe message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            throw e;
        }
    }

    public static ReliableFileDescriptor[] createReliablePipe() throws OsHelperErrnoException, IOException {
        try {
            FileDescriptor[] createSocketPair = createSocketPair();
            FileDescriptor[] createPipe2 = OsHelper.createPipe2(524288);
            return new ReliableFileDescriptor[]{new ReliableFileDescriptor(createPipe2[0], createSocketPair[0]), new ReliableFileDescriptor(createPipe2[1], createSocketPair[1])};
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to createReliablePipe message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            throw e;
        } catch (IOException e2) {
            HiLog.error(TAG, "fail to createReliablePipe message = %s", e2.getMessage());
            throw e2;
        }
    }

    private static void setFdOwner(FileDescriptor fileDescriptor, Object obj) {
        if (fileDescriptor != null && obj != null) {
            if (fileDescriptor.getOwnerId$() != 0) {
                HiLog.error(TAG, "original owner is overwritten", new Object[0]);
            }
            fileDescriptor.setOwnerId$(generateFdOwnerId(obj));
        }
    }

    private static long generateFdOwnerId(Object obj) {
        if (obj == null) {
            return 0;
        }
        return ((long) System.identityHashCode(obj)) | -72057594037927936L;
    }

    private static FileDescriptor[] createSocketPair() throws OsHelperErrnoException, IOException {
        FileDescriptor fileDescriptor = new FileDescriptor();
        FileDescriptor fileDescriptor2 = new FileDescriptor();
        try {
            OsHelper.createSocketPair(1, NetManager.CallbackHandler.CALLBACK_PRECHECK, 0, fileDescriptor, fileDescriptor2);
            IoManager.setFileBlocking(fileDescriptor, false);
            IoManager.setFileBlocking(fileDescriptor2, false);
            return new FileDescriptor[]{fileDescriptor, fileDescriptor2};
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to createSocketPair message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            throw e;
        } catch (IOException e2) {
            HiLog.error(TAG, "fail to setFileBlocking message = %s", e2.getMessage());
            throw e2;
        }
    }

    private static ReliableFileDescriptor takeChargeOfFileDescriptor(FileDescriptor fileDescriptor) throws OsHelperErrnoException, IOException {
        try {
            FileDescriptor fileDescriptor2 = new FileDescriptor();
            fileDescriptor2.setInt$(OsHelper.manipulateFileDescriptor(fileDescriptor, OsHelperConstants.F_DUPFD_CLOEXEC, 0));
            return new ReliableFileDescriptor(fileDescriptor2);
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to takeChargeOfFileDescriptor message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            throw e;
        }
    }

    public static ReliableFileDescriptor takeChargeOfFileDescriptor(int i) throws OsHelperErrnoException, IOException {
        FileDescriptor fileDescriptor = new FileDescriptor();
        fileDescriptor.setInt$(i);
        return takeChargeOfFileDescriptor(fileDescriptor);
    }

    public static ReliableFileDescriptor dupFromSocket(Socket socket) throws OsHelperErrnoException, IOException {
        FileDescriptor fileDescriptor$ = socket.getFileDescriptor$();
        if (fileDescriptor$ == null) {
            return null;
        }
        try {
            return takeChargeOfFileDescriptor(fileDescriptor$);
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to dupFromSocket message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            throw e;
        } catch (IOException e2) {
            HiLog.error(TAG, "fail to dupFromSocket message = %s", e2.getMessage());
            throw e2;
        }
    }

    public long getFileTotalSize() {
        try {
            FileStat fileStatus = OsHelper.getFileStatus(this.mFd);
            if (OsHelperConstants.isRegularFile(fileStatus.fileMode)) {
                return fileStatus.fileSize;
            }
            return -1;
        } catch (OsHelperErrnoException e) {
            HiLog.error(TAG, "fail to getFileTotalSize message = %s, errno = %d", e.getMessage(), Integer.valueOf(e.getErrnoValue()));
            return -1;
        }
    }
}
