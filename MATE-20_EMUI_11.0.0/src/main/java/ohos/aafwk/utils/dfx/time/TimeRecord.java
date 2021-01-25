package ohos.aafwk.utils.dfx.time;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

public class TimeRecord extends TimeEvent {
    private static final LogLabel LABEL = LogLabel.create();
    private WeakReference<Object> caller;
    private String callerStr;
    private StringBuilder runTimeDescription;
    private State state;
    private final Queue<TimeEvent> subEvents = new ArrayDeque();
    private final Map<TimeEventType, TimeEvent> subEventsPool = new HashMap(TimeEventType.values().length);

    /* access modifiers changed from: package-private */
    public enum State {
        INIT,
        START,
        DONE,
        FORCE_DONE
    }

    public /* synthetic */ boolean lambda$addRecord$3$TimeRecord(TimeRecord timeRecord) {
        return timeRecord != this;
    }

    TimeRecord(TimeEventType timeEventType, String str, Object obj) {
        super(str, timeEventType, obj.toString());
        Arrays.stream(TimeEventType.getSubEvents(timeEventType)).forEach(new Consumer(obj) {
            /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeRecord$6tFj0dXpUS75k6y_8Afy6DHJprM */
            private final /* synthetic */ Object f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                TimeRecord.this.lambda$new$0$TimeRecord(this.f$1, (TimeEventType) obj);
            }
        });
        this.caller = new WeakReference<>(obj);
        this.callerStr = obj.toString();
        this.state = State.INIT;
        this.runTimeDescription = new StringBuilder();
    }

    public /* synthetic */ void lambda$new$0$TimeRecord(Object obj, TimeEventType timeEventType) {
        this.subEventsPool.put(timeEventType, new TimeEvent("init", timeEventType, obj.toString()));
    }

    /* access modifiers changed from: package-private */
    public State getState() {
        return this.state;
    }

    public void start(long j) {
        start(null, j);
        this.subEvents.clear();
        this.state = State.START;
    }

    @Override // ohos.aafwk.utils.dfx.time.TimeEvent
    public boolean done(long j) {
        this.state = State.DONE;
        this.subEvents.stream().filter($$Lambda$TimeRecord$EiCmPwtzkRqOTkTEbpTprK_PSuY.INSTANCE).forEach(new Consumer(j) {
            /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeRecord$jdfQATP4EY1RBxSwuFpNexXjJyo */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((TimeEvent) obj).done(this.f$0);
            }
        });
        if (getType() == TimeEventType.ANONYMOUS) {
            return true;
        }
        super.done(j);
        return true;
    }

    static /* synthetic */ boolean lambda$done$1(TimeEvent timeEvent) {
        return !(timeEvent instanceof TimeRecord);
    }

    @Override // ohos.aafwk.utils.dfx.time.TimeEvent
    public boolean done() {
        return done(System.nanoTime());
    }

    /* access modifiers changed from: package-private */
    public void forceDone() {
        if (this.state != State.DONE && this.state != State.FORCE_DONE) {
            done();
            this.state = State.FORCE_DONE;
        }
    }

    @Override // ohos.aafwk.utils.dfx.time.TimeEvent
    public void clear() {
        this.subEvents.clear();
        this.subEventsPool.clear();
        this.runTimeDescription.setLength(0);
        this.callerStr = null;
        super.clear();
    }

    private void start(String str, long j) {
        super.start(str, this.callerStr, j, j, System.currentTimeMillis());
    }

    public TimeEvent eventStart(TimeEventType timeEventType, String str, long j) {
        return eventStart(timeEventType, str, this.callerStr, j);
    }

    public TimeEvent eventStart(TimeEventType timeEventType, String str, String str2, long j) {
        if (timeEventType == null) {
            Log.error("type is null for event start", new Object[0]);
            return null;
        }
        if (this.state == State.INIT) {
            start(j);
        }
        TimeEvent timeEvent = this.subEventsPool.get(timeEventType);
        if (timeEvent == null) {
            timeEvent = new TimeEvent(str, timeEventType, this.callerStr);
        }
        if (this.subEvents.offer(timeEvent)) {
            timeEvent.start(str, str2, j, getStartNano(), getStartMs());
            return timeEvent;
        }
        Log.error(LABEL, "event start offer new event to queue failed.", new Object[0]);
        return null;
    }

    public void addRecord(TimeRecord timeRecord) {
        Optional.ofNullable(timeRecord).filter(new Predicate() {
            /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeRecord$Waf48cdLzEkcMxoNAkhiLo2dgc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return TimeRecord.this.lambda$addRecord$3$TimeRecord((TimeRecord) obj);
            }
        }).map(new Function() {
            /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeRecord$WdLr7riB8Ri67XpT7A2iQjpjxt0 */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return TimeRecord.this.lambda$addRecord$4$TimeRecord((TimeRecord) obj);
            }
        });
    }

    public /* synthetic */ Boolean lambda$addRecord$4$TimeRecord(TimeRecord timeRecord) {
        return Boolean.valueOf(this.subEvents.offer(timeRecord));
    }

    public void eventDone(TimeEvent timeEvent) {
        if (timeEvent == null) {
            Log.error(LABEL, "event done meets null event.", new Object[0]);
        } else {
            timeEvent.done();
        }
    }

    public StringBuilder getRunTimeDescription() {
        return this.runTimeDescription;
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.subEvents.size();
    }

    /* access modifiers changed from: package-private */
    public boolean isCallerAvailable() {
        return this.caller.get() != null;
    }

    private int getDuration() {
        if (getState() == State.DONE || getState() == State.FORCE_DONE) {
            return (int) (super.getEndNano() - getStartNano());
        }
        return 0;
    }

    static /* synthetic */ boolean lambda$getFinalDone$5(TimeEvent timeEvent) {
        return timeEvent instanceof TimeRecord;
    }

    /* access modifiers changed from: package-private */
    public long getFinalDone() {
        Optional<TimeEvent> max = this.subEvents.stream().filter($$Lambda$TimeRecord$yn_tPDW5PxDC2Z3ykUuqOuT0kq4.INSTANCE).max(Comparator.comparing($$Lambda$TimeRecord$LmCrKbpkX1DMUMNAHC3GfRBjJmM.INSTANCE));
        if (!max.isPresent()) {
            return super.getEndNano();
        }
        if (max.get().getDurationNano() > super.getDurationNano()) {
            return max.get().getEndNano();
        }
        return super.getEndNano();
    }

    private void dumpMainEventOnly(String str, PrintWriter printWriter) {
        super.dump(str, printWriter);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.aafwk.utils.dfx.time.TimeEvent
    public void dump(String str, PrintWriter printWriter) {
        if (str != null && printWriter != null) {
            super.dump(str, printWriter);
            this.subEvents.forEach(new Consumer(str, printWriter) {
                /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeRecord$44Xfljqb2MclTmhaoL7JvDDogDk */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TimeRecord.lambda$dump$7(this.f$0, this.f$1, (TimeEvent) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$dump$7(String str, PrintWriter printWriter, TimeEvent timeEvent) {
        if (timeEvent instanceof TimeRecord) {
            ((TimeRecord) timeEvent).dumpMainEventOnly(str, printWriter);
            return;
        }
        timeEvent.dump(str + Log.getDumpPrefix() + Log.getDumpPrefix(), printWriter);
    }
}
