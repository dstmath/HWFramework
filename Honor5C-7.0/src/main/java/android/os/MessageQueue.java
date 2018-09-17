package android.os;

import android.util.Log;
import android.util.Printer;
import android.util.SparseArray;
import java.io.FileDescriptor;
import java.util.ArrayList;

public final class MessageQueue {
    private static final boolean DEBUG = false;
    private static final int OPT_FRAME_DELAY = 16;
    private static final int OPT_QUEUE_COUNT = 300;
    private static final String TAG = "MessageQueue";
    private static boolean mIawareEnabled;
    private boolean mBlocked;
    private SparseArray<FileDescriptorRecord> mFileDescriptorRecords;
    private final ArrayList<IdleHandler> mIdleHandlers;
    private boolean mLastIsVsync;
    private long mLastVsyncEnd;
    int mMessageCount;
    Message mMessages;
    private int mNextBarrierToken;
    private IdleHandler[] mPendingIdleHandlers;
    private long mPtr;
    private final boolean mQuitAllowed;
    private boolean mQuitting;

    public interface IdleHandler {
        boolean queueIdle();
    }

    private static final class FileDescriptorRecord {
        public final FileDescriptor mDescriptor;
        public int mEvents;
        public OnFileDescriptorEventListener mListener;
        public int mSeq;

        public FileDescriptorRecord(FileDescriptor descriptor, int events, OnFileDescriptorEventListener listener) {
            this.mDescriptor = descriptor;
            this.mEvents = events;
            this.mListener = listener;
        }
    }

    public interface OnFileDescriptorEventListener {
        public static final int EVENT_ERROR = 4;
        public static final int EVENT_INPUT = 1;
        public static final int EVENT_OUTPUT = 2;

        int onFileDescriptorEvents(FileDescriptor fileDescriptor, int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.MessageQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.MessageQueue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.MessageQueue.<clinit>():void");
    }

    private static native void nativeDestroy(long j);

    private static native long nativeInit();

    private static native boolean nativeIsPolling(long j);

    private native void nativePollOnce(long j, int i);

    private static native void nativeSetFileDescriptorEvents(long j, int i, int i2);

    private static native void nativeWake(long j);

    MessageQueue(boolean quitAllowed) {
        this.mIdleHandlers = new ArrayList();
        this.mLastIsVsync = DEBUG;
        this.mLastVsyncEnd = 0;
        this.mQuitAllowed = quitAllowed;
        this.mPtr = nativeInit();
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    private void dispose() {
        if (this.mPtr != 0) {
            nativeDestroy(this.mPtr);
            this.mPtr = 0;
            this.mMessageCount = 0;
        }
    }

    public boolean isIdle() {
        boolean z = true;
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            if (this.mMessages != null && now >= this.mMessages.when) {
                z = DEBUG;
            }
        }
        return z;
    }

    public void addIdleHandler(IdleHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Can't add a null IdleHandler");
        }
        synchronized (this) {
            this.mIdleHandlers.add(handler);
        }
    }

    public void removeIdleHandler(IdleHandler handler) {
        synchronized (this) {
            this.mIdleHandlers.remove(handler);
        }
    }

    public boolean isPolling() {
        boolean isPollingLocked;
        synchronized (this) {
            isPollingLocked = isPollingLocked();
        }
        return isPollingLocked;
    }

    private boolean isPollingLocked() {
        return !this.mQuitting ? nativeIsPolling(this.mPtr) : DEBUG;
    }

    public void addOnFileDescriptorEventListener(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else {
            synchronized (this) {
                updateOnFileDescriptorEventListenerLocked(fd, events, listener);
            }
        }
    }

