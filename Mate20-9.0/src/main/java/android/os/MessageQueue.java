package android.os;

import android.util.Log;
import android.util.Printer;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import java.io.FileDescriptor;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public final class MessageQueue {
    private static final boolean DEBUG = false;
    private static final int OPT_FRAME_DELAY = 16;
    private static final int OPT_QUEUE_COUNT = 300;
    private static final String TAG = "MessageQueue";
    private static boolean mIawareEnabled;
    private boolean mBlocked;
    private SparseArray<FileDescriptorRecord> mFileDescriptorRecords;
    private final ArrayList<IdleHandler> mIdleHandlers = new ArrayList<>();
    private boolean mIsReduceDelay = false;
    private boolean mLastIsVsync = false;
    public long mLastVsyncEnd = 0;
    int mMessageCount;
    Message mMessages;
    private int mNextBarrierToken;
    private IdleHandler[] mPendingIdleHandlers;
    private long mPtr;
    private final boolean mQuitAllowed;
    private boolean mQuitting;

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

    public interface IdleHandler {
        boolean queueIdle();
    }

    public interface OnFileDescriptorEventListener {
        public static final int EVENT_ERROR = 4;
        public static final int EVENT_INPUT = 1;
        public static final int EVENT_OUTPUT = 2;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Events {
        }

        int onFileDescriptorEvents(FileDescriptor fileDescriptor, int i);
    }

    private static native void nativeDestroy(long j);

    private static native long nativeInit();

    private static native boolean nativeIsPolling(long j);

    private native void nativePollOnce(long j, int i);

    private static native void nativeSetFileDescriptorEvents(long j, int i, int i2);

    private static native void nativeWake(long j);

    static {
        boolean z = false;
        mIawareEnabled = false;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && SystemProperties.getBoolean("persist.sys.iaware.vsyncfirst", true)) {
            z = true;
        }
        mIawareEnabled = z;
    }

    MessageQueue(boolean quitAllowed) {
        this.mQuitAllowed = quitAllowed;
        this.mPtr = nativeInit();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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

    public void enableReduceDelay(boolean enabled) {
        this.mIsReduceDelay = mIawareEnabled && enabled;
    }

    public boolean isReduceDelay() {
        return this.mIsReduceDelay;
    }

    public void setLastFrameDoneTime(long lastFrameDoneTime) {
        this.mLastVsyncEnd = lastFrameDoneTime;
    }

    public boolean isIdle() {
        boolean z;
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            if (this.mMessages != null) {
                if (now >= this.mMessages.when) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public void addIdleHandler(IdleHandler handler) {
        if (handler != null) {
            synchronized (this) {
                this.mIdleHandlers.add(handler);
            }
            return;
        }
        throw new NullPointerException("Can't add a null IdleHandler");
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
        return !this.mQuitting && nativeIsPolling(this.mPtr);
    }

    public void addOnFileDescriptorEventListener(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        } else if (listener != null) {
            synchronized (this) {
                updateOnFileDescriptorEventListenerLocked(fd, events, listener);
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }

    public void removeOnFileDescriptorEventListener(FileDescriptor fd) {
        if (fd != null) {
            synchronized (this) {
                updateOnFileDescriptorEventListenerLocked(fd, 0, null);
            }
            return;
        }
        throw new IllegalArgumentException("fd must not be null");
    }

    private void updateOnFileDescriptorEventListenerLocked(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        int fdNum = fd.getInt$();
        int index = -1;
        FileDescriptorRecord record = null;
        if (this.mFileDescriptorRecords != null) {
            index = this.mFileDescriptorRecords.indexOfKey(fdNum);
            if (index >= 0) {
                record = this.mFileDescriptorRecords.valueAt(index);
                if (record != null && record.mEvents == events) {
                    return;
                }
            }
        }
        if (events != 0) {
            int events2 = events | 4;
            if (record == null) {
                if (this.mFileDescriptorRecords == null) {
                    this.mFileDescriptorRecords = new SparseArray<>();
                }
                this.mFileDescriptorRecords.put(fdNum, new FileDescriptorRecord(fd, events2, listener));
            } else {
                record.mListener = listener;
                record.mEvents = events2;
                record.mSeq++;
            }
            nativeSetFileDescriptorEvents(this.mPtr, fdNum, events2);
        } else if (record != null) {
            record.mEvents = 0;
            this.mFileDescriptorRecords.removeAt(index);
            nativeSetFileDescriptorEvents(this.mPtr, fdNum, 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        r4 = r2.onFileDescriptorEvents(r0.mDescriptor, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        if (r4 == 0) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        r4 = r4 | 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        if (r4 == r1) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r5 = r7.mFileDescriptorRecords.indexOfKey(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002d, code lost:
        if (r5 < 0) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0035, code lost:
        if (r7.mFileDescriptorRecords.valueAt(r5) != r0) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0039, code lost:
        if (r0.mSeq != r3) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003b, code lost:
        r0.mEvents = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003d, code lost:
        if (r4 != 0) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        r7.mFileDescriptorRecords.removeAt(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0044, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0049, code lost:
        return r4;
     */
    private int dispatchEvents(int fd, int events) {
        synchronized (this) {
            FileDescriptorRecord record = this.mFileDescriptorRecords.get(fd);
            if (record == null) {
                return 0;
            }
            int oldWatchedEvents = record.mEvents;
            int events2 = events & oldWatchedEvents;
            if (events2 == 0) {
                return oldWatchedEvents;
            }
            OnFileDescriptorEventListener listener = record.mListener;
            int seq = record.mSeq;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00df, code lost:
        r0 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00fd, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00fe, code lost:
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00ff, code lost:
        if (r5 >= r6) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0101, code lost:
        r7 = r1.mPendingIdleHandlers[r5];
        r1.mPendingIdleHandlers[r5] = null;
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0110, code lost:
        r10 = r7.queueIdle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0112, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0113, code lost:
        r11 = r0;
        android.util.Log.wtf(TAG, "IdleHandler threw exception", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x012b, code lost:
        r6 = 0;
        r0 = 0;
     */
    public Message next() {
        int nextPollTimeoutMillis;
        int i;
        IdleHandler idler;
        boolean keep;
        long ptr = this.mPtr;
        if (ptr == 0) {
            return null;
        }
        int pendingIdleHandlerCount = -1;
        int pendingIdleHandlerCount2 = 0;
        while (true) {
            int nextPollTimeoutMillis2 = pendingIdleHandlerCount2;
            if (nextPollTimeoutMillis2 != 0) {
                Binder.flushPendingCommands();
            }
            nativePollOnce(ptr, nextPollTimeoutMillis2);
            synchronized (this) {
                try {
                    long now = SystemClock.uptimeMillis();
                    Message prevMsg = null;
                    Message vsyncMsg = null;
                    Message msg = this.mMessages;
                    if (mIawareEnabled && msg != null && msg.isVsync() && msg.next != null && msg.next.when < msg.when) {
                        if (this.mMessageCount <= 300 && now - this.mLastVsyncEnd >= 4) {
                            if (now - msg.when >= 16) {
                                if (Log.HWLog) {
                                    Log.d(TAG, "VSync First!");
                                }
                            }
                        }
                        vsyncMsg = msg;
                        prevMsg = msg;
                        msg = msg.next;
                    }
                    if (msg != null && msg.target == null) {
                        while (true) {
                            prevMsg = msg;
                            msg = msg.next;
                            if (vsyncMsg == null || (msg != null && (msg == null || msg.when <= vsyncMsg.when))) {
                                if (msg != null) {
                                    if (msg.isAsynchronous()) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        msg = vsyncMsg;
                        prevMsg = null;
                    }
                    if (msg == null) {
                        nextPollTimeoutMillis = -1;
                    } else if (now < msg.when) {
                        nextPollTimeoutMillis = (int) Math.min(msg.when - now, 2147483647L);
                    } else {
                        this.mBlocked = false;
                        if (prevMsg != null) {
                            prevMsg.next = msg.next;
                        } else {
                            this.mMessages = msg.next;
                        }
                        msg.next = null;
                        msg.markInUse();
                        this.mMessageCount--;
                        return msg;
                    }
                    try {
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
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i2 = nextPollTimeoutMillis;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
        if (!keep) {
            synchronized (this) {
                this.mIdleHandlers.remove(idler);
            }
        }
        int i3 = i + 1;
    }

    /* access modifiers changed from: package-private */
    public void quit(boolean safe) {
        if (this.mQuitAllowed) {
            synchronized (this) {
                if (!this.mQuitting) {
                    this.mQuitting = true;
                    if (safe) {
                        removeAllFutureMessagesLocked();
                    } else {
                        removeAllMessagesLocked();
                    }
                    nativeWake(this.mPtr);
                    return;
                }
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
            Message prev = null;
            Message p = this.mMessages;
            if (when != 0) {
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
            }
            if (prev != null) {
                msg.next = p;
                prev.next = msg;
            } else {
                msg.next = p;
                this.mMessages = msg;
            }
            this.mMessageCount++;
        }
        return token;
    }

    public void removeSyncBarrier(int token) {
        boolean needWake;
        synchronized (this) {
            Message prev = null;
            Message p = this.mMessages;
            while (p != null && (p.target != null || p.arg1 != token)) {
                prev = p;
                p = p.next;
            }
            if (p != null) {
                if (prev != null) {
                    prev.next = p.next;
                    needWake = false;
                } else {
                    this.mMessages = p.next;
                    if (this.mMessages != null) {
                        if (this.mMessages.target == null) {
                            needWake = false;
                        }
                    }
                    needWake = true;
                }
                p.recycleUnchecked();
                this.mMessageCount--;
                if (needWake && !this.mQuitting) {
                    nativeWake(this.mPtr);
                }
            } else {
                throw new IllegalStateException("The specified message queue synchronization  barrier token has not been posted or has already been removed.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00aa, code lost:
        return true;
     */
    public boolean enqueueMessage(Message msg, long when) {
        Message prev;
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        } else if (!msg.isInUse()) {
            synchronized (this) {
                boolean needWake = false;
                if (this.mQuitting) {
                    IllegalStateException e = new IllegalStateException(msg.target + " sending message to a Handler on a dead thread");
                    Log.w(TAG, e.getMessage(), e);
                    msg.recycle();
                    return false;
                }
                msg.markInUse();
                msg.when = when;
                long now = SystemClock.uptimeMillis();
                if (when < now) {
                    msg.expectedDispatchTime = now;
                } else {
                    msg.expectedDispatchTime = when;
                }
                Message p = this.mMessages;
                if (!mIawareEnabled || !msg.isVsync()) {
                    if (!(p == null || when == 0)) {
                        if (when >= p.when) {
                            if (this.mBlocked && p.target == null && msg.isAsynchronous()) {
                                needWake = true;
                            }
                            while (true) {
                                prev = p;
                                p = p.next;
                                if (p == null) {
                                    break;
                                } else if (when < p.when) {
                                    break;
                                } else if (needWake && p.isAsynchronous()) {
                                    needWake = false;
                                }
                            }
                            msg.next = p;
                            prev.next = msg;
                        }
                    }
                    msg.next = p;
                    this.mMessages = msg;
                    needWake = this.mBlocked;
                } else {
                    msg.next = p;
                    this.mMessages = msg;
                    needWake = this.mBlocked;
                }
                this.mMessageCount++;
                if (needWake) {
                    nativeWake(this.mPtr);
                }
            }
        } else {
            throw new IllegalStateException(msg + " This message is already in use.");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasMessages(Handler h, int what, Object object) {
        if (h == null) {
            return false;
        }
        synchronized (this) {
            for (Message p = this.mMessages; p != null; p = p.next) {
                if (p.target == h && p.what == what && (object == null || p.obj == object)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasMessages(Handler h, Runnable r, Object object) {
        if (h == null) {
            return false;
        }
        synchronized (this) {
            for (Message p = this.mMessages; p != null; p = p.next) {
                if (p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasMessages(Handler h) {
        if (h == null) {
            return false;
        }
        synchronized (this) {
            for (Message p = this.mMessages; p != null; p = p.next) {
                if (p.target == h) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeMessages(Handler h, int what, Object object) {
        if (h != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && p.what == what && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && n2.what == what && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n2;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeMessages(Handler h, Runnable r, Object object) {
        if (h != null && r != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && n2.callback == r && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n2;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeCallbacksAndMessages(Handler h, Object object) {
        if (h != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                    this.mMessageCount--;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                        this.mMessageCount--;
                    } else {
                        p = n2;
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
            if (p.when > now) {
                removeAllMessagesLocked();
            } else {
                while (true) {
                    Message n = p.next;
                    if (n != null) {
                        if (n.when > now) {
                            p.next = null;
                            do {
                                Message p2 = n;
                                n = p2.next;
                                p2.recycleUnchecked();
                                this.mMessageCount--;
                            } while (n != null);
                            break;
                        }
                        p = n;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(Printer pw, String prefix, Handler h) {
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            int n = 0;
            for (Message msg = this.mMessages; msg != null; msg = msg.next) {
                if (h == null || h == msg.target) {
                    pw.println(prefix + "Message " + n + ": " + msg.toString(now));
                }
                n++;
            }
            pw.println(prefix + "(Total messages: " + n + ", polling=" + isPollingLocked() + ", quitting=" + this.mQuitting + ")");
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long messageQueueToken = proto.start(fieldId);
        synchronized (this) {
            for (Message msg = this.mMessages; msg != null; msg = msg.next) {
                msg.writeToProto(proto, 2246267895809L);
            }
            proto.write(1133871366146L, isPollingLocked());
            proto.write(1133871366147L, this.mQuitting);
        }
        proto.end(messageQueueToken);
    }

    public int getMessageCount() {
        return this.mMessageCount;
    }
}
