package ohos.eventhandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import ohos.eventhandler.ICourier;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;

public class EventHandler {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108208, "JavaEventHandler");
    private static ThreadLocal<EventHandler> currentEventHandler = new ThreadLocal<>();
    private ICourier courier;
    private final EventRunner eventRunner;
    private List<InnerEvent> events = new ArrayList();
    private final Object handlerLock = new Object();

    /* access modifiers changed from: package-private */
    public interface IEventFilter {
        boolean match(InnerEvent innerEvent);
    }

    /* access modifiers changed from: protected */
    public void processEvent(InnerEvent innerEvent) {
    }

    public enum Priority {
        IMMEDIATE(0),
        HIGH(1),
        LOW(2),
        IDLE(3);
        
        private final int value;

        private Priority(int i) {
            this.value = i;
        }
    }

    /* access modifiers changed from: private */
    public enum EventType {
        SYNC_EVENT(0),
        DELAY_EVENT(1),
        TIMING_EVENT(2);
        
        private final int value;

        private EventType(int i) {
            this.value = i;
        }
    }

    public EventHandler(EventRunner eventRunner2) throws IllegalArgumentException {
        if (eventRunner2 != null) {
            this.eventRunner = eventRunner2;
        } else {
            HiLog.error(LOG_LABEL, "Parameter 'runner' MUST NOT be invalid", new Object[0]);
            throw new IllegalArgumentException("Parameter 'runner' MUST NOT be invalid");
        }
    }

    public static EventHandler current() {
        return currentEventHandler.get();
    }

    private void sendEvent(InnerEvent innerEvent, long j, Priority priority, EventType eventType) throws IllegalArgumentException {
        if (innerEvent == null || innerEvent.owner != null || priority == null) {
            HiLog.error(LOG_LABEL, "Failed to send an invalid event", new Object[0]);
            throw new IllegalArgumentException("Failed to send an invalid event");
        }
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "No runner before sending events is illegal.", new Object[0]);
            return;
        }
        long nanoTime = System.nanoTime();
        innerEvent.sendTime = nanoTime;
        if (j > 0) {
            innerEvent.handleTime = nanoTime + EventHandlerUtils.fromMillsToNanoSeconds(j);
        } else {
            innerEvent.handleTime = nanoTime;
        }
        innerEvent.owner = new WeakReference<>(this);
        this.eventRunner.eventQueue.insert(innerEvent, priority);
    }

    private void sendEvent(InnerEvent innerEvent, long j, Priority priority, boolean z) throws IllegalArgumentException {
        if (z) {
            sendEvent(innerEvent, j, priority, EventType.SYNC_EVENT);
        } else {
            sendEvent(innerEvent, j, priority, EventType.DELAY_EVENT);
        }
    }

    public void sendEvent(InnerEvent innerEvent, long j, Priority priority) throws IllegalArgumentException {
        sendEvent(innerEvent, j, priority, EventType.DELAY_EVENT);
    }

    public void sendEvent(InnerEvent innerEvent) throws IllegalArgumentException {
        sendEvent(innerEvent, 0, Priority.LOW);
    }

    public void sendEvent(InnerEvent innerEvent, long j) throws IllegalArgumentException {
        sendEvent(innerEvent, j, Priority.LOW);
    }

    public void sendEvent(InnerEvent innerEvent, Priority priority) throws IllegalArgumentException {
        sendEvent(innerEvent, 0, priority);
    }

    public void sendEvent(int i) throws IllegalArgumentException {
        sendEvent(InnerEvent.get(i), 0, Priority.LOW);
    }

    public void sendEvent(int i, long j) throws IllegalArgumentException {
        sendEvent(InnerEvent.get(i), j, Priority.LOW);
    }

    public void sendEvent(int i, Priority priority) throws IllegalArgumentException {
        sendEvent(InnerEvent.get(i), 0, priority);
    }

    public void sendEvent(int i, long j, Priority priority) throws IllegalArgumentException {
        sendEvent(InnerEvent.get(i), j, priority);
    }

    public void postTask(Runnable runnable, long j, Priority priority) throws IllegalArgumentException {
        sendEvent(InnerEvent.get(runnable), j, priority);
    }

    public void postTask(Runnable runnable) throws IllegalArgumentException {
        postTask(runnable, 0, Priority.LOW);
    }

    public void postTask(Runnable runnable, long j) throws IllegalArgumentException {
        postTask(runnable, j, Priority.LOW);
    }

    public void postTask(Runnable runnable, Priority priority) throws IllegalArgumentException {
        postTask(runnable, 0, priority);
    }

    public void sendSyncEvent(InnerEvent innerEvent, Priority priority) throws IllegalArgumentException {
        if (innerEvent == null) {
            throw new IllegalArgumentException("Failed to send an invalid event");
        } else if (priority == null || priority == Priority.IDLE) {
            throw new IllegalArgumentException("Failed to send an sync event with invalid priority");
        } else {
            EventRunner eventRunner2 = this.eventRunner;
            if (eventRunner2 == null) {
                HiLog.error(LOG_LABEL, "No runner before sending events is illegal.", new Object[0]);
            } else if (eventRunner2 == EventRunner.current()) {
                distributeEvent(innerEvent);
                innerEvent.drop();
            } else {
                innerEvent.prepareWait();
                sendEvent(innerEvent, 0, priority);
                try {
                    innerEvent.waitUntilProcessed();
                } catch (InterruptedException unused) {
                    HiLog.error(LOG_LABEL, "event wait break out.", new Object[0]);
                }
            }
        }
    }

    public void sendSyncEvent(InnerEvent innerEvent) throws IllegalArgumentException {
        sendSyncEvent(innerEvent, Priority.LOW);
    }

    public void sendSyncEvent(int i, Priority priority) throws IllegalArgumentException {
        sendSyncEvent(InnerEvent.get(i), priority);
    }

    public void sendSyncEvent(int i) throws IllegalArgumentException {
        sendSyncEvent(i, Priority.LOW);
    }

    public void postSyncTask(Runnable runnable, Priority priority) throws IllegalArgumentException {
        sendSyncEvent(InnerEvent.get(runnable), priority);
    }

    public void postSyncTask(Runnable runnable) throws IllegalArgumentException {
        postSyncTask(runnable, Priority.LOW);
    }

    public void postTask(Runnable runnable, Object obj) throws IllegalArgumentException {
        InnerEvent innerEvent = InnerEvent.get(runnable);
        innerEvent.object = obj;
        sendEvent(innerEvent);
    }

    public void sendTimingEvent(InnerEvent innerEvent, long j, Priority priority) throws IllegalArgumentException {
        long fromNanoSecondsToMills = j - EventHandlerUtils.fromNanoSecondsToMills(System.nanoTime());
        if (fromNanoSecondsToMills > 0) {
            sendEvent(innerEvent, fromNanoSecondsToMills, priority, EventType.TIMING_EVENT);
        } else {
            sendEvent(innerEvent, 0, priority, EventType.TIMING_EVENT);
        }
    }

    public void sendTimingEvent(InnerEvent innerEvent, long j) throws IllegalArgumentException {
        sendTimingEvent(innerEvent, j, Priority.LOW);
    }

    public void sendTimingEvent(int i, long j) throws IllegalArgumentException {
        sendTimingEvent(InnerEvent.get(i), j, Priority.LOW);
    }

    public void sendTimingEvent(int i, long j, Priority priority) throws IllegalArgumentException {
        sendTimingEvent(InnerEvent.get(i), j, priority);
    }

    public void postTimingTask(Runnable runnable, long j, Priority priority) throws IllegalArgumentException {
        sendTimingEvent(InnerEvent.get(runnable), j, priority);
    }

    public void postTimingTask(Runnable runnable, Object obj, long j) throws IllegalArgumentException {
        InnerEvent innerEvent = InnerEvent.get(runnable);
        innerEvent.object = obj;
        sendTimingEvent(innerEvent, j);
    }

    public void postTimingTask(Runnable runnable, long j) throws IllegalArgumentException {
        postTimingTask(runnable, j, Priority.LOW);
    }

    public void removeAllEvent() {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this);
        }
    }

    public void removeEvent(int i) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, i);
        }
    }

    public void removeEvent(int i, long j) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, i, j);
        }
    }

    public void removeEvent(int i, Object obj) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, i, obj);
        }
    }

    public void removeEvent(int i, long j, Object obj) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, i, j, obj);
        }
    }

    public void removeTask(Runnable runnable) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, runnable);
        }
    }

    public void removeTask(Runnable runnable, Object obj) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        } else {
            this.eventRunner.eventQueue.remove(this, runnable, obj);
        }
    }

    public final EventRunner getEventRunner() {
        return this.eventRunner;
    }

    public final boolean isIdle() {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null || eventRunner2.eventQueue == null) {
            return false;
        }
        return !this.eventRunner.eventQueue.hasInnerEvent(this, 0, null, null);
    }

    public final boolean hasInnerEvent(Runnable runnable) {
        if (runnable == null) {
            return false;
        }
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 != null && eventRunner2.eventQueue != null) {
            return this.eventRunner.eventQueue.hasInnerEvent(this, 0, null, runnable);
        }
        HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        return false;
    }

    public final boolean hasInnerEvent(long j) {
        if (j == 0) {
            return false;
        }
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 != null && eventRunner2.eventQueue != null) {
            return this.eventRunner.eventQueue.hasInnerEvent(this, j, null, null);
        }
        HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        return false;
    }

    public final boolean hasInnerEvent(Object obj) {
        if (obj == null) {
            return false;
        }
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 != null && eventRunner2.eventQueue != null) {
            return this.eventRunner.eventQueue.hasInnerEvent(this, 0, obj, null);
        }
        HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        return false;
    }

    public final boolean hasInnerEvent(int i) {
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 != null && eventRunner2.eventQueue != null) {
            return this.eventRunner.eventQueue.hasInnerEvent(this, i, null);
        }
        HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        return false;
    }

    public final boolean hasInnerEvent(int i, Object obj) {
        if (obj == null) {
            return false;
        }
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 != null && eventRunner2.eventQueue != null) {
            return this.eventRunner.eventQueue.hasInnerEvent(this, i, obj);
        }
        HiLog.error(LOG_LABEL, "eventRunner or eventQueue is empty.", new Object[0]);
        return false;
    }

    public final void dump(Dumper dumper) {
        if (dumper == null) {
            HiLog.info(LOG_LABEL, "parameter dumper is null, please ensure method input is correct!", new Object[0]);
            return;
        }
        String tag = dumper.getTag() == null ? "" : dumper.getTag();
        dumper.dump(tag + " " + this + " @ " + System.nanoTime());
        EventRunner eventRunner2 = this.eventRunner;
        if (eventRunner2 == null) {
            dumper.dump(tag + " runner uninitialized");
            return;
        }
        eventRunner2.dump(dumper);
    }

    public String getEventName(InnerEvent innerEvent) {
        if (innerEvent.task == null) {
            return String.valueOf(innerEvent.getUniqueId());
        }
        return innerEvent.task.toString();
    }

    public void distributeEvent(InnerEvent innerEvent) {
        if (innerEvent != null) {
            EventHandler eventHandler = currentEventHandler.get();
            currentEventHandler.set(this);
            if (innerEvent.task != null) {
                innerEvent.task.run();
            } else {
                processEvent(innerEvent);
            }
            currentEventHandler.set(eventHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public final ICourier getICourier() {
        ICourier iCourier = this.courier;
        if (iCourier != null) {
            return iCourier;
        }
        this.courier = new CourierImpl();
        return this.courier;
    }

    private final class CourierImpl extends ICourier.Stub {
        private CourierImpl() {
        }

        @Override // ohos.eventhandler.ICourier
        public void send(InnerEvent innerEvent) {
            innerEvent.sendingUid = IPCSkeleton.getCallingUid();
            EventHandler.this.sendEvent(innerEvent);
        }
    }
}
