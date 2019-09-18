package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;

public class MemoryLockFile {
    private static final String TAG = "AwareMem_MemLockFile";
    private final ArrayList<PinnedFile> mPinnedFiles = new ArrayList<>();

    private static class PinnedFile {
        long mAddress;
        String mFilename;
        long mLength;

        PinnedFile(long address, long length, String filename) {
            this.mAddress = address;
            this.mLength = length;
            this.mFilename = normalize(filename);
        }

        private String normalize(String path) {
            return new File(path.trim()).getName();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f8 A[SYNTHETIC, Splitter:B:45:0x00f8] */
    private PinnedFile pinFile(String fileToPin, long offset, long length, long maxSize) {
        FileDescriptor fd;
        long length2;
        long address;
        PinnedFile pinnedFile;
        long length3;
        FileDescriptor fd2;
        String str = fileToPin;
        long j = maxSize;
        FileDescriptor fd3 = new FileDescriptor();
        try {
            fd = Os.open(str, OsConstants.O_RDONLY | OsConstants.O_CLOEXEC | OsConstants.O_NOFOLLOW, OsConstants.O_RDONLY);
            try {
                StructStat sb = Os.fstat(fd);
                if (offset + length > sb.st_size) {
                    try {
                        Os.close(fd);
                        AwareLog.e(TAG, "Failed to pin file " + str + ", request extends beyond end of file.  offset + length =  " + (offset + length) + ", file length = " + sb.st_size);
                        return null;
                    } catch (ErrnoException e) {
                        e = e;
                        long j2 = length;
                        AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
                        if (fd.valid()) {
                        }
                        return null;
                    }
                } else {
                    if (length == 0) {
                        length2 = sb.st_size - offset;
                    } else {
                        length2 = length;
                    }
                    if (j <= 0 || length2 <= j) {
                        try {
                            address = Os.mmap(0, length2, OsConstants.PROT_READ, OsConstants.MAP_PRIVATE, fd, offset);
                            Os.close(fd);
                            Os.mlock(address, length2);
                            pinnedFile = pinnedFile;
                            length3 = length2;
                            fd2 = fd;
                        } catch (ErrnoException e2) {
                            e = e2;
                            FileDescriptor fileDescriptor = fd;
                            long j3 = length2;
                            AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
                            if (fd.valid()) {
                                try {
                                    Os.close(fd);
                                } catch (ErrnoException eClose) {
                                    ErrnoException errnoException = eClose;
                                    AwareLog.e(TAG, "Failed to close fd, error = " + eClose.getMessage());
                                }
                            }
                            return null;
                        }
                        try {
                            pinnedFile = new PinnedFile(address, length3, str);
                            return pinnedFile;
                        } catch (ErrnoException e3) {
                            e = e3;
                            long j4 = length3;
                            fd = fd2;
                            AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
                            if (fd.valid()) {
                            }
                            return null;
                        }
                    } else {
                        try {
                            AwareLog.e(TAG, "Could not pin file " + str + ", size = " + length2 + ", maxSize = " + j);
                            Os.close(fd);
                            return null;
                        } catch (ErrnoException e4) {
                            e = e4;
                            long j5 = length2;
                            AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
                            if (fd.valid()) {
                            }
                            return null;
                        }
                    }
                }
            } catch (ErrnoException e5) {
                e = e5;
                FileDescriptor fileDescriptor2 = fd;
                long j6 = length;
                AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
                if (fd.valid()) {
                }
                return null;
            }
        } catch (ErrnoException e6) {
            e = e6;
            long j7 = length;
            fd = fd3;
            AwareLog.e(TAG, "Could not pin file " + str + " with error " + e.getMessage());
            if (fd.valid()) {
            }
            return null;
        }
    }

    private void unpinFile(PinnedFile pf) {
        try {
            Os.munlock(pf.mAddress, pf.mLength);
        } catch (ErrnoException e) {
            AwareLog.e(TAG, "Failed to unpin file with error " + e.getMessage());
        }
    }

    public void iAwareAddPinFile() {
        ArrayList<String> filesToPin = MemoryConstant.getFilesToPin();
        int fileNumSize = filesToPin.size();
        for (int i = 0; i < fileNumSize; i++) {
            PinnedFile pf = pinFile(filesToPin.get(i), 0, 0, 0);
            if (pf != null) {
                this.mPinnedFiles.add(pf);
                AwareLog.d(TAG, "Pinned file " + pf.mFilename + "ok");
            } else {
                AwareLog.e(TAG, "Failed to pin file");
            }
        }
    }

    public void clearPinFile() {
        int pinnedFileNum = this.mPinnedFiles.size();
        for (int i = 0; i < pinnedFileNum; i++) {
            unpinFile(this.mPinnedFiles.get(i));
        }
        this.mPinnedFiles.clear();
    }
}