    public void removeOnFileDescriptorEventListener(FileDescriptor fd) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        }
        synchronized (this) {
            updateOnFileDescriptorEventListenerLocked(fd, 0, null);
        }
    }

    private void updateOnFileDescriptorEventListenerLocked(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        int fdNum = fd.getInt$();
        int index = -1;
        FileDescriptorRecord fileDescriptorRecord = null;
        if (this.mFileDescriptorRecords != null) {
            index = this.mFileDescriptorRecords.indexOfKey(fdNum);
            if (index >= 0) {
                fileDescriptorRecord = (FileDescriptorRecord) this.mFileDescriptorRecords.valueAt(index);
                if (fileDescriptorRecord != null && fileDescriptorRecord.mEvents == events) {
                    return;
                }
            }
        }
        if (events != 0) {
            events |= 4;
            if (fileDescriptorRecord == null) {
                if (this.mFileDescriptorRecords == null) {
                    this.mFileDescriptorRecords = new SparseArray();
                }
                this.mFileDescriptorRecords.put(fdNum, new FileDescriptorRecord(fd, events, listener));
            } else {
                fileDescriptorRecord.mListener = listener;
                fileDescriptorRecord.mEvents = events;
                fileDescriptorRecord.mSeq++;
            }
            nativeSetFileDescriptorEvents(this.mPtr, fdNum, events);
        } else if (fileDescriptorRecord != null) {
            fileDescriptorRecord.mEvents = 0;
            this.mFileDescriptorRecords.removeAt(index);
        }
    }

    private int dispatchEvents(int fd, int events) {
        synchronized (this) {
            FileDescriptorRecord record = (FileDescriptorRecord) this.mFileDescriptorRecords.get(fd);
            if (record == null) {
                return 0;
            }
            int oldWatchedEvents = record.mEvents;
            events &= oldWatchedEvents;
            if (events == 0) {
                return oldWatchedEvents;
            }
            OnFileDescriptorEventListener listener = record.mListener;
            int seq = record.mSeq;
            int newWatchedEvents = listener.onFileDescriptorEvents(record.mDescriptor, events);
            if (newWatchedEvents != 0) {
                newWatchedEvents |= 4;
            }
            if (newWatchedEvents != oldWatchedEvents) {
                synchronized (this) {
                    int index = this.mFileDescriptorRecords.indexOfKey(fd);
                    if (index >= 0 && this.mFileDescriptorRecords.valueAt(index) == record && record.mSeq == seq) {
                        record.mEvents = newWatchedEvents;
                        if (newWatchedEvents == 0) {
                            this.mFileDescriptorRecords.removeAt(index);
                        }
                    }
                }
            }
            return newWatchedEvents;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Message next() {
        long ptr = this.mPtr;
        if (ptr == 0) {
            return null;
        }
        Message msg;
        int pendingIdleHandlerCount = -1;
        int nextPollTimeoutMillis = 0;
        loop0:
        while (true) {
            synchronized (this) {
                if (this.mLastIsVsync) {
                    this.mLastVsyncEnd = SystemClock.uptimeMillis();
                }
            }
            if (nextPollTimeoutMillis != 0) {
                Binder.flushPendingCommands();
            }
            nativePollOnce(ptr, nextPollTimeoutMillis);
            synchronized (this) {
                long now = SystemClock.uptimeMillis();
                Message message = null;
                msg = this.mMessages;
                if (!(!mIawareEnabled || msg == null || !msg.isVsync() || msg.next == null || msg.next.target == null)) {
                    if (msg.next.when < msg.when) {
                        if (!this.mLastIsVsync && this.mMessageCount <= OPT_QUEUE_COUNT) {
                            if (now - this.mLastVsyncEnd >= 16 && now - msg.when >= 16) {
                                if (Log.HWLog) {
                                    Log.d(TAG, "VSync First!");
                                }
                            }
                        }
                        message = msg;
                        msg = msg.next;
                    }
                }
                if (msg != null && msg.target == null) {
                    do {
                        message = msg;
                        msg = msg.next;
                        if (msg == null) {
                            break;
                        }
                    } while (!msg.isAsynchronous());
                }
                if (msg != null) {
                    if (now >= msg.when) {
                        break;
                    }
                    nextPollTimeoutMillis = (int) Math.min(msg.when - now, 2147483647L);
                } else {
                    nextPollTimeoutMillis = -1;
                }
                if (this.mQuitting) {
                    dispose();
                    return null;
                }
                if (pendingIdleHandlerCount < 0 && (this.mMessages == null || now < this.mMessages.when)) {
                    pendingIdleHandlerCount = this.mIdleHandlers.size();
                }
                if (pendingIdleHandlerCount <= 0) {
                    this.mBlocked = true;
                } else {
                    if (this.mPendingIdleHandlers == null) {
                        this.mPendingIdleHandlers = new IdleHandler[Math.max(pendingIdleHandlerCount, 4)];
                    }
                    this.mPendingIdleHandlers = (IdleHandler[]) this.mIdleHandlers.toArray(this.mPendingIdleHandlers);
                    for (int i = 0; i < pendingIdleHandlerCount; i++) {
                        IdleHandler idler = this.mPendingIdleHandlers[i];
                        this.mPendingIdleHandlers[i] = null;
                        boolean keep = DEBUG;
                        try {
                            keep = idler.queueIdle();
                        } catch (Throwable t) {
                            Log.wtf(TAG, "IdleHandler threw exception", t);
                        }
                        if (!keep) {
                            synchronized (this) {
                                this.mIdleHandlers.remove(idler);
                            }
                        }
                    }
                    pendingIdleHandlerCount = 0;
                    nextPollTimeoutMillis = 0;
                }
            }
        }
        this.mBlocked = DEBUG;
        if (message != null) {
            message.next = msg.next;
        } else {
            this.mMessages = msg.next;
        }
        msg.next = null;
        msg.markInUse();
        this.mMessageCount--;
        if (mIawareEnabled) {
            this.mLastIsVsync = msg.isVsync();
        }
        return msg;
    }

    void quit(boolean safe) {
        if (this.mQuitAllowed) {
            synchronized (this) {
                if (this.mQuitting) {
                    return;
                }
                this.mQuitting = true;
                if (safe) {
                    removeAllFutureMessagesLocked();
                } else {
                    removeAllMessagesLocked();
                }
                nativeWake(this.mPtr);
                return;
            }
        }
        throw new IllegalStateException("Main thread not allowed to quit.");
    }

    public int postSyncBarrier() {
        return postSyncBarrier(SystemClock.uptimeMillis());
    }

    private int postSyncBarrier(long when) {
        int token;
        synchronized (this) {
            token = this.mNextBarrierToken;
            this.mNextBarrierToken = token + 1;
            Message msg = Message.obtain();
            msg.markInUse();
            msg.when = when;
            msg.arg1 = token;
            Message message = null;
            Message p = this.mMessages;
            if (when != 0) {
                while (p != null && p.when <= when) {
                    message = p;
                    p = p.next;
                }
            }
            if (message != null) {
                msg.next = p;
                message.next = msg;
            } else {
                msg.next = p;
                this.mMessages = msg;
            }
            this.mMessageCount++;
        }
        return token;
    }

    public void removeSyncBarrier(int token) {
        synchronized (this) {
            Message prev = null;
            Message p = this.mMessages;
            while (p != null && (p.target != null || p.arg1 != token)) {
                prev = p;
                p = p.next;
            }
            if (p == null) {
                throw new IllegalStateException("The specified message queue synchronization  barrier token has not been posted or has already been removed.");
            }
            boolean needWake;
            if (prev != null) {
                prev.next = p.next;
                needWake = DEBUG;
            } else {
                this.mMessages = p.next;
                needWake = (this.mMessages == null || this.mMessages.target != null) ? true : DEBUG;
            }
            p.recycleUnchecked();
            this.mMessageCount--;
            if (needWake && !this.mQuitting) {
                nativeWake(this.mPtr);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean enqueueMessage(Message msg, long when) {
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        } else if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        } else {
            synchronized (this) {
                if (this.mQuitting) {
                    IllegalStateException e = new IllegalStateException(msg.target + " sending message to a Handler on a dead thread");
                    Log.w(TAG, e.getMessage(), e);
                    msg.recycle();
                    return DEBUG;
                }
                boolean z;
                msg.markInUse();
                msg.when = when;
                Message p = this.mMessages;
                if (mIawareEnabled && msg.isVsync()) {
                    msg.next = p;
                    this.mMessages = msg;
                    z = this.mBlocked;
                } else if (p == null || when == 0 || when < p.when) {
                    msg.next = p;
                    this.mMessages = msg;
                    z = this.mBlocked;
                } else {
                    z = (this.mBlocked && p.target == null) ? msg.isAsynchronous() : DEBUG;
                    while (true) {
                        Message prev = p;
                        p = p.next;
                        if (p == null || when < p.when) {
                            msg.next = p;
                            prev.next = msg;
                        } else if (z) {
                            if (p.isAsynchronous()) {
                                z = DEBUG;
                            }
                        }
                    }
                }
                this.mMessageCount++;
                if (z) {
                    nativeWake(this.mPtr);
                }
                return true;
            }
        }
    }

    boolean hasMessages(Handler h, int what, Object object) {
        if (h == null) {
            return DEBUG;
        }
        synchronized (this) {
            Message p = this.mMessages;
            while (p != null) {
                if (p.target == h && p.what == what && (object == null || p.obj == object)) {
                    return true;
                }
                p = p.next;
            }
            return DEBUG;
        }
    }

    boolean hasMessages(Handler h, Runnable r, Object object) {
        if (h == null) {
            return DEBUG;
        }
        synchronized (this) {
            Message p = this.mMessages;
            while (p != null) {
                if (p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    return true;
                }
                p = p.next;
            }
            return DEBUG;
        }
    }

    void removeMessages(Handler h, int what, Object object) {
        if (h != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && p.what == what && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && n.what == what && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    void removeMessages(Handler h, Runnable r, Object object) {
        if (h != null && r != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && n.callback == r && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    void removeCallbacksAndMessages(Handler h, Object object) {
        if (h != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    private void removeAllMessagesLocked() {
        Message p = this.mMessages;
        while (p != null) {
            Message n = p.next;
            p.recycleUnchecked();
            p = n;
        }
        this.mMessages = null;
        this.mMessageCount = 0;
    }

    private void removeAllFutureMessagesLocked() {
        long now = SystemClock.uptimeMillis();
        Message p = this.mMessages;
        if (p != null) {
            if (p.when <= now) {
                Message n;
                while (true) {
                    n = p.next;
                    if (n != null) {
                        if (n.when > now) {
                            break;
                        }
                        p = n;
                    } else {
                        return;
                    }
                }
                p.next = null;
                while (true) {
                    p = n;
                    n = n.next;
                    p.recycleUnchecked();
                    this.mMessageCount--;
                    if (n == null) {
                        break;
                    }
                }
            } else {
                removeAllMessagesLocked();
            }
        }
    }

    void dump(Printer pw, String prefix) {
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            int n = 0;
            for (Message msg = this.mMessages; msg != null; msg = msg.next) {
                pw.println(prefix + "Message " + n + ": " + msg.toString(now));
                n++;
            }
            pw.println(prefix + "(Total messages: " + n + ", polling=" + isPollingLocked() + ", quitting=" + this.mQuitting + ")");
        }
    }
}
