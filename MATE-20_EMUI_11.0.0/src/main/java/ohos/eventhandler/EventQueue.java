package ohos.eventhandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class EventQueue {
    private static final int DEFAULT_MAX_HANDLED_COUNT = 5;
    private static final int INITIAL_EVENT_QUEUE_NUM = 10;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108208, "JavaEventQueue");
    private static final int SUB_EVENT_QUEUE_NUM = (EventHandler.Priority.IDLE.ordinal() + 1);
    private PriorityBlockingQueue<InnerEvent> idleEvents = new PriorityBlockingQueue<>(10, new Comparator<InnerEvent>() {
        /* class ohos.eventhandler.EventQueue.AnonymousClass1 */

        public int compare(InnerEvent innerEvent, InnerEvent innerEvent2) {
            if (innerEvent == null || innerEvent2 == null || innerEvent.handleTime == innerEvent2.handleTime) {
                return 0;
            }
            return innerEvent.handleTime > innerEvent2.handleTime ? 1 : -1;
        }
    });
    private long idleTimeStamp = System.nanoTime();
    private boolean inIdle = true;
    boolean inPlatformRunner = false;
    InnerEvent innerEvent;
    private volatile boolean isFinished = true;
    final Object lock = new Object();
    long nextWakeUpTime = Long.MAX_VALUE;
    WeakReference<EventRunner.EventInnerRunner> platformRunner = null;
    private List<SubEventQueue> subEventQueues = new ArrayList(SUB_EVENT_QUEUE_NUM);
    private long wakeUpTime = Long.MAX_VALUE;

    EventQueue() {
    }

    /* access modifiers changed from: private */
    public static class SubEventQueue {
        int handledCount;
        int maxHandledCount;
        PriorityBlockingQueue<InnerEvent> queue;

        private SubEventQueue() {
            this.queue = new PriorityBlockingQueue<>(10, new Comparator<InnerEvent>() {
                /* class ohos.eventhandler.EventQueue.SubEventQueue.AnonymousClass1 */

                public int compare(InnerEvent innerEvent, InnerEvent innerEvent2) {
                    if (innerEvent == null || innerEvent2 == null || innerEvent.handleTime == innerEvent2.handleTime) {
                        return 0;
                    }
                    return innerEvent.handleTime > innerEvent2.handleTime ? 1 : -1;
                }
            });
            this.handledCount = 0;
            this.maxHandledCount = 5;
        }
    }

    /* access modifiers changed from: package-private */
    public void prepare() {
        synchronized (this.lock) {
            this.isFinished = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void finish() {
        synchronized (this.lock) {
            this.isFinished = true;
            this.lock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void insert(InnerEvent innerEvent2, EventHandler.Priority priority) {
        boolean z = false;
        if (innerEvent2 == null) {
            HiLog.error(LOG_LABEL, "Could not insert an invalid event", new Object[0]);
        } else if (this.subEventQueues.size() == SUB_EVENT_QUEUE_NUM) {
            synchronized (this.lock) {
                int i = AnonymousClass8.$SwitchMap$ohos$eventhandler$EventHandler$Priority[priority.ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    if (innerEvent2.handleTime < this.wakeUpTime) {
                        z = true;
                    }
                    this.subEventQueues.get(priority.ordinal()).queue.add(innerEvent2);
                } else if (i == 4) {
                    this.subEventQueues.get(priority.ordinal()).queue.add(innerEvent2);
                }
                if (!this.inPlatformRunner) {
                    if (z) {
                        this.lock.notifyAll();
                    }
                    return;
                }
                if (this.platformRunner != null) {
                    if (this.platformRunner.get() != null) {
                        if (z) {
                            this.platformRunner.get().wake(innerEvent2.handleTime - System.nanoTime());
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.eventhandler.EventQueue$8  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$ohos$eventhandler$EventHandler$Priority = new int[EventHandler.Priority.values().length];

        static {
            try {
                $SwitchMap$ohos$eventhandler$EventHandler$Priority[EventHandler.Priority.IMMEDIATE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$eventhandler$EventHandler$Priority[EventHandler.Priority.HIGH.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$eventhandler$EventHandler$Priority[EventHandler.Priority.LOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$eventhandler$EventHandler$Priority[EventHandler.Priority.IDLE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isQueueEmpty() {
        List<SubEventQueue> list = this.subEventQueues;
        if (list == null || list.size() != SUB_EVENT_QUEUE_NUM) {
            return false;
        }
        for (int i = 0; i < SUB_EVENT_QUEUE_NUM; i++) {
            if (!(this.subEventQueues.get(i) == null || this.subEventQueues.get(i).queue == null || this.subEventQueues.get(i).queue.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass2 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return (innerEvent == null || innerEvent.owner == null || innerEvent.owner.get() != eventHandler) ? false : true;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler, final int i) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass3 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return innerEvent != null && innerEvent.owner != null && innerEvent.owner.get() == eventHandler && innerEvent.eventId == i;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler, final Runnable runnable) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass4 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return innerEvent != null && innerEvent.owner != null && innerEvent.owner.get() == eventHandler && innerEvent.task == runnable;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler, final int i, final long j) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass5 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return innerEvent != null && innerEvent.owner != null && innerEvent.owner.get() == eventHandler && innerEvent.eventId == i && innerEvent.param == j;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler, final int i, final Object obj) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass6 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return innerEvent != null && innerEvent.owner != null && innerEvent.owner.get() == eventHandler && innerEvent.eventId == i && innerEvent.object == obj;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(final EventHandler eventHandler, final int i, final long j, final Object obj) {
        if (eventHandler == null) {
            HiLog.error(LOG_LABEL, "Empty owner", new Object[0]);
        } else {
            remove(new EventHandler.IEventFilter() {
                /* class ohos.eventhandler.EventQueue.AnonymousClass7 */

                @Override // ohos.eventhandler.EventHandler.IEventFilter
                public boolean match(InnerEvent innerEvent) {
                    return innerEvent != null && innerEvent.owner != null && innerEvent.owner.get() == eventHandler && innerEvent.eventId == i && innerEvent.param == j && innerEvent.object == obj;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x005c A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x001a A[ADDED_TO_REGION, SYNTHETIC] */
    public boolean hasInnerEvent(EventHandler eventHandler, long j, Object obj, Runnable runnable) {
        boolean z;
        boolean z2;
        boolean z3;
        if (eventHandler == null) {
            return false;
        }
        synchronized (this.lock) {
            for (int i = 0; i < SUB_EVENT_QUEUE_NUM; i++) {
                Iterator<InnerEvent> it = this.subEventQueues.get(i).queue.iterator();
                while (it.hasNext()) {
                    InnerEvent next = it.next();
                    boolean z4 = next.owner != null && next.owner.get() == eventHandler;
                    if (j != 0) {
                        if (next.param != j) {
                            z = false;
                            if (obj != null) {
                                if (next.object != obj) {
                                    z2 = false;
                                    if (runnable != null) {
                                        if (next.task != runnable) {
                                            z3 = false;
                                            if (!z4 && z && z2 && z3) {
                                                return true;
                                            }
                                        }
                                    }
                                    z3 = true;
                                    if (!z4) {
                                    }
                                }
                            }
                            z2 = true;
                            if (runnable != null) {
                            }
                            z3 = true;
                            if (!z4) {
                            }
                        }
                    }
                    z = true;
                    if (obj != null) {
                    }
                    z2 = true;
                    if (runnable != null) {
                    }
                    z3 = true;
                    if (!z4) {
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasInnerEvent(EventHandler eventHandler, int i, Object obj) {
        boolean z;
        if (eventHandler == null) {
            return false;
        }
        synchronized (this.lock) {
            for (int i2 = 0; i2 < SUB_EVENT_QUEUE_NUM; i2++) {
                Iterator<InnerEvent> it = this.subEventQueues.get(i2).queue.iterator();
                while (it.hasNext()) {
                    InnerEvent next = it.next();
                    boolean z2 = next.owner != null && next.owner.get() == eventHandler;
                    boolean z3 = i == next.eventId;
                    if (obj != null) {
                        if (next.object != obj) {
                            z = false;
                            if (!z2 && z3 && z) {
                                return true;
                            }
                        }
                    }
                    z = true;
                    if (!z2) {
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<InnerEvent> getEvent() {
        synchronized (this.lock) {
            while (!this.isFinished) {
                this.nextWakeUpTime = Long.MAX_VALUE;
                Optional<InnerEvent> expiredEventLocked = getExpiredEventLocked();
                if (expiredEventLocked.isPresent()) {
                    return expiredEventLocked;
                }
                waitUntilLocked(this.nextWakeUpTime);
            }
            HiLog.error(LOG_LABEL, "getEvent: Break out", new Object[0]);
            return Optional.empty();
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<InnerEvent> getExpiredEvent() {
        Optional<InnerEvent> expiredEventLocked;
        synchronized (this.lock) {
            expiredEventLocked = getExpiredEventLocked();
        }
        return expiredEventLocked;
    }

    /* access modifiers changed from: package-private */
    public void prepareSubEventQueue() {
        for (int i = 0; i < SUB_EVENT_QUEUE_NUM; i++) {
            this.subEventQueues.add(new SubEventQueue());
        }
    }

    private Optional<InnerEvent> getExpiredEventLocked() {
        InnerEvent peek;
        long nanoTime = System.nanoTime();
        this.wakeUpTime = Long.MAX_VALUE;
        Optional<InnerEvent> pickEventLocked = pickEventLocked(nanoTime);
        this.nextWakeUpTime = this.wakeUpTime;
        if (pickEventLocked.isPresent()) {
            this.inIdle = false;
            return pickEventLocked;
        }
        if (!this.inIdle) {
            this.idleTimeStamp = nanoTime;
            this.inIdle = true;
        }
        if (this.idleEvents.isEmpty() || (peek = this.idleEvents.peek()) == null || peek.sendTime > this.idleTimeStamp || peek.handleTime > nanoTime) {
            return Optional.empty();
        }
        return popFrontEventFromListLocked(this.idleEvents);
    }

    private void remove(EventHandler.IEventFilter iEventFilter) {
        synchronized (this.lock) {
            for (int i = 0; i < SUB_EVENT_QUEUE_NUM; i++) {
                if (this.subEventQueues.get(i).queue != null) {
                    if (!this.subEventQueues.get(i).queue.isEmpty()) {
                        Iterator<InnerEvent> it = this.subEventQueues.get(i).queue.iterator();
                        while (it.hasNext()) {
                            if (iEventFilter.match(it.next())) {
                                it.remove();
                            }
                        }
                    }
                }
            }
            Iterator<InnerEvent> it2 = this.idleEvents.iterator();
            while (it2.hasNext()) {
                if (iEventFilter.match(it2.next())) {
                    it2.remove();
                }
            }
        }
    }

    private Optional<InnerEvent> pickEventLocked(long j) {
        int i = SUB_EVENT_QUEUE_NUM;
        int i2 = 0;
        while (true) {
            if (i2 >= SUB_EVENT_QUEUE_NUM) {
                break;
            }
            if (!(this.subEventQueues.get(i2).queue == null || this.subEventQueues.get(i2).queue.isEmpty() || this.subEventQueues.get(i2).queue.peek() == null)) {
                long j2 = this.subEventQueues.get(i2).queue.peek().handleTime;
                if (j2 >= this.wakeUpTime) {
                    continue;
                } else {
                    this.wakeUpTime = j2;
                    if (j2 <= j) {
                        if (i < SUB_EVENT_QUEUE_NUM && this.subEventQueues.get(i).handledCount < this.subEventQueues.get(i).maxHandledCount) {
                            this.subEventQueues.get(i).handledCount++;
                            break;
                        }
                        i = i2;
                    } else {
                        continue;
                    }
                }
            }
            i2++;
        }
        if (i >= SUB_EVENT_QUEUE_NUM) {
            return Optional.empty();
        }
        for (int i3 = 0; i3 < i; i3++) {
            this.subEventQueues.get(i3).handledCount = 0;
        }
        return popFrontEventFromListLocked(this.subEventQueues.get(i).queue);
    }

    private void waitUntilLocked(long j) {
        try {
            long fromNanoSecondsToMills = EventHandlerUtils.fromNanoSecondsToMills(j - System.nanoTime());
            if (fromNanoSecondsToMills >= 2147483647L) {
                this.lock.wait(2147483647L);
            } else if (fromNanoSecondsToMills > 0) {
                this.lock.wait(fromNanoSecondsToMills);
            } else {
                HiLog.info(LOG_LABEL, "waitUntilLocked: not need to wait", new Object[0]);
            }
        } catch (InterruptedException unused) {
            HiLog.error(LOG_LABEL, "waitUntilLocked: Break out", new Object[0]);
        }
    }

    private Optional<InnerEvent> popFrontEventFromListLocked(PriorityBlockingQueue<InnerEvent> priorityBlockingQueue) {
        return Optional.of(priorityBlockingQueue.poll());
    }
}
