package android.icu.util;

import java.util.Date;

public class SimpleDateRule implements DateRule {
    private static GregorianCalendar gCalendar;
    private Calendar calendar;
    private int dayOfMonth;
    private int dayOfWeek;
    private int month;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.SimpleDateRule.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.SimpleDateRule.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.SimpleDateRule.<clinit>():void");
    }

    public SimpleDateRule(int month, int dayOfMonth) {
        this.calendar = gCalendar;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = 0;
    }

    SimpleDateRule(int month, int dayOfMonth, Calendar cal) {
        this.calendar = gCalendar;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = 0;
        this.calendar = cal;
    }

    public SimpleDateRule(int month, int dayOfMonth, int dayOfWeek, boolean after) {
        this.calendar = gCalendar;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        if (!after) {
            dayOfWeek = -dayOfWeek;
        }
        this.dayOfWeek = dayOfWeek;
    }

    public Date firstAfter(Date start) {
        return doFirstBetween(start, null);
    }

    public Date firstBetween(Date start, Date end) {
        return doFirstBetween(start, end);
    }

    public boolean isOn(Date date) {
        boolean z = true;
        Calendar c = this.calendar;
        synchronized (c) {
            c.setTime(date);
            int dayOfYear = c.get(6);
            c.setTime(computeInYear(c.get(1), c));
            if (c.get(6) != dayOfYear) {
                z = false;
            }
        }
        return z;
    }

    public boolean isBetween(Date start, Date end) {
        return firstBetween(start, end) != null;
    }

    private Date doFirstBetween(Date start, Date end) {
        Calendar c = this.calendar;
        synchronized (c) {
            c.setTime(start);
            int year = c.get(1);
            int mon = c.get(2);
            if (mon > this.month) {
                year++;
            }
            Date result = computeInYear(year, c);
            if (mon == this.month && result.before(start)) {
                result = computeInYear(year + 1, c);
            }
            if (end == null || !result.after(end)) {
                return result;
            }
            return null;
        }
    }

    private Date computeInYear(int year, Calendar c) {
        Date time;
        synchronized (c) {
            c.clear();
            c.set(0, c.getMaximum(0));
            c.set(1, year);
            c.set(2, this.month);
            c.set(5, this.dayOfMonth);
            if (this.dayOfWeek != 0) {
                int delta;
                c.setTime(c.getTime());
                int weekday = c.get(7);
                if (this.dayOfWeek > 0) {
                    delta = ((this.dayOfWeek - weekday) + 7) % 7;
                } else {
                    delta = -(((this.dayOfWeek + weekday) + 7) % 7);
                }
                c.add(5, delta);
            }
            time = c.getTime();
        }
        return time;
    }
}
