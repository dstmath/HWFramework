package com.android.server;

import android.content.Context;
import android.net.INetworkStatsService;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import dalvik.system.SocketTagger;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketException;

public final class NetworkManagementSocketTagger extends SocketTagger {
    private static final boolean LOGD = false;
    public static final String PROP_QTAGUID_ENABLED = "net.qtaguid_enabled";
    private static final String TAG = "NetworkManagementSocketTagger";
    private static INetworkStatsService sStatsService;
    private static ThreadLocal<SocketTags> threadSocketTags = new ThreadLocal<SocketTags>() {
        /* class com.android.server.NetworkManagementSocketTagger.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public SocketTags initialValue() {
            return new SocketTags();
        }
    };

    public static class SocketTags {
        public int statsTag = -1;
        public int statsUid = -1;
    }

    private static native int native_deleteTagData(int i, int i2);

    private static native int native_setCounterSet(int i, int i2);

    private static native int native_tagSocketFd(FileDescriptor fileDescriptor, int i, int i2);

    private static native int native_untagSocketFd(FileDescriptor fileDescriptor);

    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (NetworkManagementSocketTagger.class) {
            if (sStatsService == null) {
                sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            }
            iNetworkStatsService = sStatsService;
        }
        return iNetworkStatsService;
    }

    public static void install() {
        SocketTagger.set(new NetworkManagementSocketTagger());
    }

    public static int setThreadSocketStatsTag(int tag) {
        int old = threadSocketTags.get().statsTag;
        threadSocketTags.get().statsTag = tag;
        return old;
    }

    public static int getThreadSocketStatsTag() {
        return threadSocketTags.get().statsTag;
    }

    public static int setThreadSocketStatsUid(int uid) {
        int old = threadSocketTags.get().statsUid;
        threadSocketTags.get().statsUid = uid;
        return old;
    }

    public static int getThreadSocketStatsUid() {
        return threadSocketTags.get().statsUid;
    }

    public void tag(FileDescriptor fd) throws SocketException {
        SocketTags options = threadSocketTags.get();
        if (options.statsTag == -1 && StrictMode.vmUntaggedSocketEnabled()) {
            StrictMode.onUntaggedSocket();
        }
        tagSocketFd(fd, options.statsTag, options.statsUid);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0039, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003a, code lost:
        if (r0 != null) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0040, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0041, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0044, code lost:
        throw r4;
     */
    private void tagSocketFd(FileDescriptor fd, int tag, int uid) {
        if (tag != -1 || uid != -1) {
            int myUid = Process.myUid();
            if (!SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, false)) {
                return;
            }
            if (uid == -1 || myUid == uid || myUid == 1000) {
                int errno = native_tagSocketFd(fd, tag, uid);
                if (errno < 0) {
                    Log.i(TAG, "tagSocketFd(" + fd.getInt$() + ", " + tag + ", " + uid + ") failed with errno" + errno);
                    return;
                }
                return;
            }
            try {
                ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(fd);
                if (getStatsService().tagSocket(pfd, tag, uid) < 0) {
                    Log.i(TAG, "Process does not have permission to tagother uid on socket");
                }
                if (pfd != null) {
                    pfd.close();
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (IOException e2) {
                Log.i(TAG, "dup socket fd failed");
            }
        }
    }

    public void untag(FileDescriptor fd) throws SocketException {
        unTagSocketFd(fd);
    }

    private void unTagSocketFd(FileDescriptor fd) {
        int errno;
        SocketTags options = threadSocketTags.get();
        if (!(options.statsTag == -1 && options.statsUid == -1) && SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, false) && (errno = native_untagSocketFd(fd)) < 0) {
            Log.w(TAG, "untagSocket(" + fd.getInt$() + ") failed with errno " + errno);
        }
    }

    public static int setKernelCounterSet(int uid, int counterSet) {
        if (!SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, false)) {
            return 0;
        }
        int errno = native_setCounterSet(counterSet, uid);
        if (errno < 0) {
            Log.w(TAG, "setKernelCountSet(" + uid + ", " + counterSet + ") failed with errno " + errno);
        }
        return errno;
    }

    public static void resetKernelUidStats(int uid) {
        int errno;
        if (SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, false) && (errno = native_deleteTagData(0, uid)) < 0) {
            Slog.w(TAG, "problem clearing counters for uid " + uid + " : errno " + errno);
        }
    }

    public static int kernelToTag(String string) {
        int length = string.length();
        if (length > 10) {
            return Long.decode(string.substring(0, length - 8)).intValue();
        }
        return 0;
    }
}
