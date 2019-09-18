package android.os;

import android.os.Parcelable;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;

public final class Message implements Parcelable {
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            Message msg = Message.obtain();
            msg.readFromParcel(source);
            return msg;
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    static final int FLAGS_TO_CLEAR_ON_COPY_FROM = 1;
    static final int FLAG_ASYNCHRONOUS = 2;
    static final int FLAG_IN_USE = 1;
    static final int FLAG_VSYNC = 4;
    private static final int MAX_POOL_SIZE = 50;
    private static boolean gCheckRecycle = true;
    private static Message sPool;
    private static int sPoolSize = 0;
    public static final Object sPoolSync = new Object();
    public int arg1;
    public int arg2;
    Runnable callback;
    Bundle data;
    public long etraceID = 0;
    public long expectedDispatchTime;
    int flags;
    Message next;
    public Object obj;
    public Messenger replyTo;
    public int sendingUid = -1;
    Handler target;
    public int what;
    long when;

    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool == null) {
                return new Message();
            }
            Message m = sPool;
            sPool = m.next;
            m.next = null;
            m.flags = 0;
            sPoolSize--;
            return m;
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

    public static Message obtain(Handler h, Runnable callback2) {
        Message m = obtain();
        m.target = h;
        m.callback = callback2;
        return m;
    }

    public static Message obtain(Handler h, int what2) {
        Message m = obtain();
        m.target = h;
        m.what = what2;
        return m;
    }

    public static Message obtain(Handler h, int what2, Object obj2) {
        Message m = obtain();
        m.target = h;
        m.what = what2;
        m.obj = obj2;
        return m;
    }

    public static Message obtain(Handler h, int what2, int arg12, int arg22) {
        Message m = obtain();
        m.target = h;
        m.what = what2;
        m.arg1 = arg12;
        m.arg2 = arg22;
        return m;
    }

    public static Message obtain(Handler h, int what2, int arg12, int arg22, Object obj2) {
        Message m = obtain();
        m.target = h;
        m.what = what2;
        m.arg1 = arg12;
        m.arg2 = arg22;
        m.obj = obj2;
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

    /* access modifiers changed from: package-private */
    public void recycleUnchecked() {
        this.flags = 1;
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
            if (sPoolSize < 50) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
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

    public void setTarget(Handler target2) {
        this.target = target2;
    }

    public Handler getTarget() {
        return this.target;
    }

    public Runnable getCallback() {
        return this.callback;
    }

    public Message setCallback(Runnable r) {
        this.callback = r;
        return this;
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

    public void setData(Bundle data2) {
        this.data = data2;
    }

    public Message setWhat(int what2) {
        this.what = what2;
        return this;
    }

    public void sendToTarget() {
        this.target.sendMessage(this);
    }

    public boolean isAsynchronous() {
        return (this.flags & 2) != 0;
    }

    public boolean isVsync() {
        return (this.flags & 4) != 0;
    }

    public void setAsynchronous(boolean async) {
        if (async) {
            this.flags |= 2;
        } else {
            this.flags &= -3;
        }
    }

    public void setVsync(boolean vsync) {
        if (vsync) {
            this.flags |= 4;
        } else {
            this.flags &= -5;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInUse() {
        return (this.flags & 1) == 1;
    }

    /* access modifiers changed from: package-private */
    public void markInUse() {
        this.flags |= 1;
    }

    public String toString() {
        return toString(SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    public String toString(long now) {
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

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long messageToken = proto.start(fieldId);
        proto.write(1112396529665L, this.when);
        if (this.target != null) {
            if (this.callback != null) {
                proto.write(1138166333442L, this.callback.getClass().getName());
            } else {
                proto.write(1120986464259L, this.what);
            }
            if (this.arg1 != 0) {
                proto.write(1120986464260L, this.arg1);
            }
            if (this.arg2 != 0) {
                proto.write(1120986464261L, this.arg2);
            }
            if (this.obj != null) {
                proto.write(1138166333446L, this.obj.toString());
            }
            proto.write(1138166333447L, this.target.getClass().getName());
        } else {
            proto.write(1120986464264L, this.arg1);
        }
        proto.end(messageToken);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags2) {
        if (this.callback == null) {
            dest.writeInt(this.what);
            dest.writeInt(this.arg1);
            dest.writeInt(this.arg2);
            if (this.obj != null) {
                try {
                    dest.writeInt(1);
                    dest.writeParcelable((Parcelable) this.obj, flags2);
                } catch (ClassCastException e) {
                    throw new RuntimeException("Can't marshal non-Parcelable objects across processes.");
                }
            } else {
                dest.writeInt(0);
            }
            dest.writeLong(this.when);
            dest.writeBundle(this.data);
            Messenger.writeMessengerOrNullToParcel(this.replyTo, dest);
            dest.writeInt(this.sendingUid);
            return;
        }
        throw new RuntimeException("Can't marshal callbacks across processes.");
    }

    /* access modifiers changed from: private */
    public void readFromParcel(Parcel source) {
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
