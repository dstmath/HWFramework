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

    /* access modifiers changed from: private */
    public static class PinnedFile {
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

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0106 A[SYNTHETIC, Splitter:B:52:0x0106] */
    private PinnedFile pinFile(String fileToPin, long offset, long length, long maxSize) {
        String str;
        FileDescriptor fd;
        String str2;
        ErrnoException e;
        long length2;
        long address;
        FileDescriptor fd2 = new FileDescriptor();
        try {
            fd = Os.open(fileToPin, OsConstants.O_RDONLY | OsConstants.O_CLOEXEC | OsConstants.O_NOFOLLOW, OsConstants.O_RDONLY);
            try {
                StructStat sb = Os.fstat(fd);
                if (offset + length > sb.st_size) {
                    try {
                        Os.close(fd);
                        AwareLog.e(TAG, "Failed to pin file " + fileToPin + ", request extends beyond end of file.  offset + length =  " + (offset + length) + ", file length = " + sb.st_size);
                        return null;
                    } catch (ErrnoException e2) {
                        e = e2;
                        str2 = TAG;
                        str = "Could not pin file ";
                        AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
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
                    if (maxSize <= 0 || length2 <= maxSize) {
                        try {
                            try {
                                address = Os.mmap(0, length2, OsConstants.PROT_READ, OsConstants.MAP_PRIVATE, fd, offset);
                                Os.close(fd);
                            } catch (ErrnoException e3) {
                                e = e3;
                                str2 = TAG;
                                str = "Could not pin file ";
                                AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                                if (fd.valid()) {
                                }
                                return null;
                            }
                            try {
                                Os.mlock(address, length2);
                                str2 = TAG;
                                str = "Could not pin file ";
                            } catch (ErrnoException e4) {
                                e = e4;
                                str2 = TAG;
                                str = "Could not pin file ";
                                AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                                if (fd.valid()) {
                                }
                                return null;
                            }
                        } catch (ErrnoException e5) {
                            e = e5;
                            str = "Could not pin file ";
                            str2 = TAG;
                            AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                            if (fd.valid()) {
                            }
                            return null;
                        }
                        try {
                            return new PinnedFile(address, length2, fileToPin);
                        } catch (ErrnoException e6) {
                            e = e6;
                            AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                            if (fd.valid()) {
                                try {
                                    Os.close(fd);
                                } catch (ErrnoException eClose) {
                                    AwareLog.e(str2, "Failed to close fd, error = " + eClose.getMessage());
                                }
                            }
                            return null;
                        }
                    } else {
                        try {
                            AwareLog.e(TAG, "Could not pin file " + fileToPin + ", size = " + length2 + ", maxSize = " + maxSize);
                            Os.close(fd);
                            return null;
                        } catch (ErrnoException e7) {
                            e = e7;
                            str = "Could not pin file ";
                            str2 = TAG;
                            AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                            if (fd.valid()) {
                            }
                            return null;
                        }
                    }
                }
            } catch (ErrnoException e8) {
                e = e8;
                str2 = TAG;
                str = "Could not pin file ";
                AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
                if (fd.valid()) {
                }
                return null;
            }
        } catch (ErrnoException e9) {
            e = e9;
            str2 = TAG;
            str = "Could not pin file ";
            fd = fd2;
            AwareLog.e(str2, str + fileToPin + " with error " + e.getMessage());
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

    public void addPinFile() {
        ArrayList<String> filesToPin = MemoryConstant.getFilesToPin();
        int fileNumSize = filesToPin.size();
        for (int i = 0; i < fileNumSize; i++) {
            PinnedFile pf = pinFile(filesToPin.get(i), 0, 0, 0);
            if (pf != null) {
                this.mPinnedFiles.add(pf);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "Pinned file " + pf.mFilename + "ok");
                }
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
