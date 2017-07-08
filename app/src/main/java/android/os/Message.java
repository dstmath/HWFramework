package android.os;

import android.os.Parcelable.Creator;
import android.util.TimeUtils;

public final class Message implements Parcelable {
    public static final Creator<Message> CREATOR = null;
    static final int FLAGS_TO_CLEAR_ON_COPY_FROM = 1;
    static final int FLAG_ASYNCHRONOUS = 2;
    static final int FLAG_IN_USE = 1;
    static final int FLAG_VSYNC = 4;
    private static final int MAX_POOL_SIZE = 50;
    private static boolean gCheckRecycle;
    private static Message sPool;
    private static int sPoolSize;
    private static final Object sPoolSync = null;
    public int arg1;
    public int arg2;
    Runnable callback;
    Bundle data;
    int flags;
    Message next;
    public Object obj;
    public Messenger replyTo;
    public int sendingUid;
    Handler target;
    public int what;
    long when;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Message.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Message.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.Message.<clinit>():void");
    }

    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                Message m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0;
                sPoolSize--;
                return m;
            }
            return new Message();
        }
    }

    public static Message obtain(Message orig) {
        Message m = obtain();
        m.what = orig.what;
        m.arg1 = orig.arg1;
        m.arg2 = orig.arg2;
        m.obj = orig.obj;
        m.replyTo = orig.replyTo;
        m.sendingUid = orig.sendingUid;
        if (orig.data != null) {
            m.data = new Bundle(orig.data);
        }
        m.target = orig.target;
        m.callback = orig.callback;
        return m;
    }

    public static Message obtain(Handler h) {
        Message m = obtain();
        m.target = h;
        return m;
    }

    public static Message obtain(Handler h, Runnable callback) {
        Message m = obtain();
        m.target = h;
        m.callback = callback;
        return m;
    }

    public static Message obtain(Handler h, int what) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        return m;
    }

    public static Message obtain(Handler h, int what, Object obj) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.obj = obj;
        return m;
    }

    public static Message obtain(Handler h, int what, int arg1, int arg2) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        return m;
    }

    public static Message obtain(Handler h, int what, int arg1, int arg2, Object obj) {
        Message m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = obj;
        return m;
    }

    public static void updateCheckRecycle(int targetSdkVersion) {
        if (targetSdkVersion < 21) {
            gCheckRecycle = false;
        }
    }

    public void recycle() {
        if (!isInUse()) {
            recycleUnchecked();
        } else if (gCheckRecycle) {
            throw new IllegalStateException("This message cannot be recycled because it is still in use.");
        }
    }

    void recycleUnchecked() {
        this.flags = FLAG_IN_USE;
        this.what = 0;
        this.arg1 = 0;
        this.arg2 = 0;
        this.obj = null;
        this.replyTo = null;
        this.sendingUid = -1;
        this.when = 0;
        this.target = null;
        this.callback = null;
        this.data = null;
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                this.next = sPool;
                sPool = this;
                sPoolSize += FLAG_IN_USE;
            }
        }
    }

    public void copyFrom(Message o) {
        this.flags = o.flags & -2;
        this.what = o.what;
        this.arg1 = o.arg1;
        this.arg2 = o.arg2;
        this.obj = o.obj;
        this.replyTo = o.replyTo;
        this.sendingUid = o.sendingUid;
        if (o.data != null) {
            this.data = (Bundle) o.data.clone();
        } else {
            this.data = null;
        }
    }

    public long getWhen() {
        return this.when;
    }

    public void setTarget(Handler target) {
        this.target = target;
    }

    public Handler getTarget() {
        return this.target;
    }

    public Runnable getCallback() {
        return this.callback;
    }

    public Bundle getData() {
        if (this.data == null) {
            this.data = new Bundle();
        }
        return this.data;
    }

    public Bundle peekData() {
        return this.data;
    }

    public void setData(Bundle data) {
        this.data = data;
    }

    public void sendToTarget() {
        this.target.sendMessage(this);
    }

    public boolean isAsynchronous() {
        return (this.flags & FLAG_ASYNCHRONOUS) != 0;
    }

    public boolean isVsync() {
        return (this.flags & FLAG_VSYNC) != 0;
    }

    public void setAsynchronous(boolean async) {
        if (async) {
            this.flags |= FLAG_ASYNCHRONOUS;
        } else {
            this.flags &= -3;
        }
    }

    public void setVsync(boolean vsync) {
        if (vsync) {
            this.flags |= FLAG_VSYNC;
        } else {
            this.flags &= -5;
        }
    }

    boolean isInUse() {
        return (this.flags & FLAG_IN_USE) == FLAG_IN_USE;
    }

    void markInUse() {
        this.flags |= FLAG_IN_USE;
    }

    public Message() {
        this.sendingUid = -1;
    }

    public String toString() {
        return toString(SystemClock.uptimeMillis());
    }

    String toString(long now) {
        StringBuilder b = new StringBuilder();
        b.append("{ when=");
        TimeUtils.formatDuration(this.when - now, b);
        if (this.target != null) {
            if (this.callback != null) {
                b.append(" callback=");
                b.append(this.callback.getClass().getName());
            } else {
                b.append(" what=");
                b.append(this.what);
            }
            if (this.arg1 != 0) {
                b.append(" arg1=");
                b.append(this.arg1);
            }
            if (this.arg2 != 0) {
                b.append(" arg2=");
                b.append(this.arg2);
            }
            if (this.obj != null) {
                b.append(" obj=");
                b.append(this.obj);
            }
            b.append(" target=");
            b.append(this.target.getClass().getName());
        } else {
            b.append(" barrier=");
            b.append(this.arg1);
        }
        b.append(" }");
        return b.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.callback != null) {
            throw new RuntimeException("Can't marshal callbacks across processes.");
        }
        dest.writeInt(this.what);
        dest.writeInt(this.arg1);
        dest.writeInt(this.arg2);
        if (this.obj != null) {
            try {
                Parcelable p = this.obj;
                dest.writeInt(FLAG_IN_USE);
                dest.writeParcelable(p, flags);
            } catch (ClassCastException e) {
                throw new RuntimeException("Can't marshal non-Parcelable objects across processes.");
            }
        }
        dest.writeInt(0);
        dest.writeLong(this.when);
        dest.writeBundle(this.data);
        Messenger.writeMessengerOrNullToParcel(this.replyTo, dest);
        dest.writeInt(this.sendingUid);
    }

    private void readFromParcel(Parcel source) {
        this.what = source.readInt();
        this.arg1 = source.readInt();
        this.arg2 = source.readInt();
        if (source.readInt() != 0) {
            this.obj = source.readParcelable(getClass().getClassLoader());
        }
        this.when = source.readLong();
        this.data = source.readBundle();
        this.replyTo = Messenger.readMessengerOrNullFromParcel(source);
        this.sendingUid = source.readInt();
    }
}
