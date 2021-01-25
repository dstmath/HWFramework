package ohos.eventhandler;

import java.lang.ref.WeakReference;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class InnerEvent implements Sequenceable {
    public static final Sequenceable.Producer<InnerEvent> PRODUCER = $$Lambda$InnerEvent$Y46Dnmv28s3dNUZGNU2GDj_cX_Y.INSTANCE;
    public int eventId;
    long handleTime;
    private volatile boolean isFinished;
    final Object lock;
    public Object object;
    WeakReference<EventHandler> owner;
    private PacMap pacMap;
    public long param;
    public Courier replyTo;
    long sendTime;
    public int sendingUid;
    Runnable task;

    private InnerEvent() {
        this.eventId = 0;
        this.param = 0;
        this.object = null;
        this.sendingUid = -1;
        this.handleTime = Long.MAX_VALUE;
        this.sendTime = Long.MAX_VALUE;
        this.task = null;
        this.owner = null;
        this.lock = new Object();
        this.pacMap = null;
        this.isFinished = false;
    }

    static /* synthetic */ InnerEvent lambda$static$0(Parcel parcel) {
        InnerEvent innerEvent = new InnerEvent();
        innerEvent.unmarshalling(parcel);
        return innerEvent;
    }

    public static InnerEvent get() {
        return InnerEventPool.getInstance().get();
    }

    public static InnerEvent copyFrom(InnerEvent innerEvent) throws CloneNotSupportedException {
        InnerEvent innerEvent2 = InnerEventPool.getInstance().get();
        innerEvent2.eventId = innerEvent.eventId;
        innerEvent2.task = innerEvent.task;
        innerEvent2.param = innerEvent.param;
        innerEvent2.owner = innerEvent.owner;
        innerEvent2.object = innerEvent.object;
        PacMap pacMap2 = innerEvent.pacMap;
        if (pacMap2 != null) {
            Object clone = pacMap2.clone();
            if (clone != null && (clone instanceof PacMap)) {
                innerEvent2.pacMap = (PacMap) clone;
            }
        } else {
            innerEvent2.pacMap = null;
        }
        innerEvent2.sendingUid = innerEvent.sendingUid;
        return innerEvent2;
    }

    public static InnerEvent get(int i, long j, Object obj) {
        InnerEvent innerEvent = get();
        innerEvent.eventId = i;
        innerEvent.param = j;
        innerEvent.object = obj;
        return innerEvent;
    }

    public static InnerEvent get(int i) {
        return get(i, 0, null);
    }

    public static InnerEvent get(int i, long j) {
        return get(i, j, null);
    }

    public static InnerEvent get(int i, Object obj) {
        return get(i, 0, obj);
    }

    static InnerEvent get(Runnable runnable) {
        if (runnable != null) {
            InnerEvent innerEvent = get();
            innerEvent.task = runnable;
            return innerEvent;
        }
        throw new IllegalArgumentException("Failed to get InnerEvent with invalid task");
    }

    public void drop() {
        notifyLock();
        this.eventId = 0;
        this.param = 0;
        this.object = null;
        this.task = null;
        this.owner = null;
        this.replyTo = null;
        this.sendingUid = -1;
        this.handleTime = Long.MAX_VALUE;
        this.sendTime = Long.MAX_VALUE;
        this.pacMap = null;
        InnerEventPool.getInstance().put(this);
    }

    /* access modifiers changed from: package-private */
    public long getUniqueId() {
        return (long) System.identityHashCode(this);
    }

    /* access modifiers changed from: package-private */
    public void waitUntilProcessed() throws InterruptedException {
        synchronized (this.lock) {
            while (!this.isFinished) {
                this.lock.wait();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareWait() {
        synchronized (this.lock) {
            this.isFinished = false;
        }
    }

    public PacMap getPacMap() {
        PacMap pacMap2 = this.pacMap;
        return pacMap2 == null ? new PacMap() : pacMap2;
    }

    public void setPacMap(PacMap pacMap2) {
        this.pacMap = pacMap2;
    }

    public PacMap peekPacMap() {
        return this.pacMap;
    }

    public Runnable getTask() {
        return this.task;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeInt(this.eventId) || !parcel.writeLong(this.param)) {
            return false;
        }
        if (this.pacMap == null) {
            if (!parcel.writeInt(0)) {
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            return false;
        } else {
            parcel.writeSequenceable(this.pacMap);
        }
        if (!parcel.writeInt(this.sendingUid)) {
            return false;
        }
        Courier courier = this.replyTo;
        if (courier == null) {
            return true;
        }
        IRemoteObject remoteObject = courier.getRemoteObject();
        if (remoteObject == null || !(parcel instanceof MessageParcel)) {
            return false;
        }
        return ((MessageParcel) parcel).writeRemoteObject(remoteObject);
    }

    public boolean unmarshalling(Parcel parcel) {
        IRemoteObject readRemoteObject;
        this.eventId = parcel.readInt();
        this.param = parcel.readLong();
        if (parcel.readInt() == 1) {
            PacMap pacMap2 = new PacMap();
            if (!parcel.readSequenceable(pacMap2)) {
                return false;
            }
            this.pacMap = pacMap2;
        }
        this.sendingUid = parcel.readInt();
        if ((parcel instanceof MessageParcel) && (readRemoteObject = ((MessageParcel) parcel).readRemoteObject()) != null) {
            this.replyTo = new Courier(readRemoteObject);
        }
        return true;
    }

    private void notifyLock() {
        synchronized (this.lock) {
            this.lock.notifyAll();
            this.isFinished = true;
        }
    }

    /* access modifiers changed from: private */
    public static class InnerEventPool {
        private static final Object LOCK = new Object();
        private static final int MAX_BUFFER_POOL_SIZE = 64;
        private static InnerEventPool instance = new InnerEventPool();
        private InnerEvent[] eventPool = new InnerEvent[64];
        private int poolSize = 0;

        private InnerEventPool() {
        }

        public static InnerEventPool getInstance() {
            return instance;
        }

        public InnerEvent get() {
            synchronized (LOCK) {
                if (this.poolSize <= 0) {
                    return new InnerEvent();
                }
                InnerEvent[] innerEventArr = this.eventPool;
                int i = this.poolSize - 1;
                this.poolSize = i;
                InnerEvent innerEvent = innerEventArr[i];
                this.eventPool[this.poolSize] = null;
                return innerEvent;
            }
        }

        public void put(InnerEvent innerEvent) {
            synchronized (LOCK) {
                if (this.poolSize < 64) {
                    InnerEvent[] innerEventArr = this.eventPool;
                    int i = this.poolSize;
                    this.poolSize = i + 1;
                    innerEventArr[i] = innerEvent;
                }
            }
        }
    }
}
