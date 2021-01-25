package ohos.aafwk.utils.dfx.time;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import ohos.aafwk.utils.log.Log;

public class RecordPool {
    private static final int MAX_SIZE = 50;
    private Queue<TimeRecord> records;
    private int size;
    private TimeEventType type;

    /* access modifiers changed from: package-private */
    public void init(int i) {
        if (i <= 0 || i >= 50) {
            throw new IllegalArgumentException("size exceed limit: " + i);
        }
        this.size = i;
        clear();
        this.records = new ArrayDeque(i);
    }

    /* access modifiers changed from: package-private */
    public Optional<TimeRecord> getRecord(Object obj, TimeEventType timeEventType, String str) {
        TimeRecord timeRecord;
        Objects.requireNonNull(timeEventType, "type is null");
        TimeEventType timeEventType2 = this.type;
        if (timeEventType2 == null) {
            this.type = timeEventType;
        } else if (timeEventType2 != timeEventType) {
            throw new IllegalArgumentException("type not match when get record. " + this.type + " vs " + timeEventType);
        }
        if (this.records == null) {
            init(this.size);
        }
        if (this.records.size() <= this.size - 1) {
            timeRecord = new TimeRecord(timeEventType, str, obj);
            if (!this.records.offer(timeRecord)) {
                Log.error("get record error, offer to pool failed. pool size: %{public}d", Integer.valueOf(this.records.size()));
                return Optional.empty();
            }
        } else {
            timeRecord = this.records.poll();
            if (timeRecord == null) {
                Log.error("get record error, poll from pool failed.", new Object[0]);
                return Optional.empty();
            } else if (!this.records.offer(timeRecord)) {
                Log.error("get record error, offer to pool failed.", new Object[0]);
                return Optional.empty();
            }
        }
        return Optional.of(timeRecord);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        Queue<TimeRecord> queue = this.records;
        if (queue != null) {
            queue.forEach($$Lambda$RecordPool$caKHU74EumXIhpOdvpE1D7zTXac.INSTANCE);
            this.records.removeIf($$Lambda$RecordPool$hpt7UUkeLit_deQU6ju_eZ2KMY0.INSTANCE);
        }
    }

    static /* synthetic */ boolean lambda$clear$1(TimeRecord timeRecord) {
        return timeRecord.getType() != TimeEventType.ANONYMOUS;
    }

    /* access modifiers changed from: package-private */
    public void clearDeadRecords() {
        Queue<TimeRecord> queue = this.records;
        if (queue != null) {
            queue.forEach($$Lambda$RecordPool$dk3z7wIuny4GFDC83IPZkFUxs.INSTANCE);
        }
    }

    static /* synthetic */ void lambda$clearDeadRecords$2(TimeRecord timeRecord) {
        if (!timeRecord.isCallerAvailable()) {
            timeRecord.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public int size() {
        Queue<TimeRecord> queue = this.records;
        if (queue == null) {
            return 0;
        }
        return queue.size();
    }

    /* access modifiers changed from: package-private */
    public void forceDone() {
        Queue<TimeRecord> queue = this.records;
        if (queue != null) {
            queue.forEach($$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A.INSTANCE);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(String str, PrintWriter printWriter) {
        if (this.records != null) {
            printWriter.println(str + "Main Event Type: " + this.type);
            printWriter.println(str + "Main Event Count: " + this.records.size());
            this.records.forEach(new Consumer(str, printWriter) {
                /* class ohos.aafwk.utils.dfx.time.$$Lambda$RecordPool$gI5CVMrua2yj9mnVZYQCKRqcDM */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    RecordPool.lambda$dump$4(this.f$0, this.f$1, (TimeRecord) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$dump$4(String str, PrintWriter printWriter, TimeRecord timeRecord) {
        long finalDone = timeRecord.getFinalDone();
        if (finalDone != -1) {
            timeRecord.updateDone(finalDone);
        }
        timeRecord.dump(str + Log.getDumpPrefix(), printWriter);
    }
}
