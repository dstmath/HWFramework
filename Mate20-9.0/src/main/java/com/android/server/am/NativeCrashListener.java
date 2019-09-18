package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.system.UnixSocketAddress;
import android.util.Slog;
import com.android.server.UiModeManagerService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;

final class NativeCrashListener extends Thread {
    static final boolean DEBUG = false;
    static final String DEBUGGERD_SOCKET_PATH = "/data/system/ndebugsocket";
    static final boolean MORE_DEBUG = false;
    static final long SOCKET_TIMEOUT_MILLIS = 10000;
    static final String TAG = "NativeCrashListener";
    final ActivityManagerService mAm;

    class NativeCrashReporter extends Thread {
        ProcessRecord mApp;
        String mCrashReport;
        int mSignal;

        NativeCrashReporter(ProcessRecord app, int signal, String report) {
            super("NativeCrashReport");
            this.mApp = app;
            this.mSignal = signal;
            this.mCrashReport = report;
        }

        public void run() {
            try {
                ApplicationErrorReport.CrashInfo ci = new ApplicationErrorReport.CrashInfo();
                ci.exceptionClassName = "Native crash";
                ci.exceptionMessage = Os.strsignal(this.mSignal);
                ci.throwFileName = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                ci.throwClassName = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                ci.throwMethodName = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                ci.stackTrace = this.mCrashReport;
                NativeCrashListener.this.mAm.handleApplicationCrashInner("native_crash", this.mApp, this.mApp.processName, ci);
            } catch (Exception e) {
                Slog.e(NativeCrashListener.TAG, "Unable to report native crash", e);
            }
        }
    }

    NativeCrashListener(ActivityManagerService am) {
        super(TAG);
        this.mAm = am;
    }

    public void run() {
        byte[] ackSignal = new byte[1];
        File socketFile = new File(DEBUGGERD_SOCKET_PATH);
        if (socketFile.exists()) {
            socketFile.delete();
        }
        try {
            FileDescriptor serverFd = Os.socket(OsConstants.AF_UNIX, OsConstants.SOCK_STREAM, 0);
            Os.bind(serverFd, UnixSocketAddress.createFileSystem(DEBUGGERD_SOCKET_PATH));
            Os.listen(serverFd, 1);
            Os.chmod(DEBUGGERD_SOCKET_PATH, 511);
            while (true) {
                try {
                    FileDescriptor peerFd = Os.accept(serverFd, null);
                    if (peerFd != null) {
                        consumeNativeCrashData(peerFd);
                    }
                    if (peerFd != null) {
                        try {
                            Os.write(peerFd, ackSignal, 0, 1);
                        } catch (Exception e) {
                        }
                        try {
                            Os.close(peerFd);
                        } catch (ErrnoException e2) {
                        }
                    }
                } catch (Exception e3) {
                    Slog.w(TAG, "Error handling connection", e3);
                    if (0 != 0) {
                        try {
                            Os.write(null, ackSignal, 0, 1);
                        } catch (Exception e4) {
                        }
                        Os.close(null);
                    }
                }
            }
        } catch (Exception e5) {
            try {
                Slog.e(TAG, "Unable to init native debug socket!", e5);
            } finally {
                File socketFile2 = new File(DEBUGGERD_SOCKET_PATH);
                if (socketFile2.exists()) {
                    socketFile2.delete();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    Os.write(null, ackSignal, 0, 1);
                } catch (Exception e6) {
                }
                try {
                    Os.close(null);
                } catch (ErrnoException e7) {
                }
            }
            throw th;
        }
    }

    static int unpackInt(byte[] buf, int offset) {
        return ((buf[offset] & 255) << 24) | ((buf[offset + 1] & 255) << 16) | ((buf[offset + 2] & 255) << 8) | (buf[offset + 3] & 255);
    }

    static int readExactly(FileDescriptor fd, byte[] buffer, int offset, int numBytes) throws ErrnoException, InterruptedIOException {
        int totalRead = 0;
        while (numBytes > 0) {
            int n = Os.read(fd, buffer, offset + totalRead, numBytes);
            if (n <= 0) {
                return -1;
            }
            numBytes -= n;
            totalRead += n;
        }
        return totalRead;
    }

    /* access modifiers changed from: package-private */
    public void consumeNativeCrashData(FileDescriptor fd) {
        ProcessRecord pr;
        byte[] buf = new byte[4096];
        ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
        try {
            StructTimeval timeout = StructTimeval.fromMillis(10000);
            Os.setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeout);
            Os.setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, timeout);
            if (readExactly(fd, buf, 0, 8) != 8) {
                Slog.e(TAG, "Unable to read from debuggerd");
                return;
            }
            int pid = unpackInt(buf, 0);
            int signal = unpackInt(buf, 4);
            if (pid > 0) {
                synchronized (this.mAm.mPidsSelfLocked) {
                    pr = this.mAm.mPidsSelfLocked.get(pid);
                }
                if (pr == null) {
                    Slog.w(TAG, "Couldn't find ProcessRecord for pid " + pid);
                } else if (!pr.persistent) {
                    while (true) {
                        int bytes = Os.read(fd, buf, 0, buf.length);
                        if (bytes > 0) {
                            if (buf[bytes - 1] == 0) {
                                os.write(buf, 0, bytes - 1);
                                break;
                            } else {
                                os.write(buf, 0, bytes);
                                continue;
                            }
                        }
                        if (bytes <= 0) {
                            break;
                        }
                    }
                    synchronized (this.mAm) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        pr.crashing = true;
                        pr.forceCrashReport = true;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    new NativeCrashReporter(pr, signal, new String(os.toByteArray(), "UTF-8")).start();
                }
            } else {
                Slog.e(TAG, "Bogus pid!");
            }
        } catch (Exception e) {
            Slog.e(TAG, "Exception dealing with report", e);
        } catch (Throwable th) {
            while (true) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }
}
