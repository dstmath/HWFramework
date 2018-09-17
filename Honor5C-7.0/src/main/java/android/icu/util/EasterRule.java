package android.icu.util;

import java.util.Date;

/* compiled from: EasterHoliday */
class EasterRule implements DateRule {
    private static GregorianCalendar gregorian;
    private static GregorianCalendar orthodox;
    private GregorianCalendar calendar;
    private int daysAfterEaster;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.EasterRule.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.EasterRule.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.EasterRule.<clinit>():void");
    }

    public EasterRule(int daysAfterEaster, boolean isOrthodox) {
        this.calendar = gregorian;
        this.daysAfterEaster = daysAfterEaster;
        if (isOrthodox) {
            orthodox.setGregorianChange(new Date(Long.MAX_VALUE));
            this.calendar = orthodox;
        }
    }

    public Date firstAfter(Date start) {
        return doFirstBetween(start, null);
    }

    public Date firstBetween(Date start, Date end) {
        return doFirstBetween(start, end);
    }

    public boolean isOn(Date date) {
        boolean z;
        synchronized (this.calendar) {
            this.calendar.setTime(date);
            int dayOfYear = this.calendar.get(6);
            this.calendar.setTime(computeInYear(this.calendar.getTime(), this.calendar));
            z = this.calendar.get(6) == dayOfYear;
        }
        return z;
    }

    public boolean isBetween(Date start, Date end) {
        return firstBetween(start, end) != null;
    }

    private Date doFirstBetween(Date start, Date end) {
        synchronized (this.calendar) {
            Date result = computeInYear(start, this.calendar);
            if (result.before(start)) {
                this.calendar.setTime(start);
                this.calendar.get(1);
                this.calendar.add(1, 1);
                result = computeInYear(this.calendar.getTime(), this.calendar);
            }
            if (end == null || result.before(end)) {
                return result;
            }
            return null;
        }
    }

    private Date computeInYear(Date date, GregorianCalendar cal) {
        Date time;
        if (cal == null) {
            cal = this.calendar;
        }
        synchronized (cal) {
            int i;
            int j;
            cal.setTime(date);
            int year = cal.get(1);
            int g = year % 19;
            if (cal.getTime().after(cal.getGregorianChange())) {
                int c = year / 100;
                int h = ((((c - (c / 4)) - (((c * 8) + 13) / 25)) + (g * 19)) + 15) % 30;
                i = h - ((h / 28) * (1 - (((h / 28) * (29 / (h + 1))) * ((21 - g) / 11))));
                j = ((((((year / 4) + year) + i) + 2) - c) + (c / 4)) % 7;
            } else {
                i = ((g * 19) + 15) % 30;
                j = (((year / 4) + year) + i) % 7;
            }
            int l = i - j;
            int m = ((l + 40) / 44) + 3;
            int d = (l + 28) - ((m / 4) * 31);
            cal.clear();
            cal.set(0, 1);
            cal.set(1, year);
            cal.set(2, m - 1);
            cal.set(5, d);
            cal.getTime();
            cal.add(5, this.daysAfterEaster);
            time = cal.getTime();
        }
        return time;
    }
}
