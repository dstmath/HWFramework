package com.android.server;

import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import dalvik.system.SocketTagger;
import java.io.FileDescriptor;
import java.net.SocketException;

public final class NetworkManagementSocketTagger extends SocketTagger {
    private static final boolean LOGD = false;
    public static final String PROP_QTAGUID_ENABLED = "net.qtaguid_enabled";
    private static final String TAG = "NetworkManagementSocketTagger";
    private static ThreadLocal<SocketTags> threadSocketTags;

    public static class SocketTags {
        public int statsTag;
        public int statsUid;

        public SocketTags() {
            this.statsTag = -1;
            this.statsUid = -1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.NetworkManagementSocketTagger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.NetworkManagementSocketTagger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkManagementSocketTagger.<clinit>():void");
    }

    private static native int native_deleteTagData(int i, int i2);

    private static native int native_setCounterSet(int i, int i2);

    private static native int native_tagSocketFd(FileDescriptor fileDescriptor, int i, int i2);

    private static native int native_untagSocketFd(FileDescriptor fileDescriptor);

    public static void install() {
        SocketTagger.set(new NetworkManagementSocketTagger());
    }

    public static void setThreadSocketStatsTag(int tag) {
        ((SocketTags) threadSocketTags.get()).statsTag = tag;
    }

    public static int getThreadSocketStatsTag() {
        return ((SocketTags) threadSocketTags.get()).statsTag;
    }

    public static void setThreadSocketStatsUid(int uid) {
        ((SocketTags) threadSocketTags.get()).statsUid = uid;
    }

    public void tag(FileDescriptor fd) throws SocketException {
        SocketTags options = (SocketTags) threadSocketTags.get();
        tagSocketFd(fd, options.statsTag, options.statsUid);
    }

    private void tagSocketFd(FileDescriptor fd, int tag, int uid) {
        if (!(tag == -1 && uid == -1) && SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, LOGD)) {
            int errno = native_tagSocketFd(fd, tag, uid);
            if (errno < 0) {
                Log.i(TAG, "tagSocketFd(" + fd.getInt$() + ", " + tag + ", " + uid + ") failed with errno" + errno);
            }
        }
    }

    public void untag(FileDescriptor fd) throws SocketException {
        unTagSocketFd(fd);
    }

    private void unTagSocketFd(FileDescriptor fd) {
        SocketTags options = (SocketTags) threadSocketTags.get();
        if (!(options.statsTag == -1 && options.statsUid == -1) && SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, LOGD)) {
            int errno = native_untagSocketFd(fd);
            if (errno < 0) {
                Log.w(TAG, "untagSocket(" + fd.getInt$() + ") failed with errno " + errno);
            }
        }
    }

    public static void setKernelCounterSet(int uid, int counterSet) {
        if (SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, LOGD)) {
            int errno = native_setCounterSet(counterSet, uid);
            if (errno < 0) {
                Log.w(TAG, "setKernelCountSet(" + uid + ", " + counterSet + ") failed with errno " + errno);
            }
        }
    }

    public static void resetKernelUidStats(int uid) {
        if (SystemProperties.getBoolean(PROP_QTAGUID_ENABLED, LOGD)) {
            int errno = native_deleteTagData(0, uid);
            if (errno < 0) {
                Slog.w(TAG, "problem clearing counters for uid " + uid + " : errno " + errno);
            }
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
