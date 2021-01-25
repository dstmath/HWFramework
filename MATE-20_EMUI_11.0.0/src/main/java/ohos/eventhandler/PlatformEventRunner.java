package ohos.eventhandler;

import java.lang.ref.WeakReference;
import java.util.Optional;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class PlatformEventRunner extends EventRunner.EventInnerRunner {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108208, "PlatformEventRunner");
    private static ThreadLocal<EventRunner> platformCurrentRunner = new ThreadLocal<>();
    HandlerAdapter handlerAdapter;

    /* access modifiers changed from: package-private */
    @Override // ohos.eventhandler.EventRunner.EventInnerRunner
    public boolean stop() {
        return true;
    }

    PlatformEventRunner(EventRunner eventRunner, boolean z) {
        super(eventRunner, z);
        this.handlerAdapter = null;
        this.handlerAdapter = new HandlerAdapter();
        this.thread = Thread.currentThread();
    }

    static boolean checkCurrent() {
        return HandlerAdapter.checkCurrent();
    }

    static Optional<EventRunner> getEventRunner() {
        EventRunner eventRunner = new EventRunner(false);
        PlatformEventRunner platformEventRunner = new PlatformEventRunner(eventRunner, false);
        eventRunner.setEventInnerRunner(platformEventRunner);
        eventRunner.eventQueue = platformEventRunner.eventQueue;
        eventRunner.eventQueue.platformRunner = new WeakReference<>(platformEventRunner);
        eventRunner.eventQueue.inPlatformRunner = true;
        platformCurrentRunner.set(eventRunner);
        return Optional.of(eventRunner);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.eventhandler.EventRunner.EventInnerRunner
    public void wake(long j) {
        this.handlerAdapter.postTask(this, EventHandlerUtils.fromNanoSecondsToMills(j));
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.eventhandler.EventRunner.EventInnerRunner
    public boolean startToRun() {
        Optional<InnerEvent> expiredEvent = this.eventQueue.getExpiredEvent();
        while (expiredEvent.isPresent() && expiredEvent.get().owner != null) {
            EventHandler eventHandler = expiredEvent.get().owner.get();
            if (eventHandler != null) {
                eventHandler.distributeEvent(expiredEvent.get());
                expiredEvent.get().drop();
            }
            expiredEvent = this.eventQueue.getExpiredEvent();
        }
        if (this.eventQueue.nextWakeUpTime == Long.MAX_VALUE) {
            return true;
        }
        this.handlerAdapter.postTask(this, EventHandlerUtils.fromNanoSecondsToMills(this.eventQueue.nextWakeUpTime - System.nanoTime()));
        return true;
    }
}
