package com.huawei.zxing.client.result;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CalendarParsedResult extends ParsedResult {
    private static final DateFormat DATE_FORMAT = null;
    private static final Pattern DATE_TIME = null;
    private static final DateFormat DATE_TIME_FORMAT = null;
    private static final Pattern RFC2445_DURATION = null;
    private static final long[] RFC2445_DURATION_FIELD_UNITS = null;
    private final String[] attendees;
    private final String description;
    private final Date end;
    private final boolean endAllDay;
    private final double latitude;
    private final String location;
    private final double longitude;
    private final String organizer;
    private final Date start;
    private final boolean startAllDay;
    private final String summary;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.client.result.CalendarParsedResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.client.result.CalendarParsedResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.client.result.CalendarParsedResult.<clinit>():void");
    }

    public CalendarParsedResult(String summary, String startString, String endString, String durationString, String location, String organizer, String[] attendees, String description, double latitude, double longitude) {
        super(ParsedResultType.CALENDAR);
        this.summary = summary;
        try {
            this.start = parseDate(startString);
            if (endString == null) {
                long durationMS = parseDurationMS(durationString);
                this.end = durationMS < 0 ? null : new Date(this.start.getTime() + durationMS);
            } else {
                try {
                    this.end = parseDate(endString);
                } catch (ParseException pe) {
                    throw new IllegalArgumentException(pe.toString());
                }
            }
            this.startAllDay = startString.length() == 8;
            boolean z = endString != null && endString.length() == 8;
            this.endAllDay = z;
            this.location = location;
            this.organizer = organizer;
            this.attendees = attendees;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
        } catch (ParseException pe2) {
            throw new IllegalArgumentException(pe2.toString());
        }
    }

    public String getSummary() {
        return this.summary;
    }

    public Date getStart() {
        return this.start;
    }

    public boolean isStartAllDay() {
        return this.startAllDay;
    }

    public Date getEnd() {
        return this.end;
    }

    public boolean isEndAllDay() {
        return this.endAllDay;
    }

    public String getLocation() {
        return this.location;
    }

    public String getOrganizer() {
        return this.organizer;
    }

    public String[] getAttendees() {
        return this.attendees;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(100);
        ParsedResult.maybeAppend(this.summary, result);
        ParsedResult.maybeAppend(format(this.startAllDay, this.start), result);
        ParsedResult.maybeAppend(format(this.endAllDay, this.end), result);
        ParsedResult.maybeAppend(this.location, result);
        ParsedResult.maybeAppend(this.organizer, result);
        ParsedResult.maybeAppend(this.attendees, result);
        ParsedResult.maybeAppend(this.description, result);
        return result.toString();
    }

    private static Date parseDate(String when) throws ParseException {
        if (!DATE_TIME.matcher(when).matches()) {
            throw new ParseException(when, 0);
        } else if (when.length() == 8) {
            return DATE_FORMAT.parse(when);
        } else {
            Date date;
            if (when.length() == 16 && when.charAt(15) == 'Z') {
                date = DATE_TIME_FORMAT.parse(when.substring(0, 15));
                Calendar calendar = new GregorianCalendar();
                long milliseconds = date.getTime() + ((long) calendar.get(15));
                calendar.setTime(new Date(milliseconds));
                date = new Date(milliseconds + ((long) calendar.get(16)));
            } else {
                date = DATE_TIME_FORMAT.parse(when);
            }
            return date;
        }
    }

    private static String format(boolean allDay, Date date) {
        if (date == null) {
            return null;
        }
        DateFormat format;
        if (allDay) {
            format = DateFormat.getDateInstance(2);
        } else {
            format = DateFormat.getDateTimeInstance(2, 2);
        }
        return format.format(date);
    }

    private static long parseDurationMS(CharSequence durationString) {
        if (durationString == null) {
            return -1;
        }
        Matcher m = RFC2445_DURATION.matcher(durationString);
        if (!m.matches()) {
            return -1;
        }
        long durationMS = 0;
        for (int i = 0; i < RFC2445_DURATION_FIELD_UNITS.length; i++) {
            String fieldValue = m.group(i + 1);
            if (fieldValue != null) {
                durationMS += RFC2445_DURATION_FIELD_UNITS[i] * ((long) Integer.parseInt(fieldValue));
            }
        }
        return durationMS;
    }
}
