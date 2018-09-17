package android_maps_conflict_avoidance.com.google.common.util;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import java.util.Hashtable;

public class StopwatchStats {
    private static Hashtable instancesByName;
    private Clock clock;
    private int count;
    private final short eventType;
    private int last;
    private String logStatus;
    private int max;
    private int min;
    private String name;
    private long start;
    private long total;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void");
    }

    public StopwatchStats(Clock clock, String name, String logStatus, short eventType) {
        this.min = Integer.MAX_VALUE;
        this.start = -1;
        this.clock = clock;
        this.name = name;
        this.logStatus = logStatus;
        this.eventType = (short) eventType;
        instancesByName.put(name, this);
    }

    public StopwatchStats(String name, String logStatus, short eventType) {
        this(Config.getInstance().getClock(), name, logStatus, eventType);
    }

    public void start() {
        this.start = getCurrentTime();
    }

    public void stop() {
        if (this.start != -1) {
            addSample((int) (getCurrentTime() - this.start));
            this.start = -1;
        }
    }

    private void addSample(int msec) {
        this.last = msec;
        this.total += (long) msec;
        this.count++;
        if (this.min > msec) {
            this.min = msec;
        }
        if (this.max < msec) {
            this.max = msec;
        }
        if (this.logStatus != null && this.eventType != (short) -1) {
            Log.addEvent(this.eventType, this.logStatus, "" + msec);
        }
    }

    public int getAverage() {
        return this.count <= 0 ? 0 : (int) (((this.total + ((long) this.count)) - 1) / ((long) this.count));
    }

    public int getMin() {
        return this.count <= 0 ? 0 : this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getLast() {
        return this.last;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.name != null) {
            sb.append(this.name);
            sb.append(":");
        }
        sb.append(getMin());
        sb.append(",");
        sb.append(getAverage());
        sb.append(",");
        sb.append(getMax());
        sb.append(":");
        sb.append(getLast());
        sb.append(":");
        sb.append(this.total);
        return sb.toString();
    }

    private long getCurrentTime() {
        return this.clock.relativeTimeMillis();
    }
}
