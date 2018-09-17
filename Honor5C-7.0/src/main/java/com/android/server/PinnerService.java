package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class PinnerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "PinnerService";
    private BinderService mBinderService;
    private final Context mContext;
    private final ArrayList<String> mPinnedFiles;

    private final class BinderService extends Binder {
        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            PinnerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", PinnerService.TAG);
            pw.println("Pinned Files:");
            for (int i = 0; i < PinnerService.this.mPinnedFiles.size(); i++) {
                pw.println((String) PinnerService.this.mPinnedFiles.get(i));
            }
        }
    }

    public PinnerService(Context context) {
        super(context);
        this.mPinnedFiles = new ArrayList();
        this.mContext = context;
    }

    public void onStart() {
        Slog.e(TAG, "Starting PinnerService");
        this.mBinderService = new BinderService();
        publishBinderService("pinner", this.mBinderService);
        String[] filesToPin = this.mContext.getResources().getStringArray(17236066);
        for (int i = 0; i < filesToPin.length; i++) {
            if (pinFile(filesToPin[i], 0, 0)) {
                this.mPinnedFiles.add(filesToPin[i]);
                Slog.i(TAG, "Pinned file = " + filesToPin[i]);
            } else {
                Slog.e(TAG, "Failed to pin file = " + filesToPin[i]);
            }
        }
    }

    private boolean pinFile(String fileToPin, long offset, long length) {
        FileDescriptor fd = new FileDescriptor();
        try {
            fd = Os.open(fileToPin, (OsConstants.O_RDONLY | OsConstants.O_CLOEXEC) | OsConstants.O_NOFOLLOW, OsConstants.O_RDONLY);
            StructStat sb = Os.fstat(fd);
            if (offset + length > sb.st_size) {
                Os.close(fd);
                return DEBUG;
            }
            if (length == 0) {
                length = sb.st_size - offset;
            }
            long address = Os.mmap(0, length, OsConstants.PROT_READ, OsConstants.MAP_PRIVATE, fd, offset);
            Os.close(fd);
            Os.mlock(address, length);
            return true;
        } catch (ErrnoException e) {
            Slog.e(TAG, "Failed to pin file " + fileToPin + " with error " + e.getMessage());
            if (fd.valid()) {
                try {
                    Os.close(fd);
                } catch (ErrnoException eClose) {
                    Slog.e(TAG, "Failed to close fd, error = " + eClose.getMessage());
                }
            }
            return DEBUG;
        }
    }
}
