package ohos.aafwk.utils.dfx.time;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeEvent {
    static final int INVALID_TIME = -1;
    private static final String INVALID_TIME_STRING = "Invalid Time";
    private String caller;
    private String description;
    private long durationNano = -1;
    private long endNano = -1;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private boolean isDone;
    private long mainStartNano = -1;
    private long startMs = -1;
    private long startNano = -1;
    private TimeEventType type;

    TimeEvent(String str, TimeEventType timeEventType, String str2) {
        this.type = timeEventType;
        this.caller = str2;
        this.description = str;
    }

    /* access modifiers changed from: package-private */
    public TimeEventType getType() {
        return this.type;
    }

    /* access modifiers changed from: package-private */
    public void start(String str, String str2, long j, long j2, long j3) {
        if (str != null) {
            this.description = str;
        }
        this.caller = str2;
        this.startMs = j3;
        this.mainStartNano = j2;
        this.startNano = j;
        this.isDone = false;
    }

    /* access modifiers changed from: package-private */
    public long getEndNano() {
        return this.endNano;
    }

    /* access modifiers changed from: package-private */
    public long getStartMs() {
        return this.startMs;
    }

    /* access modifiers changed from: package-private */
    public long getStartNano() {
        return this.startNano;
    }

    /* access modifiers changed from: package-private */
    public long getDurationNano() {
        return this.durationNano;
    }

    /* access modifiers changed from: package-private */
    public void updateDone(long j) {
        this.endNano = j;
        this.durationNano = j - this.startNano;
    }

    public boolean done(long j) {
        if (!this.isDone) {
            updateDone(j);
            this.isDone = true;
        }
        return true;
    }

    public boolean done() {
        return done(System.nanoTime());
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.isDone = false;
        this.startMs = -1;
        this.startNano = -1;
        this.mainStartNano = -1;
        this.durationNano = -1;
        this.endNano = -1;
    }

    /* access modifiers changed from: package-private */
    public void dump(String str, PrintWriter printWriter) {
        if (str != null && printWriter != null) {
            long j = this.startMs;
            String format2 = j != -1 ? this.format.format(new Date(j)) : INVALID_TIME_STRING;
            if (this instanceof TimeRecord) {
                TimeRecord timeRecord = (TimeRecord) this;
                printWriter.println(str + "MainEvent: " + this.type);
                printWriter.println(str + "EventState: " + timeRecord.getState());
                printWriter.println(str + "caller: " + this.caller);
                printWriter.println(str + "Available: " + timeRecord.isCallerAvailable());
                if (timeRecord.getRunTimeDescription().length() > 0) {
                    printWriter.println(str + "Runtime Msg: " + timeRecord.getRunTimeDescription().toString());
                }
                printWriter.println(str + "Sub Event Count:  " + timeRecord.size());
            } else {
                printWriter.println(str + "SubEvent:  " + this.type);
                printWriter.println(str + "caller: " + this.caller);
            }
            printWriter.println(str + "Start: " + format2);
            printWriter.println(str + "Description:   " + this.description);
            if (this.mainStartNano != -1) {
                printWriter.println(str + "Main Start Nano:    " + this.mainStartNano);
            }
            printWriter.println(str + "Start Nano:    " + this.startNano);
            printWriter.println(str + "Done Nano:     " + this.endNano);
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("Duration(ms):  ");
            printWriter.print(sb.toString());
            long j2 = this.durationNano;
            if (j2 >= 0) {
                printWriter.format("%.3f%n", Float.valueOf(((float) j2) / 1000000.0f));
            } else {
                printWriter.println(INVALID_TIME_STRING);
            }
        }
    }
}
