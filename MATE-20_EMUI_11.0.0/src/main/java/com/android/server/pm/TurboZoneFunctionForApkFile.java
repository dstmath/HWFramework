package com.android.server.pm;

import android.content.pm.PackageInstaller;
import android.util.Slog;
import java.io.FileDescriptor;

public class TurboZoneFunctionForApkFile {
    private static final long BLOCK_SIZE = 4096;
    private static final String TAG = "TurboZone";
    private static boolean isLoadLibraryFailed;

    private static native int nativeGetTzEnable();

    private static native int nativeGetTzFreeBlocks(FileDescriptor fileDescriptor);

    private static native int nativeSetTzKeyFile(FileDescriptor fileDescriptor);

    static {
        isLoadLibraryFailed = false;
        try {
            System.loadLibrary("turbozonefunctionforapkfile_jni");
            isLoadLibraryFailed = false;
            Slog.d(TAG, "libturbozonefunctionforapkfile_jni library Load Success!");
        } catch (UnsatisfiedLinkError e) {
            isLoadLibraryFailed = true;
            Slog.e(TAG, "libturbozonefunctionforapkfile_jni library not found!");
        }
    }

    public static void setTurboZoneKeyFileFlag(FileDescriptor targetFd, Long lengthBytes, PackageInstaller.SessionParams params) {
        if (isLoadLibraryFailed || targetFd == null) {
            Slog.i(TAG, "TurboZone: apk install processing: LoadLibrary " + isLoadLibraryFailed + "\n");
        } else if ((params.installFlags & 16) != 0) {
            int freeBlocks = nativeGetTzFreeBlocks(targetFd);
            long freeBlocksBytes = ((long) freeBlocks) * BLOCK_SIZE;
            Slog.d(TAG, "TurboZone: freeBlocksBytes " + freeBlocksBytes + " freeBlocks " + freeBlocks + " apklengthBytes " + lengthBytes);
            if (freeBlocksBytes <= 0 || lengthBytes.longValue() <= 0 || lengthBytes.longValue() >= freeBlocksBytes) {
                Slog.e(TAG, "TurboZone: No enough space when move apk! \n");
            } else if (nativeSetTzKeyFile(targetFd) != 0) {
                Slog.e(TAG, "TurboZone: Set apk KEY FILE Fail \n");
            }
        }
    }
}
