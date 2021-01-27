package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcelable;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import huawei.hiview.HiTraceId;

public final class Message implements Parcelable {
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        /* class android.os.Message.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Message createFromParcel(Parcel source) {
            Message msg = Message.obtain();
            msg.readFromParcel(source);
            return msg;
        }

        @Override // android.os.Parcelable.Creator
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    static final int FLAGS_TO_CLEAR_ON_COPY_FROM = 1;
    static final int FLAG_ASYNCHRONOUS = 2;
    static final int FLAG_IN_USE = 1;
    static final int FLAG_VSYNC = 4;
    private static final int MAX_POOL_SIZE = 50;
    public static final int UID_NONE = -1;
    private static boolean gCheckRecycle = true;
    private static Message sPool;
    private static int sPoolSize = 0;
    public static final Object sPoolSync = new Object();
    public int arg1;
    public int arg2;
    @UnsupportedAppUsage
    Runnable callback;
    Bundle data;
    public long expectedDispatchTime;
    @UnsupportedAppUsage
    int flags;
    @UnsupportedAppUsage
    Message next;
    public Object obj;
    public Messenger replyTo;
    public int sendingUid = -1;
    @UnsupportedAppUsage
    Handler target;
    public HiTraceId traceId;
    public int what;
    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public long when;
    public int workSourceUid = -1;

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
        m.workSourceUid = orig.workSourceUid;
        Bundle bundle = orig.data;
        if (bundle != null) {
            m.data = new Bundle(bundle);
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
    @UnsupportedAppUsage
    public void recycleUnchecked() {
        this.flags = 1;
        this.what = 0;
        this.arg1 = 0;
        this.arg2 = 0;
        this.obj = null;
        this.replyTo = null;
        this.sendingUid = -1;
        this.workSourceUid = -1;
        this.when = 0;
        this.target = null;
        this.callback = null;
        this.data = null;
        this.traceId = null;
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
        this.workSourceUid = o.workSourceUid;
        Bundle bundle = o.data;
        if (bundle != null) {
            this.data = (Bundle) bundle.clone();
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

    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    public void markInUse() {
        this.flags |= 1;
    }

    public String toString() {
        return toString(SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
            Runnable runnable = this.callback;
            if (runnable != null) {
                proto.write(1138166333442L, runnable.getClass().getName());
            } else {
                proto.write(1120986464259L, this.what);
            }
            int i = this.arg1;
            if (i != 0) {
                proto.write(1120986464260L, i);
            }
            int i2 = this.arg2;
            if (i2 != 0) {
                proto.write(1120986464261L, i2);
            }
            Object obj2 = this.obj;
            if (obj2 != null) {
                proto.write(1138166333446L, obj2.toString());
            }
            proto.write(1138166333447L, this.target.getClass().getName());
        } else {
            proto.write(1120986464264L, this.arg1);
        }
        proto.end(messageToken);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        if (this.callback == null) {
            dest.writeInt(this.what);
            dest.writeInt(this.arg1);
            dest.writeInt(this.arg2);
            Object obj2 = this.obj;
            if (obj2 != null) {
                try {
                    dest.writeInt(1);
                    dest.writeParcelable((Parcelable) obj2, flags2);
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
            dest.writeInt(this.workSourceUid);
            return;
        }
        throw new RuntimeException("Can't marshal callbacks across processes.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        this.workSourceUid = source.readInt();
    }
}
