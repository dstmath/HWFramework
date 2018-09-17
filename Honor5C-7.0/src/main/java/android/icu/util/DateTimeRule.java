package android.icu.util;

import android.icu.impl.Grego;
import java.io.Serializable;

public class DateTimeRule implements Serializable {
    public static final int DOM = 0;
    public static final int DOW = 1;
    private static final String[] DOWSTR = null;
    public static final int DOW_GEQ_DOM = 2;
    public static final int DOW_LEQ_DOM = 3;
    private static final String[] MONSTR = null;
    public static final int STANDARD_TIME = 1;
    public static final int UTC_TIME = 2;
    public static final int WALL_TIME = 0;
    private static final long serialVersionUID = 2183055795738051443L;
    private final int dateRuleType;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int millisInDay;
    private final int month;
    private final int timeRuleType;
    private final int weekInMonth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.DateTimeRule.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.DateTimeRule.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.DateTimeRule.<clinit>():void");
    }

    public DateTimeRule(int month, int dayOfMonth, int millisInDay, int timeType) {
        this.dateRuleType = WALL_TIME;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.dayOfWeek = WALL_TIME;
        this.weekInMonth = WALL_TIME;
    }

    public DateTimeRule(int month, int weekInMonth, int dayOfWeek, int millisInDay, int timeType) {
        this.dateRuleType = STANDARD_TIME;
        this.month = month;
        this.weekInMonth = weekInMonth;
        this.dayOfWeek = dayOfWeek;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.dayOfMonth = WALL_TIME;
    }

    public DateTimeRule(int month, int dayOfMonth, int dayOfWeek, boolean after, int millisInDay, int timeType) {
        this.dateRuleType = after ? UTC_TIME : DOW_LEQ_DOM;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.weekInMonth = WALL_TIME;
    }

    public int getDateRuleType() {
        return this.dateRuleType;
    }

    public int getRuleMonth() {
        return this.month;
    }

    public int getRuleDayOfMonth() {
        return this.dayOfMonth;
    }

    public int getRuleDayOfWeek() {
        return this.dayOfWeek;
    }

    public int getRuleWeekInMonth() {
        return this.weekInMonth;
    }

    public int getTimeRuleType() {
        return this.timeRuleType;
    }

    public int getRuleMillisInDay() {
        return this.millisInDay;
    }

    public String toString() {
        String sDate = null;
        String sTimeRuleType = null;
        switch (this.dateRuleType) {
            case WALL_TIME /*0*/:
                sDate = Integer.toString(this.dayOfMonth);
                break;
            case STANDARD_TIME /*1*/:
                sDate = Integer.toString(this.weekInMonth) + DOWSTR[this.dayOfWeek];
                break;
            case UTC_TIME /*2*/:
                sDate = DOWSTR[this.dayOfWeek] + ">=" + Integer.toString(this.dayOfMonth);
                break;
            case DOW_LEQ_DOM /*3*/:
                sDate = DOWSTR[this.dayOfWeek] + "<=" + Integer.toString(this.dayOfMonth);
                break;
        }
        switch (this.timeRuleType) {
            case WALL_TIME /*0*/:
                sTimeRuleType = "WALL";
                break;
            case STANDARD_TIME /*1*/:
                sTimeRuleType = "STD";
                break;
            case UTC_TIME /*2*/:
                sTimeRuleType = "UTC";
                break;
        }
        int time = this.millisInDay;
        int millis = time % Grego.MILLIS_PER_SECOND;
        time /= Grego.MILLIS_PER_SECOND;
        int secs = time % 60;
        time /= 60;
        int mins = time % 60;
        int hours = time / 60;
        StringBuilder buf = new StringBuilder();
        buf.append("month=");
        buf.append(MONSTR[this.month]);
        buf.append(", date=");
        buf.append(sDate);
        buf.append(", time=");
        buf.append(hours);
        buf.append(":");
        buf.append(mins / 10);
        buf.append(mins % 10);
        buf.append(":");
        buf.append(secs / 10);
        buf.append(secs % 10);
        buf.append(".");
        buf.append(millis / 100);
        buf.append((millis / 10) % 10);
        buf.append(millis % 10);
        buf.append("(");
        buf.append(sTimeRuleType);
        buf.append(")");
        return buf.toString();
    }
}
